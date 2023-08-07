package adf.impl.centralized;

import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandAmbulance;
import adf.core.agent.communication.standard.bundle.centralized.MessageReport;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static rescuecore2.standard.entities.StandardEntityURN.*;

/**
 * 救护队的默认命令执行器
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultCommandExecutorAmbulance
        extends CommandExecutor<CommandAmbulance> {
    /**
     * 未知动作
     */
    private static final int ACTION_UNKNOWN = -1;

    /**
     * 动作:休息
     */
    private static final int ACTION_REST = CommandAmbulance.ACTION_REST;

    /**
     * 动作:移动
     */
    private static final int ACTION_MOVE = CommandAmbulance.ACTION_MOVE;


    /**
     * 动作：救援
     */
    private static final int ACTION_RESCUE = CommandAmbulance.ACTION_RESCUE;


    /**
     * 动作:装载
     */
    private static final int ACTION_LOAD = CommandAmbulance.ACTION_LOAD;

    /**
     * 动作:卸载
     */
    private static final int ACTION_UNLOAD = CommandAmbulance.ACTION_UNLOAD;

    /**
     * 动作:自主行动
     */
    private static final int ACTION_AUTONOMY = CommandAmbulance.ACTION_AUTONOMY;

    /**
     * 路径规划算法
     */
    private PathPlanning pathPlanning;

    /**
     * 复合动作:运输
     */
    private ExtAction actionTransport;

    /**
     * 复合动作:移动
     */
    private ExtAction actionExtMove;

    /**
     * 命令类型
     */
    private int commandType;

    /**
     * 命令目标的EntityID(实体id)
     */
    private EntityID target;

    /**
     * 命令执行者的EntityID(实体id)
     */
    private EntityID commanderID;

    /**
     * {@link DefaultCommandExecutorAmbulance}的构造函数
     *
     * @param ai(agentInfo)    代理信息
     * @param wi(worldInfo)    世界信息
     * @param si(scenarioInfo) 场景信息
     * @param moduleManager    模块管理器
     * @param developData      开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public DefaultCommandExecutorAmbulance(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
        super(ai, wi, si, moduleManager, developData);
        this.commandType = ACTION_UNKNOWN;
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.pathPlanning = moduleManager.getModule(
                        "DefaultCommandExecutorAmbulance.PathPlanning",
                        "adf.impl.module.algorithm.DijkstraPathPlanning");
                this.actionTransport = moduleManager.getExtAction(
                        "DefaultCommandExecutorAmbulance.ExtActionTransport",
                        "adf.impl.extaction.DefaultExtActionTransport");
                this.actionExtMove = moduleManager.getExtAction(
                        "DefaultCommandExecutorAmbulance.ExActionMove",
                        "adf.impl.extaction.DefaultExtActionMove");
                break;
        }
    }


    /**
     * 设置命令
     *
     * @param command 救护队命令
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor setCommand(CommandAmbulance command) {
        EntityID agentID = this.agentInfo.getID();
        if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                .getValue() == agentID.getValue()) {
            this.commandType = command.getAction();
            this.target = command.getTargetID();
            this.commanderID = command.getSenderID();
        }
        return this;
    }


    /**
     * 每个回合都会执行这个方法来更新agent所持有的信息
     *
     * @param messageManager 消息管理器
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor updateInfo(MessageManager messageManager) {
        super.updateInfo(messageManager);
        if (this.getCountUpdateInfo() >= 2) {
            return this;
        }
        this.pathPlanning.updateInfo(messageManager);
        this.actionTransport.updateInfo(messageManager);
        this.actionExtMove.updateInfo(messageManager);

        if (this.isCommandCompleted()) {
            if (this.commandType != ACTION_UNKNOWN) {
                messageManager
                        .addMessage(new MessageReport(true, true, false, this.commanderID));
                if (this.commandType == ACTION_LOAD) {
                    this.commandType = ACTION_UNLOAD;
                    this.target = null;
                } else {
                    this.commandType = ACTION_UNKNOWN;
                    this.target = null;
                    this.commanderID = null;
                }
            }
        }
        return this;
    }


    /**
     * 预计算时执行的方法
     *
     * @param precomputeData 预计算数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor precompute(PrecomputeData precomputeData) {
        super.precompute(precomputeData);
        if (this.getCountPrecompute() >= 2) {
            return this;
        }
        this.pathPlanning.precompute(precomputeData);
        this.actionTransport.precompute(precomputeData);
        this.actionExtMove.precompute(precomputeData);
        return this;
    }


    /**
     * 预计算模式的初始化处理方法
     *
     * @param precomputeData 预计算数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor resume(PrecomputeData precomputeData) {
        super.resume(precomputeData);
        if (this.getCountResume() >= 2) {
            return this;
        }
        this.pathPlanning.resume(precomputeData);
        this.actionTransport.resume(precomputeData);
        this.actionExtMove.resume(precomputeData);
        return this;
    }


    /**
     * 无预计算模式的初始化处理方法
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor preparate() {
        super.preparate();
        if (this.getCountPreparate() >= 2) {
            return this;
        }
        this.pathPlanning.preparate();
        this.actionTransport.preparate();
        this.actionExtMove.preparate();
        return this;
    }


    /**
     * 计算应该执行的动作,将结果保存在{@link #result}中
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor calc() {
        this.result = null;
        switch (this.commandType) {
            case ACTION_REST:
                EntityID position = this.agentInfo.getPosition();
                if (this.target == null) {
                    Collection<EntityID> refuges = this.worldInfo.getEntityIDsOfType(REFUGE);
                    if (refuges.contains(position)) {
                        this.result = new ActionRest();
                    } else {
                        this.pathPlanning.setFrom(position);
                        this.pathPlanning.setDestination(refuges);
                        List<EntityID> path = this.pathPlanning.calc().getResult();
                        if (path != null && path.size() > 0) {
                            this.result = new ActionMove(path);
                        } else {
                            this.result = new ActionRest();
                        }
                    }
                    return this;
                }
                if (position.getValue() != this.target.getValue()) {
                    List<EntityID> path = this.pathPlanning.getResult(position,
                            this.target);
                    if (path != null && path.size() > 0) {
                        this.result = new ActionMove(path);
                        return this;
                    }
                }
                this.result = new ActionRest();
                return this;
            case ACTION_MOVE:
                if (this.target != null) {
                    this.result = this.actionExtMove.setTarget(this.target).calc()
                            .getAction();
                }
                return this;
            case ACTION_RESCUE:
                if (this.target != null) {
                    this.result = this.actionTransport.setTarget(this.target).calc()
                            .getAction();
                }
                return this;
            case ACTION_LOAD:
                if (this.target != null) {
                    this.result = this.actionTransport.setTarget(this.target).calc()
                            .getAction();
                }
                return this;
            case ACTION_UNLOAD:
                if (this.target != null) {
                    this.result = this.actionTransport.setTarget(this.target).calc()
                            .getAction();
                }
                return this;
            case ACTION_AUTONOMY:
                if (this.target == null) {
                    return this;
                }
                StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
                if (targetEntity instanceof Area) {
                    if (this.agentInfo.someoneOnBoard() == null) {
                        this.result = this.actionExtMove.setTarget(this.target).calc()
                                .getAction();
                    } else {
                        this.result = this.actionTransport.setTarget(this.target).calc()
                                .getAction();
                    }
                } else if (targetEntity instanceof Human) {
                    this.result = this.actionTransport.setTarget(this.target).calc()
                            .getAction();
                }
        }
        return this;
    }


    /**
     * 判断命令是否执行完毕
     *
     * @return true:执行完毕 || false:未执行完毕
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private boolean isCommandCompleted() {
        Human agent = (Human) this.agentInfo.me();
        switch (this.commandType) {
            case ACTION_REST:
                if (this.target == null) {
                    return (agent.getDamage() == 0);
                }
                if (Objects.requireNonNull(this.worldInfo.getEntity(this.target))
                        .getStandardURN() == REFUGE) {
                    if (agent.getPosition().getValue() == this.target.getValue()) {
                        return (agent.getDamage() == 0);
                    }
                }
                return false;
            case ACTION_MOVE:
                return this.target == null || this.agentInfo.getPosition()
                        .getValue() == this.target.getValue();
            case ACTION_RESCUE:
                if (this.target == null) {
                    return true;
                }
                Human human = (Human) Objects
                        .requireNonNull(this.worldInfo.getEntity(this.target));
                return human.isBuriednessDefined() && human.getBuriedness() == 0
                        || (human.isHPDefined() && human.getHP() == 0);
            case ACTION_LOAD:
                if (this.target == null) {
                    return true;
                }
                Human human1 = (Human) Objects
                        .requireNonNull(this.worldInfo.getEntity(this.target));
                if ((human1.isHPDefined() && human1.getHP() == 0)) {
                    return true;
                }
                if (human1.getStandardURN() != CIVILIAN) {
                    this.commandType = ACTION_RESCUE;
                    return this.isCommandCompleted();
                }
                if (human1.isPositionDefined()) {
                    EntityID position = human1.getPosition();
                    if (this.worldInfo.getEntityIDsOfType(AMBULANCE_TEAM)
                            .contains(position)) {
                        return true;
                    } else if (this.worldInfo.getEntity(position)
                            .getStandardURN() == REFUGE) {
                        return true;
                    }
                }
                return false;
            case ACTION_UNLOAD:
                if (this.target != null) {
                    StandardEntity entity = this.worldInfo.getEntity(this.target);
                    if (entity != null && entity instanceof Area) {
                        if (this.target.getValue() != this.agentInfo.getPosition()
                                .getValue()) {
                            return false;
                        }
                    }
                }
                return (this.agentInfo.someoneOnBoard() == null);
            case ACTION_AUTONOMY:
                if (this.target != null) {
                    StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
                    if (targetEntity instanceof Area) {
                        this.commandType = this.agentInfo.someoneOnBoard() == null
                                ? ACTION_MOVE
                                : ACTION_UNLOAD;
                        return this.isCommandCompleted();
                    } else if (targetEntity instanceof Human) {
                        Human h = (Human) targetEntity;
                        if ((h.isHPDefined() && h.getHP() == 0)) {
                            return true;
                        }
                        this.commandType = h.getStandardURN() == CIVILIAN ? ACTION_LOAD
                                : ACTION_RESCUE;
                        return this.isCommandCompleted();
                    }
                }
                return true;
        }
        return true;
    }
}