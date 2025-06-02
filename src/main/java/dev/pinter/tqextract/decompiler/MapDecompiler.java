/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Extract.

    TQ Extract is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Extract is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Extract.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.pinter.tqextract.decompiler;

import br.com.pinter.tqdatabase.data.RandomAccessFileLE;
import br.com.pinter.tqrespec.logging.Log;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.pinter.tqextract.decompiler.MapBlock.Type.INSTANCEDATA;
import static dev.pinter.tqextract.decompiler.MapBlock.Type.LEVELS;
import static dev.pinter.tqextract.decompiler.MapBlock.Type.MINIMAP;
import static dev.pinter.tqextract.decompiler.MapBlock.Type.QUESTS;
import static dev.pinter.tqextract.decompiler.MapBlock.Type.SECTOR;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

public class MapDecompiler {
    private static final System.Logger logger = Log.getLogger(MapDecompiler.class);

    private final Map<Integer, MapBlock> mapBlocks;
    private final Path worldMap;
    private final Path outputDir;
    private final float wrlTgaScaling;

    /**
     * Creates an instance to decompile de map to the outputdir.
     *
     * @param worldMap      Map file
     * @param outputDir     Directory to write the files
     * @param wrlTgaScaling Scaling of images written to WRL, used in the layout mode of the Editor.
     *                      1 equals to 1/4 of the image, 2 is half, 4 is full size. Bigger images will
     *                      make the editor crash.
     * @throws IOException if I/O error happens
     */
    public MapDecompiler(Path worldMap, Path outputDir, int wrlTgaScaling) throws IOException {
        this.worldMap = worldMap;
        this.outputDir = outputDir;
        this.wrlTgaScaling = 0.25f * wrlTgaScaling;
        this.mapBlocks = searchBlocks();
    }

    /**
     * Creates an instance to decompile de map to the outputdir.
     *
     * @param outputDir directory to write the files
     * @throws IOException if I/O error happens
     */
    public MapDecompiler(Path worldMap, Path outputDir) throws IOException {
        this(worldMap, outputDir, 1);
    }

    public void extractAll() throws IOException {
        List<MapLevel> levels = searchLevels();
        createLevelFiles(levels);
        createTgaFiles(levels);
        createSd();
        createWorldWrl(levels);
    }

    public void createTgaFiles() throws IOException {
        createTgaFiles(searchLevels());
    }

    public void createLevelFiles() throws IOException {
        createLevelFiles(searchLevels());
    }

    public void createSd() throws IOException {
        Path sdPath = Path.of(outputDir.toString(), "Levels", "World", "world01.sd");
        if (sdPath.getParent().toFile().mkdirs() && !Files.exists(sdPath.getParent())) {
            throw new IOException("Erro creating destination directory");
        }

        MapBlock sector = mapBlocks.get(SECTOR.value());

        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            map.seek(sector.start());

            try (RandomAccessFileLE out = new RandomAccessFileLE(sdPath.toString(), "rw")) {
                map.getChannel().transferTo(map.getFilePointer(), sector.size(), out.getChannel());
                logger.log(INFO, "SD ''{0}'' created with ''{1}'' bytes", sdPath, out.length());
            }
        }
    }

    private Map<Integer, MapBlock> searchBlocks() throws IOException {
        final Map<Integer, MapBlock> blocks = new HashMap<>();
        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            //test magic-number("ARC") and size
            byte[] magicNumber = new byte[3];

            map.readFully(magicNumber, 0, magicNumber.length);
            map.seek(map.getFilePointer() + 1);
            if (Arrays.compare(magicNumber, new byte[]{0x4D, 0x41, 0x50}) != 0
                    || map.length() < 33) {
                throw new IOException(String.format("Invalid file '%s' (length=%s)", worldMap, map.length()));
            }
            int blockId;
            int blockSize;
            int blockStart;

            //ignore first byte
            map.readIntLE();

            do {
                blockId = map.readIntLE();
                blockSize = map.readIntLE();
                blockStart = (int) map.getFilePointer();

                logger.log(INFO, "[Block {0}({1})] length:{2}; start:{3}; end:{4};\n",
                        MapBlock.Type.of(blockId),
                        "0x%02X".formatted(blockId),
                        "% 10d".formatted(blockSize),
                        "0x%08X".formatted(blockStart),
                        "0x%08X".formatted(blockStart + blockSize - 1));

                blocks.put(blockId, new MapBlock(blockId, blockSize, blockStart));
                map.seek(map.getFilePointer() + blockSize);
            }
            while (map.getFilePointer() + blockSize < map.length());
        }
        return blocks;
    }

    private List<MapLevel> searchLevels() throws IOException {
        List<MapLevel> levelFileNames = new ArrayList<>();
        MapBlock levels = mapBlocks.get(LEVELS.value());
        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            map.seek(levels.start());
            int levelCount = map.readIntLE();

            for (int i = 0; i < levelCount; i++) {
                byte[] data = new byte[13 * 4];
                map.readFully(data);
                String recordName = map.readPrefixedString();
                String levelFileName = map.readPrefixedString();
                int levelOffset = map.readIntLE();
                int levelSize = map.readIntLE();

                levelFileNames.add(i, new MapLevel(i, levelFileName, recordName, data, levelOffset, levelSize));
            }
        }
        return levelFileNames;
    }

    private Map<Integer, MapTga> searchTga() throws IOException {
        Map<Integer, MapTga> images = new HashMap<>();
        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            MapBlock minimap = mapBlocks.get(MINIMAP.value());
            map.seek(minimap.start());
            map.readIntLE();//ignore first byte
            int count = map.readIntLE();
            for (int i = 0; i < count; i++) {
                int offset = map.readIntLE();
                int length = map.readIntLE();
                short dataType = map.readInt8LE(offset + 2);
                short bitsPerPixel = map.readInt8LE(offset + 16);
                short width = map.readShortLE(offset + 12);
                short height = map.readShortLE(offset + 14);
                short newWidth = calcDimension(wrlTgaScaling, width);
                short newHeight = calcDimension(wrlTgaScaling, height);

                if (!(newWidth % 2 == 0) || !(newHeight % 2 == 0)) {
                    logger.log(WARNING, "WARNING: New width or height of TGA are not multiple of 2 ({0}x{1})", newWidth, newHeight);
                }

                if (width < 0 || height < 0) {
                    throw new IllegalArgumentException("invalid image");
                }
                int size = (((newWidth) * (newHeight) * 3) + 18); //scaling
                if (length <= 0) {
                    size = 0;
                }

                images.put(i, new MapTga(i, width, height, size, offset, length, dataType, bitsPerPixel, newWidth, newHeight));
            }
        }
        return images;
    }

    private void writeWrlQuests(RandomAccessFileLE map, Path wrlPath) throws IOException {
        //quests
        MapBlock quests = mapBlocks.get(QUESTS.value());
        map.seek(quests.start());
        int stringCount = map.readIntLE();
        for (int i = 0; i < stringCount; i++) {
            logger.log(DEBUG, "[Block {0} {1}", QUESTS, map.readPrefixedString());
        }
        try (RandomAccessFileLE out = new RandomAccessFileLE(wrlPath.toString(), "rw")) {
            out.seek(out.length());
            out.writeIntLE(quests.id());
            out.writeIntLE(quests.size());
            if (map.getChannel().transferTo(quests.start(), quests.size(), out.getChannel()) != quests.size()) {
                throw new IOException("Error writing data");
            }
        }
    }

    private void writeWrlInstanceData(RandomAccessFileLE map, Path wrlPath) throws IOException {
        //instance data
        MapBlock instData = mapBlocks.get(INSTANCEDATA.value());
        try (RandomAccessFileLE out = new RandomAccessFileLE(wrlPath.toString(), "rw")) {
            out.seek(out.length());
            out.writeIntLE(instData.id());
            out.writeIntLE(instData.size());
            if (map.getChannel().transferTo(instData.start(), instData.size(), out.getChannel()) != instData.size()) {
                throw new IOException("Error writing data");
            }
        }
    }

    private void writeWrlLevels(Path wrlPath, List<MapLevel> levels) throws IOException {
        try (RandomAccessFileLE out = new RandomAccessFileLE(wrlPath.toString(), "rw")) {
            out.seek(out.length());
            int levelCount = levels.size();
            out.writeIntLE(0x13);
            out.writeIntLE(mapBlocks.get(LEVELS.value()).size() - (levelCount * 8));
            out.writeIntLE(levelCount);

            for (MapLevel lvl : levels) {
                out.writePrefixedString(lvl.fileName());
                for (int f = 0; f < 6; f++) {
                    float n = ByteBuffer.allocate(4)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .put(lvl.data(), f * 4, 4).rewind()
                            .getInt();

                    byte[] d = new byte[4];
                    ByteBuffer.allocate(4)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .putFloat(n).rewind()
                            .get(d);
                    System.arraycopy(d, 0, lvl.data(), (f * 4), d.length);
                }
                out.write(lvl.data());
                out.writePrefixedString(lvl.recordName());
            }
        }
    }

    private void createWorldWrl(List<MapLevel> levels) throws IOException {
        Path wrlPath = Path.of(outputDir.toString(), "Levels", "World", "world01.wrl");
        if (!wrlPath.getParent().toFile().mkdirs() && !Files.exists(wrlPath.getParent())) {
            throw new IOException("Error creating directory " + wrlPath.getParent());
        }
        //write magic with version 6, legacy map versions are below 5
        Files.write(wrlPath, new byte[]{0x57, 0x52, 0x4C, 0x06},
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        Map<Integer, MapTga> images = searchTga();

        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            writeWrlQuests(map, wrlPath);
            writeWrlInstanceData(map, wrlPath);
            writeWrlLevels(wrlPath, levels);

            try (RandomAccessFileLE out = new RandomAccessFileLE(wrlPath.toString(), "rw")) {
                out.seek(out.length());

                AtomicInteger total = new AtomicInteger(0);
                images.values().forEach(m -> total.addAndGet(m.size() + 16));
                out.writeIntLE(0x15);
                out.writeIntLE(total.get());

                for (var e : images.entrySet()) {
                    MapTga tga = e.getValue();
                    String filename = levels.get(e.getValue().id()).fileName();
                    if (filename == null) {
                        throw new IllegalStateException("Level data not found");
                    }

                    out.writeIntLE(0);
                    out.writeIntLE(tga.newWidth()); //scaling
                    out.writeIntLE(tga.newHeight());
                    out.writeIntLE(tga.size());

                    byte[] scaledTga = resizeTga(map.get(tga.srcOffset(), tga.srcLength()), tga.newWidth(), tga.newHeight());
                    out.write(scaledTga);
                }
                out.getChannel().force(true);
                logger.log(INFO, "WRL ''{0}'' created with ''{1}'' bytes", wrlPath, out.length());
            }
        }
    }

    private short calcDimension(double desiredFactor, short currentSize) {
        int first = (int) Math.round(currentSize * desiredFactor);
        if (first % 2 != 0) {
            first = first & ~1;
        }
        double factor = first / ((double) currentSize);
        return (short) Math.round(currentSize * factor);
    }

    private byte[] resizeTga(byte[] data, short newWidth, short newHeight) {
        if (data.length == 0) {
            return new byte[0];
        }
        ByteBuffer tga = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        short width = tga.getShort(12);
        short height = tga.getShort(14);
        short dataType = tga.getShort(2);
        short bitsPerPixel = tga.getShort(16);
        int[] pixels = new int[width * height];
        if (dataType != 2) {
            throw new IllegalStateException("Unsupported TGA dataType: " + dataType);
        }
        if (bitsPerPixel != 24) {
            throw new IllegalStateException("Unsupported TGA bpp: " + bitsPerPixel);
        }
        tga.position(18);
        for (int i = 0; i < pixels.length; i++) {
            int red = Byte.toUnsignedInt(tga.get());
            int green = Byte.toUnsignedInt(tga.get()) << 8;
            int blue = Byte.toUnsignedInt(tga.get()) << 16;
            int alpha = 255 << 24;
            pixels[i] = alpha | red | green | blue;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(pixels, pixels.length, 0), new Point()));

        double wScale = (double) newWidth / width;
        double hScale = (double) newHeight / height;

        tga.putShort(12, newWidth);
        tga.putShort(14, newHeight);
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        AffineTransformOp at = new AffineTransformOp(AffineTransform.getScaleInstance(wScale, hScale), AffineTransformOp.TYPE_BICUBIC);
        scaledImage = at.filter(image, scaledImage);
        int[] scaledBuf = ((DataBufferInt) scaledImage.getRaster().getDataBuffer()).getData();
        ByteBuffer out1 = ByteBuffer.allocate((newWidth * newHeight * 3) + 18).order(ByteOrder.LITTLE_ENDIAN);
        out1.put(data, 0, 18);
        for (int px : scaledBuf) {
            int blue = (px) & 0xff;
            int green = (px >> 8) & 0xff;
            int red = (px >> 16) & 0xff;
            out1.put((byte) (blue));
            out1.put((byte) (green));
            out1.put((byte) (red));
        }

        return out1.array();
    }

    private void createTgaFiles(List<MapLevel> levels) throws IOException {
        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            Map<Integer, MapTga> images = searchTga();
            for (var e : images.entrySet()) {
                MapTga tga = e.getValue();
                String filename = levels.get(e.getValue().id()).fileName();
                if (filename == null) {
                    throw new IllegalStateException("Level data not found");
                }
                filename = filename.replaceAll("(?i)(.*)\\.lvl", "$1.tga");
                Path levelFilePath = Path.of(filename.replace("\\", File.separator).replace("/", File.separator));
                Path dest = Path.of(outputDir.toString(), levelFilePath.getParent().toString());
                if (dest.toFile().mkdirs() && !Files.exists(dest)) {
                    throw new IOException("Erro creating destination directory");
                }
                logger.log(INFO, "[Block {0}] size:{1}; offset:{2}; end:{3}}; file:{4}",
                        MINIMAP,
                        "% 10d".formatted(tga.srcLength()),
                        "0x%08X".formatted(tga.srcOffset()),
                        "0x%08X".formatted(tga.srcOffset() + tga.srcLength() - 1),
                        filename);
                Files.write(Path.of(dest.toString(), levelFilePath.getFileName().toString()), map.get(tga.srcOffset(), tga.srcLength()));
            }
        }
    }

    private void createLevelFiles(List<MapLevel> levels) throws IOException {
        for (MapLevel lvl : levels) {
            logger.log(INFO, "[Block {0}] size:{1}; offset:{2}; end:{3}}; file:{4}",
                    LEVELS,
                    "% 10d".formatted(lvl.length()),
                    "0x%08X".formatted(lvl.offset()),
                    "0x%08X".formatted(lvl.offset() + lvl.length() - 1),
                    lvl.fileName());

            Path levelFilePath = Path.of(lvl.fileName().replace("\\", File.separator).replace("/", File.separator));

            Path dest = Path.of(outputDir.toString(), levelFilePath.getParent().toString());
            if (dest.toFile().mkdirs() && !Files.exists(dest)) {
                throw new IOException("Erro creating destination directory");
            }

            createRlv(dest, levelFilePath, lvl);
            createLvl(dest, levelFilePath, lvl);
        }
    }

    private void createLvl(Path dest, Path levelFilePath, MapLevel mapLevel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(mapLevel.length()).order(ByteOrder.LITTLE_ENDIAN);
        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            map.getChannel().read(buf, mapLevel.offset());
            buf.rewind();
            Path destLvl = Path.of(dest.toString(), levelFilePath.getFileName().toString().replaceAll("(?i)(.*)\\.lvl", "$1.lvl"));
            byte[] magic = new byte[4];
            buf.get(magic);
            Files.write(destLvl, magic);
            while (buf.hasRemaining()) {
                // read 4 bytes to check for level block start
                // value 5 is found at start of file, 6 at start of terrain data
                int blockType = buf.getInt();
                int length = buf.getInt();
                int start = buf.position();
                int dataType = buf.getInt();
                int dataFirstByte = buf.getInt();
                if (blockType == 0x06 && dataType == 0x02 && dataFirstByte == 0x01) { //current position has terrain data
                    int dbrCount = buf.getInt();
                    int width = buf.getInt();
                    int height = buf.getInt();
                    int section1Size = width * height * 4;
                    int section2Size = width * height * ((dbrCount / 8) + 1);

                    // terrainruntimeformat header, 0x02 + 32 bytes
                    // skip 9 integers after the length, since we already read 5 we skip the 4 remaining
                    buf.position(buf.position() + 16);
                    int section1Start = buf.position();

                    //skip the two sections
                    buf.position(buf.position() + section1Size);
                    buf.position(buf.position() + section2Size);

                    int section3Start = buf.position();

                    int section3Size = 0;

                    int d = 0;
                    do {
                        int strLen = buf.getInt();
                        // add the string length
                        section3Size += 4 + strLen;

                        //skip the string
                        buf.position(buf.position() + strLen);
                        if (d != 0) {
                            section3Size += (width - 1) * (height - 1);
                            buf.position(buf.position() + ((width - 1) * (height - 1)));
                        }
                        d++;
                    } while (d < dbrCount);

                    int lvlBlockSize = 16 + section3Size + (section1Size * 2) + (section1Size * 3) + ((width - 1) * (height - 1) * 4);

                    try (RandomAccessFileLE lvl = new RandomAccessFileLE(destLvl.toString(), "rw")) {
                        lvl.seek(lvl.length());
                        lvl.writeIntLE(blockType);
                        lvl.writeIntLE(lvlBlockSize);
                        lvl.writeIntLE(0);
                        lvl.writeIntLE(width);
                        lvl.writeIntLE(height);
                        lvl.writeIntLE(dbrCount);
                        //write section3
                        if (lvl.getChannel().write(buf.slice(section3Start, section3Size)) == 0) {
                            throw new IOException("Error writing lvl");
                        }
                        int chunks = section1Size / 4;
                        ByteBuffer secBuf = ByteBuffer.allocate((8 * chunks) + (12 * chunks)).order(ByteOrder.LITTLE_ENDIAN);
                        for (int i = 0; i < chunks; i++) {
                            secBuf.put(8 * i, buf, section1Start + (4 * i), 4);
                            secBuf.put((8 * chunks) + (12 * i), buf, section1Start + (4 * i), 4);
                        }
                        if (lvl.getChannel().write(secBuf) == 0) {
                            throw new IOException("Error writing lvl");
                        }
                        lvl.write(new byte[(width - 1) * (height - 1) * 4]);
                    }
                } else {
                    // copy data when other types are found (0x05, 0x14, 0x0B, 0x00, 0x01, 0x02, 0x09, 0x03 and 0x17)
                    try (RandomAccessFileLE lvl = new RandomAccessFileLE(destLvl.toString(), "rw")) {
                        lvl.seek(lvl.length());
                        lvl.writeIntLE(blockType);
                        lvl.writeIntLE(length);
                        if (lvl.getChannel().write(buf.slice(start, length)) != length) {
                            throw new IOException("Error writing lvl");
                        }
                    }
                }
                // go to the next offset
                buf.position(start + length);
            }
        }
    }

    private void createRlv(Path dest, Path levelFilePath, MapLevel lvl) throws IOException {
        Path destRlv = Path.of(dest.toString(), levelFilePath.getFileName().toString().replaceAll("(?i)(.*)\\.lvl", "$1.rlv"));

        try (RandomAccessFileLE map = new RandomAccessFileLE(worldMap.toString(), "r")) {
            try (RandomAccessFileLE rlv = new RandomAccessFileLE(destRlv.toString(), "rw")) {
                map.getChannel().transferTo(lvl.offset(), lvl.length(), rlv.getChannel());
            }
        }
    }
}