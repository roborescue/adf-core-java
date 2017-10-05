package adf.agent.platoon;

import adf.agent.Agent;
import adf.agent.action.Action;
import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.module.ModuleManager;
import adf.component.tactics.Tactics;
import adf.launcher.ConsoleOutput;
import rescuecore2.standard.entities.StandardEntity;

public abstract class Platoon<E extends StandardEntity> extends Agent<E> {
	private Tactics rootTactics;

	Platoon(Tactics tactics, boolean isPrecompute, String dataStorageName, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		super(isPrecompute, dataStorageName, isDebugMode, moduleConfig, developData);
		this.rootTactics = tactics;
	}

	@Override
	protected void postConnect() {
		super.postConnect();
		//model.indexClass(StandardEntityURN.ROAD);
		//distance = config.getIntValue(DISTANCE_KEY);

		this.agentInfo = new AgentInfo(this, this.model);
		this.moduleManager = new ModuleManager(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleConfig, this.developData);

		this.rootTactics.initialize(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleManager, this.messageManager, this.developData);

		switch (this.scenarioInfo.getMode()) {
			case NON_PRECOMPUTE:
				this.rootTactics.preparate(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleManager, this.developData);
				this.worldInfo.registerRollbackListener();
				break;
			case PRECOMPUTATION_PHASE:
				this.rootTactics.precompute(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleManager, this.precomputeData, this.developData);
				this.precomputeData.setReady(true, this.worldInfo);
				if (!this.precomputeData.write()) {
					ConsoleOutput.out(ConsoleOutput.State.ERROR, "[ERROR ] Failed to write PrecomputeData.");
				}
				break;
			case PRECOMPUTED:
				this.rootTactics.resume(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleManager, this.precomputeData, this.developData);
				this.worldInfo.registerRollbackListener();
				break;
			default:
		}
	}

	protected void think() {
		Action action = this.rootTactics.think(this.agentInfo, this.worldInfo, this.scenarioInfo, this.moduleManager, this.messageManager, this.developData);
		if (action != null) {
			this.agentInfo.setExecutedAction(this.agentInfo.getTime(), action);
			send(action.getCommand(this.getID(), this.agentInfo.getTime()));
		}
	}
}

