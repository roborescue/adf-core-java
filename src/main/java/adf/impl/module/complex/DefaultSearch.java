package adf.impl.module.complex;

import static rescuecore2.standard.entities.StandardEntityURN.AMBULANCE_CENTRE;
import static rescuecore2.standard.entities.StandardEntityURN.AMBULANCE_TEAM;
import static rescuecore2.standard.entities.StandardEntityURN.BUILDING;
import static rescuecore2.standard.entities.StandardEntityURN.FIRE_BRIGADE;
import static rescuecore2.standard.entities.StandardEntityURN.FIRE_STATION;
import static rescuecore2.standard.entities.StandardEntityURN.GAS_STATION;
import static rescuecore2.standard.entities.StandardEntityURN.POLICE_FORCE;
import static rescuecore2.standard.entities.StandardEntityURN.POLICE_OFFICE;
import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.Clustering;
import adf.core.component.module.algorithm.PathPlanning;
import adf.core.component.module.complex.Search;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

public class DefaultSearch extends Search {

  private PathPlanning pathPlanning;
  private Clustering clustering;

  private EntityID result;
  private Collection<EntityID> unsearchedBuildingIDs;

  public DefaultSearch(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    this.unsearchedBuildingIDs = new HashSet<>();

    StandardEntityURN agentURN = ai.me().getStandardURN();
    switch (si.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        if (agentURN == AMBULANCE_TEAM) {
          this.pathPlanning = moduleManager.getModule(
              "DefaultSearch.PathPlanning.Ambulance",
              "adf.impl.module.algorithm.DijkstraPathPlanning");
          this.clustering = moduleManager.getModule(
              "DefaultSearch.Clustering.Ambulance",
              "adf.impl.module.algorithm.KMeansClustering");
        } else if (agentURN == FIRE_BRIGADE) {
          this.pathPlanning = moduleManager.getModule(
              "DefaultSearch.PathPlanning.Fire",
              "adf.impl.module.algorithm.DijkstraPathPlanning");
          this.clustering = moduleManager.getModule(
              "DefaultSearch.Clustering.Fire",
              "adf.impl.module.algorithm.KMeansClustering");
        } else if (agentURN == POLICE_FORCE) {
          this.pathPlanning = moduleManager.getModule(
              "DefaultSearch.PathPlanning.Police",
              "adf.impl.module.algorithm.DijkstraPathPlanning");
          this.clustering = moduleManager.getModule(
              "DefaultSearch.Clustering.Police",
              "adf.impl.module.algorithm.KMeansClustering");
        }
        break;
    }

    registerModule(this.pathPlanning);
    registerModule(this.clustering);
  }


  @Override
  public Search updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }

    this.unsearchedBuildingIDs
        .removeAll(this.worldInfo.getChanged().getChangedEntities());

    if (this.unsearchedBuildingIDs.isEmpty()) {
      this.reset();
      this.unsearchedBuildingIDs
          .removeAll(this.worldInfo.getChanged().getChangedEntities());
    }
    return this;
  }


  @Override
  public Search calc() {
    this.result = null;
    this.pathPlanning.setFrom(this.agentInfo.getPosition());
    this.pathPlanning.setDestination(this.unsearchedBuildingIDs);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path != null && path.size() > 0) {
      this.result = path.get(path.size() - 1);
    }
    return this;
  }


  private void reset() {
    this.unsearchedBuildingIDs.clear();

    Collection<StandardEntity> clusterEntities = null;
    if (this.clustering != null) {
      int clusterIndex = this.clustering
          .getClusterIndex(this.agentInfo.getID());
      clusterEntities = this.clustering.getClusterEntities(clusterIndex);

    }
    if (clusterEntities != null && clusterEntities.size() > 0) {
      for (StandardEntity entity : clusterEntities) {
        if (entity instanceof Building && entity.getStandardURN() != REFUGE) {
          this.unsearchedBuildingIDs.add(entity.getID());
        }
      }
    } else {
      this.unsearchedBuildingIDs
          .addAll(this.worldInfo.getEntityIDsOfType(BUILDING, GAS_STATION,
              AMBULANCE_CENTRE, FIRE_STATION, POLICE_OFFICE));
    }
  }


  @Override
  public EntityID getTarget() {
    return this.result;
  }


  @Override
  public Search precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    return this;
  }


  @Override
  public Search resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.worldInfo.requestRollback();
    return this;
  }


  @Override
  public Search preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.worldInfo.requestRollback();
    return this;
  }
}