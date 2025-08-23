package site.kuril.infrastructure.dao.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import site.kuril.domain.agent.adapter.port.IAgentRepository;
import site.kuril.domain.agent.model.valobj.*;
import site.kuril.infrastructure.dao.*;
import site.kuril.infrastructure.dao.po.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;

/**
 * Agent仓储实现
 * 实现数据获取适配器端口
 */
@Slf4j
@Repository
public class AgentRepository implements IAgentRepository {

    @Resource
    private IAiClientApiDao aiClientApiDao;

    @Resource
    private IAiClientModelDao aiClientModelDao;

    @Resource
    private IAiClientToolMcpDao aiClientToolMcpDao;

    @Resource
    private IAiClientSystemPromptDao aiClientSystemPromptDao;

    @Resource
    private IAiClientAdvisorDao aiClientAdvisorDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiClientConfigDao aiClientConfigDao;

    @Resource
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Override
    public List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询API配置: {}", clientIds);
        
        // 通过配置表查询关联的API ID
        List<AiClientConfig> configs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientIds.contains(config.getSourceId()) &&
                                 "api".equals(config.getTargetType()))
                .collect(Collectors.toList());
        
        if (configs.isEmpty()) {
            return List.of();
        }
        
        List<String> apiIds = configs.stream()
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return aiClientApiDao.queryAll()
                .stream()
                .filter(api -> apiIds.contains(api.getApiId()) && api.getStatus() == 1)
                .map(this::convertToAiClientApiVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIds) {
        log.info("根据模型ID列表查询API配置: {}", modelIds);
        
        // 通过模型表查询关联的API ID
        List<AiClientModel> models = aiClientModelDao.queryAll()
                .stream()
                .filter(model -> modelIds.contains(model.getModelId()) && model.getStatus() == 1)
                .collect(Collectors.toList());
        
        if (models.isEmpty()) {
            return List.of();
        }
        
        List<String> apiIds = models.stream()
                .map(AiClientModel::getApiId)
                .collect(Collectors.toList());
        
        return aiClientApiDao.queryAll()
                .stream()
                .filter(api -> apiIds.contains(api.getApiId()) && api.getStatus() == 1)
                .map(this::convertToAiClientApiVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientModelVO> AiClientModelVOByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询模型配置: {}", clientIds);
        
        // 通过配置表查询关联的模型ID
        List<AiClientConfig> configs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientIds.contains(config.getSourceId()) &&
                                 "model".equals(config.getTargetType()))
                .collect(Collectors.toList());
        
        if (configs.isEmpty()) {
            return List.of();
        }
        
        List<String> modelIds = configs.stream()
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return aiClientModelDao.queryAll()
                .stream()
                .filter(model -> modelIds.contains(model.getModelId()) && model.getStatus() == 1)
                .map(model -> convertToAiClientModelVO(model, modelIds))
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientModelVO> AiClientModelVOByModelIds(List<String> modelIds) {
        log.info("根据模型ID列表查询模型配置: {}", modelIds);
        
        return aiClientModelDao.queryAll()
                .stream()
                .filter(model -> modelIds.contains(model.getModelId()) && model.getStatus() == 1)
                .map(model -> convertToAiClientModelVO(model))
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientToolMcpVO> AiClientToolMcpVOByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询MCP工具配置: {}", clientIds);
        
        // 通过配置表查询关联的MCP ID
        List<AiClientConfig> configs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientIds.contains(config.getSourceId()) &&
                                 "mcp".equals(config.getTargetType()))
                .collect(Collectors.toList());
        
        if (configs.isEmpty()) {
            return List.of();
        }
        
        List<String> mcpIds = configs.stream()
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return aiClientToolMcpDao.queryAll()
                .stream()
                .filter(mcp -> mcpIds.contains(mcp.getMcpId()) && mcp.getStatus() == 1)
                .map(this::convertToAiClientToolMcpVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientSystemPromptVO> AiClientSystemPromptVOByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询系统提示词配置: {}", clientIds);
        
        // 通过配置表查询关联的提示词ID
        List<AiClientConfig> configs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientIds.contains(config.getSourceId()) &&
                                 "prompt".equals(config.getTargetType()))
                .collect(Collectors.toList());
        
        if (configs.isEmpty()) {
            return List.of();
        }
        
        List<String> promptIds = configs.stream()
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return aiClientSystemPromptDao.queryAll()
                .stream()
                .filter(prompt -> promptIds.contains(prompt.getPromptId()) && prompt.getStatus() == 1)
                .map(this::convertToAiClientSystemPromptVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientAdvisorVO> AiClientAdvisorVOByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询顾问配置: {}", clientIds);
        
        // 通过配置表查询关联的顾问ID
        List<AiClientConfig> configs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientIds.contains(config.getSourceId()) &&
                                 "advisor".equals(config.getTargetType()))
                .collect(Collectors.toList());
        
        if (configs.isEmpty()) {
            return List.of();
        }
        
        List<String> advisorIds = configs.stream()
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return aiClientAdvisorDao.queryAll()
                .stream()
                .filter(advisor -> advisorIds.contains(advisor.getAdvisorId()) && advisor.getStatus() == 1)
                .map(this::convertToAiClientAdvisorVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiClientVO> AiClientVOByClientIds(List<String> clientIds) {
        log.info("根据客户端ID列表查询客户端配置: {}", clientIds);
        
        return aiClientDao.queryAll()
                .stream()
                .filter(client -> clientIds.contains(client.getClientId()) && client.getStatus() == 1)
                .map(this::convertToAiClientVO)
                .collect(Collectors.toList());
    }

    // 转换方法
    private AiClientApiVO convertToAiClientApiVO(AiClientApi po) {
        return AiClientApiVO.builder()
                .apiId(po.getApiId())
                .baseUrl(po.getBaseUrl())
                .apiKey(po.getApiKey())
                .completionsPath(po.getCompletionsPath())
                .embeddingsPath(po.getEmbeddingsPath())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientModelVO convertToAiClientModelVO(AiClientModel po, List<String> modelIds) {
        return convertToAiClientModelVO(po);
    }

    private AiClientModelVO convertToAiClientModelVO(AiClientModel po) {
        // 查询当前模型关联的 MCP 工具 ID
        List<String> toolMcpIds = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "model".equals(config.getSourceType()) && 
                                 po.getModelId().equals(config.getSourceId()) &&
                                 "mcp".equals(config.getTargetType()) &&
                                 config.getStatus() == 1)
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());

        return AiClientModelVO.builder()
                .modelId(po.getModelId())
                .apiId(po.getApiId())
                .modelName(po.getModelName())
                .modelType(po.getModelType())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .toolMcpIds(toolMcpIds)
                .build();
    }

    private AiClientToolMcpVO convertToAiClientToolMcpVO(AiClientToolMcp po) {
        AiClientToolMcpVO.AiClientToolMcpVOBuilder builder = AiClientToolMcpVO.builder()
                .mcpId(po.getMcpId())
                .mcpName(po.getMcpName())
                .transportType(po.getTransportType())
                .transportConfig(po.getTransportConfig())
                .requestTimeout(po.getRequestTimeout())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime());

        // 根据传输类型解析配置
        if (StringUtils.isNotBlank(po.getTransportConfig())) {
            try {
                if ("sse".equals(po.getTransportType())) {
                    AiClientToolMcpVO.TransportConfigSse sseConfig = JSON.parseObject(po.getTransportConfig(), AiClientToolMcpVO.TransportConfigSse.class);
                    builder.transportConfigSse(sseConfig);
                } else if ("stdio".equals(po.getTransportType())) {
                    AiClientToolMcpVO.TransportConfigStdio stdioConfig = JSON.parseObject(po.getTransportConfig(), AiClientToolMcpVO.TransportConfigStdio.class);
                    builder.transportConfigStdio(stdioConfig);
                }
            } catch (Exception e) {
                log.warn("解析MCP传输配置失败，mcpId: {}, transportType: {}, config: {}", 
                        po.getMcpId(), po.getTransportType(), po.getTransportConfig(), e);
            }
        }

        return builder.build();
    }

    private AiClientSystemPromptVO convertToAiClientSystemPromptVO(AiClientSystemPrompt po) {
        return AiClientSystemPromptVO.builder()
                .promptId(po.getPromptId())
                .promptName(po.getPromptName())
                .promptContent(po.getPromptContent())
                .description(po.getDescription())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientAdvisorVO convertToAiClientAdvisorVO(AiClientAdvisor po) {
        return AiClientAdvisorVO.builder()
                .advisorId(po.getAdvisorId())
                .advisorName(po.getAdvisorName())
                .advisorType(po.getAdvisorType())
                .orderNum(po.getOrderNum())
                .extParam(po.getExtParam())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    private AiClientVO convertToAiClientVO(AiClient po) {
        String clientId = po.getClientId();
        
        // 查询客户端关联的模型Bean名称
        String modelBeanName = null;
        List<AiClientConfig> modelConfigs = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientId.equals(config.getSourceId()) &&
                                 "model".equals(config.getTargetType()) &&
                                 config.getStatus() == 1)
                .collect(Collectors.toList());
        
        if (!modelConfigs.isEmpty()) {
            String modelId = modelConfigs.get(0).getTargetId();
            modelBeanName = AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(modelId);
        }
        
        // 查询客户端关联的MCP工具Bean名称列表
        List<String> mcpBeanNameList = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientId.equals(config.getSourceId()) &&
                                 "mcp".equals(config.getTargetType()) &&
                                 config.getStatus() == 1)
                .map(config -> AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(config.getTargetId()))
                .collect(Collectors.toList());
        
        // 查询客户端关联的顾问Bean名称列表
        List<String> advisorBeanNameList = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientId.equals(config.getSourceId()) &&
                                 "advisor".equals(config.getTargetType()) &&
                                 config.getStatus() == 1)
                .map(config -> AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(config.getTargetId()))
                .collect(Collectors.toList());
        
        // 查询客户端关联的提示词ID列表
        List<String> promptIdList = aiClientConfigDao.queryAll()
                .stream()
                .filter(config -> "client".equals(config.getSourceType()) && 
                                 clientId.equals(config.getSourceId()) &&
                                 "prompt".equals(config.getTargetType()) &&
                                 config.getStatus() == 1)
                .map(AiClientConfig::getTargetId)
                .collect(Collectors.toList());
        
        return AiClientVO.builder()
                .clientId(po.getClientId())
                .clientName(po.getClientName())
                .description(po.getDescription())
                .modelBeanName(modelBeanName)
                .mcpBeanNameList(mcpBeanNameList.isEmpty() ? null : mcpBeanNameList)
                .advisorBeanNameList(advisorBeanNameList.isEmpty() ? null : advisorBeanNameList)
                .promptIdList(promptIdList.isEmpty() ? null : promptIdList)
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public Map<String, AiAgentClientFlowConfigVO> queryAiAgentClientFlowConfig(String aiAgentId) {
        if (aiAgentId == null || aiAgentId.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            log.info("查询AI智能体客户端流程配置, aiAgentId: {}", aiAgentId);
            
            // 根据智能体ID查询流程配置列表
            List<AiAgentFlowConfig> flowConfigs = aiAgentFlowConfigDao.queryByAgentId(aiAgentId);
            
            if (flowConfigs == null || flowConfigs.isEmpty()) {
                log.warn("未找到智能体流程配置, aiAgentId: {}", aiAgentId);
                return new HashMap<>();
            }
            
            // 转换为Map结构，key为clientType，value为AiAgentClientFlowConfigVO
            Map<String, AiAgentClientFlowConfigVO> result = new HashMap<>();
            
            for (AiAgentFlowConfig flowConfig : flowConfigs) {
                AiAgentClientFlowConfigVO configVO = AiAgentClientFlowConfigVO.builder()
                        .clientId(flowConfig.getClientId())
                        .clientName(flowConfig.getClientName())
                        .clientType(flowConfig.getClientType())
                        .sequence(flowConfig.getSequence())
                        .build();
                
                result.put(flowConfig.getClientType(), configVO);
            }
            
            log.info("查询AI智能体客户端流程配置完成, aiAgentId: {}, 配置数量: {}", aiAgentId, result.size());
            return result;
            
        } catch (NumberFormatException e) {
            log.error("智能体ID格式错误: {}", aiAgentId, e);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("查询AI智能体客户端流程配置失败, aiAgentId: {}", aiAgentId, e);
            return new HashMap<>();
        }
    }

} 