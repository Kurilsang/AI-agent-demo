package site.kuril.test.spring.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import site.kuril.test.spring.ai.advisors.RagAnswerAdvisor;
import site.kuril.test.spring.ai.advisors.RagAdvisorConfig;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;

/**
 * AutoAgent自动智能体测试
 * 演示不同类型的Agent执行流程：固定步骤和动态决策
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoAgentTest {

    @Resource
    private VectorStore vectorStore;

    // 不同类型的ChatClient
    private OpenAiChatModel chatModel;
    private ChatClient planningChatClient;    // 任务规划客户端
    private ChatClient executorChatClient;    // 任务执行客户端
    private ChatClient reactChatClient;       // 响应式处理客户端

    // MCP客户端
    private McpSyncClient stdioMcpClient;
    private McpSyncClient sseMcpClient01;
    private McpSyncClient sseMcpClient02;
    
    // RAG顾问
    private RagAnswerAdvisor ragAnswerAdvisor;

    /**
     * 带重试机制的ChatClient调用
     * 🔄 解决超时问题的核心方法
     */
    private String callWithRetry(ChatClient chatClient, String prompt, OpenAiChatOptions options, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("🔄 尝试第{}次ChatClient调用 (最大{}次)", attempt, maxRetries);
                
                String result = chatClient
                        .prompt(prompt)
                        .options(options)
                        .call().content();
                        
                log.info("✅ 第{}次调用成功，获得响应长度: {}", attempt, result.length());
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("⚠️ 第{}次调用失败: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // 指数退避策略：2^attempt 秒
                        long delayMs = (long) Math.pow(2, attempt) * 1000;
                        log.info("😴 等待{}ms后重试...", delayMs);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试过程中被中断", ie);
                    }
                }
            }
        }
        
        log.error("❌ 所有{}次重试都失败了，最后一次错误: {}", maxRetries, lastException.getMessage());
        throw new RuntimeException("ChatClient调用失败，已重试" + maxRetries + "次", lastException);
    }

    @Before
    public void init() {
        log.info("========== 初始化AutoAgent测试环境 ==========");

        try {
            // 初始化MCP客户端
            initializeMcpClients();

            // 初始化RAG顾问
            initializeRagAdvisor();

            // 初始化 OpenAI API 配置
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl("https://yunwu.ai")
                    .apiKey("sk-Z6K1lJbIhibBudBKwzAlSrZNsBdGFbkXVseWx8sWdkh9L8O1")  // 请替换为真实的API Key
                    .completionsPath("v1/chat/completions")
                    .embeddingsPath("v1/embeddings")
                    .build();

            // 初始化ChatModel with MCP工具
            chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            // 启用真实环境：添加MCP工具
                            .toolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient, sseMcpClient01, sseMcpClient02).getToolCallbacks())
                            .build())
                    .build();

            // 初始化PlanningAgent ChatClient - 负责任务规划
            planningChatClient = ChatClient.builder(chatModel)
                    .defaultSystem("""
                            # 角色
                            你是一个智能任务规划助手，名叫AutoAgentPlanning。
                            
                            # 说明
                            你是任务规划助手，根据用户需求，拆解任务列表，制定执行计划。
                            每次执行前，必须先输出本轮思考过程，再生成具体的任务列表。
                            
                            # 技能
                            - 擅长将用户任务拆解为具体、独立的任务列表
                            - 对简单任务，避免过度拆解
                            - 对复杂任务，合理拆解为多个有逻辑关联的子任务
                            
                            # 处理需求
                            ## 拆解任务
                            - 深度推理分析用户输入，识别核心需求及潜在挑战
                            - 将复杂问题分解为可管理、可执行、独立且清晰的子任务
                            - 任务按顺序或因果逻辑组织，上下任务逻辑连贯
                            - 拆解最多不超过5个任务
                            
                            ## 输出格式
                            请按以下格式输出任务计划：
                            
                            **任务规划：**
                            1. [任务1描述]
                            2. [任务2描述]
                            3. [任务3描述]
                            ...
                            
                            **执行策略：**
                            [整体执行策略说明]
                            
                            今天是 {current_date}。
                            """)
                    .defaultAdvisors(
                            PromptChatMemoryAdvisor.builder(
                                    MessageWindowChatMemory.builder()
                                            .maxMessages(50)
                                            .build()
                            ).build(),
                            SimpleLoggerAdvisor.builder().build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(2000)
                            .temperature(0.7)
                            .build())
                    .build();

            // 初始化ExecutorAgent ChatClient - 负责任务执行
            executorChatClient = ChatClient.builder(chatModel)
                    .defaultSystem("""
                            # 角色
                            你是一个智能任务执行助手，名叫AutoAgentExecutor。
                            
                            # 说明
                            你负责执行具体的任务，根据规划的任务列表逐步完成每个子任务。
                            
                            # 执行流程
                            请使用交替进行的"思考、行动、观察"三个步骤来系统地解决任务：
                            
                            **思考：** 基于当前上下文，分析当前任务需求，明确下一步行动目标
                            **行动：** 调用相应的工具或执行具体操作
                            **观察：** 记录执行结果，分析是否达到预期目标
                            
                            # 技能
                            - 擅长使用各种工具完成具体任务
                            - 能够处理文件操作、搜索、分析等多种类型的任务
                            - 具备错误处理和重试机制
                            
                            # 约束
                            - 严格按照任务列表执行，不偏离目标
                            - 每个任务完成后需要确认结果
                            - 遇到错误时要分析原因并尝试解决
                            
                            今天是 {current_date}。
                            """)
                    // 启用真实环境：添加工具回调
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient, sseMcpClient01, sseMcpClient02).getToolCallbacks())
                    .defaultAdvisors(
                            // 注意：RAG功能现在通过工具类ragAnswerAdvisor在使用时手动调用
                            // 这样可以更灵活地控制何时启用RAG检索
                            PromptChatMemoryAdvisor.builder(
                                    MessageWindowChatMemory.builder()
                                            .maxMessages(100)
                                            .build()
                            ).build(),
                            SimpleLoggerAdvisor.builder().build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(4000)
                            .temperature(0.5)
                            .build())
                    .build();

            // 初始化 React Agent ChatClient - 负责响应式处理
            reactChatClient = ChatClient.builder(chatModel)
                    .defaultSystem("""
                            # 角色
                            你是一个智能响应助手，名叫 AutoAgent React。
                            
                            # 说明
                            你负责对用户的即时问题进行快速响应和处理，适用于简单的查询和交互。
                            
                            # 处理方式
                            - 对于简单问题，直接给出答案
                            - 对于需要工具的问题，调用相应工具获取信息
                            - 保持响应的及时性和准确性
                            
                            今天是 {current_date}。
                            """)
                    // 启用真实环境：添加工具回调
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(stdioMcpClient, sseMcpClient01, sseMcpClient02).getToolCallbacks())
                    .defaultAdvisors(
                            PromptChatMemoryAdvisor.builder(
                                    MessageWindowChatMemory.builder()
                                            .maxMessages(30)
                                            .build()
                            ).build(),
                            SimpleLoggerAdvisor.builder().build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(1500)
                            .temperature(0.3)
                            .build())
                    .build();

            log.info("✅ AutoAgent测试环境初始化完成");
            log.info("✅ MCP工具已启用：文件系统操作、SSE服务");
            log.info("✅ RAG顾问已启用：向量存储检索");

        } catch (Exception e) {
            log.warn("初始化过程中的注意事项: {}", e.getMessage());
            log.info("如果MCP服务未运行，工具功能将不可用，但基础对话功能仍可正常使用");
        }
    }

    /**
     * 初始化RAG顾问
     */
    private void initializeRagAdvisor() {
        try {
            if (vectorStore != null) {
                // 创建RAG顾问实例
                ragAnswerAdvisor = new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                        .topK(5)
                        .filterExpression("knowledge == 'article-prompt-words'")
                        .build());
                
                log.info("✅ RAG顾问初始化成功：向量存储检索");
            } else {
                log.warn("VectorStore未初始化，RAG顾问将无法使用");
            }
        } catch (Exception e) {
            log.warn("RAG顾问初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 初始化MCP客户端
     */
    private void initializeMcpClients() {
        try {
            // 初始化STDIO MCP客户端 - 文件系统操作
            stdioMcpClient = createStdioMcpClient();
            log.info("✅ STDIO MCP客户端初始化成功：文件系统操作");
            
            // 初始化SSE MCP客户端01 - CSDN发布服务  
            sseMcpClient01 = createSseMcpClient01();
            log.info("✅ SSE MCP客户端01初始化成功：CSDN发布服务");
            
            // 初始化SSE MCP客户端02 - 微信通知服务
            sseMcpClient02 = createSseMcpClient02();
            log.info("✅ SSE MCP客户端02初始化成功：微信通知服务");
            
        } catch (Exception e) {
            log.warn("MCP客户端初始化部分失败: {}", e.getMessage());
            log.info("将使用有限的工具功能继续测试");
        }
    }

    /**
     * 创建STDIO MCP客户端 - 文件系统操作
     */
    private McpSyncClient createStdioMcpClient() {
        try {
            // 基于真实的文件系统MCP服务器
            // https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem
            // 🎯 优化：限制扫描范围到当前项目，避免扫描整个开发目录
            var stdioParams = ServerParameters.builder("D:\\Develop\\nodeJs\\npx.cmd")
                    .args("-y", "@modelcontextprotocol/server-filesystem", 
                          "D:/Develop/Projects/xfg/ai-agent-station-study-3-3-agent-case")  // 只扫描当前项目
                    .build();

            // 🕒 优化：增加超时时间到3分钟，避免大文件扫描超时
            var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                    .requestTimeout(Duration.ofMinutes(3)).build();

            var init = mcpClient.initialize();
            log.info("STDIO MCP客户端初始化结果: {}", init);

            return mcpClient;
        } catch (Exception e) {
            log.warn("STDIO MCP客户端创建失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建SSE MCP客户端01 - CSDN发布服务
     */
    private McpSyncClient createSseMcpClient01() {
        try {
            HttpClientSseClientTransport sseClientTransport = 
                    HttpClientSseClientTransport.builder("http://127.0.0.1:8102").build();

            McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport)
                    .requestTimeout(Duration.ofMinutes(180)).build();

            var init = mcpSyncClient.initialize();
            log.info("SSE MCP客户端01初始化结果: {}", init);

            return mcpSyncClient;
        } catch (Exception e) {
            log.warn("SSE MCP客户端01创建失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 创建SSE MCP客户端02 - 微信通知服务
     */
    private McpSyncClient createSseMcpClient02() {
        try {
            HttpClientSseClientTransport sseClientTransport = 
                    HttpClientSseClientTransport.builder("http://127.0.0.1:8101").build();

            McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport)
                    .requestTimeout(Duration.ofMinutes(180)).build();

            var init = mcpSyncClient.initialize();
            log.info("SSE MCP客户端02初始化结果: {}", init);

            return mcpSyncClient;
        } catch (Exception e) {
            log.warn("SSE MCP客户端02创建失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 测试固定步骤的Agent工作流
     */
    @Test
    public void test_fixed_workflow_agent() {
        String userRequest = "帮我创建一个关于Spring AI框架的技术文档，包括核心概念、使用示例和最佳实践";

        log.info("=== 固定步骤 AutoAgent 工作流程测试开始 ===");
        log.info("用户请求: {}", userRequest);

        try {
            // 第一步：任务规划 (Planning)
            log.info("--- 步骤1: 任务规划 ---");
            String planningResult = planningChatClient
                    .prompt("请为以下用户需求制定详细的执行计划：" + userRequest)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .call().content();

            log.info("规划结果: {}", planningResult);

            // 第二步：任务执行 (Execution)
            log.info("--- 步骤2: 任务执行 ---");
            String executionContext = String.format("""
                    根据以下任务规划，请逐步执行每个任务：
                    
                    任务规划：
                    %s
                    
                    原始用户需求：%s
                    
                    请开始执行第一个任务。
                    """, planningResult, userRequest);

            String executionResult = executorChatClient
                    .prompt(executionContext)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .call().content();

            log.info("执行结果: {}", executionResult);

            // 第三步：结果总结和验证
            log.info("--- 步骤3: 结果总结 ---");
            String summaryContext = String.format("""
                    请对以下执行结果进行总结，并验证是否满足用户的原始需求：
                    
                    原始需求：%s
                    
                    执行结果：%s
                    
                    请提供最终的总结报告。
                    """, userRequest, executionResult);

            String summaryResult = reactChatClient
                    .prompt(summaryContext)
                    .system(s -> s.param("current_date", LocalDate.now().toString()))
                    .call().content();

            log.info("总结报告: {}", summaryResult);

        } catch (Exception e) {
            log.warn("固定工作流测试过程中的注意事项: {}", e.getMessage());
        }

        log.info("=== 固定步骤 AutoAgent 工作流程测试结束 ===");
    }

    /**
     * 测试动态多轮执行的Agent
     */
    @Test
    public void test_dynamic_multi_step_execution() {
        // 配置参数
        int maxSteps = 5; // 🎯 优化：减少执行步数，降低超时风险，提高成功率
        String userInput = "分析Spring Boot相关知识，生成学习指南。包括核心概念、实践示例和进阶路径。";
        String sessionId = "dynamic-execution-" + System.currentTimeMillis();

        log.info("=== 动态多轮执行测试开始 ===");
        log.info("用户输入: {}", userInput);
        log.info("最大执行步数: {}", maxSteps);
        log.info("会话ID: {}", sessionId);

        // 初始化执行上下文
        StringBuilder executionHistory = new StringBuilder();
        String currentTask = userInput;
        boolean isCompleted = false;

        try {
            // 初始化任务分析器 ChatClient - 负责任务分析和状态判断
            ChatClient taskAnalyzerClient = ChatClient.builder(chatModel)
                    .defaultSystem("""
                            # 角色
                            你是一个专业的任务分析师，名叫 AutoAgent Task Analyzer。
                            
                            # 核心职责
                            你负责分析任务的当前状态、执行历史和下一步行动计划：
                            1. **状态分析** : 深度分析当前任务完成情况和执行历史
                            2. **进度评估** : 评估任务完成进度和质量
                            3. **策略制定** : 制定下一步最优执行策略
                            4. **完成判断** : 准确判断任务是否已完成
                            
                            # 分析原则
                            - **全面性** : 综合考虑所有执行历史和当前状态
                            - **准确性** : 准确评估任务完成度和质量
                            - **前瞻性** : 预测可能的问题和最优路径
                            - **效率性** : 优化执行路径，避免重复工作
                            
                            # 输出格式
                            **任务状态分析:** 
                            [当前任务完成情况的详细分析]
                            
                            **执行历史评估:** 
                            [对已完成工作的质量和效果评估]
                            
                            **下一步策略:** 
                            [具体的下一步执行计划和策略]
                            
                            **完成度评估:**  [0-100]%
                            **任务状态:**  [CONTINUE/COMPLETED]
                            """)
                    .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(2000)
                            .temperature(0.3)
                            .build())
                    .build();

            // 初始化精准执行器 ChatClient - 负责具体任务执行
            ChatClient precisionExecutorClient = ChatClient.builder(chatModel)
                    .defaultSystem("""
                            # 角色
                            你是一个精准任务执行器，名叫 AutoAgent Precision Executor。
                            
                            # 核心能力
                            你专注于精准执行具体的任务步骤：
                            1. **精准执行** : 严格按照分析师的策略执行任务
                            2. **工具使用** : 熟练使用各种工具完成复杂操作
                            3. **质量控制** : 确保每一步执行的准确性和完整性
                            4. **结果记录** : 详细记录执行过程和结果
                            
                            # 执行原则
                            - **专注性** : 专注于当前分配的具体任务
                            - **精准性** : 确保执行结果的准确性和质量
                            - **完整性** : 完整执行所有必要的步骤
                            - **可追溯性** : 详细记录执行过程便于后续分析
                            
                            # 输出格式
                            **执行目标:** 
                            [本轮要执行的具体目标]
                            
                            **执行过程:** 
                            [详细的执行步骤和使用的工具]
                            
                            **执行结果:** 
                            [执行的具体结果和获得的信息]
                            
                            **质量检查:** 
                            [对执行结果的质量评估]
                            """)
                    // TODO: 在真实环境中添加工具回调
                    // .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClients).getToolCallbacks())
                    .defaultAdvisors(
                            // TODO: 在真实环境中添加RAG顾问
                            // new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                            //         .topK(8)
                            //         .filterExpression("knowledge == 'article-prompt-words'")
                            //         .build()),
                            SimpleLoggerAdvisor.builder().build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(4000)
                            .temperature(0.5)
                            .build())
                    .build();

            // 开始精准多轮执行
            for (int step = 1; step <= maxSteps && !isCompleted; step++) {
                log.info("\n🎯 === 执行第 {} 步 ===", step);

                try {
                    // 第一阶段：任务分析
                    log.info("\n📊 阶段1: 任务状态分析");
                    String analysisPrompt = String.format("""
                            **原始用户需求:**  %s
                            
                            **当前执行步骤:**  第 %d 步 (最大 %d 步)
                            
                            **历史执行记录:** 
                            %s
                            
                            **当前任务:**  %s
                            
                            请分析当前任务状态，评估执行进度，并制定下一步策略。
                            """,
                            userInput,
                            step,
                            maxSteps,
                            executionHistory.length() > 0 ? executionHistory.toString() : "[首次执行]",
                            currentTask
                    );

                    // 🔄 使用重试机制调用任务分析器，3次重试
                    String analysisResult = callWithRetry(
                            taskAnalyzerClient, 
                            analysisPrompt,
                            OpenAiChatOptions.builder()
                                    .model("gpt-4o")
                                    .maxTokens(2000)
                                    .temperature(0.3)
                                    .build(),
                            3  // 最大重试3次
                    );

                    log.info("分析结果: {}", analysisResult);

                    // 检查是否已完成
                    if (analysisResult.contains("任务状态: COMPLETED") ||
                            analysisResult.contains("完成度评估: 100%")) {
                        isCompleted = true;
                        log.info("✅ 任务分析显示已完成！");
                        break;
                    }

                    // 第二阶段：精准执行
                    log.info("\n⚡ 阶段2: 精准任务执行");
                    String executionPrompt = String.format("""
                            **分析师策略:**  %s
                            
                            **执行指令:**  根据上述分析师的策略，执行具体的任务步骤。
                            
                            **执行要求:** 
                            1. 严格按照策略执行
                            2. 使用必要的工具
                            3. 确保执行质量
                            4. 详细记录过程
                            """, analysisResult);

                    // 🔄 使用重试机制调用精准执行器，3次重试  
                    String executionResult = callWithRetry(
                            precisionExecutorClient,
                            executionPrompt,
                            OpenAiChatOptions.builder()
                                    .model("gpt-4o")
                                    .maxTokens(4000)
                                    .temperature(0.5)
                                    .build(),
                            3  // 最大重试3次
                    );

                    log.info("执行结果: {}", executionResult);

                    // 更新执行历史
                    String stepSummary = String.format("""
                            === 第 %d 步完整记录 ===
                            【分析阶段】%s
                            【执行阶段】%s
                            """, step, analysisResult, executionResult);

                    executionHistory.append(stepSummary);

                    // 提取下一步任务
                    currentTask = extractNextTask(analysisResult, executionResult, currentTask);

                    // 添加步骤间的延迟
                    Thread.sleep(1000);

                } catch (Exception e) {
                    log.error("❌ 第 {} 步执行出现异常: {}", step, e.getMessage());
                    executionHistory.append(String.format("\n=== 第 %d 步执行异常 ===\n错误: %s\n", step, e.getMessage()));
                    currentTask = "处理上一步的执行异常，继续完成原始任务";
                }
            }

            // 输出执行总结
            logExecutionSummary(maxSteps, executionHistory, isCompleted);

        } catch (Exception e) {
            log.warn("动态多轮执行测试过程中的注意事项: {}", e.getMessage());
        }

        log.info("\n🏁 === 动态多轮执行测试结束 ===");
    }

    /**
     * 测试使用动态实例化的组件
     */
    @Test
    public void test_dynamic_agent_components() {
        log.info("========== 动态Agent组件使用演示 ==========");
        
        log.info("本测试演示如何使用我们已经实现的动态实例化系统：");
        
        log.info("1. 动态API构建：");
        log.info("   - 通过AiClientApiNode构建OpenAiApi实例");
        log.info("   - 自动注册到Spring容器中");
        log.info("   - 可配置多个不同的API提供商");
        
        log.info("2. 动态模型构建：");
        log.info("   - 通过AiClientModelNode构建OpenAiChatModel");
        log.info("   - 支持不同模型配置（GPT-4、GPT-3.5等）");
        log.info("   - 集成MCP工具支持");
        
        log.info("3. 动态顾问构建：");
        log.info("   - 通过AiClientAdvisorNode构建各种Advisor");
        log.info("   - 支持PromptChatMemoryAdvisor、RagAnswerAdvisor等");
        log.info("   - 可配置不同的顾问策略");
        
        log.info("4. 动态客户端构建：");
        log.info("   - 通过AiClientNode构建完整的ChatClient");
        log.info("   - 整合API、模型、顾问、提示词等所有组件");
        log.info("   - 支持Agent工作流执行");
        
        log.info("5. Agent执行流程：");
        log.info("   - 固定步骤流程：规划 -> 执行 -> 总结");
        log.info("   - 动态决策流程：分析 -> 执行 -> 评估 -> 循环");
        log.info("   - 多轮对话支持：维护上下文状态");
        
        log.info("6. 与数据库的集成：");
        log.info("   - ai_client_api表：API配置");
        log.info("   - ai_client_model表：模型配置");
        log.info("   - ai_client_tool_mcp表：MCP工具配置");
        log.info("   - ai_client_advisor表：顾问配置");
        log.info("   - ai_client表：客户端配置");
        
        log.info("========== 动态Agent组件使用演示完成 ==========");
    }

    /**
     * 测试RAG顾问功能
     * 演示如何使用RAG顾问进行知识检索和增强
     */
    @Test
    public void test_rag_advisor_functionality() {
        log.info("========== 测试RAG顾问功能 ==========");

        if (ragAnswerAdvisor == null) {
            log.warn("RAG顾问未初始化，跳过测试");
            return;
        }

        try {
            // 1. 测试基本的RAG检索功能
            log.info("1. 测试基本RAG检索功能");
            String testQuery = "如何实现Spring AI的聊天功能？";
            log.info("测试查询: {}", testQuery);

            // 获取检索结果统计
            RagAnswerAdvisor.RagSearchResult searchResult = ragAnswerAdvisor.getSearchResult(testQuery);
            log.info("检索结果: {}", searchResult);
            log.info("检索到的文档数量: {}", searchResult.getDocumentCount());
            log.info("检索状态: {}", searchResult.getStatus());

            // 2. 测试消息增强功能
            log.info("2. 测试消息增强功能");
            String originalMessage = "请介绍Spring AI的主要特性";
            log.info("原始消息: {}", originalMessage);

            String enhancedMessage = ragAnswerAdvisor.enhanceUserMessage(originalMessage);
            log.info("增强后的消息长度: {} 字符", enhancedMessage.length());
            log.info("增强后的消息前100字符: {}", 
                    enhancedMessage.length() > 100 ? 
                    enhancedMessage.substring(0, 100) + "..." : 
                    enhancedMessage);

            // 3. 测试与ChatClient的集成
            log.info("3. 测试与ChatClient的集成");
            if (executorChatClient != null) {
                String userQuestion = "如何配置OpenAI API？";
                log.info("用户问题: {}", userQuestion);

                // 使用RAG增强问题
                String ragEnhancedQuestion = ragAnswerAdvisor.enhanceUserMessage(userQuestion);
                log.info("RAG增强问题长度: {} 字符", ragEnhancedQuestion.length());

                // 注意：在实际使用中，这里会调用ChatClient获取回答
                 String response = executorChatClient.prompt()
                         .user(ragEnhancedQuestion)
                         .call()
                         .content();
                 log.info("AI回答: {}", response);

                log.info("RAG增强的问题已准备好，可用于ChatClient调用");
            }

            // 4. 测试不同检索配置
            log.info("4. 测试不同检索配置");
            
            // 测试技术文档配置
            RagAdvisorConfig techConfig = RagAdvisorConfig.forTechnicalDocuments(vectorStore);
            RagAnswerAdvisor techRagAdvisor = techConfig.createRagAdvisor();
            
            RagAnswerAdvisor.RagSearchResult techResult = techRagAdvisor.getSearchResult("技术文档相关问题");
            log.info("技术文档检索结果: {}", techResult);

            // 测试FAQ配置
            RagAdvisorConfig faqConfig = RagAdvisorConfig.forFAQ(vectorStore);
            RagAnswerAdvisor faqRagAdvisor = faqConfig.createRagAdvisor();
            
            RagAnswerAdvisor.RagSearchResult faqResult = faqRagAdvisor.getSearchResult("常见问题");
            log.info("FAQ检索结果: {}", faqResult);

            log.info("========== RAG顾问功能测试完成 ==========");

        } catch (Exception e) {
            log.error("RAG顾问测试过程中发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 从分析和执行结果中提取下一步任务
     */
    private String extractNextTask(String analysisResult, String executionResult, String currentTask) {
        // 简化的任务提取逻辑
        if (analysisResult.contains("下一步策略:")) {
            String[] lines = analysisResult.split("\n");
            for (String line : lines) {
                if (line.contains("下一步策略:")) {
                    return line.substring(line.indexOf(":") + 1).trim();
                }
            }
        }
        return "继续执行当前任务：" + currentTask;
    }

    /**
     * 记录执行总结
     */
    private void logExecutionSummary(int maxSteps, StringBuilder executionHistory, boolean isCompleted) {
        log.info("\n📋 === 执行总结 ===");
        log.info("任务状态: {}", isCompleted ? "✅ 已完成" : "⏸️ 未完成");
        log.info("执行历史长度: {} 字符", executionHistory.length());

        if (isCompleted) {
            log.info("🎉 任务成功完成！");
        } else {
            log.info("⚠️ 任务在 {} 步内未完成，可能需要：", maxSteps);
            log.info("1. 增加最大执行步数");
            log.info("2. 优化任务拆解策略");
            log.info("3. 改进执行效率");
        }
    }
} 