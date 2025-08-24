package site.kuril.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * AI Agent 自动装配配置属性
 * 用于从配置文件中读取AI Agent自动装配相关配置
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.agent.auto-config")
public class AiAgentAutoConfigProperties {

    /**
     * 是否启用自动装配
     */
    private boolean enabled = false;

    /**
     * 需要自动装配的客户端ID列表
     * 支持逗号分隔的字符串或列表形式
     * 例如: ["3101,3102,3103,3104"] 或 ["3101", "3102", "3103", "3104"]
     */
    private List<String> clientIds;
}
