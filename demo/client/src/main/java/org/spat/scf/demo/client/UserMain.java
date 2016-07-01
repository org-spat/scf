package org.spat.scf.demo.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.spat.scf.client.SCFInit;
import org.spat.scf.client.proxy.component.ProxyFactory;
import org.spat.scf.demo.contract.IUserInfoService;
import org.spat.scf.demo.entity.UserInfo;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class UserMain {

	public static String url = "tcp://demo/UserInfoService";
	public static IUserInfoService service = ProxyFactory.create(IUserInfoService.class, url);

	private static CountDownLatch latch = new CountDownLatch(1);

	public static void testThreads() throws Exception {
		long start = System.currentTimeMillis();
		for (int j = 1; j <= 60; j++) {
			System.err.println("线程数：" + j);
			// Thread.sleep(0);
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i <= 200000; i++) {
						getUser();
					}
					latch.countDown();
				}
			}).start();
		}
		latch.await();
		long ent = System.currentTimeMillis();
		System.out.println("耗时：" + (ent - start));
		System.out.println();
	}

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		String userDir = System.getProperty("user.dir");
		String rootPath = userDir + "/config/";
		System.out.println("user.dir: " + userDir);

		SCFInit.init(rootPath + "scf.config");

		addUser();
		System.out.println(
				"======================================================================================================================================================================");
		getUser();
		System.out.println(
				"======================================================================================================================================================================");
		getUserList();
		System.out.println("=======压测开始============");
		try {
			testThreads();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getUserList() {
		List<UserInfo> listUserInfo = new ArrayList<UserInfo>();
		try {
			listUserInfo = service.getUserList(12);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (listUserInfo != null && listUserInfo.isEmpty() == false) {
			for (UserInfo user : listUserInfo) {
				System.out.println(
						"get userlist: userid=" + user.getUserId() + " userName=" + user.getUserName() + " password="
								+ user.getPassword() + " birthday=" + user.getBirthday() + " age=" + user.getAge());
			}
		}
	}

	private static void getUser() {
		UserInfo user = null;
		try {
			user = service.getUserByUserId(1223L);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (user != null) {
			System.out.println("getuser: userid=" + user.getUserId() + " userName=" + user.getUserName() + " password="
					+ user.getPassword() + " birthday=" + user.getBirthday() + " age=" + user.getAge());
		}

	}

	private static void addUser() {
		UserInfo user = new UserInfo();
		user.setAge(12);
		user.setBirthday(new Date());
		user.setPassword("dfedfgfdg");
		user.setSex(true);
		user.setUserId(12336774L);
		user.setUserName("peida chang");

		try {
			service.insertUser(user);
			System.out.println("insertUser succ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void Test() throws NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.get("test.Rectangle");
		cc.getAttribute("");
	}

}
