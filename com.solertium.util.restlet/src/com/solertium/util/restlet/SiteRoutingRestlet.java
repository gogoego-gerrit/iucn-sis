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
package com.solertium.util.restlet;

import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * SiteRoutingRestlet provides an alternate attachment means for
 * virtual hosts.  Due to the sequential regex scanning used by Component's
 * virtual host attachment mechanism, hosts near the end of the list can
 * be very expensive to reach.  This becomes significant if a Restlet
 * a few hundred virtual hosts or more, but even with a handful of vhosts,
 * the sequential scan can bleed ms of performance on each request.
 * 
 * This mechanism supports DNS style lookups; "foo.bar.baz" or "*.bar.baz"
 * 
 * Regexes are NOT supported.
 * 
 * @author Rob Heittman <rob.heittman@solertium.com>
 *
 */
public class SiteRoutingRestlet extends Restlet {

    final private ConcurrentHashMap<String, Restlet> m;
    
    private ThreadLocal<String> currentMapping = new ThreadLocal<String>();

    public SiteRoutingRestlet(Context context, int size){
        super(context);
        m = new ConcurrentHashMap<String,Restlet>(size);
    }

    public SiteRoutingRestlet(Context context){
        super(context);
        m = new ConcurrentHashMap<String,Restlet>();
    }
   
    public void addMapping(String s, Restlet r) {
        m.put(s,r);
    }
   
    public void removeMapping(String s) {
        m.remove(s);
    }
   
    public void handle(Request request, Response response){
    	Reference rr = request.getResourceRef();
    	String domain;
    	if(Protocol.RIAP.equals(rr.getSchemeProtocol())){
    		domain = currentMapping.get();
    	} else {
    		domain = rr.getHostDomain(true);
    		currentMapping.set(domain);
    	}
        Restlet restlet = m.get(domain);
        if(restlet!=null){
            restlet.handle(request,response);
            return;
        }
        final int dot = domain.indexOf(".");
        if(dot>-1){
            domain = "*."+domain.substring(dot,domain.length());
        }
        restlet = m.get(domain);
        if(restlet!=null){
            restlet.handle(request,response);
        }
        response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        return;
    }
   
}
