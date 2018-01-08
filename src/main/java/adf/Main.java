package adf;

import adf.launcher.AgentLauncher;
import adf.launcher.ConsoleOutput;
import adf.launcher.LaunchSupporter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.InvocationTargetException;

public class Main {
	public static final String VERSION_CODE = "2.3.0";

	public static void main(String... args) {
		ConsoleOutput.version();
		List<String> launcherArguments = new ArrayList<>();
		launcherArguments.addAll(Arrays.asList(args));
		(new LaunchSupporter()).delegate(launcherArguments);
		System.gc();

		try {
			AgentLauncher connector = new AgentLauncher((String[]) launcherArguments.toArray(new String[0]));
			connector.start();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			ConsoleOutput.out(ConsoleOutput.State.ERROR, "Loader is not found.");
			e.printStackTrace();
		}
	}
}

