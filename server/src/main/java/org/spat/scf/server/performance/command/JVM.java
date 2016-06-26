package org.spat.scf.server.performance.command;

import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.contract.log.ILog;
import org.spat.scf.server.contract.log.LogFactory;
import org.spat.scf.server.performance.Command;
import org.spat.scf.server.performance.CommandType;
import org.spat.scf.server.performance.JVMMonitor;
import org.spat.scf.server.performance.MonitorGC;
import org.spat.scf.server.performance.MonitorMemory;


public class JVM extends CommandHelperBase{
	
	private static ILog logger = LogFactory.getLogger(JVM.class);
	@Override
	public Command createCommand(String commandStr) {
		
		if(commandStr != null && !commandStr.equalsIgnoreCase("")) {
			String[] args = commandStr.split("\\-");
			if(args[0].trim().equalsIgnoreCase("jvm")) {
				Command entity = new Command();
				entity.setCommandType(CommandType.JVM);
				entity.setSecond(0);
				entity.setGroup(0);
				if(args.length > 1) {
					for(int i=1; i<args.length; i++) {
						if(args[i].trim().startsWith("gcutil")) {
							commandStrSet(entity,"gcutil",args[i]);
						}else if(args[i].trim().startsWith("class")){
							commandStrSet(entity,"class",args[i]);
						}else if(args[i].trim().startsWith("gcheap")){
							commandStrSet(entity,"gcheap",args[i]);						
						}else if(args[i].trim().startsWith("help")){
							entity.setCommand("help");
						}else if(args[i].trim().startsWith("thread")){
							commandStrSet(entity,"thread",args[i]);
						}else if(args[i].trim().startsWith("memory")){
							commandStrSet(entity,"memory",args[i]);
						}else if(args[i].trim().startsWith("heap")){
							commandStrSet(entity,"heap",args[i]);
						}else if(args[i].trim().startsWith("noheap")){
							commandStrSet(entity,"noheap",args[i]);
						}
					}
				}
				return entity;
			}
		}
		return null;
	}
	/**
	 * 根据命令输入，取出间隔时间，循环次数
	 * 
	 * @author fanwb
	 * @date:2012.8.6
	 * */
	public void commandStrSet(Command entity,String sCom,String sMsg){
		entity.setCommand(sCom);
		String[] tcAry=sMsg.trim().split("\\s+");
		if(tcAry.length>1){
			try{
				entity.setSecond(Integer.parseInt(tcAry[1].trim()));
			}catch(Exception e) {
				logger.error("jvm input second is error");
			}
			if(tcAry.length>2){
				try {
					entity.setGroup(Integer.parseInt(tcAry[2].trim()));
				}catch(Exception e) {
					logger.error("jvm input times is error");
				}

			}
		}
	}
	
	@Override
	public void execCommand(final Command command, final MessageEvent event)
			throws Exception {
		if(command.getCommandType()==CommandType.JVM){
			Clear.setStop(true);
			Thread thread = new Thread(){
				public void run(){
					try {
						if(command.getCommand()=="gcutil"){					
							jvmGcutil(command,event);
						}else if(command.getCommand()=="class"){
							jvmClass(command,event);
						}else if(command.getCommand()=="gcheap"){
							jvmGc(command,event);
						}else if(command.getCommand()=="help"){
							jvmHelp(event);
						}else if(command.getCommand()=="thread"){
							jvmThread(command,event);
						}else if(command.getCommand()=="memory"){
							jvmMemory(command,event);
						}else if(command.getCommand()=="heap"){
							jvmHeapMemory(command,event);
						}else if(command.getCommand()=="noheap"){
							jvmNoHeapMemory(command,event);
						}		
						
					} catch (Exception ex) {
						logger.error("jvm command error", ex);
					}
					
				}
			};						
			thread.start();
		}
	}
	@Override
	public void messageReceived(SCFContext context) {
		
	}
	@Override
	public void removeChannel(Command command, Channel channel) {

	}
	@Override
	public int getChannelCount() {
		return 0;
	}
	
	/**
	 * jvm 中实现 -gcutil 操作
	 * 
	 * @author fanwb
	 * @date 2012.8.1
	 */
	public void jvmGcutil(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times=command.getGroup();
		
		StringBuffer sb = new StringBuffer();
		sb.append("S\t");
		sb.append("E\t");
		sb.append("O\t");
		sb.append("P\t");
		sb.append("C\t");
		sb.append("YGC\t");
		sb.append("YGCT\t");
		sb.append("FGC\t");
		sb.append("FGCT\t");
		sb.append("GCT\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
			MonitorGC monitorGC=JVMMonitor.getGcTime();
			DecimalFormat df = new DecimalFormat( "0.00"); 
			StringBuffer strb = new StringBuffer();
			
			strb.append(df.format(monitorMemory.getSurvivor().getPercentage()));
			strb.append("\t");
			strb.append(df.format(monitorMemory.getEden().getPercentage()));
			strb.append("\t");
			strb.append(df.format(monitorMemory.getOld().getPercentage()));
			strb.append("\t");
			strb.append(df.format(monitorMemory.getPerm().getPercentage()));
			strb.append("\t");
			strb.append(df.format(monitorMemory.getCodeCache().getPercentage()));
			strb.append("\t");
			strb.append(monitorGC.getyGcCount());
			strb.append("\t");
			strb.append(df.format(monitorGC.getyGcTime()));
			strb.append("\t");
			strb.append(monitorGC.getfGcCount());
			strb.append("\t");
			strb.append(df.format(monitorGC.getfGcTime()));
			strb.append("\t");
			strb.append(df.format(monitorGC.getGcTime()));
			strb.append("\r\n");
			byte[] responseByte =  strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	/**
	 * jvm 中实现 -class 操作
	 * 
	 * @author fanwb
	 * @date 2012.8.1
	 */
	
	public void jvmClass(Command command, MessageEvent event) throws IOException{
		boolean loop=true;
		int times=command.getGroup();
		StringBuffer sb = new StringBuffer();
		sb.append("Loaded\t");
		sb.append("Unloaded\t");
		sb.append("TotalLoaded\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			StringBuffer strb = new StringBuffer();
			strb.append(JVMMonitor.getLoadedClassCount());
			strb.append("\t");
			strb.append(JVMMonitor.getUnloadedClassCount());
			strb.append("\t\t");
			strb.append(JVMMonitor.getTotalLoadedClassCount());	
			strb.append("\r\n");
			byte[] responseByte =  strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * jvm 中实现 -gcheap操作 查看heap内存区使用情况
	 * 
	 * @author fanwb
	 * @date 2012.8.3
	 */	
	public void jvmGc(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times=command.getGroup();
		
		StringBuffer sb = new StringBuffer();
		sb.append("SC\t");
		sb.append("SU\t");
		sb.append("PC\t");
		sb.append("PU\t");
		sb.append("EC\t");
		sb.append("EU\t");
		sb.append("OC\t");
		sb.append("OU\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
			DecimalFormat df = new DecimalFormat( "0.0"); 
			StringBuffer strb = new StringBuffer();
			strb.append((double)monitorMemory.getSurvivor().getCommitted()/1024.0);
			strb.append("\t");
			strb.append(df.format((double)monitorMemory.getSurvivor().getUsed()/1024.0));
			strb.append("\t");
			strb.append((double)monitorMemory.getPerm().getCommitted()/1024.0);
			strb.append("\t");
			strb.append(df.format((double)monitorMemory.getPerm().getUsed()/1024.0));
			strb.append("\t");
			strb.append((double)monitorMemory.getEden().getCommitted()/1024.0);
			strb.append("\t");
			strb.append(df.format((double)monitorMemory.getEden().getUsed()/1024.0));
			strb.append("\t");
			strb.append((double)monitorMemory.getOld().getCommitted()/1024.0);
			strb.append("\t");
			strb.append(df.format((double)monitorMemory.getOld().getUsed()/1024.0));
			strb.append("\r\n");
			
			byte[] responseByte =  strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));	
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	
	}
	/**
	 * jvm 中 -h 实现  help
	 * 
	 * @author fanwb
	 * @date 2012.8.2
	 */
	public void jvmHelp(MessageEvent event) throws IOException{
		StringBuilder sbMsg = new StringBuilder();
		sbMsg.append("*******************************************************************\r\n\n");
		sbMsg.append("jvm [option] [time] [count]\r\n");
		sbMsg.append("\t*[option]:\r\n");
		sbMsg.append("\t\t* -gcutil: detection heap memory usage\r\n");
		sbMsg.append("\t\t* -class : load class\r\n");
		sbMsg.append("\t\t* -gcheap: heap memory used and committed \r\n");
		sbMsg.append("\t\t* -memory:JVM memory used \r\n");
		sbMsg.append("\t\t* -heap  :Virtual Machine heap memory used \r\n");
		sbMsg.append("\t\t* -noheap:Virtual Machine noheap memory used \r\n");
		sbMsg.append("\t\t* -thread: thread counts \r\n");
		sbMsg.append("\t\t* -help  : help\r\n");
		sbMsg.append("\t* time	 : [time] milliseconds apart test again\r\n");
		sbMsg.append("\t* count	 : detection [count] times\r\n");
		sbMsg.append("\t* example: jvm -gcutil\r\n");
		sbMsg.append("\t* example: jvm -gcutil 1000\r\n");
		sbMsg.append("\t* example: jvm -gcutil 1000 5\r\n");
		sbMsg.append("\t* example: jvm -class\r\n");
		sbMsg.append("\t* example: jvm -class 1000\r\n");
		sbMsg.append("\t* example: jvm -class 1000 5\r\n");
		sbMsg.append("\t* example: jvm -gcheap\r\n");
		sbMsg.append("\t* example: jvm -gcheap 1000\r\n");
		sbMsg.append("\t* example: jvm -gcheap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -memory\r\n");
		sbMsg.append("\t* example: jvm -memory 1000\r\n");
		sbMsg.append("\t* example: jvm -memory 1000 5\r\n");
		sbMsg.append("\t* example: jvm -heap\r\n");
		sbMsg.append("\t* example: jvm -heap 1000\r\n");
		sbMsg.append("\t* example: jvm -heap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -noheap\r\n");
		sbMsg.append("\t* example: jvm -noheap 1000\r\n");
		sbMsg.append("\t* example: jvm -noheap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -thread\r\n");
		sbMsg.append("\t* example: jvm -thread 1000\r\n");
		sbMsg.append("\t* example: jvm -thread 1000 5\r\n\n");
		sbMsg.append("*******************************************************************\r\n\n");
		byte[] responseByte = sbMsg.toString().getBytes("utf-8");
		event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
	}
	
	/**
	 * jvm 中 -thread 实现  thread统计
	 * 
	 * @author fanwb
	 * @date 2012.8.3
	 */
	public void jvmThread(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();
		sb.append("ATC\t");
		sb.append("PTC\t");
		sb.append("DTC\t");
		sb.append("TSTC\t");
		sb.append("DLC\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		
		
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			StringBuilder strb = new StringBuilder();
			strb.append(JVMMonitor.getAllThreadsCount());
			strb.append("\t");
			strb.append(JVMMonitor.getPeakThreadCount());
			strb.append("\t");
			strb.append(JVMMonitor.getDaemonThreadCount());
			strb.append("\t");
			strb.append(JVMMonitor.getTotalStartedThreadCount());
			strb.append("\t");
			strb.append(JVMMonitor.getDeadLockCount());
			strb.append("\r\n");
			
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * jvm 中 -memory 实现  memory统计
	 * 
	 * @author fanwb
	 * @date 2012.8.3
	 */
	public void jvmMemory(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();
		sb.append("TM\t\t");
		sb.append("UM\t\t");				
		sb.append("MUM\r\n");		

		event.getChannel().write(ChannelBuffers.copiedBuffer( sb.toString().getBytes("utf-8")));
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			StringBuilder strb = new StringBuilder();
			strb.append(JVMMonitor.getTotolMemory());
			strb.append("\t\t");
			strb.append(JVMMonitor.getUsedMemory());
			strb.append("\t\t");
			strb.append(JVMMonitor.getMaxUsedMemory());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * jvm 中 -heapMemory 实现  heapMemory统计
	 *
	 * @author fanwb
	 * @date 2012.8.6
	 */
	public void jvmHeapMemory(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();
		sb.append("I\t\t");
		sb.append("C\t\t");
		sb.append("M\t\t");	
		sb.append("U\r\n");		

		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			MemoryUsage memoryUsage=JVMMonitor.getJvmHeapMemory();
			StringBuilder strb = new StringBuilder();
			strb.append(memoryUsage.getInit());
			strb.append("\t\t");
			strb.append(memoryUsage.getCommitted());
			strb.append("\t\t");
			strb.append(memoryUsage.getMax());
			strb.append("\t");
			strb.append(memoryUsage.getUsed());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void jvmNoHeapMemory(Command command, MessageEvent event) throws IOException{
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();		
		sb.append("I\t\t");
		sb.append("C\t\t");
		sb.append("M\t\t");	
		sb.append("U\r\n");		

		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		while(loop&&event.getChannel().isConnected()&&Clear.isStop()){
			MemoryUsage memoryUsage=JVMMonitor.getJvmNoHeapMemory();
			StringBuilder strb = new StringBuilder();
			strb.append(memoryUsage.getInit());
			strb.append("\t");
			strb.append(memoryUsage.getCommitted());
			strb.append("\t");
			strb.append(memoryUsage.getMax());
			strb.append("\t");
			strb.append(memoryUsage.getUsed());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if((command.getSecond() == 0)||(--times == 0)){
				loop = false;
			}	
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
