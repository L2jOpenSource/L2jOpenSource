package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

/**
 * This class ...
 * @version $Revision: 1.6.2.5.2.12 $ $Date: 2005/03/31 09:19:16 $
 */
public class PetInfo extends L2GameServerPacket
{
	private final L2Summon summonInstance;
	private final int x, y, z, heading;
	private final boolean isSummoned;
	private final int mAtkSpd, pAtkSpd;
	private final int runSpd, walkSpd, swimRunSpd, swimWalkSpd;
	private int flRunSpd;
	private int flWalkSpd;
	private int flyRunSpd;
	private int flyWalkSpd;
	private final int maxHp, maxMp;
	private int maxFed, curFed;
	
	/**
	 * rev 478 dddddddddddddddddddffffdddcccccSSdddddddddddddddddddddddddddhc
	 * @param summon
	 */
	public PetInfo(final L2Summon summon)
	{
		summonInstance = summon;
		isSummoned = summonInstance.isShowSummonAnimation();
		x = summonInstance.getX();
		y = summonInstance.getY();
		z = summonInstance.getZ();
		heading = summonInstance.getHeading();
		mAtkSpd = summonInstance.getMAtkSpd();
		pAtkSpd = summonInstance.getPAtkSpd();
		runSpd = summonInstance.getRunSpeed();
		walkSpd = summonInstance.getWalkSpeed();
		swimRunSpd = flRunSpd = flyRunSpd = runSpd;
		swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
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
		writeC(0xb1);
		writeD(summonInstance.getSummonType());
		writeD(summonInstance.getObjectId());
		writeD(summonInstance.getTemplate().idTemplate + 1000000);
		writeD(0); // 1=attackable
		
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
		writeD(0);
		writeD(mAtkSpd);
		writeD(pAtkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(swimRunSpd);
		writeD(swimWalkSpd);
		writeD(flRunSpd);
		writeD(flWalkSpd);
		writeD(flyRunSpd);
		writeD(flyWalkSpd);
		
		writeF(1/* cha.getProperMultiplier() */);
		writeF(1/* cha.getAttackSpeedMultiplier() */);
		writeF(summonInstance.getTemplate().collisionRadius);
		writeF(summonInstance.getTemplate().collisionHeight);
		writeD(0); // right hand weapon
		writeD(0);
		writeD(0); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(summonInstance.isRunning() ? 1 : 0); // running=1
		writeC(summonInstance.isInCombat() ? 1 : 0); // attacking 1=true
		writeC(summonInstance.isAlikeDead() ? 1 : 0); // dead 1=true
		writeC(isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(summonInstance.getName());
		writeS(summonInstance.getTitle());
		writeD(1);
		writeD(summonInstance.getOwner() != null ? summonInstance.getOwner().getPvpFlag() : 0); // 0 = white,2= purpleblink, if its greater then karma = purple
		writeD(summonInstance.getOwner() != null ? summonInstance.getOwner().getKarma() : 0); // karma
		writeD(curFed); // how fed it is
		writeD(maxFed); // max fed it can be
		writeD((int) summonInstance.getCurrentHp());// current hp
		writeD(maxHp);// max hp
		writeD((int) summonInstance.getCurrentMp());// current mp
		writeD(maxMp);// max mp
		writeD(summonInstance.getStat().getSp()); // sp
		writeD(summonInstance.getLevel());// lvl
		writeQ(summonInstance.getStat().getExp());
		writeQ(summonInstance.getExpForThisLevel());// 0% absolute value
		writeQ(summonInstance.getExpForNextLevel());// 100% absoulte value
		writeD(summonInstance instanceof L2PetInstance ? summonInstance.getInventory().getTotalWeight() : 0);// weight
		writeD(summonInstance.getMaxLoad());// max weight it can carry
		writeD(summonInstance.getPAtk(null));// patk
		writeD(summonInstance.getPDef(null));// pdef
		writeD(summonInstance.getMAtk(null, null));// matk
		writeD(summonInstance.getMDef(null, null));// mdef
		writeD(summonInstance.getAccuracy());// accuracy
		writeD(summonInstance.getEvasionRate(null));// evasion
		writeD(summonInstance.getCriticalHit(null, null));// critical
		writeD(runSpd);// speed
		writeD(summonInstance.getPAtkSpd());// atkspeed
		writeD(summonInstance.getMAtkSpd());// casting speed
		
		writeD(0);// c2 abnormal visual effect... bleed=1; poison=2; poison & bleed=3; flame=4;
		final int npcId = summonInstance.getTemplate().npcId;
		
		if (npcId >= 12526 && npcId <= 12528)
		{
			writeH(1);// c2 ride button
		}
		else
		{
			writeH(0);
		}
		
		writeC(0); // c2
		
		// Following all added in C4.
		writeH(0); // ??
		writeC(0); // team aura (1 = blue, 2 = red)
		writeD(summonInstance.getSoulShotsPerHit()); // How many soulshots this servitor uses per hit
		writeD(summonInstance.getSpiritShotsPerHit()); // How many spiritshots this servitor uses per hit
	}
	
	@Override
	public String getType()
	{
		return "[S] b1 PetInfo";
	}
	
}
