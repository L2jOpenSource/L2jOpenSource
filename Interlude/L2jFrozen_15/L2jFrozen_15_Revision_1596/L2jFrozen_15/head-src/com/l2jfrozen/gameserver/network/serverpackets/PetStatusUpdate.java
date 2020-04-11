package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 * @version $Revision: 1.5.2.3.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class PetStatusUpdate extends L2GameServerPacket
{
	private final L2Summon summonInstance;
	private final int maxHp, maxMp;
	private int maxFed, curFed;
	
	public PetStatusUpdate(final L2Summon summon)
	{
		summonInstance = summon;
		maxHp = summonInstance.getMaxHp();
		maxMp = summonInstance.getMaxMp();
		if (summonInstance instanceof L2PetInstance)
		{
			final L2PetInstance pet = (L2PetInstance) summonInstance;
			curFed = pet.getCurrentFed(); // how fed it is
			maxFed = pet.getMaxFed(); // max fed it can be
		}
		else if (summonInstance instanceof L2SummonInstance)
		{
			final L2SummonInstance sum = (L2SummonInstance) summonInstance;
			curFed = sum.getTimeRemaining();
			maxFed = sum.getTotalLifeTime();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb5);
		writeD(summonInstance.getSummonType());
		writeD(summonInstance.getObjectId());
		writeD(summonInstance.getX());
		writeD(summonInstance.getY());
		writeD(summonInstance.getZ());
		writeS(summonInstance.getOwner().getName());
		writeD(curFed);
		writeD(maxFed);
		writeD((int) summonInstance.getCurrentHp());
		writeD(maxHp);
		writeD((int) summonInstance.getCurrentMp());
		writeD(maxMp);
		writeD(summonInstance.getLevel());
		writeQ(summonInstance.getStat().getExp());
		writeQ(summonInstance.getExpForThisLevel());// 0% absolute value
		writeQ(summonInstance.getExpForNextLevel());// 100% absolute value
	}
	
	@Override
	public String getType()
	{
		return "[S] B5 PetStatusUpdate";
	}
}
