package adf.agent.action.ambulance;

import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.messages.AKRescue;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ActionRescue extends Action {
	protected EntityID target;

	public ActionRescue(@Nonnull EntityID targetID) {
		super();
		this.target = targetID;
	}

	public ActionRescue(@Nonnull Human human) {
		this(human.getID());
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionRescue [target=" + target + "]";
	}

	@Nonnull
	public EntityID getTarget() {
		return this.target;
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		return new AKRescue(agentID, time, this.target);
	}
}
