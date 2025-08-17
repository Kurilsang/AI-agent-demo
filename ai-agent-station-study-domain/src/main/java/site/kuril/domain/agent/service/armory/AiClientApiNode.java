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
        log.info("Ai Agent 构建，API 构建节点 {}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientApiVO> aiClientApiList = context.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());

        if (aiClientApiList == null || aiClientApiList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client api");
            return null;
        }

        for (AiClientApiVO aiClientApiVO : aiClientApiList) {
            // 构建 OpenAiApi
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .baseUrl(aiClientApiVO.getBaseUrl())
                    .apiKey(aiClientApiVO.getApiKey())
                    .completionsPath(aiClientApiVO.getCompletionsPath())
                    .embeddingsPath(aiClientApiVO.getEmbeddingsPath())
                    .build();

            // 注册 OpenAiApi Bean 对象
            String beanName = AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientApiVO.getApiId());
            registerBean(beanName, OpenAiApi.class, openAiApi);
            
            log.info("成功构建并注册 OpenAiApi，Bean名称: {}，API配置: [baseUrl={}, apiId={}]", 
                    beanName, aiClientApiVO.getBaseUrl(), aiClientApiVO.getApiId());
        }

        return router(requestParameter, dynamicContext);
    }

    /**
     * 简化版本的策略获取方法
     * 完整版本应实现：StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(...)
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity armoryCommandEntity, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 返回默认处理器，表示流程结束
        return null;
    }

} 