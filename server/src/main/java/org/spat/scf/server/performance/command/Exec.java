package org.spat.scf.server.performance.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.spat.scf.server.contract.context.SCFContext;
import org.spat.scf.server.performance.Command;
import org.spat.scf.server.performance.CommandType;

public class Exec extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		if (commandStr != null && !commandStr.equalsIgnoreCase("")) {
			String[] args = commandStr.split("\\|");
			if (args[0].trim().equalsIgnoreCase("exec")) {
				String execStr = commandStr.replaceFirst("exec\\|", "");
				if (execStr.startsWith("netstat") || execStr.startsWith("top")) {
					Command entity = new Command();
					entity.setCommandType(CommandType.Exec);
					if(execStr.equalsIgnoreCase("top")) {
						entity.setCommand("top -bn 1");
					} else {
						entity.setCommand(execStr);
					}
					return entity;
				}
			}
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Exec) {

			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			String execStr = null;

			try {
				String osName = System.getProperty("os.name");
				execStr = command.getCommand();
				if (osName.toLowerCase().startsWith("windows") && command.getCommand().equalsIgnoreCase("top")) {
					execStr = System.getenv("windir")
							+ "\\system32\\wbem\\wmic.exe process get Caption,"
							+ "KernelModeTime,UserModeTime,ThreadCount";
					// Caption,KernelModeTime,UserModeTime,ThreadCount,ReadOperationCount,WriteOperationCount
				}

				logger.info("exec command:" + execStr);

				proc = rt.exec(execStr);

				StringBuilder sbMsg = new StringBuilder();
				StreamHelper errorStream = new StreamHelper(proc.getErrorStream(), sbMsg);           
				StreamHelper outputStream = new StreamHelper(proc.getInputStream(), sbMsg);
				errorStream.start();
				outputStream.start();

				Thread.sleep(2000);
				proc.waitFor();

				byte[] responseByte = sbMsg.toString().getBytes("utf-8");
				event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			} catch (Exception ex) {
				logger.error("exec command error", ex);
			} finally {
				proc.destroy();
			}

			/*
			 * logger.info("exec commad"); String msg = "not allow"; byte[]
			 * responseByte = msg.getBytes("utf-8");
			 * event.getChannel().write(ChannelBuffers
			 * .copiedBuffer(responseByte));
			 */
		}
	}

	@Override
	public void messageReceived(SCFContext context) {
		// do nothing
	}

	@Override
	public void removeChannel(Command command, Channel channel) {
		// do nothing
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

	private class StreamHelper extends Thread {
		
		private InputStream inStream;
		private StringBuilder sbMsg;

		StreamHelper(InputStream inStream, StringBuilder sbMsg) {
			this.inStream = inStream;
			this.sbMsg = sbMsg;
		}

		public void run() {
			InputStreamReader streamReader = null;
			BufferedReader bufferReader = null;
			try {
				streamReader = new InputStreamReader(inStream);
				bufferReader = new BufferedReader(streamReader);
				String line = null;
				while ((line = bufferReader.readLine()) != null) {
					sbMsg.append(line);
					sbMsg.append("\r\n");
				}	
			} catch (IOException ex) {
				logger.error("read stream from exec error", ex);
			} finally {
				if(bufferReader != null) {
					try {
						bufferReader.close();
					} catch(Exception ex) {
						logger.error("close BufferedReader error when exec command", ex);
					}
				}
				
				if(streamReader != null) {
					try {
						streamReader.close();
					} catch(Exception ex) {
						logger.error("close InputStreamReader error when exec command", ex);
					}
				}
				
				if(inStream != null) {
					try {
						inStream.close();
					} catch(Exception ex) {
						logger.error("close InputStream error when exec command", ex);
					}
				}
			}
		}
	}
}