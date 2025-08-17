package site.kuril.domain.agent.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 装备命令实体
 * 用于请求加载数据策略
 */
@Data
public class ArmoryCommandEntity {

    /**
     * 命令类型
     */
    private String commandType;

    /**
     * 命令索引（clientId、modelId、apiId...）
     */
    private List<String> commandIdList;

} 