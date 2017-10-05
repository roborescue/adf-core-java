package adf.component.module.complex;


import adf.agent.communication.MessageManager;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import rescuecore2.standard.entities.Road;

public abstract class RoadDetector extends TargetDetector<Road> {
	public RoadDetector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
		super(ai, wi, si, moduleManager, developData);
	}

	@Override
	public RoadDetector precompute(PrecomputeData precomputeData) {
		super.precompute(precomputeData);
		return this;
	}

	@Override
	public RoadDetector resume(PrecomputeData precomputeData) {
		super.resume(precomputeData);
		return this;
	}

	@Override
	public RoadDetector preparate() {
		super.preparate();
		return this;
	}

	@Override
	public RoadDetector updateInfo(MessageManager messageManager) {
		super.updateInfo(messageManager);
		return this;
	}

	@Override
	public abstract RoadDetector calc();
}

