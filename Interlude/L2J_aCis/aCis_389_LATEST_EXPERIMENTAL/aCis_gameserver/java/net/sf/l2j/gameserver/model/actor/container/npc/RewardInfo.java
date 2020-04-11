package net.sf.l2j.gameserver.model.actor.container.npc;

import net.sf.l2j.gameserver.model.actor.Playable;

/**
 * This class is used to retain damage infos made on a L2Attackable. It is used for reward purposes.
 */
public final class RewardInfo
{
	private final Playable _attacker;
	
	private int _damage;
	
	public RewardInfo(Playable attacker)
	{
		_attacker = attacker;
	}
	
	public Playable getAttacker()
	{
		return _attacker;
	}
	
	public void addDamage(int damage)
	{
		_damage += damage;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj instanceof RewardInfo)
			return (((RewardInfo) obj)._attacker == _attacker);
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _attacker.getObjectId();
	}
}