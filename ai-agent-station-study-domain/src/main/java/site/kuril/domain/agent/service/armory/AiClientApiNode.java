package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientApiVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.List;

/**
 * AI客户端API节点
 * 用于构建和注册OpenAiApi对象到Spring容器
 */
@Slf4j
@Service
public class AiClientApiNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，API 接口请求{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientApiVO> aiClientApiList = context.getValue(dataName());

        if (aiClientApiList == null || aiClientApiList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client api");
            return "SUCCESS";
        }

        for (AiClientApiVO aiClientApiVO : aiClientApiList) {
            // 构建OpenAiApi对象
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(aiClientApiVO.getBaseUrl())
                    .apiKey(aiClientApiVO.getApiKey())
                    .build();

            // 注册Bean对象
            registerBean(beanName(aiClientApiVO.getApiId()), OpenAiApi.class, openAiApi);

            log.info("成功构建并注册 OpenAiApi，Bean名称: {}，API配置: [baseUrl={}, apiId={}]",
                    beanName(aiClientApiVO.getApiId()),
                    aiClientApiVO.getBaseUrl(),
                    aiClientApiVO.getApiId());
        }

        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity armoryCommandEntity, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        log.info("=== AiClientApiNode.get() 方法被调用 ===");
        
        try {
            // 通过ApplicationContext获取下一个节点，避免循环依赖
            AiClientToolMcpNode aiClientToolMcpNode = applicationContext.getBean(AiClientToolMcpNode.class);
            log.info("✅ 成功获取 AiClientToolMcpNode: {}", aiClientToolMcpNode.getClass().getSimpleName());
            
            return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
                @Override
                public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                    log.info("=== 准备调用aiClientToolMcpNode.process() ===");
                    return aiClientToolMcpNode.process(entity, context);
                }
            };
        } catch (Exception e) {
            log.error("⚠️ 获取AiClientToolMcpNode失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT_API.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_API.getDataName();
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("=== AiClientApiNode.router() 方法被调用 ===");
        
        // 路由到下一个节点
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        
        if (nextHandler != null) {
            log.info("✅ 找到下一个处理节点，准备执行");
            String result = nextHandler.apply(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
            log.info("✅ 下一个处理节点执行完成，返回结果: {}", result);
            return result;
        }
        
        log.warn("⚠️ 没有找到下一个处理节点，返回SUCCESS");
        return "SUCCESS";
    }

} 