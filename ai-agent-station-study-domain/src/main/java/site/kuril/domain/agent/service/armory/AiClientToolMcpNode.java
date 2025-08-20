package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientToolMcpVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI客户端MCP工具节点
 * 用于构建和注册McpSyncClient对象到Spring容器
 */
@Slf4j
@Service
public class AiClientToolMcpNode extends AbstractArmorySupport {

    @Resource
    private AiClientModelNode aiClientModelNode;

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Tool MCP 工具配置{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientToolMcpVO> aiClientToolMcpList = context.getValue(dataName());

        if (aiClientToolMcpList == null || aiClientToolMcpList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client tool mcp");
            return router(requestParameter, dynamicContext);
        }

        for (AiClientToolMcpVO mcpVO : aiClientToolMcpList) {
            // TODO: 创建 MCP 服务
            // 暂时简化实现，只输出日志
            log.info("处理MCP配置: mcpId={}, mcpName={}, transportType={}", 
                    mcpVO.getMcpId(), mcpVO.getMcpName(), mcpVO.getTransportType());

            // TODO: 注册 MCP 对象到Spring容器
            // registerBean(beanName(mcpVO.getMcpId()), McpSyncClient.class, mcpSyncClient);
        }

        return router(requestParameter, dynamicContext);
    }

    /**
     * 获取下一个处理节点
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
            @Override
            public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                return aiClientModelNode.doApply(entity, context);
            }
        };
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName();
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