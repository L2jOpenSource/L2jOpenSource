package com.l2jfrozen.gameserver.templates;

/**
 * This class is dedicated to the management of EtcItem.
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:30:10 $
 */
public final class L2EtcItem extends L2Item
{
	/**
	 * Constructor for EtcItem.
	 * @see        L2Item constructor
	 * @param type : L2EtcItemType designating the type of object Etc
	 * @param set  : StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public L2EtcItem(final L2EtcItemType type, final StatsSet set)
	{
		super(type, set);
	}
	
	/**
	 * Returns the type of Etc Item
	 * @return L2EtcItemType
	 */
	@Override
	public L2EtcItemType getItemType()
	{
		return (L2EtcItemType) super.type;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	@Override
	public final boolean isConsumable()
	{
		return getItemType() == L2EtcItemType.SHOT || getItemType() == L2EtcItemType.POTION; // || (type == L2EtcItemType.SCROLL));
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
}
