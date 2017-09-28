package zh.zk.conf;

/**
 * 异常类
 * 
 * @author hui.zhao
 *
 */
public class ZKConfException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ZKConfException() {
	}

	public ZKConfException(String message) {
		super(message);
	}

	public ZKConfException(Throwable throwable) {
		super(throwable);
	}

	public ZKConfException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
