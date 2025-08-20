package site.kuril.domain.agent.service.armory.business.data.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.*;
import site.kuril.domain.agent.service.armory.business.data.ILoadDataStrategy;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

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
        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;

        try {
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

            // 等待所有异步任务完成并获取结果
            List<AiClientApiVO> aiClientApiList = aiClientApiListFuture.get();
            List<AiClientModelVO> aiClientModelList = aiClientModelListFuture.get();
            List<AiClientToolMcpVO> aiClientToolMcpList = aiClientToolMcpListFuture.get();
            List<AiClientSystemPromptVO> aiClientSystemPromptList = aiClientSystemPromptListFuture.get();
            List<AiClientAdvisorVO> aiClientAdvisorList = aiClientAdvisorListFuture.get();
            List<AiClientVO> aiClientList = aiClientListFuture.get();

            // 将加载的数据存储到动态上下文中
            context.put(AiAgentEnumVO.AI_CLIENT_API.getDataName(), aiClientApiList);
            context.put(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), aiClientModelList);
            context.put(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), aiClientToolMcpList);
            context.put(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), aiClientSystemPromptList);
            context.put(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), aiClientAdvisorList);
            context.put(AiAgentEnumVO.AI_CLIENT.getDataName(), aiClientList);

            log.info("AI客户端数据加载策略执行完成，客户端ID列表: {}，加载数据：API({})，模型({})，MCP工具({})，提示词({})，顾问({})，客户端({})", 
                    clientIdList, 
                    aiClientApiList.size(), 
                    aiClientModelList.size(), 
                    aiClientToolMcpList.size(), 
                    aiClientSystemPromptList.size(), 
                    aiClientAdvisorList.size(), 
                    aiClientList.size());
        } catch (Exception e) {
            log.error("AI客户端数据加载失败", e);
            throw new RuntimeException("数据加载失败", e);
        }
    }

} 