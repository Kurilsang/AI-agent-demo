package site.kuril.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import site.kuril.infrastructure.dao.*;
import site.kuril.infrastructure.dao.po.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExtendedDaoTest {

    @Autowired
    private IAiClientApiDao aiClientApiDao;

    @Autowired
    private IAiClientConfigDao aiClientConfigDao;

    @Autowired
    private IAiClientModelDao aiClientModelDao;

    @Autowired
    private IAiClientRagOrderDao aiClientRagOrderDao;

    @Autowired
    private IAiClientSystemPromptDao aiClientSystemPromptDao;

    @Autowired
    private IAiClientToolMcpDao aiClientToolMcpDao;

    // API配置表测试
    @Test
    public void test_aiClientApi_crud() {
        log.info("=== 测试AI客户端API配置CRUD操作 ===");
        
        // 插入测试
        AiClientApi apiConfig = AiClientApi.builder()
                .apiId("test_api_001")
                .baseUrl("https://test.example.com")
                .apiKey("test-api-key-123")
                .completionsPath("/v1/chat/completions")
                .embeddingsPath("/v1/embeddings")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientApiDao.insert(apiConfig);
        log.info("API配置插入结果: {}, 生成ID: {}", insertResult, apiConfig.getId());

        // 查询测试
        AiClientApi queryResult = aiClientApiDao.queryByApiId("test_api_001");
        if (queryResult != null) {
            log.info("查询API配置: {}", queryResult);
        }

        // 更新测试
        if (queryResult != null) {
            queryResult.setApiKey("updated-api-key-456");
            queryResult.setUpdateTime(LocalDateTime.now());
            int updateResult = aiClientApiDao.updateByApiId(queryResult);
            log.info("API配置更新结果: {}", updateResult);
        }

        // 删除测试（注释掉避免影响其他测试）
        // aiClientApiDao.deleteByApiId("test_api_001");
    }

    // 客户端配置关联表测试
    @Test
    public void test_aiClientConfig_crud() {
        log.info("=== 测试AI客户端配置关联CRUD操作 ===");
        
        AiClientConfig config = AiClientConfig.builder()
                .sourceType("client")
                .sourceId("test_client_001")
                .targetType("model")
                .targetId("test_model_001")
                .extParam("{\"timeout\": 30}")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientConfigDao.insert(config);
        log.info("客户端配置关联插入结果: {}, 生成ID: {}", insertResult, config.getId());

        // 根据源ID查询
        List<AiClientConfig> configs = aiClientConfigDao.queryBySourceId("test_client_001");
        log.info("根据源ID查询到配置关联数量: {}", configs.size());
        configs.forEach(c -> log.info("配置关联: {}", c));
    }

    // 模型配置表测试
    @Test
    public void test_aiClientModel_crud() {
        log.info("=== 测试AI客户端模型配置CRUD操作 ===");
        
        AiClientModel model = AiClientModel.builder()
                .modelId("test_model_001")
                .apiId("test_api_001")
                .modelName("gpt-4-test")
                .modelType("openai")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientModelDao.insert(model);
        log.info("模型配置插入结果: {}, 生成ID: {}", insertResult, model.getId());

        // 根据模型类型查询
        List<AiClientModel> openaiModels = aiClientModelDao.queryByModelType("openai");
        log.info("OpenAI类型模型数量: {}", openaiModels.size());
    }

    // 知识库配置表测试
    @Test
    public void test_aiClientRagOrder_crud() {
        log.info("=== 测试知识库配置CRUD操作 ===");
        
        AiClientRagOrder ragOrder = AiClientRagOrder.builder()
                .ragId("test_rag_001")
                .ragName("测试知识库")
                .knowledgeTag("技术文档")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientRagOrderDao.insert(ragOrder);
        log.info("知识库配置插入结果: {}, 生成ID: {}", insertResult, ragOrder.getId());

        // 根据知识标签查询
        List<AiClientRagOrder> rags = aiClientRagOrderDao.queryByKnowledgeTag("技术文档");
        log.info("技术文档标签的知识库数量: {}", rags.size());
    }

    // 系统提示词配置表测试
    @Test
    public void test_aiClientSystemPrompt_crud() {
        log.info("=== 测试系统提示词配置CRUD操作 ===");
        
        AiClientSystemPrompt prompt = AiClientSystemPrompt.builder()
                .promptId("test_prompt_001")
                .promptName("测试提示词")
                .promptContent("你是一个专业的测试助手，请协助完成测试任务。")
                .description("用于测试的提示词")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientSystemPromptDao.insert(prompt);
        log.info("系统提示词插入结果: {}, 生成ID: {}", insertResult, prompt.getId());

        // 模糊查询
        List<AiClientSystemPrompt> prompts = aiClientSystemPromptDao.queryByPromptNameLike("测试");
        log.info("包含'测试'的提示词数量: {}", prompts.size());
    }

    // MCP工具配置表测试
    @Test
    public void test_aiClientToolMcp_crud() {
        log.info("=== 测试MCP工具配置CRUD操作 ===");
        
        AiClientToolMcp mcp = AiClientToolMcp.builder()
                .mcpId("test_mcp_001")
                .mcpName("测试MCP工具")
                .transportType("sse")
                .transportConfig("{\"baseUri\":\"http://test.example.com\",\"sseEndpoint\":\"/sse\"}")
                .requestTimeout(180)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        int insertResult = aiClientToolMcpDao.insert(mcp);
        log.info("MCP工具插入结果: {}, 生成ID: {}", insertResult, mcp.getId());

        // 根据传输类型查询
        List<AiClientToolMcp> sseMcps = aiClientToolMcpDao.queryByTransportType("sse");
        log.info("SSE传输类型的MCP工具数量: {}", sseMcps.size());
        
        List<AiClientToolMcp> stdioMcps = aiClientToolMcpDao.queryByTransportType("stdio");
        log.info("STDIO传输类型的MCP工具数量: {}", stdioMcps.size());
    }

    // 综合查询测试
    @Test
    public void test_comprehensiveQueries() {
        log.info("=== 综合查询测试 ===");
        
        // 统计各表数据量
        int apiCount = aiClientApiDao.queryAll().size();
        int configCount = aiClientConfigDao.queryAll().size();
        int modelCount = aiClientModelDao.queryAll().size();
        int ragCount = aiClientRagOrderDao.queryAll().size();
        int promptCount = aiClientSystemPromptDao.queryAll().size();
        int mcpCount = aiClientToolMcpDao.queryAll().size();
        
        log.info("数据库表统计:");
        log.info("- API配置表: {} 条记录", apiCount);
        log.info("- 客户端配置关联表: {} 条记录", configCount);
        log.info("- 模型配置表: {} 条记录", modelCount);
        log.info("- 知识库配置表: {} 条记录", ragCount);
        log.info("- 系统提示词表: {} 条记录", promptCount);
        log.info("- MCP工具配置表: {} 条记录", mcpCount);
        
        // 查询启用状态的配置
        log.info("启用状态配置统计:");
        log.info("- 启用的API配置: {} 个", aiClientApiDao.queryEnabledApis().size());
        log.info("- 启用的配置关联: {} 个", aiClientConfigDao.queryEnabledConfigs().size());
        log.info("- 启用的模型配置: {} 个", aiClientModelDao.queryEnabledModels().size());
        log.info("- 启用的知识库: {} 个", aiClientRagOrderDao.queryEnabledRags().size());
        log.info("- 启用的提示词: {} 个", aiClientSystemPromptDao.queryEnabledPrompts().size());
        log.info("- 启用的MCP工具: {} 个", aiClientToolMcpDao.queryEnabledMcps().size());
    }
} 