package site.kuril.domain.agent.service.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * AI Agent 构建使用示例
 * 演示如何使用数据加载策略和动态实例化API
 */
@Slf4j
@Service
public class AiAgentBuildExample {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 示例：构建客户端API
     */
    public String buildClientApi(String... clientIds) throws Exception {
        log.info("开始构建客户端API，客户端ID列表: {}", Arrays.toString(clientIds));

        // 获取策略处理器
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> handler =
                defaultArmoryStrategyFactory.armoryStrategyHandler();

        // 创建装备命令实体
        ArmoryCommandEntity commandEntity = ArmoryCommandEntity.builder()
                .commandType(AiAgentEnumVO.AI_CLIENT.getCode())
                .commandIdList(Arrays.asList(clientIds))
                .build();

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行构建过程
        String result = handler.apply(commandEntity, dynamicContext);

        log.info("客户端API构建完成，结果: {}", result);
        return result;
    }

    /**
     * 示例：验证构建的API是否可用
     */
    public boolean verifyApiBean(String apiId) {
        try {
            String beanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiId);
            OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(beanName);
            log.info("验证成功 - API Bean: {} 已注册到Spring容器", beanName);
            return true;
        } catch (Exception e) {
            log.warn("验证失败 - API Bean: {} 未找到: {}", 
                    AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiId), e.getMessage());
            return false;
        }
    }

    /**
     * 示例：获取构建的API Bean
     */
    public OpenAiApi getApiBean(String apiId) {
        try {
            String beanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName(apiId);
            OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(beanName);
            log.info("成功获取API Bean: {}", beanName);
            return openAiApi;
        } catch (Exception e) {
            log.error("获取API Bean失败，apiId: {}", apiId, e);
            throw new RuntimeException("API Bean获取失败", e);
        }
    }

    /**
     * 示例：批量构建和验证
     */
    public void batchBuildAndVerify(String... clientIds) throws Exception {
        log.info("开始批量构建和验证，客户端数量: {}", clientIds.length);

        // 1. 批量构建
        String buildResult = buildClientApi(clientIds);
        log.info("批量构建结果: {}", buildResult);

        // 2. 验证结果
        String[] testApiIds = {"1001", "1002", "2001", "2002", "3001"};
        int successCount = 0;
        
        for (String apiId : testApiIds) {
            if (verifyApiBean(apiId)) {
                successCount++;
            }
        }

        log.info("批量验证完成，成功构建API数量: {}/{}", successCount, testApiIds.length);
    }

} 