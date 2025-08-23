package site.kuril.domain.agent.service.armory.business.data.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientApiVO;
import site.kuril.domain.agent.model.valobj.AiClientModelVO;
import site.kuril.domain.agent.model.valobj.AiClientToolMcpVO;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI客户端模型数据加载策略
 * 专门用于加载模型相关的配置数据
 */
@Slf4j
@Service
public class AiClientModelLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    private IAgentRepository repository;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext) {
        List<String> modelIdList = armoryCommandEntity.getCommandIdList();

        // 类型转换
        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;

        // 异步加载API配置数据
        CompletableFuture<List<AiClientApiVO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", modelIdList);
            return repository.queryAiClientApiVOListByModelIds(modelIdList);
        }, threadPoolExecutor);

        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", modelIdList);
            return repository.AiClientModelVOByModelIds(modelIdList);
        }, threadPoolExecutor);

        // 根据文章提示，Model还需要加载MCP工具配置
        CompletableFuture<List<AiClientToolMcpVO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) for models {}", modelIdList);
            // 使用现有的方法，通过客户端ID查询（因为模型和客户端是关联的）
            return repository.AiClientToolMcpVOByClientIds(modelIdList);
        }, threadPoolExecutor);

        try {
            // 等待所有异步任务完成
            List<AiClientApiVO> aiClientApiList = aiClientApiListFuture.get();
            List<AiClientModelVO> aiClientModelList = aiClientModelListFuture.get();
            List<AiClientToolMcpVO> aiClientToolMcpList = aiClientToolMcpListFuture.get();

            // 将数据存储到动态上下文中
            context.put(AiAgentEnumVO.AI_CLIENT_API.getDataName(), aiClientApiList);
            context.put(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), aiClientModelList);
            context.put(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), aiClientToolMcpList);

            log.info("AI客户端模型数据加载策略执行完成，模型ID列表: {}，加载数据：API({}), 模型({}), MCP工具({})",
                    modelIdList, aiClientApiList.size(), aiClientModelList.size(), aiClientToolMcpList.size());

        } catch (Exception e) {
            log.error("AI客户端模型数据加载失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据加载失败", e);
        }
    }
} 