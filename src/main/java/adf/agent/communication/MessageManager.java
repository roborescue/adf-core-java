package adf.agent.communication;

import adf.agent.communication.standard.bundle.StandardMessageBundle;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
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
	private List<CommunicationMessage> receivedMessageList;
	private int heardAgentHelpCount;
	private List<MessageCoordinator> messageCoordinatorList;

	private Set<String> checkDuplicationCache;

	public MessageManager() {
		this.standardMessageClassCount = 1;    // 00001
		this.customMessageClassCount = 16;    // 10000
		this.messageClassMap = new HashMap<>(32);
		this.messageClassIDMap = new HashMap<>(32);
		this.sendMessageList = new ArrayList<>();
		this.checkDuplicationCache = new HashSet<>();
		this.receivedMessageList = new ArrayList<>();
		this.heardAgentHelpCount = 0;
		this.messageCoordinatorList = new ArrayList<>();
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

		MessageCoordinator messageCoordinator = messageBundle.getMessageCoordinator();
		if (messageCoordinator != null) {
			messageCoordinatorList.add(messageCoordinator);
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
	public List<CommunicationMessage> getSendMessageList() {
		return this.sendMessageList;
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
		for (int index = (this.messageCoordinatorList.size() - 1); 0 <= index; index--) {
			MessageCoordinator coordinator = this.messageCoordinatorList.get(index);
			coordinator.coordinate(agentInfo, worldInfo, scenarioInfo, this, this.sendMessageList);
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
