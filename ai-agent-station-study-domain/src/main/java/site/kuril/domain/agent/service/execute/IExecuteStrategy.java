package site.kuril.domain.agent.service.execute;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;

/**
 * 执行策略接口
 * 定义AutoAgent执行策略的基本契约，支持SSE流式响应
 * 
 * @author Kuril
 */
public interface IExecuteStrategy {

    /**
     * 执行策略方法
     * @param requestParameter 执行请求参数
     * @param emitter SSE流式响应发射器，用于实时发送执行过程数据
     * @throws Exception 执行过程中可能抛出的异常
     */
    void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception;

}
