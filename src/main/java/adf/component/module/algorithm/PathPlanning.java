package adf.component.module.algorithm;


import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import adf.component.module.AbstractModule;
import rescuecore2.misc.Pair;
import rescuecore2.worldmodel.EntityID;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class PathPlanning extends AbstractModule {
	public PathPlanning(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
		super(ai, wi, si, moduleManager, developData);
	}

	public abstract List<EntityID> getResult();

	public abstract PathPlanning setFrom(EntityID id);

	public abstract PathPlanning setDestination(Collection<EntityID> targets);

	public PathPlanning setDestination(EntityID... targets) {
		return this.setDestination(Arrays.asList(targets));
	}

	@Override
	public PathPlanning precompute(PrecomputeData precomputeData) {
		super.precompute(precomputeData);
		return this;
	}

	@Override
	public PathPlanning resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		return this;
	}

	@Override
	public PathPlanning preparate() {
		super.preparate();
		return this;
	}

	@Override
	public PathPlanning updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		return this;
	}

	@Override
	public abstract PathPlanning calc();

	public double getDistance() {
		double sum = 0.0;
		List<EntityID> path = getResult();
		if (path == null || path.size() <= 1) {
			return sum;
		}

		Pair<Integer, Integer> prevPoint = null;
		for (EntityID id : path) {
			Pair<Integer, Integer> point = worldInfo.getLocation(worldInfo.getEntity(id));
			if (prevPoint != null) {
				int x = prevPoint.first() - point.first();
				int y = prevPoint.second() - point.second();
				sum += x * x + y * y;
			}
			prevPoint = point;
		}

		return Math.sqrt(sum);
	}

	// Alias
	public double getDistance(EntityID from, EntityID dest) {
		return this.setFrom(from).setDestination(dest).calc().getDistance();
	}

	public List<EntityID> getResult(EntityID from, EntityID dest) {
		return this.setFrom(from).setDestination(dest).calc().getResult();
	}
}
