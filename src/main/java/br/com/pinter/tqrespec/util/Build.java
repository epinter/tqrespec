/*
 * Copyright (C) 2022 Emerson Pinter - All Rights Reserved
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

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Build {
    private static String buildVersion;
    private static String buildTitle;

    private Build() {
    }

    public static String version() {
        if (StringUtils.isNotBlank(buildVersion)) {
            return buildVersion;
        }
        String implementationVersion = Build.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            Attributes attr = readManifest();
            if (!attr.isEmpty()) {
                implementationVersion = attr.getValue("Implementation-Version");
            }
        }
        buildVersion = Objects.requireNonNullElse(implementationVersion, "0.0");
        return buildVersion;
    }

    public static String title() {
        if (StringUtils.isNotBlank(buildTitle)) {
            return buildTitle;
        }
        String implementationTitle = Build.class.getPackage().getImplementationTitle();
        if (implementationTitle == null) {
            Attributes attr = readManifest();
            if (!attr.isEmpty()) {
                implementationTitle = attr.getValue("Implementation-Title");
            }
        }
        buildTitle = Objects.requireNonNullElse(implementationTitle, "Development");
        return buildTitle;
    }

    private static Attributes readManifest() {
        if (!Build.class.getModule().isNamed()) {
            return new Attributes();
        }

        Manifest manifest;
        try {
            FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
            InputStream stream = Files.newInputStream(
                    fs.getPath("modules", Build.class.getModule().getName(), "META-INF/MANIFEST.MF"));
            manifest = new Manifest(stream);
            return manifest.getMainAttributes();
        } catch (IOException ignored) {
            //ignored
        }
        return new Attributes();
    }

}
