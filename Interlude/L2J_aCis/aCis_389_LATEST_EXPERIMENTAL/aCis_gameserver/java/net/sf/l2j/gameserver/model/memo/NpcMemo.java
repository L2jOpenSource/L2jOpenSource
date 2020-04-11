package net.sf.l2j.gameserver.model.memo;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;

/**
 * A basic implementation of {@link AbstractMemo} used for NPC. There is no restore/save options, it is mostly used for dynamic content.
 */
@SuppressWarnings("serial")
public class NpcMemo extends AbstractMemo
{
	@Override
	public int getInteger(String key)
	{
		return super.getInteger(key, 0);
	}
	
	@Override
	public boolean restoreMe()
	{
		return true;
	}
	
	@Override
	public boolean storeMe()
	{
		return true;
	}
	
	/**
	 * Gets the stored player.
	 * @param name the name of the variable
	 * @return the stored player or {@code null}
	 */
	public Player getPlayer(String name)
	{
		return getObject(name, Player.class);
	}
	
	/**
	 * Gets the stored summon.
	 * @param name the name of the variable
	 * @return the stored summon or {@code null}
	 */
	public Summon getSummon(String name)
	{
		return getObject(name, Summon.class);
	}
}