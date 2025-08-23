package site.kuril.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行 Agent 请求实体对象
 * 用于封装 AutoAgent 执行所需的基本参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCommandEntity {

    /**
     * AI智能体ID
     */
    private String aiAgentId;

    /**
     * 用户输入消息
     */
    private String message;

    /**
     * 会话ID，用于保持对话连续性
     */
    private String sessionId;

    /**
     * 最大执行步数，防止无限循环
     */
    private Integer maxStep;

    /**
     * 扩展参数，用于传递额外的配置信息
     */
    private String extParam;

}
