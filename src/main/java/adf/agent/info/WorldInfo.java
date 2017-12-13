package adf.agent.info;

import adf.component.module.complex.BuildingDetector;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;
import java.lang.reflect.InvocationTargetException;

import static rescuecore2.standard.entities.StandardEntityURN.*;

public class WorldInfo implements Iterable<StandardEntity> {
	private StandardWorldModel world;
	private ChangeSet changed;
	private int time;

	private Map<EntityID, Map<Integer, Map<String, Object>>> rollback;
	private boolean runRollback;

	public WorldInfo(@Nonnull StandardWorldModel world) {
		this.setWorld(world);
		this.time = -1;
		this.runRollback = Boolean.FALSE;
		this.rollback = new HashMap<>();
	}

	// agent init ///////////////////////////////////////////////////////////////////////////////////////////////

	public void indexClass(StandardEntityURN... urns) {
		this.world.indexClass(urns);
	}

	public void index() {
		this.world.index();
	}

	@Nonnull
	public WorldInfo requestRollback() {
		this.runRollback = Boolean.TRUE;
		return this;
	}

	// get Flag ////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @deprecated change method name {@link #isRequestedRollback()}
	 * @return boolean
	 */
	@Deprecated
	public boolean needRollback() {
		return this.runRollback;
	}

	public boolean isRequestedRollback() {
		return this.runRollback;
	}


	// getEntity ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@CheckForNull
	public StandardEntity getEntity(@Nonnull EntityID entityID) {
		return this.world.getEntity(entityID);
	}

	@CheckForNull
	public StandardEntity getEntity(int targetTime, @Nonnull EntityID entityID) {
		return this.getEntity(targetTime, Objects.requireNonNull(this.getEntity(entityID)));

	}

	@CheckForNull
	public StandardEntity getEntity(int targetTime, @Nonnull StandardEntity entity) {
		if (targetTime <= 0) {
			targetTime = this.time + targetTime;
		}
		Map<String, Object> rollbackProperties = new HashMap<>();
		Map<Integer, Map<String, Object>> entityHistory = this.rollback.get(entity.getID());
		if (entityHistory == null) {
			return (StandardEntity) entity.copy();
		}

		for (int i = this.time - 1; i >= targetTime; i--) {
			Map<String, Object> changeProperties = entityHistory.get(i);
			if (changeProperties == null) {
				continue;
			}
			rollbackProperties.putAll(changeProperties);
		}

		Set<Property> propertySet = entity.getProperties();
		if (rollbackProperties.size() >= propertySet.size()) {
			boolean notExist = true;
			for (Property property : propertySet) {
				String key = property.getURN();
				if (rollbackProperties.containsKey(key)) {
					if (rollbackProperties.get(key) != null) {
						notExist = false;
						break;
					}
				}
			}
			if (notExist) {
				return null;
			}
		}

		if (entity.getStandardURN() == FIRE_BRIGADE) {
			return this.createRollbackFireBrigade(entity, rollbackProperties);
		}
		if (entity instanceof Human) {
			return this.createRollbackHuman(entity, rollbackProperties);
		}
		if (entity instanceof Building) {
			return this.createRollbackBuilding(entity, rollbackProperties);
		}
		if (entity instanceof Road) {
			return this.createRollbackRoad(entity, rollbackProperties);
		}
		if (entity.getStandardURN() == BLOCKADE) {
			return this.createRollbackBlockade(entity, rollbackProperties);
		}
		if (entity.getStandardURN() == WORLD) {
			return this.createRollbackWorld(entity, rollbackProperties);
		}
		return null;
	}

	// getEntityOfType /////////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public Collection<StandardEntity> getEntitiesOfType(@Nonnull StandardEntityURN urn) {
		return this.world.getEntitiesOfType(urn);
	}

	@Nonnull
	public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN... urns) {
		return this.world.getEntitiesOfType(urns);
	}

	@Nonnull
	public Collection<StandardEntity> getEntitiesOfType(int targetTime, @Nonnull StandardEntityURN urn) {
		return this.world.getEntitiesOfType(urn)
			.stream()
			.map(entity -> this.getEntity(targetTime, entity))
			.collect(Collectors.toCollection(HashSet::new));
	}

	@Nonnull
	public Collection<StandardEntity> getEntitiesOfType(int targetTime, StandardEntityURN... urns) {
		return this.world.getEntitiesOfType(urns)
			.stream()
			.map(entity -> this.getEntity(targetTime, entity))
			.collect(Collectors.toCollection(HashSet::new));
	}

	@Nonnull
	public Collection<EntityID> getEntityIDsOfType(@Nonnull StandardEntityURN urn) {
		return this.convertToID(this.world.getEntitiesOfType(urn));
	}

	@Nonnull
	public Collection<EntityID> getEntityIDsOfType(StandardEntityURN... urns) {
		return this.convertToID(this.world.getEntitiesOfType(urns));
	}

	@Nonnull
	public Collection<EntityID> getEntityIDsOfType(int targetTime, @Nonnull StandardEntityURN urn) {
		return this.convertToID(this.getEntitiesOfType(targetTime, urn));
	}

	@Nonnull
	public Collection<EntityID> getEntityIDsOfType(int targetTime, StandardEntityURN... urns) {
		return this.convertToID(this.getEntitiesOfType(targetTime, urns));
	}

	// getObjectsInRange ///////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(@Nonnull EntityID entityID, @Nonnegative int range) {
		return this.world.getObjectsInRange(entityID, range);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(@Nonnull StandardEntity entity, @Nonnegative int range) {
		return this.world.getObjectsInRange(entity, range);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int x, int y, @Nonnegative int range) {
		return this.world.getObjectsInRange(x, y, range);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRectangle(int x1, int y1, int x2, int y2) {
		return this.world.getObjectsInRectangle(x1, y1, x2, y2);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull EntityID entityID, @Nonnegative int range) {
		return this.getObjectsInRange(targetTime, entityID, range, false);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull EntityID entityID, @Nonnegative int range, boolean ignoreHuman) {
		return this.getObjectsInRange(targetTime, Objects.requireNonNull(this.getEntity(entityID)), range, ignoreHuman);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull StandardEntity entity, @Nonnegative int range) {
		return this.getObjectsInRange(targetTime, entity, range, false);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, @Nonnull StandardEntity entity, @Nonnegative int range, boolean ignoreHuman) {
		Pair<Integer, Integer> location = this.getLocation(entity);
		if (location == null) return new HashSet<>();

		return this.getObjectsInRange(targetTime, location.first(), location.second(), range, ignoreHuman);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, int x, int y, @Nonnegative int range) {
		return this.getObjectsInRange(targetTime, x, y, range, false);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRange(int targetTime, int x, int y, @Nonnegative int range, boolean ignoreHuman) {
		return this.getObjectsInRectangle(targetTime, x - range, y - range, x + range, y + range, ignoreHuman);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRectangle(int targetTime, int x1, int y1, int x2, int y2) {
		return this.getObjectsInRectangle(targetTime, x1, y1, x2, y2, false);
	}

	@Nonnull
	public Collection<StandardEntity> getObjectsInRectangle(int targetTime, int x1, int y1, int x2, int y2, boolean ignoreHuman) {
		Collection<StandardEntity> result = new HashSet<>();
		if (ignoreHuman) {
			result.addAll(
				this.world.getObjectsInRectangle(x1, y1, x2, y2).stream()
					.map(target -> this.getEntity(targetTime, target))
					.collect(Collectors.toList())
			);
		} else {
			Collection<EntityID> areaIDs = new HashSet<>();
			for (StandardEntity entity : this.world.getObjectsInRectangle(x1, y1, x2, y2)) {
				if (entity instanceof Area) {
					result.add(this.getEntity(targetTime, entity));
					areaIDs.add(entity.getID());
				} else if (entity.getStandardURN() == BLOCKADE) {
					result.add(this.getEntity(targetTime, entity));
				}
			}
			Collection<StandardEntity> humans = this.getEntitiesOfType(
				CIVILIAN,
				AMBULANCE_TEAM,
				FIRE_BRIGADE,
				POLICE_FORCE
			);
			for (StandardEntity entity : humans) {
				Human rollback = (Human) Objects.requireNonNull(this.getEntity(targetTime, entity));
				EntityID position = rollback.getPosition();
				if (position != null && areaIDs.contains(position)) {
					result.add(rollback);
				}
			}
		}
		return result;
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(@Nonnull EntityID entity, int range) {
		return this.convertToID(this.world.getObjectsInRange(entity, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(@Nonnull StandardEntity entity, int range) {
		return this.convertToID(this.world.getObjectsInRange(entity, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int x, int y, int range) {
		return this.convertToID(this.world.getObjectsInRange(x, y, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRectangle(int x1, int y1, int x2, int y2) {
		return this.convertToID(this.world.getObjectsInRectangle(x1, y1, x2, y2));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull EntityID entity, int range) {
		return this.convertToID(this.getObjectsInRange(targetTime, entity, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull EntityID entity, int range, boolean ignoreHuman) {
		return this.convertToID(this.getObjectsInRange(targetTime, entity, range, ignoreHuman));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull StandardEntity entity, int range) {
		return this.convertToID(this.getObjectsInRange(targetTime, entity, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, @Nonnull StandardEntity entity, int range, boolean ignoreHuman) {
		return this.convertToID(this.getObjectsInRange(targetTime, entity, range, ignoreHuman));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, int x, int y, int range) {
		return this.convertToID(this.getObjectsInRange(targetTime, x, y, range));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRange(int targetTime, int x, int y, int range, boolean ignoreHuman) {
		return this.convertToID(this.getObjectsInRange(targetTime, x, y, range, ignoreHuman));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRectangle(int targetTime, int x1, int y1, int x2, int y2) {
		return this.convertToID(this.getObjectsInRectangle(targetTime, x1, y1, x2, y2));
	}

	@Nonnull
	public Collection<EntityID> getObjectIDsInRectangle(int targetTime, int x1, int y1, int x2, int y2, boolean ignoreHuman) {
		return this.convertToID(this.getObjectsInRectangle(targetTime, x1, y1, x2, y2, ignoreHuman));
	}

	// getAllEntities //////////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public Collection<StandardEntity> getAllEntities() {
		return this.world.getAllEntities();
	}

	@Nonnull
	public Collection<StandardEntity> getAllEntities(int targetTime) {
		return this.getAllEntities()
			.stream()
			.map(entity -> this.getEntity(targetTime, entity))
			.collect(Collectors.toCollection(HashSet::new));
	}

	// getChangeInfo ///////////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public ChangeSet getChanged() {
		return this.changed;
	}

	// other //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public Collection<Building> getFireBuildings() {
		Set<Building> fireBuildings = new HashSet<>();
		for (StandardEntity entity : this.getEntitiesOfType(BUILDING, GAS_STATION, AMBULANCE_CENTRE, FIRE_STATION, POLICE_OFFICE)) {
			Building building = (Building) entity;
			if (building.isOnFire()) fireBuildings.add(building);
		}
		return fireBuildings;
	}

	@Nonnull
	public Collection<EntityID> getFireBuildingIDs() {
		return this.getFireBuildings().stream().map(Building::getID).collect(Collectors.toList());
	}

	public int getNumberOfBuried(@Nonnull Building building) {
		return this.getNumberOfBuried(building.getID());
	}

	public int getNumberOfBuried(@Nonnull EntityID entityID) {
		int value = 0;
		for (StandardEntity entity : this.getEntitiesOfType(CIVILIAN, AMBULANCE_TEAM, FIRE_BRIGADE, POLICE_FORCE)) {
			Human human = (Human) entity;
			if (Objects.requireNonNull(human.getPosition()).getValue() == entityID.getValue()) {
				if (human.isBuriednessDefined() && human.getBuriedness() > 0) value++;
			}
		}
		return value;
	}

	@Nonnull
	public Collection<Human> getBuriedHumans(@Nonnull Building building) {
		return this.getBuriedHumans(building.getID());
	}

	@Nonnull
	public Collection<Human> getBuriedHumans(@Nonnull EntityID entityID) {
		Collection<Human> result = new HashSet<>();
		for (StandardEntity entity : this.getEntitiesOfType(CIVILIAN, AMBULANCE_TEAM, FIRE_BRIGADE, POLICE_FORCE)) {
			Human human = (Human) entity;
			if (Objects.requireNonNull(human.getPosition()).getValue() == entityID.getValue()) {
				if (human.isBuriednessDefined() && human.getBuriedness() > 0) result.add(human);
			}
		}
		return result;
	}

	@Nonnull
	public Collection<Blockade> getBlockades(@Nonnull EntityID entityID) {
		StandardEntity entity = this.getEntity(entityID);
		if (entity != null && entity instanceof Area) {
			return this.getBlockades((Area) entity);
		}
		return new HashSet<>();
	}

	@Nonnull
	public Collection<Blockade> getBlockades(@Nonnull Area area) {
		if (area.isBlockadesDefined()) {
			Collection<Blockade> blockages = new HashSet<>();
			for (EntityID id : area.getBlockades()) {
				if (id != null) {
					StandardEntity blockade = this.getEntity(id);
					if (blockade != null && blockade.getStandardURN() == BLOCKADE) {
						blockages.add((Blockade) blockade);
					}
				}
			}
			return blockages;
		}
		return new HashSet<>();
	}

	// getPosition /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Nullable
	public StandardEntity getPosition(@Nonnull Human human) {
		return human.getPosition(this.world);
	}

	@Nullable
	public StandardEntity getPosition(@Nonnull Blockade blockade) {
		return this.getEntity(Objects.requireNonNull(blockade.getPosition()));
	}

	@Nullable
	public StandardEntity getPosition(@Nonnull EntityID entityID) {
		StandardEntity entity = Objects.requireNonNull(this.getEntity(entityID));
		if (entity instanceof Human) return this.getPosition((Human) entity);
		return (entity.getStandardURN() == BLOCKADE) ? this.getPosition((Blockade) entity) : null;
	}

	@Nullable
	public StandardEntity getPosition(int targetTime, @Nonnull Human human) {
		return this.getEntity(targetTime, human.getPosition(this.world));
	}

	@Nullable
	public StandardEntity getPosition(int targetTime, @Nonnull Blockade blockade) {
		return this.getEntity(targetTime, Objects.requireNonNull(blockade.getPosition()));
	}


	@Nullable
	public StandardEntity getPosition(int targetTime, @Nonnull EntityID entityID) {
		StandardEntity entity = Objects.requireNonNull(this.getEntity(entityID));
		if (entity instanceof Human) return this.getPosition(targetTime, (Human) entity);
		return (entity.getStandardURN() == BLOCKADE) ? this.getPosition(targetTime, (Blockade) entity) : null;
	}

	// getLocation /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Nullable
	public Pair<Integer, Integer> getLocation(@Nonnull StandardEntity entity) {
		return entity.getLocation(this.world);
	}

	@Nullable
	public Pair<Integer, Integer> getLocation(@Nonnull EntityID entityID) {
		return this.getLocation(Objects.requireNonNull(this.getEntity(entityID)));
	}

	@Nullable
	public Pair<Integer, Integer> getLocation(int targetTime, @Nonnull StandardEntity entity) {
		StandardEntity target = this.getEntity(targetTime, entity);
		return (target != null) ? target.getLocation(this.world) : null;
	}

	@Nullable
	public Pair<Integer, Integer> getLocation(int targetTime, @Nonnull EntityID entityID) {
		StandardEntity target = this.getEntity(targetTime, entityID);
		return (target != null) ? target.getLocation(this.world) : null;
	}

	// getDistance /////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getDistance(@Nonnull EntityID first, @Nonnull EntityID second) {
		return this.world.getDistance(first, second);
	}

	public int getDistance(@Nonnull StandardEntity first, @Nonnull StandardEntity second) {
		return this.world.getDistance(first, second);
	}

	public int getDistance(int targetTime, @Nonnull EntityID first, @Nonnull EntityID second) {
		return this.world.getDistance(
			Objects.requireNonNull(this.getEntity(targetTime, first)),
			Objects.requireNonNull(this.getEntity(targetTime, second))
		);
	}

	public int getDistance(int targetTime, @Nonnull StandardEntity first, @Nonnull StandardEntity second) {
		return this.world.getDistance(
			Objects.requireNonNull(this.getEntity(targetTime, first)),
			Objects.requireNonNull(this.getEntity(targetTime, second))
		);
	}

	// getWorldBounds //////////////////////////////////////////////////////////////////////////////////////////////////

	@Nonnull
	public Rectangle2D getBounds() {
		return this.world.getBounds();
	}

	@Nonnull
	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds() {
		return this.world.getWorldBounds();
	}

	// addEntity //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void addEntity(@Nonnull Entity entity) {
		entity.addEntityListener(new ChangeListener());
		this.world.addEntity(entity);
	}

	@SafeVarargs
	public final void addEntity(
		@Nonnull Entity entity,
		@Nonnull Class<? extends EntityListener> listener,
		Class<? extends EntityListener>... otherListeners
	) {
		try {
			entity.addEntityListener(listener.getDeclaredConstructor().newInstance());
			for (Class<? extends EntityListener> other : otherListeners) {
				Object otherListener = other.getDeclaredConstructor().newInstance();
				if (otherListener instanceof EntityListener) {
					entity.addEntityListener((EntityListener) otherListener);
				}
			}
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}
		this.addEntity(entity);
	}

	public void addEntities(@Nonnull Collection<? extends Entity> entities) {
		entities.forEach(this::addEntity);
	}

	@SafeVarargs
	public final void addEntities(
		@Nonnull Collection<? extends Entity> entities,
		@Nonnull Class<? extends EntityListener> listener,
		Class<? extends EntityListener>... otherListeners
	) {
		entities.forEach(entity -> {
			this.addEntity(entity, listener, otherListeners);
		});
	}

	// registerRollbackListener ////////////////////////////////////////////////////////////////////////////////////////////////

	public void registerEntityListener(@Nonnull Class<? extends EntityListener> listener) {
		for (StandardEntity entity : this.getAllEntities()) {
			try {
				entity.addEntityListener(listener.getDeclaredConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException  | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void registerWorldListener(@Nonnull Class<? extends WorldModelListener<StandardEntity>> listener) {
		try {
			this.world.addWorldModelListener(listener.getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	// removeEntity // marge ///////////////////////////////////////////////////////////////////////////////////////////

	public void removeEntity(@Nonnull StandardEntity e) {
		this.world.removeEntity(e.getID());
	}

	public void removeEntity(@Nonnull EntityID id) {
		this.world.removeEntity(id);
	}

	public void removeAllEntities() {
		this.world.removeAllEntities();
	}

	public void merge(@Nonnull ChangeSet changeSet) {
		this.world.merge(changeSet);
	}

	// system //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	@Nonnull
	public Iterator<StandardEntity> iterator() {
		return this.world.iterator();
	}

	public void setWorld(@Nonnull StandardWorldModel world) {
		this.world = world;
	}

	/**
	 * @return StandardWorldmodel
	 */
	@Nonnull
	public StandardWorldModel getRawWorld() {
		return this.world;
	}

	public void setChanged(@Nonnull ChangeSet changed) {
		this.changed = changed;
	}

	public void setTime(int time) {
		this.time = time;
	}

	@Nonnull
	private Collection<EntityID> convertToID(@Nonnull Collection<StandardEntity> entities) {
		return entities.stream().map(StandardEntity::getID).collect(Collectors.toList());
	}

	// rollback ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void registerRollbackListener() {
		if (this.isRequestedRollback()) {
			this.world.addWorldModelListener(new RollbackListener());
			for (StandardEntity entity : this.getAllEntities()) {
				entity.addEntityListener(new ChangeListener());
			}
		}
	}

	@Nonnull
	private Blockade createRollbackBlockade(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		Blockade copy = (Blockade) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case APEXES:
					if (isDefined) copy.setApexes((int[]) value);
					else copy.undefineApexes();
					break;
				case REPAIR_COST:
					if (isDefined) copy.setRepairCost((Integer) value);
					else copy.undefineRepairCost();
					break;
				case X:
					if (isDefined) copy.setX((Integer) value);
					else copy.undefineX();
					break;
				case Y:
					if (isDefined) copy.setY((Integer) value);
					else copy.undefineY();
					break;
				case POSITION:
					if (isDefined) copy.setPosition((EntityID) value);
					else copy.undefinePosition();
			}
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private Building createRollbackBuilding(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		Building copy = (Building) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case FIERYNESS:
					if (isDefined) copy.setFieryness((Integer) value);
					else copy.undefineFieryness();
					break;
				case TEMPERATURE:
					if (isDefined) copy.setTemperature((Integer) value);
					else copy.undefineTemperature();
					break;
				case BROKENNESS:
					if (isDefined) copy.setBrokenness((Integer) value);
					else copy.undefineBrokenness();
					break;
				case IGNITION:
					if (isDefined) copy.setIgnition((Boolean) value);
					else copy.undefineIgnition();
					break;
				case IMPORTANCE:
					if (isDefined) copy.setImportance((Integer) value);
					else copy.undefineImportance();
					break;
				case BLOCKADES:
					if (isDefined) copy.setBlockades((List<EntityID>) value);
					else copy.undefineBlockades();
					break;
				case BUILDING_CODE:
					if (isDefined) copy.setBuildingCode((Integer) value);
					else copy.undefineBuildingCode();
					break;
				case BUILDING_ATTRIBUTES:
					if (isDefined) copy.setBuildingAttributes((Integer) value);
					else copy.undefineBuildingAttributes();
					break;
				case BUILDING_AREA_GROUND:
					if (isDefined) copy.setGroundArea((Integer) value);
					else copy.undefineGroundArea();
					break;
				case BUILDING_AREA_TOTAL:
					if (isDefined) copy.setTotalArea((Integer) value);
					else copy.undefineTotalArea();
					break;
				case X:
					if (isDefined) copy.setX((Integer) value);
					else copy.undefineX();
					break;
				case Y:
					if (isDefined) copy.setY((Integer) value);
					else copy.undefineY();
					break;
				case FLOORS:
					if (isDefined) copy.setFloors((Integer) value);
					else copy.undefineFloors();
					break;
				case EDGES:
					if (isDefined) copy.setEdges((List<Edge>) value);
					else copy.undefineEdges();
			}
		}
		return copy;
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private Road createRollbackRoad(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		Road copy = (Road) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case BLOCKADES:
					if (isDefined) copy.setBlockades((List<EntityID>) value);
					else copy.undefineBlockades();
					break;
				case X:
					if (isDefined) copy.setX((Integer) value);
					else copy.undefineX();
					break;
				case Y:
					if (isDefined) copy.setY((Integer) value);
					else copy.undefineY();
					break;
				case EDGES:
					if (isDefined) copy.setEdges((List<Edge>) value);
					else copy.undefineEdges();
			}
		}
		return copy;
	}

	@Nonnull
	private World createRollbackWorld(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		World copy = (World) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case START_TIME:
					if (isDefined) copy.setStartTime((Integer) value);
					else copy.undefineStartTime();
					break;
				case LONGITUDE:
					if (isDefined) copy.setLongitude((Integer) value);
					else copy.undefineLongitude();
					break;
				case LATITUDE:
					if (isDefined) copy.setLatitude((Integer) value);
					else copy.undefineLatitude();
					break;
				case WIND_FORCE:
					if (isDefined) copy.setWindForce((Integer) value);
					else copy.undefineWindForce();
					break;
				case WIND_DIRECTION:
					if (isDefined) copy.setWindDirection((Integer) value);
					else copy.undefineWindDirection();
			}
		}
		return copy;
	}

	@Nonnull
	private Human createRollbackHuman(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		Human copy = (Human) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case X:
					if (isDefined) copy.setX((Integer) value);
					else copy.undefineX();
					break;
				case Y:
					if (isDefined) copy.setY((Integer) value);
					else copy.undefineY();
					break;
				case POSITION:
					if (isDefined) copy.setPosition((EntityID) value);
					else copy.undefinePosition();
					break;
				case POSITION_HISTORY:
					if (isDefined) copy.setPositionHistory((int[]) value);
					else copy.undefinePositionHistory();
					break;
				case DIRECTION:
					if (isDefined) copy.setDirection((Integer) value);
					else copy.undefineDirection();
					break;
				case STAMINA:
					if (isDefined) copy.setStamina((Integer) value);
					else copy.undefineStamina();
					break;
				case HP:
					if (isDefined) copy.setHP((Integer) value);
					else copy.undefineHP();
					break;
				case DAMAGE:
					if (isDefined) copy.setDamage((Integer) value);
					else copy.undefineDamage();
					break;
				case BURIEDNESS:
					if (isDefined) copy.setBuriedness((Integer) value);
					else copy.undefineBuriedness();
					break;
				case TRAVEL_DISTANCE:
					if (isDefined) copy.setTravelDistance((Integer) value);
					else copy.undefineTravelDistance();
			}
		}
		return copy;
	}

	@Nonnull
	private FireBrigade createRollbackFireBrigade(@Nonnull StandardEntity entity, @Nonnull Map<String, Object> cache) {
		FireBrigade copy = (FireBrigade) entity.copy();
		for (String urn : cache.keySet()) {
			Object value = cache.get(urn);
			StandardPropertyURN type = StandardPropertyURN.fromString(urn);
			boolean isDefined = value != null;
			switch (type) {
				case X:
					if (isDefined) copy.setX((Integer) value);
					else copy.undefineX();
					break;
				case Y:
					if (isDefined) copy.setY((Integer) value);
					else copy.undefineY();
					break;
				case POSITION:
					if (isDefined) copy.setPosition((EntityID) value);
					else copy.undefinePosition();
					break;
				case POSITION_HISTORY:
					if (isDefined) copy.setPositionHistory((int[]) value);
					else copy.undefinePositionHistory();
					break;
				case DIRECTION:
					if (isDefined) copy.setDirection((Integer) value);
					else copy.undefineDirection();
					break;
				case STAMINA:
					if (isDefined) copy.setStamina((Integer) value);
					else copy.undefineStamina();
					break;
				case HP:
					if (isDefined) copy.setHP((Integer) value);
					else copy.undefineHP();
					break;
				case DAMAGE:
					if (isDefined) copy.setDamage((Integer) value);
					else copy.undefineDamage();
					break;
				case BURIEDNESS:
					if (isDefined) copy.setBuriedness((Integer) value);
					else copy.undefineBuriedness();
					break;
				case TRAVEL_DISTANCE:
					if (isDefined) copy.setTravelDistance((Integer) value);
					else copy.undefineTravelDistance();
					break;
				case WATER_QUANTITY:
					if (isDefined) copy.setWater((Integer) value);
					else copy.undefineWater();
			}
		}
		return copy;
	}

	private class RollbackListener implements WorldModelListener<StandardEntity> {
		@Override
		public void entityAdded(WorldModel<? extends StandardEntity> model, StandardEntity e) {
			EntityID entityID = e.getID();
			Map<Integer, Map<String, Object>> entityHistory = rollback.get(entityID);
			if (entityHistory == null) {
				entityHistory = new HashMap<>();
			}
			Map<String, Object> changeProperties = entityHistory.get(time);
			if (changeProperties == null) {
				changeProperties = new HashMap<>();
			}
			Map<String, Object> addedPoint = new HashMap<>();

			for (Property property : e.getProperties()) {
				changeProperties.put(property.getURN(), property.getValue());
				addedPoint.put(property.getURN(), null);
			}

			entityHistory.put(time, changeProperties);
			entityHistory.put(time - 1, addedPoint);
			rollback.put(entityID, entityHistory);

		}

		@Override
		public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
			EntityID entityID = e.getID();

			Map<Integer, Map<String, Object>> entityHistory = rollback.get(entityID);
			if (entityHistory == null) {
				entityHistory = new HashMap<>();
			}
			Map<String, Object> changeProperties = entityHistory.get(time);
			if (changeProperties == null) {
				changeProperties = new HashMap<>();
			}

			for (Property property : e.getProperties()) {
				changeProperties.put(property.getURN(), property.getValue());
			}

			entityHistory.put(time, changeProperties);
			rollback.put(entityID, entityHistory);
		}
	}

	private class ChangeListener implements EntityListener {
		@Override
		public void propertyChanged(Entity entity, Property property, Object oldValue, Object newValue) {
			EntityID entityID = entity.getID();

			Map<Integer, Map<String, Object>> entityHistory = rollback.get(entityID);
			if (entityHistory == null) {
				entityHistory = new HashMap<>();
			}
			Map<String, Object> changeProperties = entityHistory.get(time);
			if (changeProperties == null) {
				changeProperties = new HashMap<>();
			}

			changeProperties.put(property.getURN(), oldValue);

			entityHistory.put(time, changeProperties);
			rollback.put(entityID, entityHistory);
		}
	}
}
