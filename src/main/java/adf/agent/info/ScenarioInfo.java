package adf.agent.info;

import adf.launcher.ConfigKey;
import rescuecore2.config.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScenarioInfo {
	public enum Mode {
		NON_PRECOMPUTE,
		PRECOMPUTED,
		PRECOMPUTATION_PHASE
	}

	private Config config;
	private Mode mode;

	/**
	 * (internal invoking only).
	 *
	 * @param config base config
	 * @param mode   agent run mode
	 */
	public ScenarioInfo(@Nonnull Config config, @Nonnull Mode mode) {
		this.config = config;
		this.mode = mode;
	}

	/**
	 * (internal invoking only).
	 *
	 * @param config base config
	 */
	public ScenarioInfo(@Nonnull Config config) {
		this(config, Mode.NON_PRECOMPUTE);
	}

	/**
	 * (internal invoking only).
	 *
	 * @param config base config
	 */
	public void setConfig(@Nonnull Config config) {
		this.config = config;
	}

	/**
	 * @return agent run mode
	 */
	@Nonnull
	public Mode getMode() {
		return mode;
	}

	/**
	 * @return raw config
	 */
	@Nonnull
	public Config getRawConfig() {
		return this.config;
	}

	/**
	 * @return fire.extinguish.max-sum
	 */
	public int getFireExtinguishMaxSum() {
		return this.config.getIntValue("fire.extinguish.max-sum");
	}

	/**
	 * @return comms.channels.max.platoon
	 */
	public int getCommsChannelsMaxPlatoon() {
		return config.getIntValue("comms.channels.max.platoon");
	}

	public int getKernelAgentsThinkTime() {
		return config.getIntValue("kernel.agents.think-time");
	}

	public int getFireTankMaximum() {
		return config.getIntValue("fire.tank.maximum");
	}

	public int getClearRepairRate() {
		return config.getIntValue("clear.repair.rate");
	}

	public int getKernelStartupConnectTime() {
		return config.getIntValue("kernel.startup.connect-time");
	}

	@Nullable
	public String getKernelHost() {
		return config.getValue("kernel.host");
	}

	public int getScenarioAgentsAt() {
		return config.getIntValue("scenario.agents.at");
	}

	public int getPerceptionLosMaxDistance() {
		return config.getIntValue("perception.los.max-distance");
	}

	public int getScenarioAgentsFb() {
		return config.getIntValue("scenario.agents.fb");
	}

	public int getScenarioAgentsPo() {
		return config.getIntValue("scenario.agents.po");
	}

	@Nullable
	public String getKernelCommunicationModel() {
		return config.getValue("kernel.communication-model");
	}

	public int getPerceptionLosPrecisionDamage() {
		return config.getIntValue("perception.los.precision.damage");
	}

	public int getScenarioAgentsAc() {
		return config.getIntValue("scenario.agents.ac");
	}

	public int getCommsChannelsMaxOffice() {
		return config.getIntValue("comms.channels.max.centre");
	}

	public int getFireExtinguishMaxDistance() {
		return config.getIntValue("fire.extinguish.max-distance");
	}

	public int getKernelAgentsIgnoreuntil() {
		return config.getIntValue("kernel.agents.ignoreuntil");
	}

	public int getClearRepairDistance() {
		return this.config.getIntValue("clear.repair.distance");
	}

	public int getCommsChannelsCount() {
		return config.getIntValue("comms.channels.count");
	}

	public int getCommsChannelBandwidth(int channel) {
		if (channel < getCommsChannelsCount()) {
			return config.getIntValue("comms.channels."+channel+".bandwidth");
		} else {
			return 0;
		}
	}

	@Nullable
	public String getKernelPerception() {
		return config.getValue("kernel.perception");
	}

	public int getPerceptionLosPrecisionHp() {
		return config.getIntValue("perception.los.precision.hp");
	}

	public int getClearRepairRad() {
		return config.getIntValue("clear.repair.rad");
	}

	public int getFireTankRefillHydrantRate() {
		return config.getIntValue("fire.tank.refill_hydrant_rate");
	}

	public int getScenarioAgentsPf() {
		return config.getIntValue("scenario.agents.pf");
	}

	public int getScenarioAgentsFs() {
		return config.getIntValue("scenario.agents.fs");
	}

	public int getVoiceMessagesSize() {
		return config.getIntValue("comms.channels.0.messages.size");
	}

	public int getFireTankRefillRate() {
		return this.config.getIntValue("fire.tank.refill_rate", 500);
	}

	public int getKernelTimesteps() {
		return this.config.getIntValue("kernel.timesteps");
	}

	public boolean isDebugMode() {
		return this.config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false);
	}

	public boolean isDevelopMode() {
		return this.config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false);
	}
}
