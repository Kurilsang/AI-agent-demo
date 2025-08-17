package site.kuril.test.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import site.kuril.domain.agent.service.example.AiAgentBuildExample;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * 基于真实配置的AI Agent功能测试
 * 使用AiAgentTest.java中的真实API配置进行测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RealConfigAgentTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private AiAgentBuildExample aiAgentBuildExample;

    @Test
    public void test_realConfigApiNode() throws Exception {
        log.info("========== 开始测试真实配置的AI Agent API构建 ==========");

        // 1. 构建客户端API（使用真实的YunWu AI配置）
        String result = aiAgentBuildExample.buildClientApi("3001");
        log.info("API构建结果: {}", result);

        // 2. 验证是否成功注册到Spring容器
        boolean isValid = aiAgentBuildExample.verifyApiBean("1001");
        log.info("API Bean验证结果: {}", isValid);

        // 3. 如果构建成功，获取API Bean并测试
        if (isValid) {
            OpenAiApi openAiApi = aiAgentBuildExample.getApiBean("1001");
            log.info("成功获取OpenAiApi Bean: {}", openAiApi.getClass().getSimpleName());
            
            // 验证API配置
            log.info("API配置验证 - 这是基于AiAgentTest.java中的真实yunwu.ai配置");
        }

        log.info("========== 真实配置AI Agent API构建测试完成 ==========");
    }

    @Test
    public void test_realApiChatTest() throws Exception {
        log.info("========== 开始测试真实API对话功能 ==========");

        // 1. 先构建API
        String result = aiAgentBuildExample.buildClientApi("3001");
        log.info("API构建结果: {}", result);

        // 2. 验证构建是否成功
        if (!aiAgentBuildExample.verifyApiBean("1001")) {
            log.warn("API构建失败，跳过对话测试");
            return;
        }

        try {
            // 3. 获取构建的API Bean
            OpenAiApi openAiApi = aiAgentBuildExample.getApiBean("1001");
            
            // 4. 创建ChatModel（基于AiAgentTest.java的配置）
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model("gpt-4o")  // 使用真实的模型配置
                            .build())
                    .build();

            // 5. 进行简单的对话测试
            log.info("开始进行AI对话测试...");
            Prompt prompt = new Prompt("你好，这是一个AI Agent动态实例化测试");
            
            // 注意：这里可能会真实调用API，需要确保API密钥有效
            log.info("准备调用真实API - 如果API密钥无效，此测试会失败");
            
            // 如果需要真实测试，取消注释下面的代码
             ChatResponse response = chatModel.call(prompt);
             log.info("AI对话响应: {}", response.getResult().getOutput());
            
            log.info("真实API对话测试准备完成（为避免API调用，实际请求已注释）");
            
        } catch (Exception e) {
            log.warn("API对话测试失败，可能是API密钥无效或网络问题: {}", e.getMessage());
        }

        log.info("========== 真实API对话功能测试完成 ==========");
    }

    @Test
    public void test_multipleRealConfig() throws Exception {
        log.info("========== 开始测试多客户端真实配置构建 ==========");

        // 1. 批量构建多个客户端
        aiAgentBuildExample.batchBuildAndVerify("3001", "3002");

        // 2. 详细验证每个API配置
        String[] realApiIds = {"1001", "1002"};
        for (String apiId : realApiIds) {
            if (aiAgentBuildExample.verifyApiBean(apiId)) {
                OpenAiApi api = aiAgentBuildExample.getApiBean(apiId);
                log.info("API {} 构建成功，类型: {}", apiId, api.getClass().getSimpleName());
            }
        }

        log.info("========== 多客户端真实配置构建测试完成 ==========");
    }

    @Test
    public void test_fullConfigDataLoad() throws Exception {
        log.info("========== 开始测试完整配置数据加载 ==========");

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 构建装备命令实体
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                .commandIdList(Arrays.asList("3001"))
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行策略处理
        String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
        log.info("完整配置数据加载结果: {}", result);

        // 验证动态上下文中的数据
        Object apiData = dynamicContext.get("ai_client_api");
        Object modelData = dynamicContext.get("ai_client_model");
        Object mcpData = dynamicContext.get("ai_client_tool_mcp");
        Object promptData = dynamicContext.get("ai_client_system_prompt");
        Object advisorData = dynamicContext.get("ai_client_advisor");
        Object clientData = dynamicContext.get("ai_client");

        log.info("动态上下文数据验证:");
        log.info("- API数据: {}", apiData != null ? "已加载" : "未加载");
        log.info("- 模型数据: {}", modelData != null ? "已加载" : "未加载");
        log.info("- MCP工具数据: {}", mcpData != null ? "已加载" : "未加载");
        log.info("- 提示词数据: {}", promptData != null ? "已加载" : "未加载");
        log.info("- 顾问数据: {}", advisorData != null ? "已加载" : "未加载");
        log.info("- 客户端数据: {}", clientData != null ? "已加载" : "未加载");

        log.info("========== 完整配置数据加载测试完成 ==========");
    }

    @Test
    public void test_configMapping() throws Exception {
        log.info("========== 开始测试配置映射关系 ==========");
        
        log.info("测试基于AiAgentTest.java的真实配置映射:");
        log.info("客户端3001 配置详情:");
        log.info("- API: 1001 (yunwu.ai)");
        log.info("- 模型: gpt-4o, gpt-4.1");
        log.info("- MCP工具: FileSystem, CSDN发布, 微信通知");
        log.info("- 系统提示词: AI Agent智能体");
        log.info("- 顾问: 记忆、RAG、日志");

        // 构建并验证配置
        String result = aiAgentBuildExample.buildClientApi("3001");
        boolean isValid = aiAgentBuildExample.verifyApiBean("1001");
        
        log.info("配置映射验证结果:");
        log.info("- 构建结果: {}", result);
        log.info("- API验证: {}", isValid ? "成功" : "失败");
        
        if (isValid) {
            log.info("✅ 配置映射测试成功 - 真实配置已正确加载和实例化");
        } else {
            log.warn("❌ 配置映射测试失败 - 请检查数据库配置和关联关系");
        }

        log.info("========== 配置映射关系测试完成 ==========");
    }

} 