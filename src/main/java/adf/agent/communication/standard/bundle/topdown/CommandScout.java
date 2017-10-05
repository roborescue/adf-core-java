package adf.agent.communication.standard.bundle.topdown;

import adf.component.communication.util.BitStreamReader;
import rescuecore2.worldmodel.EntityID;

/**
 * @deprecated change class name {@link adf.agent.communication.standard.bundle.centralized.CommandScout}
 */
@Deprecated
public class CommandScout extends adf.agent.communication.standard.bundle.centralized.CommandScout {
	public CommandScout(boolean isRadio, EntityID toID, EntityID targetID, int range) {
		super(isRadio, toID, targetID, range);
	}

	public CommandScout(boolean isRadio, int from, int ttl, BitStreamReader bitStreamReader) {
		super(isRadio, from, ttl, bitStreamReader);
	}
}
