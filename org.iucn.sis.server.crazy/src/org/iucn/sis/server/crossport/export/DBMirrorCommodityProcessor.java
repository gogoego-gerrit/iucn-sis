package org.iucn.sis.server.crossport.export;

import java.io.IOException;
import java.util.Queue;

import javax.naming.NamingException;

import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.SystemExecutionContext;

public abstract class DBMirrorCommodityProcessor<T> implements Runnable {

	protected ExecutionContext ec;
	protected Queue<T> commodity;
	protected Thread myThread;
	protected String threadID;
	protected boolean die = false;
	
	public DBMirrorCommodityProcessor(Queue<T> commodity, String threadID) throws IOException, NamingException, DBException {
		createAndConnectWithPostgres();
		this.commodity = commodity;
		this.threadID = threadID;
		myThread = new Thread(this);
		myThread.start();
	}
	
	public String getThreadID() {
		return threadID;
	}
	
	public Thread getMyThread() {
		return myThread;
	}
	
	public void forceDeath() {
		this.die = true;
		myThread.interrupt();
	}
	
	public boolean shouldDie() {
		return die;
	}
	
	public void setDie(boolean die) {
		this.die = die;
	}
	
	protected void createAndConnectWithPostgres() throws IOException, NamingException, DBException {
		ec = new SystemExecutionContext(DBMirrorManager.DS);
		ec.setExecutionLevel(ExecutionContext.ADMIN);
		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
	}
	
	public void run() {
		synchronized (this) {
			while( !die ) {
				try {
					T polled = commodity.poll();

					while( polled != null ) {
						process(polled);
						polled = commodity.poll();
					}
					System.out.println("Thread " + threadID + " going back to sleep.");

					wait();
				} catch (InterruptedException e) {}
			}
			
			System.out.println("Thread " + threadID + " signing off!");
		}
	}
	
	protected abstract void process(T data);
}
