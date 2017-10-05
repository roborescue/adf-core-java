package adf.component;

import adf.component.tactics.*;

abstract public class AbstractLoader {
	abstract public String getTeamName();

	abstract public TacticsAmbulanceTeam getTacticsAmbulanceTeam();

	abstract public TacticsFireBrigade getTacticsFireBrigade();

	abstract public TacticsPoliceForce getTacticsPoliceForce();

	abstract public TacticsAmbulanceCentre getTacticsAmbulanceCentre();

	abstract public TacticsFireStation getTacticsFireStation();

	abstract public TacticsPoliceOffice getTacticsPoliceOffice();
}
