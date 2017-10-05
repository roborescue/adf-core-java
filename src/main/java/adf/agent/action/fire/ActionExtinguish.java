package adf.agent.action.fire;

import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.messages.AKExtinguish;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ActionExtinguish extends Action {

	protected EntityID target;
	private int power;

	public ActionExtinguish(@Nonnull EntityID targetID, int maxPower) {
		super();
		this.target = targetID;
		this.power = maxPower;
	}

	public ActionExtinguish(@Nonnull Building building, int maxPower) {
		this(building.getID(), maxPower);
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionExtinguish [target=" + target + ", power=" + power + "]";
	}

	public int getPower() {
		return this.power;
	}

	@Nonnull
	public EntityID getTarget() {
		return this.target;
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		return new AKExtinguish(agentID, time, this.target, this.power);
	}
}
