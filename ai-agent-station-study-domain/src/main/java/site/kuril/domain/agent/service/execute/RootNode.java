package site.kuril.domain.agent.service.execute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AutoAgent执行根节点
 * 负责初始化执行环境和加载必要的配置数据
 */
@Slf4j
@Service("executeRootNode")
public class RootNode extends AbstractExecuteSupport {

    @Resource
    private Step1AnalyzerNode step1AnalyzerNode;

    @Override
    protected String doApply(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("=== 🚀 AutoAgent动态多轮执行开始 ===");
        log.info("👤 用户输入: {}", requestParameter.getMessage());
        log.info("📊 最大执行步数: {}", requestParameter.getMaxStep());
        log.info("🆔 会话ID: {}", requestParameter.getSessionId());
        log.info("🤖 智能体ID: {}", requestParameter.getAiAgentId());

        // 查询AI智能体客户端流程配置
        Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap = 
                repository.queryAiAgentClientFlowConfig(requestParameter.getAiAgentId());

        if (aiAgentClientFlowConfigVOMap.isEmpty()) {
            log.error("❌ 未找到智能体流程配置, aiAgentId: {}", requestParameter.getAiAgentId());
            throw new RuntimeException("未找到智能体流程配置");
        }

        log.info("📋 加载到的客户端配置: {}", aiAgentClientFlowConfigVOMap.keySet());

        // 初始化动态上下文
        dynamicContext.setAiAgentClientFlowConfigVOMap(aiAgentClientFlowConfigVOMap);
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setCurrentTask(requestParameter.getMessage());
        dynamicContext.setMaxStep(requestParameter.getMaxStep());
        dynamicContext.setStep(1);
        dynamicContext.setCompleted(false);

        log.info("✅ 执行环境初始化完成，准备进入任务分析阶段");
        
        return "ROOT_NODE_COMPLETED";
    }

    @Override
    public DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> 
            get(ExecuteCommandEntity requestParameter, DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 始终路由到任务分析节点
        return step1AnalyzerNode;
    }

}
