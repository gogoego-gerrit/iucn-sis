package com.solertium.gogoego.server;

import java.util.Properties;

import org.gogoego.api.mail.InstanceMailer;

import com.solertium.mail.Mailer;

/**
 * This class monitors for certain (configurable) bad conditions and triggers a
 * restart of the local GoGoEgo node.  Although this is not very desirable, it is
 * better than simply going into a down state when a node restart would
 * temporarily resolve the problem.
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 *
 */
public class Seppuku implements Runnable {

	boolean stoppu = false;
	
	final double heapWarning;
	final double heapUrgent;
	final double heapCritical;
	final int interval;
	final String admin;
	final Bootstrap bootstrap;
	
	public Seppuku(Bootstrap bootstrap, Properties p){
		this.bootstrap = bootstrap;
		String s = p.getProperty("seppuku.heap.warning");
		if(s!=null) heapWarning = Double.valueOf(s);
		else heapWarning = 0.10;
		s = p.getProperty("seppuku.heap.urgent");
		if(s!=null) heapUrgent = Double.valueOf(s);
		else heapUrgent = 0.05;
		s = p.getProperty("seppuku.heap.critical");
		if(s!=null) heapCritical = Double.valueOf(s);
		else heapCritical = 0.02;
		admin = p.getProperty("seppuku.admin");
		s = p.getProperty("seppuku.interval");
		if(s!=null) interval = Integer.valueOf(s);
		else interval = 10000;
	}
	
	public void run() {
		while(!stoppu){
			long total = Runtime.getRuntime().totalMemory();
			long remaining = total - Runtime.getRuntime().freeMemory();
			double d = (double)remaining/(double)total;
			if(d<heapCritical){
				System.err.println("HEAP CRITICAL:" + remaining + "b ("+(d*100)+"%) remaining");
				System.err.println("Forcing a restart.");
				if(admin!=null){
					Mailer mailer = InstanceMailer.getInstance().getMailer();
					mailer.setSubject("Committed seppuku");
					mailer.setTo(admin);
					mailer.setBody("A restart will be forced on this node due to critical heap:\n  "+ remaining + "b ("+(d*100)+"%) remaining");
					try{
						mailer.send();
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
				bootstrap.restart();
			} else if(d<heapUrgent){
				System.err.println("URGENT WARNING: very low heap:" + remaining + "b ("+(d*100)+"%) remaining");
				System.err.println("Forcing a gc");
				System.gc();
			} else if(d<heapWarning){
				System.err.println("Warning: low heap:" + remaining +"b ("+(d*100)+"%) remaining");
			}
			try{
				Thread.sleep(interval);
			} catch (InterruptedException ix) {
				ix.printStackTrace();
			}
		}
	}
	
	public void stop() {
		stoppu = true;
	}

}
