package adf.agent.config;

import adf.launcher.ConsoleOutput;
import adf.launcher.annotation.NoStructureWarning;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ModuleConfig extends Config {
	public static final String DEFAULT_CONFIG_FILE_NAME = "config" + File.separator + "module.cfg";

	public ModuleConfig(@Nonnull String fileName, @Nonnull List<String> rawData) {
		super();

		if (this.isJson(fileName)) {
			this.setDataFile(fileName);
			this.setRawData(rawData);
		} else {
			try {
				read(new File(fileName));
			} catch (ConfigException e) {
				e.printStackTrace();
				throw new RuntimeException("ModuleConfig file is not found : " + fileName);
			}
		}
	}

	private boolean isJson(@Nonnull String fileName) {
		String pattern = "^\\s*\\{";

		if (fileName.equals("")) {
			return false;
		}
		File file = new File(fileName);
		if (!(file.isFile())) {
			return false;
		}

		String rawData = "";
		try {
			rawData = Files.lines(Paths.get(file.getPath()), Charset.forName("UTF-8"))
				.collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Pattern.compile(pattern).matcher(rawData).find();
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
			ConsoleOutput.out(ConsoleOutput.State.WARN, "ModuleData input is invalid : " + data);
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
				setValue(key, list.toString());
			} else {
				setValue(key, String.valueOf(object));
			}
		}
	}

	private void setRawData(@Nonnull List<String> rawData) {
		for (String data : rawData) {
			setRawData(data, true);
		}
	}

	private void setDataFile(@Nonnull String developDataFileName) {
		File file = new File(developDataFileName);

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
}

