package site.kuril.test.spring.ai.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RAG回答顾问 - 检索增强生成
 * 
 * <p>
 * 该类提供RAG（Retrieval-Augmented Generation）功能的实用方法。
 * 主要用于在AI对话中增强用户输入，通过从向量存储中检索相关文档来提供更丰富的上下文。
 * </p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>基于用户输入进行语义检索</li>
 *   <li>从向量存储中获取相关文档</li>
 *   <li>将检索结果作为上下文增强用户输入</li>
 *   <li>支持可配置的检索参数（topK、过滤条件等）</li>
 *   <li>提供消息增强功能，可直接集成到ChatClient中</li>
 * </ul>
 * 
 * @author AI Agent Station
 * @version 1.0
 */
@Slf4j
public class RagAnswerAdvisor {

    private static final String DEFAULT_USER_TEXT_ADVISE = """
            Context information is below.
            ---------------------
            {context}
            ---------------------
            Given the context and provided history information and not prior knowledge,
            reply to the user comment. If the answer is not in the context, inform
            the user that you can't answer the question.
            """;

    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final String userTextAdviseTemplate;
    private final boolean protectFromBlocking;

    /**
     * 构造函数
     * 
     * @param vectorStore 向量存储，用于检索相关文档
     * @param searchRequest 搜索请求配置，包含topK、过滤条件等参数
     */
    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this(vectorStore, searchRequest, DEFAULT_USER_TEXT_ADVISE, true);
    }

    /**
     * 完整构造函数
     * 
     * @param vectorStore 向量存储
     * @param searchRequest 搜索请求配置
     * @param userTextAdviseTemplate 用户文本建议模板
     * @param protectFromBlocking 是否保护免受阻塞
     */
    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, 
                           String userTextAdviseTemplate, boolean protectFromBlocking) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.userTextAdviseTemplate = userTextAdviseTemplate;
        this.protectFromBlocking = protectFromBlocking;
    }

    /**
     * 增强用户消息，添加从向量存储检索到的相关上下文
     * 
     * @param userMessage 原始用户消息
     * @return 增强后的用户消息，如果检索失败则返回原始消息
     */
    public String enhanceUserMessage(String userMessage) {
        
        log.debug("RAG顾问开始处理用户消息");
        
        if (!StringUtils.hasText(userMessage)) {
            log.debug("用户消息为空，跳过RAG处理");
            return userMessage;
        }

        log.debug("用户输入文本: {}", userMessage);

        try {
            // 从向量存储中检索相关文档
            List<Document> documents = retrieveDocuments(userMessage);
            
            if (documents.isEmpty()) {
                log.info("未检索到相关文档，返回原始消息");
                return userMessage;
            }

            log.info("检索到 {} 个相关文档", documents.size());

            // 构建增强的用户消息
            String enhancedMessage = enhanceMessageWithContext(userMessage, documents);
            
            return enhancedMessage;
            
        } catch (Exception e) {
            log.warn("RAG检索过程中发生错误: {}", e.getMessage());
            if (protectFromBlocking) {
                log.info("RAG错误保护已启用，返回原始消息");
                return userMessage;
            } else {
                throw new RuntimeException("RAG检索失败", e);
            }
        }
    }

    /**
     * 增强消息列表，为最后一个用户消息添加检索到的上下文
     * 
     * @param messages 原始消息列表
     * @return 增强后的消息列表
     */
    public List<Message> enhanceMessages(List<Message> messages) {
        
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        // 获取最后一个用户消息
        String lastUserText = getUserText(messages);
        
        if (!StringUtils.hasText(lastUserText)) {
            log.debug("未找到用户文本消息，跳过RAG处理");
            return messages;
        }

        // 增强用户消息
        String enhancedUserText = enhanceUserMessage(lastUserText);
        
        if (enhancedUserText.equals(lastUserText)) {
            // 没有进行增强，返回原始消息列表
            return messages;
        }

        // 替换最后一个用户消息
        List<Message> enhancedMessages = new ArrayList<>(messages);
        for (int i = enhancedMessages.size() - 1; i >= 0; i--) {
            if (enhancedMessages.get(i) instanceof UserMessage) {
                enhancedMessages.set(i, new UserMessage(enhancedUserText));
                break;
            }
        }

        return enhancedMessages;
    }

    /**
     * 获取检索结果的统计信息
     * 
     * @param userText 用户输入文本
     * @return 包含检索统计信息的对象
     */
    public RagSearchResult getSearchResult(String userText) {
        
        if (!StringUtils.hasText(userText)) {
            return new RagSearchResult(0, new ArrayList<>(), "用户输入为空");
        }

        try {
            List<Document> documents = retrieveDocuments(userText);
            return new RagSearchResult(documents.size(), documents, "检索成功");
            
        } catch (Exception e) {
            log.warn("RAG检索失败: {}", e.getMessage());
            return new RagSearchResult(0, new ArrayList<>(), "检索失败: " + e.getMessage());
        }
    }

    /**
     * 从消息列表中提取用户文本
     */
    private String getUserText(List<Message> chatMessages) {
        return chatMessages.stream()
                .filter(message -> message instanceof UserMessage)
                .map(message -> ((UserMessage) message).getText())
                .filter(StringUtils::hasText)
                .reduce((first, second) -> second) // 获取最后一个用户消息
                .orElse("");
    }

    /**
     * 从向量存储中检索相关文档
     */
    private List<Document> retrieveDocuments(String userText) {
        log.debug("开始向量检索，查询文本: {}", userText);
        
        // 创建基于用户输入的搜索请求
        SearchRequest.Builder searchBuilder = SearchRequest.from(searchRequest)
                .query(userText);
        
        SearchRequest finalSearchRequest = searchBuilder.build();
        
        log.debug("搜索参数 - TopK: {}, 过滤表达式: {}", 
                finalSearchRequest.getTopK(), 
                finalSearchRequest.getFilterExpression());
        
        List<Document> documents = vectorStore.similaritySearch(finalSearchRequest);
        
        log.debug("检索结果数量: {}", documents.size());
        
        // 记录检索到的文档信息（调试模式）
        if (log.isDebugEnabled()) {
            documents.forEach(doc -> 
                log.debug("文档内容片段: {}", 
                    doc.getText().length() > 100 ? 
                    doc.getText().substring(0, 100) + "..." : 
                    doc.getText())
            );
        }
        
        return documents;
    }

    /**
     * 使用检索到的文档增强消息
     */
    private String enhanceMessageWithContext(String userMessage, List<Document> documents) {
        
        // 构建上下文信息
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        log.debug("构建的上下文长度: {} 字符", context.length());

        // 使用模板构建增强的用户消息，将原始用户消息放在上下文后面
        String enhancedUserMessage = userTextAdviseTemplate.replace("{context}", context) + "\n\nUser Question: " + userMessage;

        log.debug("增强的用户消息总长度: {} 字符", enhancedUserMessage.length());

        log.info("RAG增强完成，添加了 {} 个文档作为上下文", documents.size());

        return enhancedUserMessage;
    }

    /**
     * 获取顾问名称
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * RAG检索结果类
     */
    public static class RagSearchResult {
        private final int documentCount;
        private final List<Document> documents;
        private final String status;

        public RagSearchResult(int documentCount, List<Document> documents, String status) {
            this.documentCount = documentCount;
            this.documents = documents;
            this.status = status;
        }

        public int getDocumentCount() {
            return documentCount;
        }

        public List<Document> getDocuments() {
            return documents;
        }

        public String getStatus() {
            return status;
        }

        public boolean isSuccess() {
            return "检索成功".equals(status);
        }

        @Override
        public String toString() {
            return String.format("RagSearchResult{documentCount=%d, status='%s'}", documentCount, status);
        }
    }
}
