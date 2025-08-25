package site.kuril.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import io.modelcontextprotocol.client.McpSyncClient;
import site.kuril.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import site.kuril.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

// 移除静态导入，使用字符串常量

/**
 * Step2: 精准执行节点
 * 负责根据分析师的策略，执行具体的任务步骤
 */
@Slf4j
@Service("step2PrecisionExecutorNode")
public class Step2PrecisionExecutorNode extends AbstractExecuteSupport {

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n⚡ === 执行第 {} 步：精准任务执行 ===", dynamicContext.getStep());
        
        // 从动态上下文中获取分析结果
        String analysisResult = dynamicContext.getValue("analysisResult");
        if (analysisResult == null || analysisResult.trim().isEmpty()) {
            log.warn("⚠️ 分析结果为空，使用默认执行策略");
            analysisResult = "执行当前任务步骤";
        }
        
        // 构建执行提示词
        String executionPrompt = buildExecutionPrompt(analysisResult);

        // 获取精准执行客户端
        AiAgentClientFlowConfigVO executorConfig = dynamicContext.getAiAgentClientFlowConfigVOMap()
                .get(AiClientTypeEnumVO.PRECISION_EXECUTOR_CLIENT.getCode());
        
        if (executorConfig == null) {
            log.error("❌ 未找到精准执行客户端配置");
            throw new RuntimeException("未找到精准执行客户端配置");
        }

        ChatClient chatClient = getChatClientByClientId(executorConfig.getClientId());

        // 获取MCP客户端用于工具调用
        McpSyncClient[] mcpClients = getMcpClientsForClient(executorConfig.getClientId());
        
        // 执行具体任务
        log.info("🔧 开始精准任务执行...");
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model("gpt-4o")
                .maxTokens(4000)
                .temperature(0.5);
        
        // 如果有MCP客户端，配置工具回调
        if (mcpClients.length > 0) {
            log.info("🛠️ 配置{}个MCP工具回调", mcpClients.length);
            optionsBuilder.toolCallbacks(new SyncMcpToolCallbackProvider(mcpClients).getToolCallbacks());
        }
        
        String executionResult = chatClient
                .prompt(executionPrompt)
                .options(optionsBuilder.build())
                .advisors(a -> a
                        .param("CHAT_MEMORY_CONVERSATION_ID", requestParameter.getSessionId())
                        .param("CHAT_MEMORY_RETRIEVE_SIZE", 1024))
                .call().content();

        // 解析执行结果并发送SSE
        parseExecutionResult(dynamicContext, executionResult, requestParameter.getSessionId());
        
        // 将执行结果保存到动态上下文中
        dynamicContext.setValue("executionResult", executionResult);
        
        // 更新执行历史
        updateExecutionHistory(dynamicContext, analysisResult, executionResult);

        return "EXECUTION_COMPLETED";
    }

    @Override
    public DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 路由到质量监督节点
        return getExecuteHandler("step3QualitySupervisorNode");
    }

    /**
     * 构建执行提示词
     */
    private String buildExecutionPrompt(String analysisResult) {
        return String.format("""
                **分析师策略:** %s
                
                **执行指令:** 根据上述分析师的策略，执行具体的任务步骤。
                
                **🔧 可用工具说明:**
                - **FileSystem工具**: 用于创建、读取、写入文件（如创建.txt、.md、.java等文件）
                - **CSDN文章发布工具**: 用于将文章发布到CSDN平台并返回真实的文章URL
                - **其他工具**: 根据任务需要调用相应的工具
                
                **⚠️ 重要执行要求:**
                1. **必须实际使用工具**: 不能只描述过程，必须真正调用相应的工具
                2. **必须返回真实结果**: 如果涉及文件创建或文章发布，必须返回实际的文件路径或URL
                3. **工具调用优先**: 如果任务涉及文件操作或发布操作，优先使用相应的MCP工具
                4. **严格按照策略执行**: 完全按照分析师的策略执行，不要跳过任何步骤
                
                **📋 特别注意:**
                - 如果任务是"写文章并发布到CSDN"，必须：
                  1. 使用FileSystem工具创建实际的文章文件（.md或.txt格式）
                  2. 使用CSDN发布工具将文章发布并获取真实的URL
                  3. 在执行结果中提供真实的文件路径和CSDN文章链接
                - 禁止使用占位符如"[待填写链接]"或"[链接示例]"
                - 必须提供可验证的实际结果
                
                **请严格按照以下格式输出:**
                
                **执行目标:**
                [明确的执行目标]
                
                **执行过程:**
                [详细的执行步骤，包括实际调用的工具和参数]
                
                **执行结果:**
                [具体的执行成果，包括真实的文件路径、URL等]
                
                **质量检查:**
                [对执行结果的自我质量评估，确认工具调用成功]
                """, analysisResult);
    }
    
    /**
     * 解析执行结果并发送SSE
     */
    private void parseExecutionResult(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                    String executionResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n⚡ === 第 {} 步执行结果 ===", step);
        log.info("{}", executionResult);
        
        // 先发送完整的执行结果
        sendExecutionSubResult(dynamicContext, "execution_process", executionResult, sessionId);
        
        // 解析不同部分并分别发送
        String[] lines = executionResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 识别不同的执行部分
            if (line.contains("执行目标:")) {
                // 发送上一个section
                sendExecutionSubResult(dynamicContext, getExecutionSubType(currentSection), sectionContent.toString(), sessionId);
                currentSection = "target";
                sectionContent = new StringBuilder();
                log.info("\n🎯 执行目标:");
                continue;
            } else if (line.contains("执行过程:")) {
                sendExecutionSubResult(dynamicContext, getExecutionSubType(currentSection), sectionContent.toString(), sessionId);
                currentSection = "process";
                sectionContent = new StringBuilder();
                log.info("\n🔧 执行过程:");
                continue;
            } else if (line.contains("执行结果:")) {
                sendExecutionSubResult(dynamicContext, getExecutionSubType(currentSection), sectionContent.toString(), sessionId);
                currentSection = "result";
                sectionContent = new StringBuilder();
                log.info("\n📈 执行结果:");
                continue;
            } else if (line.contains("质量检查:")) {
                sendExecutionSubResult(dynamicContext, getExecutionSubType(currentSection), sectionContent.toString(), sessionId);
                currentSection = "quality";
                sectionContent = new StringBuilder();
                log.info("\n🔍 质量检查:");
                continue;
            }
            
            // 收集内容
            sectionContent.append(line).append("\n");
            
            // 输出具体内容
            switch (currentSection) {
                case "target":
                    log.info("   🎯 {}", line);
                    break;
                case "process":
                    log.info("   ⚙️ {}", line);
                    break;
                case "result":
                    log.info("   📊 {}", line);
                    break;
                case "quality":
                    log.info("   ✅ {}", line);
                    break;
                default:
                    if (!currentSection.isEmpty()) {
                        log.info("   📝 {}", line);
                    }
                    break;
            }
        }
        
        // 发送最后一个section
        sendExecutionSubResult(dynamicContext, getExecutionSubType(currentSection), sectionContent.toString(), sessionId);
    }
    
    /**
     * 获取执行阶段子类型
     */
    private String getExecutionSubType(String section) {
        switch (section) {
            case "target": return "execution_target";
            case "process": return "execution_process";
            case "result": return "execution_result";
            case "quality": return "execution_quality";
            default: return "execution_process";
        }
    }
    
    /**
     * 发送执行阶段细分结果到流式输出
     */
    private void sendExecutionSubResult(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                      String subType, String content, String sessionId) {
        if (!content.trim().isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createExecutionSubResult(
                    dynamicContext.getStep(), subType, content.trim(), sessionId);
            sendSseResult(dynamicContext, result);
        }
    }

    /**
     * 更新执行历史
     */
    private void updateExecutionHistory(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                      String analysisResult, String executionResult) {
        String stepSummary = String.format("""
                
                === 第 %d 步执行记录 ===
                【分析阶段】%s
                【执行阶段】%s
                """, dynamicContext.getStep(), 
                extractSummary(analysisResult), 
                extractSummary(executionResult));
        
        dynamicContext.getExecutionHistory().append(stepSummary);
        log.info("📋 执行历史已更新");
    }

    /**
     * 提取结果摘要
     */
    private String extractSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "无内容";
        }
        
        // 简化处理：取前200个字符作为摘要
        String summary = content.trim();
        if (summary.length() > 200) {
            summary = summary.substring(0, 200) + "...";
        }
        
        return summary;
    }
    
    /**
     * 获取指定客户端的MCP客户端
     */
    private McpSyncClient[] getMcpClientsForClient(String clientId) {
        try {
            java.util.List<McpSyncClient> mcpClientList = new java.util.ArrayList<>();
            
            // 对于客户端3102，它有CSDN和FileSystem两个工具
            if ("3102".equals(clientId)) {
                // 尝试获取CSDN MCP客户端 (bean名称: ai_client_tool_mcp_5001)
                try {
                    Object csdnBean = getBean("ai_client_tool_mcp_5001");
                    if (csdnBean instanceof McpSyncClient) {
                        McpSyncClient csdnClient = (McpSyncClient) csdnBean;
                        mcpClientList.add(csdnClient);
                        log.info("✅ 成功获取CSDN MCP客户端");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 无法获取CSDN MCP客户端: {}", e.getMessage());
                }
                
                // 尝试获取FileSystem MCP客户端 (bean名称: ai_client_tool_mcp_5003) 
                try {
                    Object fileSystemBean = getBean("ai_client_tool_mcp_5003");
                    if (fileSystemBean instanceof McpSyncClient) {
                        McpSyncClient fileSystemClient = (McpSyncClient) fileSystemBean;
                        mcpClientList.add(fileSystemClient);
                        log.info("✅ 成功获取FileSystem MCP客户端");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 无法获取FileSystem MCP客户端: {}", e.getMessage());
                }
            }
            
            return mcpClientList.toArray(new McpSyncClient[0]);
        } catch (Exception e) {
            log.error("❌ 获取MCP客户端时出错: {}", e.getMessage());
            return new McpSyncClient[0];
        }
    }

}
