package adf.core.component.tactics;

import adf.core.agent.action.Action;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.AbstractModule;

import java.util.ArrayList;
import java.util.List;

public abstract class Tactics {

  private Tactics parentTactics;
  private List<AbstractModule> modules = new ArrayList<>();
  private List<ExtAction> modulesExtAction = new ArrayList<>();
  private List<CommandExecutor> modulesCommandExecutor = new ArrayList<>();

  public Tactics(Tactics parent) {
    this.parentTactics = parent;
  }


  public Tactics() {
    this(null);
  }


  abstract public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData);

  abstract public void precompute(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      PrecomputeData precomputeData, DevelopData developData);

  abstract public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      PrecomputeData precomputeData, DevelopData developData);

  abstract public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData developData);

  abstract public Action think(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData);


  public Tactics getParentTactics() {
    return parentTactics;
  }


  protected void registerModule(AbstractModule module) {
    modules.add(module);
  }


  protected boolean unregisterModule(AbstractModule module) {
    return modules.remove(module);
  }


  protected void registerModule(ExtAction module) {
    modulesExtAction.add(module);
  }


  protected boolean unregisterModule(ExtAction module) {
    return modulesExtAction.remove(module);
  }


  protected void registerModule(CommandExecutor module) {
    modulesCommandExecutor.add(module);
  }


  protected boolean unregisterModule(CommandExecutor module) {
    return modulesCommandExecutor.remove(module);
  }


  protected void modulesPrecompute(PrecomputeData precomputeData) {
    for (AbstractModule module : modules) {
      module.precompute(precomputeData);
    }
    for (ExtAction module : modulesExtAction) {
      module.precompute(precomputeData);
    }
    for (CommandExecutor module : modulesCommandExecutor) {
      module.precompute(precomputeData);
    }
  }


  protected void modulesResume(PrecomputeData precomputeData) {
    for (AbstractModule module : modules) {
      module.resume(precomputeData);
    }
    for (ExtAction module : modulesExtAction) {
      module.resume(precomputeData);
    }
    for (CommandExecutor module : modulesCommandExecutor) {
      module.resume(precomputeData);
    }
  }


  protected void modulesPreparate() {
    for (AbstractModule module : modules) {
      module.preparate();
    }
    for (ExtAction module : modulesExtAction) {
      module.preparate();
    }
    for (CommandExecutor module : modulesCommandExecutor) {
      module.preparate();
    }
  }


  protected void modulesUpdateInfo(MessageManager messageManager) {
    for (AbstractModule module : modules) {
      module.updateInfo(messageManager);
    }
    for (ExtAction module : modulesExtAction) {
      module.updateInfo(messageManager);
    }
    for (CommandExecutor module : modulesCommandExecutor) {
      module.updateInfo(messageManager);
    }
  }
}