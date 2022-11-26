package adf.core.component.centralized;

import adf.core.agent.action.Action;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.communication.CommunicationMessage;

public abstract class CommandExecutor<C extends CommunicationMessage> {

  protected ScenarioInfo scenarioInfo;
  protected AgentInfo agentInfo;
  protected WorldInfo worldInfo;
  protected ModuleManager moduleManager;
  protected DevelopData developData;

  private int countPrecompute;
  private int countResume;
  private int countPreparate;
  private int countUpdateInfo;
  private int countUpdateInfoCurrentTime;

  protected Action result;

  public CommandExecutor(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    this.worldInfo = wi;
    this.agentInfo = ai;
    this.scenarioInfo = si;
    this.moduleManager = moduleManager;
    this.developData = developData;
    this.result = null;
    this.countPrecompute = 0;
    this.countResume = 0;
    this.countPreparate = 0;
    this.countUpdateInfo = 0;
    this.countUpdateInfoCurrentTime = 0;
  }


  public abstract CommandExecutor setCommand(C command);

  public abstract CommandExecutor calc();


  public Action getAction() {
    return result;
  }


  public CommandExecutor precompute(PrecomputeData precomputeData) {
    this.countPrecompute++;
    return this;
  }


  public CommandExecutor resume(PrecomputeData precomputeData) {
    this.countResume++;
    return this;
  }


  public CommandExecutor preparate() {
    this.countPreparate++;
    return this;
  }


  public CommandExecutor updateInfo(MessageManager messageManager) {
    if (this.countUpdateInfoCurrentTime != this.agentInfo.getTime()) {
      this.countUpdateInfo = 0;
      this.countUpdateInfoCurrentTime = this.agentInfo.getTime();
    }
    this.countUpdateInfo++;
    return this;
  }


  public int getCountPrecompute() {
    return this.countPrecompute;
  }


  public int getCountResume() {
    return this.countResume;
  }


  public int getCountPreparate() {
    return this.countPreparate;
  }


  public int getCountUpdateInfo() {
    if (this.countUpdateInfoCurrentTime != this.agentInfo.getTime()) {
      this.countUpdateInfo = 0;
      this.countUpdateInfoCurrentTime = this.agentInfo.getTime();
    }
    return this.countUpdateInfo;
  }


  public void resetCountPrecompute() {
    this.countPrecompute = 0;
  }


  public void resetCountResume() {
    this.countResume = 0;
  }


  public void resetCountPreparate() {
    this.countPreparate = 0;
  }


  public void resetCountUpdateInfo() {
    this.countUpdateInfo = 0;
  }
}