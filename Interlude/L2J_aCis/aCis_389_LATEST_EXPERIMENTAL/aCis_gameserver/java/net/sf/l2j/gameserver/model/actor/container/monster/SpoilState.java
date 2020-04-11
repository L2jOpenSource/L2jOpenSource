package net.sf.l2j.gameserver.model.actor.container.monster;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

/**
 * A container holding all related informations of a {@link Monster} spoil state.<br>
 * <br>
 * A spoil occurs when a {@link Player} procs a spoil skill over a Monster.
 */
public class SpoilState
{
	private final List<IntIntHolder> _sweepItems = new ArrayList<>();
	
	private int _spoilerId;
	
	public SpoilState()
	{
	}
	
	public int getSpoilerId()
	{
		return _spoilerId;
	}
	
	public void setSpoilerId(int value)
	{
		_spoilerId = value;
	}
	
	/**
	 * @return true if the spoiler objectId is set.
	 */
	public boolean isSpoiled()
	{
		return _spoilerId > 0;
	}
	
	/**
	 * @param player : The Player to test.
	 * @return true if the given {@link Player} set as parameter is the actual spoiler.
	 */
	public boolean isActualSpoiler(Player player)
	{
		return player != null && player.getObjectId() == _spoilerId;
	}
	
	/**
	 * @return the {@link List} containing all {@link IntIntHolder} that can be spoiled.
	 */
	public List<IntIntHolder> getSweepItems()
	{
		return _sweepItems;
	}
	
	/**
	 * @return true if _sweepItems {@link List} is filled.
	 */
	public boolean isSweepable()
	{
		return !_sweepItems.isEmpty();
	}
	
	/**
	 * Clear all spoil related variables.
	 */
	public void clear()
	{
		_spoilerId = 0;
		_sweepItems.clear();
	}
}