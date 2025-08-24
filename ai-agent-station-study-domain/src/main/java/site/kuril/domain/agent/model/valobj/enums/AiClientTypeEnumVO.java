package site.kuril.domain.agent.model.valobj.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI客户端类型枚举
 * 用于 AutoAgent 多轮对话执行流程中的不同客户端类型
 */
@Getter
@AllArgsConstructor
public enum AiClientTypeEnumVO {

    DEFAULT("DEFAULT", "通用对话客户端", "默认的通用对话客户端"),
    TASK_ANALYZER_CLIENT("TASK_ANALYZER_CLIENT", "任务分析客户端", "专业的任务分析师，负责分析任务状态和制定执行策略"),
    PRECISION_EXECUTOR_CLIENT("PRECISION_EXECUTOR_CLIENT", "精准执行客户端", "精准任务执行器，负责严格按照策略执行具体任务"),
    QUALITY_SUPERVISOR_CLIENT("QUALITY_SUPERVISOR_CLIENT", "质量监督客户端", "专业的质量监督员，负责监督和评估执行质量"),
    RESPONSE_ASSISTANT("RESPONSE_ASSISTANT", "智能响应助手", "智能响应助手，负责响应式处理和最终结果输出"),
    ;

    private final String code;
    private final String name;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static AiClientTypeEnumVO getByCode(String code) {
        for (AiClientTypeEnumVO enumVO : values()) {
            if (enumVO.getCode().equals(code)) {
                return enumVO;
            }
        }
        return DEFAULT;
    }

    /**
     * 根据name获取枚举
     */
    public static AiClientTypeEnumVO getByName(String name) {
        for (AiClientTypeEnumVO enumVO : values()) {
            if (enumVO.getName().equals(name)) {
                return enumVO;
            }
        }
        return DEFAULT;
    }

}
