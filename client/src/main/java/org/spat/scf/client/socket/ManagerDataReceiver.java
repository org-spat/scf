
package org.spat.scf.client.socket;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.spat.scf.client.logger.ILog;
import org.spat.scf.client.logger.LogFactory;

/**
 * DataReceiver
 * @author Service Platform Architecture Team 
 */
class ManagerDataReceiver {

    private static ManagerDataReceiver _ManagerDataReceiver = null;
    private final static Object LockHelper = new Object(); 
    private final ReentrantLock lock = new ReentrantLock();
    private ManagerWorker managerWorker = null;
    
    private ManagerDataReceiver() throws IOException {
    	managerWorker = new ManagerWorker();
        Thread thread = new Thread(managerWorker);
        thread.setName("DataReceiver-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public static ManagerDataReceiver instance() throws ClosedChannelException, IOException {
        if (_ManagerDataReceiver == null) {
            synchronized (LockHelper) {
                if (_ManagerDataReceiver == null) {
                	_ManagerDataReceiver = new ManagerDataReceiver();
                }
            }
        }
        return _ManagerDataReceiver;
    }

    public void RegSocketChannel(final ManagerSocket socket) throws ClosedChannelException, IOException {
    	lock.lock();
		try{
			managerWorker.register(socket);
		}finally{
			lock.unlock();
		}
    }

    public void UnRegSocketChannel(CSocket socket) {
    	
    }
    
    public static void closeRecv() {
    	Worker.setControl(false);
    }
}

class ManagerWorker implements Runnable {

    private static ILog logger = LogFactory.getLogger(ManagerWorker.class);
    private List<ManagerSocket> sockets = new ArrayList<ManagerSocket>();
    private static boolean control = true;
    private Selector selector;
    private final Object locker = new Object();

    public ManagerWorker() throws IOException {
        selector = Selector.open();
    }
    
    public void register(ManagerSocket socket) throws IOException {
		if(socket.is_connecting()) {
			synchronized (locker) {
				sockets.add(socket);
			}
			selector.wakeup();
		} else {
			throw new IOException("channel is not open when register selector");
		}
	}
    
    @Override
    public void run() {
    	while (isControl()) {
    		ManagerSocket nioChannel = null;
			try {
				selector.select();
				if(sockets.size() > 0) {
					synchronized (locker) {
						for(ManagerSocket channel : sockets) {
							channel.getChannel().register(selector, SelectionKey.OP_READ, channel);
						}
						sockets.clear();
					}
				}
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				while (it.hasNext()) {
					SelectionKey key = (SelectionKey) it.next();
					if(key.isValid()){
						if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
							nioChannel = (ManagerSocket) key.attachment();
							nioChannel.frameHandle();
						}
					}
				}
				selectedKeys.clear();
			} catch (IOException e) {
				if(nioChannel != null) {
					nioChannel.close();
				}
				logger.error("receive data error", e);
			} catch(NotYetConnectedException e) {
				if(nioChannel != null) {
					nioChannel.close();
				}
				logger.error("receive data error", e);
			} catch (InterruptedException e) {
				logger.error("receive data error", e);
			} catch (Throwable t) {
				logger.error("receive data error", t);
			}
    	}
    }

	protected static boolean isControl() {
		return control;
	}

	protected static void setControl(boolean control) {
		ManagerWorker.control = control;
	}
   
}