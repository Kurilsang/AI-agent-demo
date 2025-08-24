package site.kuril.api;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import site.kuril.domain.agent.model.entity.ExecuteCommandEntity;

/**
 * AI Agent 服务接口
 * 提供AutoAgent自动对话功能，支持SSE流式响应
 * 
 * @author Kuril
 */
public interface IAiAgentService {

    /**
     * AutoAgent 自动对话接口
     * 支持SSE流式响应，实时返回AI的思考过程和执行结果
     * 
     * @param executeCommandEntity 执行命令实体，包含用户输入、会话ID、最大步数等
     * @return ResponseBodyEmitter SSE流式响应发射器，用于实时推送执行过程数据
     */
    ResponseBodyEmitter autoAgent(ExecuteCommandEntity executeCommandEntity);

}
