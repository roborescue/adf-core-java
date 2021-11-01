package adf.agent.action.common;

import javax.annotation.Nonnull;

import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKRest;
import rescuecore2.worldmodel.EntityID;

import adf.agent.action.Action;

public class ActionRest extends Action {

  public ActionRest() {
    super();
  }

  @Override
  @Nonnull
  public String toString() {
    return "ActionRest []";
  }

  @Override
  @Nonnull
  public Message getCommand(@Nonnull EntityID agentID, int time) {
    return new AKRest(agentID, time);
  }
}