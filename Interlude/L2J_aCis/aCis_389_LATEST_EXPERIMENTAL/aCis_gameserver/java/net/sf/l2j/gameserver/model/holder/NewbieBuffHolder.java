package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.commons.util.StatsSet;

/**
 * A container used by Newbie Buffers. Those are beneficial magic effects launched on newbie players in order to help them in their Lineage 2 adventures.<br>
 * <br>
 * Those buffs got level limitation, and are class based (fighter or mage type).
 */
public class NewbieBuffHolder extends IntIntHolder
{
	private int _lowerLevel;
	private int _upperLevel;
	private boolean _isMagicClass;
	
	public NewbieBuffHolder(StatsSet set)
	{
		super(set.getInteger("skillId"), set.getInteger("skillLevel"));
		
		_lowerLevel = set.getInteger("lowerLevel");
		_upperLevel = set.getInteger("upperLevel");
		_isMagicClass = set.getBool("isMagicClass");
	}
	
	/**
	 * @return the lower level that the player must achieve in order to obtain this buff.
	 */
	public int getLowerLevel()
	{
		return _lowerLevel;
	}
	
	/**
	 * @return the upper level that the player mustn't exceed in order to obtain this buff.
	 */
	public int getUpperLevel()
	{
		return _upperLevel;
	}
	
	/**
	 * @return false if it's a fighter buff, true if it's a magic buff.
	 */
	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}
}