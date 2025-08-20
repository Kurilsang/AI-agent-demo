package site.kuril.domain.agent.service.armory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

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
     * 获取Bean名称
     * @param beanId Bean ID
     * @return Bean名称
     */
    protected abstract String beanName(String beanId);

    /**
     * 获取数据名称
     * @return 数据名称
     */
    protected abstract String dataName();

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

    /**
     * 动态注册Bean到Spring容器
     * @param beanName Bean名称
     * @param beanClass Bean类型
     * @param beanInstance Bean实例
     */
    protected synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        
        // 构建Bean定义
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        
        // 如果Bean已存在，先移除
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }
        
        // 注册新的Bean
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
        log.info("成功注册Bean: {}", beanName);
    }

    /**
     * 从Spring容器获取Bean
     * @param beanName Bean名称
     * @return Bean实例
     */
    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 路由方法（简化版本）
     * 完整版本应使用扳手工程的router方法
     */
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 简化版本的路由实现
        return "SUCCESS";
    }

} 