package site.kuril.domain.agent.service.armory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

/**
 * 装备抽象支撑类
 * 注意：此实现需要引入扳手工程的设计模式框架
 * 完整版本应继承：AbstractMultiThreadStrategyRouter<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>
 * 
 * 扳手工程地址：https://gitcode.net/KnowledgePlanet/ai-agent-station
 * 需要先下载扳手工程，用 idea 打开，点击 install，然后引入依赖：
 * cn.bugstack.wrench:xfg-wrench-starter-design-framework
 */
public abstract class AbstractArmorySupport {

    private final Logger log = LoggerFactory.getLogger(AbstractArmorySupport.class);

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Resource
    protected IAgentRepository repository;

    /**
     * 多线程处理方法
     * 完整版本签名：protected void multiThread(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext)
     */
    protected void multiThread(ArmoryCommandEntity requestParameter, Object dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 缺省的实现
    }

    /**
     * 应用处理方法
     * 完整版本签名：protected String doApply(ArmoryCommandEntity requestParameter, DefaultArmoryStrategyFactory.DynamicContext dynamicContext)
     */
    protected abstract String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception;

} 