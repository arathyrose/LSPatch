package org.lsposed.patch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import wind.android.content.res.AXmlResourceParser;
import wind.v1.XmlPullParser;
import wind.v1.XmlPullParserException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.annotation.Nullable;
import com.wind.meditor.core.ManifestEditor;

/**
 * Created by Wind
 */
public class ManifestParser {

    public static Pair parseManifestFile(InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        AXmlResourceParser parser = new AXmlResourceParser();
        String packageName = null;
        String appComponentFactory = null;
        int minSdkVersion = 0;
        try {
            parser.open(buffer);
            while (true) {
                int type = parser.next();
                System.out.println("Parsing is at :"+String.valueOf(type));
                if (type == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                if (type == XmlPullParser.START_TAG) {
                    int attrCount = parser.getAttributeCount();
                    for (int i = 0; i < attrCount; i++) {
                        String attrName = parser.getAttributeName(i);
                        int attrNameRes = parser.getAttributeNameResource(i);

                        String name = parser.getName();

                        if ("manifest".equals(name)) {
                            if ("package".equals(attrName)) {
                                packageName = parser.getAttributeValue(i);
                            }
                        }

                        if ("uses-sdk".equals(name)) {
                            if ("android:minSdkVersion".equals(attrName)) {
                                minSdkVersion = Integer.parseInt(parser.getAttributeValue(i));
                            }
                        }

                        if ("appComponentFactory".equals(attrName) || attrNameRes == 0x0101057a) {
                            appComponentFactory = parser.getAttributeValue(i);
                        }

                        if (packageName != null && packageName.length() > 0 &&
                                appComponentFactory != null && appComponentFactory.length() > 0 &&
                                minSdkVersion > 0
                        ) {
                            return new Pair(packageName, appComponentFactory, minSdkVersion);
                        }
                    }
                } else if (type == XmlPullParser.END_TAG) {
                    // ignored
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return new Pair("error", e.getMessage(), 12);
        }
        return new Pair(packageName, appComponentFactory, minSdkVersion);
    }

    /**
     * Get the package name and the main application name from the manifest file
     */
    public static Pair parseManifestFile(String filePath) throws IOException {
        File file = new File(filePath);
        try (var is = new FileInputStream(file)) {
            return parseManifestFile(is);
        }
    }

    public static class Pair {
        public String packageName;
        public String appComponentFactory;

        public int minSdkVersion;

        public Pair(String packageName, String appComponentFactory, int minSdkVersion) {
            this.packageName = packageName;
            this.appComponentFactory = appComponentFactory;
            this.minSdkVersion = minSdkVersion;
        }
    }

}
