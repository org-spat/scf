package org.spat.scf.maven;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal run
 */
public class RunMojo extends AbstractSCFMojo {

	public static void main(String[] args) throws Exception {
		RunMojo runMojo = new RunMojo();
		runMojo.name = "demo";
		runMojo.scfhome = "d:/scfv3/";
		runMojo.execute();
		Thread.currentThread().join();
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		Deployer deployer = new Deployer();
		deployer.deploy(this);
		getLog().info("===================开始启动SCF服务===================");
		String[] args = new String[1];
		args[0] = "-Dscf.service.name=" + name;
		System.setProperty("user.dir", scfhome+"/bin");
		try {
			ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
			Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			File path = new File(scfhome + "/lib");
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.getName().endsWith(".jar")) {
					method.invoke(sysLoader, file.toURI().toURL());
				}
			}
			method.setAccessible(false);
			Thread.currentThread().setContextClassLoader(sysLoader);
			Class<?> mainCls = sysLoader.loadClass("org.spat.scf.server.bootstrap.Main");
			Method main = mainCls.getMethod("main", String[].class);
			main.invoke(null, (Object) args);
			getLog().info("===================SCF启动成功===================");
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
