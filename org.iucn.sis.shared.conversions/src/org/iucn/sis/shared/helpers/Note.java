package org.iucn.sis.shared.helpers;

import java.util.ArrayList;

import com.solertium.lwxml.shared.NativeElement;
import com.solertium.lwxml.shared.NativeNodeList;

public class Note {
	public static ArrayList<Note> notesFromXML(NativeElement XMLBody) {
		ArrayList<Note> list = new ArrayList<Note>();
		NativeNodeList noteList = XMLBody.getElementsByTagName("note");
		for (int i = 0; i < noteList.getLength(); i++) {
			Note current = new Note();
			current.setId(Long.valueOf(((NativeElement) noteList.item(i)).getAttribute("id")).longValue());
			current.setDate(((NativeElement) noteList.item(i)).getAttribute("date"));
			current.setUser(((NativeElement) noteList.item(i)).getAttribute("user"));
			current.setCanonicalName(((NativeElement) noteList.item(i)).getAttribute("canonicalName"));
			current.setBody(((NativeElement) noteList.item(i)).getText());
			list.add(current);

		}

		return list;

	}

	private String noteBody = "";
	private String canonicalName = "";
	private String user = "";
	private String date = "";

	private long id;

	public Note() {
		try {
//			user = SimpleSISClient.currentUser.username;
//			date = FormattedDate.impl.getDate();
//
//			id = Random.nextInt();
		} catch (Exception ignored) {
			// Probably not running on client
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Note) {
			Note note = (Note) obj;

			if (noteBody == note.noteBody && canonicalName == note.canonicalName && user == note.user && id == note.id)
				return true;
		}

		return false;
	}

	public String getBody() {
		return noteBody;
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	public String getDate() {
		return date;
	}

	public String getUser() {
		return user;
	}

	public void setBody(String body) {
		noteBody = body;
	}

	public void setCanonicalName(String name) {
		canonicalName = name;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String toXML() {
		String ret = "<note id=\"" + id + "\" user=\"" + user + "\" date=\"" + date + "\" canonicalName= \""
				+ canonicalName + "\">";
		ret += XMLUtils.clean(noteBody);
		ret += "</note>\r\n";
		// SysDebugger.getInstance().println(ret);
		return ret;
	}

}
