package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientSystemPromptVO;
import site.kuril.domain.agent.model.valobj.AiClientVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

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

            // 2. 对话模型
            // TODO: 获取对话模型 - 暂时简化实现
            Object chatModel = null;
            String modelBeanName = aiClientVO.getModelBeanName();
            if (modelBeanName != null) {
                try {
                    chatModel = getBean(modelBeanName);
                    log.info("成功获取模型Bean: {}", modelBeanName);
                } catch (Exception e) {
                    log.warn("获取模型Bean失败: {}", e.getMessage());
                }
            }

            // 3. MCP 服务
            // TODO: 获取MCP服务列表 - 暂时简化实现
            List<String> mcpBeanNameList = aiClientVO.getMcpBeanNameList();
            if (mcpBeanNameList != null) {
                for (String mcpBeanName : mcpBeanNameList) {
                    try {
                        Object mcpClient = getBean(mcpBeanName);
                        log.info("成功获取MCP Bean: {}", mcpBeanName);
                    } catch (Exception e) {
                        log.warn("获取MCP Bean失败: {}", e.getMessage());
                    }
                }
            }

            // 4. advisor 顾问角色
            // TODO: 获取顾问角色列表 - 暂时简化实现
            List<String> advisorBeanNameList = aiClientVO.getAdvisorBeanNameList();
            if (advisorBeanNameList != null) {
                for (String advisorBeanName : advisorBeanNameList) {
                    try {
                        Object advisor = getBean(advisorBeanName);
                        log.info("成功获取顾问Bean: {}", advisorBeanName);
                    } catch (Exception e) {
                        log.warn("获取顾问Bean失败: {}", e.getMessage());
                    }
                }
            }

            // 5. 构建对话客户端
            // TODO: 实际构建ChatClient - 暂时简化实现，只注册配置对象
            Object chatClient = aiClientVO; // 暂时用配置对象代替
            
            // 注册Bean对象
            registerBean(beanName(aiClientVO.getClientId()), Object.class, chatClient);
            
            log.info("成功创建AI客户端: clientId={}, clientName={}, beanName={}", 
                    aiClientVO.getClientId(), 
                    aiClientVO.getClientName(), 
                    beanName(aiClientVO.getClientId()));
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

} 