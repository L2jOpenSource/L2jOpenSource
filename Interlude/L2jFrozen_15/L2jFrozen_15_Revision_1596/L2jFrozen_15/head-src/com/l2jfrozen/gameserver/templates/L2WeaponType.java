package com.l2jfrozen.gameserver.templates;

/**
 * @author mkizub <BR>
 *         Description of Weapon Type
 */
public enum L2WeaponType
{
	NONE(1, "Shield"), // Shields!!!
	SWORD(2, "Sword"),
	BLUNT(3, "Blunt"),
	DAGGER(4, "Dagger"),
	BOW(5, "Bow"),
	POLE(6, "Pole"),
	ETC(7, "Etc"),
	FIST(8, "Fist"),
	DUAL(9, "Dual Sword"),
	DUALFIST(10, "Dual Fist"),
	BIGSWORD(11, "Big Sword"), // Two Handed Swords
	PET(12, "Pet"),
	ROD(13, "Rod"),
	BIGBLUNT(14, "Big Blunt"); // Two handed blunt
	
	private final int id;
	private final String name;
	
	/**
	 * Constructor of the L2WeaponType.
	 * @param id   : int designating the ID of the WeaponType
	 * @param name : String designating the name of the WeaponType
	 */
	private L2WeaponType(final int id, final String name)
	{
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	public int mask()
	{
		return 1 << id;
	}
	
	/**
	 * Returns the name of the WeaponType
	 * @return String
	 */
	@Override
	public String toString()
	{
		return name;
	}
	
}
