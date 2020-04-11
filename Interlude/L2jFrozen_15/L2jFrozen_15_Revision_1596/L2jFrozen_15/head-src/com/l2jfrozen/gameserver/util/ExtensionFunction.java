package com.l2jfrozen.gameserver.util;

/**
 * This interface can be implemented by extensions to register simple functions with the DynamicExtension handler It's in the responsibility of the extensions to interpret the get and set functions
 * @version $Revision: $ $Date: $
 * @author  Galun
 */
public interface ExtensionFunction
{
	
	/**
	 * get an object identified with a name (should have a human readable output with toString())
	 * @param  name the name of an object or a result of a function
	 * @return      the object
	 */
	public Object get(String name);
	
	/**
	 * set the named object to the new value supplied in obj
	 * @param name the name of the object
	 * @param obj  the new value
	 */
	public void set(String name, Object obj);
}
