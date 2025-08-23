package site.kuril.test.spring.ai.advisors;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * RAG顾问配置类
 * 
 * <p>
 * 该类用于配置RAG（检索增强生成）顾问的各种参数，包括：
 * </p>
 * <ul>
 *   <li>检索参数配置（topK、过滤条件等）</li>
 *   <li>向量存储配置</li>
 *   <li>提示词模板配置</li>
 *   <li>错误处理配置</li>
 * </ul>
 * 
 * @author AI Agent Station
 * @version 1.0
 */
@Data
@Builder
public class RagAdvisorConfig {

    /**
     * 向量存储实例
     */
    private final VectorStore vectorStore;

    /**
     * 检索的最大文档数量，默认为5
     */
    @Builder.Default
    private final Integer topK = 5;

    /**
     * 过滤表达式，用于筛选特定类型的文档
     * 例如：knowledge == 'article-prompt-words'
     */
    private final String filterExpression;

    /**
     * 相似度阈值，只返回相似度大于该阈值的文档
     */
    private final Double similarityThreshold;

    /**
     * 用户文本建议模板
     * 使用 {context} 占位符来插入检索到的上下文
     */
    @Builder.Default
    private final String userTextAdviseTemplate = """
            Context information is below.
            ---------------------
            {context}
            ---------------------
            Given the context and provided history information and not prior knowledge,
            reply to the user comment. If the answer is not in the context, inform
            the user that you can't answer the question.
            """;

    /**
     * 是否启用错误保护，当RAG检索失败时是否继续正常处理
     */
    @Builder.Default
    private final Boolean protectFromBlocking = true;

    /**
     * 是否启用调试日志
     */
    @Builder.Default
    private final Boolean enableDebugLogs = false;

    /**
     * 检索超时时间（秒）
     */
    @Builder.Default
    private final Integer timeoutSeconds = 10;

    /**
     * 创建SearchRequest对象
     * 
     * @return 配置好的SearchRequest实例
     */
    public SearchRequest createSearchRequest() {
        SearchRequest.Builder builder = SearchRequest.builder()
                .topK(topK);

        if (filterExpression != null && !filterExpression.trim().isEmpty()) {
            builder.filterExpression(filterExpression);
        }

        if (similarityThreshold != null) {
            builder.similarityThreshold(similarityThreshold);
        }

        return builder.build();
    }

    /**
     * 创建RagAnswerAdvisor实例
     * 
     * @return 配置好的RagAnswerAdvisor实例
     */
    public RagAnswerAdvisor createRagAdvisor() {
        return new RagAnswerAdvisor(
                vectorStore,
                createSearchRequest(),
                userTextAdviseTemplate,
                protectFromBlocking
        );
    }

    /**
     * 验证配置的有效性
     * 
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() {
        if (vectorStore == null) {
            throw new IllegalArgumentException("VectorStore不能为空");
        }

        if (topK == null || topK <= 0) {
            throw new IllegalArgumentException("topK必须大于0");
        }

        if (topK > 100) {
            throw new IllegalArgumentException("topK不应超过100，以避免性能问题");
        }

        if (similarityThreshold != null && (similarityThreshold < 0.0 || similarityThreshold > 1.0)) {
            throw new IllegalArgumentException("相似度阈值必须在0.0到1.0之间");
        }

        if (userTextAdviseTemplate == null || userTextAdviseTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("用户文本建议模板不能为空");
        }

        if (!userTextAdviseTemplate.contains("{context}")) {
            throw new IllegalArgumentException("用户文本建议模板必须包含{context}占位符");
        }

        if (timeoutSeconds != null && timeoutSeconds <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0秒");
        }
    }

    /**
     * 创建用于技术文档检索的默认配置
     * 
     * @param vectorStore 向量存储实例
     * @return 技术文档RAG配置
     */
    public static RagAdvisorConfig forTechnicalDocuments(VectorStore vectorStore) {
        return RagAdvisorConfig.builder()
                .vectorStore(vectorStore)
                .topK(5)
                .filterExpression("document_type == 'technical'")
                .similarityThreshold(0.7)
                .userTextAdviseTemplate("""
                        Based on the technical documentation below:
                        ---------------------
                        {context}
                        ---------------------
                        Please provide a technical answer to the user's question.
                        If the information is not available in the documentation,
                        please indicate that you need more specific documentation.
                        """)
                .protectFromBlocking(true)
                .enableDebugLogs(true)
                .timeoutSeconds(15)
                .build();
    }

    /**
     * 创建用于文章提示词的默认配置
     * 
     * @param vectorStore 向量存储实例
     * @return 文章提示词RAG配置
     */
    public static RagAdvisorConfig forArticlePrompts(VectorStore vectorStore) {
        return RagAdvisorConfig.builder()
                .vectorStore(vectorStore)
                .topK(5)
                .filterExpression("knowledge == 'article-prompt-words'")
                .similarityThreshold(0.6)
                .protectFromBlocking(true)
                .enableDebugLogs(false)
                .timeoutSeconds(10)
                .build();
    }

    /**
     * 创建用于FAQ检索的默认配置
     * 
     * @param vectorStore 向量存储实例
     * @return FAQ RAG配置
     */
    public static RagAdvisorConfig forFAQ(VectorStore vectorStore) {
        return RagAdvisorConfig.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .filterExpression("document_type == 'faq'")
                .similarityThreshold(0.8)
                .userTextAdviseTemplate("""
                        Based on the FAQ information below:
                        ---------------------
                        {context}
                        ---------------------
                        Please answer the user's question based on the FAQ.
                        If the exact answer is not in the FAQ, provide the closest relevant information.
                        """)
                .protectFromBlocking(true)
                .enableDebugLogs(false)
                .timeoutSeconds(5)
                .build();
    }
}
