package org.spat.utility.tools;

public class ThreadRenameRunnable implements Runnable {
	
    private final Runnable runnable;
    private final String proposedThreadName;

    public ThreadRenameRunnable(Runnable runnable, String proposedThreadName) {
        if (runnable == null) {
            throw new NullPointerException("runnable");
        }
        if (proposedThreadName == null) {
            throw new NullPointerException("proposedThreadName");
        }
        this.runnable = runnable;
        this.proposedThreadName = proposedThreadName;
    }

    public void run() {
        final Thread currentThread = Thread.currentThread();
        final String oldThreadName = currentThread.getName();
        final String newThreadName = proposedThreadName;

        // 修改线程名
        boolean renamed = false;
        if (!oldThreadName.equals(newThreadName)) {
            try {
                currentThread.setName(newThreadName);
                renamed = true;
            } catch (SecurityException e) {
            	e.printStackTrace();
            }
        }

        try {
            runnable.run();
        } finally {
            if (renamed) {
                // 执行完毕后将线程名重置回去
                currentThread.setName(oldThreadName);
            }
        }
    }
}