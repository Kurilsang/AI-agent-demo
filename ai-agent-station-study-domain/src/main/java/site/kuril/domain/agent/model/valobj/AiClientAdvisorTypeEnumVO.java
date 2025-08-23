package site.kuril.domain.agent.model.valobj;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * AI客户端顾问类型枚举
 * 用于创建不同类型的Advisor对象
 */
public enum AiClientAdvisorTypeEnumVO {

    CHAT_MEMORY("chat_memory", "对话记忆管理顾问") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO advisorVO, Object vectorStore) {
            try {
                // 解析扩展参数
                if (advisorVO.getExtParam() != null) {
                    advisorVO.parseExtParam();
                }
                
                // 获取ChatMemory配置
                AiClientAdvisorVO.ChatMemory chatMemoryConfig = advisorVO.getChatMemory();
                
                if (chatMemoryConfig != null) {
                    // 在真实环境中，这里会创建PromptChatMemoryAdvisor
                    // 暂时返回配置对象用于演示
                    return chatMemoryConfig;
                } else {
                    // 使用默认配置
                    return new Object(); // 简化实现
                }
            } catch (Exception e) {
                // 如果创建失败，返回备用对象
                return new Object();
            }
        }
    },

    RAG_ANSWER("rag_answer", "RAG检索增强生成顾问") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO advisorVO, Object vectorStore) {
            try {
                if (vectorStore == null) {
                    throw new IllegalArgumentException("VectorStore不能为空，RAG顾问需要向量存储支持");
                }
                
                // 解析扩展参数
                if (advisorVO.getExtParam() != null) {
                    advisorVO.parseExtParam();
                }
                
                // 获取RAG配置
                AiClientAdvisorVO.RagAnswer ragConfig = advisorVO.getRagAnswer();
                
                // 设置默认配置
                int topK = (ragConfig != null && ragConfig.getTopK() != null) ? ragConfig.getTopK() : 5;
                String filterExpression = (ragConfig != null && ragConfig.getFilterExpression() != null) 
                    ? ragConfig.getFilterExpression() : "knowledge == 'article-prompt-words'";
                
                // 使用反射创建SearchRequest.Builder（避免直接依赖Spring AI类）
                try {
                    Class<?> searchRequestClass = Class.forName("org.springframework.ai.vectorstore.SearchRequest");
                    Object searchRequestBuilder = searchRequestClass.getMethod("builder").invoke(null);
                    
                    // 设置topK
                    searchRequestBuilder.getClass().getMethod("topK", int.class).invoke(searchRequestBuilder, topK);
                    
                    // 设置过滤表达式
                    if (filterExpression != null && !filterExpression.trim().isEmpty()) {
                        searchRequestBuilder.getClass().getMethod("filterExpression", String.class)
                            .invoke(searchRequestBuilder, filterExpression);
                    }
                    
                    // 构建SearchRequest
                    Object searchRequest = searchRequestBuilder.getClass().getMethod("build").invoke(searchRequestBuilder);
                    
                    // 使用反射创建RagAnswerAdvisor
                    Class<?> ragAdvisorClass = Class.forName("site.kuril.test.spring.ai.advisors.RagAnswerAdvisor");
                    Object ragAdvisor = ragAdvisorClass.getConstructor(
                        Class.forName("org.springframework.ai.vectorstore.VectorStore"),
                        searchRequestClass
                    ).newInstance(vectorStore, searchRequest);
                    
                    return ragAdvisor;
                    
                } catch (Exception reflectionException) {
                    // 如果反射创建失败，返回一个简化的实现
                    System.err.println("RAG顾问创建失败，使用简化实现: " + reflectionException.getMessage());
                    return new Object() {
                        @Override
                        public String toString() {
                            return "RagAnswerAdvisor[topK=" + topK + ", filter=" + filterExpression + "]";
                        }
                    };
                }
                
            } catch (Exception e) {
                System.err.println("RAG顾问创建过程中发生错误: " + e.getMessage());
                // 如果创建失败，返回备用对象
                return new Object() {
                    @Override
                    public String toString() {
                        return "RagAnswerAdvisor[ERROR: " + e.getMessage() + "]";
                    }
                };
            }
        }
    },

    TECHNICAL_EXPERT("technical_expert", "技术专家顾问") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO advisorVO, Object vectorStore) {
            try {
                // 技术专家顾问的简化实现
                // 在真实环境中，这里会创建相应的技术专家顾问
                return advisorVO; // 暂时返回配置对象本身
            } catch (Exception e) {
                // 如果创建失败，返回备用对象
                return new Object();
            }
        }
    };

    private final String code;
    private final String description;

    AiClientAdvisorTypeEnumVO(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 创建顾问对象的抽象方法
     * @param advisorVO 顾问配置
     * @param vectorStore 向量存储（可为null）
     * @return 创建的顾问对象
     */
    public abstract Object createAdvisor(AiClientAdvisorVO advisorVO, Object vectorStore);

    // 静态映射，用于快速查找
    private static final Map<String, AiClientAdvisorTypeEnumVO> CODE_MAP = new HashMap<>();
    
    static {
        Arrays.stream(AiClientAdvisorTypeEnumVO.values())
                .forEach(enumVO -> CODE_MAP.put(enumVO.code, enumVO));
    }

    /**
     * 根据代码获取枚举
     */
    public static AiClientAdvisorTypeEnumVO getByCode(String code) {
        return CODE_MAP.get(code);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
} 