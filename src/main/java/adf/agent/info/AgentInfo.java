package adf.agent.info;

import adf.agent.Agent;
import adf.agent.action.Action;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AgentInfo {
	private Agent agent;
	private StandardWorldModel world;
	private int time;
	private ChangeSet changed;
	private Collection<Command> heard;
	private long thinkStartTime;

	private Map<Integer, Action> actionHistory;

	public AgentInfo(@Nonnull Agent agent, @Nonnull StandardWorldModel world) {
		this.agent = Objects.requireNonNull(agent);
		this.world = Objects.requireNonNull(world);
		this.time = 0;
		this.actionHistory = new HashMap<>();
		recordThinkStartTime();
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getTime() {
		return this.time;
	}

	public void setHeard(@Nonnull Collection<Command> heard) {
		this.heard = heard;
	}

	@Nullable
	public Collection<Command> getHeard() {
		return this.heard;
	}

	@Nonnull
	public EntityID getID() {
		return agent.getID();
	}

	@Nonnull
	public StandardEntity me() {
		return this.world.getEntity(this.agent.getID());
	}

	public double getX() {
		return agent.getX();
	}

	public double getY() {
		return agent.getY();
	}

	@Nonnull
	public EntityID getPosition() {
		StandardEntity entity = this.world.getEntity(this.agent.getID());
		return (entity instanceof Human) ? ((Human) entity).getPosition() : entity.getID();
	}

	@Nonnull
	public Area getPositionArea() {
		return (Area) this.world.getEntity(this.getPosition());
	}

	public void setChanged(@Nonnull ChangeSet changed) {
		this.changed = changed;
	}

	@Nullable
	public ChangeSet getChanged() {
		return this.changed;
	}

	@Nullable
	public Human someoneOnBoard() {
		EntityID id = this.agent.getID();
		for (StandardEntity next : this.world.getEntitiesOfType(StandardEntityURN.CIVILIAN)) {
			Human human = (Human) next;
			if (human.getPosition().equals(id)) {
				return human;
			}
		}
		return null;
	}

	public boolean isWaterDefined() {
		StandardEntity entity = this.world.getEntity(this.agent.getID());
		return entity.getStandardURN().equals(StandardEntityURN.FIRE_BRIGADE) && ((FireBrigade) entity).isWaterDefined();
	}

	public int getWater() {
		StandardEntity entity = this.world.getEntity(this.agent.getID());
		if (entity.getStandardURN().equals(StandardEntityURN.FIRE_BRIGADE)) {
			return ((FireBrigade) entity).getWater();
		}
		return 0;
	}

	@Nullable
	public Action getExecutedAction(int time) {
		if (time > 0) return this.actionHistory.get(time);
		return this.actionHistory.get(this.getTime() + time);
	}

	public void setExecutedAction(int time, @Nullable Action action) {
		this.actionHistory.put(time > 0 ? time : this.getTime() + time, action);
	}

	public void recordThinkStartTime() {
		this.thinkStartTime = System.currentTimeMillis();
	}

	public long getThinkTimeMillis() {
		return (System.currentTimeMillis() - this.thinkStartTime);
	}
}

