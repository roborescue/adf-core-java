package adf.impl.tactics.utils;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.MessageUtil;
import adf.core.agent.communication.standard.bundle.StandardMessage;
import adf.core.agent.communication.standard.bundle.StandardMessagePriority;
import adf.core.agent.communication.standard.bundle.centralized.CommandPolice;
import adf.core.agent.communication.standard.bundle.information.MessageBuilding;
import adf.core.agent.communication.standard.bundle.information.MessageCivilian;
import adf.core.agent.communication.standard.bundle.information.MessageRoad;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.component.communication.CommunicationMessage;
import rescuecore2.config.NoSuchConfigOptionException;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.*;

import static rescuecore2.standard.entities.StandardEntityURN.*;

/**
 * 消息工具类
 */
public class MessageTool {

    @SuppressWarnings("unused")
    private DevelopData developData;

    private int sendingAvoidTimeReceived;
    @SuppressWarnings("unused")
    private int sendingAvoidTimeSent;
    private int sendingAvoidTimeClearRequest;
    private int estimatedMoveDistance;

    private int maxTimeStep = Integer.MAX_VALUE;
    private Map<EntityID, Integer> prevBrokenessMap;
    private EntityID lastPosition;
    private int lastSentTime;
    private int stayCount;

    private Map<EntityID, Integer> receivedTimeMap;

    private Set<EntityID> agentsPotition;

    /**
     * 收到可通行道路
     */
    private Set<EntityID> receivedPassableRoads;

    /**
     * 支配代理 ID
     */
    private EntityID dominanceAgentID;

    public MessageTool(ScenarioInfo scenarioInfo, DevelopData developData) {
        this.developData = developData;

        // 初始化发送信息的时间间隔
        this.sendingAvoidTimeReceived = developData
                .getInteger("sample.tactics.MessageTool.sendingAvoidTimeReceived", 3);
        this.sendingAvoidTimeSent = developData
                .getInteger("sample.tactics.MessageTool.sendingAvoidTimeSent", 5);
        this.sendingAvoidTimeClearRequest = developData.getInteger(
                "sample.tactics.MessageTool.sendingAvoidTimeClearRequest", 5);
        // 初始化估计的移动距离
        this.estimatedMoveDistance = developData
                .getInteger("sample.tactics.MessageTool.estimatedMoveDistance", 40000);

        this.lastPosition = new EntityID(0);
        this.lastSentTime = 0;
        this.stayCount = 0;

        this.prevBrokenessMap = new HashMap<>();
        this.receivedTimeMap = new HashMap<>();
        this.agentsPotition = new HashSet<>();
        this.receivedPassableRoads = new HashSet<>();

        this.dominanceAgentID = new EntityID(0);
    }

    /**
     * 反映消息。
     * 将接收到的消息中的实体解析为对应的实体对象，并记录接收到消息的时间。
     *
     * @param agentInfo      代理信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   情境信息
     * @param messageManager 消息管理器
     */
    public void reflectMessage(AgentInfo agentInfo, WorldInfo worldInfo,
                               ScenarioInfo scenarioInfo, MessageManager messageManager) {
        Set<EntityID> changedEntities = worldInfo.getChanged().getChangedEntities();
        changedEntities.add(agentInfo.getID());
        int time = agentInfo.getTime();
        // 遍历接收到的消息列表
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(StandardMessage.class)) {
            StandardEntity entity = null;
            // 使用MessageUtil的reflectMessage方法解析消息，返回对应的实体对象
            entity = MessageUtil.reflectMessage(worldInfo, (StandardMessage) message);
            if (entity != null) {
                // 将实体对象的ID和接收到该消息的时间存入receivedTimeMap中
                this.receivedTimeMap.put(entity.getID(), time);
            }
        }
    }

    /**
     * 发送信息消息。
     * 根据位置的变化和实体的主导性，发送相关的信息消息。
     *
     * @param agentInfo      代理信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   情境信息
     * @param messageManager 消息管理器
     */
    @SuppressWarnings("incomplete-switch")
    public void sendInformationMessages(AgentInfo agentInfo, WorldInfo worldInfo,
                                        ScenarioInfo scenarioInfo, MessageManager messageManager) {
        Set<EntityID> changedEntities = worldInfo.getChanged().getChangedEntities();

        // 更新信息
        this.updateInfo(agentInfo, worldInfo, scenarioInfo, messageManager);

        // 如果位置已经移动并且当前实体为主导实体
        if (isPositionMoved(agentInfo) && isDominance(agentInfo)) {
            // 遍历变化的实体列表
            for (EntityID entityID : changedEntities) {
                // 如果该实体不是最近接收到的实体
                if (!(isRecentlyReceived(agentInfo, entityID))) {
                    StandardEntity entity = worldInfo.getEntity(entityID);
                    CommunicationMessage message = null;
                    // 根据实体的标准类型选择相应的消息类型
                    switch (entity.getStandardURN()) {
                        case ROAD:
                            Road road = (Road) entity;
                            if (isNonBlockadeAndNotReceived(road)) {
                                // 构建Road消息
                                message = new MessageRoad(true, StandardMessagePriority.LOW,
                                        road, null, true, false);
                            }
                            break;
                        case BUILDING:
                            Building building = (Building) entity;
                            if (isOnFireOrWaterDameged(building)) {
                                // 构建Building消息
                                message = new MessageBuilding(true, StandardMessagePriority.LOW,
                                        building);
                            }
                            break;
                        case CIVILIAN:
                            Civilian civilian = (Civilian) entity;
                            if (isUnmovalCivilian(civilian)) {
                                // 构建Civilian消息
                                message = new MessageCivilian(true, StandardMessagePriority.LOW,
                                        civilian);
                            }
                            break;
                    }

                    // 将消息添加到消息管理器中
                    messageManager.addMessage(message);
                }
            }
        }

        // 记录最后的位置
        recordLastPosition(agentInfo);
    }

    /**
     * 发送请求消息。
     * 如果实体为救援人员或消防队，根据实体的位置和状态发送请求消息。
     *
     * @param agentInfo      代理信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   情境信息
     * @param messageManager 消息管理器
     */
    public void sendRequestMessages(AgentInfo agentInfo, WorldInfo worldInfo,
                                    ScenarioInfo scenarioInfo, MessageManager messageManager) {
        // 如果实体为救援人员或消防队
        if (agentInfo.me().getStandardURN() == AMBULANCE_TEAM
                || agentInfo.me().getStandardURN() == FIRE_BRIGADE) {
            int currentTime = agentInfo.getTime();
            Human agent = (Human) agentInfo.me();
            int agentX = agent.getX();
            int agentY = agent.getY();
            StandardEntity positionEntity = worldInfo.getPosition(agent);
            if (positionEntity instanceof Road) {
                boolean isSendRequest = false;

                Road road = (Road) positionEntity;
                // 如果道路存在障碍物且有需要清理的
                if (road.isBlockadesDefined() && road.getBlockades().size() > 0) {
                    for (Blockade blockade : worldInfo.getBlockades(road)) {
                        if (blockade == null || !blockade.isApexesDefined()) {
                            continue;
                        }

                        // 如果当前位置在障碍物的范围内，设置isSendRequest为true
                        if (this.isInside(agentX, agentY, blockade.getApexes())) {
                            isSendRequest = true;
                        }
                    }
                }

                // 如果停留在同一个道路上且超过了最大旅行时间，设置isSendRequest为true
                if (this.lastPosition != null
                        && this.lastPosition.getValue() == road.getID().getValue()) {
                    this.stayCount++;
                    if (this.stayCount > this.getMaxTravelTime(road)) {
                        isSendRequest = true;
                    }
                } else {
                    this.lastPosition = road.getID();
                    this.stayCount = 0;
                }

                // 如果需要发送请求且距离上次发送请求的时间超过了规定的时间间隔
                if (isSendRequest && ((currentTime
                        - this.lastSentTime) >= this.sendingAvoidTimeClearRequest)) {
                    this.lastSentTime = currentTime;
                    // 构建CommandPolice消息
                    messageManager.addMessage(new CommandPolice(true, null,
                            agent.getPosition(), CommandPolice.ACTION_CLEAR));
                }
            }
        }
    }

    /**
     * 更新信息。
     * 根据最新的信息，更新内部记录的状态和数据。
     *
     * @param agentInfo      代理信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   情境信息
     * @param messageManager 消息管理器
     */
    private void updateInfo(AgentInfo agentInfo, WorldInfo worldInfo,
                            ScenarioInfo scenarioInfo, MessageManager messageManager) {
        // 如果最大的时间步数还没有被初始化，则从情境信息中获取并设置最大时间步数
        if (this.maxTimeStep == Integer.MAX_VALUE) {
            try {
                this.maxTimeStep = scenarioInfo.getKernelTimesteps();
            } catch (NoSuchConfigOptionException e) {
            }
        }

        // 清空agentsPotition集合，并将当前实体ID设置为主导实体ID
        this.agentsPotition.clear();
        this.dominanceAgentID = agentInfo.getID();

        // 遍历所有的人类实体，并将它们的位置添加到agentsPotition集合中
        for (StandardEntity entity : worldInfo.getEntitiesOfType(AMBULANCE_TEAM,
                FIRE_BRIGADE, POLICE_FORCE)) {
            Human human = (Human) entity;
            this.agentsPotition.add(human.getPosition());
            // 如果当前实体位置等于human的位置并且当前实体ID小于human的ID，则将human的ID设置为主导实体ID
            if (agentInfo.getPosition().equals(human.getPosition())
                    && dominanceAgentID.getValue() < entity.getID().getValue()) {
                this.dominanceAgentID = entity.getID();
            }
        }

        boolean aftershock = false;
        // 遍历变化的实体列表
        for (EntityID id : agentInfo.getChanged().getChangedEntities()) {
            // 如果该实体ID在prevBrokenessMap中存在且是BUILDING类型的实体
            if (this.prevBrokenessMap.containsKey(id)
                    && worldInfo.getEntity(id).getStandardURN().equals(BUILDING)) {
                Building building = (Building) worldInfo.getEntity(id);
                int brokenness = this.prevBrokenessMap.get(id);
                // 如果建筑物的破损程度大于之前记录的破损程度，则设置aftershock为true
                if (building.isBrokennessDefined()) {
                    if (building.getBrokenness() > brokenness) {
                        aftershock = true;
                    }
                }
            }
        }
        // 清空prevBrokenessMap集合，并遍历变化的实体列表，将BUILDING类型的实体的破损程度记录到prevBrokenessMap中
        this.prevBrokenessMap.clear();
        for (EntityID id : agentInfo.getChanged().getChangedEntities()) {
            if (!worldInfo.getEntity(id).getStandardURN().equals(BUILDING)) {
                continue;
            }
            Building building = (Building) worldInfo.getEntity(id);
            if (building.isBrokennessDefined()) {
                this.prevBrokenessMap.put(id, building.getBrokenness());
            }
        }
        // 如果出现了余震，则清空receivedPassableRoads集合
        if (aftershock) {
            this.receivedPassableRoads.clear();
        }

        // 根据收到的Road消息更新receivedPassableRoads集合
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(MessageRoad.class)) {
            MessageRoad messageRoad = (MessageRoad) message;
            Boolean passable = messageRoad.isPassable();
            if (passable != null && passable) {
                this.receivedPassableRoads.add(messageRoad.getRoadID());
            }
        }
    }

    /**
     * 判断实体位置是否发生移动。
     *
     * @param agentInfo 代理信息
     * @return 如果实体位置发生移动返回true，否则返回false
     */
    private boolean isPositionMoved(AgentInfo agentInfo) {
        return !(agentInfo.getID().equals(lastPosition));
    }

    /**
     * 判断实体是否为主导实体。
     *
     * @param agentInfo 代理信息
     * @return 如果实体为主导实体返回true，否则返回false
     */
    private boolean isDominance(AgentInfo agentInfo) {
        return agentInfo.getID().equals(this.dominanceAgentID);
    }

    /**
     * 判断实体对应的消息是否在规定的时间范围内接收到。
     *
     * @param agentInfo 代理信息
     * @param id        实体ID
     * @return 如果消息在时间范围内接收到返回true，否则返回false
     */
    private boolean isRecentlyReceived(AgentInfo agentInfo, EntityID id) {
        return (this.receivedTimeMap.containsKey(id) && ((agentInfo.getTime()
                - this.receivedTimeMap.get(id)) < this.sendingAvoidTimeReceived));
    }

    /**
     * 判断道路是否没有障碍物且该道路的消息没有接收到。
     *
     * @param road 道路实体
     * @return 如果道路没有障碍物且该道路的消息没有接收到返回true，否则返回false
     */
    private boolean isNonBlockadeAndNotReceived(Road road) {
        if ((!road.isBlockadesDefined())
                || (road.isBlockadesDefined() && (road.getBlockades().size() <= 0))) {
            if (!(this.receivedPassableRoads.contains(road.getID()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断建筑物是否着火或者水坏了。
     *
     * @param building 建筑物实体
     * @return 如果建筑物着火或者水坏了返回true，否则返回false
     */
    private boolean isOnFireOrWaterDameged(Building building) {
        final List<StandardEntityConstants.Fieryness> ignoreFieryness = Arrays
                .asList(StandardEntityConstants.Fieryness.UNBURNT,
                        StandardEntityConstants.Fieryness.BURNT_OUT);

        if (building.isFierynessDefined()
                && ignoreFieryness.contains(building.getFierynessEnum())) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为无法转移的平民。
     *
     * @param civilian 平民实体
     * @return 如果平民无法转移返回true，否则返回false
     */
    private boolean isUnmovalCivilian(Civilian civilian) {
        return civilian.isDamageDefined() && (civilian.getDamage() > 0);
    }

    /**
     * 记录最后的位置。
     *
     * @param agentInfo 代理信息
     */
    private void recordLastPosition(AgentInfo agentInfo) {
        this.lastPosition = agentInfo.getPosition();
    }

    /**
     * 判断坐标点是否在多边形内部。
     *
     * @param pX   坐标点的X坐标
     * @param pY   坐标点的Y坐标
     * @param apex 多边形的顶点坐标数组
     * @return 如果坐标点在多边形内部返回true，否则返回false
     */
    private boolean isInside(double pX, double pY, int[] apex) {
        Point2D p = new Point2D(pX, pY);
        Vector2D v1 = (new Point2D(apex[apex.length - 2], apex[apex.length - 1]))
                .minus(p);
        Vector2D v2 = (new Point2D(apex[0], apex[1])).minus(p);
        double theta = this.getAngle(v1, v2);

        for (int i = 0; i < apex.length - 2; i += 2) {
            v1 = (new Point2D(apex[i], apex[i + 1])).minus(p);
            v2 = (new Point2D(apex[i + 2], apex[i + 3])).minus(p);
            theta += this.getAngle(v1, v2);
        }
        return Math.round(Math.abs((theta / 2) / Math.PI)) >= 1;
    }

    /**
     * 计算两个向量之间的夹角。
     *
     * @param v1 向量1
     * @param v2 向量2
     * @return 两个向量之间的夹角
     */
    private double getAngle(Vector2D v1, Vector2D v2) {
        double flag = (v1.getX() * v2.getY()) - (v1.getY() * v2.getX());
        double angle = Math.acos(((v1.getX() * v2.getX()) + (v1.getY() * v2.getY()))
                / (v1.getLength() * v2.getLength()));
        if (flag > 0) {
            return angle;
        }
        if (flag < 0) {
            return -1 * angle;
        }
        return 0.0D;
    }

    /**
     * 获取最大旅行时间。
     *
     * @param area 区域实体
     * @return 最大旅行时间
     */
    private int getMaxTravelTime(Area area) {
        int distance = 0;
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : area.getEdges()) {
            if (edge.isPassable()) {
                edges.add(edge);
            }
        }
        if (edges.size() <= 1) {
            return this.maxTimeStep;
        }
        for (int i = 0; i < edges.size(); i++) {
            for (int j = 0; j < edges.size(); j++) {
                if (i != j) {
                    Edge edge1 = edges.get(i);
                    double midX1 = (edge1.getStartX() + edge1.getEndX()) / 2;
                    double midY1 = (edge1.getStartY() + edge1.getEndY()) / 2;
                    Edge edge2 = edges.get(j);
                    double midX2 = (edge2.getStartX() + edge2.getEndX()) / 2;
                    double midY2 = (edge2.getStartY() + edge2.getEndY()) / 2;
                    int d = this.getDistance(midX1, midY1, midX2, midY2);
                    if (distance < d) {
                        distance = d;
                    }
                }
            }
        }

        if (distance > 0) {
            return 1
                    + (int) Math.ceil(distance / (double) this.estimatedMoveDistance);
        }

        return this.maxTimeStep;
    }

    /**
     * 计算两点之间的距离。
     *
     * @param fromX 起点的X坐标
     * @param fromY 起点的Y坐标
     * @param toX   终点的X坐标
     * @param toY   终点的Y坐标
     * @return 两点之间的距离
     */
    private int getDistance(double fromX, double fromY, double toX, double toY) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        return (int) Math.hypot(dx, dy);
    }
}