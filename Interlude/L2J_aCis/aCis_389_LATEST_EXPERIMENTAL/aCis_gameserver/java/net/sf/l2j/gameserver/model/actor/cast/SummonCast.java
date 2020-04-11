package net.sf.l2j.gameserver.model.actor.cast;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * This class groups all cast data related to a {@link Summon}.
 */
public class SummonCast extends CreatureCast
{
	public SummonCast(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public void doCast(L2Skill skill)
	{
		final Player actingPlayer = _creature.getActingPlayer();
		
		if (!actingPlayer.checkPvpSkill(_creature.getTarget(), skill) && !actingPlayer.getAccessLevel().allowPeaceAttack())
		{
			// Send a System Message to the Player
			actingPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			
			// Send ActionFailed to the Player
			actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		super.doCast(skill);
	}
}