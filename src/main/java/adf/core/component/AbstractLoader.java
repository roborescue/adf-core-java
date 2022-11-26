package adf.core.component;

import adf.core.component.tactics.*;

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