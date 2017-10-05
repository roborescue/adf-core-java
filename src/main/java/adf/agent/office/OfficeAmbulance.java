package adf.agent.office;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsAmbulanceCentre;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntityURN;

import java.util.EnumSet;

public class OfficeAmbulance extends Office<Building> {
	public OfficeAmbulance(TacticsAmbulanceCentre tacticsAmbulanceCenter, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		super(tacticsAmbulanceCenter, isPrecompute, DATASTORAGE_FILE_NAME_AMBULANCE, isDebugMode, moduleConfig, developData);
	}

	@Override
	protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
		return EnumSet.of(StandardEntityURN.AMBULANCE_CENTRE);
	}

	@Override
	protected void postConnect() {
		super.postConnect();
	}
}
