package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.DM;

public class DMCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"dmjoin",
		"dmleave",
		"dminfo"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.startsWith("dmjoin"))
		{
			JoinDM(activeChar);
		}
		else if (command.startsWith("dmleave"))
		{
			LeaveDM(activeChar);
		}
		
		else if (command.startsWith("dminfo"))
		{
			DMinfo(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinDM(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.is_joining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if (DM.is_joining() && activeChar.inEventDM)
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
		else if (activeChar.getLevel() < DM.get_minlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too low.");
			return false;
		}
		else if (activeChar.getLevel() > DM.get_maxlvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too high.");
			return false;
		}
		else if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you have Karma.");
			return false;
		}
		else if (DM.is_teleport() || DM.isStarted())
		{
			activeChar.sendMessage("DeathMatch Event registration period is over. You can't register now.");
			return false;
		}
		else
		{
			activeChar.sendMessage("Your participation in the DeathMatch event has been approved.");
			DM.addPlayer(activeChar);
			return true;
		}
	}
	
	public boolean LeaveDM(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.is_joining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if ((DM.is_teleport() || DM.isStarted()) && activeChar.inEventDM)
		{
			activeChar.sendMessage("You can not leave now because DeathMatch event has started.");
			return false;
		}
		else if (DM.is_joining() && !activeChar.inEventDM)
		{
			activeChar.sendMessage("You aren't registered in the DeathMatch Event.");
			return false;
		}
		else
		{
			DM.removePlayer(activeChar);
			return true;
		}
	}
	
	public boolean DMinfo(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!DM.is_joining())
		{
			activeChar.sendMessage("There is no DeathMatch Event in progress.");
			return false;
		}
		else if (DM.is_teleport() || DM.isStarted())
		{
			activeChar.sendMessage("I can't provide you this info. Command available only in joining period.");
			return false;
		}
		else
		{
			if (DM.players.size() == 1)
			{
				activeChar.sendMessage("There is " + DM.players.size() + " player participating in this event.");
				activeChar.sendMessage("Reward: " + DM.get_rewardAmount() + " " + ItemTable.getInstance().getTemplate(DM.get_rewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + DM.get_minlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + DM.get_maxlvl() + ".");
			}
			else
			{
				activeChar.sendMessage("There are " + DM.players.size() + " players participating in this event.");
				activeChar.sendMessage("Reward: " + DM.get_rewardAmount() + " " + ItemTable.getInstance().getTemplate(DM.get_rewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + DM.get_minlvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + DM.get_maxlvl() + ".");
			}
			return true;
		}
	}
}