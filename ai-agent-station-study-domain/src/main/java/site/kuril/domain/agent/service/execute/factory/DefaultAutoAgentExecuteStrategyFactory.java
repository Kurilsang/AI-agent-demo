package site.kuril.domain.agent.service.execute.factory;

import lombok.Data;
import org.springframework.stereotype.Component;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import site.kuril.domain.agent.service.execute.RootNode;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认AutoAgent执行策略工厂
 * 用于管理AutoAgent的多轮对话执行流程
 */
@Component
public class DefaultAutoAgentExecuteStrategyFactory {

    @Resource(name = "executeRootNode")
    private RootNode rootNode;

    /**
     * 获取策略处理器
     */
    public StrategyHandler<ExecuteCommandEntity, DynamicContext, String> armoryStrategyHandler() {
        return new StrategyHandler<ExecuteCommandEntity, DynamicContext, String>() {
            @Override
            public String apply(ExecuteCommandEntity entity, DynamicContext context) throws Exception {
                return rootNode.process(entity, context);
            }
        };
    }

    /**
     * 策略处理器接口
     */
    public interface StrategyHandler<T, C, R> {
        R apply(T entity, C context) throws Exception;
    }

    /**
     * 动态上下文
     * 用于存储AutoAgent执行过程中的状态和数据
     */
    @Data
    public static class DynamicContext {
        
        /**
         * 通用数据存储
         */
        private final Map<String, Object> contextData = new ConcurrentHashMap<>();
        
        /**
         * AI智能体客户端流程配置Map
         */
        private Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap;
        
        /**
         * 执行历史记录
         */
        private StringBuilder executionHistory;
        
        /**
         * 当前任务描述
         */
        private String currentTask;
        
        /**
         * 当前执行步数
         */
        private int step = 1;
        
        /**
         * 最大执行步数
         */
        private int maxStep;
        
        /**
         * 任务是否已完成
         */
        private boolean completed = false;

        /**
         * 存储键值对数据
         */
        public void setValue(String key, Object value) {
            contextData.put(key, value);
        }

        /**
         * 获取键值对数据
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue(String key) {
            return (T) contextData.get(key);
        }

        /**
         * 检查是否包含指定键
         */
        public boolean containsKey(String key) {
            return contextData.containsKey(key);
        }

        /**
         * 清除所有数据
         */
        public void clear() {
            contextData.clear();
            aiAgentClientFlowConfigVOMap = null;
            executionHistory = null;
            currentTask = null;
            step = 1;
            maxStep = 0;
            completed = false;
        }

        /**
         * 获取所有上下文数据
         */
        public Map<String, Object> getAllData() {
            return new ConcurrentHashMap<>(contextData);
        }
    }

}
