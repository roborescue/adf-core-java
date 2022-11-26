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

public class DefaultTacticsFireBrigade extends TacticsFireBrigade {

    private HumanDetector humanDetector;
    private Search search;

    private ExtAction actionFireRescue;
    private ExtAction actionExtMove;

    private CommandExecutor<CommandFire> commandExecutorFire;
  private CommandExecutor<CommandScout> commandExecutorScout;

  private MessageTool messageTool;

  private CommunicationMessage recentCommand;
  private Boolean isVisualDebug;

  @Override
  public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData) {
    messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
        "MessageManager.PlatoonChannelSubscriber",
        "adf.impl.module.comm.DefaultChannelSubscriber"));
    messageManager.setMessageCoordinator(moduleManager.getMessageCoordinator(
        "MessageManager.PlatoonMessageCoordinator",
        "adf.impl.module.comm.DefaultMessageCoordinator"));

    worldInfo.indexClass(StandardEntityURN.CIVILIAN,
        StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
        StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.ROAD,
        StandardEntityURN.HYDRANT, StandardEntityURN.BUILDING,
        StandardEntityURN.REFUGE, StandardEntityURN.GAS_STATION,
        StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.FIRE_STATION,
        StandardEntityURN.POLICE_OFFICE);

    this.messageTool = new MessageTool(scenarioInfo, developData);

    this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
        .getModuleConfig().getBooleanValue("VisualDebug", false));

    this.recentCommand = null;

    // init Algorithm Module & ExtAction
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
    registerModule(this.humanDetector);
    registerModule(this.search);
    registerModule(this.actionFireRescue);
    registerModule(this.actionExtMove);
    registerModule(this.commandExecutorFire);
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
    FireBrigade agent = (FireBrigade) agentInfo.me();
    EntityID agentID = agentInfo.getID();
    // command
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
        .getReceivedMessageList(CommandFire.class)) {
      CommandFire command = (CommandFire) message;
      if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
          .getValue() == agentID.getValue()) {
        this.recentCommand = command;
        this.commandExecutorFire.setCommand(command);
      }
    }
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
    // autonomous
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

    messageManager.addMessage(new MessageFireBrigade(true, agent,
        MessageFireBrigade.ACTION_REST, agent.getPosition()));
    return new ActionRest();
  }


  private void sendActionMessage(MessageManager messageManager,
      FireBrigade fireBrigade, Action action) {
    Class<? extends Action> actionClass = action.getClass();
    int actionIndex = -1;
    EntityID target = null;
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
    if (actionIndex != -1) {
      messageManager.addMessage(
          new MessageFireBrigade(true, fireBrigade, actionIndex, target));
    }
  }
}