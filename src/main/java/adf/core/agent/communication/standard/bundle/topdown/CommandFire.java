package adf.core.agent.communication.standard.bundle.topdown;

import adf.core.component.communication.util.BitStreamReader;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name
 * {@link adf.core.agent.communication.standard.bundle.centralized.CommandFire}
 */
@Deprecated
public class CommandFire extends
    adf.core.agent.communication.standard.bundle.centralized.CommandFire {

  public CommandFire(boolean isRadio, EntityID toID, EntityID targetID, int action) {
    super(isRadio, toID, targetID, action);
  }


  public CommandFire(boolean isRadio, int from, int ttl, BitStreamReader bitStreamReader) {
    super(isRadio, from, ttl, bitStreamReader);
  }
}