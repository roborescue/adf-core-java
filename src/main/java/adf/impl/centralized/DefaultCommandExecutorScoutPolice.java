package adf.impl.centralized;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
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
import rescuecore2.worldmodel.AbstractEntity;
import rescuecore2.worldmodel.EntityID;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;

/**
 * 警察的侦察动作的命令执行器
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultCommandExecutorScoutPolice
        extends CommandExecutor<CommandScout> {

    /**
     * 未知动作
     */
    private static final int ACTION_UNKNOWN = -1;
    /**
     * 动作:侦察
     */
    private static final int ACTION_SCOUT = 1;
    /**
     * 路径规划算法
     */
    private PathPlanning pathPlanning;
    /**
     * 扩展动作:清理
     */
    private ExtAction actionExtClear;

    /**
     * 命令类型
     */
    private int commandType;

    /**
     * 侦察目标的EntityID的集合
     */
    private Collection<EntityID> scoutTargets;
    /**
     * 命令执行者的EntityID
     */
    private EntityID commanderID;

    /**
     * {@link DefaultCommandExecutorScoutPolice}的构造函数
     *
     * @param ai:           agentInfo     代理信息
     * @param wi:           worldInfo     世界信息
     * @param si:           scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public DefaultCommandExecutorScoutPolice(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
        super(ai, wi, si, moduleManager, developData);
        this.commandType = ACTION_UNKNOWN;
        this.scoutTargets = new HashSet<>();
        this.commanderID = null;

        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.pathPlanning = moduleManager.getModule(
                        "DefaultCommandExecutorScoutPolice.PathPlanning",
                        "adf.impl.module.algorithm.DijkstraPathPlanning");
                this.actionExtClear = moduleManager.getExtAction(
                        "DefaultCommandExecutorScoutPolice.ExtActionClear",
                        "adf.impl.extaction.DefaultExtActionClear");
                break;
        }
    }


    /**
     * 设置命令
     *
     * @param command 侦察命令
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandExecutor<CommandScout> setCommand(CommandScout command) {
        EntityID agentID = this.agentInfo.getID();
        if (command.isToIDDefined() && (Objects.requireNonNull(command.getToID())
                .getValue() == agentID.getValue())) {
            EntityID target = command.getTargetID();
            if (target == null) {
                target = this.agentInfo.getPosition();
            }
            this.commandType = ACTION_SCOUT;
            this.commanderID = command.getSenderID();
            this.scoutTargets = new HashSet<>();
            this.scoutTargets.addAll(
                    worldInfo.getObjectsInRange(target, command.getRange()).stream()
                            .filter(e -> e instanceof Area && e.getStandardURN() != REFUGE)
                            .map(AbstractEntity::getID).collect(Collectors.toList()));
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
    public CommandExecutor precompute(PrecomputeData precomputeData) {
        super.precompute(precomputeData);
        if (this.getCountPrecompute() >= 2) {
            return this;
        }
        this.pathPlanning.precompute(precomputeData);
        this.actionExtClear.precompute(precomputeData);
        return this;
    }


    /**
     * 预计算模式的初始化处理方法
     *
     * @param precomputeData 预计算数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public CommandExecutor resume(PrecomputeData precomputeData) {
        super.resume(precomputeData);
        if (this.getCountResume() >= 2) {
            return this;
        }
        this.pathPlanning.resume(precomputeData);
        this.actionExtClear.resume(precomputeData);
        return this;
    }


    /**
     * 无预计算模式的初始化处理方法
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public CommandExecutor preparate() {
        super.preparate();
        if (this.getCountPreparate() >= 2) {
            return this;
        }
        this.pathPlanning.preparate();
        this.actionExtClear.preparate();
        return this;
    }


    /**
     * 每个回合都会执行这个方法来更新agent所持有的信息
     *
     * @param messageManager 消息管理器
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public CommandExecutor updateInfo(MessageManager messageManager) {
        super.updateInfo(messageManager);
        if (this.getCountUpdateInfo() >= 2) {
            return this;
        }
        this.pathPlanning.updateInfo(messageManager);
        this.actionExtClear.updateInfo(messageManager);

        if (this.isCommandCompleted()) {
            if (this.commandType != ACTION_UNKNOWN) {
                messageManager
                        .addMessage(new MessageReport(true, true, false, this.commanderID));
                this.commandType = ACTION_UNKNOWN;
                this.scoutTargets = new HashSet<>();
                this.commanderID = null;
            }
        }
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
        EntityID position = this.agentInfo.getPosition();
        if (this.commandType == ACTION_SCOUT) {
            if (this.scoutTargets == null || this.scoutTargets.isEmpty()) {
                return this;
            }
            this.pathPlanning.setFrom(position);
            this.pathPlanning.setDestination(this.scoutTargets);
            List<EntityID> path = this.pathPlanning.calc().getResult();
            if (path != null) {
                EntityID target = path.size() > 0 ? path.get(path.size() - 1)
                        : position;
                Action action = this.actionExtClear.setTarget(target).calc()
                        .getAction();
                if (action == null) {
                    action = new ActionMove(path);
                }
                this.result = action;
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
        if (this.commandType == ACTION_SCOUT) {
            if (this.scoutTargets != null) {
                this.scoutTargets
                        .removeAll(this.worldInfo.getChanged().getChangedEntities());
            }
            return (this.scoutTargets == null || this.scoutTargets.isEmpty());
        }
        return true;
    }
}