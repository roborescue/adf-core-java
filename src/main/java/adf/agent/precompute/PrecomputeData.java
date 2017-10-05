package adf.agent.precompute;

import adf.agent.info.WorldInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public final class PrecomputeData {
	public static final String DEFAULT_FILE_NAME = "data.bin";
	public static final File PRECOMP_DATA_DIR = new File("precomp_data");

	private String fileName;

	private PreData data;

	public PrecomputeData() {
		this(DEFAULT_FILE_NAME);
	}

	public PrecomputeData(String name) {
		this.fileName = name;
		this.init();
	}

	private PrecomputeData(String name, PreData precomputeDatas) {
		this.fileName = name;
		this.data = precomputeDatas;
	}

	public static void removeData(String name) {
		if (!PRECOMP_DATA_DIR.exists()) {
			return;
		}

		File file = new File(PRECOMP_DATA_DIR, name);
		if (!file.exists()) {
			return;
		}

		file.delete();
	}

	public static void removeData() {
		removeData(DEFAULT_FILE_NAME);
	}

	public PrecomputeData copy() {
		return new PrecomputeData(this.fileName, this.data.copy());
	}

	private void init() {
		this.data = this.read(this.fileName);
		if (this.data == null) {
			this.data = new PreData();
		}
	}

	private PreData read(String name) {
		try {
			if (!PRECOMP_DATA_DIR.exists()) {
				if (!PRECOMP_DATA_DIR.mkdir()) {
					return null;
				}
			}

			File readFile = new File(PRECOMP_DATA_DIR, name);
			if (!readFile.exists()) {
				return null;
			}

			FileInputStream fis = new FileInputStream(readFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] binary = new byte[1024];
			while (true) {
				int len = bis.read(binary);
				if (len < 0) {
					break;
				}
				bout.write(binary, 0, len);
			}

			binary = bout.toByteArray();
			ObjectMapper om = new ObjectMapper(new MessagePackFactory());
			PreData ds = om.readValue(binary, PreData.class);
			bis.close();
			fis.close();
			return ds;
		} catch (IOException e) {
			return null;
		}
	}

	public boolean write() {
		try {
			if (!PRECOMP_DATA_DIR.exists()) {
				if (!PRECOMP_DATA_DIR.mkdir()) {
					return false;
				}
			}
			ObjectMapper om = new ObjectMapper(new MessagePackFactory());
			byte[] binary = om.writeValueAsBytes(this.data);
			FileOutputStream fos = new FileOutputStream(new File(PRECOMP_DATA_DIR, this.fileName));
			fos.write(binary);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			;
			return false;
		}
	}

	public Integer setInteger(String name, int value) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.intValues.put(callClassName + ":" + name, value);
	}

	public Double setDouble(String name, double value) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.doubleValues.put(callClassName + ":" + name, value);
	}

	public Boolean setBoolean(String name, boolean value) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.boolValues.put(callClassName + ":" + name, value);
	}

	public String setString(String name, String value) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.stringValues.put(callClassName + ":" + name, value);
	}

	public EntityID setEntityID(String name, EntityID value) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		Integer id = this.data.idValues.put(callClassName + ":" + name, value.getValue());
		return id == null ? null : new EntityID(id);
	}

	public List<Integer> setIntegerList(String name, List<Integer> list) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.intLists.put(callClassName + ":" + name, list);
	}

	public List<Double> setDoubleList(String name, List<Double> list) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.doubleLists.put(callClassName + ":" + name, list);
	}

	public List<String> setStringList(String name, List<String> list) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.stringLists.put(callClassName + ":" + name, list);
	}

	public List<EntityID> setEntityIDList(String name, List<EntityID> list) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		List<Integer> cvtList = new ArrayList<>();
		for (EntityID id : list) {
			cvtList.add(id.getValue());
		}

		cvtList = this.data.idLists.put(callClassName + ":" + name, cvtList);
		return cvtList == null ? null : cvtList.stream().map(EntityID::new).collect(Collectors.toList());
	}

	public List<Boolean> setBooleanList(String name, List<Boolean> list) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.boolLists.put(callClassName + ":" + name, list);
	}

	public boolean setReady(boolean isReady, WorldInfo worldInfo) {
		this.data.isReady = isReady;
		this.data.readyID = makeReadyID(worldInfo);
		return (this.data.isReady && this.data.readyID.equals(this.makeReadyID(worldInfo)));
	}

	public Integer getInteger(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.intValues.get(callClassName + ":" + name);
	}

	public Double getDouble(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.doubleValues.get(callClassName + ":" + name);
	}

	public Boolean getBoolean(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.boolValues.get(callClassName + ":" + name);
	}

	public String getString(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.stringValues.get(callClassName + ":" + name);
	}

	public EntityID getEntityID(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		Integer id = this.data.idValues.get(callClassName + ":" + name);
		return id == null ? null : new EntityID(id);
	}

	public List<Integer> getIntegerList(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.intLists.get(callClassName + ":" + name);
	}

	public List<Double> getDoubleList(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.doubleLists.get(callClassName + ":" + name);
	}

	public List<String> getStringList(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.stringLists.get(callClassName + ":" + name);
	}

	public List<EntityID> getEntityIDList(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		List<Integer> cvtList = this.data.idLists.get(callClassName + ":" + name);
		return cvtList == null ? null : cvtList.stream().map(EntityID::new).collect(Collectors.toList());
	}

	public List<Boolean> getBooleanList(String name) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if (stackTraceElements == null || stackTraceElements.length == 0) {
			return null;
		}

		String callClassName = stackTraceElements[2].getClassName();
		return this.data.boolLists.get(callClassName + ":" + name);
	}

	public boolean isReady(WorldInfo worldInfo) {
		return (this.data.isReady && this.data.readyID.equals(this.makeReadyID(worldInfo)));
	}

	private String makeReadyID(WorldInfo worldInfo) {
		return "" + worldInfo.getBounds().getX() + "" + worldInfo.getBounds().getY() + "" + worldInfo.getAllEntities().size();
	}
}

