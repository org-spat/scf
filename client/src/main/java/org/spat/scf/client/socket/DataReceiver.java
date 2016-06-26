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
class DataReceiver {

    private static DataReceiver _DataReceiver = null;
    private final static Object LockHelper = new Object(); 
    private final ReentrantLock lock = new ReentrantLock();
    private Worker worker = null;
    
    private DataReceiver() throws IOException {
        worker = new Worker();
        Thread thread = new Thread(worker);
        thread.setName("DataReceiver-thread");
        thread.setDaemon(true);
        thread.start();
    }

    public static DataReceiver instance() throws ClosedChannelException, IOException {
        if (_DataReceiver == null) {
            synchronized (LockHelper) {
                if (_DataReceiver == null) {
                    _DataReceiver = new DataReceiver();
                }
            }
        }
        return _DataReceiver;
    }

    public void RegSocketChannel(final CSocket socket) throws ClosedChannelException, IOException {
    	lock.lock();
		try{
			worker.register(socket);
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

class Worker implements Runnable {

    private static ILog logger = LogFactory.getLogger(Worker.class);
    private List<CSocket> sockets = new ArrayList<CSocket>();
    private static boolean control = true;
    private Selector selector;
    private final Object locker = new Object();

    public Worker() throws IOException {
        selector = Selector.open();
    }
    
    public void register(CSocket csocket) throws IOException {
		if(csocket.connecting()) {
			synchronized (locker) {
				sockets.add(csocket);
			}
			selector.wakeup();
		} else {
			throw new IOException("channel is not open when register selector");
		}
	}
    
    @Override
    public void run() {
    	while (isControl()) {
    		CSocket nioChannel = null;
			try {
				selector.select();
				if(sockets.size() > 0) {
					synchronized (locker) {
						for(CSocket channel : sockets) {
							try {
								channel.getChannle().register(selector, SelectionKey.OP_READ, channel);
							} catch (Exception e) {
								logger.error("register socket error", e);
							}
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
							nioChannel = (CSocket) key.attachment();
							nioChannel.frameHandle();
						}
					}
				}
				selectedKeys.clear();
			} catch (IOException e) {
				if(nioChannel != null) {
					nioChannel.closeAndDisponse();
				}
				logger.error("receive data error", e);
			} catch(NotYetConnectedException e) {
				if(nioChannel != null) {
					nioChannel.closeAndDisponse();
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
		Worker.control = control;
	}
   
}


