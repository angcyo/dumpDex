package com.wrbug.dumpdex;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：管理日志工具类
 * 创建人员：Robi
 * 创建时间：2018/06/25 08:53
 * 修改人员：Robi
 * 修改时间：2018/06/25 08:53
 * 修改备注：
 * Version: 1.0.0
 */
public class RLog {
    /**
     * 保留多少天的日志, 最小值为1天
     */
    public static int logDays = 7;

    /**
     * 日志保存存在路径, 默认在SD卡/包名/r_log目录下
     */
    public static String logPath = "";

    /**
     * 每个日志文件, 最大的大小
     */
    public static long maxLogFileSize = 1024 * 1024 * 10;

    public static void saveToSDCard(final String data) {
        saveToSDCard(getLogPath(), data);
    }

    public static void saveToSDCard(final String logPath, final String data) {
        try {
            String dataTime = getDataTime("yyyy-MM-dd_HH-mm-ss-SSS");
            PrintWriter pw = createPrintWrite(logPath);
            pw.println(dataTime + " " + Thread.currentThread().getName());
            pw.println(data);
            //换行
            pw.println();
            pw.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void saveToSDCard(Exception exception) {
        try {
            PrintWriter pw = createPrintWrite(getLogPath());
            exception.printStackTrace(pw);
            pw.println();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLogPath() {
        if (!TextUtils.isEmpty(logPath)) {
            return logPath;
        }
        return Environment.getExternalStorageDirectory().getAbsoluteFile() +
                File.separator + "angcyo_dumpDex" + File.separator + "rlog";
    }

    public static void setLogFolder(String folderName) {
        clearLog();
        logPath = Environment.getExternalStorageDirectory().getAbsoluteFile() +
                File.separator + folderName + File.separator + "rlog";
    }

    public static String getLogFilePath() {
        String saveFolder = getLogPath();
        String dataTime = getDataTime("yyyy-MM-dd");
        return saveFolder + "/" + dataTime + ".rlog";
    }

    private static PrintWriter createPrintWrite(final String logPath) {
        try {
            String saveFolder = logPath;

            File folder = new File(saveFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return null;
                }
            }

            //清理日志
            checkLogFile(saveFolder);

            String dataTime = getDataTime("yyyy-MM-dd");

            File file = new File(saveFolder, dataTime + ".rlog");
            boolean append = true;
            if (file.length() > maxLogFileSize /*大于10MB重写*/) {
                append = false;
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
            return pw;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * 清除多余的文件
     */
    private static void checkLogFile(String filePath) {
        File folder = new File(filePath);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            int day = Math.max(logDays, 1);

            if (files != null && files.length > day) {
                //需要清理
                List<File> fileList = Arrays.asList(files);
                Collections.sort(fileList);

                int size = fileList.size() - day;

                //L.w("排序后的文件列表...");
                for (int i = 0; i < size; i++) {
                    File file = fileList.get(i);
                    //L.e(i + " :" + file.getAbsolutePath());
                    try {
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 清空log日志
     */
    public static void clearLog() {
        String saveFolder = getLogPath();
        //FileUtils.deleteFilesInDir(saveFolder);
    }

    static String getDataTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }
}
