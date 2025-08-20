package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客户端配置值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientVO {

    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联的系统提示词ID列表
     */
    private List<String> promptIdList;

    /**
     * 关联的模型Bean名称
     */
    private String modelBeanName;

    /**
     * 关联的MCP工具Bean名称列表
     */
    private List<String> mcpBeanNameList;

    /**
     * 关联的顾问角色Bean名称列表
     */
    private List<String> advisorBeanNameList;

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