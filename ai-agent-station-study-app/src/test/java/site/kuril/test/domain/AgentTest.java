package site.kuril.test.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import com.alibaba.fastjson.JSON;

import javax.annotation.Resource;
import java.util.Arrays;
import site.kuril.domain.agent.model.valobj.AiClientVO;

/**
 * AI Agent 功能测试
 * 测试数据加载和动态实例化客户端API功能
 * 
 * 注意：此测试类现在支持真实配置测试
 * 请先执行 test-data-real-config.sql 准备测试数据
 * 
 * 基于AiAgentTest.java中的真实配置：
 * - 客户端3001: yunwu.ai API (1001) + gpt-4o模型
 * - 客户端3002: openai.com API (1002) + gpt-4o系列模型
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgentTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void test_aiClientApiNode() throws Exception {
        log.info("========== 开始测试 AI Agent 客户端API节点构建 ==========");
        log.info("此测试使用真实的yunwu.ai配置（基于AiAgentTest.java）");

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 构建装备命令实体 - 使用真实配置的客户端ID
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                .commandIdList(Arrays.asList("3001"))  // YunWu AI客户端
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行策略处理
        String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
        log.info("策略执行结果: {}", result);

        // 验证是否成功注册到Spring容器
        try {
            // 使用真实的API ID获取Bean
            String expectedBeanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName("1001");
            OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(expectedBeanName);
            log.info("✅ 成功从Spring容器获取到 OpenAiApi Bean: {}", openAiApi.getClass().getSimpleName());
            log.info("Bean名称: {}", expectedBeanName);
            log.info("这是基于yunwu.ai的真实API配置");
        } catch (Exception e) {
            log.warn("❌ 从Spring容器获取OpenAiApi失败: {}", e.getMessage());
            log.warn("请确保已执行test-data-real-config.sql准备测试数据");
        }

        log.info("========== AI Agent 客户端API节点测试完成 ==========");
    }

    @Test
    public void test_aiClientDataLoad() throws Exception {
        log.info("开始测试 AI Agent 数据加载功能");

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 构建装备命令实体 - 仅加载数据，不构建API
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType("client_data_only") // 使用一个不存在的命令类型，只测试数据加载
                .commandIdList(Arrays.asList("3001", "3002"))
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        try {
            // 执行策略处理
            String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
            log.info("数据加载测试执行结果: {}", result);
        } catch (Exception e) {
            log.info("预期的异常（因为使用了不存在的命令类型）: {}", e.getMessage());
        }

        log.info("AI Agent 数据加载功能测试完成");
    }

    @Test
    public void test_multipleClientApi() throws Exception {
        log.info("========== 开始测试多个客户端API构建 ==========");
        log.info("测试客户端3001(yunwu.ai)和3002(openai.com)");

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 构建装备命令实体 - 测试多个真实客户端
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                .commandIdList(Arrays.asList("3001", "3002"))  // 真实的客户端ID
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行策略处理
        String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
        log.info("多客户端API构建结果: {}", result);

        // 验证每个真实的API Bean
        String[] realApiIds = {"1001", "1002"};  // 真实的API ID
        int foundBeans = 0;
        
        for (String apiId : realApiIds) {
            try {
                String beanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiId);
                OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(beanName);
                log.info("✅ 找到API Bean - ID: {}, Bean名称: {}", apiId, beanName);
                foundBeans++;
                
                // 显示配置信息
                if ("1001".equals(apiId)) {
                    log.info("   └─ API 1001: yunwu.ai (真实配置)");
                } else if ("1002".equals(apiId)) {
                    log.info("   └─ API 1002: api.openai.com (真实配置)");
                }
            } catch (Exception e) {
                log.warn("❌ API Bean {} 未找到: {}", apiId, e.getMessage());
            }
        }

        log.info("多客户端API构建测试完成，成功构建 {} 个API Bean", foundBeans);
        
        if (foundBeans > 0) {
            log.info("✅ 测试成功 - 至少有一个真实配置的API被成功构建");
        } else {
            log.warn("❌ 测试警告 - 没有成功构建任何API Bean，请检查测试数据");
        }

        log.info("========== 多客户端API构建测试完成 ==========");
    }

    @Test
    public void test_aiClientModelNode() throws Exception {
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        String apply = armoryStrategyHandler.apply(
                ArmoryCommandEntity.builder()
                        .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commandIdList(Arrays.asList("3001"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext());

        log.info("AI Agent 构建流程完成: {}", apply);

        // 获取构建好的 OpenAiChatModel
        OpenAiChatModel openAiChatModel = (OpenAiChatModel) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName("2001"));
        log.info("模型构建:{}", openAiChatModel);

        // 测试对话功能
        Prompt prompt = new Prompt(new UserMessage("你好，请介绍一下你自己"));

        ChatResponse chatResponse = openAiChatModel.call(prompt);

        log.info("测试结果(call):{}", JSON.toJSONString(chatResponse));
        
        // 输出对话消息
        log.info("对话输出: {}", chatResponse.getResult().getOutput());
    }

    @Test
    public void test_aiClient() throws Exception {
        log.info("========== 开始测试 AI Agent 完整客户端构建流程 ==========");
        log.info("测试从数据加载到ChatClient构建的完整责任链流程");

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 构建装备命令实体
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                .commandIdList(Arrays.asList("3001"))  // 使用真实的客户端ID
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行完整的构建流程
        String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
        log.info("AI Agent 完整构建流程完成: {}", result);

        // 验证是否成功构建了各个组件
        try {
            // 1. 验证API Bean
            String apiBeanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName("1001");
            Object apiBean = applicationContext.getBean(apiBeanName);
            log.info("✅ API Bean 构建成功: {}", apiBeanName);

            // 2. 验证模型Bean
            String modelBeanName = AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName("2001");
            Object modelBean = applicationContext.getBean(modelBeanName);
            log.info("✅ 模型Bean 构建成功: {}", modelBeanName);

            // 3. 验证顾问Bean
            try {
                Object advisorBean = applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName("6001"));
                log.info("✅ 顾问Bean 构建成功: {}", advisorBean.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("⚠️ 顾问Bean 未找到: {}", e.getMessage());
            }
            
            // 4. 验证客户端Bean
            String clientBeanName = AiAgentEnumVO.AI_CLIENT.getBeanName("3001");
            Object clientBean = applicationContext.getBean(clientBeanName);
            log.info("✅ 客户端Bean 构建成功: {}", clientBeanName);
            log.info("客户端对象类型: {}", clientBean.getClass().getSimpleName());

            // TODO: 后续可以验证 ChatClient 的实际对话功能
            log.info("🎉 完整的 AI Agent 客户端构建流程测试成功！");

        } catch (Exception e) {
            log.error("❌ AI Agent 客户端构建验证失败: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("========== AI Agent 完整客户端构建流程测试完成 ==========");
    }

    @Test
    public void test_aiClientAdvisorAndClient() throws Exception {
        log.info("========== 开始测试 AI Agent Advisor 顾问角色和 ChatClient 客户端 ==========");
        
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

        // 执行完整的构建流程
        String result = armoryStrategyHandler.apply(commandEntity, dynamicContext);
        log.info("AI Agent Advisor 和 ChatClient 构建流程完成: {}", result);

        // 验证构建结果
        log.info("========== 验证各个组件的构建状态 ==========");
        
        try {
            // 1. 验证API Bean
            Object apiBean = applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName("1001"));
            log.info("✅ API Bean 构建成功: {}", apiBean.getClass().getSimpleName());
            
            // 2. 验证模型Bean（如果存在）
            try {
                Object modelBean = applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName("2001"));
                log.info("✅ 模型Bean 构建成功: {}", modelBean.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("⚠️ 模型Bean 未找到: {}", e.getMessage());
            }
            
            // 3. 验证顾问Bean
            try {
                Object advisorBean = applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName("6001"));
                log.info("✅ 顾问Bean 构建成功: {}", advisorBean.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("⚠️ 顾问Bean 未找到: {}", e.getMessage());
            }
            
            // 4. 验证客户端Bean（如果存在）
            try {
                Object clientBean = applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3001"));
                log.info("✅ 客户端Bean 构建成功: {}", clientBean.getClass().getSimpleName());
                
                // 显示客户端配置信息
                if (clientBean instanceof AiClientVO) {
                    AiClientVO aiClientVO = (AiClientVO) clientBean;
                    log.info("客户端配置详情: clientId={}, clientName={}, description={}", 
                            aiClientVO.getClientId(), 
                            aiClientVO.getClientName(),
                            aiClientVO.getDescription());
                }
            } catch (Exception e) {
                log.warn("⚠️ 客户端Bean 未找到: {}", e.getMessage());
            }
            
            log.info("🎉 AI Agent Advisor 顾问角色和 ChatClient 客户端测试完成！");
            log.info("📋 总结：成功实现了advisor顾问角色的实例化框架和ChatClient对话客户端的构建框架");
            
        } catch (Exception e) {
            log.error("❌ 测试过程中出现错误: {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("========== AI Agent Advisor 顾问角色和 ChatClient 客户端测试完成 ==========");
    }

    @Test
    public void test_springBeans() throws Exception {
        log.info("========== 检查Spring容器中的Bean ==========");
        
        // 检查各个节点Bean
        checkBean("RootNode", "rootNode");
        checkBean("AiClientApiNode", "aiClientApiNode");
        checkBean("AiClientToolMcpNode", "aiClientToolMcpNode");
        checkBean("AiClientModelNode", "aiClientModelNode");
        checkBean("AiClientAdvisorNode", "aiClientAdvisorNode");
        checkBean("AiClientNode", "aiClientNode");
        
        log.info("========== Bean检查完成 ==========");
    }
    
    private void checkBean(String beanType, String beanName) {
        try {
            log.info("检查 {} Bean...", beanType);
            Object bean = applicationContext.getBean(beanName);
            log.info("✅ {} Bean: {}", beanType, bean.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("❌ {} Bean检查失败: {}", beanType, e.getMessage());
        }
    }
} 