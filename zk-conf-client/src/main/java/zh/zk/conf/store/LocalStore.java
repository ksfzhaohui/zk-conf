package zh.zk.conf.store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zh.zk.conf.ZKPropPlaceholderConfigurer;

/**
 * 本地文件存储配置数据
 * 
 * @author hui.zhao
 *
 */
public class LocalStore {

	private static Logger log = LoggerFactory.getLogger(LocalStore.class);

	private static String lineSeparator = (String) System.getProperties().get("line.separator");
	private static String localStoreUrl;

	static {
		String filename = ZKPropPlaceholderConfigurer.getAppName();
		StringBuilder sb = new StringBuilder();
		sb.append(System.getProperty("java.io.tmpdir")).append(System.getProperty("file.separator")).append(filename);
		localStoreUrl = sb.toString();
		log.info("localStoreUrl=" + localStoreUrl);
	}

	/**
	 * 存储配置数据到文件
	 * 
	 * @param map
	 */
	public static synchronized void store(Map<String, String> map) {
		Iterator<String> keyItor = map.keySet().iterator();
		Writer fw = null;
		try {
			fw = new BufferedWriter(new FileWriter(new File(localStoreUrl)));
			while (keyItor.hasNext()) {
				String key = (String) keyItor.next();
				fw.write(key + "=" + (String) map.get(key) + lineSeparator);
			}
			log.info("刷新本地存储信息成功，调整内容:" + map.toString());
		} catch (IOException e) {
			log.error("更新本地存储配置信息失败", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
			fw = null;
		}
	}

	/**
	 * 从文件中加载存储的配置数据
	 * 
	 * @return
	 */
	public static synchronized HashMap<String, String> load() {
		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader bfr = null;
		try {
			bfr = new BufferedReader(new InputStreamReader(new FileInputStream(localStoreUrl)));
			String line = bfr.readLine();
			while (line != null) {
				if ((line.trim().length() != 0) && (line.trim().indexOf("#") != 0)) {
					int idx = line.indexOf("=");
					if (idx != -1) {
						map.put(new String(line.substring(0, idx)), new String(line.substring(idx + 1)));
					}
					line = bfr.readLine();
				}
			}
			if (null != bfr) {
				try {
					bfr.close();
				} catch (IOException e) {
				}
			}
			bfr = null;
		} catch (Exception e) {
			log.error("在远程获取配置失效情况下，从本地存储{}加载配置信息失败", localStoreUrl, e);
			throw new RuntimeException(e.getMessage());
		} finally {
			if (null != bfr) {
				try {
					bfr.close();
				} catch (IOException e) {
				}
			}
			bfr = null;
		}
		return map;
	}

}
