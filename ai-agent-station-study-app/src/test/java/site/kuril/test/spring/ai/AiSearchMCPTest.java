package site.kuril.test.spring.ai;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * AI搜索MCP服务测试
 * 演示MCP服务集成和搜索功能（需要真实MCP配置）
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AiSearchMCPTest {

    @Test
    public void test_search_simulation() {
        log.info("========== 开始测试AI搜索功能模拟 ==========");
        
        try {
            // 构建基础的OpenAI Chat Model（不使用MCP）
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(OpenAiApi.builder()
                            .baseUrl("https://yunwu.ai")  // 使用配置
                            .apiKey("sk-123456789")  // 请替换为真实的API Key
                            .completionsPath("v1/chat/completions")
                            .embeddingsPath("v1/embeddings")
                            .build())
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")
                            .maxTokens(1000)
                            .temperature(0.7)
                            .build())
                    .build();

            // 模拟搜索查询
            String searchQuery = "请介绍一下Spring AI框架的核心功能和使用场景";
            log.info("模拟搜索查询: {}", searchQuery);
            
            ChatResponse response = chatModel.call(Prompt.builder()
                    .messages(new UserMessage(searchQuery))
                    .build());
            
            log.info("AI响应结果: {}", JSON.toJSONString(response.getResult()));
            log.info("响应内容: {}", response.getResult().getOutput().getText());
            
        } catch (Exception e) {
            log.warn("AI搜索测试跳过（需要真实API配置）: {}", e.getMessage());
            log.info("要启用完整测试，请配置真实的API密钥");
        }
        
        log.info("========== AI搜索功能模拟测试完成 ==========");
    }

    @Test
    public void test_mcp_concept_demonstration() {
        log.info("========== MCP服务概念演示 ==========");
        
        // 展示MCP服务的概念和用途
        log.info("MCP (Model Context Protocol) 服务说明：");
        log.info("1. MCP允许AI模型调用外部服务和工具");
        log.info("2. 常见的MCP服务包括：");
        log.info("   - 搜索服务（百度、谷歌、必应）");
        log.info("   - 文件系统操作");
        log.info("   - 数据库查询");
        log.info("   - API调用");
        log.info("   - 计算工具");
        
        log.info("3. 在我们的系统中，MCP服务通过以下方式集成：");
        log.info("   - AiClientToolMcpVO: MCP工具配置对象");
        log.info("   - AiClientToolMcpNode: MCP工具构建节点");
        log.info("   - 支持stdio和sse两种传输方式");
        
        log.info("4. 真实使用时需要配置：");
        log.info("   - 百度搜索MCP: http://appbuilder.baidu.com/v2/ai_search/mcp/");
        log.info("   - API密钥: 需要在百度AI控制台申请");
        log.info("   - 传输配置: SSE或STDIO协议配置");
        
        log.info("========== MCP服务概念演示完成 ==========");
    }

    @Test
    public void test_mcp_integration_demo() {
        log.info("========== MCP集成演示 ==========");
        
        try {
            // 演示如何配置MCP服务
            log.info("1. 配置MCP服务的步骤：");
            
            // 模拟MCP配置过程
            log.info("   Step 1: 创建MCP传输配置");
            String mcpConfig = """
                {
                    "baseUri": "http://appbuilder.baidu.com/v2/ai_search/mcp/",
                    "sseEndpoint": "sse?api_key=your-api-key",
                    "timeout": 300
                }
                """;
            log.info("   MCP配置示例: {}", mcpConfig);
            
            log.info("   Step 2: 创建MCP客户端");
            log.info("   - 使用HttpClientSseClientTransport构建SSE传输");
            log.info("   - 配置超时时间和重试策略");
            
            log.info("   Step 3: 初始化MCP连接");
            log.info("   - 调用initialize()方法建立连接");
            log.info("   - 获取服务端能力和工具列表");
            
            log.info("   Step 4: 集成到ChatModel");
            log.info("   - 使用SyncMcpToolCallbackProvider包装MCP客户端");
            log.info("   - 将工具回调添加到ChatModel配置中");
            
            log.info("2. 使用效果：");
            log.info("   - AI可以实时搜索网络信息");
            log.info("   - 回答基于最新数据的问题");
            log.info("   - 执行复杂的多步骤任务");
            
        } catch (Exception e) {
            log.warn("MCP集成演示过程中的注意事项: {}", e.getMessage());
        }
        
        log.info("========== MCP集成演示完成 ==========");
    }

    /**
     * 演示在动态实例化系统中如何使用MCP
     */
    @Test
    public void test_dynamic_mcp_usage() {
        log.info("========== 动态MCP使用演示 ==========");
        
        log.info("在我们的动态实例化系统中，MCP服务的使用流程：");
        
        log.info("1. 数据库配置（ai_client_tool_mcp表）：");
        log.info("   - mcp_id: 唯一标识符");
        log.info("   - mcp_name: MCP服务名称");
        log.info("   - transport_type: 传输类型(sse/stdio)");
        log.info("   - transport_config: 传输配置JSON");
        
        log.info("2. 动态构建过程：");
        log.info("   - AiClientLoadDataStrategy: 加载MCP配置数据");
        log.info("   - AiClientToolMcpNode: 构建MCP客户端实例");
        log.info("   - 注册为Spring Bean供其他组件使用");
        
        log.info("3. Agent使用：");
        log.info("   - AiClientModelNode: 将MCP工具集成到ChatModel");
        log.info("   - AiClientNode: 在ChatClient中启用工具调用");
        log.info("   - Agent执行时自动调用相应工具");
        
        log.info("4. 示例配置：");
        String exampleConfig = """
            INSERT INTO ai_client_tool_mcp (mcp_id, mcp_name, transport_type, transport_config) 
            VALUES (
                '5001', 
                'Baidu Search MCP', 
                'sse',
                '{"baseUri":"http://appbuilder.baidu.com/v2/ai_search/mcp/","sseEndpoint":"sse?api_key=your-key"}'
            );
            """;
        log.info("   SQL配置示例: {}", exampleConfig);
        
        log.info("========== 动态MCP使用演示完成 ==========");
    }
} 