package adf.agent.action.ambulance;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.messages.control.AKCommand;
import rescuecore2.standard.commands.AKLoad;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.worldmodel.EntityID;

import adf.agent.action.Action;

public class ActionLoad extends Action {

  protected EntityID target;

  public ActionLoad(@Nonnull EntityID targetID) {
    super();
    this.target = targetID;
  }

  public ActionLoad(@Nonnull Civilian civilian) {
    this(civilian.getID());
  }

  @Override
  @Nonnull
  public String toString() {
    return "ActionLoad [target=" + target + "]";
  }

  @Nonnull
  public EntityID getTarget() {
    return this.target;
  }

  @Override
  @Nonnull
  public Message getCommand(@Nonnull EntityID agentID, int time) {
    return new AKCommand(new AKLoad(agentID, time, this.target));
  }
}
