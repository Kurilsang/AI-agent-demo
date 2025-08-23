package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientToolMcpVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI客户端MCP工具节点
 * 用于构建和注册McpSyncClient对象到Spring容器
 */
@Slf4j
@Service
public class AiClientToolMcpNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Tool MCP 工具配置{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientToolMcpVO> aiClientToolMcpList = context.getValue(dataName());

        if (aiClientToolMcpList == null || aiClientToolMcpList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client tool mcp");
            return "SUCCESS";
        }

        for (AiClientToolMcpVO aiClientToolMcpVO : aiClientToolMcpList) {
            log.info("处理MCP配置: mcpId={}, mcpName={}, transportType={}", 
                    aiClientToolMcpVO.getMcpId(), 
                    aiClientToolMcpVO.getMcpName(), 
                    aiClientToolMcpVO.getTransportType());

            // 构建MCP客户端
            log.info("开始创建MCP客户端: mcpId={}", aiClientToolMcpVO.getMcpId());
            McpSyncClient mcpClient = createMcpClient(aiClientToolMcpVO);
            
            if (mcpClient != null) {
                // 注册Bean对象
                registerBean(beanName(aiClientToolMcpVO.getMcpId()), McpSyncClient.class, mcpClient);
                
                log.info("成功创建MCP客户端: mcpId={}, mcpName={}, transportType={}, beanName={}", 
                        aiClientToolMcpVO.getMcpId(), 
                        aiClientToolMcpVO.getMcpName(),
                        aiClientToolMcpVO.getTransportType(),
                        beanName(aiClientToolMcpVO.getMcpId()));
            } else {
                log.warn("MCP客户端创建失败: mcpId={}, transportType={}", 
                        aiClientToolMcpVO.getMcpId(), 
                        aiClientToolMcpVO.getTransportType());
            }
        }

        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        try {
            // 通过ApplicationContext获取下一个节点，避免循环依赖
            AiClientModelNode aiClientModelNode = applicationContext.getBean(AiClientModelNode.class);
            log.info("✅ 成功获取 AiClientModelNode: {}", aiClientModelNode.getClass().getSimpleName());
            
            return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
                @Override
                public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                    return aiClientModelNode.process(entity, context);
                }
            };
        } catch (Exception e) {
            log.error("⚠️ 获取AiClientModelNode失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName();
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 路由到下一个节点
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        
        if (nextHandler != null) {
            return nextHandler.apply(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        }
        
        return "SUCCESS";
    }

    /**
     * 创建MCP客户端
     * 根据传输类型和配置创建相应的MCP客户端
     * 在真实环境中，这里会创建实际的McpSyncClient对象
     */
    private McpSyncClient createMcpClient(AiClientToolMcpVO mcpConfig) {
        try {
            String transportType = mcpConfig.getTransportType();
            
            if ("sse".equals(transportType)) {
                return createSseMcpClient(mcpConfig);
            } else if ("stdio".equals(transportType)) {
                return createStdioMcpClient(mcpConfig);
            } else {
                log.warn("不支持的传输类型: {}", transportType);
                return null;
            }
        } catch (Exception e) {
            log.error("创建MCP客户端失败: mcpId={}, 错误: {}", mcpConfig.getMcpId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建SSE类型的MCP客户端
     */
    private McpSyncClient createSseMcpClient(AiClientToolMcpVO mcpConfig) {
        log.info("创建SSE MCP客户端: mcpId={}", mcpConfig.getMcpId());
        
        AiClientToolMcpVO.TransportConfigSse sseConfig = mcpConfig.getTransportConfigSse();
        if (sseConfig == null) {
            log.warn("SSE配置为空，无法创建客户端: mcpId={}", mcpConfig.getMcpId());
            return null;
        }
        
        try {
            // 创建SSE传输
            HttpClientSseClientTransport transport;
            if (sseConfig.getSseEndpoint() != null && !sseConfig.getSseEndpoint().isEmpty()) {
                transport = HttpClientSseClientTransport.builder(sseConfig.getBaseUri())
                        .sseEndpoint(sseConfig.getSseEndpoint())
                        .build();
            } else {
                transport = HttpClientSseClientTransport.builder(sseConfig.getBaseUri()).build();
            }
            
            // 创建MCP同步客户端
            McpSyncClient mcpClient = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(mcpConfig.getRequestTimeout()))
                    .build();
            
            // 初始化客户端
            var initResult = mcpClient.initialize();
            log.info("SSE MCP客户端初始化成功: mcpId={}, baseUri={}, initResult={}", 
                    mcpConfig.getMcpId(), sseConfig.getBaseUri(), initResult);
            
            return mcpClient;
            
        } catch (Exception e) {
            log.error("创建SSE MCP客户端失败: mcpId={}, 错误: {}", mcpConfig.getMcpId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建STDIO类型的MCP客户端
     */
    private McpSyncClient createStdioMcpClient(AiClientToolMcpVO mcpConfig) {
        log.info("创建STDIO MCP客户端: mcpId={}", mcpConfig.getMcpId());
        
        AiClientToolMcpVO.TransportConfigStdio stdioConfig = mcpConfig.getTransportConfigStdio();
        if (stdioConfig == null || stdioConfig.getStdio() == null || stdioConfig.getStdio().isEmpty()) {
            log.warn("STDIO配置为空，无法创建客户端: mcpId={}", mcpConfig.getMcpId());
            return null;
        }
        
        try {
            // 获取第一个stdio配置（通常只有一个）
            Map.Entry<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> firstEntry = 
                    stdioConfig.getStdio().entrySet().iterator().next();
            AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = firstEntry.getValue();
            
            log.info("STDIO配置详情: key={}, command={}, args={}", 
                    firstEntry.getKey(), stdio.getCommand(), 
                    stdio.getArgs() != null ? String.join(" ", stdio.getArgs()) : "null");
            
            // 构建服务器参数
            ServerParameters.Builder paramBuilder = ServerParameters.builder(stdio.getCommand());
            
            // 添加参数
            if (stdio.getArgs() != null && stdio.getArgs().length > 0) {
                paramBuilder.args(stdio.getArgs());
                log.info("添加命令参数: {}", String.join(" ", stdio.getArgs()));
            }
            
            // 注意：当前版本的ServerParameters可能不支持environment方法
            // 如果需要环境变量，可能需要通过其他方式设置
            
            ServerParameters serverParams = paramBuilder.build();
            
            // 创建STDIO传输
            StdioClientTransport transport = new StdioClientTransport(serverParams);
            
            // 创建MCP同步客户端
            McpSyncClient mcpClient = McpClient.sync(transport)
                    .requestTimeout(Duration.ofSeconds(mcpConfig.getRequestTimeout()))
                    .build();
            
            // 初始化客户端
            var initResult = mcpClient.initialize();
            log.info("STDIO MCP客户端初始化成功: mcpId={}, command={}, initResult={}", 
                    mcpConfig.getMcpId(), stdio.getCommand(), initResult);
            
            return mcpClient;
            
        } catch (Exception e) {
            log.error("创建STDIO MCP客户端失败: mcpId={}, 错误: {}", mcpConfig.getMcpId(), e.getMessage(), e);
            return null;
        }
    }

} 