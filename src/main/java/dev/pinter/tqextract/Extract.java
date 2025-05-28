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

package dev.pinter.tqextract;

import br.com.pinter.tqdatabase.Database;
import br.com.pinter.tqdatabase.Resources;
import br.com.pinter.tqdatabase.data.TextureConverter;
import br.com.pinter.tqdatabase.models.DbRecord;
import br.com.pinter.tqdatabase.models.ResourceFile;
import br.com.pinter.tqdatabase.models.ResourceType;
import br.com.pinter.tqdatabase.models.Texture;
import br.com.pinter.tqdatabase.models.TextureType;
import br.com.pinter.tqrespec.logging.Log;
import dev.pinter.tqextract.decompiler.MapDecompiler;
import dev.pinter.tqextract.image.DDSReader;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.function.TriFunction;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class Extract {
    private static final System.Logger logger = Log.getLogger(Extract.class);
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicInteger arcDoneFutures = new AtomicInteger(0);
    private final List<CompletableFuture<Path>> arcFutures = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executorArc;
    private final ExecutorService executorMap;
    private final ExecutorService executorArz;
    private boolean convertTexToDds = false;
    private boolean convertTexToPng = false;
    private boolean convertTexIgnoreMip = false;
    private boolean decompileMap = true;
    private final Path outputDir;
    private int mapWrlScaling = 0;
    private TriFunction<String, Integer, Integer, Void> progressHandler = (s, c, t) -> null;
    private Function<String, Void> printInfoHandler = s -> null;

    public Extract(Path outputDir, int maxThreads) {
        this(outputDir, Path.of(System.getProperty("java.io.tmpdir"), "tqextract.log"), maxThreads);
    }

    public Extract(Path outputDir, Path logFile, int maxThreads) {
        this.outputDir = outputDir;

        Log.setupGlobalLogging(logFile);

        int threads = Math.max(1, maxThreads);
        executorArc = Executors.newFixedThreadPool(threads);
        executorMap = Executors.newSingleThreadExecutor();
        executorArz = Executors.newFixedThreadPool(threads);

        logger.log(INFO, "Max threads set to {0}", maxThreads);
    }

    public void setProgressHandler(TriFunction<String, Integer, Integer, Void> function) {
        this.progressHandler = function;
    }

    public void setPrintInfoHandler(Function<String, Void> printInfoHandler) {
        this.printInfoHandler = printInfoHandler;
    }

    public void setConvertTexToDds(boolean convertTexToDds) {
        this.convertTexToDds = convertTexToDds;
    }

    public void setConvertTexToPng(boolean convertTexToPng) {
        this.convertTexToPng = convertTexToPng;
    }

    public void setConvertTexIgnoreMip(boolean convertTexIgnoreMip) {
        this.convertTexIgnoreMip = convertTexIgnoreMip;
    }

    public void setDecompileMap(boolean decompileMap) {
        this.decompileMap = decompileMap;
    }

    public void setMapWrlScaling(int scaling) {
        this.mapWrlScaling = scaling > 0 ? scaling : 1;
    }

    public void processDirectory(Path basePath) throws IOException {
        logger.log(INFO, "Files will be extracted to ''{0}''", outputDir);
        printInfo("Files will be extracted to '%s'", outputDir);

        List<Path> arcToExtract = searchFiles(basePath, "arc");
        List<Path> arzToExtract = searchFiles(basePath, "arz");

        for (var file : arcToExtract) {
            Path relativePath = basePath.relativize(file);
            printInfo("Found ARC '%s'", relativePath);
            logger.log(INFO, "Found ARC ''{0}''", relativePath);
            try {
                processArc(file, Path.of(outputDir.toString(), relativePath.toString()), executorArc);
            } catch (IOException ignored) {
            }
        }
        latch.countDown();
        logger.log(INFO, "ARC extraction starting");
        CompletableFuture.allOf(arcFutures.toArray(new CompletableFuture<?>[0]))
                .thenRun(() -> {
                    logger.log(INFO, "ARC extraction finished");
                    printInfo("%nARC extraction finished");
                }).join();
        executorArc.shutdown();
        executorMap.shutdown();
        for (var arz : arzToExtract) {
            extractArz(arz, outputDir);
        }
        executorArz.shutdown();
        waitMessage();
    }

    public void processFile(Path arc) throws IOException {
        logger.log(INFO, "Files will be extracted to ''{0}''", outputDir);
        printInfo("Files will be extracted to '%s'", outputDir);
        printInfo("Found ARC '%s'", arc);
        logger.log(INFO, "Found ARC ''{0}''", arc);
        processArc(arc, Path.of(outputDir.toString(), arc.getParent().relativize(arc).toString()), executorArc);
        latch.countDown();
        logger.log(INFO, "ARC extraction starting");
        CompletableFuture.allOf(arcFutures.toArray(new CompletableFuture<?>[0]))
                .thenRun(() -> {
                    logger.log(INFO, "ARC extraction finished");
                    printInfo("%nARC extraction finished");
                }).join();
        executorArc.shutdown();
        executorMap.shutdown();
        executorArz.shutdown();
        waitMessage();
    }

    public void extractArz(Path arz) throws IOException {
        extractArz(arz, outputDir);
    }

    private void waitMessage() {
        if (!executorArc.isTerminated() || !executorMap.isTerminated()) {
            printInfo("Waiting workers to finish");
        }
    }

    private void processArc(Path file, Path dest, Executor executor) throws IOException {
        Resources rec;
        try {
            rec = new Resources(file);
        } catch (IOException e) {
            logger.log(ERROR, "Error", e);
            throw e;
        }
        for (var entry : rec.getNames()) {
            arcFutures.add(
                    CompletableFuture
                            .supplyAsync(() -> {
                                try {
                                    latch.await();
                                } catch (InterruptedException ignored) {
                                }
                                return extractFile(entry, rec, dest);
                            }, executor)
                            .thenApply(f -> {
                                logger.log(DEBUG, "Extracted: {0}", f);
                                arcDoneFutures.incrementAndGet();
                                progressHandler.apply("ARC", arcDoneFutures.get(), arcFutures.size());
                                return f;
                            }).exceptionally(e -> {
                                logger.log(ERROR, "Error", e);
                                //noinspection CallToPrintStackTrace
                                e.printStackTrace();
                                arcDoneFutures.incrementAndGet();
                                progressHandler.apply("ARC", arcDoneFutures.get(), arcFutures.size());
                                return null;
                            })
            );
        }
    }

    private List<Path> searchFiles(Path path, String ext) throws IOException {
        List<Path> files = new ArrayList<>();

        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().matches(String.format("(?i).*\\.%s$", ext))) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.exists(Path.of(dir.getParent().toString(), "TQ.exe"))
                        && dir.getFileName().toString().equalsIgnoreCase("SteamWorkshop")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(path, fileVisitor);
        return files;
    }

    private void printInfo(String msg, Object... obj) {
        if (this.printInfoHandler != null) {
            this.printInfoHandler.apply(String.format(msg, obj));
        }
    }

    private void extractArz(Path arz, Path dest) throws IOException {
        Database database = new Database(new String[]{arz.toString()}, false);
        printInfo("Extracting '%s' to '%s'", database.getLoadedDb(), dest);
        logger.log(INFO, "Extracting ''{0}'' to ''{1}''", database.getLoadedDb(), dest);
        AtomicInteger count = new AtomicInteger();
        List<CompletableFuture<Path>> arzFutures = Collections.synchronizedList(new ArrayList<>());

        database.processTree(arz, n -> {
            arzFutures.add(
                    CompletableFuture.runAsync(() -> {
                        try {
                            Path dir = Path.of(dest.toString(), n.getName().getParent().toString());
                            if (!dir.toFile().mkdirs()) {
                                if (!Files.exists(dir)) {
                                    throw new RuntimeException("Failed to create directory " + dir);
                                }
                            }
                            Path recordFile = Path.of(dest.toString(), n.getName().toString());
                            if (n.isRecordLoaded()) {
                                Files.writeString(recordFile, String.join(System.lineSeparator(), n.getRecord().asFile()));
                            } else {
                                DbRecord r = database.getRecord(Database.normalizeRecordPath(n.getName().toString()));
                                Files.writeString(recordFile, String.join(System.lineSeparator(), r.asFile()));
                            }
                        } catch (IOException e) {
                            logger.log(ERROR, "Error", e);
                        }
                    }, executorArz).handle((v, e) -> {
                        count.incrementAndGet();
                        progressHandler.apply("ARZ", count.get(), database.getRecordCount());
                        return null;
                    })
            );
            return null;
        });
        CompletableFuture.allOf(arzFutures.toArray(new CompletableFuture<?>[0])).thenRun(() -> {
            printInfo("%nRecords extracted from ARZ '%s': %s", arz.getFileName(), count);
            logger.log(INFO, "Records extracted from ARZ ''{0}'': {1}", arz.getFileName(), count);
        }).join();
    }

    private Path extractFile(String resourceName, Resources rec, Path destDir) {
        Path outfile;
        try {
            ResourceFile entry = rec.getFile(resourceName);
            outfile = Path.of(destDir.toString(), entry.getPath().toString());

            if (outfile.getParent() == null) {
                throw new IllegalArgumentException("Invalid directory " + outfile);
            }

            if (!outfile.getParent().toFile().mkdirs() && !Files.exists(outfile.getParent())) {
                throw new RuntimeException("Failed to create directory " + outfile.getParent());
            }


            if (entry.getResourceType() == ResourceType.TEXTURE) {
                logger.log(DEBUG, "[{0}] Texture found: name:''{1}''; type:{2};",
                        Path.of(rec.getPath().getParent().getFileName().toString(), rec.getPath().getFileName().toString()),
                        entry.getName(), ((Texture) entry).getTextureType());
                extractTexture(entry, destDir);
            } else if (entry.getResourceType() == ResourceType.MAP) {
                Files.write(outfile, entry.getData());
                if (decompileMap) {
                    logger.log(INFO, "Map ''{0}'' found. Starting MapDecompiler.", entry.getPath());
                    printInfo("Map '%s' found. Starting MapDecompiler.", entry.getPath());
                    executorMap.submit(() -> {
                        try {
                            MapDecompiler mapDecompiler = new MapDecompiler(
                                    outfile,
                                    Path.of(outfile.getParent().toString(), "source." + outfile.getFileName().toString()),
                                    mapWrlScaling);
                            mapDecompiler.extractAll();
                            logger.log(INFO, "Map ''{0}'' extraction finished.", entry.getPath());
                            printInfo("Map '%s' extraction finished.", entry.getPath());
                        } catch (IOException e) {
                            logger.log(ERROR, "MapDecompiler failed", e);
                            printInfo("ERROR: MapDecompiler failed: "
                                    + (e.getMessage() != null ? e.getMessage() : e.getClass()));
                        }
                    });
                }
            } else {
                Files.write(outfile, entry.getData());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outfile;
    }

    private void extractTexture(ResourceFile resourceFile, Path destDir) throws IOException {
        Path texture;
        byte[] data = new byte[0];
        if (!isTex(resourceFile)) {
            return;
        }

        if (convertTexToPng) {
            texture = Path.of(destDir.toString(), resourceFile.getPath().toString().replaceAll("(?i)(.*)\\.tex$", "$1.png"));
            Texture dds = new TextureConverter((Texture) resourceFile).convert(TextureType.DDS, true);

            int[] pixels;
            try {
                pixels = DDSReader.readARGB(dds.getData());
            } catch (NotImplementedException e) {
                logger.log(ERROR, "Error converting ''{0}'' to PNG", texture.toString(), e);
                throw e;
            }

            if (pixels != null) {
                BufferedImage image = new BufferedImage(dds.getWidth(), dds.getHeight(), BufferedImage.TYPE_INT_ARGB);
                image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(pixels, pixels.length, 0), new Point()));
                ImageIO.write(image, "png", texture.toFile());
                return;
            }
        } else if (convertTexToDds) {
            texture = Path.of(destDir.toString(), resourceFile.getPath().toString().replaceAll("(?i)(.*)\\.tex$", "$1.dds"));
            data = new TextureConverter((Texture) resourceFile).convert(TextureType.DDS, convertTexIgnoreMip).getData();
        } else {
            texture = Path.of(destDir.toString(), resourceFile.getPath().toString());
            data = resourceFile.getData();
        }

        Files.write(texture, data);
    }

    private boolean isTex(ResourceFile entry) {
        return entry.getResourceType() == ResourceType.TEXTURE
                && EnumSet.of(TextureType.TEXV1, TextureType.TEXV2).contains(((Texture) entry).getTextureType());
    }
}
