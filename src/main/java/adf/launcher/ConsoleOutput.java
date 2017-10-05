package adf.launcher;

import adf.Main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static adf.Main.VERSION_CODE;

public class ConsoleOutput {
	private static final int titleLength = 6;

	private static final Semaphore semaphore = new Semaphore(1, true);

	public static enum State {INFO, WARN, ERROR, NOTICE, START, FINISH}

	;

	public static void out(State state, String out) {

		boolean colorMode = System.getProperty("color", "0").equals("1");
		if (colorMode) {
			switch (state) {
				case INFO:
					System.out.print("\u001B[36m");
					break;
				case WARN:
					System.out.print("\u001B[33m");
					break;
				case ERROR:
					System.out.print("\u001B[31m");
					break;
				case NOTICE:
					System.out.print("\u001B[35m");
					break;
				case START:
				case FINISH:
					System.out.print("\u001B[32m");
					break;
			}
		}

		try {
			semaphore.acquire();
			System.out.print('[');
			System.out.print(state.name());
			for (int i = state.name().length(); i < titleLength; i++) {
				System.out.print(' ');
			}
			System.out.print("]" + (colorMode ? "\u001B[0m " : " "));
			System.out.println(out);
			semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void info(String out) {
		out(State.INFO, out);
	}

	public static void warn(String out) {
		out(State.WARN, out);
	}

	public static void error(String out) {
		out(State.ERROR, out);
	}

	public static void notice(String out) {
		out(State.NOTICE, out);
	}

	public static void start(String out) {
		out(State.START, out);
	}

	public static void finish(String out) {
		out(State.FINISH, out);
	}

	public static void version() {
		System.out.println("[ RCRS ADF Version " + VERSION_CODE + " (build " + getTimestamp() + ") ]\n");
	}

	private static String getTimestamp() {
		String buildTimestamp = "null";

		Class clazz = Main.class;
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
			return buildTimestamp;
		}

		String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		try {
			Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			Attributes attributes = manifest.getMainAttributes();
			buildTimestamp = attributes.getValue("Build-Timestamp");
		} catch (IOException e) {
		}

		return buildTimestamp;
	}
}
