package site.kuril.domain.agent.service.armory.factory;

import org.springframework.stereotype.Component;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.service.armory.RootNode;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 默认装备策略工厂
 * 简化版本的策略工厂实现
 * 
 * 注意：完整版本应使用扳手工程的 DefaultArmoryStrategyFactory
 */
@Component
public class DefaultArmoryStrategyFactory {

    @Resource(name = "armoryRootNode")
    private RootNode rootNode;

    /**
     * 获取策略处理器
     */
    public StrategyHandler<ArmoryCommandEntity, DynamicContext, String> armoryStrategyHandler() {
        return new StrategyHandler<ArmoryCommandEntity, DynamicContext, String>() {
            @Override
            public String apply(ArmoryCommandEntity entity, DynamicContext context) throws Exception {
                return rootNode.process(entity, context);
            }
        };
    }

    /**
     * 策略处理器接口（简化版本）
     */
    public interface StrategyHandler<T, C, R> {
        R apply(T entity, C context) throws Exception;
    }

    /**
     * 动态上下文
     * 用于存储加载的配置数据
     */
    public static class DynamicContext {
        private final Map<String, Object> contextData = new ConcurrentHashMap<>();

        public void put(String key, Object value) {
            contextData.put(key, value);
        }

        public Object get(String key) {
            return contextData.get(key);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key, Class<T> type) {
            Object value = contextData.get(key);
            if (value != null && type.isInstance(value)) {
                return (T) value;
            }
            return null;
        }

        /**
         * 获取指定类型的值
         */
        @SuppressWarnings("unchecked")
        public <T> T getValue(String key) {
            return (T) contextData.get(key);
        }

        public void clear() {
            contextData.clear();
        }

        public Map<String, Object> getAllData() {
            return new ConcurrentHashMap<>(contextData);
        }
    }

} 