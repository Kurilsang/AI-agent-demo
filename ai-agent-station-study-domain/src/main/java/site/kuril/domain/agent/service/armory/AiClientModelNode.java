package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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
        log.info("Ai Agent 构建节点，Mode 对话模型{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientModelVO> aiClientModelList = context.getValue(dataName());

        if (aiClientModelList == null || aiClientModelList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client model");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientModelVO modelVO : aiClientModelList) {

            // 获取当前模型关联的 API Bean 对象
            OpenAiApi openAiApi = getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(modelVO.getApiId()));
            if (null == openAiApi) {
                throw new RuntimeException("mode 2 api is null");
            }

            // TODO: 获取当前模型关联的 Tool MCP Bean 对象
            // 暂时简化实现，不使用MCP功能
            log.info("模型关联的MCP工具ID列表: {}", modelVO.getToolMcpIds());

            // 实例化对话模型（如果有其他模型对接，可以使用 one-api 服务，转换为 openai 模型格式）
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(
                            OpenAiChatOptions.builder()
                                    .model(modelVO.getModelName())
                                    // TODO: 后续添加 MCP 工具回调
                                    .build())
                    .build();

            // 注册 Bean 对象
            registerBean(beanName(modelVO.getModelId()), OpenAiChatModel.class, chatModel);
        }

        return router(requestParameter, dynamicContext);
    }

    /**
     * 获取下一个处理节点（模型构建是最后一个节点）
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        // 返回 null 表示流程结束
        return null;
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
        // 模型构建是最后一个节点，直接返回成功
        return "SUCCESS";
    }

} 