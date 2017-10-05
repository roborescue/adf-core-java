package adf.agent.develop;

import adf.launcher.ConsoleOutput;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class DevelopData {
	public static final String DEFAULT_FILE_NAME = System.getProperty("user.dir") + File.separator + "data" + File.separator + "develop.json";

	private boolean developFlag;

	private Map<String, Integer> intValues;
	private Map<String, Double> doubleValues;
	private Map<String, String> stringValues;
	private Map<String, Boolean> boolValues;

	private Map<String, List<Integer>> intLists;
	private Map<String, List<Double>> doubleLists;
	private Map<String, List<String>> stringLists;
	private Map<String, List<Boolean>> boolLists;

	public DevelopData(boolean developFlag, @Nonnull String developDataFileName, @Nonnull List<String> rawData) {
		this.developFlag = developFlag;

		this.intValues = new HashMap<>();
		this.doubleValues = new HashMap<>();
		this.stringValues = new HashMap<>();
		this.boolValues = new HashMap<>();

		this.intLists = new HashMap<>();
		this.doubleLists = new HashMap<>();
		this.stringLists = new HashMap<>();
		this.boolLists = new HashMap<>();

		if (developFlag) {
			this.setDataFile(developDataFileName);
			this.setRawData(rawData);
		}
	}

	public boolean isDevelopMode() {
		return this.developFlag;
	}

	@Nonnull
	public Integer getInteger(@Nonnull String name, int defaultValue) {
		if (this.developFlag) {
			Integer value = this.intValues.get(name);
			if (value == null) {
				String rawData = this.stringValues.get(name);
				if (rawData != null && !rawData.equals("")) {
					value = Integer.valueOf(rawData);
				}
				if (value != null) {
					this.intValues.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nonnull
	public Double getDouble(@Nonnull String name, double defaultValue) {
		if (this.developFlag) {
			Double value = this.doubleValues.get(name);
			if (value == null) {
				String rawData = this.stringValues.get(name);
				if (rawData != null && !rawData.equals("")) {
					value = Double.valueOf(rawData);
				}
				if (value != null) {
					this.doubleValues.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nonnull
	public Boolean getBoolean(@Nonnull String name, boolean defaultValue) {
		if (this.developFlag) {
			Boolean value = this.boolValues.get(name);
			if (value == null) {
				String rawData = this.stringValues.get(name);
				if (rawData != null && !rawData.equals("")) {
					value = Boolean.valueOf(rawData);
				}
				if (value != null) {
					this.boolValues.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nullable
	public String getString(@Nonnull String name, @Nullable String defaultValue) {
		if (this.developFlag) {
			String value = this.stringValues.get(name);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nullable
	public List<Integer> getIntegerList(@Nonnull String name, @Nullable List<Integer> defaultValue) {
		if (this.developFlag) {
			List<Integer> value = this.intLists.get(name);
			if (value == null || value.isEmpty()) {
				List<String> rawData = this.stringLists.get(name);
				if (rawData != null) {
					value = new ArrayList<>();
					for (String str : rawData) {
						value.add(Integer.valueOf(str));
					}
				}
				if (value != null) {
					this.intLists.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nullable
	public List<Double> getDoubleList(@Nonnull String name, @Nullable List<Double> defaultValue) {
		if (this.developFlag) {
			List<Double> value = this.doubleLists.get(name);
			if (value == null || value.isEmpty()) {
				List<String> rawData = this.stringLists.get(name);
				if (rawData != null) {
					value = new ArrayList<>();
					for (String str : rawData) {
						value.add(Double.valueOf(str));
					}
				}
				if (value != null) {
					this.doubleLists.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nullable
	public List<Boolean> getBooleanList(@Nonnull String name, @Nullable List<Boolean> defaultValue) {
		if (this.developFlag) {
			List<Boolean> value = this.boolLists.get(name);
			if (value == null || value.isEmpty()) {
				List<String> rawData = this.stringLists.get(name);
				if (rawData != null) {
					value = new ArrayList<>();
					for (String str : rawData) {
						value.add(Boolean.valueOf(str));
					}
				}
				if (value != null) {
					this.boolLists.put(name, value);
				}
			}
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Nullable
	public List<String> getStringList(@Nonnull String name, @Nullable List<String> defaultValue) {
		if (this.developFlag) {
			List<String> value = this.stringLists.get(name);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	private void setRawData(@Nonnull String rawData, boolean isBase64) {
		if (rawData.equals("")) {
			return;
		}
		String data = (isBase64 ? new String(Base64.getDecoder().decode(rawData)) : rawData);

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> dataMap = new HashMap<>();

		try {
			dataMap = mapper.readValue(data, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			ConsoleOutput.out(ConsoleOutput.State.WARN, "DevelopData input is invalid : " + data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String key : dataMap.keySet()) {
			Object object = dataMap.get(key);
			if (object instanceof List) {
				List<String> list = new ArrayList<>(((List) object).size());
				for (Object o : (List) object) {
					list.add(String.valueOf(o));
				}
				this.stringLists.put(key, list);
			} else {
				this.stringValues.put(key, String.valueOf(object));
			}
		}
	}

	private void setRawData(@Nonnull List<String> rawData) {
		for (String data : rawData) {
			setRawData(data, true);
		}
	}

	private void setDataFile(@Nonnull String developDataFileName) {
		if (developDataFileName.equals("")) {
			return;
		}
		File file = new File(developDataFileName);
		if (developDataFileName.equals(DEFAULT_FILE_NAME) && !(file.isFile())) {
			return;
		}

		String rawData = "";
		try {
			rawData = Files.lines(
				Paths.get(file.getPath()),
				Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		setRawData(rawData, false);
	}

	public void clear() {
		this.intValues.clear();
		this.doubleValues.clear();
		this.stringValues.clear();
		this.boolValues.clear();

		this.intLists.clear();
		this.doubleLists.clear();
		this.stringLists.clear();
		this.boolLists.clear();
	}

}
