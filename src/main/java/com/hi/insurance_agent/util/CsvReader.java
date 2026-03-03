package com.hi.insurance_agent.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CsvReader {
    /**
     * 读取 CSV 文件（格式：编号,名称），并返回 Map<编号, 名称>
     *
     * @param filePath CSV 文件路径（例如 "data/insurance_map.csv"）
     * @return Map<String, String> 编号 -> 名称
     */
    public static Map<String, String> readCsvToMap(String filePath) {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) { // 跳过表头
                    isFirstLine = false;
                    continue;
                }

                // 去掉可能的 BOM 头和多余空格
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                // 按逗号分割（只切一次，防止名称中有逗号）
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;

                String id = parts[0].trim();
                String name = parts[1].trim();

                if (!id.isEmpty() && !name.isEmpty()) {
                    map.put(id, name);
                }
            }
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return map;
    }
}
