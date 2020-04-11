package com.l2jfrozen.gameserver.templates;

/**
 * Description of EtcItem Type
 */

public enum L2EtcItemType
{
	ARROW(0, "Arrow"),
	MATERIAL(1, "Material"),
	PET_COLLAR(2, "PetCollar"),
	POTION(3, "Potion"),
	RECEIPE(4, "Receipe"),
	SCROLL(5, "Scroll"),
	QUEST(6, "Quest"),
	MONEY(7, "Money"),
	OTHER(8, "Other"),
	SPELLBOOK(9, "Spellbook"),
	SEED(10, "Seed"),
	SHOT(11, "Shot"),
	HERB(12, "Herb");
	
	final int id;
	final String name;
	
	/**
	 * Constructor of the L2EtcItemType.
	 * @param id   : int designating the ID of the EtcItemType
	 * @param name : String designating the name of the EtcItemType
	 */
	L2EtcItemType(final int id, final String name)
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
		return 1 << id + 21;
	}
	
	/**
	 * Returns the name of the EtcItemType
	 * @return String
	 */
	@Override
	public String toString()
	{
		return name;
	}
}
