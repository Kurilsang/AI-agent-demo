package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientSystemPromptVO;
import site.kuril.domain.agent.model.valobj.AiClientVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI客户端节点
 * 用于构建和注册ChatClient对话客户端到Spring容器
 */
@Slf4j
@Service
public class AiClientNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，客户端{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientVO> aiClientList = context.getValue(dataName());
        Map<String, AiClientSystemPromptVO> systemPromptMap = context.getValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName());

        if (aiClientList == null || aiClientList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client");
            return "SUCCESS";
        }

        for (AiClientVO aiClientVO : aiClientList) {
            // 1. 预设话术
            StringBuilder defaultSystem = new StringBuilder("Ai 智能体 \r\n");
            List<String> promptIdList = aiClientVO.getPromptIdList();
            if (promptIdList != null) {
                for (String promptId : promptIdList) {
                    AiClientSystemPromptVO aiClientSystemPromptVO = systemPromptMap.get(promptId);
                    if (aiClientSystemPromptVO != null) {
                        defaultSystem.append(aiClientSystemPromptVO.getPromptContent());
                    }
                }
            }

            // 2. 获取对话模型
            Object chatModel = null;
            String modelBeanName = aiClientVO.getModelBeanName();
            if (modelBeanName != null) {
                try {
                    chatModel = getBean(modelBeanName);
                    log.info("成功获取模型Bean: {}", modelBeanName);
                } catch (Exception e) {
                    log.warn("获取模型Bean失败: {}, 错误: {}", modelBeanName, e.getMessage());
                }
            } else {
                log.warn("客户端配置中未指定模型Bean名称: clientId={}", aiClientVO.getClientId());
            }

            // 3. 获取MCP服务列表
            List<Object> mcpClients = new ArrayList<>();
            List<String> mcpBeanNameList = aiClientVO.getMcpBeanNameList();
            if (mcpBeanNameList != null && !mcpBeanNameList.isEmpty()) {
                for (String mcpBeanName : mcpBeanNameList) {
                    try {
                        Object mcpClient = getBean(mcpBeanName);
                        mcpClients.add(mcpClient);
                        log.info("成功获取MCP Bean: {}", mcpBeanName);
                    } catch (Exception e) {
                        log.warn("获取MCP Bean失败: {}, 错误: {}", mcpBeanName, e.getMessage());
                    }
                }
            } else {
                log.info("客户端配置中未指定MCP工具: clientId={}", aiClientVO.getClientId());
            }

            // 4. 获取顾问角色列表
            List<Object> advisors = new ArrayList<>();
            List<String> advisorBeanNameList = aiClientVO.getAdvisorBeanNameList();
            if (advisorBeanNameList != null && !advisorBeanNameList.isEmpty()) {
                for (String advisorBeanName : advisorBeanNameList) {
                    try {
                        Object advisor = getBean(advisorBeanName);
                        advisors.add(advisor);
                        log.info("成功获取顾问Bean: {}", advisorBeanName);
                    } catch (Exception e) {
                        log.warn("获取顾问Bean失败: {}, 错误: {}", advisorBeanName, e.getMessage());
                    }
                }
            } else {
                log.info("客户端配置中未指定顾问角色: clientId={}", aiClientVO.getClientId());
            }

            // 5. 构建对话客户端
            Object chatClient = buildChatClient(aiClientVO, chatModel, mcpClients, advisors, defaultSystem.toString());
            
            // 注册Bean对象
            registerBean(beanName(aiClientVO.getClientId()), Object.class, chatClient);
            
            log.info("成功创建AI客户端: clientId={}, clientName={}, beanName={}, 组件统计[模型:{}, MCP:{}, 顾问:{}]", 
                    aiClientVO.getClientId(), 
                    aiClientVO.getClientName(), 
                    beanName(aiClientVO.getClientId()),
                    chatModel != null ? 1 : 0,
                    mcpClients.size(),
                    advisors.size());
        }

        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点（客户端构建是最后一个节点）
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        // 返回 null 表示流程结束
        return null;
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT.getDataName();
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 客户端构建是最后一个节点，直接返回成功
        return "SUCCESS";
    }

    /**
     * 构建ChatClient对象
     * 在真实环境中，这里会使用Spring AI的ChatClient.builder()构建
     * 目前简化实现，返回一个包含所有组件的配置对象
     */
    private Object buildChatClient(AiClientVO clientConfig, Object chatModel, List<Object> mcpClients, 
                                  List<Object> advisors, String systemPrompt) {
        
        // 创建一个简化的ChatClient配置对象
        ChatClientConfig chatClientConfig = new ChatClientConfig();
        chatClientConfig.setClientConfig(clientConfig);
        chatClientConfig.setChatModel(chatModel);
        chatClientConfig.setMcpClients(mcpClients);
        chatClientConfig.setAdvisors(advisors);
        chatClientConfig.setSystemPrompt(systemPrompt);
        
        log.info("构建ChatClient配置完成: clientId={}, 系统提示词长度={}, 组件数量[模型:{}, MCP:{}, 顾问:{}]",
                clientConfig.getClientId(),
                systemPrompt.length(),
                chatModel != null ? 1 : 0,
                mcpClients.size(),
                advisors.size());
        
        return chatClientConfig;
    }

    /**
     * 简化的ChatClient配置类，用于包装所有组件
     * 在真实环境中，会返回Spring AI的ChatClient对象
     */
    public static class ChatClientConfig {
        private AiClientVO clientConfig;
        private Object chatModel;
        private List<Object> mcpClients;
        private List<Object> advisors;
        private String systemPrompt;

        // Getters and Setters
        public AiClientVO getClientConfig() { return clientConfig; }
        public void setClientConfig(AiClientVO clientConfig) { this.clientConfig = clientConfig; }
        
        public Object getChatModel() { return chatModel; }
        public void setChatModel(Object chatModel) { this.chatModel = chatModel; }
        
        public List<Object> getMcpClients() { return mcpClients; }
        public void setMcpClients(List<Object> mcpClients) { this.mcpClients = mcpClients; }
        
        public List<Object> getAdvisors() { return advisors; }
        public void setAdvisors(List<Object> advisors) { this.advisors = advisors; }
        
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    }
} 