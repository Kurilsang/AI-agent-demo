package site.kuril.domain.agent.model.valobj;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

/**
 * AI客户端顾问配置值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientAdvisorVO {

    /**
     * 顾问ID
     */
    private String advisorId;

    /**
     * 顾问名称
     */
    private String advisorName;

    /**
     * 顾问类型(PromptChatMemory/RagAnswer/SimpleLoggerAdvisor等)
     */
    private String advisorType;

    /**
     * 顺序号
     */
    private Integer orderNum;

    /**
     * 扩展参数配置，json 记录
     */
    private String extParam;

    /**
     * ChatMemory配置对象
     */
    private ChatMemory chatMemory;

    /**
     * RagAnswer配置对象
     */
    private RagAnswer ragAnswer;

    /**
     * 状态(0:禁用,1:启用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 解析扩展参数
     */
    public void parseExtParam() {
        if (StringUtils.isNotBlank(extParam)) {
            try {
                if ("ChatMemory".equals(advisorType)) {
                    this.chatMemory = JSON.parseObject(extParam, ChatMemory.class);
                } else if ("RagAnswer".equals(advisorType)) {
                    this.ragAnswer = JSON.parseObject(extParam, RagAnswer.class);
                }
            } catch (Exception e) {
                // 如果解析失败，保持原值，记录警告日志
                System.err.println("解析顾问配置失败: " + e.getMessage());
            }
        }
    }

    /**
     * ChatMemory配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatMemory {
        /**
         * 最大消息数量
         */
        private Integer maxMessages;
    }

    /**
     * RagAnswer配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RagAnswer {
        /**
         * TopK配置
         */
        private Integer topK;
        
        /**
         * 过滤表达式
         */
        private String filterExpression;
    }

} 