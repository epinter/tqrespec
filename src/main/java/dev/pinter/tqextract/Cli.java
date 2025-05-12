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

import br.com.pinter.tqrespec.core.GameNotFoundException;
import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.core.InjectionContext;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import com.google.inject.Inject;
import com.google.inject.Module;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.INFO;

@Command(name = "tqextract-cli", mixinStandardHelpOptions = true, sortSynopsis = false,
        subcommands = {
                Cli.OptionExtractAll.class,
                Cli.OptionExtractArc.class,
                Cli.OptionExtractArz.class
        }
)
public class Cli {
    private static final System.Logger logger = Log.getLogger(Cli.class);

    @Inject
    private GameInfo gameInfo;
    private final AtomicInteger lastProgressLog = new AtomicInteger(0);

    @Option(names = {"--output-dir", "-o"}, required = true, description = "Output directory", paramLabel = "directory", order = 0)
    private Path outputDir;

    @Option(names = {"--logfile", "-l"}, description = "Logfile path", paramLabel = "file", order = 1)
    private Path logfile;

    @Option(names = {"--max-threads", "-t"}, description = "Max threads, limits the parallel work when extracting ARC and ARZ." +
            " Higher the number, higher will be the memory usage.", order = 2)
    private int maxThreads;

    private Cli() {
        prepareInjectionContext();
        this.maxThreads = maxThreads == 0 ? Math.max(5, Runtime.getRuntime().availableProcessors() - 4) : maxThreads;
    }

    public static void main(String[] args) {
        final AtomicBoolean cleanShutdown = new AtomicBoolean();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!cleanShutdown.get()) {
                System.err.printf("%n%nAborted.%n");
            }
        }));
        new CommandLine(new Cli()).execute(args);
        cleanShutdown.set(true);
    }

    @Command(name = "all", mixinStandardHelpOptions = true,
            description = "Extract all game files, arc and arz. All TEX are converted to DDS.")
    static class OptionExtractAll implements Runnable {
        @ParentCommand
        private Cli parent;

        @Option(names = {"--no-mapdecompiler"}, description = "Disable map decompiler when a map is found")
        private boolean skipDecompiler;

        @Option(names = {"--no-texconversion"}, description = "Disable TEX conversion to DDS")
        private boolean skipTexConversion;

        @Option(names = {"--wrlscaling"}, description = """
                Scaling of images written to WRL, used in the layout mode of the Editor.
                     1 equals to 1/4 of the image, 2 is half, 4 is full size. Bigger images will make the editor crash. Default is 2.""")
        private int mapWrlScaling;

        @Override
        public void run() {
            mapWrlScaling = mapWrlScaling < 1 ? 2 : mapWrlScaling;

            if (parent.outputDir == null) {
                System.err.println("ERROR: Output directory not specified");
                System.exit(1);
            }

            Extract extract = parent.createExtract();
            extract.setMapWrlScaling(mapWrlScaling);
            extract.setConvertTexToDds(!skipTexConversion);
            extract.setDecompileMap(!skipDecompiler);

            try {
                extract.processDirectory(Path.of(parent.gameInfo.getGamePath()));
            } catch (GameNotFoundException | IOException e) {
                logger.log(INFO, "Error", e);
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    @Command(name = "arc", mixinStandardHelpOptions = true,
            description = "Extract arc files")
    static class OptionExtractArc implements Runnable {
        @ParentCommand
        private Cli parent;

        @Option(names = {"--no-mapdecompiler"}, description = "Disable map decompiler when a MAP is found")
        private boolean skipDecompiler;

        @Option(names = {"--no-texconversion"}, description = "Disable TEX conversion to DDS")
        private boolean skipTexConversion;

        @Option(names = {"--wrlscaling"}, description = """
                Scaling of images written to WRL, used in the layout mode of the Editor.
                     1 equals to 1/4 of the image, 2 is half, 4 is full size. Bigger images will make the editor crash. Default is 2.""")
        private int mapWrlScaling;

        @CommandLine.ArgGroup(exclusive = true)
        private InputOptions input;

        static class InputOptions {
            @Option(names = {"--input-file", "-f"}, required = true, description = "Full path of the arc file, this option and " +
                    "--directory are mutually exclusive", paramLabel = "<file.arc>")
            private Path inputArc;

            @Option(names = {"--directory", "-d"}, required = true, description = "A directory with multiple arc files to extract, " +
                    "this option and --input-file are mutually exclusive", paramLabel = "<directory>")
            private Path inputDir;
        }


        @Override
        public void run() {
            mapWrlScaling = mapWrlScaling < 1 ? 2 : mapWrlScaling;

            if (input == null || (input.inputArc == null && input.inputDir == null)) {
                System.err.printf("ERROR: An input file or directory must be specified%n");
                System.exit(1);
            }

            if (parent.outputDir == null) {
                System.err.println("ERROR: Output directory not specified");
                System.exit(1);
            }

            if (input.inputArc != null && !Files.isRegularFile(input.inputArc)) {
                System.err.printf("ERROR: File not found '%s'", input.inputArc);
                System.exit(1);
            }

            if (input.inputDir != null && !Files.isDirectory(input.inputDir)) {
                System.err.printf("ERROR: Directory not found '%s'", input.inputDir);
                System.exit(1);
            }

            Extract extract = parent.createExtract();
            extract.setMapWrlScaling(mapWrlScaling);
            extract.setConvertTexToDds(!skipTexConversion);
            extract.setDecompileMap(!skipDecompiler);

            try {
                if (input.inputArc != null) {
                    extract.processFile(input.inputArc);
                } else if (input.inputDir != null) {
                    extract.processDirectory(input.inputDir);
                }

            } catch (IOException e) {
                logger.log(INFO, "Error", e);
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    @Command(name = "arz", mixinStandardHelpOptions = true,
            description = "Extract arc files")
    public static class OptionExtractArz implements Runnable {
        @ParentCommand
        private Cli parent;

        @Option(names = {"--input-file", "-f"}, description = "Full path of the arz file", paramLabel = "<database.arz>")
        private Path inputArz;

        @Override
        public void run() {
            if (inputArz == null) {
                try {
                    if (parent.gameInfo != null && parent.gameInfo.getDatabasePath() != null) {
                        String[] databasePath = parent.gameInfo.getDatabasePath();
                        if (databasePath.length >= 1) {
                            inputArz = Path.of(databasePath[0]);
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                if (inputArz == null || !inputArz.toFile().isFile()) {
                    System.err.printf("ERROR: An input file must be specified%n");
                    System.exit(1);
                }
            }

            if (parent.outputDir == null) {
                System.err.println("ERROR: Output directory not specified");
                System.exit(1);
            }

            if (!Files.isRegularFile(inputArz)) {
                System.err.printf("ERROR: File not found '%s'", inputArz);
                System.exit(1);
            }

            Extract extract = parent.createExtract();

            try {
                extract.extractArz(inputArz);
            } catch (IOException e) {
                logger.log(INFO, "Error", e);
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    public Extract createExtract() {
        Extract extract;
        if (logfile != null) {
            extract = new Extract(outputDir, logfile, maxThreads);
        } else {
            extract = new Extract(outputDir, maxThreads);
        }
        extract.setProgressHandler((tag, count, total) -> {
            printProgress(tag, count, total);
            return null;
        });
        extract.setPrintInfoHandler(s -> {
            System.out.println("\033[2K\r" + s);
            return null;
        });

        return extract;
    }

    public void prepareInjectionContext() {
        List<Module> modules = new ArrayList<>();
        modules.add(new GuiceModule());
        InjectionContext injectionContext = new InjectionContext(this, modules);
        injectionContext.initialize();
    }

    private void printProgress(String tag, int count, int total) {
        double pct = (Math.round(((count / (total * 1.0f)) * 100.0f) * Math.pow(10, 2)) / Math.pow(10, 2));

        synchronized (lastProgressLog) {
            if ((Math.round(pct) != 100
                    && Math.round(pct) != lastProgressLog.get()
                    && Math.round(pct) % 10 == 0) || count == total) {
                lastProgressLog.set((int) Math.round(pct));
                logger.log(INFO, "Progress ({0}): {1} ({2}/{3})\r", tag, "%d%%".formatted(Math.round(pct)), count, total);
            }

            if (pct % 0.25 == 0) {
                System.out.printf("\033[2KProgress (%s): %.2f%% (%s/%s)\r", tag, pct, count, total);
                if (count == total) {
                    System.out.println();
                }
            }
        }
    }
}
