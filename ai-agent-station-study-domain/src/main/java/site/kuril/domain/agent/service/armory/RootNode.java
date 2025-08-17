package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 根节点实现
 * 继承抽象支撑类，实现数据加载的路由分发
 * 
 * 注意：完整版本需要继承扳手工程的设计模式框架
 * 当前为简化实现版本
 */
@Slf4j
@Service
public class RootNode extends AbstractArmorySupport {

    private final Map<String, ILoadDataStrategy> loadDataStrategyMap;

    @Resource
    private AiClientApiNode aiClientApiNode;

    public RootNode(Map<String, ILoadDataStrategy> loadDataStrategyMap) {
        this.loadDataStrategyMap = loadDataStrategyMap;
    }

    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter, Object dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 获取命令；不同的命令类型，对应不同的数据加载策略
        String commandType = requestParameter.getCommandType();

        // 获取策略
        AiAgentEnumVO aiAgentEnumVO = AiAgentEnumVO.getByCode(commandType);
        String loadDataStrategyKey = aiAgentEnumVO.getLoadDataStrategy();

        // 加载数据
        ILoadDataStrategy loadDataStrategy = loadDataStrategyMap.get(loadDataStrategyKey);
        if (loadDataStrategy != null) {
            loadDataStrategy.loadData(requestParameter, dynamicContext);
            log.info("数据加载策略执行完成，命令类型: {}, 策略: {}", commandType, loadDataStrategyKey);
        } else {
            log.warn("未找到对应的数据加载策略，命令类型: {}", commandType);
        }
    }

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建，数据加载节点 {}", JSON.toJSONString(requestParameter));
        return router(requestParameter, dynamicContext);
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 执行多线程数据加载
        multiThread(requestParameter, dynamicContext);
        
        // 路由到下一个节点
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        
        if (nextHandler != null) {
            return nextHandler.apply(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        }
        
        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点
     * 完整版本应实现：StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(...)
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity armoryCommandEntity, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        // 根据命令类型决定下一个节点
        String commandType = armoryCommandEntity.getCommandType();
        if (AiAgentEnumVO.AI_CLIENT.getCode().equals(commandType)) {
            // 客户端命令，路由到API构建节点
            return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
                @Override
                public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                    return aiClientApiNode.doApply(entity, context);
                }
            };
        }
        
        // 默认不路由到下一个节点
        return null;
    }

    /**
     * 简化版本的处理方法
     */
    public String process(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext) throws Exception {
        return doApply(armoryCommandEntity, dynamicContext);
    }

} 