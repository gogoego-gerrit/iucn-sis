/*******************************************************************************
 * Copyright (C) 2007-2009 Solertium Corporation
 * 
 * This file is part of the open source GoGoEgo project.
 * 
 * Unless you have been granted a different license in writing by the
 * copyright holders for GoGoEgo, you may only modify or redistribute
 * this code under the terms of one of the following licenses:
 * 
 * 1) The Eclipse Public License, v.1.0
 *    http://www.eclipse.org/legal/epl-v10.html
 * 
 * 2) The GNU General Public License, version 2 or later
 *     http://www.gnu.org/licenses
 ******************************************************************************/
package com.solertium.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * This class examines the Subversion revisions being used to build
 * a project and emits the information into the specified file as a
 * simple HTML document.<p>
 * 
 * Syntax:<p>
 * 
 * RevisionExaminer [workspace] [targetfile] [[project],[project]...]
 * 
 * @author rob.heittman
 */
public class RevisionExaminer {
	
	public static void main(String[] args){
		String workspace = args[0];
		String target = args[1];
		File targetfile = new File(target);
		StringBuilder info = new StringBuilder(1024);
		info.append("<div>Built "+new Date()+" by "+System.getProperty("user.name")+"</div>\n");
		for(int i=2;i<args.length;i++){
			String project = args[i];
			File projectHome = new File(new File(workspace),project);
			File svnEntries = new File(projectHome,".svn/entries");
			if(svnEntries.exists()){
				try {
					BufferedReader br = new BufferedReader(new FileReader(svnEntries));
					for(int j=1;j<=10;j++) br.readLine();
					String rev = br.readLine();
					info.append("<div>" + project+" r"+rev+"</div>\n");
				} catch (IOException io) {
					info.append(project+" (unreadable revision)</div>\n");
				}
			} else {
				info.append(project+" (unknown revision)</div>\n");
			}
		}
		try {
			System.out.println(info.toString());
			FileWriter fw = new FileWriter(targetfile);
			fw.write("<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"css/styles.css\"/></head><body>\n");
			fw.write(info.toString());
			fw.write("</body></html>\n");
			fw.close();
			System.out.println("OK wrote "+targetfile.getPath());
		} catch (IOException io) {
			io.printStackTrace();
			System.out.println("ERR");
		}
	}

}
