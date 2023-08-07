package adf.impl.tactics;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.action.fire.ActionRescue;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandFire;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
import adf.core.agent.communication.standard.bundle.information.MessageFireBrigade;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.complex.HumanDetector;
import adf.core.component.module.complex.Search;
import adf.core.component.tactics.TacticsFireBrigade;
import adf.core.debug.WorldViewLauncher;
import adf.impl.tactics.utils.MessageTool;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.Objects;

/**
 * 消防部队的默认策略
 * <p>
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsFireBrigade extends TacticsFireBrigade {

    // Modules
    private HumanDetector humanDetector;
    private Search search;

    // ExtActions
    private ExtAction actionFireRescue;
    private ExtAction actionExtMove;

    // CommandExecutors
    private CommandExecutor<CommandFire> commandExecutorFire;
    private CommandExecutor<CommandScout> commandExecutorScout;

    // Tool for sending and receiving messages
    private MessageTool messageTool;

    // Recently received command (if any)
    private CommunicationMessage recentCommand;

    // Whether to enable visual debug
    private Boolean isVisualDebug;

    /**
     * 初始化方法
     * 在此方法中进行模块的初始化和注册
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           MessageManager messageManager, DevelopData developData) {
        // 设置消息管理器的订阅者和协调者
        messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
                "MessageManager.PlatoonChannelSubscriber",
                "adf.impl.module.comm.DefaultChannelSubscriber"));
        messageManager.setMessageCoordinator(moduleManager.getMessageCoordinator(
                "MessageManager.PlatoonMessageCoordinator",
                "adf.impl.module.comm.DefaultMessageCoordinator"));

        // Index entity types
        worldInfo.indexClass(StandardEntityURN.CIVILIAN,
                StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.ROAD,
                StandardEntityURN.HYDRANT, StandardEntityURN.BUILDING,
                StandardEntityURN.REFUGE, StandardEntityURN.GAS_STATION,
                StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.FIRE_STATION,
                StandardEntityURN.POLICE_OFFICE);

        // 创建消息工具
        this.messageTool = new MessageTool(scenarioInfo, developData);

        // 从方案配置中获取可视化调试标志
        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));

        // 将最近收到的命令初始化为空
        this.recentCommand = null;

        // 根据仿真模式初始化算法模块和扩展动作
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.humanDetector = moduleManager.getModule(
                        "DefaultTacticsFireBrigade.HumanDetector",
                        "adf.impl.module.complex.DefaultHumanDetector");
                this.search = moduleManager.getModule(
                        "DefaultTacticsFireBrigade.Search",
                        "adf.impl.module.complex.DefaultSearch");
                this.actionFireRescue = moduleManager.getExtAction(
                        "DefaultTacticsFireBrigade.ExtActionFireRescue",
                        "adf.impl.extaction.DefaultExtActionFireRescue");
                this.actionExtMove = moduleManager.getExtAction(
                        "DefaultTacticsFireBrigade.ExtActionMove",
                        "adf.impl.extaction.DefaultExtActionMove");
                this.commandExecutorFire = moduleManager.getCommandExecutor(
                        "DefaultTacticsFireBrigade.CommandExecutorFire",
                        "adf.impl.centralized.DefaultCommandExecutorFire");
                this.commandExecutorScout = moduleManager.getCommandExecutor(
                        "DefaultTacticsFireBrigade.CommandExecutorScout",
                        "adf.impl.centralized.DefaultCommandExecutorScout");
                break;
        }

        // 注册模块
        registerModule(this.humanDetector);
        registerModule(this.search);
        registerModule(this.actionFireRescue);
        registerModule(this.actionExtMove);
        registerModule(this.commandExecutorFire);
        registerModule(this.commandExecutorScout);
    }

    /**
     * 预计算
     * 在precompute恢复阶段进行模块的恢复操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void precompute(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           PrecomputeData precomputeData, DevelopData developData) {
        // 预计算模块
        modulesPrecompute(precomputeData);
    }

    /**
     * 恢复方法
     * 在precompute恢复阶段进行模块的恢复操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData developData) {
        // 恢复模块
        modulesResume(precomputeData);

        // 显示可视化调试（如果已启用）
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    /**
     * 准备方法
     * 在precompute准备阶段进行模块的准备操作
     *
     * @param agentInfo     代理的信息
     * @param worldInfo     世界的信息
     * @param scenarioInfo  场景的信息
     * @param moduleManager 模块管理器
     * @param developData   开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData developData) {
        // 准备模块
        modulesPreparate();

        // 显示可视化调试（如果已启用）
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    /**
     * 思考方法
     * 在此方法中进行模块信息更新、可视化调试和发送消息等操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public Action think(AgentInfo agentInfo, WorldInfo worldInfo,
                        ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                        MessageManager messageManager, DevelopData developData) {
        // 反映收到的消息
        this.messageTool.reflectMessage(agentInfo, worldInfo, scenarioInfo,
                messageManager);

        // 发送请求消息
        this.messageTool.sendRequestMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);

        //发送信息消息
        this.messageTool.sendInformationMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);

        // 根据收到的消息更新模块信息
        modulesUpdateInfo(messageManager);

        // 显示可视化调试（如果已启用）
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }

        // 获取当前代理及其 ID
        FireBrigade agent = (FireBrigade) agentInfo.me();
        EntityID agentID = agentInfo.getID();

        // 处理收到的命令
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandScout.class)) {
            CommandScout command = (CommandScout) message;
            // 如果命令目标 ID 与代理 ID 相同，请设置最近的命令并执行它
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorScout.setCommand(command);
            }
        }
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandFire.class)) {
            CommandFire command = (CommandFire) message;
            // 如果命令目标 ID 与代理 ID 相同，设置最近的命令并执行它
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorFire.setCommand(command);
            }
        }
        // 如果找到有效的最近命令，执行相应的操作
        if (this.recentCommand != null) {
            Action action = null;
            if (this.recentCommand.getClass() == CommandFire.class) {
                action = this.commandExecutorFire.calc().getAction();
            } else if (this.recentCommand.getClass() == CommandScout.class) {
                action = this.commandExecutorScout.calc().getAction();
            }
            if (action != null) {
                this.sendActionMessage(messageManager, agent, action);
                return action;
            }
        }

        // 自主操作的计算目标
        EntityID target = this.humanDetector.calc().getTarget();
        Action action = this.actionFireRescue.setTarget(target).calc().getAction();
        if (action != null) {
            this.sendActionMessage(messageManager, agent, action);
            return action;
        }
        target = this.search.calc().getTarget();
        action = this.actionExtMove.setTarget(target).calc().getAction();
        if (action != null) {
            this.sendActionMessage(messageManager, agent, action);
            return action;
        }

        // 如果未找到有效的操作，发送 REST 操作消息
        messageManager.addMessage(new MessageFireBrigade(true, agent,
                MessageFireBrigade.ACTION_REST, agent.getPosition()));
        return new ActionRest();
    }


    /**
     * Sends an action message to the message manager.
     *
     * @param messageManager The message manager
     * @param fireBrigade    The fire brigade agent
     * @param action         The action to be sent
     */
    private void sendActionMessage(MessageManager messageManager,
                                   FireBrigade fireBrigade, Action action) {
        Class<? extends Action> actionClass = action.getClass();
        int actionIndex = -1;
        EntityID target = null;

        // Determine the action index and target according to the action class
        if (actionClass == ActionMove.class) {
            actionIndex = MessageFireBrigade.ACTION_MOVE;
            List<EntityID> path = ((ActionMove) action).getPath();
            if (path.size() > 0) {
                target = path.get(path.size() - 1);
            }
        } else if (actionClass == ActionRescue.class) {
            actionIndex = MessageFireBrigade.ACTION_RESCUE;
            target = ((ActionRescue) action).getTarget();
        } else if (actionClass == ActionRest.class) {
            actionIndex = MessageFireBrigade.ACTION_REST;
            target = fireBrigade.getPosition();
        }

        // If a valid action index is found, send the action message
        if (actionIndex != -1) {
            messageManager.addMessage(
                    new MessageFireBrigade(true, fireBrigade, actionIndex, target));
        }
    }
}
