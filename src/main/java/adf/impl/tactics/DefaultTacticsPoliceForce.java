package adf.impl.tactics;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.action.police.ActionClear;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandPolice;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
import adf.core.agent.communication.standard.bundle.information.MessagePoliceForce;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.complex.RoadDetector;
import adf.core.component.module.complex.Search;
import adf.core.component.tactics.TacticsPoliceForce;
import adf.core.debug.WorldViewLauncher;
import adf.impl.tactics.utils.MessageTool;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.Objects;

/**
 * 警察部队的默认策略
 * Default strategy for the PoliceForce agent.
 * <p>
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsPoliceForce extends TacticsPoliceForce {

    private int clearDistance; // 清理障碍物的距离

    private RoadDetector roadDetector; // 道路检测模块
    private Search search; // 搜索模块

    private ExtAction actionExtClear; // 清理障碍物的扩展动作
    private ExtAction actionExtMove; // 移动的扩展动作

    private CommandExecutor<CommandPolice> commandExecutorPolice; // 警察命令执行器
    private CommandExecutor<CommandScout> commandExecutorScout; // 侦察命令执行器

    private MessageTool messageTool; // 消息工具

    private CommunicationMessage recentCommand; // 最近的命令

    private Boolean isVisualDebug; // 是否启用可视化调试

    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           MessageManager messageManager, DevelopData developData) {
        messageManager.setChannelSubscriber(
                moduleManager.getChannelSubscriber("MessageManager.PlatoonChannelSubscriber",
                        "adf.impl.module.comm.DefaultChannelSubscriber"));
        messageManager.setMessageCoordinator(
                moduleManager.getMessageCoordinator("MessageManager.PlatoonMessageCoordinator",
                        "adf.impl.module.comm.DefaultMessageCoordinator"));

        worldInfo.indexClass(StandardEntityURN.ROAD, StandardEntityURN.HYDRANT,
                StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,
                StandardEntityURN.BLOCKADE);

        this.messageTool =
                new MessageTool(scenarioInfo, developData);

        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));

        this.clearDistance = scenarioInfo.getClearRepairDistance();
        this.recentCommand = null;

        // 初始化算法模块和扩展动作
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.search = moduleManager.getModule("DefaultTacticsPoliceForce.Search",
                        "adf.impl.module.complex.DefaultSearch");
                this.roadDetector = moduleManager.getModule(
                        "DefaultTacticsPoliceForce.RoadDetector",
                        "adf.impl.module.complex.DefaultRoadDetector");
                this.actionExtClear = moduleManager.getExtAction(
                        "DefaultTacticsPoliceForce.ExtActionClear",
                        "adf.impl.extaction.DefaultExtActionClear");
                this.actionExtMove = moduleManager.getExtAction(
                        "DefaultTacticsPoliceForce.ExtActionMove",
                        "adf.impl.extaction.DefaultExtActionMove");
                this.commandExecutorPolice = moduleManager.getCommandExecutor(
                        "DefaultTacticsPoliceForce.CommandExecutorPolice",
                        "adf.impl.centralized.DefaultCommandExecutorPolice");
                this.commandExecutorScout = moduleManager.getCommandExecutor(
                        "DefaultTacticsPoliceForce.CommandExecutorScout",
                        "adf.impl.centralized.DefaultCommandExecutorScoutPolice");
                break;
        }
        registerModule(this.search);
        registerModule(this.roadDetector);
        registerModule(this.actionExtClear);
        registerModule(this.actionExtMove);
        registerModule(this.commandExecutorPolice);
        registerModule(this.commandExecutorScout);
    }

    @Override
    public void precompute(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           PrecomputeData precomputeData, DevelopData developData) {
        modulesPrecompute(precomputeData);
    }

    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData developData) {
        modulesResume(precomputeData);

        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData developData) {
        modulesPreparate();

        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    @Override
    public Action think(AgentInfo agentInfo, WorldInfo worldInfo,
                        ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                        MessageManager messageManager, DevelopData developData) {
        this.messageTool.reflectMessage(agentInfo, worldInfo, scenarioInfo,
                messageManager);
        this.messageTool.sendRequestMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);
        this.messageTool.sendInformationMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);

        modulesUpdateInfo(messageManager);

        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }

        PoliceForce agent = (PoliceForce) agentInfo.me();
        EntityID agentID = agent.getID();

        // 命令处理
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandScout.class)) {
            CommandScout command = (CommandScout) message;
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorScout.setCommand(command);
            }
        }
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandPolice.class)) {
            CommandPolice command = (CommandPolice) message;
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorPolice.setCommand(command);
            }
        }

        if (this.recentCommand != null) {
            Action action = null;
            if (this.recentCommand.getClass() == CommandPolice.class) {
                action = this.commandExecutorPolice.calc().getAction();
            } else if (this.recentCommand.getClass() == CommandScout.class) {
                action = this.commandExecutorScout.calc().getAction();
            }
            if (action != null) {
                this.sendActionMessage(worldInfo, messageManager, agent, action);
                return action;
            }
        }

        // 自主行动
        EntityID target = this.roadDetector.calc().getTarget();
        Action action = this.actionExtClear.setTarget(target).calc().getAction();
        if (action != null) {
            this.sendActionMessage(worldInfo, messageManager, agent, action);
            return action;
        }

        target = this.search.calc().getTarget();
        action = this.actionExtClear.setTarget(target).calc().getAction();
        if (action != null) {
            this.sendActionMessage(worldInfo, messageManager, agent, action);
            return action;
        }

        messageManager.addMessage(new MessagePoliceForce(true, agent,
                MessagePoliceForce.ACTION_REST, agent.getPosition()));
        return new ActionRest();
    }

    private void sendActionMessage(WorldInfo worldInfo,
                                   MessageManager messageManager, PoliceForce policeForce, Action action) {
        Class<? extends Action> actionClass = action.getClass();
        int actionIndex = -1;
        EntityID target = null;
        if (actionClass == ActionMove.class) {
            List<EntityID> path = ((ActionMove) action).getPath();
            actionIndex = MessagePoliceForce.ACTION_MOVE;
            if (path.size() > 0) {
                target = path.get(path.size() - 1);
            }
        } else if (actionClass == ActionClear.class) {
            actionIndex = MessagePoliceForce.ACTION_CLEAR;
            ActionClear ac = (ActionClear) action;
            target = ac.getTarget();
            if (target == null) {
                for (StandardEntity entity : worldInfo.getObjectsInRange(ac.getPosX(),
                        ac.getPosY(), this.clearDistance)) {
                    if (entity.getStandardURN() == StandardEntityURN.BLOCKADE) {
                        target = entity.getID();
                        break;
                    }
                }
            }
        } else if (actionClass == ActionRest.class) {
            actionIndex = MessagePoliceForce.ACTION_REST;
            target = policeForce.getPosition();
        }

        if (actionIndex != -1) {
            messageManager.addMessage(
                    new MessagePoliceForce(true, policeForce, actionIndex, target));
        }
    }
}
