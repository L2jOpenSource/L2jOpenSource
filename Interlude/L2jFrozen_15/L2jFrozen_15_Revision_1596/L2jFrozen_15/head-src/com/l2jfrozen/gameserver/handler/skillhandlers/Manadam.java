package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;

/**
 * Class handling the Mana damage skill
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.MANADAM
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		L2Character target = null;
		
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		/*
		 * boolean ss = false; boolean bss = false; L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance(); if(weaponInst != null) { if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) { bss = true; weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); } else
		 * if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) { ss = true; weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); } } weaponInst = null;
		 */
		final boolean sps = activeChar.checkSps();
		final boolean bss = activeChar.checkBss();
		
		for (final L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			if (target.reflectSkill(skill))
			{
				target = activeChar;
			}
			
			if (target == null)
			{
				continue;
			}
			
			final boolean acted = Formulas.getInstance().calcMagicAffected(activeChar, target, skill);
			if (target.isInvul() || !acted)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
			}
			else
			{
				final double damage = Formulas.getInstance().calcManaDam(activeChar, target, skill, sps, bss);
				
				final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				
				if (damage > 0)
				{
					if (target.isSleeping())
					{
						target.stopSleeping(null);
					}
				}
				
				StatusUpdate sump = new StatusUpdate(target.getObjectId());
				sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				target.sendPacket(sump);
				sump = null;
				
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1);
				
				if (activeChar instanceof L2NpcInstance)
				{
					final int mobId = ((L2NpcInstance) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else if (activeChar instanceof L2Summon)
				{
					final int mobId = ((L2Summon) activeChar).getNpcId();
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(activeChar.getName());
				}
				sm.addNumber((int) mp);
				target.sendPacket(sm);
				
				target = null;
				sm = null;
				
				if (activeChar instanceof L2PcInstance)
				{
					final SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.sendPacket(sm2);
				}
			}
		}
		
		if (bss)
		{
			activeChar.removeBss();
		}
		else if (sps)
		{
			activeChar.removeSps();
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
