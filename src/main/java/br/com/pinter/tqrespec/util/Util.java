/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.util;

import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.tqdata.Txt;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Util {
    private static final System.Logger logger = Log.getLogger(Util.class.getName());
    private static String buildVersion;
    private static String buildTitle;

    private Util() {
    }

    public static String getBuildVersion() {
        if (StringUtils.isNotBlank(buildVersion)) {
            return buildVersion;
        }
        String implementationVersion = Util.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            Attributes attr = readManifest();
            if (attr != null) {
                implementationVersion = attr.getValue("Implementation-Version");
            }
        }
        buildVersion = Objects.requireNonNullElse(implementationVersion, "0.0");
        return buildVersion;
    }

    public static String getBuildTitle() {
        if (StringUtils.isNotBlank(buildTitle)) {
            return buildTitle;
        }
        String implementationTitle = Util.class.getPackage().getImplementationTitle();
        if (implementationTitle == null) {
            Attributes attr = readManifest();
            if (attr != null) {
                implementationTitle = attr.getValue("Implementation-Title");
            }
        }
        buildTitle = Objects.requireNonNullElse(implementationTitle, "Development");
        return buildTitle;
    }

    private static Attributes readManifest() {
        if (!Util.class.getModule().isNamed()) {
            return null;
        }

        Manifest manifest;
        try {
            FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
            InputStream stream = Files.newInputStream(
                    fs.getPath("modules", Util.class.getModule().getName(), "META-INF/MANIFEST.MF"));
            manifest = new Manifest(stream);
            return manifest.getMainAttributes();
        } catch (IOException ignored) {
            //ignored
        }
        return null;
    }

    public static void showError(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(getBuildTitle());
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private static void showWarning(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(getBuildTitle());
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showInformation(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(getBuildTitle());
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static String getUIMessage(String message) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI", State.get().getLocale());
        if (ui.containsKey(message)) {
            return ui.getString(message);
        }
        return message;
    }

    public static String getUIMessage(String message, Object... parameters) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI", State.get().getLocale());
        if (ui.containsKey(message)) {
            return MessageFormat.format(ui.getString(message), parameters);
        }
        return message;
    }

    public static void copyDirectoryRecurse(Path source, Path target, boolean replace) throws FileAlreadyExistsException {
        copyDirectoryRecurse(source, target, replace, FileSystems.getDefault(), null);
    }

    public static void copyDirectoryRecurse(Path source, Path target, boolean replace, String excludeRegex) throws FileAlreadyExistsException {
        copyDirectoryRecurse(source, target, replace, FileSystems.getDefault(), excludeRegex);
    }

    public static void copyDirectoryRecurse(Path source, Path target, boolean replace, FileSystem fileSystem, String excludeRegex) throws FileAlreadyExistsException {
        if (!replace && Files.exists(target)) {
            throw new FileAlreadyExistsException(target.toString() + " already exists");
        }

        FileVisitor<Path> fileVisitor = new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = fileSystem.getPath(target.toString(), source.relativize(dir).toString());

                try {
                    if (excludeRegex != null && dir.getFileName().toString().matches(excludeRegex)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    Files.copy(dir, targetDir, COPY_ATTRIBUTES);
                } catch (DirectoryNotEmptyException ignored) {
                    //ignore
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, "Unable to create directory ''{0}''", targetDir);
                    throw new IOException("Unable to create directory " + targetDir);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path targetFile = fileSystem.getPath(target.toString(), source.relativize(file).toString());

                try {
                    if (excludeRegex != null && file.getFileName().toString().matches(excludeRegex)) {
                        return FileVisitResult.CONTINUE;
                    }
                    Files.copy(file, targetFile, replace ? new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING}
                            : new CopyOption[]{COPY_ATTRIBUTES});
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, "Unable to create file ''{0}''", targetFile);
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.TERMINATE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(source, fileVisitor);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    public static void closeApplication() {
        if (State.get().getSaveInProgress() != null && !State.get().getSaveInProgress()
                || State.get().getSaveInProgress() == null) {
            Platform.exit();
            System.exit(0);
        }

        Util.showWarning(Util.getUIMessage("alert.saveinprogress_header"), Util.getUIMessage("alert.saveinprogress_content"));
    }

    public static String cleanTagString(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        return value.replaceAll("(?:\\{[^}]+\\})*([^{}:]*)(?:\\{[^}]+\\})*", "$1")
                .replace(":", "")
                .trim();
    }

    private static void setLabeledText(Object obj, String text) {
        Labeled control = (Labeled) obj;
        control.setText(text);
    }

    private static void setTabText(Object obj, String text) {
        Tab control = (Tab) obj;
        control.setText(text);
    }

    public static void tryTagText(Txt txt, Object control, String tag, boolean capitalized, boolean needsClean) {
        if (!txt.isTagStringValid(tag))
            return;

        String text = capitalized ? txt.getCapitalizedString(tag) : txt.getString(tag);

        if (needsClean) {
            text = Util.cleanTagString(text);
        }
        if (control instanceof Labeled) {
            logger.log(System.Logger.Level.DEBUG, "settext control labeled");
            setLabeledText(control, text);
        } else if (control instanceof Tab) {
            logger.log(System.Logger.Level.DEBUG, "settext control tab");
            setTabText(control, text);
        } else {
            throw new UnsupportedOperationException("BUG: trying to set text on unsupported control");
        }
    }

    public static Tooltip simpleTooltip(String message) {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setFont(Constants.UI.TOOLTIP_FONT);
        tooltip.setShowDelay(Duration.millis(Constants.UI.TOOLTIP_SHOWDELAY_MILLIS));
        tooltip.setShowDuration(Duration.millis(Constants.UI.TOOLTIP_SHOWDURATION_MILLIS));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(Constants.UI.TOOLTIP_MAXWIDTH);
        return tooltip;
    }
}
