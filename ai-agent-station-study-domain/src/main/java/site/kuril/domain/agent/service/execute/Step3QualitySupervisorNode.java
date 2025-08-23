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
 * Step3: 质量监督节点
 * 负责监督和评估执行质量，识别问题并提供改进建议
 */
@Slf4j
@Service("step3QualitySupervisorNode")
public class Step3QualitySupervisorNode extends AbstractExecuteSupport {

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n🔍 === 执行第 {} 步：质量监督检查 ===", dynamicContext.getStep());
        
        // 从动态上下文中获取执行结果
        String executionResult = dynamicContext.getValue("executionResult");
        if (executionResult == null || executionResult.trim().isEmpty()) {
            log.warn("⚠️ 执行结果为空，跳过质量监督");
            return "SUPERVISION_SKIPPED";
        }
        
        // 构建监督提示词
        String supervisionPrompt = buildSupervisionPrompt(requestParameter, executionResult);

        // 获取质量监督客户端
        AiAgentClientFlowConfigVO supervisorConfig = dynamicContext.getAiAgentClientFlowConfigVOMap()
                .get(AiClientTypeEnumVO.QUALITY_SUPERVISOR_CLIENT.getCode());
        
        if (supervisorConfig == null) {
            log.error("❌ 未找到质量监督客户端配置");
            throw new RuntimeException("未找到质量监督客户端配置");
        }

        ChatClient chatClient = getChatClientByClientId(supervisorConfig.getClientId());

        // 执行质量监督
        log.info("🔍 开始质量监督检查...");
        String supervisionResult = chatClient
                .prompt(supervisionPrompt)
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .maxTokens(3000)
                        .temperature(0.2)
                        .build())
                .advisors(a -> a
                        .param("CHAT_MEMORY_CONVERSATION_ID", requestParameter.getSessionId())
                        .param("CHAT_MEMORY_RETRIEVE_SIZE", 1024))
                .call().content();

        // 解析监督结果
        parseSupervisionResult(dynamicContext.getStep(), supervisionResult);
        
        // 将监督结果保存到动态上下文中
        dynamicContext.setValue("supervisionResult", supervisionResult);
        
        // 根据监督结果决定下一步行动
        processSupervisionDecision(dynamicContext, supervisionResult);
        
        // 更新完整的执行历史
        updateCompleteExecutionHistory(dynamicContext);
        
        // 增加步骤计数
        dynamicContext.setStep(dynamicContext.getStep() + 1);

        return "SUPERVISION_COMPLETED";
    }

    @Override
    public DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getExecuteHandler("step4LogExecutionSummaryNode");
        }
        
        // 否则返回到Step1AnalyzerNode进行下一轮分析
        log.info("🔄 准备进入下一轮分析执行循环");
        return getExecuteHandler("step1AnalyzerNode");
    }

    /**
     * 构建监督提示词
     */
    private String buildSupervisionPrompt(ExecuteCommandEntity requestParameter, String executionResult) {
        return String.format("""
                **用户原始需求:** %s
                
                **执行结果:** %s
                
                **监督要求:** 请评估执行结果的质量，识别问题，并提供改进建议。
                
                **请严格按照以下格式输出:**
                
                **质量评估:**
                [对执行结果的整体质量评估]
                
                **问题识别:**
                [发现的问题和不足之处]
                
                **改进建议:**
                [具体的改进建议和优化方案]
                
                **质量评分:** [0-100]分
                
                **是否通过:** [PASS/FAIL/OPTIMIZE]
                """, requestParameter.getMessage(), executionResult);
    }
    
    /**
     * 解析监督结果
     */
    private void parseSupervisionResult(int step, String supervisionResult) {
        log.info("\n🔍 === 第 {} 步监督结果 ===", step);
        
        String[] lines = supervisionResult.split("\n");
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 识别不同的监督部分
            if (line.contains("质量评估:")) {
                currentSection = "assessment";
                log.info("\n📊 质量评估:");
                continue;
            } else if (line.contains("问题识别:")) {
                currentSection = "issues";
                log.info("\n⚠️ 问题识别:");
                continue;
            } else if (line.contains("改进建议:")) {
                currentSection = "suggestions";
                log.info("\n💡 改进建议:");
                continue;
            } else if (line.contains("质量评分:")) {
                String score = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 质量评分: {}", score);
                continue;
            } else if (line.contains("是否通过:")) {
                String status = line.substring(line.indexOf(":") + 1).trim();
                switch (status) {
                    case "PASS":
                        log.info("\n✅ 检查结果: 通过");
                        break;
                    case "FAIL":
                        log.info("\n❌ 检查结果: 未通过");
                        break;
                    case "OPTIMIZE":
                        log.info("\n🔧 检查结果: 需要优化");
                        break;
                    default:
                        log.info("\n❓ 检查结果: {}", status);
                        break;
                }
                continue;
            }
            
            // 输出具体内容
            switch (currentSection) {
                case "assessment":
                    log.info("   📋 {}", line);
                    break;
                case "issues":
                    log.info("   ⚠️ {}", line);
                    break;
                case "suggestions":
                    log.info("   💡 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }

    /**
     * 处理监督决策
     */
    private void processSupervisionDecision(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                          String supervisionResult) {
        if (supervisionResult.contains("是否通过: FAIL")) {
            log.info("❌ 质量检查未通过，需要重新执行");
            dynamicContext.setCurrentTask("根据质量监督的建议重新执行任务");
        } else if (supervisionResult.contains("是否通过: OPTIMIZE")) {
            log.info("🔧 质量检查建议优化，继续改进");
            dynamicContext.setCurrentTask("根据质量监督的建议优化执行结果");
        } else if (supervisionResult.contains("是否通过: PASS")) {
            log.info("✅ 质量检查通过，任务完成");
            dynamicContext.setCompleted(true);
        } else {
            // 默认情况：继续执行
            log.info("🔄 继续执行下一轮任务");
        }
    }

    /**
     * 更新完整的执行历史
     */
    private void updateCompleteExecutionHistory(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        String stepSummary = String.format("""
                
                === 第 %d 步完整记录 ===
                【分析阶段】%s
                【执行阶段】%s
                【监督阶段】%s
                """, 
                dynamicContext.getStep(), 
                extractSummary(dynamicContext.getValue("analysisResult")), 
                extractSummary(dynamicContext.getValue("executionResult")),
                extractSummary(dynamicContext.getValue("supervisionResult")));
        
        // 更新执行历史（替换当前步骤的记录）
        StringBuilder history = dynamicContext.getExecutionHistory();
        String currentHistory = history.toString();
        
        // 移除当前步骤的不完整记录，添加完整记录
        String incompletePattern = "=== 第 " + dynamicContext.getStep() + " 步执行记录 ===";
        int lastIncompleteIndex = currentHistory.lastIndexOf(incompletePattern);
        if (lastIncompleteIndex >= 0) {
            history.setLength(lastIncompleteIndex);
        }
        
        history.append(stepSummary);
        log.info("📋 完整执行历史已更新");
    }

    /**
     * 提取结果摘要
     */
    private String extractSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "无内容";
        }
        
        // 简化处理：取前150个字符作为摘要
        String summary = content.trim();
        if (summary.length() > 150) {
            summary = summary.substring(0, 150) + "...";
        }
        
        return summary;
    }

}
