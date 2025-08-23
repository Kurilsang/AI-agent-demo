package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
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
            ChatClient chatClient = buildChatClient(aiClientVO, chatModel, mcpClients, advisors, defaultSystem.toString());
            
            // 注册Bean对象
            registerBean(beanName(aiClientVO.getClientId()), ChatClient.class, chatClient);
            
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
     * 使用Spring AI的ChatClient.builder()构建真正的ChatClient实例
     */
    private ChatClient buildChatClient(AiClientVO clientConfig, Object chatModel, List<Object> mcpClients, 
                                      List<Object> advisors, String systemPrompt) {
        
        log.info("开始构建ChatClient: clientId={}, 系统提示词长度={}, 组件数量[模型:{}, MCP:{}, 顾问:{}]",
                clientConfig.getClientId(),
                systemPrompt.length(),
                chatModel != null ? 1 : 0,
                mcpClients.size(),
                advisors.size());

        // 1. 如果没有ChatModel，抛出异常
        if (chatModel == null) {
            log.error("未找到ChatModel，无法创建ChatClient: clientId={}", clientConfig.getClientId());
            throw new IllegalStateException("ChatModel is required to create ChatClient for clientId: " + clientConfig.getClientId());
        }

        // 2. 确保chatModel是ChatModel类型
        if (!(chatModel instanceof ChatModel)) {
            log.error("ChatModel类型不正确: {}, 期望: ChatModel", chatModel.getClass().getName());
            throw new IllegalArgumentException("ChatModel type is incorrect: " + chatModel.getClass().getName());
        }

        ChatModel model = (ChatModel) chatModel;

        // 3. 构建ChatClient.Builder
        ChatClient.Builder builder = ChatClient.builder(model)
                .defaultSystem(systemPrompt);

        // 4. 添加顾问角色
        if (advisors != null && !advisors.isEmpty()) {
            List<Advisor> advisorList = new ArrayList<>();
            for (Object advisor : advisors) {
                if (advisor instanceof Advisor) {
                    advisorList.add((Advisor) advisor);
                    log.info("添加顾问角色到ChatClient: {}", advisor.getClass().getSimpleName());
                } else {
                    log.warn("顾问角色类型不正确，跳过: {}", advisor.getClass().getName());
                }
            }
            
            if (!advisorList.isEmpty()) {
                builder.defaultAdvisors(advisorList.toArray(new Advisor[0]));
                log.info("成功添加 {} 个顾问角色到ChatClient", advisorList.size());
            }
        }

        // 5. MCP工具集成（在模型层面已经处理，这里记录日志）
        if (mcpClients != null && !mcpClients.isEmpty()) {
            log.info("MCP工具已在模型层面集成: {} 个工具", mcpClients.size());
        }

        // 6. 构建ChatClient
        ChatClient chatClient = builder.build();
        
        log.info("ChatClient构建完成: clientId={}, clientName={}", 
                clientConfig.getClientId(), 
                clientConfig.getClientName());
        
        return chatClient;
    }
} 