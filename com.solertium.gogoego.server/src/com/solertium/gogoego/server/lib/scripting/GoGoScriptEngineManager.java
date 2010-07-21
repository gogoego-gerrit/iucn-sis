/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
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
 *    http://www.gnu.org/licenses
 */
package com.solertium.gogoego.server.lib.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.SimpleBindings;

/**
 * I have modified the javax.script.ScriptEngineManager to not attempt to use 
 * any service discovery.  Additionally, the manager starts completely empty 
 * by default, and also has a register function to handle what the init function 
 * used to take care of by default.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, 
 * <a href="http://www.solertium.com">Solertium Corporation</a>
 * 
 * The <code>ScriptEngineManager</code> implements a discovery and instantiation
 * mechanism for <code>ScriptEngine</code> classes and also maintains a
 * collection of key/value pairs storing state shared by all engines created
 * by the Manager. This class uses the <a href="../../../technotes/guides/jar/jar.html#Service%20Provider">service provider</a> mechanism to enumerate all the 
 * implementations of <code>ScriptEngineFactory</code>. <br><br>
 * The <code>ScriptEngineManager</code> provides a method to return an array of all these factories 
 * as well as utility methods which look up factories on the basis of language name, file extension 
 * and mime type.
 * <p>
 * The <code>Bindings</code> of key/value pairs, referred to as the "Global Scope"  maintained
 * by the manager is available to all instances of <code>ScriptEngine</code> created
 * by the <code>ScriptEngineManager</code>.  The values in the <code>Bindings</code> are
 * generally exposed in all scripts.
 *
 * @author Mike Grogan
 * @author A. Sundararajan
 * @since 1.6
 * 
 */
public class GoGoScriptEngineManager {
    private static final boolean DEBUG = false;
    
    /** Set of script engine factories discovered. */
    private Map<String, ScriptEngineFactory> engineSpis;

    /** Map of engine name to script engine factory. */
    private HashMap<String, ScriptEngineFactory> nameAssociations;

    /** Map of script file extension to script engine factory. */
    private HashMap<String, ScriptEngineFactory> extensionAssociations;

    /** Map of script script MIME type to script engine factory. */
    private HashMap<String, ScriptEngineFactory> mimeTypeAssociations;

    /** Global bindings associated with script engines created by this manager. */
    private Bindings globalScope;

    /**
     * If the thread context ClassLoader can be accessed by the caller, 
     * then the effect of calling this constructor is the same as calling 
     * <code>ScriptEngineManager(Thread.currentThread().getContextClassLoader())</code>.
     * Otherwise, the effect is the same as calling <code>ScriptEngineManager(null)</code>.
     *
     * @see java.lang.Thread#getContextClassLoader
     */
    public GoGoScriptEngineManager() {
        globalScope = new SimpleBindings();
        engineSpis = new HashMap<String, ScriptEngineFactory>();
        nameAssociations = new HashMap<String, ScriptEngineFactory>();
        extensionAssociations = new HashMap<String, ScriptEngineFactory>();
        mimeTypeAssociations = new HashMap<String, ScriptEngineFactory>();
    }

    /**
     * <code>setBindings</code> stores the specified <code>Bindings</code>
     * in the <code>globalScope</code> field. ScriptEngineManager sets this
     * <code>Bindings</code> as global bindings for <code>ScriptEngine</code>
     * objects created by it.
     *
     * @param bindings The specified <code>Bindings</code>
     * @throws IllegalArgumentException if bindings is null.
     */
    public void setBindings(Bindings bindings) {
        if (bindings == null) {
            throw new IllegalArgumentException(
                    "Global scope cannot be null.");
        }

        globalScope = bindings;
    }

    /**
     * <code>getBindings</code> returns the value of the <code>globalScope</code> field.
     * ScriptEngineManager sets this <code>Bindings</code> as global bindings for 
     * <code>ScriptEngine</code> objects created by it.
     *
     * @return The globalScope field.
     */
    public Bindings getBindings() {
        return globalScope;
    }

    /**
     * Sets the specified key/value pair in the Global Scope.
     * @param key Key to set
     * @param value Value to set.
     * @throws NullPointerException if key is null.
     * @throws IllegalArgumentException if key is empty string.
     */
    public void put(String key, Object value) {
        globalScope.put(key, value);
    }

    /**
     * Gets the value for the specified key in the Global Scope
     * @param key The key whose value is to be returned.
     * @return The value for the specified key.
     */
    public Object get(String key) {
        return globalScope.get(key);
    }
    
    /**
     * Used to override the discovery mechanism, but it acts as if 
     * it was done by the service discovery, so if later this is 
     * further overridden via register* functions, it will work as 
     * originally intended.
     * @param factory
     */
    public void register(ScriptEngineFactory factory) {
    	engineSpis.put(getRegistrationScheme(factory), factory);
    }
    
    /**
     * The use of the registration scheme allows me to remove a 
     * script engine when it is removed via OSGi.  Brute force, but 
     * works.
     * @param registrationScheme
     * @return
     */
    public ScriptEngineFactory unregister(String registrationScheme) {
    	return engineSpis.remove(registrationScheme);
    }
    
    public String getRegistrationScheme(ScriptEngineFactory factory) {
    	return factory.getClass().getName()+"\0"+factory.getEngineVersion();
    }

    /**
     * Looks up and creates a <code>ScriptEngine</code> for a given  name.
     * The algorithm first searches for a <code>ScriptEngineFactory</code> that has been
     * registered as a handler for the specified name using the <code>registerEngineName</code>
     * method.
     * <br><br> If one is not found, it searches the array of <code>ScriptEngineFactory</code> instances
     * stored by the constructor for one with the specified name.  If a <code>ScriptEngineFactory</code>
     * is found by either method, it is used to create instance of <code>ScriptEngine</code>.
     * @param shortName The short name of the <code>ScriptEngine</code> implementation.
     * returned by the <code>getNames</code> method of its <code>ScriptEngineFactory</code>.
     * @return A <code>ScriptEngine</code> created by the factory located in the search.  Returns null
     * if no such factory was found.  The <code>ScriptEngineManager</code> sets its own <code>globalScope</code>
     * <code>Bindings</code> as the <code>GLOBAL_SCOPE</code> <code>Bindings</code> of the newly
     * created <code>ScriptEngine</code>.
     * @throws NullPointerException if shortName is null.
     */
    public ScriptEngine getEngineByName(String shortName) {
        if (shortName == null)
            throw new NullPointerException();
        //look for registered name first
        Object obj;
        if (null != (obj = nameAssociations.get(shortName))) {
            ScriptEngineFactory spi = (ScriptEngineFactory) obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(),
                        ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }
        }

        for (ScriptEngineFactory spi : engineSpis.values()) {
            List<String> names = null;
            try {
                names = spi.getNames();
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }

            if (names != null) {
                for (String name : names) {
                    if (shortName.equals(name)) {
                        try {
                            ScriptEngine engine = spi.getScriptEngine();
                            engine.setBindings(getBindings(),
                                    ScriptContext.GLOBAL_SCOPE);
                            return engine;
                        } catch (Exception exp) {
                            if (DEBUG)
                                exp.printStackTrace();
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Look up and create a <code>ScriptEngine</code> for a given extension.  The algorithm
     * used by <code>getEngineByName</code> is used except that the search starts
     * by looking for a <code>ScriptEngineFactory</code> registered to handle the
     * given extension using <code>registerEngineExtension</code>.
     * @param extension The given extension
     * @return The engine to handle scripts with this extension.  Returns <code>null</code>
     * if not found.
     * @throws NullPointerException if extension is null.
     */
    public ScriptEngine getEngineByExtension(String extension) {
        if (extension == null)
            throw new NullPointerException();
        //look for registered extension first
        Object obj;
        if (null != (obj = extensionAssociations.get(extension))) {
            ScriptEngineFactory spi = (ScriptEngineFactory) obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(),
                        ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }
        }

        for (ScriptEngineFactory spi : engineSpis.values()) {
            List<String> exts = null;
            try {
                exts = spi.getExtensions();
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }
            if (exts == null)
                continue;
            for (String ext : exts) {
                if (extension.equals(ext)) {
                    try {
                        ScriptEngine engine = spi.getScriptEngine();
                        engine.setBindings(getBindings(),
                                ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception exp) {
                        if (DEBUG)
                            exp.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Look up and create a <code>ScriptEngine</code> for a given mime type.  The algorithm
     * used by <code>getEngineByName</code> is used except that the search starts
     * by looking for a <code>ScriptEngineFactory</code> registered to handle the
     * given mime type using <code>registerEngineMimeType</code>.
     * @param mimeType The given mime type
     * @return The engine to handle scripts with this mime type.  Returns <code>null</code>
     * if not found.
     * @throws NullPointerException if mimeType is null.
     */
    public ScriptEngine getEngineByMimeType(String mimeType) {
        if (mimeType == null)
            throw new NullPointerException();
        //look for registered types first
        Object obj;
        if (null != (obj = mimeTypeAssociations.get(mimeType))) {
            ScriptEngineFactory spi = (ScriptEngineFactory) obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(),
                        ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }
        }

        for (ScriptEngineFactory spi : engineSpis.values()) {
            List<String> types = null;
            try {
                types = spi.getMimeTypes();
            } catch (Exception exp) {
                if (DEBUG)
                    exp.printStackTrace();
            }
            if (types == null)
                continue;
            for (String type : types) {
                if (mimeType.equals(type)) {
                    try {
                        ScriptEngine engine = spi.getScriptEngine();
                        engine.setBindings(getBindings(),
                                ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception exp) {
                        if (DEBUG)
                            exp.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns an array whose elements are instances of all the <code>ScriptEngineFactory</code> classes
     * found by the discovery mechanism.
     * @return List of all discovered <code>ScriptEngineFactory</code>s.
     */
    public List<ScriptEngineFactory> getEngineFactories() {
        List<ScriptEngineFactory> res = new ArrayList<ScriptEngineFactory>(
                engineSpis.size());
        for (ScriptEngineFactory spi : engineSpis.values()) {
            res.add(spi);
        }
        return Collections.unmodifiableList(res);
    }

    /**
     * Registers a <code>ScriptEngineFactory</code> to handle a language
     * name.  Overrides any such association found using the Discovery mechanism.
     * @param name The name to be associated with the <code>ScriptEngineFactory</code>.
     * @param factory The class to associate with the given name.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void registerEngineName(String name,
            ScriptEngineFactory factory) {
        if (name == null || factory == null)
            throw new NullPointerException();
        nameAssociations.put(name, factory);
    }

    /**
     * Registers a <code>ScriptEngineFactory</code> to handle a mime type.
     * Overrides any such association found using the Discovery mechanism.
     *
     * @param type The mime type  to be associated with the
     * <code>ScriptEngineFactory</code>.
     *
     * @param factory The class to associate with the given mime type.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void registerEngineMimeType(String type,
            ScriptEngineFactory factory) {
        if (type == null || factory == null)
            throw new NullPointerException();
        mimeTypeAssociations.put(type, factory);
    }

    /**
     * Registers a <code>ScriptEngineFactory</code> to handle an extension.
     * Overrides any such association found using the Discovery mechanism.
     *
     * @param extension The extension type  to be associated with the
     * <code>ScriptEngineFactory</code>.
     * @param factory The class to associate with the given extension.
     * @throws NullPointerException if any of the parameters is null.
     */
    public void registerEngineExtension(String extension,
            ScriptEngineFactory factory) {
        if (extension == null || factory == null)
            throw new NullPointerException();
        extensionAssociations.put(extension, factory);
    }



}

