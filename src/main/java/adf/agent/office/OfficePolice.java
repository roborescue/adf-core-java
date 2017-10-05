package adf.agent.office;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsPoliceOffice;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class OfficePolice extends Office<Building> {
	public OfficePolice(TacticsPoliceOffice tacticsPoliceOffice, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		super(tacticsPoliceOffice, isPrecompute, DATASTORAGE_FILE_NAME_POLICE, isDebugMode, moduleConfig, developData);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.POLICE_OFFICE);
	}

	@Override
	protected void postConnect() {
		super.postConnect();
	}
}
