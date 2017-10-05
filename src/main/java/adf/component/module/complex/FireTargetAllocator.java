package adf.component.module.complex;

import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

public abstract class FireTargetAllocator extends TargetAllocator {
	public FireTargetAllocator(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
		super(ai, wi, si, moduleManager, developData);
	}

	@Override
	public abstract Map<EntityID, EntityID> getResult();

	@Override
	public abstract FireTargetAllocator calc();

	@Override
	public FireTargetAllocator resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		return this;
	}

	@Override
	public FireTargetAllocator preparate() {
		super.preparate();
		return this;
	}

	@Override
	public FireTargetAllocator updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		return this;
	}
}

