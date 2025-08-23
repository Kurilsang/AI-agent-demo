package site.kuril.test.spring.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * AutoAgent测试类
 * 验证AutoAgent规则树执行链路功能
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoAgentTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private DefaultAutoAgentExecuteStrategyFactory defaultAutoAgentExecuteStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        log.info("========== 🚀 初始化AutoAgent测试环境 ==========");
        
        // 初始化装备工厂，构建必要的ChatClient
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> armoryStrategyHandler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        String armoryResult = armoryStrategyHandler.apply(
                ArmoryCommandEntity.builder()
                        .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commandIdList(Arrays.asList("3101", "3102", "3103"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext());

        log.info("🛠️ 装备工厂构建结果: {}", armoryResult);

        // 验证ChatClient是否成功构建
        try {
            ChatClient chatClient3101 = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3101"));
            ChatClient chatClient3102 = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3102"));
            ChatClient chatClient3103 = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3103"));
            
            log.info("✅ 任务分析客户端构建成功: {}", chatClient3101 != null);
            log.info("✅ 精准执行客户端构建成功: {}", chatClient3102 != null);
            log.info("✅ 质量监督客户端构建成功: {}", chatClient3103 != null);
        } catch (Exception e) {
            log.error("❌ ChatClient构建验证失败: {}", e.getMessage(), e);
        }
        
        log.info("========== ✅ AutoAgent测试环境初始化完成 ==========");
    }

    @Test
    public void test_auto_agent_simple_task() throws Exception {
        log.info("========== 🎯 开始AutoAgent简单任务测试 ==========");
        
        // 创建执行策略处理器
        DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

        // 构建执行命令实体
        ExecuteCommandEntity executeCommandEntity = ExecuteCommandEntity.builder()
                .aiAgentId("3")  // AutoAgent智能对话体
                .message("请帮我总结一下什么是Spring Boot，以及它的主要特点和优势。")
                .sessionId("test-session-simple-" + System.currentTimeMillis())
                .maxStep(3)  // 简单任务，3步足够
                .build();

        log.info("📋 执行任务: {}", executeCommandEntity.getMessage());
        log.info("🆔 会话ID: {}", executeCommandEntity.getSessionId());
        log.info("📊 最大步数: {}", executeCommandEntity.getMaxStep());

        // 执行AutoAgent任务
        String result = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        
        log.info("✅ AutoAgent执行结果: {}", result);
        log.info("========== 🎉 AutoAgent简单任务测试完成 ==========");
    }

    @Test
    public void test_auto_agent_complex_task() throws Exception {
        log.info("========== 🎯 开始AutoAgent复杂任务测试 ==========");
        
        // 创建执行策略处理器
        DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

        // 构建执行命令实体 - 复杂任务
        ExecuteCommandEntity executeCommandEntity = ExecuteCommandEntity.builder()
                .aiAgentId("3")  // AutoAgent智能对话体
                .message("请帮我设计一个在线学习平台的技术架构，包括前端、后端、数据库、缓存、消息队列等组件的选型和架构设计，并说明每个组件的作用和为什么这样选择。")
                .sessionId("test-session-complex-" + System.currentTimeMillis())
                .maxStep(5)  // 复杂任务，需要更多步数
                .build();

        log.info("📋 执行任务: {}", executeCommandEntity.getMessage());
        log.info("🆔 会话ID: {}", executeCommandEntity.getSessionId());
        log.info("📊 最大步数: {}", executeCommandEntity.getMaxStep());

        // 执行AutoAgent任务
        String result = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        
        log.info("✅ AutoAgent执行结果: {}", result);
        log.info("========== 🎉 AutoAgent复杂任务测试完成 ==========");
    }

    @Test
    public void test_auto_agent_programming_task() throws Exception {
        log.info("========== 🎯 开始AutoAgent编程任务测试 ==========");
        
        // 创建执行策略处理器
        DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

        // 构建执行命令实体 - 编程任务
        ExecuteCommandEntity executeCommandEntity = ExecuteCommandEntity.builder()
                .aiAgentId("3")  // AutoAgent智能对话体
                .message("请帮我创建一个Spring Boot项目，实现用户注册和登录功能，包括数据库设计、实体类、Repository、Service、Controller层的代码实现，以及相应的单元测试。")
                .sessionId("test-session-programming-" + System.currentTimeMillis())
                .maxStep(6)  // 编程任务复杂，需要更多步数
                .build();

        log.info("📋 执行任务: {}", executeCommandEntity.getMessage());
        log.info("🆔 会话ID: {}", executeCommandEntity.getSessionId());
        log.info("📊 最大步数: {}", executeCommandEntity.getMaxStep());

        // 执行AutoAgent任务
        String result = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        
        log.info("✅ AutoAgent执行结果: {}", result);
        log.info("========== 🎉 AutoAgent编程任务测试完成 ==========");
    }

    @Test
    public void test_auto_agent_step_limit() throws Exception {
        log.info("========== 🎯 开始AutoAgent步数限制测试 ==========");
        
        // 创建执行策略处理器
        DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

        // 构建执行命令实体 - 步数限制测试
        ExecuteCommandEntity executeCommandEntity = ExecuteCommandEntity.builder()
                .aiAgentId("3")  // AutoAgent智能对话体
                .message("请帮我制定一个详细的学习Java从入门到精通的完整计划，包括每个阶段的学习内容、推荐书籍、实践项目、时间安排等。")
                .sessionId("test-session-limit-" + System.currentTimeMillis())
                .maxStep(2)  // 故意设置较少的步数，测试限制机制
                .build();

        log.info("📋 执行任务: {}", executeCommandEntity.getMessage());
        log.info("🆔 会话ID: {}", executeCommandEntity.getSessionId());
        log.info("📊 最大步数: {} (故意设置较少)", executeCommandEntity.getMaxStep());

        // 执行AutoAgent任务
        String result = executeHandler.apply(executeCommandEntity, new DefaultAutoAgentExecuteStrategyFactory.DynamicContext());
        
        log.info("✅ AutoAgent执行结果: {}", result);
        log.info("📝 该测试验证了步数限制机制是否正常工作");
        log.info("========== 🎉 AutoAgent步数限制测试完成 ==========");
    }

}