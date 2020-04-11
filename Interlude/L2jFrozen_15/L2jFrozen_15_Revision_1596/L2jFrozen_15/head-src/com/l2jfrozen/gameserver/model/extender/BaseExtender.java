package com.l2jfrozen.gameserver.model.extender;

import com.l2jfrozen.gameserver.model.L2Object;

/**
 * @author Azagthtot BaseExtender
 */
public class BaseExtender
{
	public enum EventType
	{
		LOAD("load"), // null
		STORE("store"), // null
		CAST("cast"), // L2Skill , L2Character, L2Character[]
		ATTACK("attack"), // L2Character
		CRAFT("craft"),
		ENCHANT("enchant"),
		SPAWN("spawn"), // null
		DELETE("delete"), // null
		SETOWNER("setwoner"), // int, String
		DROP("drop"), // null
		DIE("die"), // L2Character
		REVIVE("revive"), // null
		SETINTENTION("setintention"); // CtrlIntention
		public final String name;
		
		EventType(final String name)
		{
			this.name = name;
		}
	}
	
	/**
	 * @param  object as L2Object<br>
	 * @return        as boolean<br>
	 */
	public static boolean canCreateFor(final L2Object object)
	{
		return true;
	}
	
	protected L2Object owner;
	private BaseExtender next = null;
	
	/**
	 * @param owner - L2Object
	 */
	public BaseExtender(final L2Object owner)
	{
		this.owner = owner;
	}
	
	/**
	 * @return as Object
	 */
	public L2Object getOwner()
	{
		return owner;
	}
	
	/**
	 * onEvent - super.onEvent(event,params);<BR>
	 * <BR>
	 * @param  event  as String<br>
	 * @param  params as Object[]<br>
	 * @return        as Object
	 */
	public Object onEvent(final String event, final Object... params)
	{
		if (next == null)
		{
			return null;
		}
		return next.onEvent(event, params);
	}
	
	/**
	 * @param  simpleClassName as String<br>
	 * @return                 as BaseExtender - null
	 */
	public BaseExtender getExtender(final String simpleClassName)
	{
		if (this.getClass().getSimpleName().compareTo(simpleClassName) == 0)
		{
			return this;
		}
		else if (next != null)
		{
			return next.getExtender(simpleClassName);
		}
		else
		{
			return null;
		}
	}
	
	public void removeExtender(final BaseExtender ext)
	{
		if (next != null)
		{
			if (next == ext)
			{
				next = next.next;
			}
			else
			{
				next.removeExtender(ext);
			}
		}
	}
	
	public BaseExtender getNextExtender()
	{
		return next;
	}
	
	/**
	 * @param newExtender as BaseExtender
	 */
	public void addExtender(final BaseExtender newExtender)
	{
		if (next == null)
		{
			next = newExtender;
		}
		else
		{
			next.addExtender(newExtender);
		}
	}
}
