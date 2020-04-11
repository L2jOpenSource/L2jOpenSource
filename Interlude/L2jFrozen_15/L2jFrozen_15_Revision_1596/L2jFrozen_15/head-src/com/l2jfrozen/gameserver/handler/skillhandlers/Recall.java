package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(Recall.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.RECALL
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		try
		{
			if (activeChar instanceof L2PcInstance)
			{
				final L2PcInstance instance = (L2PcInstance) activeChar;
				
				if (instance.isInOlympiadMode())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
					return;
				}
				
				// Checks summoner not in siege zone
				if (activeChar.isInsideZone(L2Character.ZONE_SIEGE))
				{
					((L2PcInstance) activeChar).sendMessage("You cannot summon in siege zone.");
					return;
				}
				
				if (activeChar.isInsideZone(L2Character.ZONE_PVP))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
					return;
				}
				
				if (GrandBossManager.getInstance().getZone(instance) != null && !instance.isGM())
				{
					instance.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
					return;
				}
			}
			
			for (final L2Object target1 : targets)
			{
				if (!(target1 instanceof L2Character))
				{
					continue;
				}
				
				L2Character target = (L2Character) target1;
				
				if (target instanceof L2PcInstance)
				{
					final L2PcInstance targetChar = (L2PcInstance) target;
					
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skill in a festival."));
						continue;
					}
					
					if (targetChar.isInFunEvent())
					{
						targetChar.sendMessage("You can't use escape skill in Event.");
						continue;
					}
					
					if (targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't escape from jail."));
						continue;
					}
					
					if (targetChar.isInDuel())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skills during a duel."));
						continue;
					}
					
					if (targetChar.isAlikeDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}
					
					if (targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}
					
					/*
					 * Like L2OFF player can be recalled also if he is on combat/rooted if(targetChar.isRooted() || targetChar.isInCombat()) { SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED); sm.addString(targetChar.getName()); activeChar.sendPacket(sm); sm = null;
					 * continue; }
					 */
					
					if (GrandBossManager.getInstance().getZone(targetChar) != null && !targetChar.isGM())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					if (targetChar.isInOlympiadMode())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
						continue;
					}
					
					if (targetChar.isInsideZone(L2Character.ZONE_PVP))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
				}
				
				target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				target = null;
			}
			
			if (skill.isMagic() && skill.useSpiritShot())
			{
				if (activeChar.checkBss())
				{
					activeChar.removeBss();
				}
				if (activeChar.checkSps())
				{
					activeChar.removeSps();
				}
			}
			else if (skill.useSoulShot())
			{
				if (activeChar.checkSs())
				{
					activeChar.removeSs();
				}
			}
		}
		catch (final Throwable e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}