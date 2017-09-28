package zh.zk.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zh.zk.conf.service.ZKWatcherService;
import zh.zk.conf.store.MemoryStore;

/**
 * 动态参数获取工具类
 * 
 * @author hui.zhao
 *
 */
public class DynamicConfUtil {

	private static Logger logger = LoggerFactory.getLogger(DynamicConfUtil.class);

	/**
	 * 获取动态配置
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		boolean bool = MemoryStore.containsKey(key);
		if (!bool) {
			try {
				String value = ZKWatcherService.readPath(key);
				MemoryStore.updateKV(key, value);
			} catch (Exception e) {
				logger.error("readPath error key=" + key, e);
			}
		}
		return MemoryStore.getValue(key);
	}

}
