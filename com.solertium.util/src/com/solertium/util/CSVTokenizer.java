/*
 * Copyright (C) 2000-2005 Cluestream Ventures, LLC
 * Copyright (C) 2006-2009 Solertium Corporation
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
 *    http://www.gnu.org/licenses
 */

package com.solertium.util;

public class CSVTokenizer {
    
    public final int READING_TOKEN=1;
    public final int READING_QUOTE=2;
    public final static char DELIM=',';
    public boolean nullOnEnd = false;
    
    String line;
    int ptr = 0;
    public CSVTokenizer(String line) {
        this.line = line;
    }
    public void setNullOnEnd(boolean nullOnEnd) {
		this.nullOnEnd = nullOnEnd;
	}
    public String nextToken(){
        if(line==null) return null;
        StringBuilder tokbuf = new StringBuilder(128);
        int mode = READING_TOKEN;
        boolean escape_next_char = false;
        for(int i=ptr;i<line.length();i++){
            ptr=i+1;
            char c = line.charAt(i);
            if(escape_next_char){
                tokbuf.append(c);
                escape_next_char=false;
                continue;
            }
            switch(c){
                case(DELIM):
                    if(mode==READING_TOKEN){
                        return tokbuf.toString();
                    } else {
                        tokbuf.append(c);
                    }
                    break;
                case('"'):
                    if(mode==READING_TOKEN){
                        mode=READING_QUOTE;
                    } else if(mode==READING_QUOTE){
                        mode=READING_TOKEN;
                    } else {
                        tokbuf.append(c);
                    }
                    break;
                case('\\'):
                    escape_next_char=true;
                    break;
                default:
                    tokbuf.append(c);
            }
        }
        
        final String out = tokbuf.toString();
        return !nullOnEnd ? out : !out.equals("") ? out : ptr < line.length() ? tokbuf.toString() : null;
    }
}
