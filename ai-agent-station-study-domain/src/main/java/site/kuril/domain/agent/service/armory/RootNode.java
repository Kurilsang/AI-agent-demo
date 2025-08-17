package site.kuril.domain.agent.service.armory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;

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

    public RootNode(Map<String, ILoadDataStrategy> loadDataStrategyMap) {
        this.loadDataStrategyMap = loadDataStrategyMap;
    }

    @Override
    protected void multiThread(ArmoryCommandEntity requestParameter, Object dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 通过策略加载数据
        String commandType = requestParameter.getCommandType();
        ILoadDataStrategy loadDataStrategy = loadDataStrategyMap.get(commandType);
        
        if (loadDataStrategy != null) {
            loadDataStrategy.loadData(requestParameter, dynamicContext);
            log.info("数据加载策略执行完成，命令类型: {}, 命令ID列表: {}", commandType, requestParameter.getCommandIdList());
        } else {
            log.warn("未找到对应的数据加载策略，命令类型: {}", commandType);
        }
    }

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 执行路由处理
        multiThread(requestParameter, dynamicContext);
        
        // 返回处理结果标识
        return "SUCCESS";
    }

    /**
     * 简化版本的策略获取方法
     * 完整版本应实现：StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(...)
     */
    public String process(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext) throws Exception {
        return doApply(armoryCommandEntity, dynamicContext);
    }

} 