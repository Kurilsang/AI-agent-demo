package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI客户端MCP工具配置值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientToolMcpVO {

    /**
     * MCP ID
     */
    private String mcpId;

    /**
     * MCP名称
     */
    private String mcpName;

    /**
     * 传输类型(sse/stdio)
     */
    private String transportType;

    /**
     * 传输配置(sse/stdio)
     */
    private String transportConfig;

    /**
     * SSE传输配置对象
     */
    private TransportConfigSse transportConfigSse;

    /**
     * Stdio传输配置对象
     */
    private TransportConfigStdio transportConfigStdio;

    /**
     * 请求超时时间(分钟)
     */
    private Integer requestTimeout;

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
     * SSE传输配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigSse {
        private String baseUri;
        private String sseEndpoint;
    }

    /**
     * Stdio传输配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransportConfigStdio {
        private Map<String, Stdio> stdio;
        
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Stdio {
            private String command;
            private String[] args;
            private Map<String, String> env;
        }
    }

} 