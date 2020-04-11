package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @author  earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class BalanceLife implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.BALANCE_LIFE
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		// L2Character activeChar = activeChar;
		// check for other effects
		try
		{
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			
			if (handler != null)
			{
				handler.useSkill(activeChar, skill, targets);
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		L2Character target = null;
		
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		
		double fullHP = 0;
		double currentHPs = 0;
		
		for (final L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			// We should not heal if char is dead
			if (target == null || target.isDead())
			{
				continue;
			}
			
			// Avoid characters heal inside Baium lair from outside
			if ((activeChar.isInsideZone(12007) || target.isInsideZone(12007)) && ((GrandBossManager.getInstance().getZone(activeChar) == null && GrandBossManager.getInstance().getZone(target) != null) || (GrandBossManager.getInstance().getZone(target) == null && GrandBossManager.getInstance().getZone(activeChar) != null)))
			{
				continue;
			}
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquiped())
				{
					continue;
				}
				else if (player != null && player.isCursedWeaponEquiped())
				{
					continue;
				}
			}
			
			player = null;
			
			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}
		target = null;
		
		final double percentHP = currentHPs / fullHP;
		
		for (final L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			if (target == null || target.isDead())
			{
				continue;
			}
			
			final double newHP = target.getMaxHp() * percentHP;
			final double totalHeal = newHP - target.getCurrentHp();
			
			target.setCurrentHp(newHP);
			
			if (totalHeal > 0)
			{
				target.setLastHealAmount((int) totalHeal);
			}
			
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			su = null;
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("HP of the party has been balanced.");
			target.sendPacket(sm);
			sm = null;
			
		}
		target = null;
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
