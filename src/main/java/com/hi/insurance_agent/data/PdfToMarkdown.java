package com.hi.insurance_agent.data;

import com.hi.insurance_agent.util.GetFileName;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


@Slf4j
public class PdfToMarkdown {

    /**
     * 解析pdf文件的内容
     *
     * @param pdfPath pdf路径
     * @return pdf文本
     */
    public static String parsePdf(String pdfPath) {
        String pdfText = "";
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            pdfText = stripper.getText(document);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return pdfText;
    }

    /**
     * 将 PDF 转为 Markdown 并保存为文件
     *
     * @param pdfPath  PDF 文件路径
     * @param savePath 输出 Markdown 文件路径
     * @return 生成的 Markdown 文件路径
     */
    public static boolean convertPdfToMarkdown(String pdfPath, String savePath) throws Exception {

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IOException("pdf文件不存在: " + pdfPath);
        }
        File markdownFile = new File(savePath);
        File parentDir = markdownFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建保存目录: " + parentDir.getAbsolutePath());
            }
        }
        String pdfText = parsePdf(pdfPath);

        // 写入 .md 文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
            writer.write(pdfText);
        }

        log.info("无AI润色的Markdown 文件已保存到: " + savePath);
        return true;
    }

    /**
     * 将 PDF 转为 Markdown 并保存为文件，用AI润色Markdown排版格式
     *
     * @param pdfPath  PDF 文件路径
     * @param savePath 输出 Markdown 文件路径
     * @return 生成的 Markdown 文件路径
     */
    public static boolean convertPdfToMarkdown(String pdfPath, String savePath, ChatClient chatClient) throws Exception {

        File pdfFile = new File(pdfPath);
        if (!pdfFile.exists()) {
            throw new IOException("pdf文件不存在: " + pdfPath);
        }
        File markdownFile = new File(savePath);
        File parentDir = markdownFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建保存目录: " + parentDir.getAbsolutePath());
            }
        }
        /* 需要提供
         * title: xxx
         * index:
         * category: 保险产品说明
         * effective_date: 2023-01-01
         * abolition_statement: 本文件未被废止，现行有效
         */
        String pdfText = parsePdf(pdfPath);
        String abolition_statement = "false";
        String fileName = GetFileName.getExactFileName(pdfPath);
        String subDir = GetFileName.getSubDir(pdfPath);
        List<String> splitList = List.of(fileName.split("-"));
        if (splitList.getLast().contains("过期")) {
            abolition_statement = "true";
        }
        String category = splitList.get(1);
        String title = splitList.get(0);
        // 拼接 YAML 元信息块
        String metaData = String.format("""
                Here is meta data:
                ---
                title: %s
                index: %s
                category: %s
                effective_date:
                abolition_statement: %s
                efg: 
                path:
                ---
                """, title, subDir, category, abolition_statement);
        // 让AI处理
        String markdownText = checkMarkdownByAI(metaData, pdfText, chatClient);

        // 写入 .md 文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(savePath))) {
            writer.write(markdownText);
        }

        log.info("AI润色的Markdown 文件已保存到: " + savePath);
        return true;
    }

    public static String checkMarkdownByAI(String prompt, String text, ChatClient chatClient) {
        String chatId = UUID.randomUUID().toString();
        ChatResponse response = chatClient
                .prompt(prompt)
                .user(text)
                // 注入参数，包括对话id和上下文记忆长度.  传入对话id作为保存文件名
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 1))
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }

}