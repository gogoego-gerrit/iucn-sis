/*
 * FormFiller.java - Fill in an HTML form with form values
 *
 * Copyright (C) 2004 Cluestream Ventures, LLC
 * Copyright (C) 2005-2009 Solertium Corporation
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 *
 * You may download a copy of the GNU General Public License
 * electronically from http://www.cluestream.com/licenses/gpl.html.
 *
 * If the GNU General Public License is not suitable for your
 * purposes (if, for example, you are producing non-free software),
 * please contact the copyright holder to discuss alternative licensing.
 * Please direct your inquiries to: sales@cluestream.com.
 */

package com.solertium.util.restlet;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.restlet.data.Form;

import com.solertium.util.BaseTagListener;
import com.solertium.util.DateHelper;
import com.solertium.util.TagFilter;

/**
 * 
 *
 * @author Rob Heittman (rob@cluestream.com)
 * @version {$ID }
 */
public class FormFiller extends BaseTagListener {

    public boolean writeSpans = true;
    public boolean elideScript = true;
    
    private Map<String,String> map;
    public FormFiller(Map<String,String> map){
        this.map = map;
    }
    
    public static String process(Form f, String template) throws Exception {
        f.add("AUTO-DATE", new SimpleDateFormat(DateHelper.longDateFormat).format(new Date()));
        FormFiller ff = new FormFiller(f.getValuesMap());
        StringWriter strw = new StringWriter(template.length()+1024);
        ff.serialize(new StringReader(template), strw);
        return strw.toString();
    }
    
    private String spanWrap(String s){
        if(s==null) s="";
        if(writeSpans==true){
            return "<span class=\"formanswer\">"+s+"</span>";
        } else {
            return s;
        }
    }
    
    List<String> l = new ArrayList<String>();

    public void serialize(Reader r,Writer w) throws Exception {
        TagFilter tf = new TagFilter(r,w);
        serialize(tf);
    }
    public void serialize(TagFilter tf) throws Exception {
        tf.shortCircuitClosingTags = false;
        l = new ArrayList<String>();
        l.add("form");
        l.add("/form");
        l.add("input");
        l.add("textarea");
        l.add("/textarea");
        l.add("select");
        l.add("/select");
        l.add("option");
        l.add("/option");
        l.add("script");
        l.add("/script");
        tf.registerListener(this);
        tf.parse();
    }

    public List<String> interestingTagNames(){
        return l;
    }

    public void process(TagFilter.Tag t){
        String tn = t.name;
        if(tn==null) return;
        tn = tn.toLowerCase();
        if(tn.equals("/form")){
            t.newTagText="";
        } else if (tn.equals("form")) {
            t.newTagText="";
        } else if (tn.equals("script")) {
            if(elideScript) {
                try{
              	  parent.stopWritingBeforeTag();
                } catch (IOException x) {
              	  throw new RuntimeException(x);
                }
            }
        } else if (tn.equals("/script")) {
          if(elideScript) {
              try{
            	  parent.stopWritingBeforeTag();
              } catch (IOException x) {
            	  throw new RuntimeException(x);
              }
          }
        } else if (tn.equals("/textarea")) {
          t.newTagText="";
        } else if (tn.equals("/input")) {
          t.newTagText="";
        } else if (tn.equals("select")) {
          try{
	          parent.write(spanWrap(map.get(t.getAttribute("name"))));
	          parent.stopWritingBeforeTag();
          } catch (IOException x) {
        	  throw new RuntimeException(x);
          }
        } else if (tn.equals("/select")) {
          parent.startWritingAfterTag();
        } else if (tn.equals("textarea") ||
                   tn.equals("input")) {
            if ("checkbox".equals(t.getAttribute("type"))){
              String value = t.getAttribute("value");
              if(value==null) value="on";
              StringBuffer buff = new StringBuffer(128);
              buff.append("[");
              String compare = ""+map.get(t.getAttribute("name"));
              if(value.equals(compare)){
                buff.append("X");
              } else {
                buff.append(" ");
              }
              buff.append("]");
              t.newTagText=spanWrap(buff.toString());
            } else if ("radio".equals(t.getAttribute("type"))){
              String value = t.getAttribute("value");
              if(value==null) value="on";
              StringBuffer buff = new StringBuffer(128);
              buff.append("(");
              String compare = ""+map.get(t.getAttribute("name"));
              if(value.equals(compare)){
                buff.append("X");
              } else {
                buff.append(" ");
              }
              buff.append(")");
              t.newTagText=spanWrap(buff.toString());
            } else if ("submit".equals(t.getAttribute("type"))){
              t.newTagText="";
            } else if ("button".equals(t.getAttribute("type"))){
              t.newTagText="";
            } else if ("hidden".equals(t.getAttribute("type"))){
              t.newTagText="";
            } else {
              t.newTagText=spanWrap(map.get(t.getAttribute("name")));
            }
        }
        return;
    }
    
}