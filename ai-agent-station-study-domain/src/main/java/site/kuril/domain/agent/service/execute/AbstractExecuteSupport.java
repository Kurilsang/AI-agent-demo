package site.kuril.domain.agent.service.execute;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 执行抽象支撑类
 * 为AutoAgent执行节点提供基础功能支持
 */
public abstract class AbstractExecuteSupport implements DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> {

    private final Logger log = LoggerFactory.getLogger(AbstractExecuteSupport.class);

    @Resource
    protected ApplicationContext applicationContext;

    @Resource
    protected IAgentRepository repository;

    /**
     * 默认策略处理器，用于结束链路
     */
    protected DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            defaultStrategyHandler = (entity, context) -> {
        log.info("⚡ 执行链路结束");
        return "EXECUTION_COMPLETED";
    };

    /**
     * StrategyHandler接口实现 - 执行策略处理
     */
    @Override
    public String apply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return process(requestParameter, dynamicContext);
    }

    /**
     * 处理执行请求
     * @param requestParameter 执行请求参数
     * @param dynamicContext 动态上下文
     * @return 处理结果
     */
    public String process(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("🎯 开始处理执行节点: {}", this.getClass().getSimpleName());
        
        // 执行具体的应用逻辑
        doApply(requestParameter, dynamicContext);
        
        // 路由到下一个节点
        return router(requestParameter, dynamicContext);
    }

    /**
     * 具体的执行逻辑，由子类实现
     * @param requestParameter 执行请求参数
     * @param dynamicContext 动态上下文
     * @return 执行结果
     */
    protected abstract String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception;

    /**
     * 获取下一个处理节点，由子类实现
     * @param requestParameter 执行请求参数
     * @param dynamicContext 动态上下文
     * @return 下一个节点处理器
     */
    public abstract DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception;

    /**
     * 路由到下一个节点
     * @param requestParameter 执行请求参数
     * @param dynamicContext 动态上下文
     * @return 下一个节点的处理结果
     */
    protected String router(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, dynamicContext);
        
        if (nextHandler == null || nextHandler == defaultStrategyHandler) {
            log.info("✅ 执行链路完成");
            return "EXECUTION_CHAIN_COMPLETED";
        }
        
        log.info("🔄 路由到下一个执行节点");
        return nextHandler.apply(requestParameter, dynamicContext);
    }

    /**
     * 从Spring容器中获取Bean
     * @param beanName Bean名称
     * @return Bean实例
     */
    @SuppressWarnings("unchecked")
    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 获取执行节点StrategyHandler
     * @param beanName Bean名称
     * @return StrategyHandler实例
     */
    protected DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            getExecuteHandler(String beanName) {
        return (AbstractExecuteSupport) applicationContext.getBean(beanName);
    }

    /**
     * 根据客户端ID获取ChatClient
     * @param clientId 客户端ID
     * @return ChatClient实例
     */
    protected ChatClient getChatClientByClientId(String clientId) {
        String beanName = "ai_client_" + clientId;
        return applicationContext.getBean(beanName, ChatClient.class);
    }

    // =================
    // SSE 流式响应支持
    // =================

    /**
     * 通用的SSE结果发送方法
     * @param dynamicContext 动态上下文
     * @param result 要发送的结果实体
     */
    protected void sendSseResult(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                AutoAgentExecuteResultEntity result) {
        try {
            ResponseBodyEmitter emitter = dynamicContext.getValue("emitter");
            if (emitter != null) {
                // 发送SSE格式的数据
                String sseData = "data: " + JSON.toJSONString(result) + "\n\n";
                emitter.send(sseData);
                log.debug("发送SSE数据: type={}, subType={}, step={}, content={}...", 
                        result.getType(), result.getSubType(), result.getStep(), 
                        result.getContent() != null && result.getContent().length() > 50 ? 
                        result.getContent().substring(0, 50) + "..." : result.getContent());
            }
        } catch (IOException e) {
            log.error("发送SSE结果失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送步骤开始通知
     */
    protected void sendStepStart(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                 String stepName, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createStepStartResult(
                dynamicContext.getStep(), stepName, sessionId);
        sendSseResult(dynamicContext, result);
    }

    /**
     * 发送步骤完成通知
     */
    protected void sendStepComplete(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                                   String stepName, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createStepCompleteResult(
                dynamicContext.getStep(), stepName, sessionId);
        sendSseResult(dynamicContext, result);
    }

    /**
     * 发送错误信息
     */
    protected void sendError(DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext, 
                            String errorMessage, String sessionId) {
        AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createErrorResult(
                dynamicContext.getStep(), errorMessage, sessionId);
        sendSseResult(dynamicContext, result);
    }

}
