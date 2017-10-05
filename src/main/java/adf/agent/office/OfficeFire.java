package adf.agent.office;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsFireStation;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class OfficeFire extends Office<Building> {
	public OfficeFire(TacticsFireStation tacticsFireStation, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		super(tacticsFireStation, isPrecompute, DATASTORAGE_FILE_NAME_FIRE, isDebugMode, moduleConfig, developData);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.FIRE_STATION);
	}

	@Override
	protected void postConnect() {
		super.postConnect();
	}
}
