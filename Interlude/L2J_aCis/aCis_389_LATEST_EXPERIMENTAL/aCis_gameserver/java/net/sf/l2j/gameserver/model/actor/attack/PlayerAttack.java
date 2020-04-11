package net.sf.l2j.gameserver.model.actor.attack;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * This class groups all attack data related to a {@link Creature}.
 */
public class PlayerAttack extends CreatureAttack
{
	public PlayerAttack(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public void doAttack(Creature target)
	{
		super.doAttack(target);
		
		((Player) _creature).clearRecentFakeDeath();
	}
}