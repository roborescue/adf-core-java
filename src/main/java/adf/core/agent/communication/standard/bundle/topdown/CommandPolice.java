package adf.core.agent.communication.standard.bundle.topdown;

import adf.core.component.communication.util.BitStreamReader;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name
 * {@link adf.core.agent.communication.standard.bundle.centralized.CommandPolice}
 */
@Deprecated
public class CommandPolice extends
    adf.core.agent.communication.standard.bundle.centralized.CommandPolice {

  public CommandPolice(boolean isRadio, EntityID toID, EntityID targetID, int action) {
    super(isRadio, toID, targetID, action);
  }


  public CommandPolice(boolean isRadio, int from, int ttl, BitStreamReader bitStreamReader) {
    super(isRadio, from, ttl, bitStreamReader);
  }
}