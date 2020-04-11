package com.l2jfrozen.gameserver.handler.skillhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Util;

/**
 * @authors L2JFrozen
 */
public class SummonFriend implements ISkillHandler
{
	// private static Logger LOGGER = Logger.getLogger(SummonFriend.class);
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activePlayer = (L2PcInstance) activeChar;
		
		if (!L2PcInstance.checkSummonerStatus(activePlayer))
		{
			return;
		}
		
		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activePlayer.inEvent)
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer.inEventCTF && CTF.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer.inEventDM && DM.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer.inEventTvT && TvT.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		
		// Checks summoner not in siege zone
		if (activeChar.isInsideZone(L2Character.ZONE_SIEGE))
		{
			((L2PcInstance) activeChar).sendMessage("You cannot summon in siege zone.");
			return;
		}
		
		// Checks summoner not in arenas, siege zones, jail
		if (activePlayer.isInsideZone(L2Character.ZONE_PVP))
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}
		
		if (GrandBossManager.getInstance().getZone(activePlayer) != null && !activePlayer.isGM())
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
			return;
		}
		
		try
		{
			for (final L2Object target1 : targets)
			{
				if (!(target1 instanceof L2Character))
				{
					continue;
				}
				
				L2Character target = (L2Character) target1;
				if (activeChar == target)
				{
					continue;
				}
				
				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;
					
					if (!L2PcInstance.checkSummonTargetStatus(targetChar, activePlayer))
					{
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
					
					if (targetChar.inEvent)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar.inEventCTF)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar.inEventDM)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar.inEventTvT)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					
					if (targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}
					
					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
					if (targetChar.isRooted() || targetChar.isInCombat())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}
					
					if (GrandBossManager.getInstance().getZone(targetChar) != null && !targetChar.isGM())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					// Check for the the target's festival status
					if (targetChar.isInOlympiadMode())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
						continue;
					}
					
					// Check for the the target's festival status
					if (targetChar.isFestivalParticipant())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Check for the target's jail status, arenas and siege zones
					if (targetChar.isInsideZone(L2Character.ZONE_PVP))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					activePlayer = null;
					
					// Requires a Summoning Crystal
					/* if (targetChar.getInventory().getItemByItemId(8615) == null) */
					if ((targetChar.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) // KidZor
					{
						((L2PcInstance) activeChar).sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal");
						targetChar.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal");
						continue;
					}
					
					if (!Util.checkIfInRange(0, activeChar, target, false))
					{
						// Check already summon
						if (!targetChar.teleportRequest((L2PcInstance) activeChar, skill))
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							activeChar.sendPacket(sm);
							continue;
						}
						
						// Summon friend
						if (skill.getId() == 1403)
						{
							// Send message
							final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addString(activeChar.getName());
							confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
							confirm.addTime(30000);
							confirm.addRequesterId(activeChar.getObjectId());
							targetChar.sendPacket(confirm);
						}
						else
						{
							L2PcInstance.teleToTarget(targetChar, (L2PcInstance) activeChar, skill);
							targetChar.teleportRequest(null, null);
						}
					}
					
					target = null;
					targetChar = null;
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