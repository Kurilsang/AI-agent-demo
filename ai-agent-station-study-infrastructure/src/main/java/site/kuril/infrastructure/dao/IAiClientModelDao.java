package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientModel;

import java.util.List;

@Mapper
public interface IAiClientModelDao {

    /**
     * 插入模型配置
     * @param model 模型配置对象
     * @return 影响行数
     */
    int insert(AiClientModel model);

    /**
     * 根据ID更新模型配置
     * @param model 模型配置对象
     * @return 影响行数
     */
    int updateById(AiClientModel model);

    /**
     * 根据模型ID更新模型配置
     * @param model 模型配置对象
     * @return 影响行数
     */
    int updateByModelId(AiClientModel model);

    /**
     * 根据ID删除模型配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据模型ID删除模型配置
     * @param modelId 模型ID
     * @return 影响行数
     */
    int deleteByModelId(String modelId);

    /**
     * 根据API ID删除所有关联模型
     * @param apiId API ID
     * @return 影响行数
     */
    int deleteByApiId(String apiId);

    /**
     * 根据ID查询模型配置
     * @param id 主键ID
     * @return 模型配置对象
     */
    AiClientModel queryById(Long id);

    /**
     * 根据模型ID查询模型配置
     * @param modelId 模型ID
     * @return 模型配置对象
     */
    AiClientModel queryByModelId(String modelId);

    /**
     * 根据API ID查询模型配置列表
     * @param apiId API ID
     * @return 模型配置列表
     */
    List<AiClientModel> queryByApiId(String apiId);

    /**
     * 根据模型类型查询模型配置
     * @param modelType 模型类型
     * @return 模型配置列表
     */
    List<AiClientModel> queryByModelType(String modelType);

    /**
     * 查询启用的模型配置
     * @return 模型配置列表
     */
    List<AiClientModel> queryEnabledModels();

    /**
     * 查询所有模型配置
     * @return 模型配置列表
     */
    List<AiClientModel> queryAll();
} 