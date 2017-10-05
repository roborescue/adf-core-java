package adf.component.module.complex;

import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import adf.component.module.AbstractModule;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

public abstract class TargetAllocator extends AbstractModule {
	public TargetAllocator(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
		super(ai, wi, si, moduleManager, developData);
	}

	public abstract Map<EntityID, EntityID> getResult();

	@Override
	public abstract TargetAllocator calc();

	@Override
	public final TargetAllocator precompute(PrecomputeData precomputeData) {
		super.precompute(precomputeData);
		return this;
	}

	@Override
	public TargetAllocator resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		return this;
	}

	@Override
	public TargetAllocator preparate() {
		super.preparate();
		return this;
	}

	@Override
	public TargetAllocator updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		return this;
	}
}

