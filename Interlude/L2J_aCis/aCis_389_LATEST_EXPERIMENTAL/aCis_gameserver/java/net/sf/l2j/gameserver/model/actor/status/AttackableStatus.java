package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;

public class AttackableStatus extends NpcStatus
{
	public AttackableStatus(Attackable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHpConsumption)
	{
		if (getActiveChar().isDead())
			return;
		
		if (getActiveChar() instanceof Monster)
			((Monster) getActiveChar()).getOverhitState().test(attacker, value);
		
		super.reduceHp(value, attacker, awake, isDOT, isHpConsumption);
	}
	
	@Override
	public Attackable getActiveChar()
	{
		return (Attackable) super.getActiveChar();
	}
}