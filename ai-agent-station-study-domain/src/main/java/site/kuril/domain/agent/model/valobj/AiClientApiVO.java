package site.kuril.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI客户端API配置值对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientApiVO {

    /**
     * 全局唯一配置ID
     */
    private String apiId;

    /**
     * API基础URL
     */
    private String baseUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 补全API路径
     */
    private String completionsPath;

    /**
     * 嵌入API路径
     */
    private String embeddingsPath;

    /**
     * 状态：0-禁用，1-启用
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