package adf.agent.communication;

import adf.agent.communication.standard.bundle.StandardMessageBundle;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.component.communication.ChannelSubscriber;
import adf.component.communication.CommunicationMessage;
import adf.component.communication.MessageBundle;
import adf.component.communication.MessageCoordinator;
import adf.launcher.ConsoleOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MessageManager {
	private int standardMessageClassCount;
	private int customMessageClassCount;
	private HashMap<Integer, Class<? extends CommunicationMessage>> messageClassMap;
	private HashMap<Class<? extends CommunicationMessage>, Integer> messageClassIDMap;
	private ArrayList<CommunicationMessage> sendMessageList;
	private List<List<CommunicationMessage>> channelSendMessageList;
	private List<CommunicationMessage> receivedMessageList;
	private int heardAgentHelpCount;
	private MessageCoordinator messageCoordinator;

	private Set<String> checkDuplicationCache;

	private ChannelSubscriber channelSubscriber;
	private int[] subscribedChannels;
	private boolean isSubscribed;

	public MessageManager() {
		this.standardMessageClassCount = 1;    // 00001
		this.customMessageClassCount = 16;    // 10000
		this.messageClassMap = new HashMap<>(32);
		this.messageClassIDMap = new HashMap<>(32);
		this.sendMessageList = new ArrayList<>();
		this.channelSendMessageList = new ArrayList<>();
		this.checkDuplicationCache = new HashSet<>();
		this.receivedMessageList = new ArrayList<>();
		this.heardAgentHelpCount = 0;

		this.messageCoordinator = null;

		channelSubscriber = null;
		subscribedChannels = new int[1];
		// by default subscribe to channel 1
		subscribedChannels[0] = 1;
		isSubscribed = false;
	}

	public void subscribeToChannels(int[] channels) {
		subscribedChannels = channels;
		isSubscribed = false;
	}
	public int[] getChannels() {
		return subscribedChannels;
	}
	public boolean getIsSubscribed() {
		return isSubscribed;
	}
	public void setIsSubscribed(boolean subscribed) {
		isSubscribed = subscribed;
	}

	public void setMessageCoordinator(MessageCoordinator mc) {
		this.messageCoordinator = mc;
	}

	public void setChannelSubscriber(ChannelSubscriber cs) {
		channelSubscriber = cs;
	}

	public void subscribe(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo) {
		if (channelSubscriber != null) {
			channelSubscriber.subscribe(agentInfo, worldInfo, scenarioInfo, this);
		}
	}

	public boolean registerMessageClass(int index, @Nonnull Class<? extends CommunicationMessage> messageClass) {
		if (index > 31) {
			throw new IllegalArgumentException("index maximum is 31");
		}

		if (messageClassMap.containsKey(index)) {
			//throw new IllegalArgumentException("index(" + index + ") is already registrated");
			ConsoleOutput.out(ConsoleOutput.State.WARN,
				"index(" + index + ") is already registered/" + messageClass.getName() + " is ignored");
			return false;
		}

		messageClassMap.put(index, messageClass);
		messageClassIDMap.put(messageClass, index);

		return true;
	}

	public void registerMessageBundle(@Nonnull MessageBundle messageBundle) {
		if (messageBundle == null) {
			return;
		}

		for (Class<? extends CommunicationMessage> messageClass : messageBundle.getMessageClassList()) {
			this.registerMessageClass(
				(messageBundle.getClass().equals(StandardMessageBundle.class) ?
					standardMessageClassCount++ : customMessageClassCount++),
				messageClass);
		}
	}

	@Nullable
	public Class<? extends CommunicationMessage> getMessageClass(int index) {
		if (!messageClassMap.containsKey(index)) {
			return null;
		}

		return messageClassMap.get(index);
	}

	public int getMessageClassIndex(@Nonnull CommunicationMessage message) {
		if (!messageClassMap.containsValue(message.getClass())) {
			throw new IllegalArgumentException(message.getClass().getName() + " isnot registorated to manager");
		}

		return messageClassIDMap.get(message.getClass());
	}

	public void addMessage(@Nonnull CommunicationMessage message) {
		this.addMessage(message, true);
	}

	public void addMessage(@Nonnull CommunicationMessage message, boolean checkDuplication) {
		if (message == null) {
			return;
		}

		String checkKey = message.getCheckKey();
		if (checkDuplication && !this.checkDuplicationCache.contains(checkKey)) {
			this.sendMessageList.add(message);
			this.checkDuplicationCache.add(checkKey);
		} else {
			this.sendMessageList.add(message);
			this.checkDuplicationCache.add(checkKey);
		}
	}

	@Nonnull
	public List<List<CommunicationMessage>> getSendMessageList() {
		return this.channelSendMessageList;
	}

	public void addReceivedMessage(@Nonnull CommunicationMessage message) {
		receivedMessageList.add(message);
	}

	@Nonnull
	public List<CommunicationMessage> getReceivedMessageList() {
		return this.receivedMessageList;
	}

	@SafeVarargs
	@Nonnull
	public final List<CommunicationMessage> getReceivedMessageList(Class<? extends CommunicationMessage>... messageClasses) {
		List<CommunicationMessage> resultList = new ArrayList<>();
		for (CommunicationMessage message : this.receivedMessageList) {
			if (Arrays.asList(messageClasses).contains(message.getClass())) {
				resultList.add(message);
			}
		}
		return resultList;
	}

	public void coordinateMessages(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo) {
		// create a list of messages for every channel including the voice comm channel
		this.channelSendMessageList = new ArrayList<List<CommunicationMessage>>(scenarioInfo.getCommsChannelsCount());
		for (int i = 0; i < scenarioInfo.getCommsChannelsCount(); i++) {
			this.channelSendMessageList.add(new ArrayList<CommunicationMessage>());
		}

		if (messageCoordinator != null) {
			messageCoordinator.coordinate(agentInfo, worldInfo, scenarioInfo, this, this.sendMessageList, this.channelSendMessageList);
		}
	}

	public void addHeardAgentHelpCount() {
		this.heardAgentHelpCount++;
	}

	public int getHeardAgentHelpCount() {
		return this.heardAgentHelpCount;
	}

	public void refresh() {
		this.sendMessageList.clear();
		this.checkDuplicationCache.clear();
		this.receivedMessageList.clear();
		this.heardAgentHelpCount = 0;
	}
}
