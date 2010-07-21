package org.iucn.sis.server.utils.logging;


public class DBTrashBuffer extends EventBuffer {

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

//	ExecutionContext ec;
//
//	public DBTrashBuffer() throws DBException {
//		try {
//			Document structDoc = DocumentUtils.newDocumentBuilder().parse(
//					EventLogger.class.getResourceAsStream("trashLog.xml"));
//			ec = new SystemExecutionContext("default");
//			ec.setExecutionLevel(ExecutionContext.ADMIN);
//			ec.createStructure(structDoc);
//		} catch (IOException iox) {
//			throw new DBException("Database could not be initialized", iox);
//		} catch (NamingException nx) {
//			throw new DBException("Database could not be initialized", nx);
//		} catch (SAXException sax) {
//			throw new RuntimeException("Database structure could not be parsed; is your build corrupt?", sax);
//		}
//	}
//
//	@Override
//	public void flush() {
//		for (int i = 0; i < getBufferSize(); i++) {
//			Document e = events.get(i);
//			if (e.getDocumentElement().getNodeName().equals("assessment")) {
//				Element event = (Element) e.getElementsByTagName("assessment").item(0);
//				InsertQuery isql = new InsertQuery();
//				Row row = new Row();
//				row.add(new CString("ID", event.getTextContent()));
//				row.add(new CString("TYPE", "ASSESSMENT"));
//				row.add(new CString("STATUS", event.getAttribute("status")));
//				row.add(new CString("USER", event.getAttribute("user")));
//				row.add(new CString("DATE", event.getAttribute("date")));
//				row.add(new CString("NODE", event.getAttribute("node")));
//				row.add(new CString("DISPLAY", event.getAttribute("display")));
//				row.add(new CString("PARENT", event.getAttribute("node")));
//
//				isql.setRow(row);
//				isql.setTable("trashLog");
//				try {
//					ec.doUpdate(isql);
//				} catch (DBException dbException) {
//
//				}
//			} else {
//				System.out.println("flusing taxon");
//				Element event = (Element) e.getElementsByTagName("taxon").item(0);
//				InsertQuery isql = new InsertQuery();
//				Row row = new Row();
//				row.add(new CString("ID", event.getTextContent()));
//				row.add(new CString("TYPE", "TAXON"));
//				row.add(new CString("USER", event.getAttribute("user")));
//				row.add(new CString("DATE", event.getAttribute("date")));
//				row.add(new CString("NODE", event.getAttribute("node")));
//				row.add(new CString("DISPLAY", event.getAttribute("display")));
//				row.add(new CString("PARENT", event.getAttribute("parent")));
//
//				isql.setRow(row);
//				isql.setTable("trashLog");
//				try {
//					ec.doUpdate(isql);
//				} catch (DBException dbException) {
//				}
//			}
//		}
//		events.clear();
//	}

}
