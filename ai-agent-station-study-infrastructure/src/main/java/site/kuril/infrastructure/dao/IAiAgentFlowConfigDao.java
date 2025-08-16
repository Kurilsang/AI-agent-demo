package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiAgentFlowConfig;

import java.util.List;

@Mapper
public interface IAiAgentFlowConfigDao {

    /**
     * 插入智能体-客户端关联配置
     * @param flowConfig 关联配置对象
     * @return 影响行数
     */
    int insert(AiAgentFlowConfig flowConfig);

    /**
     * 根据ID更新智能体-客户端关联配置
     * @param flowConfig 关联配置对象
     * @return 影响行数
     */
    int updateById(AiAgentFlowConfig flowConfig);

    /**
     * 根据ID删除智能体-客户端关联配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据智能体ID删除所有关联配置
     * @param agentId 智能体ID
     * @return 影响行数
     */
    int deleteByAgentId(Long agentId);

    /**
     * 根据客户端ID删除所有关联配置
     * @param clientId 客户端ID
     * @return 影响行数
     */
    int deleteByClientId(Long clientId);

    /**
     * 根据ID查询智能体-客户端关联配置
     * @param id 主键ID
     * @return 关联配置对象
     */
    AiAgentFlowConfig queryById(Long id);

    /**
     * 根据智能体ID查询关联配置列表
     * @param agentId 智能体ID
     * @return 关联配置列表
     */
    List<AiAgentFlowConfig> queryByAgentId(Long agentId);

    /**
     * 根据客户端ID查询关联配置列表
     * @param clientId 客户端ID
     * @return 关联配置列表
     */
    List<AiAgentFlowConfig> queryByClientId(Long clientId);

    /**
     * 根据智能体ID和客户端ID查询关联配置
     * @param agentId 智能体ID
     * @param clientId 客户端ID
     * @return 关联配置对象
     */
    AiAgentFlowConfig queryByAgentIdAndClientId(Long agentId, Long clientId);

    /**
     * 查询所有智能体-客户端关联配置
     * @return 关联配置列表
     */
    List<AiAgentFlowConfig> queryAll();
} 