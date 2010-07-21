package org.iucn.sis.server.utils.logging;

import java.io.IOException;

import javax.naming.NamingException;

import org.iucn.sis.server.api.utils.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.solertium.db.CInteger;
import com.solertium.db.CString;
import com.solertium.db.DBException;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.InsertQuery;

public class DBWorkingSetBuffer extends EventBuffer {

	ExecutionContext ec;

	public DBWorkingSetBuffer() throws DBException {
		try {
			Document structDoc = DocumentUtils.newDocumentBuilder().parse(
					EventLogger.class.getResourceAsStream("workingsetLog.xml"));
			ec = new SystemExecutionContext("default");
			ec.setExecutionLevel(ExecutionContext.ADMIN);
			ec.createStructure(structDoc);
		} catch (IOException iox) {
			throw new DBException("Database could not be initialized", iox);
		} catch (NamingException nx) {
			throw new DBException("Database could not be initialized", nx);
		} catch (SAXException sax) {
			throw new RuntimeException("Database structure could not be parsed; is your build corrupt?", sax);
		}
	}

	@Override
	public void flush() {
		for (int i = 0; i < getBufferSize(); i++) {
			Document e = events.get(i);
			Element event = (Element) e.getElementsByTagName("workingSet").item(0);
			InsertQuery isql = new InsertQuery();
			Row row = new Row();
			row.add(new CInteger("ID", Integer.valueOf(event.getAttribute("id"))));
			row.add(new CString("CREATOR", event.getAttribute("creator")));
			row.add(new CString("DATE", event.getAttribute("date")));
			row.add(new CString("NAME", event.getAttribute("name")));

			isql.setRow(row);
			isql.setTable("workingSetLog");
			try {
				ec.doUpdate(isql);
			} catch (DBException dbException) {

			}
		}
		events.clear();

	}

}
