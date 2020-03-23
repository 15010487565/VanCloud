package com.vgtech.vancloud.utils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by code on 2015/11/24.
 */
public class BufferSizeUtils {
    private static BufferSizeUtils util;

    private static File file1;
    private static File file2;

    private BufferSizeUtils(File file1, File file2) {

        this.file1 = file1;
        this.file2 = file2;
    }

    public static BufferSizeUtils getInstance(File file1, File file2) {

        util = new BufferSizeUtils(file1, file2);
        return util;
    }

    public static String getClearSize(File file1, File file2) {
        try {
            return formetFileSize(addSize(getFileSize(file1),getFileSize(file2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.00KB";
    }

    public static String getClearSize() {
        try {
            return formetFileSize(addSize(getFileSize(file1),getFileSize(file2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.00KB";
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long 单位为Bit
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (!file.exists()) {
            return size;
        }
        File[] fileList = file.listFiles();
        for (File f:fileList) {
            if (f.isDirectory()) {
                size = size + getFileSize(f);
            } else {
                size = size + f.length();
            }
        }
        return size;
    }

    /**
     * 参数的大小为Bit 返回的数据的单位也是Bit
     */
    private static long addSize(Long fileSize1, long fileSize2) {
        return fileSize1 + fileSize2;
    }

    /**
     * 转换文件大小, 参数的大小为Bit
     */
    public static String formetFileSize(long fileSize) {
        // 转换为bit
        long fileS = fileSize;

        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            return "0.00KB";
        }

        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
}
