package adf.impl.module.algorithm;

import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.PathPlanning;
import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.*;

/**
 * 基于A*的路径规划算法
 * <p>
 *
 * 调用流程:{@link #setFrom(EntityID)} -> {@link #setDestination(Collection)} -> {@link #calc()} -> {@link #getResult()}
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class AStarPathPlanning extends PathPlanning {

    private Map<EntityID, Set<EntityID>> graph;

    /**
     * 起点的EntityID
     */
    private EntityID from;
    /**
     * 目的地的EntityID
     */
    private Collection<EntityID> targets;
    /**
     * 计算出的路径的EntityID的列表
     */
    private List<EntityID> result;

    /**
     * {@link AStarPathPlanning}的构造函数
     *
     * @param agentInfo     代理信息
     * @param worldInfo     世界信息
     * @param ScenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public AStarPathPlanning(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo ScenarioInfo, ModuleManager moduleManager, DevelopData developData) {
        super(agentInfo, worldInfo, ScenarioInfo, moduleManager, developData);
        this.init();
    }


    private void init() {
        Map<EntityID,
                Set<EntityID>> neighbours = new LazyMap<EntityID, Set<EntityID>>() {

            @Override
            public Set<EntityID> createValue() {
                return new HashSet<>();
            }
        };
        for (Entity next : this.worldInfo) {
            if (next instanceof Area) {
                Collection<EntityID> areaNeighbours = ((Area) next).getNeighbours();
                neighbours.get(next.getID()).addAll(areaNeighbours);
            }
        }
        this.graph = neighbours;
    }

    /**
     * 获取路径
     *
     * @return 路径的EntityID的列表
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public List<EntityID> getResult() {
        return this.result;
    }

    /**
     * 设置起点
     *
     * @param id 起点的EntityID
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning setFrom(EntityID id) {
        this.from = id;
        return this;
    }

    /**
     * 设置目的地
     *
     * @param targets 目标的EntityID的集合
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning setDestination(Collection<EntityID> targets) {
        this.targets = targets;
        return this;
    }

    /**
     * 预计算时执行的方法
     * <p>
     * 仅重写了这个方法
     *
     * @param precomputeData 预计算数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning precompute(PrecomputeData precomputeData) {
        super.precompute(precomputeData);
        return this;
    }

    /**
     * 预计算模式的初始化处理方法
     * <p>
     * 仅重写了这个方法
     *
     * @param precomputeData 预计算数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning resume(PrecomputeData precomputeData) {
        super.resume(precomputeData);
        return this;
    }

    /**
     * 无预计算模式的初始化处理方法
     * <p>
     * 仅重写了这个方法
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning preparate() {
        super.preparate();
        return this;
    }

    /**
     * 计算路径
     *
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public PathPlanning calc() {
        // 1
        List<EntityID> open = new LinkedList<>();
        List<EntityID> close = new LinkedList<>();
        Map<EntityID, Node> nodeMap = new HashMap<>();

        // 3
        open.add(this.from);
        nodeMap.put(this.from, new Node(null, this.from));
        close.clear();

        while (true) {
            // 4
            if (open.size() < 0) {
                this.result = null;
                return this;
            }

            // 5
            Node n = null;
            for (EntityID id : open) {
                Node node = nodeMap.get(id);

                if (n == null) {
                    n = node;
                } else if (node.estimate() < n.estimate()) {
                    n = node;
                }
            }

            // 6
            if (targets.contains(n.getID())) {
                // 9
                List<EntityID> path = new LinkedList<>();
                while (n != null) {
                    path.add(0, n.getID());
                    n = nodeMap.get(n.getParent());
                }

                this.result = path;
                return this;
            }
            open.remove(n.getID());
            close.add(n.getID());

            // 7
            Collection<EntityID> neighbours = this.graph.get(n.getID());
            for (EntityID neighbour : neighbours) {
                Node m = new Node(n, neighbour);

                if (!open.contains(neighbour) && !close.contains(neighbour)) {
                    open.add(m.getID());
                    nodeMap.put(neighbour, m);
                } else if (open.contains(neighbour)
                        && m.estimate() < nodeMap.get(neighbour).estimate()) {
                    nodeMap.put(neighbour, m);
                } else if (!close.contains(neighbour)
                        && m.estimate() < nodeMap.get(neighbour).estimate()) {
                    nodeMap.put(neighbour, m);
                }
            }
        }
    }

    /**
     * 节点类
     *
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private class Node {

        EntityID id;
        EntityID parent;

        double cost;
        double heuristic;

        public Node(Node from, EntityID id) {
            this.id = id;

            if (from == null) {
                this.cost = 0;
            } else {
                this.parent = from.getID();
                this.cost = from.getCost() + worldInfo.getDistance(from.getID(), id);
            }

            this.heuristic = worldInfo.getDistance(id,
                    targets.toArray(new EntityID[targets.size()])[0]);
        }


        public EntityID getID() {
            return id;
        }


        public double getCost() {
            return cost;
        }


        public double estimate() {
            return cost + heuristic;
        }


        public EntityID getParent() {
            return this.parent;
        }
    }
}