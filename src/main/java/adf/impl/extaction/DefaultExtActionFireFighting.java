package adf.impl.extaction;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.action.fire.ActionExtinguish;
import adf.core.agent.action.fire.ActionRefill;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.*;

import static rescuecore2.standard.entities.StandardEntityURN.HYDRANT;
import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;

/**
 * 扩展动作:战斗
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultExtActionFireFighting extends ExtAction {

    /**
     * 路径规划算法
     */
    private PathPlanning pathPlanning;

    /**
     * 最大灭火距离
     */
    private int maxExtinguishDistance;
    /**
     * 最大灭火功率
     */
    private int maxExtinguishPower;
    /**
     * 表示判断目标agent是否需要休息的阈值
     */
    private int thresholdRest;
    /**
     * 内核时间
     */
    private int kernelTime;
    private int refillCompleted;
    /**
     * 补充请求
     */
    private int refillRequest;
    /**
     * 补充旗帜
     */
    private boolean refillFlag;
    /**
     * 动作目标
     */
    private EntityID target;
    /**
     * {@link DefaultExtActionFireFighting}的构造函数
     *
     * @param agentInfo     代理信息
     * @param worldInfo     世界信息
     * @param scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public DefaultExtActionFireFighting(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, DevelopData developData) {
        super(agentInfo, worldInfo, scenarioInfo, moduleManager, developData);
        this.maxExtinguishDistance = scenarioInfo.getFireExtinguishMaxDistance();
        this.maxExtinguishPower = scenarioInfo.getFireExtinguishMaxSum();
        this.thresholdRest = developData.getInteger(
                "adf.impl.extaction.DefaultExtActionFireFighting.rest", 100);
        int maxWater = scenarioInfo.getFireTankMaximum();
        this.refillCompleted = (maxWater / 10) * developData.getInteger(
                "adf.impl.extaction.DefaultExtActionFireFighting.refill.completed", 10);
        this.refillRequest = this.maxExtinguishPower * developData.getInteger(
                "adf.impl.extaction.DefaultExtActionFireFighting.refill.request", 1);
        this.refillFlag = false;

        this.target = null;

        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.pathPlanning = moduleManager.getModule(
                        "DefaultExtActionFireFighting.PathPlanning",
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
            if (entity instanceof Building) {
                this.target = target;
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

        this.refillFlag = this.needRefill(agent, this.refillFlag);
        if (this.refillFlag) {
            this.result = this.calcRefill(agent, this.pathPlanning, this.target);
            if (this.result != null) {
                return this;
            }
        }

        if (this.needRest(agent)) {
            this.result = this.calcRefugeAction(agent, this.pathPlanning, this.target,
                    false);
            if (this.result != null) {
                return this;
            }
        }

        if (this.target == null) {
            return this;
        }
        this.result = this.calcExtinguish(agent, this.pathPlanning, this.target);
        return this;
    }


    private Action calcExtinguish(FireBrigade agent, PathPlanning pathPlanning,
                                  EntityID target) {
        EntityID agentPosition = agent.getPosition();
        StandardEntity positionEntity = Objects
                .requireNonNull(this.worldInfo.getPosition(agent));
        if (StandardEntityURN.REFUGE == positionEntity.getStandardURN()) {
            Action action = this.getMoveAction(pathPlanning, agentPosition, target);
            if (action != null) {
                return action;
            }
        }

        List<StandardEntity> neighbourBuilding = new ArrayList<>();
        StandardEntity entity = this.worldInfo.getEntity(target);
        if (entity instanceof Building) {
            if (this.worldInfo.getDistance(positionEntity,
                    entity) < this.maxExtinguishDistance) {
                neighbourBuilding.add(entity);
            }
        }

        if (neighbourBuilding.size() > 0) {
            neighbourBuilding.sort(new DistanceSorter(this.worldInfo, agent));
            return new ActionExtinguish(neighbourBuilding.get(0).getID(),
                    this.maxExtinguishPower);
        }
        return this.getMoveAction(pathPlanning, agentPosition, target);
    }


    private Action getMoveAction(PathPlanning pathPlanning, EntityID from,
                                 EntityID target) {
        pathPlanning.setFrom(from);
        pathPlanning.setDestination(target);
        List<EntityID> path = pathPlanning.calc().getResult();
        if (path != null && path.size() > 0) {
            StandardEntity entity = this.worldInfo
                    .getEntity(path.get(path.size() - 1));
            if (entity instanceof Building) {
                if (entity.getStandardURN() != StandardEntityURN.REFUGE) {
                    path.remove(path.size() - 1);
                }
            }
            return new ActionMove(path);
        }
        return null;
    }


    private boolean needRefill(FireBrigade agent, boolean refillFlag) {
        if (refillFlag) {
            StandardEntityURN positionURN = Objects
                    .requireNonNull(this.worldInfo.getPosition(agent)).getStandardURN();
            return !(positionURN == REFUGE || positionURN == HYDRANT)
                    || agent.getWater() < this.refillCompleted;
        }
        return agent.getWater() <= this.refillRequest;
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


    private Action calcRefill(FireBrigade agent, PathPlanning pathPlanning,
                              EntityID target) {
        StandardEntityURN positionURN = Objects
                .requireNonNull(this.worldInfo.getPosition(agent)).getStandardURN();
        if (positionURN == REFUGE) {
            return new ActionRefill();
        }
        Action action = this.calcRefugeAction(agent, pathPlanning, target, true);
        if (action != null) {
            return action;
        }
        action = this.calcHydrantAction(agent, pathPlanning, target);
        if (action != null) {
            if (positionURN == HYDRANT
                    && action.getClass().equals(ActionMove.class)) {
                pathPlanning.setFrom(agent.getPosition());
                pathPlanning.setDestination(target);
                double currentDistance = pathPlanning.calc().getDistance();
                List<EntityID> path = ((ActionMove) action).getPath();
                pathPlanning.setFrom(path.get(path.size() - 1));
                pathPlanning.setDestination(target);
                double newHydrantDistance = pathPlanning.calc().getDistance();
                if (currentDistance <= newHydrantDistance) {
                    return new ActionRefill();
                }
            }
            return action;
        }
        return null;
    }


    private Action calcRefugeAction(Human human, PathPlanning pathPlanning,
                                    EntityID target, boolean isRefill) {
        return this.calcSupplyAction(human, pathPlanning,
                this.worldInfo.getEntityIDsOfType(StandardEntityURN.REFUGE), target,
                isRefill);
    }


    private Action calcHydrantAction(Human human, PathPlanning pathPlanning,
                                     EntityID target) {
        Collection<EntityID> hydrants = this.worldInfo.getEntityIDsOfType(HYDRANT);
        hydrants.remove(human.getPosition());
        return this.calcSupplyAction(human, pathPlanning, hydrants, target, true);
    }


    private Action calcSupplyAction(Human human, PathPlanning pathPlanning,
                                    Collection<EntityID> supplyPositions, EntityID target, boolean isRefill) {
        EntityID position = human.getPosition();
        int size = supplyPositions.size();
        if (supplyPositions.contains(position)) {
            return isRefill ? new ActionRefill() : new ActionRest();
        }
        List<EntityID> firstResult = null;
        while (supplyPositions.size() > 0) {
            pathPlanning.setFrom(position);
            pathPlanning.setDestination(supplyPositions);
            List<EntityID> path = pathPlanning.calc().getResult();
            if (path != null && path.size() > 0) {
                if (firstResult == null) {
                    firstResult = new ArrayList<>(path);
                    if (target == null) {
                        break;
                    }
                }
                EntityID supplyPositionID = path.get(path.size() - 1);
                pathPlanning.setFrom(supplyPositionID);
                pathPlanning.setDestination(target);
                List<EntityID> fromRefugeToTarget = pathPlanning.calc().getResult();
                if (fromRefugeToTarget != null && fromRefugeToTarget.size() > 0) {
                    return new ActionMove(path);
                }
                supplyPositions.remove(supplyPositionID);
                // remove failed
                if (size == supplyPositions.size()) {
                    break;
                }
                size = supplyPositions.size();
            } else {
                break;
            }
        }
        return firstResult != null ? new ActionMove(firstResult) : null;
    }

    private class DistanceSorter implements Comparator<StandardEntity> {

        private StandardEntity reference;
        private WorldInfo worldInfo;

        DistanceSorter(WorldInfo wi, StandardEntity reference) {
            this.reference = reference;
            this.worldInfo = wi;
        }


        public int compare(StandardEntity a, StandardEntity b) {
            int d1 = this.worldInfo.getDistance(this.reference, a);
            int d2 = this.worldInfo.getDistance(this.reference, b);
            return d1 - d2;
        }
    }
}