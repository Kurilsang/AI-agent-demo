package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiAgent;

import java.util.List;

@Mapper
public interface IAiAgentDao {

    /**
     * 插入智能体配置
     * @param aiAgent 智能体配置对象
     * @return 影响行数
     */
    int insert(AiAgent aiAgent);

    /**
     * 根据ID更新智能体配置
     * @param aiAgent 智能体配置对象
     * @return 影响行数
     */
    int updateById(AiAgent aiAgent);

    /**
     * 根据智能体ID更新智能体配置
     * @param aiAgent 智能体配置对象
     * @return 影响行数
     */
    int updateByAgentId(AiAgent aiAgent);

    /**
     * 根据ID删除智能体配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据智能体ID删除智能体配置
     * @param agentId 智能体ID
     * @return 影响行数
     */
    int deleteByAgentId(String agentId);

    /**
     * 根据ID查询智能体配置
     * @param id 主键ID
     * @return 智能体配置对象
     */
    AiAgent queryById(Long id);

    /**
     * 根据智能体ID查询智能体配置
     * @param agentId 智能体ID
     * @return 智能体配置对象
     */
    AiAgent queryByAgentId(String agentId);

    /**
     * 查询启用的智能体配置
     * @return 智能体配置列表
     */
    List<AiAgent> queryEnabledAgents();

    /**
     * 根据渠道类型查询智能体配置
     * @param channel 渠道类型
     * @return 智能体配置列表
     */
    List<AiAgent> queryByChannel(String channel);

    /**
     * 查询所有智能体配置
     * @return 智能体配置列表
     */
    List<AiAgent> queryAll();
} 