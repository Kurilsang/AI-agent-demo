package site.kuril.domain.agent.service.armory.business.data.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.*;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI客户端数据加载策略
 * 用于加载客户端相关的所有配置数据
 */
@Slf4j
@Service("aiClientLoadDataStrategy")
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    private IAgentRepository repository;

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext) {
        List<String> clientIdList = armoryCommandEntity.getCommandIdList();

        // 异步加载API配置数据
        CompletableFuture<List<AiClientApiVO>> aiClientApiListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", clientIdList);
            return repository.queryAiClientApiVOListByClientIds(clientIdList);
        }, threadPoolExecutor);

        // 异步加载模型配置数据
        CompletableFuture<List<AiClientModelVO>> aiClientModelListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", clientIdList);
            return repository.AiClientModelVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // 异步加载MCP工具配置数据
        CompletableFuture<List<AiClientToolMcpVO>> aiClientToolMcpListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", clientIdList);
            return repository.AiClientToolMcpVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // 异步加载系统提示词配置数据
        CompletableFuture<List<AiClientSystemPromptVO>> aiClientSystemPromptListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_system_prompt) {}", clientIdList);
            return repository.AiClientSystemPromptVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // 异步加载顾问配置数据
        CompletableFuture<List<AiClientAdvisorVO>> aiClientAdvisorListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", clientIdList);
            return repository.AiClientAdvisorVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // 异步加载客户端配置数据
        CompletableFuture<List<AiClientVO>> aiClientListFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIdList);
            return repository.AiClientVOByClientIds(clientIdList);
        }, threadPoolExecutor);

        // TODO: 等待所有异步任务完成后，将数据存储到动态上下文中
        // 完整实现需要将加载的数据存储到 dynamicContext 中供后续处理使用
        log.info("AI客户端数据加载策略执行完成，客户端ID列表: {}", clientIdList);
    }

} 