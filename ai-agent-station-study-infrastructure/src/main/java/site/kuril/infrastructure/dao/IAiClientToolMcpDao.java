package site.kuril.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import site.kuril.infrastructure.dao.po.AiClientToolMcp;

import java.util.List;

@Mapper
public interface IAiClientToolMcpDao {

    /**
     * 插入MCP客户端配置
     * @param mcp MCP客户端配置对象
     * @return 影响行数
     */
    int insert(AiClientToolMcp mcp);

    /**
     * 根据ID更新MCP客户端配置
     * @param mcp MCP客户端配置对象
     * @return 影响行数
     */
    int updateById(AiClientToolMcp mcp);

    /**
     * 根据MCP ID更新MCP客户端配置
     * @param mcp MCP客户端配置对象
     * @return 影响行数
     */
    int updateByMcpId(AiClientToolMcp mcp);

    /**
     * 根据ID删除MCP客户端配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 根据MCP ID删除MCP客户端配置
     * @param mcpId MCP ID
     * @return 影响行数
     */
    int deleteByMcpId(String mcpId);

    /**
     * 根据ID查询MCP客户端配置
     * @param id 主键ID
     * @return MCP客户端配置对象
     */
    AiClientToolMcp queryById(Long id);

    /**
     * 根据MCP ID查询MCP客户端配置
     * @param mcpId MCP ID
     * @return MCP客户端配置对象
     */
    AiClientToolMcp queryByMcpId(String mcpId);

    /**
     * 根据传输类型查询MCP客户端配置
     * @param transportType 传输类型
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryByTransportType(String transportType);

    /**
     * 查询启用的MCP客户端配置
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryEnabledMcps();

    /**
     * 查询所有MCP客户端配置
     * @return MCP客户端配置列表
     */
    List<AiClientToolMcp> queryAll();
} 