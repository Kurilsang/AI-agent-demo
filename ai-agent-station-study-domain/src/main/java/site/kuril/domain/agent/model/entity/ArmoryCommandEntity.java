package site.kuril.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.kuril.domain.agent.model.valobj.AiAgentEnumVO;

import java.util.List;

/**
 * 装备命令实体
 * 用于请求加载数据策略
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryCommandEntity {

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 命令索引（clientId、modelId、apiId...）
     */
    private List<String> commandIdList;

    /**
     * 获取数据加载策略
     * @return 数据加载策略名称
     */
    public String getLoadDataStrategy() {
        AiAgentEnumVO aiAgentEnumVO = AiAgentEnumVO.getByCode(commandType);
        return aiAgentEnumVO.getLoadDataStrategy();
    }

} 