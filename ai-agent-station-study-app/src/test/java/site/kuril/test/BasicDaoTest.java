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
public class BasicDaoTest {

    // 前五个表的DAO
    @Autowired
    private IAiAgentDao aiAgentDao;

    @Autowired
    private IAiClientDao aiClientDao;

    @Autowired
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Autowired
    private IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;

    @Autowired
    private IAiClientAdvisorDao aiClientAdvisorDao;

    // 后六个表的DAO
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

    // 前五个表的测试
    @Test
    public void test_aiAgent_queryAll() {
        List<AiAgent> agents = aiAgentDao.queryAll();
        log.info("查询所有AI智能体数量: {}", agents.size());
        agents.forEach(agent -> log.info("AI智能体: {}", agent));
    }

    @Test
    public void test_aiClient_queryAll() {
        List<AiClient> clients = aiClientDao.queryAll();
        log.info("查询所有AI客户端数量: {}", clients.size());
        clients.forEach(client -> log.info("AI客户端: {}", client));
    }

    // 后六个表的测试
    @Test
    public void test_aiClientApi_queryAll() {
        List<AiClientApi> apis = aiClientApiDao.queryAll();
        log.info("查询所有API配置数量: {}", apis.size());
        apis.forEach(api -> log.info("API配置: {}", api));
    }

    @Test
    public void test_aiClientConfig_queryAll() {
        List<AiClientConfig> configs = aiClientConfigDao.queryAll();
        log.info("查询所有客户端配置关联数量: {}", configs.size());
        configs.forEach(config -> log.info("客户端配置关联: {}", config));
    }

    @Test
    public void test_aiClientModel_queryAll() {
        List<AiClientModel> models = aiClientModelDao.queryAll();
        log.info("查询所有模型配置数量: {}", models.size());
        models.forEach(model -> log.info("模型配置: {}", model));
    }

    @Test
    public void test_aiClientRagOrder_queryAll() {
        List<AiClientRagOrder> rags = aiClientRagOrderDao.queryAll();
        log.info("查询所有知识库配置数量: {}", rags.size());
        rags.forEach(rag -> log.info("知识库配置: {}", rag));
    }

    @Test
    public void test_aiClientSystemPrompt_queryAll() {
        List<AiClientSystemPrompt> prompts = aiClientSystemPromptDao.queryAll();
        log.info("查询所有系统提示词数量: {}", prompts.size());
        prompts.forEach(prompt -> log.info("系统提示词: {}", prompt));
    }

    @Test
    public void test_aiClientToolMcp_queryAll() {
        List<AiClientToolMcp> mcps = aiClientToolMcpDao.queryAll();
        log.info("查询所有MCP工具数量: {}", mcps.size());
        mcps.forEach(mcp -> log.info("MCP工具: {}", mcp));
    }

    // 批量测试启用状态的配置
    @Test
    public void test_queryAllEnabledConfigs() {
        log.info("=== 测试查询所有启用状态的配置 ===");
        
        // 智能体
        List<AiAgent> enabledAgents = aiAgentDao.queryEnabledAgents();
        log.info("启用的智能体数量: {}", enabledAgents.size());

        // 客户端
        List<AiClient> enabledClients = aiClientDao.queryEnabledClients();
        log.info("启用的客户端数量: {}", enabledClients.size());

        // API配置
        List<AiClientApi> enabledApis = aiClientApiDao.queryEnabledApis();
        log.info("启用的API配置数量: {}", enabledApis.size());

        // 客户端配置关联
        List<AiClientConfig> enabledConfigs = aiClientConfigDao.queryEnabledConfigs();
        log.info("启用的配置关联数量: {}", enabledConfigs.size());

        // 模型配置
        List<AiClientModel> enabledModels = aiClientModelDao.queryEnabledModels();
        log.info("启用的模型配置数量: {}", enabledModels.size());

        // 知识库配置
        List<AiClientRagOrder> enabledRags = aiClientRagOrderDao.queryEnabledRags();
        log.info("启用的知识库配置数量: {}", enabledRags.size());

        // 系统提示词
        List<AiClientSystemPrompt> enabledPrompts = aiClientSystemPromptDao.queryEnabledPrompts();
        log.info("启用的系统提示词数量: {}", enabledPrompts.size());

        // MCP工具
        List<AiClientToolMcp> enabledMcps = aiClientToolMcpDao.queryEnabledMcps();
        log.info("启用的MCP工具数量: {}", enabledMcps.size());
    }

    // 特定查询测试
    @Test
    public void test_specificQueries() {
        log.info("=== 测试特定查询功能 ===");
        
        // 根据ID查询
        AiClientApi api = aiClientApiDao.queryByApiId("1001");
        if (api != null) {
            log.info("根据API ID查询到配置: {}", api);
        }

        // 根据模型类型查询
        List<AiClientModel> openaiModels = aiClientModelDao.queryByModelType("openai");
        log.info("OpenAI类型模型数量: {}", openaiModels.size());

        // 根据传输类型查询MCP
        List<AiClientToolMcp> sseMcps = aiClientToolMcpDao.queryByTransportType("sse");
        log.info("SSE传输类型MCP数量: {}", sseMcps.size());
    }
} 