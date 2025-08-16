package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientAdvisor;

import java.util.List;

@Mapper
public interface IAiClientAdvisorDao {

    /**
     * 插入顾问配置
     * @param advisor 顾问配置对象
     * @return 影响行数
     */
    int insert(AiClientAdvisor advisor);

    /**
     * 根据ID更新顾问配置
     * @param advisor 顾问配置对象
     * @return 影响行数
     */
    int updateById(AiClientAdvisor advisor);

    /**
     * 根据顾问ID更新顾问配置
     * @param advisor 顾问配置对象
     * @return 影响行数
     */
    int updateByAdvisorId(AiClientAdvisor advisor);

    /**
     * 根据ID删除顾问配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据顾问ID删除顾问配置
     * @param advisorId 顾问ID
     * @return 影响行数
     */
    int deleteByAdvisorId(String advisorId);

    /**
     * 根据ID查询顾问配置
     * @param id 主键ID
     * @return 顾问配置对象
     */
    AiClientAdvisor queryById(Long id);

    /**
     * 根据顾问ID查询顾问配置
     * @param advisorId 顾问ID
     * @return 顾问配置对象
     */
    AiClientAdvisor queryByAdvisorId(String advisorId);

    /**
     * 查询启用的顾问配置
     * @return 顾问配置列表
     */
    List<AiClientAdvisor> queryEnabledAdvisors();

    /**
     * 根据顾问类型查询顾问配置
     * @param advisorType 顾问类型
     * @return 顾问配置列表
     */
    List<AiClientAdvisor> queryByAdvisorType(String advisorType);

    /**
     * 查询所有顾问配置，按顺序号排序
     * @return 顾问配置列表
     */
    List<AiClientAdvisor> queryAllOrderByOrderNum();

    /**
     * 查询所有顾问配置
     * @return 顾问配置列表
     */
    List<AiClientAdvisor> queryAll();
} 