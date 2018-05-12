package adf.launcher;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class CoreUpdater {
	private final String CORE_URL = "https://raw.githubusercontent.com/roborescue/rcrs-adf-core/jar/build/libs/adf-core.jar";
	private final String COREMD5_URL = "https://raw.githubusercontent.com/roborescue/rcrs-adf-core/jar/build/libs/adf-core.jar.MD5";
	private final String CORESRC_URL = "https://raw.githubusercontent.com/roborescue/rcrs-adf-core/jar/build/libs/adf-core-sources.jar";
	private final String SOURCES_DIR = "sources";
	private final String SOURCES_FILE = "adf-core-sources.jar";

	public CoreUpdater() {
	}

	public boolean updateCore() {
		File coreFile = new File(System.getProperty("java.class.path"));
		String corePath = coreFile.getAbsolutePath();
		String sourcePath = coreFile.getParentFile().getAbsolutePath() + File.separator + SOURCES_DIR + File.separator + SOURCES_FILE;
		Random random = new Random();

		try {
			ConsoleOutput.start("Download jars");
			download(CORE_URL + "?" + random.nextInt(), corePath, true);
			download(CORESRC_URL + "?" + random.nextInt(), sourcePath, false);
			ConsoleOutput.finish("Download jars");
		} catch (Exception e) {
			ConsoleOutput.error("Download jars failed : " + e.toString());
			return false;
		}

		return true;
	}

	public boolean checkUpdate() {
		try {
			Random random = new Random();
			URL url = new URL(COREMD5_URL + "?" + random.nextInt());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setAllowUserInteraction(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod("GET");
			connection.connect();

			int httpStatusCode = connection.getResponseCode();
			if (httpStatusCode != HttpURLConnection.HTTP_OK) {
				return false;
			}

			InputStream dataInStream = new DataInputStream(connection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInStream));
			String md5 = bufferedReader.readLine();

			String currentMD5 = createMD5Digest((new File(System.getProperty("java.class.path"))).getAbsoluteFile());

			if (!(md5.equals(currentMD5))) {
				ConsoleOutput.info("adf-core update available");
				return true;
			}
		} catch (Exception e) {
		}

		return false;
	}

	private void download(String urlStr, String fileStr, boolean isUpdateEtag) throws Exception {
		URL url = new URL(urlStr);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setAllowUserInteraction(false);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestMethod("GET");
		connection.connect();

		int httpStatusCode = connection.getResponseCode();
		if (httpStatusCode != HttpURLConnection.HTTP_OK) {
			throw new Exception("Status code : " + httpStatusCode);
		}

		InputStream dataInStream = new DataInputStream(connection.getInputStream());
		OutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileStr)));

		byte[] bytes = new byte[4096];
		int readByte = 0;
		while (-1 != (readByte = dataInStream.read(bytes))) {
			dataOutStream.write(bytes, 0, readByte);
		}

		dataInStream.close();
		dataOutStream.close();
	}

	private String createMD5Digest(File file) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream in = new FileInputStream(file);

		try {
			byte[] buff = new byte[256];
			int len = 0;
			while ((len = in.read(buff, 0, buff.length)) >= 0) {
				md.update(buff, 0, len);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}

		return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
	}
}
