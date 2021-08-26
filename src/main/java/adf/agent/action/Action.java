package adf.agent.action;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.worldmodel.EntityID;

public abstract class Action {

  public Action() {
  }

  @Nonnull
  public abstract Message getCommand(@Nonnull EntityID agentID, int time);
}
