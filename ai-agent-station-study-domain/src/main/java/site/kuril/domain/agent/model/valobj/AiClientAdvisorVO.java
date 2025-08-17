package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

} 