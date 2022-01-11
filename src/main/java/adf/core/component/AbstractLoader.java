package adf.core.component;

import adf.core.component.tactics.TacticsAmbulanceCentre;
import adf.core.component.tactics.TacticsAmbulanceTeam;
import adf.core.component.tactics.TacticsFireBrigade;
import adf.core.component.tactics.TacticsFireStation;
import adf.core.component.tactics.TacticsPoliceForce;
import adf.core.component.tactics.TacticsPoliceOffice;

public abstract class AbstractLoader {

  private String teamName;

  public String getTeamName() {
    return this.teamName;
  }


  public void setTeamName(String teamName) {
    this.teamName = teamName;
  }


  public abstract TacticsAmbulanceTeam getTacticsAmbulanceTeam();

  public abstract TacticsFireBrigade getTacticsFireBrigade();

  public abstract TacticsPoliceForce getTacticsPoliceForce();

  public abstract TacticsAmbulanceCentre getTacticsAmbulanceCentre();

  public abstract TacticsFireStation getTacticsFireStation();

  public abstract TacticsPoliceOffice getTacticsPoliceOffice();
}