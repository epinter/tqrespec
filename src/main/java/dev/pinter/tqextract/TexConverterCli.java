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

import br.com.pinter.tqdatabase.Resources;
import br.com.pinter.tqdatabase.data.TextureConverter;
import br.com.pinter.tqdatabase.models.ResourceFile;
import br.com.pinter.tqdatabase.models.Texture;
import br.com.pinter.tqdatabase.models.TextureType;
import br.com.pinter.tqrespec.util.Build;
import dev.pinter.tqextract.image.DDSReader;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@CommandLine.Command(name = "texconverter-cli", mixinStandardHelpOptions = true, sortSynopsis = false,
        description = """
                A tool to convert Titan Quest textures.
                Conversions supported: TEX -> DDS, TEX -> PNG, DDS -> TEX.
                The conversion to TEX doesn't invert the mipmaps, like most game textures have.
                The input DDS will have its header adapted, and all content copied as-is to TEX.
                TEX with inverted mipmaps order will have its mipmaps changed to normal order (bigger first) when converted to DDS.
                """, versionProvider = TexConverterCli.VersionProvider.class)
public class TexConverterCli implements Runnable {
    @CommandLine.Option(names = {"--input-file", "-i"}, description = "Full path of the input file," +
            " or the path inside the ARC if --input-arc is used", paramLabel = "<inputfile>", required = true)
    private Path input;

    @CommandLine.Option(names = {"--input-arc", "-a"}, description = "Full path of the ARC to search the file", paramLabel = "<inputarc>")
    private Path inputArc;

    @CommandLine.Option(names = {"--output-file", "-o"}, description = "Full path of the output file", paramLabel = "<outputfile>", required = true)
    private Path output;

    @CommandLine.Option(names = {"--texv2", "--v2"}, description = "When converting DDS to TEX, use V2 format")
    private boolean toTexV2;

    private TexConverterCli() {
    }

    @Override
    public void run() {
        if (Files.exists(output)) {
            System.err.printf("File '%s' already exists, aborting%n", output);
            System.exit(1);
        }

        if (!Files.exists(input)) {
            System.err.println("Input file not found");
            System.exit(1);
        }

        byte[] inputData;
        try {
            if (inputArc != null) {
                Resources resources = new Resources(inputArc);
                ResourceFile file = resources.getFile(input.toString());
                if (file == null) {
                    System.out.println(resources.getNames());
                    throw new RuntimeException("Resource not found: " + input.toString());
                }
                inputData = file.getData();
            } else {
                inputData = Files.readAllBytes(input);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (inputData.length == 0) {
            System.err.println("Invalid input file");
            System.exit(1);
        }

        if (input.toString().toLowerCase(Locale.ROOT).endsWith("tex")
                && output.toString().toLowerCase(Locale.ROOT).endsWith("dds")) {
            try {
                Texture dds = new TextureConverter(new Texture(input.getFileName().toString(), inputData))
                        .convert(TextureType.DDS, false);
                Files.write(output, dds.getData());
                System.out.printf("Image '%s' created with '%d' bytes.%n", output, output.toFile().length());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (input.toString().toLowerCase(Locale.ROOT).endsWith("tex")
                && output.toString().toLowerCase(Locale.ROOT).endsWith("png")) {
            try {
                Texture dds = new TextureConverter(new Texture(input.getFileName().toString(), inputData))
                        .convert(TextureType.DDS, true);
                int[] pixels = DDSReader.readARGB(dds.getData());

                if (pixels != null) {
                    BufferedImage image = new BufferedImage(dds.getWidth(), dds.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(pixels, pixels.length, 0), new Point()));
                    ImageIO.write(image, "png", output.toFile());
                    System.out.printf("Image '%s' created with '%d' bytes.%n", output, output.toFile().length());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (input.toString().toLowerCase(Locale.ROOT).endsWith("dds")
                && output.toString().toLowerCase(Locale.ROOT).endsWith("tex")) {
            try {
                Texture tex = new TextureConverter(new Texture(input.getFileName().toString(), inputData))
                        .convert(toTexV2 ? TextureType.TEXV2 : TextureType.TEXV1, false);
                Files.write(output, tex.getData());
                System.out.printf("Image '%s' created with '%d' bytes.%n", output, output.toFile().length());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Invalid arguments");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new CommandLine(new TexConverterCli()).execute(args);
    }

    static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{Build.title() + " " + Build.version()};
        }
    }
}