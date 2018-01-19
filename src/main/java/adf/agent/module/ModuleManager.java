package adf.agent.module;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.component.communication.ChannelSubscriber;
import adf.component.communication.CommunicationMessage;
import adf.component.centralized.CommandExecutor;
import adf.component.centralized.CommandPicker;
import adf.component.communication.MessageCoordinator;
import adf.component.extaction.ExtAction;
import adf.component.module.AbstractModule;
import rescuecore2.config.NoSuchConfigOptionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
	private Map<String, AbstractModule> moduleMap;
	private Map<String, ExtAction> actionMap;
	private Map<String, CommandExecutor<CommunicationMessage>> executorMap;
	private Map<String, CommandPicker> pickerMap;
	private Map<String, ChannelSubscriber> channelSubscriberMap;
	private Map<String, MessageCoordinator> messageCoordinatorMap;

	private AgentInfo agentInfo;
	private WorldInfo worldInfo;
	private ScenarioInfo scenarioInfo;

	private ModuleConfig moduleConfig;

	private DevelopData developData;

	public ModuleManager(@Nonnull AgentInfo agentInfo, @Nonnull WorldInfo worldInfo, @Nonnull ScenarioInfo scenarioInfo, @Nonnull ModuleConfig moduleConfig, @Nonnull DevelopData developData) {
		this.agentInfo = agentInfo;
		this.worldInfo = worldInfo;
		this.scenarioInfo = scenarioInfo;
		this.moduleConfig = moduleConfig;
		this.developData = developData;
		this.moduleMap = new HashMap<>();
		this.actionMap = new HashMap<>();
		this.executorMap = new HashMap<>();
		this.pickerMap = new HashMap<>();
		this.channelSubscriberMap = new HashMap<>(1);
		this.messageCoordinatorMap = new HashMap<>(1);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final <T extends AbstractModule> T getModule(@Nonnull String moduleName, @Nullable String defaultClassName) {
		String className = moduleName;
		try {
			className = this.moduleConfig.getValue(moduleName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> moduleClass;
			try {
				moduleClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				moduleClass = Class.forName(className);
			}

			AbstractModule instance = this.moduleMap.get(className);
			if (instance != null) {
				return (T) instance;
			}

			if (AbstractModule.class.isAssignableFrom(moduleClass)) {
				instance = this.getModule((Class<AbstractModule>) moduleClass);
				this.moduleMap.put(className, instance);
				return (T) instance;
			}

		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("Module name is not found : " + className);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final <T extends AbstractModule> T getModule(@Nonnull String moduleName) {
		return this.getModule(moduleName, "");
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private AbstractModule getModule(@Nonnull Class<AbstractModule> moduleClass) {
		try {
			Constructor<AbstractModule> constructor = moduleClass.getConstructor(AgentInfo.class, WorldInfo.class, ScenarioInfo.class, ModuleManager.class, DevelopData.class);
			AbstractModule instance = constructor.newInstance(this.agentInfo, this.worldInfo, this.scenarioInfo, this, this.developData);
			this.moduleMap.put(moduleClass.getCanonicalName(), instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final ExtAction getExtAction(String actionName, String defaultClassName) {
		String className = actionName;
		try {
			className = this.moduleConfig.getValue(actionName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> actionClass;
			try {
				actionClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				actionClass = Class.forName(className);
			}

			ExtAction instance = this.actionMap.get(className);
			if (instance != null) {
				return instance;
			}

			if (ExtAction.class.isAssignableFrom(actionClass)) {
				instance = this.getExtAction((Class<ExtAction>) actionClass);
				this.actionMap.put(className, instance);
				return instance;
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}
		throw new IllegalArgumentException("ExtAction name is not found : " + className);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final ExtAction getExtAction(String actionName) {
		return getExtAction(actionName, "");
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private ExtAction getExtAction(Class<ExtAction> actionClass) {
		try {
			Constructor<ExtAction> constructor = actionClass.getConstructor(AgentInfo.class, WorldInfo.class, ScenarioInfo.class, ModuleManager.class, DevelopData.class);
			ExtAction instance = constructor.newInstance(this.agentInfo, this.worldInfo, this.scenarioInfo, this, this.developData);
			this.actionMap.put(actionClass.getCanonicalName(), instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final <E extends CommandExecutor<? extends CommunicationMessage>> E getCommandExecutor(String executorName, String defaultClassName) {
		String className = executorName;
		try {
			className = this.moduleConfig.getValue(executorName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> actionClass;
			try {
				actionClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				actionClass = Class.forName(className);
			}

			CommandExecutor<CommunicationMessage> instance = this.executorMap.get(className);
			if (instance != null) {
				return (E) instance;
			}

			if (CommandExecutor.class.isAssignableFrom(actionClass)) {
				instance = this.getCommandExecutor((Class<CommandExecutor<CommunicationMessage>>) actionClass);
				this.executorMap.put(className, instance);
				return (E) instance;
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("CommandExecutor name is not found : " + className);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final <E extends CommandExecutor<? extends CommunicationMessage>> E getCommandExecutor(String executorName) {
		return getCommandExecutor(executorName, "");
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private <E extends CommandExecutor<? extends CommunicationMessage>> E getCommandExecutor(Class<E> actionClass) {
		try {
			Constructor<E> constructor = actionClass.getConstructor(AgentInfo.class, WorldInfo.class, ScenarioInfo.class, ModuleManager.class, DevelopData.class);
			E instance = constructor.newInstance(this.agentInfo, this.worldInfo, this.scenarioInfo, this, this.developData);
			this.executorMap.put(actionClass.getCanonicalName(), (CommandExecutor<CommunicationMessage>) instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final CommandPicker getCommandPicker(String pickerName, String defaultClassName) {
		String className = pickerName;
		try {
			className = this.moduleConfig.getValue(pickerName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> actionClass;
			try {
				actionClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				actionClass = Class.forName(className);
			}

			CommandPicker instance = this.pickerMap.get(className);
			if (instance != null) {
				return instance;
			}

			if (CommandPicker.class.isAssignableFrom(actionClass)) {
				instance = this.getCommandPicker((Class<CommandPicker>) actionClass);
				this.pickerMap.put(className, instance);
				return instance;
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("CommandExecutor name is not found : " + className);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public final CommandPicker getCommandPicker(String pickerName) {
		return getCommandPicker(pickerName, "");
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private CommandPicker getCommandPicker(Class<CommandPicker> actionClass) {
		try {
			Constructor<CommandPicker> constructor = actionClass.getConstructor(AgentInfo.class, WorldInfo.class, ScenarioInfo.class, ModuleManager.class, DevelopData.class);
			CommandPicker instance = constructor.newInstance(this.agentInfo, this.worldInfo, this.scenarioInfo, this, this.developData);
			this.pickerMap.put(actionClass.getCanonicalName(), instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public final ChannelSubscriber getChannelSubscriber(String subscriberName, String defaultClassName) {
		String className = subscriberName;
		try {
			className = this.moduleConfig.getValue(subscriberName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> actionClass;
			try {
				actionClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				actionClass = Class.forName(className);
			}

			ChannelSubscriber instance = this.channelSubscriberMap.get(className);
			if (instance != null) {
				return instance;
			}

			if (ChannelSubscriber.class.isAssignableFrom(actionClass)) {
				instance = this.getChannelSubscriber((Class<ChannelSubscriber>) actionClass);
				this.channelSubscriberMap.put(className, instance);
				return instance;
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("channelSubscriber name is not found : " + className);
	}
	@SuppressWarnings("unchecked")
	public final ChannelSubscriber getChannelSubscriber(String subscriberName) {
		return getChannelSubscriber(subscriberName, "");
	}
	@SuppressWarnings("unchecked")
	public final ChannelSubscriber getChannelSubscriber(Class<ChannelSubscriber> subsClass) {
		try {
			Constructor<ChannelSubscriber> constructor = subsClass.getConstructor();
			ChannelSubscriber instance = constructor.newInstance();
			this.channelSubscriberMap.put(subsClass.getCanonicalName(), instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public final MessageCoordinator getMessageCoordinator(String coordinatorName, String defaultClassName) {
		String className = coordinatorName;
		try {
			className = this.moduleConfig.getValue(coordinatorName);
		} catch (NoSuchConfigOptionException ignored) {
		}

		try {
			Class<?> actionClass;
			try {
				actionClass = Class.forName(className);
			} catch (ClassNotFoundException | NullPointerException e) {
				className = defaultClassName;
				actionClass = Class.forName(className);
			}

			MessageCoordinator instance = this.messageCoordinatorMap.get(className);
			if (instance != null) {
				return instance;
			}

			if (MessageCoordinator.class.isAssignableFrom(actionClass)) {
				instance = this.getMessageCoordinator((Class<MessageCoordinator>) actionClass);
				this.messageCoordinatorMap.put(className, instance);
				return instance;
			}
		} catch (ClassNotFoundException | NullPointerException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalArgumentException("channelSubscriber name is not found : " + className);
	}
	@SuppressWarnings("unchecked")
	public final MessageCoordinator getMessageCoordinator(String coordinatorName) {
		return getMessageCoordinator(coordinatorName, "");
	}
	@SuppressWarnings("unchecked")
	public final MessageCoordinator getMessageCoordinator(Class<MessageCoordinator> subsClass) {
		try {
			Constructor<MessageCoordinator> constructor = subsClass.getConstructor();
			MessageCoordinator instance = constructor.newInstance();
			this.messageCoordinatorMap.put(subsClass.getCanonicalName(), instance);
			return instance;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public ModuleConfig getModuleConfig() {
		return this.moduleConfig;
	}
}

