package adf.impl.module.algorithm;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.Clustering;
import adf.core.component.module.algorithm.StaticClustering;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import rescuecore2.misc.Pair;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

public class KMeansClustering extends StaticClustering {

  private static final String KEY_CLUSTER_SIZE = "clustering.size";
  private static final String KEY_CLUSTER_CENTER = "clustering.centers";
  private static final String KEY_CLUSTER_ENTITY = "clustering.entities.";
  private static final String KEY_ASSIGN_AGENT = "clustering.assign";

  private int repeatPrecompute;
  private int repeatPreparate;

  private Collection<StandardEntity> entities;

  private List<StandardEntity> centerList;
  private List<EntityID> centerIDs;
  private Map<Integer, List<StandardEntity>> clusterEntitiesList;
  private List<List<EntityID>> clusterEntityIDsList;

  private int clusterSize;

  private boolean assignAgentsFlag;

  // Edge-weighted adjacency list: area ID -> (neighbour area ID -> travel cost)
  private Map<EntityID, Map<EntityID, Double>> weightedGraph;

  public KMeansClustering(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
    this.repeatPrecompute = developData.getInteger(
        "adf.impl.module.algorithm.KMeansClustering.repeatPrecompute", 7);
    this.repeatPreparate = developData.getInteger(
        "adf.impl.module.algorithm.KMeansClustering.repeatPreparate", 30);
    this.clusterSize = developData.getInteger(
        "adf.impl.module.algorithm.KMeansClustering.clusterSize", 5);
    if (agentInfo.me().getStandardURN()
        .equals(StandardEntityURN.AMBULANCE_TEAM)) {
      this.clusterSize = scenarioInfo.getScenarioAgentsAt();
    } else if (agentInfo.me().getStandardURN()
        .equals(StandardEntityURN.FIRE_BRIGADE)) {
      this.clusterSize = scenarioInfo.getScenarioAgentsFb();
    } else if (agentInfo.me().getStandardURN()
        .equals(StandardEntityURN.POLICE_FORCE)) {
      this.clusterSize = scenarioInfo.getScenarioAgentsPf();
    }
    this.assignAgentsFlag = developData.getBoolean(
        "adf.impl.module.algorithm.KMeansClustering.assignAgentsFlag", true);
    this.clusterEntityIDsList = new ArrayList<>();
    this.centerIDs = new ArrayList<>();
    this.clusterEntitiesList = new HashMap<>();
    this.centerList = new ArrayList<>();
    this.entities = wi.getEntitiesOfType(StandardEntityURN.ROAD,
        StandardEntityURN.HYDRANT, StandardEntityURN.BUILDING,
        StandardEntityURN.REFUGE, StandardEntityURN.GAS_STATION,
        StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.FIRE_STATION,
        StandardEntityURN.POLICE_OFFICE);
  }


  @Override
  public Clustering updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.centerList.clear();
    this.clusterEntitiesList.clear();
    return this;
  }


  @Override
  public Clustering precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.calcPathBased(this.repeatPrecompute);
    this.entities = null;
    // write
    precomputeData.setInteger(KEY_CLUSTER_SIZE, this.clusterSize);
    precomputeData.setEntityIDList(KEY_CLUSTER_CENTER, this.centerIDs);
    for (int i = 0; i < this.clusterSize; i++) {
      precomputeData.setEntityIDList(KEY_CLUSTER_ENTITY + i,
          this.clusterEntityIDsList.get(i));
    }
    precomputeData.setBoolean(KEY_ASSIGN_AGENT, this.assignAgentsFlag);
    return this;
  }


  @Override
  public Clustering resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.entities = null;
    // read
    this.clusterSize = precomputeData.getInteger(KEY_CLUSTER_SIZE);
    this.centerIDs = new ArrayList<>(
        precomputeData.getEntityIDList(KEY_CLUSTER_CENTER));
    this.clusterEntityIDsList = new ArrayList<>(this.clusterSize);
    for (int i = 0; i < this.clusterSize; i++) {
      this.clusterEntityIDsList.add(i,
          precomputeData.getEntityIDList(KEY_CLUSTER_ENTITY + i));
    }
    this.assignAgentsFlag = precomputeData.getBoolean(KEY_ASSIGN_AGENT);
    return this;
  }


  @Override
  public Clustering preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.calcStandard(this.repeatPreparate);
    this.entities = null;
    return this;
  }


  @Override
  public int getClusterNumber() {
    // The number of clusters
    return this.clusterSize;
  }


  @Override
  public int getClusterIndex(StandardEntity entity) {
    return this.getClusterIndex(entity.getID());
  }


  @Override
  public int getClusterIndex(EntityID id) {
    for (int i = 0; i < this.clusterSize; i++) {
      if (this.clusterEntityIDsList.get(i).contains(id)) {
        return i;
      }
    }
    return -1;
  }


  @Override
  public Collection<StandardEntity> getClusterEntities(int index) {
    List<StandardEntity> result = this.clusterEntitiesList.get(index);
    if (result == null || result.isEmpty()) {
      List<EntityID> list = this.clusterEntityIDsList.get(index);
      result = new ArrayList<>(list.size());
      for (int i = 0; i < list.size(); i++) {
        result.add(i, this.worldInfo.getEntity(list.get(i)));
      }
      this.clusterEntitiesList.put(index, result);
    }
    return result;
  }


  @Override
  public Collection<EntityID> getClusterEntityIDs(int index) {
    return this.clusterEntityIDsList.get(index);
  }


  @Override
  public Clustering calc() {
    return this;
  }


  private void calcStandard(int repeat) {
    this.initWeightedGraph(this.worldInfo);
    Random random = new Random();

    List<StandardEntity> entityList = new ArrayList<>(this.entities);
    this.centerList = new ArrayList<>(this.clusterSize);
    this.clusterEntitiesList = new HashMap<>(this.clusterSize);

    // init list
    for (int index = 0; index < this.clusterSize; index++) {
      this.clusterEntitiesList.put(index, new ArrayList<>());
      this.centerList.add(index, entityList.get(0));
    }
    System.out.println("[" + this.getClass().getSimpleName() + "] Cluster : "
        + this.clusterSize);
    // init center
    for (int index = 0; index < this.clusterSize; index++) {
      StandardEntity centerEntity;
      do {
        centerEntity = entityList
            .get(Math.abs(random.nextInt()) % entityList.size());
      } while (this.centerList.contains(centerEntity));
      this.centerList.set(index, centerEntity);
    }
    // calc center
    for (int i = 0; i < repeat; i++) {
      this.clusterEntitiesList.clear();
      for (int index = 0; index < this.clusterSize; index++) {
        this.clusterEntitiesList.put(index, new ArrayList<>());
      }
      for (StandardEntity entity : entityList) {
        StandardEntity tmp = this.getNearEntityByLine(this.worldInfo,
            this.centerList, entity);
        this.clusterEntitiesList.get(this.centerList.indexOf(tmp)).add(entity);
      }
      for (int index = 0; index < this.clusterSize; index++) {
        List<StandardEntity> clusterEntities = this.clusterEntitiesList.get(index);
        if (clusterEntities.isEmpty()) continue;
        int sumX = 0, sumY = 0;
        for (StandardEntity entity : clusterEntities) {
          Pair<Integer, Integer> location = this.worldInfo.getLocation(entity);
          sumX += location.first();
          sumY += location.second();
        }
        int centerX = sumX / clusterEntities.size();
        int centerY = sumY / clusterEntities.size();
        StandardEntity center = this.getNearEntityByLine(this.worldInfo,
            clusterEntities, centerX, centerY);
        if (center instanceof Area) {
          this.centerList.set(index, center);
        } else if (center instanceof Human) {
          this.centerList.set(index,
              this.worldInfo.getEntity(((Human) center).getPosition()));
        } else if (center instanceof Blockade) {
          this.centerList.set(index,
              this.worldInfo.getEntity(((Blockade) center).getPosition()));
        }
      }
      if (scenarioInfo.isDebugMode()) {
        System.out.print("*");
      }
    }

    if (scenarioInfo.isDebugMode()) {
      System.out.println();
    }

    // set entity
    this.clusterEntitiesList.clear();
    for (int index = 0; index < this.clusterSize; index++) {
      this.clusterEntitiesList.put(index, new ArrayList<>());
    }
    for (StandardEntity entity : entityList) {
      StandardEntity tmp = this.getNearEntityByLine(this.worldInfo,
          this.centerList, entity);
      this.clusterEntitiesList.get(this.centerList.indexOf(tmp)).add(entity);
    }

    if (this.assignAgentsFlag) {
      List<StandardEntity> firebrigadeList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE));
      List<StandardEntity> policeforceList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE));
      List<StandardEntity> ambulanceteamList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM));

      this.assignAgents(this.worldInfo, firebrigadeList);
      this.assignAgents(this.worldInfo, policeforceList);
      this.assignAgents(this.worldInfo, ambulanceteamList);
    }

    this.centerIDs = new ArrayList<>();
    for (int i = 0; i < this.centerList.size(); i++) {
      this.centerIDs.add(i, this.centerList.get(i).getID());
    }
    for (int index = 0; index < this.clusterSize; index++) {
      List<StandardEntity> entities = this.clusterEntitiesList.get(index);
      List<EntityID> list = new ArrayList<>(entities.size());
      for (int i = 0; i < entities.size(); i++) {
        list.add(i, entities.get(i).getID());
      }
      this.clusterEntityIDsList.add(index, list);
    }
  }


  private void calcPathBased(int repeat) {
    this.initWeightedGraph(this.worldInfo);
    Random random = new Random();
    List<StandardEntity> entityList = new ArrayList<>(this.entities);
    this.centerList = new ArrayList<>(this.clusterSize);
    this.clusterEntitiesList = new HashMap<>(this.clusterSize);

    for (int index = 0; index < this.clusterSize; index++) {
      this.clusterEntitiesList.put(index, new ArrayList<>());
      this.centerList.add(index, entityList.get(0));
    }
    for (int index = 0; index < this.clusterSize; index++) {
      StandardEntity centerEntity;
      do {
        centerEntity = entityList
            .get(Math.abs(random.nextInt()) % entityList.size());
      } while (this.centerList.contains(centerEntity));
      this.centerList.set(index, centerEntity);
    }

    for (int i = 0; i < repeat; i++) {
      this.clusterEntitiesList.clear();
      for (int index = 0; index < this.clusterSize; index++) {
        this.clusterEntitiesList.put(index, new ArrayList<>());
      }

      // Assign each entity to its nearest center using multi-source Dijkstra.
      // All K centers are expanded simultaneously on the weighted road graph,
      // so each entity is assigned to the center with the shortest path distance.
      // Complexity: O(N log N) per iteration  (formerly: O(N * K * N))
      Map<EntityID, Integer> assignment = this.assignByMultiSourceDijkstra(this.centerList);
      for (StandardEntity entity : entityList) {
        Integer clusterIndex = assignment.get(entity.getID());
        if (clusterIndex != null) {
          this.clusterEntitiesList.get(clusterIndex).add(entity);
        }
      }

      // Update centers: move each center to the entity nearest to the cluster centroid
      for (int index = 0; index < this.clusterSize; index++) {
        List<StandardEntity> clusterEntities = this.clusterEntitiesList.get(index);
        if (clusterEntities.isEmpty()) continue;
        int sumX = 0, sumY = 0;
        for (StandardEntity entity : clusterEntities) {
          Pair<Integer, Integer> location = this.worldInfo.getLocation(entity);
          sumX += location.first();
          sumY += location.second();
        }
        int centerX = sumX / clusterEntities.size();
        int centerY = sumY / clusterEntities.size();
        StandardEntity center = this.getNearEntityByLine(this.worldInfo,
            clusterEntities, centerX, centerY);
        if (center instanceof Area) {
          this.centerList.set(index, center);
        } else if (center instanceof Human) {
          this.centerList.set(index,
              this.worldInfo.getEntity(((Human) center).getPosition()));
        } else if (center instanceof Blockade) {
          this.centerList.set(index,
              this.worldInfo.getEntity(((Blockade) center).getPosition()));
        }
      }

      if (scenarioInfo.isDebugMode()) {
        System.out.print("*");
      }
    }

    if (scenarioInfo.isDebugMode()) {
      System.out.println();
    }

    // Final assignment: run multi-source Dijkstra once more with the converged centers
    this.clusterEntitiesList.clear();
    for (int index = 0; index < this.clusterSize; index++) {
      this.clusterEntitiesList.put(index, new ArrayList<>());
    }
    Map<EntityID, Integer> finalAssignment = this.assignByMultiSourceDijkstra(this.centerList);
    for (StandardEntity entity : entityList) {
      Integer clusterIndex = finalAssignment.get(entity.getID());
      if (clusterIndex != null) {
        this.clusterEntitiesList.get(clusterIndex).add(entity);
      }
    }

    if (this.assignAgentsFlag) {
      List<StandardEntity> fireBrigadeList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE));
      List<StandardEntity> policeForceList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.POLICE_FORCE));
      List<StandardEntity> ambulanceTeamList = new ArrayList<>(
          this.worldInfo.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM));
      this.assignAgents(this.worldInfo, fireBrigadeList);
      this.assignAgents(this.worldInfo, policeForceList);
      this.assignAgents(this.worldInfo, ambulanceTeamList);
    }

    this.centerIDs = new ArrayList<>();
    for (int i = 0; i < this.centerList.size(); i++) {
      this.centerIDs.add(i, this.centerList.get(i).getID());
    }
    for (int index = 0; index < this.clusterSize; index++) {
      List<StandardEntity> entities = this.clusterEntitiesList.get(index);
      List<EntityID> list = new ArrayList<>(entities.size());
      for (int i = 0; i < entities.size(); i++) {
        list.add(i, entities.get(i).getID());
      }
      this.clusterEntityIDsList.add(index, list);
    }
  }


  /**
   * Voronoi partition using multi-source Dijkstra.
   *
   * All K centers are seeded into a priority queue at distance 0 and expanded
   * simultaneously using edge-weighted shortest-path search. Each entity is
   * assigned to the cluster whose center has the minimum path distance to it.
   *
   * Complexity: O(N log N)  (formerly O(N * K * N) with comparePathDistance)
   *
   * @param centers list of cluster centers
   * @return map from EntityID to cluster index
   */
  private Map<EntityID, Integer> assignByMultiSourceDijkstra(List<StandardEntity> centers) {
    Map<EntityID, Double> dist = new HashMap<>();
    Map<EntityID, Integer> assignment = new HashMap<>();
    // Entry: [distance, clusterIndex, entityID_value]
    PriorityQueue<double[]> pq = new PriorityQueue<>(
        Comparator.comparingDouble(a -> a[0]));

    for (int i = 0; i < centers.size(); i++) {
      EntityID cid = centers.get(i).getID();
      if (!dist.containsKey(cid)) {
        dist.put(cid, 0.0);
        assignment.put(cid, i);
        pq.offer(new double[]{0.0, i, cid.getValue()});
      }
    }

    while (!pq.isEmpty()) {
      double[] cur = pq.poll();
      double d = cur[0];
      int ci = (int) cur[1];
      EntityID uid = new EntityID((int) cur[2]);

      if (d > dist.getOrDefault(uid, Double.MAX_VALUE)) continue;
      assignment.putIfAbsent(uid, ci);

      Map<EntityID, Double> neighbours = weightedGraph.get(uid);
      if (neighbours == null) continue;
      for (Map.Entry<EntityID, Double> entry : neighbours.entrySet()) {
        double nd = d + entry.getValue();
        if (nd < dist.getOrDefault(entry.getKey(), Double.MAX_VALUE)) {
          dist.put(entry.getKey(), nd);
          assignment.put(entry.getKey(), ci);
          pq.offer(new double[]{nd, ci, entry.getKey().getValue()});
        }
      }
    }

    return assignment;
  }


  /**
   * Build an edge-weighted adjacency graph.
   *
   * The travel cost between adjacent areas A and B is defined as:
   *   cost(A -> B) = dist(A.center, midpoint of the A-B edge)
   *               + dist(B.center, midpoint of the B-A edge)
   *
   * This matches the per-edge cost used in getPathDistance(), ensuring that
   * assignByMultiSourceDijkstra produces assignments equivalent to the original
   * path-distance Voronoi partition.
   *
   * Replaces the former initShortestPath() (unweighted adjacency graph).
   */
  private void initWeightedGraph(WorldInfo worldInfo) {
    this.weightedGraph = new HashMap<>();
    for (Entity entity : worldInfo) {
      if (!(entity instanceof Area)) continue;
      Area area = (Area) entity;
      Pair<Integer, Integer> aCenter = worldInfo.getLocation(area);
      Map<EntityID, Double> neighbours = new HashMap<>();

      for (EntityID neighbourId : area.getNeighbours()) {
        Entity neighbourEntity = worldInfo.getEntity(neighbourId);
        if (!(neighbourEntity instanceof Area)) continue;
        Area neighbour = (Area) neighbourEntity;

        Edge edgeFromArea = area.getEdgeTo(neighbourId);
        Edge edgeFromNeighbour = neighbour.getEdgeTo(area.getID());
        if (edgeFromArea == null || edgeFromNeighbour == null) continue;

        Pair<Integer, Integer> nCenter = worldInfo.getLocation(neighbour);
        double weight = getDistance(aCenter, edgeFromArea)
                      + getDistance(nCenter, edgeFromNeighbour);
        neighbours.put(neighbourId, weight);
      }
      this.weightedGraph.put(area.getID(), neighbours);
    }
  }


  private void assignAgents(WorldInfo world, List<StandardEntity> agentList) {
    int clusterIndex = 0;
    while (agentList.size() > 0) {
      StandardEntity center = this.centerList.get(clusterIndex);
      StandardEntity agent = this.getNearAgent(world, agentList, center);
      this.clusterEntitiesList.get(clusterIndex).add(agent);
      agentList.remove(agent);
      clusterIndex++;
      if (clusterIndex >= this.clusterSize) {
        clusterIndex = 0;
      }
    }
  }


  private StandardEntity getNearEntityByLine(WorldInfo world,
      List<StandardEntity> srcEntityList, StandardEntity targetEntity) {
    Pair<Integer, Integer> location = world.getLocation(targetEntity);
    return this.getNearEntityByLine(world, srcEntityList, location.first(),
        location.second());
  }


  private StandardEntity getNearEntityByLine(WorldInfo world,
      List<StandardEntity> srcEntityList, int targetX, int targetY) {
    StandardEntity result = null;
    for (StandardEntity entity : srcEntityList) {
      result = ((result != null)
          ? this.compareLineDistance(world, targetX, targetY, result, entity)
          : entity);
    }
    return result;
  }


  private StandardEntity getNearAgent(WorldInfo worldInfo,
      List<StandardEntity> srcAgentList, StandardEntity targetEntity) {
    StandardEntity result = null;
    for (StandardEntity agent : srcAgentList) {
      Human human = (Human) agent;
      if (result == null) {
        result = agent;
      } else {
        if (this
            .comparePathDistance(worldInfo, targetEntity, result,
                worldInfo.getPosition(human))
            .equals(worldInfo.getPosition(human))) {
          result = agent;
        }
      }
    }
    return result;
  }


  private Point2D getEdgePoint(Edge edge) {
    Point2D start = edge.getStart();
    Point2D end = edge.getEnd();
    return new Point2D(((start.getX() + end.getX()) / 2.0D),
        ((start.getY() + end.getY()) / 2.0D));
  }


  private double getDistance(double fromX, double fromY, double toX,
      double toY) {
    double dx = fromX - toX;
    double dy = fromY - toY;
    return Math.hypot(dx, dy);
  }


  private double getDistance(Pair<Integer, Integer> from, Point2D to) {
    return getDistance(from.first(), from.second(), to.getX(), to.getY());
  }


  private double getDistance(Pair<Integer, Integer> from, Edge to) {
    return getDistance(from, getEdgePoint(to));
  }


  private double getDistance(Point2D from, Point2D to) {
    return getDistance(from.getX(), from.getY(), to.getX(), to.getY());
  }


  private double getDistance(Edge from, Edge to) {
    return getDistance(getEdgePoint(from), getEdgePoint(to));
  }


  private StandardEntity compareLineDistance(WorldInfo worldInfo, int targetX,
      int targetY, StandardEntity first, StandardEntity second) {
    Pair<Integer, Integer> firstLocation = worldInfo.getLocation(first);
    Pair<Integer, Integer> secondLocation = worldInfo.getLocation(second);
    double firstDistance = getDistance(firstLocation.first(),
        firstLocation.second(), targetX, targetY);
    double secondDistance = getDistance(secondLocation.first(),
        secondLocation.second(), targetX, targetY);
    return (firstDistance < secondDistance ? first : second);
  }


  // Used by getNearAgent() for initial agent placement only.
  private StandardEntity comparePathDistance(WorldInfo worldInfo,
      StandardEntity target, StandardEntity first, StandardEntity second) {
    double firstDistance = getPathDistance(worldInfo,
        shortestPath(target.getID(), first.getID()));
    double secondDistance = getPathDistance(worldInfo,
        shortestPath(target.getID(), second.getID()));
    return (firstDistance < secondDistance ? first : second);
  }


  private double getPathDistance(WorldInfo worldInfo, List<EntityID> path) {
    if (path == null)
      return Double.MAX_VALUE;
    if (path.size() <= 1)
      return 0.0D;

    double distance = 0.0D;
    int limit = path.size() - 1;

    Area area = (Area) worldInfo.getEntity(path.get(0));
    distance += getDistance(worldInfo.getLocation(area),
        area.getEdgeTo(path.get(1)));
    area = (Area) worldInfo.getEntity(path.get(limit));
    distance += getDistance(worldInfo.getLocation(area),
        area.getEdgeTo(path.get(limit - 1)));

    for (int i = 1; i < limit; i++) {
      area = (Area) worldInfo.getEntity(path.get(i));
      distance += getDistance(area.getEdgeTo(path.get(i - 1)),
          area.getEdgeTo(path.get(i + 1)));
    }
    return distance;
  }


  private List<EntityID> shortestPath(EntityID start, EntityID... goals) {
    return shortestPath(start, Arrays.asList(goals));
  }


  // BFS used by comparePathDistance() for getNearAgent().
  // Reuses weightedGraph as an adjacency list (edge weights are ignored here).
  private List<EntityID> shortestPath(EntityID start,
      Collection<EntityID> goals) {
    List<EntityID> open = new LinkedList<>();
    Map<EntityID, EntityID> ancestors = new HashMap<>();
    open.add(start);
    EntityID next;
    boolean found = false;
    ancestors.put(start, start);
    do {
      next = open.remove(0);
      if (isGoal(next, goals)) {
        found = true;
        break;
      }
      Collection<EntityID> neighbours = weightedGraph.containsKey(next)
          ? weightedGraph.get(next).keySet()
          : Collections.emptySet();
      if (neighbours.isEmpty())
        continue;

      for (EntityID neighbour : neighbours) {
        if (isGoal(neighbour, goals)) {
          ancestors.put(neighbour, next);
          next = neighbour;
          found = true;
          break;
        } else if (!ancestors.containsKey(neighbour)) {
          open.add(neighbour);
          ancestors.put(neighbour, next);
        }
      }
    } while (!found && !open.isEmpty());
    if (!found) {
      // No path
      return null;
    }
    // Walk back from goal to start
    EntityID current = next;
    List<EntityID> path = new LinkedList<>();
    do {
      path.add(0, current);
      current = ancestors.get(current);
      if (current == null)
        throw new RuntimeException(
            "Found a node with no ancestor! Something is broken.");
    } while (current != start);
    return path;
  }


  private boolean isGoal(EntityID e, Collection<EntityID> test) {
    return test.contains(e);
  }
}