package site.kuril.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.kuril.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import site.kuril.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

import java.util.Map;

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

        // 发送步骤开始消息
        sendStepStart(dynamicContext, "执行总结分析", requestParameter.getSessionId());

        // 记录执行总结统计信息
        logExecutionSummary(dynamicContext, requestParameter.getSessionId());
        
        // 如果任务未完成，生成详细的最终总结报告
        if (dynamicContext.isCompleted()) {
            // 任务完成时，直接基于用户问题提供最终答案
            generateDirectAnswer(requestParameter, dynamicContext);
        } else {
            // 任务未完成时，说明情况并给出建议
            generateIncompleteReport(requestParameter, dynamicContext);
        }
        
        // 发送步骤完成消息
        sendStepComplete(dynamicContext, "总结报告生成完成", requestParameter.getSessionId());
        
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
    private void logExecutionSummary(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                   String sessionId) {
        log.info("\n📊 === AutoAgent动态多轮执行总结 ===");
        
        int actualSteps = Math.max(1, dynamicContext.getStep() - 1); // 实际执行的步数
        log.info("📈 总执行步数: {} 步", actualSteps);
        log.info("🎯 最大步数限制: {} 步", dynamicContext.getMaxStep());
        
        // 构建总结概览消息
        StringBuilder summaryOverview = new StringBuilder();
        summaryOverview.append("## 📊 执行总结概览\n\n");
        summaryOverview.append("- **总执行步数:** ").append(actualSteps).append(" 步\n");
        summaryOverview.append("- **最大步数限制:** ").append(dynamicContext.getMaxStep()).append(" 步\n");
        
        if (dynamicContext.isCompleted()) {
            log.info("✅ 任务完成状态: 已完成");
            summaryOverview.append("- **任务状态:** ✅ 已完成\n");
        } else {
            log.info("⏸️ 任务完成状态: 未完成（达到最大步数限制）");
            summaryOverview.append("- **任务状态:** ⏸️ 未完成（达到最大步数限制）\n");
        }
        
        // 计算执行效率
        double efficiency = dynamicContext.isCompleted() ? 100.0 : 
                           ((double) actualSteps / dynamicContext.getMaxStep()) * 100;
        log.info("📊 执行效率: {:.1f}%", efficiency);
        summaryOverview.append("- **执行效率:** ").append(String.format("%.1f%%", efficiency)).append("\n");
        
        // 显示客户端配置使用情况
        if (dynamicContext.getAiAgentClientFlowConfigVOMap() != null) {
            log.info("🤖 使用的客户端类型: {}", 
                    dynamicContext.getAiAgentClientFlowConfigVOMap().keySet());
            summaryOverview.append("- **使用的客户端:** ").append(dynamicContext.getAiAgentClientFlowConfigVOMap().keySet()).append("\n");
        }
        
        // 发送总结概览到前端
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "summary_overview", summaryOverview.toString(), sessionId));
    }
    
    /**
     * 记录成功完成的信息
     */
    private void logSuccessfulCompletion(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext,
                                       String sessionId) {
        log.info("\n🎉 === 任务成功完成 ===");
        log.info("✅ 所有步骤已成功执行完成");
        log.info("📋 执行历史长度: {} 字符", 
                dynamicContext.getExecutionHistory() != null ? 
                        dynamicContext.getExecutionHistory().length() : 0);
        
        // 构建成功完成消息 - 显示执行统计而不是虚假的执行步骤
        StringBuilder completionMessage = new StringBuilder();
        completionMessage.append("## ✅ 任务执行完成\n\n");
        completionMessage.append("🎯 **您的问题已经得到处理，相关的分析和建议已在前面的步骤中提供。**\n\n");
        completionMessage.append("📊 **执行统计:**\n");
        completionMessage.append("- 执行步数: ").append(Math.max(1, dynamicContext.getStep() - 1)).append("/").append(dynamicContext.getMaxStep()).append(" 步\n");
        completionMessage.append("- 任务完成时间: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        completionMessage.append("- 执行状态: 已成功完成\n\n");
        completionMessage.append("🌟 感谢您使用AI Auto Agent智能助手！");
        
        // 发送成功完成消息到前端
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "completed_work", completionMessage.toString(), sessionId));
        
        // 保存成功完成的总结
        dynamicContext.setValue("finalStatus", "SUCCESSFULLY_COMPLETED");
        dynamicContext.setValue("completionReason", "任务在规定步数内成功完成");
    }
    
    /**
     * 生成最终总结报告
     */
    private void generateFinalReport(ExecuteCommandEntity requestParameter, 
                                   DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        try {
            log.info("\n📋 === 生成未完成任务的总结报告 ===");
            
            // 发送生成报告开始消息
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "incomplete_reasons", 
                "🔄 正在生成详细的总结报告...", requestParameter.getSessionId()));
            
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
            
            // 发送详细总结报告到前端
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "evaluation", summaryResult, requestParameter.getSessionId()));
            
            // 将总结结果保存到动态上下文中
            dynamicContext.setValue("finalSummary", summaryResult);
            dynamicContext.setValue("finalStatus", "PARTIALLY_COMPLETED");
            dynamicContext.setValue("completionReason", "达到最大步数限制");
            
        } catch (Exception e) {
            log.error("❌ 生成最终总结报告时出现异常: {}", e.getMessage(), e);
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createErrorResult(4, 
                "生成总结报告时出现异常: " + e.getMessage(), requestParameter.getSessionId()));
            generateSimpleSummary(requestParameter, dynamicContext);
        }
    }

    /**
     * 构建总结提示词
     */
    private String buildSummaryPrompt(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                dynamicContext.getExecutionHistory().toString() : "";
        
        if (executionHistory.trim().isEmpty() || executionHistory.equals("[无执行历史]")) {
            // 如果没有真实的执行历史，直接说明情况
            return String.format("""
                    用户提问：%s
                    
                    执行情况：已执行 %d/%d 步，但由于达到最大步数限制，任务未能完全完成。
                    
                    请简要说明：
                    1. 当前任务的处理状态
                    2. 建议用户如何获得更好的结果（比如增加执行步数、细化问题等）
                    
                    请直接回答，不要编造虚假的执行步骤。
                    """, 
                    requestParameter.getMessage(), 
                    Math.max(1, dynamicContext.getStep() - 1),
                    dynamicContext.getMaxStep());
        } else {
            // 如果有真实的执行历史，才进行详细总结
            return String.format("""
                    请基于以下真实的执行过程进行总结：
                    
                    **用户原始问题:** %s
                    
                    **执行步数:** %d/%d 步
                    
                    **真实执行历史:** 
                    %s
                    
                    **请总结:**
                    1. **已完成的工作** - 基于上述执行历史
                    2. **未完成的原因** - 分析为什么没有达到预期目标
                    3. **改进建议** - 如何优化以获得更好结果
                    
                    请只基于真实的执行历史进行总结，不要添加任何虚构内容。
                    """, 
                    requestParameter.getMessage(), 
                    Math.max(1, dynamicContext.getStep() - 1),
                    dynamicContext.getMaxStep(),
                    executionHistory);
        }
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
    private void generateSimpleSummary(ExecuteCommandEntity requestParameter, 
                                     DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n📋 === 简化总结报告 ===");
        log.info("✅ 原始需求: {}", requestParameter.getMessage());
        log.info("📊 执行步数: {}/{} 步", Math.max(1, dynamicContext.getStep() - 1), dynamicContext.getMaxStep());
        log.info("📋 执行历史长度: {} 字符", 
                dynamicContext.getExecutionHistory() != null ? 
                        dynamicContext.getExecutionHistory().length() : 0);
        log.info("💡 建议: 增加最大步数限制或优化任务复杂度");
        
        // 构建简化总结消息 - 避免编造虚假信息
        StringBuilder simpleSummary = new StringBuilder();
        simpleSummary.append("## 📋 执行状态报告\n\n");
        simpleSummary.append("### 📝 用户问题\n");
        simpleSummary.append("**").append(requestParameter.getMessage()).append("**\n\n");
        
        simpleSummary.append("### ⚠️ 执行状态\n");
        simpleSummary.append("- 已执行步数: ").append(Math.max(1, dynamicContext.getStep() - 1))
                    .append("/").append(dynamicContext.getMaxStep()).append(" 步\n");
        simpleSummary.append("- 任务状态: 因达到最大步数限制而未完全完成\n");
        
        String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                dynamicContext.getExecutionHistory().toString() : "";
        
        if (!executionHistory.trim().isEmpty()) {
            simpleSummary.append("- 有执行记录: ").append(executionHistory.length()).append(" 字符\n\n");
        } else {
            simpleSummary.append("- 执行记录: 暂无详细记录\n\n");
        }
        
        simpleSummary.append("### 💡 建议\n");
        simpleSummary.append("- **增加步数**: 将最大执行步数设置为10步或更多\n");
        simpleSummary.append("- **细化问题**: 将复杂问题拆分为更具体的子问题\n");
        simpleSummary.append("- **重新尝试**: 使用更高的步数限制重新提问\n\n");
        
        simpleSummary.append("### 📊 总结\n");
        simpleSummary.append("由于步数限制，未能完全处理您的问题。建议增加执行步数后重试。");
        
        // 发送简化总结到前端
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "evaluation", simpleSummary.toString(), requestParameter.getSessionId()));
        
        // 保存简化总结
        dynamicContext.setValue("finalSummary", "任务部分完成，建议增加执行步数或简化任务");
        dynamicContext.setValue("finalStatus", "PARTIALLY_COMPLETED");
        dynamicContext.setValue("completionReason", "达到最大步数限制，使用简化总结");
    }
    
    /**
     * 生成针对用户问题的直接答案 - 任务完成时
     */
    private void generateDirectAnswer(ExecuteCommandEntity requestParameter,
                                    DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n🎯 === 生成最终答案 ===");
        
        try {
            // 获取执行历史中的关键内容
            String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                    dynamicContext.getExecutionHistory().toString() : "";
            
            // 构建针对用户问题的最终答案提示词
            String finalAnswerPrompt = String.format("""
                    用户问题：%s
                    
                    根据以下执行过程，直接回答用户的问题，提供具体的建议和答案：
                    
                    执行过程：
                    %s
                    
                    请注意：
                    1. 直接回答用户的问题，不要总结执行过程
                    2. 提供具体、实用的建议
                    3. 确保答案与用户问题高度相关
                    4. 如果执行过程中没有找到充分信息，请诚实说明
                    """, 
                    requestParameter.getMessage(),
                    !executionHistory.trim().isEmpty() ? executionHistory : "暂无详细执行记录");
            
            // 使用AI生成最终答案
            AiAgentClientFlowConfigVO summaryConfig = dynamicContext.getAiAgentClientFlowConfigVOMap().get("Summary");
            if (summaryConfig == null) {
                log.warn("⚠️ 未找到Summary配置，尝试使用其他可用配置");
                // 尝试使用其他配置
                summaryConfig = findAnyAvailableConfig(dynamicContext);
                if (summaryConfig == null) {
                    log.warn("⚠️ 未找到任何可用配置，使用智能答案生成");
                    sendSmartDirectAnswer(requestParameter, dynamicContext);
                    return;
                }
            }
            
            ChatClient chatClient = getChatClientByClientId(summaryConfig.getClientId());
            String finalAnswer = chatClient
                    .prompt(finalAnswerPrompt)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(3000)
                            .temperature(0.7)
                            .build())
                    .call().content();
            
            log.info("📝 最终答案已生成");
            
            // 发送最终答案到前端
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "final_answer", 
                    finalAnswer, requestParameter.getSessionId()));
            
        } catch (Exception e) {
            log.error("生成最终答案时出现异常", e);
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createErrorResult(4,
                "生成最终答案时出现异常: " + e.getMessage(), requestParameter.getSessionId()));
            // 使用智能答案而不是简化答案
            sendSmartDirectAnswer(requestParameter, dynamicContext);
        }
    }
    
    /**
     * 生成智能的直接答案 - 使用AI总结
     */
    private void sendSmartDirectAnswer(ExecuteCommandEntity requestParameter,
                                     DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("🤖 使用AI智能总结生成最终答案");
        
        try {
            String userQuestion = requestParameter.getMessage();
            String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                    dynamicContext.getExecutionHistory().toString() : "";
            
            // 找到任意可用的AI客户端
            AiAgentClientFlowConfigVO availableConfig = findAnyAvailableConfig(dynamicContext);
            if (availableConfig == null) {
                log.warn("⚠️ 未找到任何可用的AI配置，使用备用方案");
                sendFallbackAnswer(requestParameter, dynamicContext);
                return;
            }
            
            // 构建智能总结提示词
            String smartSummaryPrompt = String.format("""
                    用户的原始问题：%s
                    
                    以下是AI助手的完整执行过程和思考历史：
                    %s
                    
                    请根据上述执行历史，直接回答用户的原始问题。注意：
                    
                    1. 请提供具体、准确的答案，不要重复执行过程
                    2. 如果问题是数学计算，请直接给出计算结果
                    3. 如果问题是咨询类，请基于执行过程给出建议
                    4. 如果执行过程中没有找到明确答案，请诚实说明
                    5. 答案要简洁明了，针对用户问题直接回应
                    
                    请直接开始回答，不要说"根据执行历史"这样的开场白。
                    """, 
                    userQuestion,
                    !executionHistory.trim().isEmpty() ? executionHistory : "执行过程中暂无详细记录");
            
            ChatClient chatClient = getChatClientByClientId(availableConfig.getClientId());
            String smartAnswer = chatClient
                    .prompt(smartSummaryPrompt)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(2000)
                            .temperature(0.3)  // 较低温度，确保答案准确
                            .build())
                    .call().content();
            
            log.info("✅ AI智能总结答案生成完成");
            
            // 发送AI生成的智能答案
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "final_answer", 
                    smartAnswer, requestParameter.getSessionId()));
                    
        } catch (Exception e) {
            log.error("AI智能总结时出现异常", e);
            sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createErrorResult(4,
                "AI智能总结异常: " + e.getMessage(), requestParameter.getSessionId()));
            sendFallbackAnswer(requestParameter, dynamicContext);
        }
    }
    
    /**
     * 备用答案方案 - 当AI不可用时
     */
    private void sendFallbackAnswer(ExecuteCommandEntity requestParameter,
                                  DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        String userQuestion = requestParameter.getMessage();
        String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                dynamicContext.getExecutionHistory().toString() : "";
        
        StringBuilder fallbackAnswer = new StringBuilder();
        fallbackAnswer.append("## 📝 关于您的问题：").append(userQuestion).append("\n\n");
        
        if (!executionHistory.trim().isEmpty()) {
            // 简单提取有用信息
            String[] lines = executionHistory.split("\n");
            boolean foundUsefulInfo = false;
            
            for (String line : lines) {
                line = line.trim();
                // 寻找包含答案的行
                if (line.contains("结果") || line.contains("答案") || line.contains("=") || 
                    line.contains("建议") || line.contains("方案")) {
                    if (!foundUsefulInfo) {
                        fallbackAnswer.append("**处理结果：**\n");
                        foundUsefulInfo = true;
                    }
                    fallbackAnswer.append("- ").append(line).append("\n");
                }
            }
            
            if (!foundUsefulInfo) {
                fallbackAnswer.append("AI助手已分析了您的问题，详细信息请参考上述执行过程。");
            }
        } else {
            fallbackAnswer.append("由于系统限制，未能获取详细的执行记录。请尝试重新提问。");
        }
        
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "final_answer", 
                fallbackAnswer.toString(), requestParameter.getSessionId()));
    }
    
    /**
     * 查找任意可用的AI客户端配置
     */
    private AiAgentClientFlowConfigVO findAnyAvailableConfig(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        Map<String, AiAgentClientFlowConfigVO> configMap = dynamicContext.getAiAgentClientFlowConfigVOMap();
        if (configMap == null || configMap.isEmpty()) {
            return null;
        }
        
        // 优先使用Summary配置
        AiAgentClientFlowConfigVO summaryConfig = configMap.get("Summary");
        if (summaryConfig != null) {
            return summaryConfig;
        }
        
        // 其次使用RESPONSE_ASSISTANT
        for (AiAgentClientFlowConfigVO config : configMap.values()) {
            if ("RESPONSE_ASSISTANT".equals(config.getClientType())) {
                return config;
            }
        }
        
        // 最后使用任意可用配置
        return configMap.values().iterator().next();
    }
    
    /**
     * 生成未完成报告 - 任务未完成时
     */
    private void generateIncompleteReport(ExecuteCommandEntity requestParameter,
                                        DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n⚠️ === 生成未完成报告 ===");
        
        StringBuilder incompleteReport = new StringBuilder();
        incompleteReport.append("## ⚠️ 任务处理状态\n\n");
        incompleteReport.append("### 📝 您的问题\n");
        incompleteReport.append("**").append(requestParameter.getMessage()).append("**\n\n");
        
        incompleteReport.append("### 📊 处理状态\n");
        incompleteReport.append("- **执行步数**: ").append(Math.max(1, dynamicContext.getStep() - 1))
                .append("/").append(dynamicContext.getMaxStep()).append(" 步\n");
        incompleteReport.append("- **任务状态**: 因达到最大步数限制而未完全完成\n\n");
        
        String executionHistory = dynamicContext.getExecutionHistory() != null ? 
                dynamicContext.getExecutionHistory().toString() : "";
        
        if (!executionHistory.trim().isEmpty()) {
            incompleteReport.append("### 🔄 已完成工作\n");
            incompleteReport.append("AI助手已经开始处理您的问题，但由于步数限制未能提供完整答案。\n\n");
        } else {
            incompleteReport.append("### ❌ 处理情况\n");
            incompleteReport.append("由于步数限制，未能深入处理您的问题。\n\n");
        }
        
        incompleteReport.append("### 💡 建议\n");
        incompleteReport.append("为了获得更好的回答，建议您：\n");
        incompleteReport.append("- **增加步数**: 将最大执行步数设置为10步或更多\n");
        incompleteReport.append("- **细化问题**: 将复杂问题拆分为更具体的子问题\n");
        incompleteReport.append("- **重新尝试**: 使用更高的步数限制重新提问\n\n");
        
        incompleteReport.append("### 📞 提示\n");
        incompleteReport.append("如果您需要关于\"").append(requestParameter.getMessage())
                .append("\"的具体建议，请尝试增加执行步数后重新提问。");
        
        // 发送未完成报告到前端
        sendSseResult(dynamicContext, AutoAgentExecuteResultEntity.createSummarySubResult(4, "incomplete_report", 
                incompleteReport.toString(), requestParameter.getSessionId()));
    }

}
