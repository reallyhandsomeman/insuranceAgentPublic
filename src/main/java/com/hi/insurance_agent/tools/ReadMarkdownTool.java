package com.hi.insurance_agent.tools;

import com.hi.insurance_agent.constant.DatasetConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadMarkdownTool {

    private final String Dataset_DIR = DatasetConstant.MARKDOWN_PATH;

    @Tool(description = "Read content from a markdown")
    public String readMarkdown(@ToolParam(description = "Path to the Markdown file (.md) to read, as specified in RAG metadata.path.") String subDir) {
        String filePath = Dataset_DIR + "/" + subDir;
        Path path = Paths.get(filePath);

        try {
            //  检查扩展名是否为 .md
            String fileName = path.getFileName().toString().toLowerCase();
            if (!fileName.endsWith(".md")) {
                throw new IllegalArgumentException("仅支持 Markdown 文件: " + subDir);
            }

            // 检查是否存在
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("文件不存在: " + subDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // 读取整个 Markdown 文件内容为字符串
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read markdown file: " + subDir, e);
        }

    }
}
