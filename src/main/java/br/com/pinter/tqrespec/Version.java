/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec;

import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

public class Version implements Comparable<Version> {
    private static final boolean DBG = false;
    private String version;
    private String urlPage;
    private String url1;
    private String url2;
    private String url3;
    private int lastCheck = -2;

    public String getVersion() {
        return version;
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

    public Version(String version) {
        this.version = version;
    }

    public int getLastCheck() {
        return lastCheck;
    }

    public int checkNewerVersion(String urlPropFile) {
        URL url = null;
        Reader reader = null;
        try {
            url = new URL(urlPropFile);
            InputStream in = url.openStream();
            reader = new InputStreamReader(in, "UTF-8"); // for example
            Properties prop = new Properties();
            if (reader != null)
                prop.load(reader);
            reader.close();
            if(prop.getProperty("current_version")!=null && prop.getProperty("module")!=null && prop.getProperty("module").equals("tqrespec")) {
                String currentVersion = prop.getProperty("current_version");
                this.urlPage = prop.getProperty("urlpage");
                this.url1 = prop.getProperty("url1");
                this.url2 = prop.getProperty("url2");
                this.url3 = prop.getProperty("url3");
                if(DBG) {
                    for (Object o : prop.keySet()) {
                        String k = (String) o;
                        String v = prop.getProperty(k);
                        System.out.println(k + "=" + v);
                    }
                }
                lastCheck = this.compareTo(new Version(currentVersion));
                return lastCheck;
            }
        } catch (Exception e) {
        }
        return -2;
    }

    @Override
    public int compareTo(Version o) {
        if(o == null)
            return 1;
        String[] thisVersion = this.getVersion().split("\\.");
        String[] otherVersion = o.getVersion().split("\\.");
        int length = Math.max(thisVersion.length, otherVersion.length);
        for(int i = 0; i < length; i++) {
            int thisVersionNumber = i < thisVersion.length ?
                    Integer.parseInt(thisVersion[i]) : 0;
            int otherVersionNumber = i < otherVersion.length ?
                    Integer.parseInt(otherVersion[i]) : 0;
            if(thisVersionNumber < otherVersionNumber)
                return -1;
            if(thisVersionNumber > otherVersionNumber)
                return 1;
        }
        return 0;
    }
}

