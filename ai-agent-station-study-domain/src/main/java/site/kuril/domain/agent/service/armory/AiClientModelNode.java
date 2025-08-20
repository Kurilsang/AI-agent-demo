package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientModelVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.List;

/**
 * AI客户端模型节点
 * 用于构建和注册OpenAiChatModel对象到Spring容器
 */
@Slf4j
@Service
public class AiClientModelNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Model 模型构建{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientModelVO> aiClientModelList = context.getValue(dataName());

        if (aiClientModelList == null || aiClientModelList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client model");
            return "SUCCESS";
        }

        for (AiClientModelVO aiClientModelVO : aiClientModelList) {
            // 1. 获取对应的 OpenAiApi Bean
            String apiBeanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientModelVO.getApiId());
            OpenAiApi openAiApi = (OpenAiApi) getBean(apiBeanName);
            log.info("获取到API Bean: {}", apiBeanName);

            // 2. 获取关联的 MCP 工具列表（如果有）
            List<String> toolMcpIds = aiClientModelVO.getToolMcpIds();
            if (toolMcpIds != null && !toolMcpIds.isEmpty()) {
                log.info("MCP工具ID列表: {}", toolMcpIds);
                // TODO: 获取实际的MCP客户端Bean并配置到模型中
                // for (String mcpId : toolMcpIds) {
                //     String mcpBeanName = AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(mcpId);
                //     McpSyncClient mcpClient = (McpSyncClient) getBean(mcpBeanName);
                // }
            }

            // 3. 构建 OpenAiChatModel
            OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .build();

            // 4. 注册Bean对象
            registerBean(beanName(aiClientModelVO.getModelId()), OpenAiChatModel.class, openAiChatModel);

            log.info("成功构建并注册 OpenAiChatModel，Bean名称: {}，模型配置: [model={}, apiId={}]",
                    beanName(aiClientModelVO.getModelId()),
                    aiClientModelVO.getModelName(),
                    aiClientModelVO.getApiId());
        }

        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        try {
            // 通过ApplicationContext获取下一个节点，避免循环依赖
            AiClientAdvisorNode aiClientAdvisorNode = applicationContext.getBean(AiClientAdvisorNode.class);
            log.info("✅ 成功获取 AiClientAdvisorNode: {}", aiClientAdvisorNode.getClass().getSimpleName());
            
            return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
                @Override
                public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                    return aiClientAdvisorNode.process(entity, context);
                }
            };
        } catch (Exception e) {
            log.error("⚠️ 获取AiClientAdvisorNode失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_MODEL.getDataName();
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 路由到下一个节点
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        
        if (nextHandler != null) {
            return nextHandler.apply(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        }
        
        return "SUCCESS";
    }

} 