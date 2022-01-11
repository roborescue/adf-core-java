package adf.core.component.module.algorithm;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.AbstractModule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

public abstract class Clustering extends AbstractModule {

  public Clustering(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
  }


  public abstract int getClusterNumber();

  public abstract int getClusterIndex(StandardEntity entity);

  public abstract int getClusterIndex(EntityID id);

  public abstract Collection<StandardEntity> getClusterEntities(int index);

  public abstract Collection<EntityID> getClusterEntityIDs(int index);


  public List<Collection<StandardEntity>> getAllClusterEntities() {
    int number = this.getClusterNumber();
    List<Collection<StandardEntity>> result = new ArrayList<>(number);
    for (int i = 0; i < number; i++) {
      result.add(i, this.getClusterEntities(i));
    }
    return result;
  }


  public List<Collection<EntityID>> getAllClusterEntityIDs() {
    int number = this.getClusterNumber();
    List<Collection<EntityID>> result = new ArrayList<>(number);
    for (int i = 0; i < number; i++) {
      result.add(i, this.getClusterEntityIDs(i));
    }
    return result;
  }


  @Override
  public Clustering precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    return this;
  }


  @Override
  public Clustering resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    return this;
  }


  @Override
  public Clustering preparate() {
    super.preparate();
    return this;
  }


  @Override
  public Clustering updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    return this;
  }


  @Override
  public abstract Clustering calc();
}