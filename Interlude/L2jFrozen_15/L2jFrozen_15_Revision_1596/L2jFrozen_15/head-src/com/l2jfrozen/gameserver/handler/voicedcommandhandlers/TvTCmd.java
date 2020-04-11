package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.TvT;

public class TvTCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"tvtjoin",
		"tvtleave",
		"tvtinfo"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.startsWith("tvtjoin"))
		{
			JoinTvT(activeChar);
		}
		else if (command.startsWith("tvtleave"))
		{
			LeaveTvT(activeChar);
		}
		
		else if (command.startsWith("tvtinfo"))
		{
			TvTinfo(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinTvT(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!TvT.isJoining())
		{
			activeChar.sendMessage("There is no TvT Event in progress.");
			return false;
		}
		else if (TvT.isJoining() && activeChar.inEventTvT)
		{
			activeChar.sendMessage("You are already registered.");
			return false;
		}
		else if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are holding a Cursed Weapon.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you are in Olympiad.");
			return false;
		}
		else if (activeChar.getLevel() < TvT.getMinlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too low.");
			return false;
		}
		else if (activeChar.getLevel() > TvT.getMaxlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too high.");
			return false;
		}
		else if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you have Karma.");
			return false;
		}
		else if (TvT.isTeleport() || TvT.isStarted())
		{
			activeChar.sendMessage("TvT Event registration period is over. You can't register now.");
			return false;
		}
		else
		{
			activeChar.sendMessage("Your participation in the TvT event has been approved.");
			TvT.addPlayer(activeChar, "");
			return false;
		}
	}
	
	public boolean LeaveTvT(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!TvT.isJoining())
		{
			activeChar.sendMessage("There is no TvT Event in progress.");
			return false;
		}
		else if ((TvT.isTeleport() || TvT.isStarted()) && activeChar.inEventTvT)
		{
			activeChar.sendMessage("You can not leave now because TvT event has started.");
			return false;
		}
		else if (TvT.isJoining() && !activeChar.inEventTvT)
		{
			activeChar.sendMessage("You aren't registered in the TvT Event.");
			return false;
		}
		else
		{
			TvT.removePlayer(activeChar);
			return true;
		}
	}
	
	public boolean TvTinfo(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!TvT.isJoining())
		{
			activeChar.sendMessage("There is no TvT Event in progress.");
			return false;
		}
		else if (TvT.isTeleport() || TvT.isStarted())
		{
			activeChar.sendMessage("I can't provide you this info. Command available only in joining period.");
			return false;
		}
		else
		{
			if (TvT.playersShuffle.size() == 1)
			{
				activeChar.sendMessage("There is " + TvT.playersShuffle.size() + " player participating in this event.");
				activeChar.sendMessage("Reward: " + TvT.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(TvT.getRewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + TvT.getMinlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + TvT.getMaxlvl() + ".");
			}
			else
			{
				activeChar.sendMessage("There are " + TvT.playersShuffle.size() + " players participating in this event.");
				activeChar.sendMessage("Reward: " + TvT.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(TvT.getRewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + TvT.getMinlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + TvT.getMaxlvl() + ".");
			}
			return true;
		}
	}
}