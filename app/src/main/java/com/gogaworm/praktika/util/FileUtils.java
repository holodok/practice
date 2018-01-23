package com.gogaworm.praktika.util;

import android.os.Environment;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Ilona
 * on 19.11.2017.
 */
public final class FileUtils {
    /* Checks if external storage is available for read and write */
    static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static String getApplicationDir(String appName) {
        return Environment.getExternalStorageDirectory().toString() + File.separator + appName;
    }

    public static File[] getBackupList(String appName) {
        if (!isExternalStorageReadable()) {
            return null;
        }

        return new File(getApplicationDir(appName)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".json");
            }
        });
    }

    public static File createAppDir(String appName) {
        File directory = new File(getApplicationDir(appName));
        return directory.mkdir() ? directory : null;
    }

    public static boolean hasFileName(String appName, String fileName) {
        File[] files = getBackupList(appName);
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }
}
