package com.hi.insurance_agent.util;

import com.hi.insurance_agent.constant.DatasetConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 获取数据集中，子文件夹对应的保险条款名
 * 例如 112012/sms.pdf -> xxx保险-产品说明书
 */
public class GetFileName {

    // 缩写对应的含义
    public static Map<String, String> abbrMap = Map.of(
            "flbe", "费率表",
            "tk", "寿险条款",
            "mc", "责任条款",
            "sms", "产品说明书",
            "xjjz", "现金价值表"
    );

    // 路径-保险名对应表
    public static Map<String, String> InsuranceMap = CsvReader.readCsvToMap(DatasetConstant.CSV_PATH);

    public static String getExactFileName(String pdfPath) {
        //  获取文件名（不含扩展名）
        String originFileName = pdfPath.substring(pdfPath.lastIndexOf("/") + 1, pdfPath.lastIndexOf(".")).trim();
        //  匹配并替换为中文
        String typeName = abbrMap.getOrDefault(originFileName, originFileName);
        // 获取数据所在子目录
        String subDir = getSubDir(pdfPath);
        // 去除空格
        String insuranceName = InsuranceMap.get(subDir).replaceAll("\\s+", "");
        if (!pdfPath.contains("history"))
            return insuranceName + "-" + typeName;
        else
            return insuranceName + "-" + typeName + "-过期";
    }

    public static List<String> getFileNames(List<String> pdfPaths) {
        List<String> fileNames = new ArrayList<>();
        for (String pdfPath : pdfPaths) {
            fileNames.add(getExactFileName(pdfPath));
        }
        return fileNames;
    }

    public static String getSubDir(String pdfPath) {
        //  截取 "documents" 之后的路径
        int index = pdfPath.indexOf("documents");
        String relativePath = (index != -1) ? pdfPath.substring(index + 1 + +"documents".length()) : pdfPath;
        return relativePath.substring(0, relativePath.indexOf("/"));
    }
}
