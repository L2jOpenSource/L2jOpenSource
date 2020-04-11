package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.skills.Formulas;

/*
 * Just a quick draft to support Wrath skill. Missing angle based calculation etc.
 */

public class CpDam implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Mdam.class);
	
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.CPDAM
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PlayableInstance))
		{
			// no cp damages for not playable instances
			return;
		}
		
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		/*
		 * boolean ss = false; boolean sps = false; boolean bss = false; L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance(); if(weaponInst != null) { if(skill.isMagic()) { if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) { bss = true; } else
		 * if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) { sps = true; } } else if(weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT) { ss = true; } } // If there is no weapon equipped, check for an active summon. else if(activeChar instanceof L2Summon) {
		 * L2Summon activeSummon = (L2Summon) activeChar; if(activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) { bss = true; activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); } else if(activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT) {
		 * ss = true; activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); } activeSummon = null; } weaponInst = null;
		 */
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		final boolean ss = activeChar.checkSs();
		
		for (final L2Object target2 : targets)
		{
			if (target2 == null)
			{
				continue;
			}
			
			L2Character target = (L2Character) target2;
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}
			
			if (target.isInvul())
			{
				continue;
			}
			
			if (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
			{
				return;
			}
			
			final int damage = (int) (target.getCurrentCp() * (1 - skill.getPower()));
			
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			skill.getEffects(activeChar, target, ss, sps, bss);
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.setCurrentCp(target.getCurrentCp() - damage);
			
			target = null;
		}
		
		if (skill.isMagic())
		{
			if (bss)
			{
				activeChar.removeBss();
			}
			else if (sps)
			{
				activeChar.removeSps();
			}
			
		}
		else
		{
			
			activeChar.removeSs();
			
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
