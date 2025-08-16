package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientRagOrder;

import java.util.List;

@Mapper
public interface IAiClientRagOrderDao {

    /**
     * 插入知识库配置
     * @param ragOrder 知识库配置对象
     * @return 影响行数
     */
    int insert(AiClientRagOrder ragOrder);

    /**
     * 根据ID更新知识库配置
     * @param ragOrder 知识库配置对象
     * @return 影响行数
     */
    int updateById(AiClientRagOrder ragOrder);

    /**
     * 根据知识库ID更新知识库配置
     * @param ragOrder 知识库配置对象
     * @return 影响行数
     */
    int updateByRagId(AiClientRagOrder ragOrder);

    /**
     * 根据ID删除知识库配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据知识库ID删除知识库配置
     * @param ragId 知识库ID
     * @return 影响行数
     */
    int deleteByRagId(String ragId);

    /**
     * 根据ID查询知识库配置
     * @param id 主键ID
     * @return 知识库配置对象
     */
    AiClientRagOrder queryById(Long id);

    /**
     * 根据知识库ID查询知识库配置
     * @param ragId 知识库ID
     * @return 知识库配置对象
     */
    AiClientRagOrder queryByRagId(String ragId);

    /**
     * 根据知识标签查询知识库配置
     * @param knowledgeTag 知识标签
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryByKnowledgeTag(String knowledgeTag);

    /**
     * 根据知识库名称模糊查询知识库配置
     * @param ragName 知识库名称
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryByRagNameLike(String ragName);

    /**
     * 查询启用的知识库配置
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryEnabledRags();

    /**
     * 查询所有知识库配置
     * @return 知识库配置列表
     */
    List<AiClientRagOrder> queryAll();
} 