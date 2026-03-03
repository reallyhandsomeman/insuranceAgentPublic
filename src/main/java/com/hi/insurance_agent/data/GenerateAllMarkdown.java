package com.hi.insurance_agent.data;

import com.hi.insurance_agent.advisor.LogAdvisor;
import com.hi.insurance_agent.constant.DatasetConstant;
import com.hi.insurance_agent.constant.PromptConstant;
import com.hi.insurance_agent.util.CollectPaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class GenerateAllMarkdown {
    private static final int THREAD_POOL_SIZE = 4; // 可根据 CPU 调整

    public static boolean generateMarkdown(ChatModel chatModel) throws Exception {

        ChatClient markdownChatClient = ChatClient.builder(chatModel)
                .defaultSystem(PromptConstant.MARKDOWN_GENERATE_PROMPT)
                .defaultAdvisors(new LogAdvisor())
                .build();

        List<String> pdfPathList = CollectPaths.getAllDataPath(DatasetConstant.Dataset_DIR, "pdf");
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        AtomicBoolean hasError = new AtomicBoolean(false);

        List<CompletableFuture<Void>> futures = pdfPathList.stream()
                .filter(pdfPath -> {
                    String fileName = pdfPath.substring(pdfPath.lastIndexOf("/") + 1, pdfPath.lastIndexOf(".")).trim();
                    return !fileName.equals("flbe");
                })
                .map(pdfPath -> CompletableFuture.runAsync(() -> {
                    if (hasError.get()) return; // 立即停止新任务

                    try {
                        String markdownPath = pdfPath.replace(".pdf", ".md").replace("documents", "markdown");
                        File markdownFile = new File(markdownPath);
                        if (markdownFile.exists()) {
                            log.info("Skip (exists): {}", markdownPath);
                            return;
                        }

                        PdfToMarkdown.convertPdfToMarkdown(pdfPath, markdownPath, markdownChatClient);
                    } catch (Exception e) {
                        hasError.set(true);
                        log.error("Error converting {}: {}", pdfPath, e.getMessage());
                        throw new RuntimeException(e);
                    }
                }, executor))
                .toList();

        try {
            // 当任意一个任务失败时立即触发异常
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return true;
        } catch (CompletionException e) {
            log.error("Terminating due to error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            hasError.set(true);
            executor.shutdownNow();
            throw e;
        } finally {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
        }
    }
}

