package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientSystemPrompt;

import java.util.List;

@Mapper
public interface IAiClientSystemPromptDao {

    /**
     * 插入系统提示词配置
     * @param prompt 系统提示词配置对象
     * @return 影响行数
     */
    int insert(AiClientSystemPrompt prompt);

    /**
     * 根据ID更新系统提示词配置
     * @param prompt 系统提示词配置对象
     * @return 影响行数
     */
    int updateById(AiClientSystemPrompt prompt);

    /**
     * 根据提示词ID更新系统提示词配置
     * @param prompt 系统提示词配置对象
     * @return 影响行数
     */
    int updateByPromptId(AiClientSystemPrompt prompt);

    /**
     * 根据ID删除系统提示词配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据提示词ID删除系统提示词配置
     * @param promptId 提示词ID
     * @return 影响行数
     */
    int deleteByPromptId(String promptId);

    /**
     * 根据ID查询系统提示词配置
     * @param id 主键ID
     * @return 系统提示词配置对象
     */
    AiClientSystemPrompt queryById(Long id);

    /**
     * 根据提示词ID查询系统提示词配置
     * @param promptId 提示词ID
     * @return 系统提示词配置对象
     */
    AiClientSystemPrompt queryByPromptId(String promptId);

    /**
     * 根据提示词名称模糊查询系统提示词配置
     * @param promptName 提示词名称
     * @return 系统提示词配置列表
     */
    List<AiClientSystemPrompt> queryByPromptNameLike(String promptName);

    /**
     * 查询启用的系统提示词配置
     * @return 系统提示词配置列表
     */
    List<AiClientSystemPrompt> queryEnabledPrompts();

    /**
     * 查询所有系统提示词配置
     * @return 系统提示词配置列表
     */
    List<AiClientSystemPrompt> queryAll();
} 