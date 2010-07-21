package org.iucn.sis.server.crossport.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Writer;

import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSFactory;

public class MasterListUtils {
	private static VFS vfs = null;

	public static void execute() {
		main(null);
	}

	public static void main(String[] args) {
		try {
			vfs = VFSFactory.getVFS(new File("/var/sis/peru/sis/vfs"));

			File list = new File(MasterListUtils.class.getResource("masterList.xml").getPath());
			BufferedReader reader = new BufferedReader(new FileReader(list));

			StringBuffer curField = new StringBuffer();
			String curLine;
			while ((curLine = reader.readLine()) != null) {
				if (curLine.contains("</field>") || curLine.contains("</tree>")) {
					processField(curField.append(curLine).toString());
					curField.delete(0, curField.length());
					// count++;
					// if( count == 20 )
					// throw new Error("Done.");
				} else
					curField.append(curLine + "\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processField(String field) throws Exception {
		char suffix = 'a';

		String[] bits = field.split("\r\n");
		String canonicalName = "";
		String out = "";

		for (int i = 0; i < bits.length; i++) {
			if ((bits[i].contains("<structure>") || bits[i].contains("<structure description"))
					&& !bits[i + 1].contains("<relatedStructure>") && !bits[i + 1].contains("<treeStructure>")) {
				bits[i] = bits[i].replace("<structure", "<structure id=\"" + suffix + "\"");
				suffix++;
			} else if (bits[i].contains("<canonicalName>")) {
				try {
					canonicalName = bits[i].substring(bits[i].indexOf(">") + 1, bits[i].indexOf("</"));
				} catch (Exception e) {
					canonicalName = bits[i + 1].replaceAll("\\s", "");
				}
			}
			if (!bits[i].contains("<location>"))
				out += bits[i] + "\r\n";
		}

		Writer writer = vfs.getWriter("/browse/docs/fields/" + canonicalName + ".xml");
		writer.write(out);
		writer.close();
	}
}
