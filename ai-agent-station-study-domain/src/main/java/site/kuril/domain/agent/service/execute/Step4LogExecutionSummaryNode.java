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
 * Step4: 执行总结节点
 * 负责生成最终的执行总结报告和分析
 */
@Slf4j
@Service("step4LogExecutionSummaryNode")
public class Step4LogExecutionSummaryNode extends AbstractExecuteSupport {

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n📊 === 执行第 {} 步：执行总结分析 ===", dynamicContext.getStep());

        // 记录执行总结统计信息
        logExecutionSummary(dynamicContext);
        
        // 如果任务未完成，生成详细的最终总结报告
        if (!dynamicContext.isCompleted()) {
            generateFinalReport(requestParameter, dynamicContext);
        } else {
            logSuccessfulCompletion(dynamicContext);
        }
        
        log.info("\n🏁 === AutoAgent动态多轮执行测试结束 ===");
        
        return "AUTO_AGENT_EXECUTION_SUMMARY_COMPLETED";
    }

    @Override
    public DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 总结节点是最后一个节点，返回默认处理器表示执行结束
        return defaultStrategyHandler;
    }
    
    /**
     * 记录执行总结统计信息
     */
    private void logExecutionSummary(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n📊 === AutoAgent动态多轮执行总结 ===");
        
        int actualSteps = Math.max(1, dynamicContext.getStep() - 1); // 实际执行的步数
        log.info("📈 总执行步数: {} 步", actualSteps);
        log.info("🎯 最大步数限制: {} 步", dynamicContext.getMaxStep());
        
        if (dynamicContext.isCompleted()) {
            log.info("✅ 任务完成状态: 已完成");
        } else {
            log.info("⏸️ 任务完成状态: 未完成（达到最大步数限制）");
        }
        
        // 计算执行效率
        double efficiency = dynamicContext.isCompleted() ? 100.0 : 
                           ((double) actualSteps / dynamicContext.getMaxStep()) * 100;
        log.info("📊 执行效率: {:.1f}%", efficiency);
        
        // 显示客户端配置使用情况
        if (dynamicContext.getAiAgentClientFlowConfigVOMap() != null) {
            log.info("🤖 使用的客户端类型: {}", 
                    dynamicContext.getAiAgentClientFlowConfigVOMap().keySet());
        }
    }
    
    /**
     * 记录成功完成的信息
     */
    private void logSuccessfulCompletion(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n🎉 === 任务成功完成 ===");
        log.info("✅ 所有步骤已成功执行完成");
        log.info("📋 执行历史长度: {} 字符", 
                dynamicContext.getExecutionHistory() != null ? 
                        dynamicContext.getExecutionHistory().length() : 0);
        
        // 保存成功完成的总结
        dynamicContext.setValue("finalStatus", "SUCCESSFULLY_COMPLETED");
        dynamicContext.setValue("completionReason", "任务在规定步数内成功完成");
    }
    
    /**
     * 生成最终总结报告
     */
    private void generateFinalReport(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        try {
            log.info("\n📋 === 生成未完成任务的总结报告 ===");
            
            String summaryPrompt = buildSummaryPrompt(requestParameter, dynamicContext);
            
            // 获取任务分析客户端进行总结（复用任务分析客户端）
            AiAgentClientFlowConfigVO analyzerConfig = dynamicContext.getAiAgentClientFlowConfigVOMap()
                    .get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());
            
            if (analyzerConfig == null) {
                log.warn("⚠️ 未找到任务分析客户端配置，跳过详细总结报告生成");
                generateSimpleSummary(requestParameter, dynamicContext);
                return;
            }
            
            ChatClient chatClient = getChatClientByClientId(analyzerConfig.getClientId());
            
            log.info("🤔 开始生成最终总结报告...");
            String summaryResult = chatClient
                    .prompt(summaryPrompt)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(2000)
                            .temperature(0.3)
                            .build())
                    .advisors(a -> a
                            .param("CHAT_MEMORY_CONVERSATION_ID", requestParameter.getSessionId() + "-summary")
                            .param("CHAT_MEMORY_RETRIEVE_SIZE", 100))
                    .call().content();
            
            logFinalReport(summaryResult);
            
            // 将总结结果保存到动态上下文中
            dynamicContext.setValue("finalSummary", summaryResult);
            dynamicContext.setValue("finalStatus", "PARTIALLY_COMPLETED");
            dynamicContext.setValue("completionReason", "达到最大步数限制");
            
        } catch (Exception e) {
            log.error("❌ 生成最终总结报告时出现异常: {}", e.getMessage(), e);
            generateSimpleSummary(requestParameter, dynamicContext);
        }
    }

    /**
     * 构建总结提示词
     */
    private String buildSummaryPrompt(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return String.format("""
                请对以下未完成的任务执行过程进行总结分析：
                
                **原始用户需求:** %s
                
                **执行步数:** %d/%d 步
                
                **执行历史:** 
                %s
                
                **请按以下格式输出总结:**
                
                **已完成工作:**
                [总结已完成的工作内容和成果]
                
                **未完成原因:**
                [分析任务未完成的主要原因]
                
                **完成建议:**
                [提出完成剩余任务的具体建议]
                
                **整体评估:**
                [对整体执行效果的评估]
                """, 
                requestParameter.getMessage(), 
                Math.max(1, dynamicContext.getStep() - 1),
                dynamicContext.getMaxStep(),
                dynamicContext.getExecutionHistory() != null ? 
                        dynamicContext.getExecutionHistory().toString() : "[无执行历史]");
    }
    
    /**
     * 输出最终总结报告
     */
    private void logFinalReport(String summaryResult) {
        log.info("\n📋 === 最终总结报告 ===");
        
        String[] lines = summaryResult.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 根据内容类型添加不同图标
            if (line.contains("已完成") || line.contains("完成的工作")) {
                log.info("✅ {}", line);
            } else if (line.contains("未完成") || line.contains("原因")) {
                log.info("❌ {}", line);
            } else if (line.contains("建议") || line.contains("推荐")) {
                log.info("💡 {}", line);
            } else if (line.contains("评估") || line.contains("效果")) {
                log.info("📊 {}", line);
            } else {
                log.info("📝 {}", line);
            }
        }
    }

    /**
     * 生成简单总结（当无法使用AI时的备选方案）
     */
    private void generateSimpleSummary(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n📋 === 简化总结报告 ===");
        log.info("✅ 原始需求: {}", requestParameter.getMessage());
        log.info("📊 执行步数: {}/{} 步", Math.max(1, dynamicContext.getStep() - 1), dynamicContext.getMaxStep());
        log.info("📋 执行历史长度: {} 字符", 
                dynamicContext.getExecutionHistory() != null ? 
                        dynamicContext.getExecutionHistory().length() : 0);
        log.info("💡 建议: 增加最大步数限制或优化任务复杂度");
        
        // 保存简化总结
        dynamicContext.setValue("finalSummary", "任务部分完成，建议增加执行步数或简化任务");
        dynamicContext.setValue("finalStatus", "PARTIALLY_COMPLETED");
        dynamicContext.setValue("completionReason", "达到最大步数限制，使用简化总结");
    }

}
