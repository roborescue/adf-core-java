package adf.core.launcher;

import adf.core.component.AbstractLoader;
import adf.core.launcher.connect.Connector;
import adf.core.launcher.connect.ConnectorAmbulanceCentre;
import adf.core.launcher.connect.ConnectorAmbulanceTeam;
import adf.core.launcher.connect.ConnectorFireBrigade;
import adf.core.launcher.connect.ConnectorFireStation;
import adf.core.launcher.connect.ConnectorPoliceForce;
import adf.core.launcher.connect.ConnectorPoliceOffice;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import rescuecore2.Constants;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.config.Config;
import rescuecore2.registry.Registry;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

public class AgentLauncher {

  private Config config;

  private AbstractLoader loader;

  private List<Connector> connectors;

  public AgentLauncher(String... args) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    this.init(args);
  }


  private void init(String... args)
      throws ClassNotFoundException, ClassCastException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    this.initSystem();
    this.config = ConfigInitializer.getConfig(args);
    this.initConnector();

    if (this.config.getBooleanValue(ConfigKey.KEY_DEBUG_FLAG, false)) {
      ConsoleOutput.info("*** DEBUG MODE ***");
    }

    if (this.config.getBooleanValue(ConfigKey.KEY_DEVELOP_FLAG, false)) {
      ConsoleOutput.info("*** DEVELOP MODE ***");
    }
  }


  private void initSystem() {
    // register rescue system
    Registry.SYSTEM_REGISTRY.registerFactory(StandardEntityFactory.INSTANCE);
    Registry.SYSTEM_REGISTRY.registerFactory(StandardMessageFactory.INSTANCE);
    Registry.SYSTEM_REGISTRY.registerFactory(StandardPropertyFactory.INSTANCE);
  }


  private void initConnector()
      throws ClassNotFoundException, ClassCastException, InstantiationException,
      IllegalAccessException, NoSuchMethodException, InvocationTargetException {

    // Load AbstractLoader
    ClassLoader classLoader = this.getClass().getClassLoader();
    Class<?> c = classLoader
        .loadClass(this.config.getValue(ConfigKey.KEY_LOADER_CLASS));
    this.loader = (AbstractLoader) c.getDeclaredConstructor().newInstance();
    this.loader
        .setTeamName(this.config.getValue(ConfigKey.KEY_TEAM_NAME, "not_set"));

    // Set Connectors
    this.connectors = new ArrayList<>();

    // Platoon
    this.registerConnector(new ConnectorAmbulanceTeam());
    this.registerConnector(new ConnectorFireBrigade());
    this.registerConnector(new ConnectorPoliceForce());

    // Office
    this.registerConnector(new ConnectorAmbulanceCentre());
    this.registerConnector(new ConnectorFireStation());
    this.registerConnector(new ConnectorPoliceOffice());
  }


  private void registerConnector(Connector connector) {
    this.connectors.add(connector);
  }


  public void start() {
    String host = this.config.getValue(ConfigKey.KEY_KERNEL_HOST,
        Constants.DEFAULT_KERNEL_HOST_NAME);
    int port = this.config.getIntValue(ConfigKey.KEY_KERNEL_PORT,
        Constants.DEFAULT_KERNEL_PORT_NUMBER);
    ComponentLauncher launcher = new TCPComponentLauncher(host, port,
        this.config);
    ConsoleOutput.out(ConsoleOutput.State.START,
        "Connect to server (host:" + host + ", port:" + port + ")");

    List<Thread> threadList = this.connectors.stream()
        .map(connector -> new Thread(() -> {
          connector.connect(launcher, this.config, loader);
        })).collect(Collectors.toList());

    threadList.forEach(Thread::start);

    try {
      for (Thread thread : threadList) {
        thread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    int connectedCount = this.connectors.stream()
        .mapToInt(Connector::getCountConnected).sum();

    ConsoleOutput.finish("Done connecting to server (" + connectedCount
        + " agent" + (connectedCount > 1 ? 's' : "") + ")");

    if (this.config.getBooleanValue(ConfigKey.KEY_PRECOMPUTE, false)) {
      System.exit(0);
    }
  }
}