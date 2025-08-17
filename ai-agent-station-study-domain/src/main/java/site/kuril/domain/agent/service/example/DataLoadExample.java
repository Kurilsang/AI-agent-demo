package site.kuril.domain.agent.service.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.service.armory.RootNode;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * 数据加载使用示例
 * 演示如何使用数据加载策略系统
 */
@Slf4j
@Service
public class DataLoadExample {

    @Resource
    private RootNode rootNode;

    /**
     * 示例：加载客户端数据
     */
    public String loadClientData(String... clientIds) throws Exception {
        log.info("开始加载客户端数据，客户端ID列表: {}", Arrays.toString(clientIds));

        // 创建装备命令实体
        ArmoryCommandEntity commandEntity = new ArmoryCommandEntity();
        commandEntity.setCommandType("aiClientLoadDataStrategy");
        commandEntity.setCommandIdList(Arrays.asList(clientIds));

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行数据加载
        String result = rootNode.process(commandEntity, dynamicContext);

        log.info("客户端数据加载完成，结果: {}", result);
        return result;
    }

    /**
     * 示例：加载模型数据
     */
    public String loadModelData(String... modelIds) throws Exception {
        log.info("开始加载模型数据，模型ID列表: {}", Arrays.toString(modelIds));

        // 创建装备命令实体
        ArmoryCommandEntity commandEntity = new ArmoryCommandEntity();
        commandEntity.setCommandType("aiClientModelLoadDataStrategy");
        commandEntity.setCommandIdList(Arrays.asList(modelIds));

        // 创建动态上下文
        DefaultArmoryStrategyFactory.DynamicContext dynamicContext = new DefaultArmoryStrategyFactory.DynamicContext();

        // 执行数据加载
        String result = rootNode.process(commandEntity, dynamicContext);

        log.info("模型数据加载完成，结果: {}", result);
        return result;
    }

} 