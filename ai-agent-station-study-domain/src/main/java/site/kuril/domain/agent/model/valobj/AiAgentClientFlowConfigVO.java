package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI智能体客户端流程配置值对象
 * 用于表示智能体执行流程中的客户端配置信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentClientFlowConfigVO {

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 执行序列号
     */
    private Integer sequence;

}
