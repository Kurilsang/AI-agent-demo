package site.kuril.domain.agent.service.armory.business.data;

import site.kuril.domain.agent.model.entity.ArmoryCommandEntity;

/**
 * 数据加载策略接口
 * 用于定义不同类型的数据加载策略
 */
public interface ILoadDataStrategy {

    /**
     * 加载数据
     * @param armoryCommandEntity 装备命令实体
     * @param dynamicContext 动态上下文
     */
    void loadData(ArmoryCommandEntity armoryCommandEntity, Object dynamicContext);

} 