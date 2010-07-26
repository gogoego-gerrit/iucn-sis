/*
 * Copyright (C) 2009 Solertium Corporation
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

package org.gogoego.util.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class wraps a DOM NodeList with List semantics including
 * collection generics, to avoid the ugly casts and array iterations
 * associated with NodeList.  The ElementCollection is appropriate
 * when it is known that the NodeList contains only Element objects,
 * for example, as the result of a getElementsByTagName() call.<p>
 * 
 * The class does not check the type of the nodes in the NodeList
 * at construction time; a ClassCastException will occur at runtime
 * if the contained nodes are not all Elements.<p>
 * 
 * To avoid this in common use cases, use the provided statics for
 * fetching childElements or childElementsByTagName of a particular
 * node.  This also provides a workaround for the deep-traversal
 * tag name matching behavior of the DOM API when this is not
 * desirable.
 * 
 * Usage:<p>
 * 
 * <code>
 * ElementCollection elementCollection =<br>
 *   new ElementCollection(someElement.getElementsByTagName("div"));<br>
 * for(Element element : elementCollection){<br>
 *   // operate on element<br>
 * }<br>
 * </code>
 *  
 * @author <a href="mailto:rob.heittman@solertium.com">Rob Heittman</a>, <a
 *         href="http://www.solertium.com">Solertium Corporation</a>
 */
public class ElementCollection implements List<Element> {

	private NodeList wrappedNodeList;

	public static ElementCollection childElements(Node parent){
		ArrayNodeList scratch = new ArrayNodeList();
		for(Node n : new NodeCollection(parent.getChildNodes())){
			if(n.getNodeType() != Node.ELEMENT_NODE) continue;
			scratch.add((Element)n);
		}
		return new ElementCollection(scratch);
	}

	public static ElementCollection childElementsByTagName(Node parent, String tagName){
		ArrayNodeList scratch = new ArrayNodeList();
		for(Node n : new NodeCollection(parent.getChildNodes())){
			if(n.getNodeType() != Node.ELEMENT_NODE) continue;
			if(!n.getNodeName().equals(tagName)) continue;
			scratch.add((Element)n);
		}
		return new ElementCollection(scratch);
	}
	
	public ElementCollection(final NodeList wrappedNodeList){
		this.wrappedNodeList = wrappedNodeList;
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean add(Element arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public void add(int arg0, Element arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(Collection<? extends Element> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean addAll(int arg0, Collection<? extends Element> arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(Object arg0) {
		for(int i=0;i<wrappedNodeList.getLength();i++){
			if(wrappedNodeList.item(i).equals(arg0)) return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsAll(Collection<?> arg0) {
		for(Object o : arg0){
			if(!contains(o)) return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Element get(int arg0) {
		return (Element) wrappedNodeList.item(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public int indexOf(Object arg0) {
		for(int i=0;i<wrappedNodeList.getLength();i++){
			if(wrappedNodeList.item(i).equals(arg0)) return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		if(wrappedNodeList.getLength()<1) return true;
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<Element> iterator() {
		return new Iterator<Element>(){
			
			private int pos = 0;

			
			public boolean hasNext() {
				if(pos>=wrappedNodeList.getLength()) return false;
				return true;
			}

			
			public Element next() {
				return (Element) wrappedNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public int lastIndexOf(Object arg0) {
		for(int i=wrappedNodeList.getLength();i>=0;i--){
			if(wrappedNodeList.item(i).equals(arg0)) return i;
		}
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<Element> listIterator() {
		return new ListIterator<Element>(){
			
			private int pos = 0;

			
			public boolean hasNext() {
				if(pos>=wrappedNodeList.getLength()) return false;
				return true;
			}

			
			public Element next() {
				return (Element) wrappedNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void add(Element arg0) {
				throw new UnsupportedOperationException();
			}

			
			public boolean hasPrevious() {
				if(pos<=0) return false;
				return true;
			}

			
			public int nextIndex() {
				if(pos>=wrappedNodeList.getLength()) throw new IndexOutOfBoundsException();
				return pos++;
			}

			
			public Element previous() {
				return (Element) wrappedNodeList.item(pos--);
			}

			
			public int previousIndex() {
				if(pos==0) throw new IndexOutOfBoundsException();
				return pos--;
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void set(Element arg0) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public ListIterator<Element> listIterator(final int arg0) {
		return new ListIterator<Element>(){
			
			private int pos = arg0;

			
			public boolean hasNext() {
				if(pos>=wrappedNodeList.getLength()) return false;
				return true;
			}

			
			public Element next() {
				return (Element) wrappedNodeList.item(pos++);
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Modification of the collection is not supported.
			 * @throws UnsupportedOperationException
			 */
			
			public void add(Element arg0) {
				throw new UnsupportedOperationException();
			}

			
			public boolean hasPrevious() {
				if(pos<=0) return false;
				return true;
			}

			
			public int nextIndex() {
				if(pos>=wrappedNodeList.getLength()) throw new IndexOutOfBoundsException();
				return pos++;
			}

			
			public Element previous() {
				return (Element) wrappedNodeList.item(pos--);
			}

			
			public int previousIndex() {
				if(pos==0) throw new IndexOutOfBoundsException();
				return pos--;
			}

			
			public void set(Element arg0) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public Element remove(int arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Modification of the collection is not supported.
	 * @throws UnsupportedOperationException
	 */
	public Element set(int arg0, Element arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return wrappedNodeList.getLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Element> subList(int arg0, int arg1) {
		ArrayList<Element> al = new ArrayList<Element>();
		for(int i=arg0;i<arg1;i++)
			al.add((Element) wrappedNodeList.item(i));
		return al;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] toArray() {
		Element[] nodes = new Element[wrappedNodeList.getLength()];
		for(int i=0;i<wrappedNodeList.getLength();i++){
			nodes[i] = (Element) wrappedNodeList.item(i);
		}
		return nodes;
	}

	/**
	 * Casting to arbitrary types is not supported.
	 * @throws UnsupportedOperationException
	 */
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}
	
}
