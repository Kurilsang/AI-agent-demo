package site.kuril.domain.agent.service.armory.business.data.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiClientApiVO;
import site.kuril.domain.agent.model.valobj.AiClientModelVO;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI客户端模型数据加载策略
 * 用于加载模型相关的配置数据
 */
@Slf4j
@Service("aiClientModelLoadDataStrategy")
public class AiClientModelLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    private IAgentRepository repository;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext) {
        List<String> modelIdList = armoryCommandEntity.getCommandIdList();

        // 异步加载API配置数据
        CompletableFuture<List<AiClientApiVO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", modelIdList);
            return repository.queryAiClientApiVOListByModelIds(modelIdList);
        }, threadPoolExecutor);

        // 异步加载模型配置数据
        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", modelIdList);
            return repository.AiClientModelVOByModelIds(modelIdList);
        }, threadPoolExecutor);

        // TODO: 根据文章提示，这里还缺少一个 Model 需要的类型数据加载
        // 这部分留作作业，后续章节会提供完整实现

        // TODO: 等待所有异步任务完成后，将数据存储到动态上下文中
        // 完整实现需要将加载的数据存储到 dynamicContext 中供后续处理使用
        log.info("AI客户端模型数据加载策略执行完成，模型ID列表: {}", modelIdList);
    }

} 