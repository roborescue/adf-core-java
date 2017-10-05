package adf.agent.platoon;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsFireBrigade;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class PlatoonFire extends Platoon<FireBrigade> {
	public PlatoonFire(TacticsFireBrigade tactics, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		super(tactics, isPrecompute, DATASTORAGE_FILE_NAME_FIRE, isDebugMode, moduleConfig, developData);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
	}

	@Override
	protected void postConnect() {
		super.postConnect();
	}
}
