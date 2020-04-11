package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfoPoly extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddc
	private L2Character activeChar;
	private final L2Object object;
	private int x, y, z, heading;
	private final int npcId;
	private boolean isAttackable;
	private final boolean isSummoned;
	private boolean isRunning;
	private boolean isInCombat;
	private boolean isAlikeDead;
	private int mAtkSpd, pAtkSpd;
	private int runSpd, walkSpd, swimRunSpd, swimWalkSpd, flRunSpd, flWalkSpd, flyRunSpd, flyWalkSpd;
	private int rhand, lhand;
	private String name, title;
	private int abnormalEffect;
	private L2NpcTemplate template;
	private final int collisionRadius;
	private final int collisionHeight;
	
	/**
	 * Instantiates a new npc info poly.
	 * @param obj      the obj
	 * @param attacker the attacker
	 */
	public NpcInfoPoly(final L2Object obj, final L2Character attacker)
	{
		object = obj;
		npcId = obj.getPoly().getPolyId();
		template = NpcTable.getInstance().getTemplate(npcId);
		isAttackable = true;
		rhand = 0;
		lhand = 0;
		isSummoned = false;
		collisionRadius = template.collisionRadius;
		collisionHeight = template.collisionHeight;
		if (object instanceof L2Character)
		{
			activeChar = (L2Character) obj;
			isAttackable = obj.isAutoAttackable(attacker);
			rhand = template.rhand;
			lhand = template.lhand;
			
		}
		
		if (object instanceof L2ItemInstance)
		{
			x = object.getX();
			y = object.getY();
			z = object.getZ();
			heading = 0;
			mAtkSpd = 100; // yes, an item can be dread as death
			pAtkSpd = 100;
			runSpd = 120;
			walkSpd = 80;
			swimRunSpd = flRunSpd = flyRunSpd = runSpd;
			swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
			isRunning = isInCombat = isAlikeDead = false;
			name = "item";
			title = "polymorphed";
			abnormalEffect = 0;
		}
		else
		{
			x = activeChar.getX();
			y = activeChar.getY();
			z = activeChar.getZ();
			heading = activeChar.getHeading();
			mAtkSpd = activeChar.getMAtkSpd();
			pAtkSpd = activeChar.getPAtkSpd();
			runSpd = activeChar.getRunSpeed();
			walkSpd = activeChar.getWalkSpeed();
			swimRunSpd = flRunSpd = flyRunSpd = runSpd;
			swimWalkSpd = flWalkSpd = flyWalkSpd = walkSpd;
			isRunning = activeChar.isRunning();
			isInCombat = activeChar.isInCombat();
			isAlikeDead = activeChar.isAlikeDead();
			name = activeChar.getName();
			title = activeChar.getTitle();
			abnormalEffect = activeChar.getAbnormalEffect();
			
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x16);
		writeD(object.getObjectId());
		writeD(npcId + 1000000); // npctype id
		writeD(isAttackable ? 1 : 0);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
		writeD(0x00);
		writeD(mAtkSpd);
		writeD(pAtkSpd);
		writeD(runSpd);
		writeD(walkSpd);
		writeD(swimRunSpd/* 0x32 */); // swimspeed
		writeD(swimWalkSpd/* 0x32 */); // swimspeed
		writeD(flRunSpd);
		writeD(flWalkSpd);
		writeD(flyRunSpd);
		writeD(flyWalkSpd);
		writeF(1/* activeChar.getProperMultiplier() */);
		writeF(1/* activeChar.getAttackSpeedMultiplier() */);
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(rhand); // right hand weapon
		writeD(0);
		writeD(lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(isRunning ? 1 : 0);
		writeC(isInCombat ? 1 : 0);
		writeC(isAlikeDead ? 1 : 0);
		writeC(isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(name);
		writeS(title);
		writeD(0);
		writeD(0);
		writeD(0000); // hmm karma ??
		
		writeH(abnormalEffect); // C2
		writeH(0x00); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
	}
	
	@Override
	public String getType()
	{
		return "[S] 16 NpcInfo";
	}
}
