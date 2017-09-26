package zh.zk.conf.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本地内存存储配置数据
 * 
 * @author hui.zhao.cfs
 *
 */
public class MemoryStore {

	private static Logger logger = LoggerFactory.getLogger(MemoryStore.class);

	/** 配置内存数据 **/
	private static Map<String, String> keyValueMap = new ConcurrentHashMap<String, String>();

	/**
	 * 批次更新内存数据，同时写入文件
	 * 
	 * @param map
	 */
	public static synchronized void updateMap(Map<String, String> map) {
		try {
			keyValueMap.putAll(map);
			LocalStore.store(keyValueMap);
		} catch (Exception e) {
			logger.error("更新动态配置信息失败:" + map, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 更新单个值
	 * 
	 * @param key
	 * @param value
	 */
	public static synchronized void updateKV(String key, String value) {
		try {
			keyValueMap.put(key, value);
			LocalStore.store(keyValueMap);
		} catch (Exception e) {
			logger.error("更新动态配置信息失败:key=" + key + ",value=" + value, e);
			throw new RuntimeException(e);
		}
	}

	public static synchronized Map<String, String> getMap() {
		return keyValueMap;
	}

	public static synchronized boolean containsKey(String key) {
		return keyValueMap.containsKey(key);
	}

	/**
	 * 获取指定key对应的值
	 * 
	 * @param key
	 * @return
	 */
	public static synchronized String getValue(String key) {
		String value = null;
		try {
			value = keyValueMap.get(key);
		} catch (Exception e) {
			logger.error("获取动态配置信息失败:key=" + key, e);
			throw new RuntimeException(e);
		}
		return value;
	}

}
