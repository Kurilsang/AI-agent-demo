package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI客户端系统提示词配置值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientSystemPromptVO {

    /**
     * 提示词ID
     */
    private String promptId;

    /**
     * 提示词名称
     */
    private String promptName;

    /**
     * 提示词内容
     */
    private String promptContent;

    /**
     * 描述
     */
    private String description;

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