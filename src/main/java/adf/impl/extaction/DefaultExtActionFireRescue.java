package adf.impl.extaction;

import adf.core.agent.action.Action;
import adf.core.agent.action.ambulance.ActionRescue;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import adf.impl.centralized.DefaultCommandPickerPolice;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.List;

import static rescuecore2.standard.entities.StandardEntityURN.BLOCKADE;

/**
 * 扩展动作:救援
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultExtActionFireRescue extends ExtAction {
    /**
     * 路径规划算法
     */
    private PathPlanning pathPlanning;
    /**
     * 表示判断目标agent是否需要休息的阈值
     */
    private int thresholdRest;

    /**
     * 内核时间
     */
    private int kernelTime;
    /**
     * 动作目标
     */
    private EntityID target;

    /**
     * {@link DefaultExtActionFireRescue}的构造函数
     *
     * @param agentInfo     代理信息
     * @param worldInfo     世界信息
     * @param scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public DefaultExtActionFireRescue(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, DevelopData developData) {
        super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
        this.target = null;
        this.thresholdRest = developData
                .getInteger("adf.impl.extaction.DefaultExtActionFireRescue.rest", 100);

        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.pathPlanning = moduleManager.getModule(
                        "DefaultExtActionFireRescue.PathPlanning",
                        "adf.impl.module.algorithm.DijkstraPathPlanning");
                break;
        }
    }

    /**
     * 预计算的方法
     * <p>
     * 执行路径规划模块的预计算并获取内核时间
     *
     * @param precomputeData 预计算的数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public ExtAction precompute(PrecomputeData precomputeData) {
        super.precompute(precomputeData);
        if (this.getCountPrecompute() >= 2) {
            return this;
        }
        this.pathPlanning.precompute(precomputeData);
        try {
            this.kernelTime = this.scenarioInfo.getKernelTimesteps();
        } catch (NoSuchConfigOptionException e) {
            this.kernelTime = -1;
        }
        return this;
    }

    /**
     * 预计算模式的初始化处理方法
     * <p>
     * 执行路径规划模块的预计算并获取内核时间
     *
     * @param precomputeData 预计算的数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public ExtAction resume(PrecomputeData precomputeData) {
        super.resume(precomputeData);
        if (this.getCountResume() >= 2) {
            return this;
        }
        this.pathPlanning.resume(precomputeData);
        try {
            this.kernelTime = this.scenarioInfo.getKernelTimesteps();
        } catch (NoSuchConfigOptionException e) {
            this.kernelTime = -1;
        }
        return this;
    }

    /**
     * 无预计算模式的初始化处理方法
     * <p>
     * 执行路径规划模块的预计算并获取内核时间
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public ExtAction preparate() {
        super.preparate();
        if (this.getCountPreparate() >= 2) {
            return this;
        }
        this.pathPlanning.preparate();
        try {
            this.kernelTime = this.scenarioInfo.getKernelTimesteps();
        } catch (NoSuchConfigOptionException e) {
            this.kernelTime = -1;
        }
        return this;
    }

    /**
     * 一种更新agent内部信息的方法，每一回合都执行
     *
     * @param messageManager 消息管理器
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public ExtAction updateInfo(MessageManager messageManager) {
        super.updateInfo(messageManager);
        if (this.getCountUpdateInfo() >= 2) {
            return this;
        }
        this.pathPlanning.updateInfo(messageManager);
        return this;
    }

    /**
     * 设置目标({@link #target})
     *
     * @param target 表示操作目标的实体ID
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public ExtAction setTarget(EntityID target) {
        this.target = null;
        if (target != null) {
            StandardEntity entity = this.worldInfo.getEntity(target);
            if (entity instanceof Human || entity instanceof Area) {
                this.target = target;
                return this;
            }
        }
        return this;
    }

    /**
     * 计算Agent在每个回合中应采取的动作并将其写入{@link #result}
     * <ol>
     *     <li>如果消防队Agent需要休息则留在原地
     *     <li>如果不需要休息且有目标，则获取目标对应的Action写入{@link #result}
     *     <li>否则返回null
     * </ol>
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public ExtAction calc() {
        this.result = null;
        FireBrigade agent = (FireBrigade) this.agentInfo.me();

        if (this.needRest(agent)) {
            EntityID areaID = this.convertArea(this.target);
            ArrayList<EntityID> targets = new ArrayList<>();
            if (areaID != null) {
                targets.add(areaID);
            }
        }
        if (this.target != null) {
            this.result = this.calcRescue(agent, this.pathPlanning, this.target);
        }
        return this;
    }

    /**
     * 计算救援，返回一个对应于动作目标的动作
     * <p>
     * <ol>
     *     <li>如果目标是{@link Human}并且与消防队({@link FireBrigade})位于同一位置，则返回{@link ActionRescue};如果目标位于不同位置且可到达，则返回{@link ActionMove}
     *     <li>如果目标是{@link Blockade}并且可到达，则返回{@link ActionMove}
     *     <li>否则返回 null
     * </ol>
     *
     * @param agent        执行动作的消防队
     * @param pathPlanning 路径规划
     * @param targetID     当前目标的EntityID
     * @return 应该执行的动作
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private Action calcRescue(FireBrigade agent, PathPlanning pathPlanning, EntityID targetID) {
        StandardEntity targetEntity = this.worldInfo.getEntity(targetID);
        if (targetEntity == null) {
            return null;
        }
        EntityID agentPosition = agent.getPosition();
        if (targetEntity instanceof Human) {
            Human human = (Human) targetEntity;
            if (!human.isPositionDefined()) {
                return null;
            }
            if (human.isHPDefined() && human.getHP() == 0) {
                return null;
            }
            EntityID targetPosition = human.getPosition();
            if (agentPosition.getValue() == targetPosition.getValue()) {
                if (human.isBuriednessDefined() && human.getBuriedness() > 0) {
                    return new ActionRescue(human);
                }
            } else {
                List<EntityID> path = pathPlanning.getResult(agentPosition,
                        targetPosition);
                if (path != null && path.size() > 0) {
                    return new ActionMove(path);
                }
            }
            return null;
        }
        if (targetEntity.getStandardURN() == BLOCKADE) {
            Blockade blockade = (Blockade) targetEntity;
            if (blockade.isPositionDefined()) {
                targetEntity = this.worldInfo.getEntity(blockade.getPosition());
            }
        }
        if (targetEntity instanceof Area) {
            List<EntityID> path = pathPlanning.getResult(agentPosition,
                    targetEntity.getID());
            if (path != null && path.size() > 0) {
                return new ActionMove(path);
            }
        }
        return null;
    }

    /**
     * 判断消防员本人是否需要休息
     * <p>
     * 当目标受到等于或大于阈值的伤害时休息
     *
     * @param agent 代表消防队Agent本身的Human
     * @return 一个布尔值，指示对象是否需要休息
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private boolean needRest(Human agent) {
        int hp = agent.getHP();
        int damage = agent.getDamage();
        if (hp == 0 || damage == 0) {
            return false;
        }
        int activeTime = (hp / damage) + ((hp % damage) != 0 ? 1 : 0);
        if (this.kernelTime == -1) {
            try {
                this.kernelTime = this.scenarioInfo.getKernelTimesteps();
            } catch (NoSuchConfigOptionException e) {
                this.kernelTime = -1;
            }
        }
        return damage >= this.thresholdRest
                || (activeTime + this.agentInfo.getTime()) < this.kernelTime;
    }

    /**
     * 转换为区域
     * <p>
     * 将EntityID转换为EntityID所指示的区域或EntityID所指示的Human所在区域的EntityID
     * <ol>
     *     <li>如果EntityID所指示的实体是区域({@link Area}),则返回该区域的EntityID
     *     <li>如果是{@link Human}或 {@link Blockade}，则返回目标所在区域的 EntityID
     *     <li>否则返回null
     * </ol>
     *
     * @param targetID 目标的EntityID
     * @return 目标区域的实体ID
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private EntityID convertArea(EntityID targetID) {
        StandardEntity entity = this.worldInfo.getEntity(targetID);
        if (entity == null) {
            return null;
        }
        if (entity instanceof Human) {
            Human human = (Human) entity;
            if (human.isPositionDefined()) {
                EntityID position = human.getPosition();
                if (this.worldInfo.getEntity(position) instanceof Area) {
                    return position;
                }
            }
        } else if (entity instanceof Area) {
            return targetID;
        } else if (entity.getStandardURN() == BLOCKADE) {
            Blockade blockade = (Blockade) entity;
            if (blockade.isPositionDefined()) {
                return blockade.getPosition();
            }
        }
        return null;
    }
}