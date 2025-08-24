package site.kuril.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AutoAgent执行结果实体
 * 用于SSE流式响应中的数据传输
 * 
 * @author Kuril
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoAgentExecuteResultEntity {

    /**
     * 数据类型：analysis(分析阶段), execution(执行阶段), supervision(监督阶段), summary(总结阶段), error(错误信息), complete(完成标识)
     */
    private String type;

    /**
     * 子类型标识，用于前端细粒度展示
     * 细分类型：
     * - analysis_status(任务状态分析), analysis_history(执行历史评估), analysis_strategy(下一步策略), analysis_progress(完成度评估)
     * - execution_target(执行目标), execution_process(执行过程), execution_result(执行结果), execution_quality(质量检查)
     * - supervision_assessment(质量评估), supervision_issues(问题识别), supervision_suggestions(改进建议), supervision_score(质量评分)
     * - summary_overview(总结概览), summary_completed_work(已完成工作), summary_incomplete_reasons(未完成原因), summary_evaluation(效果评估)
     */
    private String subType;

    /**
     * 当前步骤
     */
    private Integer step;

    /**
     * 数据内容
     */
    private String content;

    /**
     * 是否完成
     */
    private Boolean completed;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 会话ID
     */
    private String sessionId;

    // =================
    // 创建分析阶段结果
    // =================

    /**
     * 创建分析阶段结果
     */
    public static AutoAgentExecuteResultEntity createAnalysisResult(Integer step, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("analysis")
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建分析阶段细分结果
     */
    public static AutoAgentExecuteResultEntity createAnalysisSubResult(Integer step, String subType, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("analysis")
                .subType(subType)
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    // =================
    // 创建执行阶段结果
    // =================

    /**
     * 创建执行阶段结果
     */
    public static AutoAgentExecuteResultEntity createExecutionResult(Integer step, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("execution")
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建执行阶段细分结果
     */
    public static AutoAgentExecuteResultEntity createExecutionSubResult(Integer step, String subType, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("execution")
                .subType(subType)
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    // =================
    // 创建监督阶段结果
    // =================

    /**
     * 创建监督阶段结果
     */
    public static AutoAgentExecuteResultEntity createSupervisionResult(Integer step, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("supervision")
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建监督阶段细分结果
     */
    public static AutoAgentExecuteResultEntity createSupervisionSubResult(Integer step, String subType, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("supervision")
                .subType(subType)
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    // =================
    // 创建总结阶段结果
    // =================

    /**
     * 创建总结阶段结果
     */
    public static AutoAgentExecuteResultEntity createSummaryResult(Integer step, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("summary")
                .step(step)
                .content(content)
                .completed(true)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建总结阶段细分结果
     */
    public static AutoAgentExecuteResultEntity createSummarySubResult(Integer step, String subType, String content, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("summary")
                .subType(subType)
                .step(step)
                .content(content)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    // =================
    // 创建错误和完成结果
    // =================

    /**
     * 创建错误结果
     */
    public static AutoAgentExecuteResultEntity createErrorResult(Integer step, String errorMessage, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("error")
                .step(step)
                .content(errorMessage)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建完成标识
     */
    public static AutoAgentExecuteResultEntity createCompleteResult(Integer step, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("complete")
                .step(step)
                .content("任务执行完成")
                .completed(true)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建步骤开始标识
     */
    public static AutoAgentExecuteResultEntity createStepStartResult(Integer step, String stepName, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("step_start")
                .step(step)
                .content("开始执行：" + stepName)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }

    /**
     * 创建步骤完成标识
     */
    public static AutoAgentExecuteResultEntity createStepCompleteResult(Integer step, String stepName, String sessionId) {
        return AutoAgentExecuteResultEntity.builder()
                .type("step_complete")
                .step(step)
                .content("完成执行：" + stepName)
                .completed(false)
                .timestamp(System.currentTimeMillis())
                .sessionId(sessionId)
                .build();
    }
}
