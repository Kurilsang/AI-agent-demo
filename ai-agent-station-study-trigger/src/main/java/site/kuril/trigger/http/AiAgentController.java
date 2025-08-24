package site.kuril.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.kuril.api.IAiAgentService;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;
import site.kuril.domain.agent.service.execute.factory.DefaultAutoAgentExecuteStrategyFactory;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * AI Agent 控制器
 * 提供AutoAgent SSE流式响应接口
 * 
 * @author Kuril
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiAgentController implements IAiAgentService {

    @Resource
    private DefaultAutoAgentExecuteStrategyFactory defaultAutoAgentExecuteStrategyFactory;

    /**
     * AutoAgent自动对话接口
     * 支持SSE流式响应，实时返回AI的思考过程和执行结果
     * 
     * @param executeCommandEntity 执行命令实体
     * @return ResponseBodyEmitter SSE流式响应
     */
    @Override
    @PostMapping(value = "/auto_agent", produces = "text/event-stream;charset=UTF-8")
    public ResponseBodyEmitter autoAgent(@RequestBody ExecuteCommandEntity executeCommandEntity) {
        log.info("收到AutoAgent请求: sessionId={}, message={}, maxStep={}", 
                executeCommandEntity.getSessionId(), 
                executeCommandEntity.getMessage(), 
                executeCommandEntity.getMaxStep());

        // 创建SSE响应发射器
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);

        // 设置SSE响应头
        emitter.onCompletion(() -> log.info("SSE连接完成: sessionId={}", executeCommandEntity.getSessionId()));
        emitter.onTimeout(() -> log.warn("SSE连接超时: sessionId={}", executeCommandEntity.getSessionId()));
        emitter.onError((throwable) -> log.error("SSE连接错误: sessionId={}, error={}", 
                executeCommandEntity.getSessionId(), throwable.getMessage(), throwable));

        // 异步执行AutoAgent任务
        CompletableFuture.runAsync(() -> {
            try {
                // 创建执行策略处理器
                DefaultAutoAgentExecuteStrategyFactory.StrategyHandler<ExecuteCommandEntity, DefaultAutoAgentExecuteStrategyFactory.DynamicContext, String> executeHandler
                        = defaultAutoAgentExecuteStrategyFactory.armoryStrategyHandler();

                // 创建动态上下文并注入emitter
                DefaultAutoAgentExecuteStrategyFactory.DynamicContext dynamicContext = 
                        new DefaultAutoAgentExecuteStrategyFactory.DynamicContext();
                dynamicContext.setValue("emitter", emitter);

                // 执行AutoAgent任务
                String result = executeHandler.apply(executeCommandEntity, dynamicContext);
                
                log.info("AutoAgent执行完成: sessionId={}, result={}", 
                        executeCommandEntity.getSessionId(), result);

                // 发送完成信号
                emitter.send("data: {\"type\":\"complete\",\"completed\":true,\"timestamp\":" + 
                        System.currentTimeMillis() + ",\"sessionId\":\"" + 
                        executeCommandEntity.getSessionId() + "\"}\n\n");
                
                // 完成响应
                emitter.complete();

            } catch (Exception e) {
                log.error("AutoAgent执行异常: sessionId={}, error={}", 
                        executeCommandEntity.getSessionId(), e.getMessage(), e);
                
                try {
                    // 发送错误信息
                    emitter.send("data: {\"type\":\"error\",\"content\":\"" + 
                            e.getMessage().replace("\"", "\\\"") + 
                            "\",\"timestamp\":" + System.currentTimeMillis() + 
                            ",\"sessionId\":\"" + executeCommandEntity.getSessionId() + 
                            "\"}\n\n");
                    emitter.complete();
                } catch (Exception sendError) {
                    log.error("发送错误信息失败: {}", sendError.getMessage());
                    emitter.completeWithError(sendError);
                }
            }
        });

        return emitter;
    }
}
