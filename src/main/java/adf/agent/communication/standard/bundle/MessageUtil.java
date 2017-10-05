package adf.agent.communication.standard.bundle;

import adf.agent.communication.standard.bundle.information.*;
import adf.agent.info.WorldInfo;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class MessageUtil {
	@Nullable
	public static StandardEntity reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull StandardMessage message) {
		StandardEntity entity = null;
		Set<EntityID> changedEntities = worldInfo.getChanged().getChangedEntities();
		Class<? extends StandardMessage> messageClass = message.getClass();

		if (messageClass == MessageCivilian.class) {
			MessageCivilian mc = (MessageCivilian) message;
			if (!changedEntities.contains(mc.getAgentID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mc);
			}
		} else if (messageClass == MessageAmbulanceTeam.class) {
			MessageAmbulanceTeam mat = (MessageAmbulanceTeam) message;
			if (!changedEntities.contains(mat.getAgentID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mat);
			}
		} else if (messageClass == MessageFireBrigade.class) {
			MessageFireBrigade mfb = (MessageFireBrigade) message;
			if (!changedEntities.contains(mfb.getAgentID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mfb);
			}
		} else if (messageClass == MessagePoliceForce.class) {
			MessagePoliceForce mpf = (MessagePoliceForce) message;
			if (!changedEntities.contains(mpf.getAgentID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mpf);
			}
		} else if (messageClass == MessageBuilding.class) {
			MessageBuilding mb = (MessageBuilding) message;
			if (!changedEntities.contains(mb.getBuildingID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mb);
			}
		} else if (messageClass == MessageRoad.class) {
			MessageRoad mr = (MessageRoad) message;
			if (!changedEntities.contains(mr.getRoadID())) {
				entity = MessageUtil.reflectMessage(worldInfo, mr);
			}
		}

		return entity;
	}

	@Nonnull
	public static Building reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageBuilding message) {
		Building building = (Building) worldInfo.getEntity(message.getBuildingID());
		if (building != null) {
			if (message.isFierynessDefined()) {
				building.setFieryness(message.getFieryness());
			}
			if (message.isBrokennessDefined()) {
				building.setBrokenness(message.getBrokenness());
			}
			if (message.isTemperatureDefined()) {
				building.setTemperature(message.getTemperature());
			}
		} else {
			building = new Building(message.getBuildingID());
			if (message.isFierynessDefined()) {
				building.setFieryness(message.getFieryness());
			}
			if (message.isBrokennessDefined()) {
				building.setBrokenness(message.getBrokenness());
			}
			if (message.isTemperatureDefined()) {
				building.setTemperature(message.getTemperature());
			}
			worldInfo.addEntity(building);
		}
		return building;
	}

	@Nullable
	public static Road reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageRoad message) {
		Road road = (Road) worldInfo.getEntity(message.getRoadID());

		if (message.getBlockadeID() == null) {
			return road;
		}

		Blockade blockade = (Blockade) worldInfo.getEntity(message.getBlockadeID());
		if (blockade != null) {
			blockade.setPosition(message.getRoadID());
			if (message.isRepairCostDefined()) {
				blockade.setRepairCost(message.getRepairCost());
			}
			if (message.isXDefined()) {
				blockade.setX(message.getBlockadeX());
			}
			if (message.isYDefined()) {
				blockade.setY(message.getBlockadeY());
			}
		} else {
			blockade = new Blockade(message.getBlockadeID());
			blockade.setPosition(message.getRoadID());
			if (message.isRepairCostDefined()) {
				blockade.setRepairCost(message.getRepairCost());
			}
			if (message.isXDefined()) {
				blockade.setX(message.getBlockadeX());
			}
			if (message.isYDefined()) {
				blockade.setY(message.getBlockadeY());
			}
			worldInfo.addEntity(blockade);
		}

		return road;
	}

	@Nonnull
	public static Civilian reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageCivilian message) {
		Civilian civilian = (Civilian) worldInfo.getEntity(message.getAgentID());
		if (civilian != null) {
			if (message.isHPDefined()) {
				civilian.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				civilian.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				civilian.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				civilian.setPosition(message.getPosition());
			}
		} else {
			civilian = new Civilian(message.getAgentID());
			if (message.isHPDefined()) {
				civilian.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				civilian.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				civilian.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				civilian.setPosition(message.getPosition());
			}
			worldInfo.addEntity(civilian);
		}
		return civilian;
	}

	@Nonnull
	public static AmbulanceTeam reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageAmbulanceTeam message) {
		AmbulanceTeam ambulanceteam = (AmbulanceTeam) worldInfo.getEntity(message.getAgentID());
		if (ambulanceteam != null) {
			if (message.isHPDefined()) {
				ambulanceteam.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				ambulanceteam.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				ambulanceteam.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				ambulanceteam.setPosition(message.getPosition());
			}
		} else {
			ambulanceteam = new AmbulanceTeam(message.getAgentID());
			if (message.isHPDefined()) {
				ambulanceteam.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				ambulanceteam.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				ambulanceteam.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				ambulanceteam.setPosition(message.getPosition());
			}
			worldInfo.addEntity(ambulanceteam);
		}
		return ambulanceteam;
	}

	@Nonnull
	public static FireBrigade reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessageFireBrigade message) {
		FireBrigade firebrigade = (FireBrigade) worldInfo.getEntity(message.getAgentID());
		if (firebrigade != null) {
			if (message.isHPDefined()) {
				firebrigade.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				firebrigade.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				firebrigade.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				firebrigade.setPosition(message.getPosition());
			}
			if (message.isWaterDefined()) {
				firebrigade.setWater(message.getWater());
			}
		} else {
			firebrigade = new FireBrigade(message.getAgentID());
			if (message.isHPDefined()) {
				firebrigade.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				firebrigade.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				firebrigade.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				firebrigade.setPosition(message.getPosition());
			}
			if (message.isWaterDefined()) {
				firebrigade.setWater(message.getWater());
			}
			worldInfo.addEntity(firebrigade);
		}
		return firebrigade;
	}

	@Nonnull
	public static PoliceForce reflectMessage(@Nonnull WorldInfo worldInfo, @Nonnull MessagePoliceForce message) {
		PoliceForce policeforce = (PoliceForce) worldInfo.getEntity(message.getAgentID());
		if (policeforce != null) {
			if (message.isHPDefined()) {
				policeforce.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				policeforce.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				policeforce.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				policeforce.setPosition(message.getPosition());
			}
		} else {
			policeforce = new PoliceForce(message.getAgentID());
			if (message.isHPDefined()) {
				policeforce.setHP(message.getHP());
			}
			if (message.isBuriednessDefined()) {
				policeforce.setBuriedness(message.getBuriedness());
			}
			if (message.isDamageDefined()) {
				policeforce.setDamage(message.getDamage());
			}
			if (message.isPositionDefined()) {
				policeforce.setPosition(message.getPosition());
			}
			worldInfo.addEntity(policeforce);
		}
		return policeforce;
	}
}

