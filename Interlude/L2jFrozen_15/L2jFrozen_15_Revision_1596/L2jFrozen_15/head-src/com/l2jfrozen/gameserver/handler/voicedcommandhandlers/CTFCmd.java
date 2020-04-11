package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;

public class CTFCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"ctfjoin",
		"ctfleave",
		"ctfinfo"
	};
	
	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.startsWith("ctfjoin"))
		{
			JoinCTF(activeChar);
		}
		else if (command.startsWith("ctfleave"))
		{
			LeaveCTF(activeChar);
		}
		
		else if (command.startsWith("ctfinfo"))
		{
			CTFinfo(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
	public boolean JoinCTF(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!CTF.isJoining())
		{
			activeChar.sendMessage("There is no CTF Event in progress.");
			return false;
		}
		else if (CTF.isJoining() && activeChar.inEventCTF)
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
		else if (activeChar.getLevel() < CTF.getMinLvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too low.");
			return false;
		}
		else if (activeChar.getLevel() > CTF.getMaxLvl())
		{
			activeChar.sendMessage("You are not allowed to participate to the event because your level is too high.");
			return false;
		}
		else if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You are not allowed to participate to the event because you have Karma.");
			return false;
		}
		else if (CTF.isTeleport() || CTF.isStarted())
		{
			activeChar.sendMessage("CTF Event registration period is over. You can't register now.");
			return false;
		}
		else
		{
			activeChar.sendMessage("Your participation in the CTF event has been approved.");
			CTF.addPlayer(activeChar, "");
			return true;
		}
	}
	
	public boolean LeaveCTF(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!CTF.isJoining())
		{
			activeChar.sendMessage("There is no CTF Event in progress.");
			return false;
		}
		else if ((CTF.isTeleport() || CTF.isStarted()) && activeChar.inEventCTF)
		{
			activeChar.sendMessage("You can not leave now because CTF event has started.");
			return false;
		}
		else if (CTF.isJoining() && !activeChar.inEventCTF)
		{
			activeChar.sendMessage("You aren't registered in the CTF Event.");
			return false;
		}
		else
		{
			CTF.removePlayer(activeChar);
			return true;
		}
	}
	
	public boolean CTFinfo(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!CTF.isJoining())
		{
			activeChar.sendMessage("There is no CTF Event in progress.");
			return false;
		}
		else if (CTF.isTeleport() || CTF.isStarted())
		{
			activeChar.sendMessage("I can't provide you this info. Command available only in joining period.");
			return false;
		}
		else
		{
			if (CTF.playersShuffle.size() == 1)
			{
				activeChar.sendMessage("There is " + CTF.playersShuffle.size() + " player participating in this event.");
				activeChar.sendMessage("Reward: " + CTF.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(CTF.get_rewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + CTF.getMinLvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + CTF.getMaxLvl() + ".");
			}
			else
			{
				activeChar.sendMessage("There are " + CTF.playersShuffle.size() + " players participating in this event.");
				activeChar.sendMessage("Reward: " + CTF.getRewardAmount() + " " + ItemTable.getInstance().getTemplate(CTF.get_rewardId()).getName() + " !");
				activeChar.sendMessage("Player Min lvl: " + CTF.getMinLvl() + ".");
				activeChar.sendMessage("Player Max lvl: " + CTF.getMaxLvl() + ".");
			}
			return true;
		}
	}
}