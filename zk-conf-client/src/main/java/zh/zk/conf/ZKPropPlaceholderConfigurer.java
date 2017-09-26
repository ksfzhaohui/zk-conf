package zh.zk.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import zh.zk.conf.service.ZKConnect;
import zh.zk.conf.service.ZKWatcherService;
import zh.zk.conf.store.LocalStore;
import zh.zk.conf.store.MemoryStore;

/**
 * zk方式加载常量
 * 
 * @author hui.zhao
 *
 */
public class ZKPropPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private static Logger logger = LoggerFactory.getLogger(ZKPropPlaceholderConfigurer.class);

	private ZKWatcherService zkService;
	private static String appName;
	private static String[] keyPatterns;

	@Override
	protected Properties mergeProperties() throws IOException {
		return loadPropFromZK(super.mergeProperties());
	}

	/**
	 * 从zk中加载配置的常量
	 * 
	 * @param result
	 * @return
	 */
	private Properties loadPropFromZK(Properties result) {
		if (appName == null || appName.equals("")) {
			logger.error("参数异常:应用对应的appName标示不能为空");
			throw new IllegalArgumentException("the appName of this app must not be blank");
		}
		if ((null == keyPatterns) || (keyPatterns.length == 0)) {
			logger.error("参数异常:应用对应的Key标示不能为空");
			throw new IllegalArgumentException("the defaultkey of this app must not be blank");
		}
		Map<String, String> map = new HashMap<String, String>();
		try {
			ZKConnect.connect();
			zkService.watcherKeys(keyPatterns);
			map = MemoryStore.getMap();
		} catch (Exception e) {
			logger.error("从远程获取动态配置信息失败", e);
			try {
				map = LocalStore.load();
			} catch (Exception ex) {
				logger.error("加载本地存储失败:", ex);
				throw new RuntimeException("loading local store failure!");
			}
		}
		fillProperties(result, map);
		return result;
	}

	/**
	 * 将所有keyValue填充Properties
	 * 
	 * @param result
	 */
	public void fillProperties(Properties result, Map<String, String> map) {
		Iterator<String> keyItor = map.keySet().iterator();
		while (keyItor.hasNext()) {
			String key = (String) keyItor.next();
			String v = (String) map.get(key);
			result.put(key, v != null ? v : "");
		}
	}

	public static String getAppName() {
		return appName;
	}

	public static void setAppName(String appName) {
		ZKPropPlaceholderConfigurer.appName = appName;
	}

	public static String[] getKeyPatterns() {
		return keyPatterns;
	}

	/**
	 * 设值同时去重
	 * 
	 * @param keyPatterns
	 */
	public static void setKeyPatterns(String[] keyPatterns) {
		if ((keyPatterns == null) || (keyPatterns.length == 0)) {
			ZKPropPlaceholderConfigurer.keyPatterns = keyPatterns;
		} else {
			Set<String> set = new HashSet<String>();
			for (String key : keyPatterns) {
				set.add(key);
			}
			ZKPropPlaceholderConfigurer.keyPatterns = (String[]) set.toArray(new String[0]);
		}
	}

	public void setZkService(ZKWatcherService zkService) {
		this.zkService = zkService;
	}

}
