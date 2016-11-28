package com.shuyu.lbsmap.utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    public static final String NAME = "LBSMap";

    public static final String BIG_END = "big";

    public static final int BIG_SIZE = 12;

    public static String getLogoNamePath(String urlIcon) {
        return FileUtils.getAppPath() + "/" + CommonUtil.MD5L(urlIcon) + "_";
    }

    public static String getAppPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(SD_PATH);
        sb.append(File.separator);
        sb.append(NAME);
        sb.append(File.separator);
        return sb.toString();
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String[] filePaths = file.list();
                for (String path : filePaths) {
                    deleteFile(filePath + File.separator + path);
                }
                file.delete();
            }
        }
    }
}