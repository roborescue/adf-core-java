package adf.impl;

import adf.core.component.AbstractLoader;
import adf.core.component.tactics.TacticsAmbulanceCentre;
import adf.core.component.tactics.TacticsAmbulanceTeam;
import adf.core.component.tactics.TacticsFireBrigade;
import adf.core.component.tactics.TacticsFireStation;
import adf.core.component.tactics.TacticsPoliceForce;
import adf.core.component.tactics.TacticsPoliceOffice;
import adf.impl.tactics.DefaultTacticsAmbulanceCentre;
import adf.impl.tactics.DefaultTacticsAmbulanceTeam;
import adf.impl.tactics.DefaultTacticsFireBrigade;
import adf.impl.tactics.DefaultTacticsFireStation;
import adf.impl.tactics.DefaultTacticsPoliceForce;
import adf.impl.tactics.DefaultTacticsPoliceOffice;

public class DefaultLoader extends AbstractLoader {

  @Override
  public TacticsAmbulanceTeam getTacticsAmbulanceTeam() {
    return new DefaultTacticsAmbulanceTeam();
  }


  @Override
  public TacticsFireBrigade getTacticsFireBrigade() {
    return new DefaultTacticsFireBrigade();
  }


  @Override
  public TacticsPoliceForce getTacticsPoliceForce() {
    return new DefaultTacticsPoliceForce();
  }


  @Override
  public TacticsAmbulanceCentre getTacticsAmbulanceCentre() {
    return new DefaultTacticsAmbulanceCentre();
  }


  @Override
  public TacticsFireStation getTacticsFireStation() {
    return new DefaultTacticsFireStation();
  }


  @Override
  public TacticsPoliceOffice getTacticsPoliceOffice() {
    return new DefaultTacticsPoliceOffice();
  }
}