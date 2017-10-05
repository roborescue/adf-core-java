package adf.component.module.complex;

import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import rescuecore2.standard.entities.Road;

/**
 * @deprecated change class name {@link RoadDetector}
 */
@Deprecated
public abstract class RoadSelector extends RoadDetector {
	public RoadSelector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
		super(ai, wi, si, moduleManager, developData);
	}

	@Override
	public RoadSelector precompute(PrecomputeData precomputeData) {
		super.precompute(precomputeData);
		return this;
	}

	@Override
	public RoadSelector resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		return this;
	}

	@Override
	public RoadSelector preparate() {
		super.preparate();
		return this;
	}

	@Override
	public RoadSelector updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		return this;
	}

	@Override
	public abstract RoadSelector calc();
}


