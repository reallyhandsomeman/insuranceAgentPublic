package com.hi.insurance_agent.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CollectPaths {
    /**
     * 递归扫描文件夹，收集 PDF/markdown 文件路径
     */
    private static void collectPaths(File folder, List<String> pdfPaths, String fileType) {
        if (!fileType.equals("pdf") && !fileType.equals("md")) {
            throw new IllegalArgumentException("Invalid file type: " + fileType + ". Only pdf or md");
        }
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                collectPaths(file, pdfPaths, fileType); // 递归进入子目录
            } else if (file.isFile() && file.getName().toLowerCase().endsWith("." + fileType.trim())) {
                pdfPaths.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * 获取数据目录 dataPath 下的所有文件路径
     *
     * @param dataPath 数据目录路径
     * @return 包含所有 PDF 文件完整路径的字符串（用换行分隔）
     */
    public static List<String> getAllDataPath(String dataPath, String fileType) {
        List<String> paths = new ArrayList<>();
        File root = new File(dataPath);
        if (!root.exists()) {
            return new ArrayList<>();
        }

        // 递归收集 PDF 文件路径
        collectPaths(root, paths, fileType);
        // 按路径字符串排序
        paths.sort(String::compareTo);
        // 拼接成一个字符串返回
        return paths;
    }
}
