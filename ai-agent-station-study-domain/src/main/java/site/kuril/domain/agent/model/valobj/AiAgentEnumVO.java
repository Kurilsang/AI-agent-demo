package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI Agent 枚举值对象
 * 管理命令类型和数据策略映射
 */
@Getter
@AllArgsConstructor
public enum AiAgentEnumVO {

    AI_CLIENT("client", "aiClientLoadDataStrategy", "ai_client", "ai_client_"),
    AI_CLIENT_API("api", "aiClientApiLoadDataStrategy", "ai_client_api", "ai_client_api_"),
    AI_CLIENT_MODEL("model", "aiClientModelLoadDataStrategy", "ai_client_model", "ai_client_model_"),
    AI_CLIENT_TOOL_MCP("tool_mcp", "aiClientToolMcpLoadDataStrategy", "ai_client_tool_mcp", "ai_client_tool_mcp_"),
    AI_CLIENT_SYSTEM_PROMPT("system_prompt", "aiClientSystemPromptLoadDataStrategy", "ai_client_system_prompt", "ai_client_system_prompt_"),
    AI_CLIENT_ADVISOR("advisor", "aiClientAdvisorLoadDataStrategy", "ai_client_advisor", "ai_client_advisor_");

    /**
     * 命令类型代码
     */
    private final String code;

    /**
     * 数据加载策略
     */
    private final String loadDataStrategy;

    /**
     * 数据名称
     */
    private final String dataName;

    /**
     * Bean名称前缀
     */
    private final String beanNamePrefix;

    /**
     * 根据代码获取枚举
     */
    public static AiAgentEnumVO getByCode(String code) {
        for (AiAgentEnumVO enumVO : values()) {
            if (enumVO.getCode().equals(code)) {
                return enumVO;
            }
        }
        throw new IllegalArgumentException("未找到对应的枚举类型: " + code);
    }

    /**
     * 获取Bean名称
     */
    public String getBeanName(String id) {
        return beanNamePrefix + id;
    }

} 