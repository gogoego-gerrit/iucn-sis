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
package org.gogoego.api.collections;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Tag;

/**
 * CollectionCacheContent.java
 * 
 * Content that is stashed away for collections.
 * 
 * @author <a href="mailto:carl.scott@solertium.com">Carl Scott</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 *
 */
public class CollectionCacheContent {
	
	private final CategoryData categoryData;
	private final GenericItem item;
	
	private long size;
	private Date timeCached;
	private Date modificationDate;
	private Tag tag;
	private MediaType mediaType;
	
	private Map<String, Object> responseAttributes;
	
	public CollectionCacheContent(CategoryData categoryData) {
		this(categoryData, null);
	}
	
	public CollectionCacheContent(CategoryData categoryData, GenericItem item) {
		this.categoryData = categoryData;
		this.item = item;
		this.timeCached = Calendar.getInstance().getTime();
	}
	
	public CategoryData getCategoryData() {
		return categoryData;
	}
	
	public GenericItem getItem() {
		return item;
	}
	
	public MediaType getMediaType() {
		return mediaType;
	}
	
	public Date getModificationDate() {
		return modificationDate;
	}
	
	public Map<String, Object> getResponseAttributes() {
		return responseAttributes;
	}
	
	public long getSize() {
		return size;
	}
	
	public Tag getTag() {
		return tag;
	}
	
	public Date getTimeCached() {
		return new Date(timeCached.getTime());
	}
	
	public boolean hasItem() {
		return item != null;
	}
	
	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}
	
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	
	public void setResponseAttributes(Map<String, Object> responseAttributes) {
		this.responseAttributes = responseAttributes;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
}
