package org.spat.scf.server.deploy.filemonitor;

import java.io.File;

/**
 * A class for describe a file
 * 
 * @author Service Platform Architecture Team 
 * 
 * 
 */
public class FileInfo {
	
	private long lastModifyTime;
	
	private long fileSize;
	
	private String filePath;
	
	private String fileName;
	
	private boolean exists;
	
	
	
	public FileInfo(){
		
	}
	
	public FileInfo(File f) throws Exception{
		if(f != null) {
			this.setFileSize(f.length());
			this.setLastModifyTime(f.lastModified());
			this.setFilePath(f.getCanonicalPath());
			this.setFileName(f.getName());
		} else {
			throw new Exception("File is null");
		}
	}
	

	public void setLastModifyTime(long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

	public boolean isExists() {
		return exists;
	}
}