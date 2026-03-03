package com.hi.insurance_agent.tools;

import com.hi.insurance_agent.constant.DatasetConstant;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 读取数据集中的费率表flbe.pdf
 */
public class ReadRatePdfTool {

    private final String Dataset_DIR = DatasetConstant.Dataset_DIR;

    /**
     * 读取对应保险的费率表flbe.pdf
     *
     * @param index 保险编号
     * @return
     */
    @Tool(description = "Read content from a RatePdf")
    public String readRatePdf(@ToolParam(description = "Index of the insurance to read, as specified in RAG metadata.index.") String index) throws IOException {
        // 拼接目录路径，例如 Dataset_DIR/111002/
        Path dirPath = Paths.get(Dataset_DIR, index);

        // 检查目录是否存在
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("Directory not found for index: " + index);
        }

        List<String> pdfList;
        try (Stream<Path> files = Files.walk(dirPath)) { //  递归遍历
            pdfList = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("flbe.pdf"))
                    .map(Path::toString)
                    .toList();

            if (pdfList.isEmpty()) {
                throw new IllegalArgumentException("No flbe.pdf found under: " + dirPath);
            }
        }
        String filePath = pdfList.getFirst();

        StringBuilder result = new StringBuilder();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();

            int pageIndex = 0;
            for (PageIterator it = extractor.extract(); it.hasNext(); ) {
                Page page = it.next();
                pageIndex++;

                // === ① 提取整页文字（去除数字） ===
                stripper.setStartPage(pageIndex);
                stripper.setEndPage(pageIndex);
                String pageText = stripper.getText(document);

                // 过滤掉主要是数字、制表符或空格的行
                String filteredText = Arrays.stream(pageText.split("\n"))
                        .filter(line -> !line.trim().matches("^[\\d\\s\\.]+$")) // 全数字行
                        .filter(line -> line.replaceAll("[\\d\\.\\s]", "").length() > 0) // 全空/数字行
                        .collect(Collectors.joining("\n"));

                result.append("【Page ").append(pageIndex).append(" Info】\n")
                        .append(filteredText.trim()).append("\n\n");

                // === ② 提取表格 ===
                result.append("【Page ").append(pageIndex).append(" Table】\n");
                for (Table table : sea.extract(page)) {
                    table.getRows().forEach(row -> {
                        row.forEach(cell -> result.append(cell.getText()).append("\t"));
                        result.append("\n");
                    });
                    result.append("\n");
                }
                result.append("--- End of Page ").append(pageIndex).append(" ---\n\n");
            }

        } catch (IOException e) {
            return "No such file %s!".formatted(filePath);
        }

        return result.toString();

    }
}
