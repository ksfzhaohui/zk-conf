package zh.zk.conf.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zh.zk.conf.store.MemoryStore;

/**
 * 基于zk的参数通知
 * 
 * @author hui.zhao.cfs
 *
 */
public class ZKWatcherService {

	private static Logger logger = LoggerFactory.getLogger(ZKWatcherService.class);

	/**
	 * 监听所有keyPatterns
	 * 
	 * @throws Exception
	 */
	public void watcherKeys(String[] keyPatterns) throws Exception {
		List<String> pathList = new ArrayList<String>();
		for (String key : keyPatterns) {
			pathList.addAll(listChildren(key));
		}

		logger.info("watcher path : " + pathList);

		if (pathList != null && pathList.size() > 0) {
			Map<String, String> map = new HashMap<String, String>();
			for (String path : pathList) {
				map.put(path, ZKConnect.readPath(path));
				watcherPath(path);
			}
			MemoryStore.updateMap(map);
		}
	}

	/**
	 * 递归获取指定path下所以子path
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private List<String> listChildren(String path) throws Exception {
		List<String> pathList = new ArrayList<String>();
		pathList.add(path);
		List<String> list = ZKConnect.getClient().getChildren().forPath(path);
		if (list != null && list.size() > 0) {
			for (String cPath : list) {
				String temp = "";
				if ("/".equals(path)) {
					temp = path + cPath;
				} else {
					temp = path + "/" + cPath;
				}
				pathList.addAll(listChildren(temp));
			}
		}
		return pathList;
	}

	private void watcherPath(String path) {
		watcherPath(path, true);
	}

	/**
	 * 监听指定的path
	 * 
	 * @param path
	 */
	private void watcherPath(String path, final boolean isInit) {
		PathChildrenCache cache = null;
		try {
			cache = new PathChildrenCache(ZKConnect.getClient(), path, true);
			cache.start(StartMode.POST_INITIALIZED_EVENT);
			cache.getListenable().addListener(new PathChildrenCacheListener() {

				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch (event.getType()) {
					case CHILD_ADDED:
						logger.info("CHILD_ADDED," + event.getData().getPath());
						if (!isInit) {
							watcherPath(event.getData().getPath());
						}
						MemoryStore.updateKV(event.getData().getPath(), new String(event.getData().getData()));
						break;
					case CHILD_UPDATED:
						logger.info("CHILD_UPDATED," + event.getData().getPath());
						MemoryStore.updateKV(event.getData().getPath(), new String(event.getData().getData()));
						break;
					case CHILD_REMOVED:
						logger.info("CHILD_REMOVED," + event.getData().getPath());
						break;
					default:
						break;
					}
				}
			});
		} catch (Exception e) {
			if (cache != null) {
				try {
					cache.close();
				} catch (IOException e1) {
				}
			}
			logger.error("watch path error", e);
		}
	}

}
