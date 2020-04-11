package com.l2jfrozen.gameserver.templates;

/**
 * Description of Armor Type
 */

public enum L2ArmorType
{
	NONE(1, "None"),
	LIGHT(2, "Light"),
	HEAVY(3, "Heavy"),
	MAGIC(4, "Magic"),
	PET(5, "Pet");
	
	final int id;
	final String name;
	
	/**
	 * Constructor of the L2ArmorType.
	 * @param id   : int designating the ID of the ArmorType
	 * @param name : String designating the name of the ArmorType
	 */
	L2ArmorType(final int id, final String name)
	{
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the ID of the ArmorType after applying a mask.
	 * @return int : ID of the ArmorType after mask
	 */
	public int mask()
	{
		return 1 << id + 16;
	}
	
	/**
	 * Returns the name of the ArmorType
	 * @return String
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
