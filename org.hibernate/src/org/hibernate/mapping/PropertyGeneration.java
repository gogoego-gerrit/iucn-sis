/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 */
package org.hibernate.mapping;

import java.io.Serializable;

/**
 * Indicates whether given properties are generated by the database and, if
 * so, at what time(s) they are generated.
 *
 * @author Steve Ebersole
 */
public class PropertyGeneration implements Serializable {

	/**
	 * Values for this property are never generated by the database.
	 */
	public static final PropertyGeneration NEVER = new PropertyGeneration( "never" );
	/**
	 * Values for this property are generated by the database on insert.
	 */
	public static final PropertyGeneration INSERT = new PropertyGeneration( "insert" );
	/**
	 * Values for this property are generated by the database on both insert and update.
	 */
	public static final PropertyGeneration ALWAYS = new PropertyGeneration( "always" );

	private final String name;

	private PropertyGeneration(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static PropertyGeneration parse(String name) {
		if ( "insert".equalsIgnoreCase( name ) ) {
			return INSERT;
		}
		else if ( "always".equalsIgnoreCase( name ) ) {
			return ALWAYS;
		}
		else {
			return NEVER;
		}
	}

	private Object readResolve() {
		return parse( name );
	}
	
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
	}
}
