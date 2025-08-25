package site.kuril.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import site.kuril.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

// 移除静态导入，使用字符串常量

/**
 * Step1: 任务分析节点
 * 负责分析当前任务状态，评估执行进度，并制定下一步策略
 */
@Slf4j
@Service("step1AnalyzerNode")
public class Step1AnalyzerNode extends AbstractExecuteSupport {

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n🎯 === 执行第 {} 步：任务分析 ===", dynamicContext.getStep());

        // 发送步骤开始通知
        sendStepStart(dynamicContext, "任务分析和状态判断", requestParameter.getSessionId());

        // 构建分析提示词
        String analysisPrompt = buildAnalysisPrompt(requestParameter, dynamicContext);

        // 获取任务分析客户端
        AiAgentClientFlowConfigVO analyzerConfig = dynamicContext.getAiAgentClientFlowConfigVOMap()
                .get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());
        
        if (analyzerConfig == null) {
            log.error("❌ 未找到任务分析客户端配置");
            throw new RuntimeException("未找到任务分析客户端配置");
        }

        ChatClient chatClient = getChatClientByClientId(analyzerConfig.getClientId());

        // 执行任务分析
        log.info("🤔 开始任务状态分析...");
        String analysisResult = chatClient
                .prompt(analysisPrompt)
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .maxTokens(2000)
                        .temperature(0.3)
                        .build())
                .advisors(a -> a
                        .param("CHAT_MEMORY_CONVERSATION_ID", requestParameter.getSessionId())
                        .param("CHAT_MEMORY_RETRIEVE_SIZE", 1024))
                .call().content();

        // 解析分析结果并发送SSE
        parseAnalysisResult(dynamicContext, analysisResult, requestParameter.getSessionId());
        
        // 将分析结果保存到动态上下文中
        dynamicContext.setValue("analysisResult", analysisResult);

        // 检查任务完成状态
        if (isTaskCompleted(analysisResult)) {
            log.info("✅ 任务分析显示已完成！");
            dynamicContext.setCompleted(true);
        } else {
            log.info("🔄 任务需要继续执行");
        }

        // 发送步骤完成通知
        sendStepComplete(dynamicContext, "任务分析和状态判断", requestParameter.getSessionId());

        return "ANALYSIS_COMPLETED";
    }

    @Override
    public DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getExecuteHandler("step4LogExecutionSummaryNode");
        }
        
        // 否则继续执行下一步：精准执行
        return getExecuteHandler("step2PrecisionExecutorNode");
    }

    /**
     * 构建任务分析提示词
     */
    private String buildAnalysisPrompt(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return String.format("""
                **原始用户需求:** %s
                
                **当前执行步骤:** 第 %d 步 (最大 %d 步)
                
                **历史执行记录:** 
                %s
                
                **当前任务:** %s
                
                请分析当前任务状态，评估执行进度，并制定下一步策略。
                
                **重要提示:**
                - 如果用户需求已经得到满足，完成度为100%%，必须输出COMPLETED
                - 如果任务还需要继续处理，输出CONTINUE
                - 简单任务（如1+1）在得到正确答案后应该COMPLETED
                
                **请严格按照以下格式输出:**
                
                **任务状态分析:**
                [当前任务完成情况的详细分析]
                
                **执行历史评估:**
                [对已完成工作的质量和效果评估]
                
                **下一步策略:**
                [具体的下一步执行计划和策略]
                
                **完成度评估:** [0-100]%%
                
                **任务状态:** [CONTINUE/COMPLETED]
                
                **注意:** 如果完成度为100%%，任务状态必须为COMPLETED！
                """,
                requestParameter.getMessage(),
                dynamicContext.getStep(),
                dynamicContext.getMaxStep(),
                !dynamicContext.getExecutionHistory().isEmpty() ? 
                        dynamicContext.getExecutionHistory().toString() : "[首次执行]",
                dynamicContext.getCurrentTask()
        );
    }

    /**
     * 解析任务分析结果并发送SSE
     */
    private void parseAnalysisResult(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                   String analysisResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n📊 === 第 {} 步分析结果 ===", step);
        
        String[] lines = analysisResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("任务状态分析:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_status";
                sectionContent = new StringBuilder();
                log.info("\n🎯 任务状态分析:");
                continue;
            } else if (line.contains("执行历史评估:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_history";
                sectionContent = new StringBuilder();
                log.info("\n📈 执行历史评估:");
                continue;
            } else if (line.contains("下一步策略:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_strategy";
                sectionContent = new StringBuilder();
                log.info("\n🚀 下一步策略:");
                continue;
            } else if (line.contains("完成度评估:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_progress";
                sectionContent = new StringBuilder();
                String progress = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 完成度评估: {}", progress);
                sectionContent.append(line).append("\n");
                continue;
            } else if (line.contains("任务状态:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_task_status";
                sectionContent = new StringBuilder();
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("COMPLETED")) {
                    log.info("\n✅ 任务状态: 已完成");
                } else {
                    log.info("\n🔄 任务状态: 继续执行");
                }
                sectionContent.append(line).append("\n");
                continue;
            }

            // 收集当前section的内容
            if (!currentSection.isEmpty()) {
                sectionContent.append(line).append("\n");
                switch (currentSection) {
                    case "analysis_status":
                        log.info("   📋 {}", line);
                        break;
                    case "analysis_history":
                        log.info("   📊 {}", line);
                        break;
                    case "analysis_strategy":
                        log.info("   🎯 {}", line);
                        break;
                    default:
                        log.info("   📝 {}", line);
                        break;
                }
            }
        }
        
        // 发送最后一个section的内容
        sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
        
        // 如果没有识别到任何section，发送完整的分析结果
        if (currentSection.isEmpty()) {
            log.warn("⚠️ 未识别到标准section格式，发送完整分析结果");
            sendAnalysisSubResult(dynamicContext, "analysis_status", analysisResult, sessionId);
        }
    }

    /**
     * 发送分析阶段细分结果到流式输出
     */
    private void sendAnalysisSubResult(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                      String subType, String content, String sessionId) {
        if (!subType.isEmpty() && !content.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    dynamicContext.getStep(), subType, content.trim(), sessionId);
            sendSseResult(dynamicContext, result);
        }
    }

    /**
     * 检查任务是否已完成
     */
    private boolean isTaskCompleted(String analysisResult) {
        // 检查任务状态为完成
        boolean statusCompleted = analysisResult.contains("任务状态: COMPLETED") ||
                                analysisResult.contains("任务状态: **COMPLETED**") ||
                                analysisResult.contains("状态: COMPLETED") ||
                                analysisResult.contains("STATUS: COMPLETED");
        
        // 检查完成度为100%（允许各种格式）
        boolean progressComplete = analysisResult.contains("完成度评估: 100%") ||
                                 analysisResult.contains("完成度评估: ** 100%") ||
                                 analysisResult.contains("完成度: 100%") ||
                                 analysisResult.contains("进度: 100%") ||
                                 analysisResult.contains("100%");
        
        // 检查明确的完成指示词（更加严格，避免描述性文字误判）
        boolean explicitCompletion = analysisResult.contains("整体任务已完成") ||
                                   analysisResult.contains("用户任务已完成") ||
                                   analysisResult.contains("主要任务完成") ||
                                   analysisResult.contains("所有目标完成") ||
                                   analysisResult.contains("TASK FINISHED") ||
                                   analysisResult.contains("无需进一步操作") ||
                                   analysisResult.contains("任务目标已完全实现") ||
                                   analysisResult.contains("停止后续步骤");
        
        // 如果完成度为100%，强制判定为完成（防止AI逻辑矛盾）
        if (progressComplete) {
            log.info("🎯 检测到完成度100%，强制判定任务完成");
            return true;
        }
        
        // 额外检查：如果完成度明确为0%，无论如何都不应该完成
        boolean zeroProgress = analysisResult.contains("完成度评估: ** 0%") ||
                             analysisResult.contains("完成度评估: 0%") ||
                             analysisResult.contains("完成度: 0%") ||
                             analysisResult.contains("进度: 0%");
        
        if (zeroProgress) {
            log.info("🚫 检测到完成度0%，强制判定任务未完成");
            return false;
        }
        
        boolean isCompleted = statusCompleted || explicitCompletion;
        
        if (isCompleted) {
            log.info("✅ 检测到任务完成信号: statusCompleted={}, progressComplete={}, explicitCompletion={}", 
                     statusCompleted, progressComplete, explicitCompletion);
        } else {
            log.info("🔄 任务尚未完成: statusCompleted={}, progressComplete={}, explicitCompletion={}", 
                     statusCompleted, progressComplete, explicitCompletion);
        }
        
        return isCompleted;
    }

}
