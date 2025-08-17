package site.kuril.domain.agent.adapter.port;

import site.kuril.domain.agent.model.valobj.*;

import java.util.List;

/**
 * Agent仓储接口
 * 数据获取适配器端口，用于完成各项数据的加载
 */
public interface IAgentRepository {

    /**
     * 根据客户端ID列表查询API配置
     * @param clientIds 客户端ID列表
     * @return API配置列表
     */
    List<AiClientApiVO> queryAiClientApiVOListByClientIds(List<String> clientIds);

    /**
     * 根据模型ID列表查询API配置
     * @param modelIds 模型ID列表  
     * @return API配置列表
     */
    List<AiClientApiVO> queryAiClientApiVOListByModelIds(List<String> modelIds);

    /**
     * 根据客户端ID列表查询模型配置
     * @param clientIds 客户端ID列表
     * @return 模型配置列表
     */
    List<AiClientModelVO> AiClientModelVOByClientIds(List<String> clientIds);

    /**
     * 根据模型ID列表查询模型配置
     * @param modelIds 模型ID列表
     * @return 模型配置列表
     */
    List<AiClientModelVO> AiClientModelVOByModelIds(List<String> modelIds);

    /**
     * 根据客户端ID列表查询MCP工具配置
     * @param clientIds 客户端ID列表
     * @return MCP工具配置列表
     */
    List<AiClientToolMcpVO> AiClientToolMcpVOByClientIds(List<String> clientIds);

    /**
     * 根据客户端ID列表查询系统提示词配置
     * @param clientIds 客户端ID列表
     * @return 系统提示词配置列表
     */
    List<AiClientSystemPromptVO> AiClientSystemPromptVOByClientIds(List<String> clientIds);

    /**
     * 根据客户端ID列表查询顾问配置
     * @param clientIds 客户端ID列表
     * @return 顾问配置列表
     */
    List<AiClientAdvisorVO> AiClientAdvisorVOByClientIds(List<String> clientIds);

    /**
     * 根据客户端ID列表查询客户端配置
     * @param clientIds 客户端ID列表
     * @return 客户端配置列表
     */
    List<AiClientVO> AiClientVOByClientIds(List<String> clientIds);

} 