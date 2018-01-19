package adf.agent;

import adf.agent.communication.MessageManager;
import adf.agent.communication.standard.StandardCommunicationModule;
import adf.agent.communication.standard.bundle.StandardMessageBundle;
import adf.agent.config.ModuleConfig;
import adf.agent.info.AgentInfo;
import adf.agent.module.ModuleManager;
import adf.agent.develop.DevelopData;
import adf.agent.precompute.PrecomputeData;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.component.communication.CommunicationModule;
import adf.launcher.ConfigKey;
import adf.launcher.ConsoleOutput;
import rescuecore2.components.AbstractAgent;
import rescuecore2.config.ConfigException;
import rescuecore2.messages.Command;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.KASense;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.messages.AKSubscribe;
import rescuecore2.worldmodel.ChangeSet;

import java.util.*;

public abstract class Agent<E extends StandardEntity> extends AbstractAgent<StandardWorldModel, E> {
	final protected static String DATASTORAGE_FILE_NAME_AMBULANCE = "ambulance.bin";
	final protected static String DATASTORAGE_FILE_NAME_FIRE = "fire.bin";
	final protected static String DATASTORAGE_FILE_NAME_POLICE = "police.bin";

	private ScenarioInfo.Mode mode;
	public AgentInfo agentInfo;
	public WorldInfo worldInfo;
	public ScenarioInfo scenarioInfo;
	protected ModuleConfig moduleConfig;
	protected ModuleManager moduleManager;
	protected PrecomputeData precomputeData;
	protected DevelopData developData;
	protected MessageManager messageManager;
	private CommunicationModule communicationModule;
	protected boolean isPrecompute;
	protected boolean isDebugMode;
	private int ignoreTime;

	public Agent(boolean isPrecompute, String dataStorageName, boolean isDebugMode, ModuleConfig moduleConfig, DevelopData developData) {
		this.isPrecompute = isPrecompute;
		this.isDebugMode = isDebugMode;

		if (isPrecompute) {
			PrecomputeData.removeData(dataStorageName);
			this.mode = ScenarioInfo.Mode.PRECOMPUTATION_PHASE;
		}

		this.moduleConfig = moduleConfig;
		this.precomputeData = new PrecomputeData(dataStorageName);
		this.developData = developData;
		this.messageManager = new MessageManager();
	}

	@Override
	public final String[] getRequestedEntityURNs() {
		EnumSet<StandardEntityURN> set = getRequestedEntityURNsEnum();
		String[] result = new String[set.size()];
		int i = 0;
		for (StandardEntityURN next : set) {
			result[i++] = next.toString();
		}

		return result;
	}

	protected abstract EnumSet<StandardEntityURN> getRequestedEntityURNsEnum();

	@Override
	protected StandardWorldModel createWorldModel() {
		return new StandardWorldModel();
	}

	@Override
	protected void postConnect() {
		super.postConnect();
		if (shouldIndex()) {
			this.model.index();
		}

		this.ignoreTime = config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY);

		this.worldInfo = new WorldInfo(this.model);

		if (!this.isPrecompute) {
			if (this.precomputeData.isReady(this.worldInfo)) {
				this.mode = ScenarioInfo.Mode.PRECOMPUTED;
			} else {
				this.mode = ScenarioInfo.Mode.NON_PRECOMPUTE;
			}
		}

		this.config.setBooleanValue(ConfigKey.KEY_DEBUG_FLAG, this.isDebugMode);
		this.config.setBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, this.developData.isDevelopMode());
		this.scenarioInfo = new ScenarioInfo(this.config, this.mode);
		this.communicationModule = null;

		switch (scenarioInfo.getMode()) {
			case NON_PRECOMPUTE:
				ConsoleOutput.out(ConsoleOutput.State.INFO,
					"Connected - " + this + " (NON_PRECOMPUTE)");
				break;
			case PRECOMPUTATION_PHASE:
				ConsoleOutput.out(ConsoleOutput.State.INFO,
					"Connected - " + this + " (PRECOMPUTATION)");
				break;
			case PRECOMPUTED:
				ConsoleOutput.out(ConsoleOutput.State.INFO,
					"Connected - " + this + " (PRECOMPUTED)");
				break;
			default:
		}
	}

	@Override
	protected void processSense(KASense sense) {
		int time = sense.getTime();
		ChangeSet changed = sense.getChangeSet();
		this.worldInfo.setTime(time);
		this.model.merge(sense.getChangeSet());
		Collection<Command> heard = sense.getHearing();
		think(time, changed, heard);
	}

	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		this.agentInfo.recordThinkStartTime();
		this.agentInfo.setTime(time);


		if (1 == time) {
			if (this.communicationModule != null) {
				ConsoleOutput.out(ConsoleOutput.State.ERROR, "[ERROR ] Loader is not found.");
				ConsoleOutput.out(ConsoleOutput.State.NOTICE, "CommunicationModule is modified - " + this);
			} else {
				this.communicationModule = new StandardCommunicationModule();
			}

			this.messageManager.registerMessageBundle(new StandardMessageBundle());
		}

		// agents can subscribe after ignore time
		if (time >= ignoreTime) {
			this.messageManager.subscribe(this.agentInfo, this.worldInfo, this.scenarioInfo);

			if (!this.messageManager.getIsSubscribed()) {
				int[] channelsToSubscribe = this.messageManager.getChannels();
				if (channelsToSubscribe != null) {
					super.send(new AKSubscribe(this.getID(), time, channelsToSubscribe));
					this.messageManager.setIsSubscribed(true);
				}
			}
		}

		this.agentInfo.setHeard(heard);
		this.agentInfo.setChanged(changed);
		this.worldInfo.setChanged(changed);

		this.messageManager.refresh();
		this.communicationModule.receive(this, this.messageManager);

		try {
			think();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.messageManager.coordinateMessages(this.agentInfo, this.worldInfo, this.scenarioInfo);
		this.communicationModule.send(this, this.messageManager);
	}

	abstract protected void think();

	protected boolean shouldIndex() {
		return true;
	}

	public double getX() {
		return me().getLocation(this.model).first();
	}

	public double getY() {
		return me().getLocation(this.model).second();
	}

	public void send(Message[] messages) {
		for (Message msg : messages) super.send(msg);
	}

	public void send(List<Message> messages) {
		for (Message msg : messages) super.send(msg);
	}

	@Override
	public String getName() {
		return moduleConfig.getValue("Team.Name") + "." + getClass().getSimpleName();
	}
}

