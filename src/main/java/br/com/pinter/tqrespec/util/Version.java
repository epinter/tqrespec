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

import br.com.pinter.tqrespec.logging.Log;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

public final class Version implements Comparable<Version> {
    private static final System.Logger logger = Log.getLogger(Version.class);

    private final String versionNumber;
    private String urlPage;
    private String url1;
    private String url2;
    private String url3;
    private int lastCheck = -2;

    public Version(String version) {
        this.versionNumber = version;
    }

    public String getVersion() {
        return versionNumber;
    }

    public String getUrlPage() {
        return urlPage;
    }

    public String getUrl1() {
        return url1;
    }

    public String getUrl2() {
        return url2;
    }

    public String getUrl3() {
        return url3;
    }

    public int getLastCheck() {
        return lastCheck;
    }

    public void checkNewerVersion(String urlPropFile) {
        URI uri = null;
        try {
            uri = new URI(urlPropFile);
        } catch (URISyntaxException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }

        try {
            StringReader versionDescriptor = new StringReader(downloadVersionDescriptor(uri));

            Properties prop = new Properties();
            prop.load(versionDescriptor);

            if (prop.getProperty("current_version") != null
                    && Objects.requireNonNullElse(prop.getProperty("module"), "").equals("tqrespec")) {
                String currentVersion = prop.getProperty("current_version");
                this.urlPage = prop.getProperty("urlpage");
                this.url1 = prop.getProperty("url1");
                this.url2 = prop.getProperty("url2");
                this.url3 = prop.getProperty("url3");
                logger.log(DEBUG, "module: ''{0}'', current_version: ''{1}'', urlPage: ''{2}'', " +
                        "url1: ''{3}'', url2: ''{4}'', url3: ''{5}''", prop.getProperty("module"), currentVersion, urlPage, url1, url2, url3);
                lastCheck = this.compareTo(new Version(currentVersion));
            }
        } catch (IOException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        } catch (InterruptedException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            Thread.currentThread().interrupt();
        }
    }

    public String downloadVersionDescriptor(URI uri) throws IOException, InterruptedException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(20))
                .build()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .timeout(Duration.ofSeconds(20))
                    .uri(uri)
                    .GET().build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        return response.body();
    }

    @Override
    public int compareTo(Version o) {
        if (o == null)
            return 1;
        String versionString = getVersion().replaceAll("[_-].*$", "");
        String otherVersionString = o.getVersion().replaceAll("[_-].*$", "");
        String[] thisVersion = versionString.split("\\.");
        String[] otherVersion = otherVersionString.split("\\.");

        int length = Math.max(thisVersion.length, otherVersion.length);
        for (int i = 0; i < length; i++) {
            int thisVersionNumber = i < thisVersion.length ?
                    Integer.parseInt(thisVersion[i]) : 0;
            int otherVersionNumber = i < otherVersion.length ?
                    Integer.parseInt(otherVersion[i]) : 0;
            if (thisVersionNumber < otherVersionNumber)
                return -1;
            if (thisVersionNumber > otherVersionNumber)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return lastCheck == version1.lastCheck &&
                versionNumber.equals(version1.versionNumber) &&
                Objects.equals(urlPage, version1.urlPage) &&
                Objects.equals(url1, version1.url1) &&
                Objects.equals(url2, version1.url2) &&
                Objects.equals(url3, version1.url3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionNumber, urlPage, url1, url2, url3, lastCheck);
    }
}

