package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiAgentTaskSchedule;

import java.util.List;

@Mapper
public interface IAiAgentTaskScheduleDao {

    /**
     * 插入智能体任务调度配置
     * @param taskSchedule 任务调度配置对象
     * @return 影响行数
     */
    int insert(AiAgentTaskSchedule taskSchedule);

    /**
     * 根据ID更新智能体任务调度配置
     * @param taskSchedule 任务调度配置对象
     * @return 影响行数
     */
    int updateById(AiAgentTaskSchedule taskSchedule);

    /**
     * 根据ID删除智能体任务调度配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据智能体ID删除所有任务调度配置
     * @param agentId 智能体ID
     * @return 影响行数
     */
    int deleteByAgentId(Long agentId);

    /**
     * 根据ID查询智能体任务调度配置
     * @param id 主键ID
     * @return 任务调度配置对象
     */
    AiAgentTaskSchedule queryById(Long id);

    /**
     * 根据智能体ID查询任务调度配置列表
     * @param agentId 智能体ID
     * @return 任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryByAgentId(Long agentId);

    /**
     * 查询启用的任务调度配置
     * @return 任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryEnabledTasks();

    /**
     * 根据任务名称查询任务调度配置
     * @param taskName 任务名称
     * @return 任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryByTaskName(String taskName);

    /**
     * 查询所有智能体任务调度配置
     * @return 任务调度配置列表
     */
    List<AiAgentTaskSchedule> queryAll();
} 