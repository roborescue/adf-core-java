package adf.agent.action.police;

import adf.agent.info.AgentInfo;
import adf.agent.platoon.Platoon;
import adf.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.misc.geometry.Vector2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.messages.AKClear;
import rescuecore2.standard.messages.AKClearArea;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ActionClear extends Action {
	protected EntityID target;
	private boolean useOldFunction;
	private int posX;
	private int posY;

	public ActionClear(@Nonnull EntityID targetID) {
		super();
		this.target = targetID;
		this.useOldFunction = true;
	}

	public ActionClear(@Nonnull Blockade blockade) {
		this(blockade.getID());
	}

	public ActionClear(@Nonnull AgentInfo agent, @Nonnull Vector2D vector) {
		this((int) (agent.getX() + vector.getX()), (int) (agent.getY() + vector.getY()));
	}

	public ActionClear(int destX, int destY) {
		super();
		this.useOldFunction = false;
		this.posX = destX;
		this.posY = destY;
	}

	public ActionClear(int destX, int destY, @Nonnull Blockade blockade) {
		this(destX, destY);
		this.target = blockade.getID();
	}

	@Override
	@Nonnull
	public String toString() {
		return "ActionClear [target=" + target + ", useOldFunction=" + useOldFunction + ", posX=" + posX + ", posY=" + posY + "]";
	}

	public boolean getUseOldFunction() {
		return this.useOldFunction;
	}

	@Nullable
	public EntityID getTarget() {
		return this.target;
	}

	public int getPosX() {
		return this.posX;
	}

	public int getPosY() {
		return this.posY;
	}

	@Override
	@Nonnull
	public Message getCommand(@Nonnull EntityID agentID, int time) {
		if (this.useOldFunction) {
			return new AKClear(agentID, time, this.target);
		} else {
			return new AKClearArea(agentID, time, this.posX, this.posY);
		}
	}
}
