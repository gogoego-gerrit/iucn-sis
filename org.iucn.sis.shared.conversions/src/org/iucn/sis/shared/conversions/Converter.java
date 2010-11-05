package org.iucn.sis.shared.conversions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.BatchUpdateException;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.Session;
import org.iucn.sis.server.api.persistance.SISPersistentManager;
import org.iucn.sis.shared.api.debug.Debug;

import com.solertium.util.TrivialExceptionHandler;

public abstract class Converter {
	
	protected BufferedWriter writer;
	protected String lineBreakRule;
	
	protected Session session;
	
	public Converter() {
		setWriter(new PrintWriter(System.out));
		setLineBreakRule("\r\n");
	}
	
	public void setWriter(Writer writer) {
		this.writer = new BufferedWriter(writer);
	}
	
	public void setLineBreakRule(String lineBreakRule) {
		this.lineBreakRule = lineBreakRule;
	}
	
	public boolean start() {
		session = SISPersistentManager.instance().getSession();
		session.beginTransaction();
		
		Date start = Calendar.getInstance().getTime();
		printf("! -- Starting %s conversion at %s", getClass().getSimpleName(), start.toString());
		
		boolean success;
		try {
			run();
			success = true;
		} catch (Throwable e) {
			success = false;
			if (e.getCause() instanceof BatchUpdateException) {
				((BatchUpdateException) e.getCause()).getNextException().printStackTrace();
			} else {
				Debug.println(e);
				try {
					print("\n\n\n REALLY CAUSED BY:");
					Debug.println(e.getCause());
					print("\n\n\n REALLY REALLY CAUSED BY:");
					Debug.println(e.getCause().getCause());
				} catch (NullPointerException e1) {

				}
			}
			print(e.getMessage());
		}
		
		Date end = Calendar.getInstance().getTime();
		
		long millis = end.getTime() - start.getTime();
		millis = millis / 1000;
		
		if (success) {
			printf("! -- Finished %s conversion successfully in %s seconds at %s", getClass().getSimpleName(), millis, start.toString());
			try {
				session.getTransaction().commit();
			} catch (Exception e) {
				printf("Conversion successful, but transaction commit failed: %s", e.getMessage());
				e.printStackTrace();
				if (e.getCause() instanceof BatchUpdateException)
					try {
						((BatchUpdateException)e).getNextException().printStackTrace();
					} catch (NullPointerException f) {
						printf("Batch Update exception has no cause.");
					}
				success = false;
			}
		}
		else {
			printf("X -- Failed to finished %s conversion in %s seconds at %s", getClass().getSimpleName(), millis, start.toString());
			try {
				session.getTransaction().rollback();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return success;
	}
	
	protected void commitAndStartTransaction() {
		session.getTransaction().commit();
		
		session = SISPersistentManager.instance().getSession();
		session.beginTransaction();
	}
	
	protected abstract void run() throws Exception;
	
	protected void print(String out) {
		try {
			writer.write(out + lineBreakRule);
			writer.flush();
		} catch (IOException e) {
			System.out.println(out);
		}
	}
	
	protected void printf(String out, Object... args) {
		print(String.format(out, args));
	}

}
