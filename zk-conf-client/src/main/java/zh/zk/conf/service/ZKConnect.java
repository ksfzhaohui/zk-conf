package zh.zk.conf.service;

import java.util.concurrent.CountDownLatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKConnect {

	private static Logger logger = LoggerFactory.getLogger(ZKConnect.class);

	private static final String zkAddress = "127.0.0.1:2181";
	private static final int timeout = 10000;
	private static CountDownLatch countDownLatch = new CountDownLatch(1);
	private static CuratorFramework client = null;

	public static CuratorFramework getClient() {
		return client;
	}

	/**
	 * 与zk进行连接
	 * 
	 * @throws InterruptedException
	 */
	public static void connect() throws InterruptedException {
		client = CuratorFrameworkFactory.builder().connectString(zkAddress).sessionTimeoutMs(timeout)
				.retryPolicy(new RetryNTimes(5, 5000)).build();
		client.getConnectionStateListenable().addListener(connectionListener);
		client.start();
		countDownLatch.await();
	}

	/**
	 * 获取指定path对应的value
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static String readPath(String path) throws Exception {
		byte[] buffer = ZKConnect.getClient().getData().forPath(path);
		String value = new String(buffer);
		logger.info("readPath:path = " + path + ",value = " + value);
		return value;
	}

	/** zk连接监听器 **/
	private static ConnectionStateListener connectionListener = new ConnectionStateListener() {

		@Override
		public void stateChanged(CuratorFramework client, ConnectionState connectionState) {
			if (connectionState == ConnectionState.CONNECTED) {
				logger.info("connected established");
				countDownLatch.countDown();
			} else if (connectionState == ConnectionState.LOST) {
				logger.info("connection lost, waiting for reconection");
				try {
					reconnect();
				} catch (Exception e) {
					logger.error("reconnect error", e);
				}
			}

		}
	};

	/**
	 * 与zk重新连接
	 * 
	 * @throws InterruptedException
	 */
	private static void reconnect() throws InterruptedException {
		unregister();
		connect();
	}

	private static void unregister() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

}
