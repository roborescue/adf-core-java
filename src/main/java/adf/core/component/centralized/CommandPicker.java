package adf.core.component.centralized;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.communication.CommunicationMessage;
import java.util.Collection;
import java.util.Map;
import rescuecore2.worldmodel.EntityID;

abstract public class CommandPicker {

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

  public CommandPicker(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    this.worldInfo = wi;
    this.agentInfo = ai;
    this.scenarioInfo = si;
    this.moduleManager = moduleManager;
    this.developData = developData;
    this.countPrecompute = 0;
    this.countResume = 0;
    this.countPreparate = 0;
    this.countUpdateInfo = 0;
    this.countUpdateInfoCurrentTime = 0;
  }


  public abstract CommandPicker
      setAllocatorResult(Map<EntityID, EntityID> allocationData);

  public abstract CommandPicker calc();

  public abstract Collection<CommunicationMessage> getResult();


  public CommandPicker precompute(PrecomputeData precomputeData) {
    this.countPrecompute++;
    return this;
  }


  public CommandPicker resume(PrecomputeData precomputeData) {
    this.countResume++;
    return this;
  }


  public CommandPicker preparate() {
    this.countPreparate++;
    return this;
  }


  public CommandPicker updateInfo(MessageManager messageManager) {
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