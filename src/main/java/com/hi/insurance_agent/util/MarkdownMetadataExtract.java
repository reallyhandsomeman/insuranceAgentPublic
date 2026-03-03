package com.hi.insurance_agent.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MarkdownMetadataExtract {

    /**
     * 抽取markdown的yaml元数据
     *
     * @param markdownPath
     * @return
     */
    public static Map<String, String> getMetadata(String markdownPath) {
        String content = "";
        try {
            content = Files.readString(Paths.get(markdownPath));
        } catch (Exception e) {
            log.info("MarkdownMetadataExtract getMetadata error{}", e.getMessage());
            return new LinkedHashMap<>();
        }

        String[] parts = content.split("---");
        String metadata = "";
        if (parts.length > 2) {
            metadata = parts[1].trim();
        }

        Map<String, String> map = new LinkedHashMap<>(); // 保持原顺序
        Pattern pattern = Pattern.compile("^(\\S+):\\s*(.*)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(metadata);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            map.put(key, value);
        }
        return map;
    }

}
