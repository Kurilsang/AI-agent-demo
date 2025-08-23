package site.kuril.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
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

        // 解析分析结果
        parseAnalysisResult(dynamicContext.getStep(), analysisResult);
        
        // 将分析结果保存到动态上下文中
        dynamicContext.setValue("analysisResult", analysisResult);

        // 检查任务完成状态
        if (isTaskCompleted(analysisResult)) {
            log.info("✅ 任务分析显示已完成！");
            dynamicContext.setCompleted(true);
        } else {
            log.info("🔄 任务需要继续执行");
        }

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
                
                **请严格按照以下格式输出:**
                
                **任务状态分析:**
                [当前任务完成情况的详细分析]
                
                **执行历史评估:**
                [对已完成工作的质量和效果评估]
                
                **下一步策略:**
                [具体的下一步执行计划和策略]
                
                **完成度评估:** [0-100]%%
                
                **任务状态:** [CONTINUE/COMPLETED]
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
     * 解析任务分析结果
     */
    private void parseAnalysisResult(int step, String analysisResult) {
        log.info("\n📊 === 第 {} 步分析结果 ===", step);

        String[] lines = analysisResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 识别不同的分析部分
            if (line.contains("任务状态分析:")) {
                currentSection = "status";
                log.info("\n🎯 任务状态分析:");
                continue;
            } else if (line.contains("执行历史评估:")) {
                currentSection = "history";
                log.info("\n📈 执行历史评估:");
                continue;
            } else if (line.contains("下一步策略:")) {
                currentSection = "strategy";
                log.info("\n🚀 下一步策略:");
                continue;
            } else if (line.contains("完成度评估:")) {
                String progress = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 完成度评估: {}", progress);
                continue;
            } else if (line.contains("任务状态:")) {
                String status = line.substring(line.indexOf(":") + 1).trim();
                if ("COMPLETED".equals(status)) {
                    log.info("\n✅ 任务状态: 已完成");
                } else {
                    log.info("\n🔄 任务状态: 继续执行");
                }
                continue;
            }

            // 输出具体内容
            switch (currentSection) {
                case "status":
                    log.info("   📋 {}", line);
                    break;
                case "history":
                    log.info("   📊 {}", line);
                    break;
                case "strategy":
                    log.info("   🎯 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }

    /**
     * 检查任务是否已完成
     */
    private boolean isTaskCompleted(String analysisResult) {
        return analysisResult.contains("任务状态: COMPLETED") ||
               analysisResult.contains("完成度评估: 100%") ||
               analysisResult.contains("任务状态: **COMPLETED**");
    }

}
