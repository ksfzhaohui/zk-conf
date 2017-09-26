package zh.zk.conf.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
		Person person = (Person) context.getBean("person");
		System.out.println(person.toString());
		Thread.sleep(100000);
	}
}
