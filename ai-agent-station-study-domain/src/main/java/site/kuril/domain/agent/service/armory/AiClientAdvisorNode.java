package site.kuril.domain.agent.service.armory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientAdvisorTypeEnumVO;
import site.kuril.domain.agent.model.valobj.AiClientAdvisorVO;
import site.kuril.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.List;

/**
 * AI客户端顾问节点
 * 用于构建和注册顾问角色对象到Spring容器
 */
@Slf4j
@Service
public class AiClientAdvisorNode extends AbstractArmorySupport {

    @Override
    protected String doApply(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        log.info("Ai Agent 构建节点，Advisor 顾问角色{}", JSON.toJSONString(requestParameter));

        DefaultArmoryStrategyFactory.DynamicContext context = (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext;
        List<AiClientAdvisorVO> aiClientAdvisorList = context.getValue(dataName());

        if (aiClientAdvisorList == null || aiClientAdvisorList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client advisor");
            return "SUCCESS";
        }

        for (AiClientAdvisorVO aiClientAdvisorVO : aiClientAdvisorList) {
            // 构建顾问访问对象
            Object advisor = createAdvisor(aiClientAdvisorVO);
            
            // 注册Bean对象（暂时注册为Object类型，后续可以改为具体的Advisor类型）
            registerBean(beanName(aiClientAdvisorVO.getAdvisorId()), Object.class, advisor);
            
            log.info("成功创建顾问角色: advisorId={}, advisorType={}, beanName={}", 
                    aiClientAdvisorVO.getAdvisorId(), 
                    aiClientAdvisorVO.getAdvisorType(), 
                    beanName(aiClientAdvisorVO.getAdvisorId()));
        }

        return "SUCCESS";
    }

    /**
     * 获取下一个处理节点
     */
    public DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(
            ArmoryCommandEntity requestParameter, 
            DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        
        try {
            // 通过ApplicationContext获取下一个节点，避免循环依赖
            AiClientNode aiClientNode = applicationContext.getBean(AiClientNode.class);
            log.info("✅ 成功获取 AiClientNode: {}", aiClientNode.getClass().getSimpleName());
            
            return new DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>() {
                @Override
                public String apply(ArmoryCommandEntity entity, DefaultArmoryStrategyFactory.DynamicContext context) throws Exception {
                    return aiClientNode.process(entity, context);
                }
            };
        } catch (Exception e) {
            log.error("⚠️ 获取AiClientNode失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    protected String beanName(String beanId) {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(beanId);
    }

    @Override
    protected String dataName() {
        return AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName();
    }

    @Override
    protected String router(ArmoryCommandEntity requestParameter, Object dynamicContext) throws Exception {
        // 路由到下一个节点
        DefaultArmoryStrategyFactory.StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> nextHandler = 
                get(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        
        if (nextHandler != null) {
            return nextHandler.apply(requestParameter, (DefaultArmoryStrategyFactory.DynamicContext) dynamicContext);
        }
        
        return "SUCCESS";
    }

    /**
     * 创建顾问对象
     */
    private Object createAdvisor(AiClientAdvisorVO aiClientAdvisorVO) {
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        AiClientAdvisorTypeEnumVO advisorTypeEnum = AiClientAdvisorTypeEnumVO.getByCode(advisorType);
        
        if (advisorTypeEnum == null) {
            log.warn("未知的顾问类型: {}", advisorType);
            return aiClientAdvisorVO; // 返回配置对象作为备选
        }
        
        // 尝试获取VectorStore，如果没有则传入null
        Object vectorStore = null;
        try {
            vectorStore = getBean("vectorStore");
            log.info("成功获取VectorStore Bean用于创建顾问: {}", advisorType);
        } catch (Exception e) {
            log.warn("无法获取VectorStore Bean，将使用null创建顾问: {}", e.getMessage());
        }
        
        // 使用枚举策略创建顾问对象
        Object advisor = advisorTypeEnum.createAdvisor(aiClientAdvisorVO, vectorStore);
        
        log.info("成功创建顾问对象: advisorId={}, advisorType={}, advisorClass={}", 
                aiClientAdvisorVO.getAdvisorId(), 
                advisorType, 
                advisor.getClass().getSimpleName());
        
        return advisor;
    }

} 