package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;

public class NpcStatus extends CreatureStatus
{
	public NpcStatus(Npc activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
			return;
		
		if (attacker != null)
		{
			final Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer != null && attackerPlayer.isInDuel())
				attackerPlayer.setDuelState(DuelState.INTERRUPTED);
		}
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
	}
	
	@Override
	public Npc getActiveChar()
	{
		return (Npc) super.getActiveChar();
	}
}