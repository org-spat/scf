package org.spat.scf.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

public class Deployer {
	public void deploy(AbstractSCFMojo mojo){
		Log loger = mojo.getLog();
		loger.info("===================开始部署===================");
		loger.info("SCF_HOME:"+mojo.getScfhome());
		loger.info("Service Name:"+mojo.getName());
		loger.info("root:"+mojo.getBasedir().getPath());
		
		List<Dependency> dependencies = mojo.getDependencies();
		for (Dependency dependency : dependencies) {
			if (dependency.getType().toLowerCase().equals("maven")) {
				String srcpath = mojo.getLocalRepository().getBasedir();
				String[] segments = dependency.getGroupId().split("\\.");
				for (String segment : segments) {
					srcpath += "/" + segment;
				}
				srcpath += "/" + dependency.getArtifactId();
				srcpath += "/" + dependency.getVersion();
				srcpath += "/" + dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
				String dstPath = mojo.getScfhome()+"/service/deploy/"+mojo.getName()+"/"+dependency.getTarget()+"/"+dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
				File dstFile = new File(dstPath);
				File srcFile = new  File(srcpath);
				if(srcFile.lastModified()>dstFile.lastModified()){
					FileUtil.copyFile(srcFile.getPath(), dstFile.getPath(),true);
					loger.info("部署:"+dstFile.getPath());
				}
			} else if (dependency.getType().toLowerCase().equals("file")) {
				String srcpath = dependency.getFile();
				srcpath=srcpath.replace("{resource}",mojo.getBasedir().getPath()+"/src/main/resources");
				srcpath=srcpath.replace("{root}", mojo.getBasedir().getPath());
				File srcFile = new  File(srcpath);
				String dstPath = mojo.getScfhome()+"/service/deploy/"+mojo.getName()+"/"+dependency.getTarget()+"/"+srcFile.getName();
				File dstFile = new File(dstPath);
				if(srcFile.lastModified()>dstFile.lastModified()){
					FileUtil.copyFile(srcFile.getPath(), dstFile.getPath(),true);
					loger.info("部署:"+dstFile.getPath());
				}
			}
		}
		loger.info("===================部署完成===================");
	}
}
