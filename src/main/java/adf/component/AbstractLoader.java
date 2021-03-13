package adf.component;

import adf.component.tactics.TacticsAmbulanceTeam;
import adf.component.tactics.TacticsAmbulanceCentre;
import adf.component.tactics.TacticsFireBrigade;
import adf.component.tactics.TacticsFireStation;
import adf.component.tactics.TacticsPoliceForce;
import adf.component.tactics.TacticsPoliceOffice;

abstract public class AbstractLoader {

  abstract public String getTeamName();

  abstract public TacticsAmbulanceTeam getTacticsAmbulanceTeam();

  abstract public TacticsFireBrigade getTacticsFireBrigade();

  abstract public TacticsPoliceForce getTacticsPoliceForce();

  abstract public TacticsAmbulanceCentre getTacticsAmbulanceCentre();

  abstract public TacticsFireStation getTacticsFireStation();

  abstract public TacticsPoliceOffice getTacticsPoliceOffice();
}
