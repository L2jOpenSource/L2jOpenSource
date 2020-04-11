package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

import main.data.memory.ObjectData;
import main.enums.TeamType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;

/**
 * This class ...
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends L2GameServerPacket
{
	// ddddddddddddddddddffffdddcccccSSddd dddddc
	// ddddddddddddddddddffffdddcccccSSddd dddddccffd
	
	private L2Character activeChar;
	private int x, y, z, heading;
	private int idTemplate;
	private boolean isAttackable, isSummoned;
	private int mAtkSpd, pAtkSpd;
	private int runSpd, walkSpd, swimRunSpd, swimWalkSpd, flRunSpd, flWalkSpd, flyRunSpd, flyWalkSpd;
	private int rhand, lhand;
	private int collisionHeight, collisionRadius;
	private String name = "";
	private String title = "";
	
	public NpcInfo(final L2NpcInstance cha, final L2Character attacker)
	{
		/*
		 * if(cha.getMxcPoly() != null) { attacker.sendPacket(new MxCPolyInfo(cha)); return; }
		 */
		if (cha.getCustomNpcInstance() != null)
		{
			attacker.sendPacket(new CustomNpcInfo(cha));
			attacker.broadcastPacket(new FinishRotation(cha));
			return;
		}
		activeChar = cha;
		idTemplate = cha.getTemplate().idTemplate;
		isAttackable = cha.isAutoAttackable(attacker);
		rhand = cha.getRightHandItem();
		lhand = cha.getLeftHandItem();
		isSummoned = false;
		collisionHeight = cha.getCollisionHeight();
		collisionRadius = cha.getCollisionRadius();
		if (cha.getTemplate().serverSideName)
		{
			name = cha.getTemplate().name;
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			title = Config.L2JMOD_CHAMP_TITLE;
		}
		else if (cha.getTemplate().serverSideTitle)
		{
			title = cha.getTemplate().title;
		}
		else
		{
			title = cha.getTitle();
		}
		
		if (Config.SHOW_NPC_LVL && activeChar instanceof L2MonsterInstance)
		{
			String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
			if (title != null)
			{
				t += " " + title;
			}
			
			title = t;
		}
		
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
	}
	
	/**
	 * Instantiates a new npc info.
	 * @param cha      the cha
	 * @param attacker the attacker
	 */
	public NpcInfo(final L2Summon cha, final L2Character attacker)
	{
		activeChar = cha;
		idTemplate = cha.getTemplate().idTemplate;
		isAttackable = cha.isAutoAttackable(attacker); // (cha.getKarma() > 0);
		rhand = 0;
		lhand = 0;
		isSummoned = cha.isShowSummonAnimation();
		collisionHeight = activeChar.getTemplate().collisionHeight;
		collisionRadius = activeChar.getTemplate().collisionRadius;
		if (cha.getTemplate().serverSideName || cha instanceof L2PetInstance || cha instanceof L2SummonInstance)
		{
			name = activeChar.getName();
			title = cha.getTitle();
		}
		
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
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar instanceof L2Summon)
		{
			if (((L2Summon) activeChar).getOwner() != null && ((L2Summon) activeChar).getOwner().getAppearance().isInvisible())
			{
				return;
			}
		}
		writeC(0x16);
		writeD(activeChar.getObjectId());
		writeD(idTemplate + 1000000); // npctype id
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
		writeF(1.1/* activeChar.getProperMultiplier() */);
		// writeF(1/*activeChar.getAttackSpeedMultiplier()*/);
		writeF(pAtkSpd / 277.478340719);
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(rhand); // right hand weapon
		writeD(0);
		writeD(lhand); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(activeChar.isRunning() ? 1 : 0);
		writeC(activeChar.isInCombat() ? 1 : 0);
		writeC(activeChar.isAlikeDead() ? 1 : 0);
		writeC(isSummoned ? 2 : 0); // invisible ?? 0=false 1=true 2=summoned (only works if model has a summon animation)
		writeS(name);
		writeS(title);
		
		if (activeChar instanceof L2Summon)
		{
			writeD(0x01);// Title color 0=client default
			writeD(((L2Summon) activeChar).getPvpFlag());
			writeD(((L2Summon) activeChar).getKarma());
		}
		else
		{
			writeD(0);
			writeD(0);
			writeD(0);
		}
		
		writeD(activeChar.getAbnormalEffect()); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeD(0000); // C2
		writeC(0000); // C2
		
		//writeC(0x00); // C3 team circle 1-blue, 2-red
		TeamType team = TeamType.NONE;
		if (activeChar instanceof L2Summon)
		{
			team = ObjectData.get(PlayerHolder.class, ((L2Summon) activeChar).getOwner()).getTeam();
		}
		else
		{
			team = ObjectData.get(CharacterHolder.class, activeChar).getTeam();
		}
		writeC(team.ordinal());
		//writeC(activeChar instanceof L2Summon ? ((L2Summon) activeChar).getOwner().getTeam().ordinal() : activeChar.getTeam().ordinal());

		
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(0x00); // C4
		writeD(0x00); // C6
	}
	
	@Override
	public String getType()
	{
		return "[S] 16 NpcInfo";
	}
}
