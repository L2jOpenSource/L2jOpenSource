package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;

/**
 * sample 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000 - 0 damage (missed 0x80) 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10 3.... format dddc dddh (ddc)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class Attack extends L2GameServerPacket
{
	private class Hit
	{
		protected int targetId;
		protected int damage;
		protected int flags;
		
		Hit(final L2Object target, final int damage, final boolean miss, final boolean crit, final boolean shld)
		{
			targetId = target.getObjectId();
			this.damage = damage;
			if (soulshot)
			{
				flags |= 0x10 | grade;
			}
			if (crit)
			{
				flags |= 0x20;
			}
			if (shld)
			{
				flags |= 0x40;
			}
			if (miss)
			{
				flags |= 0x80;
			}
			
		}
	}
	
	// dh
	
	protected final int attackerObjId;
	public final boolean soulshot;
	protected int grade;
	private final int x;
	private final int y;
	private final int z;
	private Hit[] hits;
	
	/**
	 * @param attacker the attacker L2Character
	 * @param ss       true if useing SoulShots
	 * @param grade
	 */
	public Attack(final L2Character attacker, final boolean ss, final int grade)
	{
		attackerObjId = attacker.getObjectId();
		soulshot = ss;
		this.grade = grade;
		x = attacker.getX();
		y = attacker.getY();
		z = attacker.getZ();
		hits = new Hit[0];
	}
	
	/**
	 * Add this hit (target, damage, miss, critical, shield) to the Server-Client packet Attack.
	 * @param target
	 * @param damage
	 * @param miss
	 * @param crit
	 * @param shld
	 */
	public void addHit(final L2Object target, final int damage, final boolean miss, final boolean crit, final boolean shld)
	{
		// Get the last position in the hits table
		final int pos = hits.length;
		
		// Create a new Hit object
		final Hit[] tmp = new Hit[pos + 1];
		
		// Add the new Hit object to hits table
		System.arraycopy(hits, 0, tmp, 0, hits.length);
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		hits = tmp;
	}
	
	/**
	 * Return True if the Server-Client packet Attack contains at least 1 hit.
	 * @return
	 */
	public boolean hasHits()
	{
		return hits.length > 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x05);
		
		writeD(attackerObjId);
		writeD(hits[0].targetId);
		writeD(hits[0].damage);
		writeC(hits[0].flags);
		writeD(x);
		writeD(y);
		writeD(z);
		writeH(hits.length - 1);
		for (int i = 1; i < hits.length; i++)
		{
			writeD(hits[i].targetId);
			writeD(hits[i].damage);
			writeC(hits[i].flags);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 06 Attack";
	}
}
