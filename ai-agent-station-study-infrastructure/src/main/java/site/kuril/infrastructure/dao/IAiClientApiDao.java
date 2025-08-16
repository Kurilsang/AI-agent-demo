package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientApi;

import java.util.List;

@Mapper
public interface IAiClientApiDao {

    /**
     * 插入API配置
     * @param apiConfig API配置对象
     * @return 影响行数
     */
    int insert(AiClientApi apiConfig);

    /**
     * 根据ID更新API配置
     * @param apiConfig API配置对象
     * @return 影响行数
     */
    int updateById(AiClientApi apiConfig);

    /**
     * 根据API ID更新API配置
     * @param apiConfig API配置对象
     * @return 影响行数
     */
    int updateByApiId(AiClientApi apiConfig);

    /**
     * 根据ID删除API配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据API ID删除API配置
     * @param apiId API ID
     * @return 影响行数
     */
    int deleteByApiId(String apiId);

    /**
     * 根据ID查询API配置
     * @param id 主键ID
     * @return API配置对象
     */
    AiClientApi queryById(Long id);

    /**
     * 根据API ID查询API配置
     * @param apiId API ID
     * @return API配置对象
     */
    AiClientApi queryByApiId(String apiId);

    /**
     * 查询启用的API配置
     * @return API配置列表
     */
    List<AiClientApi> queryEnabledApis();

    /**
     * 根据基础URL模糊查询API配置
     * @param baseUrl 基础URL
     * @return API配置列表
     */
    List<AiClientApi> queryByBaseUrlLike(String baseUrl);

    /**
     * 查询所有API配置
     * @return API配置列表
     */
    List<AiClientApi> queryAll();
} 