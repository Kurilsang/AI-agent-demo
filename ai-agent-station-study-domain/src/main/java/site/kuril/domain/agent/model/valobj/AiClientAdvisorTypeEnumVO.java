package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * AI客户端顾问类型策略枚举
 * 用于根据不同类型创建相应的顾问对象
 */
@Slf4j
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientAdvisorTypeEnumVO {

    CHAT_MEMORY("ChatMemory", "上下文记忆（内存模式）") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, Object vectorStore) {
            // TODO: 实现ChatMemory顾问创建逻辑
            AiClientAdvisorVO.ChatMemory chatMemory = aiClientAdvisorVO.getChatMemory();
            if (chatMemory == null) {
                aiClientAdvisorVO.parseExtParam();
                chatMemory = aiClientAdvisorVO.getChatMemory();
            }
            // 暂时返回配置对象，实际实现时需要创建PromptChatMemoryAdvisor
            return chatMemory;
        }
    },
    
    RAG_ANSWER("RagAnswer", "知识库") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, Object vectorStore) {
            // TODO: 实现RagAnswer顾问创建逻辑
            AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
            if (ragAnswer == null) {
                aiClientAdvisorVO.parseExtParam();
                ragAnswer = aiClientAdvisorVO.getRagAnswer();
            }
            // 暂时返回配置对象，实际实现时需要创建RagAnswerAdvisor
            return ragAnswer;
        }
    },
    
    TECHNICAL_EXPERT("TECHNICAL_EXPERT", "技术专家顾问") {
        @Override
        public Object createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, Object vectorStore) {
            // TODO: 实现TechnicalExpert顾问创建逻辑
            // 暂时返回配置对象本身
            log.info("创建技术专家顾问: advisorId={}, advisorName={}", 
                    aiClientAdvisorVO.getAdvisorId(), 
                    aiClientAdvisorVO.getAdvisorName());
            return aiClientAdvisorVO;
        }
    }
    
    ;

    private String code;
    private String info;
    
    // 静态Map缓存，用于快速查找
    private static final Map<String, AiClientAdvisorTypeEnumVO> CODE_MAP = new HashMap<>();
    
    // 静态初始化块，在类加载时初始化Map
    static {
        for (AiClientAdvisorTypeEnumVO enumVO : values()) {
            CODE_MAP.put(enumVO.getCode(), enumVO);
        }
    }
    
    /**
     * 策略方法：创建顾问对象
     * @param aiClientAdvisorVO 顾问配置对象
     * @param vectorStore 向量存储
     * @return 顾问对象
     */
    public abstract Object createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, Object vectorStore);
    
    /**
     * 根据code获取枚举
     * @param code 编码
     * @return 枚举对象
     */
    public static AiClientAdvisorTypeEnumVO getByCode(String code) {
        AiClientAdvisorTypeEnumVO enumVO = CODE_MAP.get(code);
        if (enumVO == null) {
            throw new RuntimeException("err! advisorType " + code + " not exist!");
        }
        return enumVO;
    }

} 