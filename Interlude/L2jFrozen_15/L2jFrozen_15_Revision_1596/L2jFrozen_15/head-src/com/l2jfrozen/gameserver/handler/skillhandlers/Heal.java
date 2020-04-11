package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Heal.class);
	
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.HEAL,
		SkillType.HEAL_PERCENT,
		SkillType.HEAL_STATIC
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		
		// check for other effects
		try
		{
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			
			if (handler != null)
			{
				handler.useSkill(activeChar, skill, targets);
			}
			
			handler = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		
		L2Character target = null;
		
		for (final L2Object target2 : targets)
		{
			target = (L2Character) target2;
			
			if (target == null || target.isDead() || target.isInvul())
			{
				continue;
			}
			
			// Avoid players heal inside Baium lair from outside
			if ((activeChar.isInsideZone(12007) || target.isInsideZone(12007)) && ((GrandBossManager.getInstance().getZone(player) == null && GrandBossManager.getInstance().getZone(target) != null) || (GrandBossManager.getInstance().getZone(target) == null && GrandBossManager.getInstance().getZone(activeChar) != null)))
			{
				continue;
			}
			
			// We should not heal walls and door
			if (target instanceof L2DoorInstance)
			{
				continue;
			}
			
			// We should not heal siege flags
			if (target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062)
			{
				activeChar.getActingPlayer().sendMessage("You cannot heal siege flags!");
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
			
			double hp = skill.getPower();
			
			if (skill.getSkillType() == SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else
			{
				if (bss)
				{
					hp *= 1.5;
				}
				else if (sps)
				{
					hp *= 1.3;
				}
			}
			
			if (skill.getSkillType() == SkillType.HEAL_STATIC)
			{
				hp = skill.getPower();
			}
			else if (skill.getSkillType() != SkillType.HEAL_PERCENT)
			{
				hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			}
			
			target.setCurrentHp(hp + target.getCurrentHp());
			target.setLastHealAmount((int) hp);
			StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			su = null;
			
			if (target instanceof L2PcInstance)
			{
				if (skill.getId() == 4051)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.REJUVENATING_HP);
					target.sendPacket(sm);
					sm = null;
				}
				else
				{
					if (activeChar instanceof L2PcInstance && activeChar != target)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						target.sendPacket(sm);
						sm = null;
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						target.sendPacket(sm);
						sm = null;
					}
				}
			}
			target = null;
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}