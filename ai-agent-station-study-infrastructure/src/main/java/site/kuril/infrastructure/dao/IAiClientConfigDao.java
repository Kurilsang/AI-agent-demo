package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientConfig;

import java.util.List;

@Mapper
public interface IAiClientConfigDao {

    /**
     * 插入客户端配置关联
     * @param clientConfig 客户端配置对象
     * @return 影响行数
     */
    int insert(AiClientConfig clientConfig);

    /**
     * 根据ID更新客户端配置关联
     * @param clientConfig 客户端配置对象
     * @return 影响行数
     */
    int updateById(AiClientConfig clientConfig);

    /**
     * 根据ID删除客户端配置关联
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据源ID删除所有关联配置
     * @param sourceId 源ID
     * @return 影响行数
     */
    int deleteBySourceId(String sourceId);

    /**
     * 根据目标ID删除所有关联配置
     * @param targetId 目标ID
     * @return 影响行数
     */
    int deleteByTargetId(String targetId);

    /**
     * 根据ID查询客户端配置关联
     * @param id 主键ID
     * @return 客户端配置对象
     */
    AiClientConfig queryById(Long id);

    /**
     * 根据源ID查询关联配置列表
     * @param sourceId 源ID
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryBySourceId(String sourceId);

    /**
     * 根据目标ID查询关联配置列表
     * @param targetId 目标ID
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryByTargetId(String targetId);

    /**
     * 根据源类型和源ID查询关联配置
     * @param sourceType 源类型
     * @param sourceId 源ID
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryBySourceTypeAndId(String sourceType, String sourceId);

    /**
     * 根据目标类型和目标ID查询关联配置
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryByTargetTypeAndId(String targetType, String targetId);

    /**
     * 查询启用的配置关联
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryEnabledConfigs();

    /**
     * 查询所有客户端配置关联
     * @return 客户端配置列表
     */
    List<AiClientConfig> queryAll();
} 