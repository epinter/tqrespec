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
import br.com.pinter.tqdatabase.TextureParseException;
import br.com.pinter.tqdatabase.data.TextureConverter;
import br.com.pinter.tqdatabase.models.ResourceFile;
import br.com.pinter.tqdatabase.models.Texture;
import br.com.pinter.tqdatabase.models.TextureType;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Build;
import dev.pinter.tqextract.image.DDSReader;
import org.apache.commons.lang3.SystemUtils;
import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@CommandLine.Command(name = "texconverter-cli", mixinStandardHelpOptions = true, sortSynopsis = false,
        description = """
                A tool to convert Titan Quest textures.
                Conversions supported: TEX -> DDS, TEX -> PNG, DDS -> TEX.
                The conversion to TEX doesn't invert the mipmaps, like most game textures have.
                The input DDS will have its header adapted, and all content copied as-is to TEX.
                TEX with inverted mipmaps order will have its mipmaps changed to normal order (bigger first) when converted to DDS.
                """, versionProvider = TexConverterCli.VersionProvider.class)
public class TexConverterCli implements Runnable {
    private static final System.Logger logger = Log.getLogger(TexConverterCli.class);

    @CommandLine.Parameters(hidden = true)
    private List<String> positional;

    @CommandLine.ArgGroup(exclusive = false, heading = "Single file conversion options%n")
    private SimpleFileOptions fileOpt;

    @CommandLine.Option(names = {"--texv2", "--v2"}, description = "When converting DDS to TEX, use V2 format")
    private boolean toTexV2;

    @CommandLine.ArgGroup(exclusive = false, heading = "Directory conversion options%n")
    private DirectoryOptions dirOpt;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    static class SimpleFileOptions {
        @CommandLine.Option(names = {"--input-arc", "-a"}, description = "Full path of the ARC to search the input file", paramLabel = "<inputarc>")
        private Path inputArc;

        @CommandLine.Option(names = {"--input-file", "-i"}, description = "Full path of the input file," +
                " or the path inside the ARC if --input-arc is used", paramLabel = "<inputfile>", required = true)
        private Path input;

        @CommandLine.Option(names = {"--output-file", "-o"}, description = "Full path of the output file", paramLabel = "<outputfile>", required = true)
        private Path output;
    }

    static class DirectoryOptions {
        @CommandLine.Option(names = {"--directory", "-d"}, description = "Full path of the directory containing textures", paramLabel = "<directory>", required = true)
        private Path directory;

        @CommandLine.ArgGroup(exclusive = true)
        private ConversionType convType;
    }

    static class ConversionType {
        @CommandLine.Option(names = {"--tex-to-dds"}, description = "TEX to DDS", required = true)
        private boolean dirTex2Dds;
        @CommandLine.Option(names = {"--tex-to-png"}, description = "TEX to PNG", required = true)
        private boolean dirTex2Png;
        @CommandLine.Option(names = {"--dds-to-tex"}, description = "DDS to TEX", required = true)
        private boolean dirDds2Tex;
    }

    private TexConverterCli() {
        Log.setupGlobalLogging(Path.of(System.getProperty("java.io.tmpdir"), "texconverter-cli.log"));
    }

    @Override
    public void run() {
        if (positional != null && !positional.isEmpty() && dirOpt == null && fileOpt == null) {
            if (Path.of(positional.getFirst()).toFile().isFile()) {
                Path in = Path.of(positional.getFirst()).toAbsolutePath();
                Path out;
                if (in.toString().matches("(?i).*\\.dds")) {
                    out = Path.of(in.toString().replaceAll("(?i)\\.dds$", ".tex"));
                } else if (in.toString().matches("(?i).*\\.tex")) {
                    out = Path.of(in.toString().replaceAll("(?i)\\.tex$", ".dds"));
                } else {
                    throw new CommandLine.ParameterException(spec.commandLine(), "Invalid argument");
                }
                try {
                    convertFile(in, out);
                    windowsPause();
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (Path.of(positional.getFirst()).toFile().isDirectory()) {
                processDirectory(Path.of(positional.getFirst()));
                windowsPause();
                return;
            }
        }

        if (fileOpt == null && dirOpt == null) {
            throw new CommandLine.ParameterException(spec.commandLine(), "Please specify -i and -o to convert a single file," +
                    " or -d to convert a directory");
        }

        if (dirOpt != null) {
            if (dirOpt.directory == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Invalid directory");
            }
            if (dirOpt.convType == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Please specify a conversion type");
            }
            processDirectory(dirOpt.directory);
            return;
        }

        if (Files.exists(fileOpt.output)) {
            System.err.printf("File '%s' already exists, aborting%n", fileOpt.output);
            System.exit(1);
        }

        if (fileOpt.inputArc == null && !Files.exists(fileOpt.input)) {
            System.err.println("Input file not found");
            System.exit(1);
        }

        try {
            convertFile(fileOpt.input, fileOpt.output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void windowsPause() {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.out.println("Press enter to exit.");
            new Scanner(System.in).nextLine();
        }
    }

    public void convertFile(Path fileIn, Path fileOut) throws IOException {
        Path outfileDir = fileOut.getParent();
        if (!outfileDir.toFile().exists()) {
            if (!outfileDir.toFile().mkdirs()) {
                if (!Files.exists(outfileDir)) {
                    logger.log(ERROR, "Failed to create directory: {0}", outfileDir);
                    throw new RuntimeException("Failed to create directory " + outfileDir);
                }
            }
        }

        byte[] inputData;
        if (fileOpt != null && fileOpt.inputArc != null) {
            Resources resources = new Resources(fileOpt.inputArc);
            ResourceFile file = resources.getFile(fileIn.toString());
            if (file == null) {
                System.out.println(resources.getNames());
                throw new RuntimeException("Resource not found: " + fileIn.toString());
            }
            inputData = file.getData();
        } else {
            inputData = Files.readAllBytes(fileIn);
        }

        if (inputData.length == 0) {
            logger.log(ERROR, "Invalid input file: {0}", fileIn);
            throw new IOException("Invalid input file");
        }

        if (fileIn.toString().matches("(?i).*\\.tex$")
                && fileOut.toString().matches("(?i).*\\.dds$")) {
            Texture dds = new TextureConverter(new Texture(fileIn.getFileName().toString(), inputData))
                    .convert(TextureType.DDS, false);
            Files.write(fileOut, dds.getData(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            System.out.printf("Image '%s' created with '%d' bytes.%n", fileOut, fileOut.toFile().length());
            logger.log(INFO, "Image ''{0}'' created with ''{1}'' bytes.", fileOut, fileOut.toFile().length());
        } else if (fileIn.toString().matches("(?i).*\\.tex$")
                && fileOut.toString().matches("(?i).*\\.png$")) {
            Texture dds = new TextureConverter(new Texture(fileIn.getFileName().toString(), inputData))
                    .convert(TextureType.DDS, true);
            int[] pixels = DDSReader.readARGB(dds.getData());

            if (pixels != null) {
                BufferedImage image = new BufferedImage(dds.getWidth(), dds.getHeight(), BufferedImage.TYPE_INT_ARGB);
                image.setData(Raster.createRaster(image.getSampleModel(), new DataBufferInt(pixels, pixels.length, 0), new Point()));
                if (Files.exists(fileOut)) {
                    logger.log(ERROR, "File already exists: {0}", fileOut);
                    throw new FileAlreadyExistsException("File already exists: " + fileOut);
                }
                ImageIO.write(image, "png", fileOut.toFile());
                System.out.printf("Image '%s' created with '%d' bytes.%n", fileOut, fileOut.toFile().length());
                logger.log(INFO, "Image ''{0}'' created with ''{1}'' bytes.", fileOut, fileOut.toFile().length());
            }
        } else if (fileIn.toString().matches("(?i).*\\.dds$")
                && fileOut.toString().matches("(?i).*\\.tex$")) {
            Texture tex = new TextureConverter(new Texture(fileIn.getFileName().toString(), inputData))
                    .convert(toTexV2 ? TextureType.TEXV2 : TextureType.TEXV1, false);
            Files.write(fileOut, tex.getData(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            System.out.printf("Image '%s' created with '%d' bytes.%n", fileOut, fileOut.toFile().length());
            logger.log(INFO, "Image ''{0}'' created with ''{1}'' bytes.", fileOut, fileOut.toFile().length());
        } else {
            logger.log(ERROR, "Invalid file (in={0}; out={1})", fileIn.getFileName(), fileOut.getFileName());
            throw new IllegalArgumentException(String.format("Invalid file (in=%s; out=%s)",
                    fileIn.getFileName(), fileOut.getFileName()));
        }
    }

    private void processDirectory(Path directory) {
        AtomicInteger converted = new AtomicInteger(0);
        Path outputDir = directory.toAbsolutePath();
        if (directory.getParent() != null) {
            outputDir = Path.of(directory.getParent().toString(), "output-texconvert", directory.getFileName().toString());
        }

        final List<Path> toConvert = new ArrayList<>();
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toAbsolutePath().toString().matches("(?i).*\\.(dds|tex)")) {
                    toConvert.add(file.toAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(directory, fileVisitor);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            logger.log(ERROR, "Error: ", e);
        }

        for (Path in : toConvert) {
            Path out;
            if (dirOpt != null) {
                if (dirOpt.convType.dirDds2Tex) {
                    if (!in.getFileName().toString().matches("(?i).*\\.dds$")) {
                        continue;
                    }
                    out = getOutputFilename(Path.of(outputDir.toString(), resolveRelativePath(directory, in.getParent()).toString()),
                            in.getFileName().toString(), "dds", "tex");
                } else if (dirOpt.convType.dirTex2Dds) {
                    if (!in.getFileName().toString().matches("(?i).*\\.tex$")) {
                        continue;
                    }
                    out = getOutputFilename(Path.of(outputDir.toString(), resolveRelativePath(directory, in.getParent()).toString()),
                            in.getFileName().toString(), "tex", "dds");
                } else if (dirOpt.convType.dirTex2Png) {
                    if (!in.getFileName().toString().matches("(?i).*\\.tex$")) {
                        continue;
                    }
                    out = getOutputFilename(Path.of(outputDir.toString(), resolveRelativePath(directory, in.getParent()).toString()),
                            in.getFileName().toString(), "tex", "png");
                } else {
                    continue;
                }
            } else if (in.toString().matches("(?i).*\\.dds$")) {
                out = getOutputFilename(Path.of(outputDir.toString(), resolveRelativePath(directory, in.getParent()).toString()),
                        in.getFileName().toString(), "dds", "tex");
            } else if (in.toString().matches("(?i).*\\.tex$")) {
                out = getOutputFilename(Path.of(outputDir.toString(), resolveRelativePath(directory, in.getParent()).toString()),
                        in.getFileName().toString(), "tex", "dds");
            } else {
                continue;
            }

            try {
                convertFile(in, out);
                converted.incrementAndGet();
            } catch (IOException | TextureParseException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                logger.log(ERROR, "Error: ", e);
            }
        }

        logger.log(INFO, "Files found: " + toConvert.size());
        logger.log(INFO, "Files converted: " + converted.get());

        System.out.println("Files found: " + toConvert.size());
        System.out.println("Files converted: " + converted.get());
    }

    private Path getOutputFilename(Path outputDir, String fileName, String inExt, String outExt) {
        return Path.of(outputDir.toString(), fileName.replaceAll("(?i)\\." + inExt + "$", "." + outExt));
    }

    private Path resolveRelativePath(Path dir, Path path) {
        return dir.toAbsolutePath().relativize(path);
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