package org.iucn.sis.server.api.persistance.hibernate;



/*
 * Created on 2004/9/10
 *
 * PersistentException.java
 */

/**
 * @author Kit
 */
public class PersistentException extends Exception {

    /**
     * PersistentException Constructor
     * 
     */
    public PersistentException() {
        super();
    }

    /**
     * PersistentException Constructor
     * @param aMessage
     */
    public PersistentException(String aMessage) {
        super(aMessage);
    }

    /**
     * PersistentException Constructor
     * @param aCause
     */
    public PersistentException(Throwable aCause) {
        super(aCause);
    }

    /**
     * PersistentException Constructor
     * @param aMessage
     * @param aCause
     */
    public PersistentException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

}
