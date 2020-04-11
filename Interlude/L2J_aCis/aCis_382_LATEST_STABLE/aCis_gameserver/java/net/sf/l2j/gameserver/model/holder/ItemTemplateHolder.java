package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.commons.util.StatsSet;

/**
 * An holder used for item stored on PlayerTemplate.
 */
public class ItemTemplateHolder extends IntIntHolder
{
	private final boolean _isEquipped;
	
	public ItemTemplateHolder(StatsSet set)
	{
		super(set.getInteger("id"), set.getInteger("count"));
		
		_isEquipped = set.getBool("isEquipped", true);
	}
	
	public final boolean isEquipped()
	{
		return _isEquipped;
	}
}