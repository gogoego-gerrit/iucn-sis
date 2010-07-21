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
package org.gogoego.api.utils;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;

public abstract class BestMatchPluginBroker<T, V extends BestMatchPluginBroker.ScoreCard> extends PluginBroker<T> {
	
	private final Scorer<T, V> scorer;
	
	public BestMatchPluginBroker(BundleContext bundleContext, String trackerKey, Scorer<T, V> scorer) {
		super(bundleContext, trackerKey);
		this.scorer = scorer;
	}
	
	public T getPlugin(V properties) {
		return getPlugin(properties, null);
	}
	
	public T getPlugin(V properties, String preferred) {
		final Map<String, T> available = new HashMap<String, T>();
		int highScore = -1;
		
		String myPreference = null;
		
		final Map<String, Object> required = properties != null ? 
			properties.getRequired() : new HashMap<String, Object>();
		final Map<String, Object> optional = properties != null ? 
			properties.getOptional() : new HashMap<String, Object>();
		
		for (Map.Entry<String, T> entry : getPlugins().entrySet()) {
			int requiredScore = 0;
			for (Map.Entry<String, Object> e : required.entrySet()) {
				int curScore = scorer.score(entry.getValue(), e.getKey(), e.getValue(), properties);
				if (curScore == 0) {
					requiredScore = 0;
					break;
				}
				else
					requiredScore += curScore;
			}
			if (!required.isEmpty() && requiredScore == 0)
				continue;
			
			int optionalScore = 0;
			for (Map.Entry<String, Object> e : optional.entrySet()) 
				optionalScore += scorer.score(entry.getValue(), e.getKey(), e.getValue(), properties);
			
			final int score = requiredScore + optionalScore;
			if (score == highScore)
				available.put(entry.getKey(), entry.getValue());
			else if (score > highScore) {
				highScore = score;
				available.clear();
				myPreference = entry.getKey();
				available.put(entry.getKey(), entry.getValue());
			}
		}
		
		return available.containsKey(preferred) ? available.get(preferred) : 
			available.get(myPreference);
	}

	public static abstract class Scorer<T, V extends ScoreCard> {
		
		public abstract int score(T plugin, String key, Object value, V properties);
		
	}
	
	public static abstract class ScoreCard {
		private final Map<String, Object> required;
		private final Map<String, Object> optional;
		
		public ScoreCard(){
			required = new HashMap<String, Object>();
			optional = new HashMap<String, Object>();
		}
		
		public void setRequiredProperty(String key, Object value) {
			required.put(key, value);
		}
		
		public void setOptionalProperty(String key, Object value) {
			optional.put(key, value);
		}
		
		public Map<String, Object> getRequired() {
			return required;
		}
		
		public Map<String, Object> getOptional() {
			return optional;
		}
	}
	
}
