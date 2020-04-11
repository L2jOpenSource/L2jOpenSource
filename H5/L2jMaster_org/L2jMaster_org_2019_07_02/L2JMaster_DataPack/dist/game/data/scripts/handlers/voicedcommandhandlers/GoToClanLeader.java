/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.L2Event;

/**
 * Voice Command - Go To Clan Leader (.cl)
 * @author swarlog
 */

public class GoToClanLeader implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"cl"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (!Config.ALLOW_CL_COMMAND)
		{
			activeChar.sendMessage("This command is disabled!");
			return false;
		}
		if (command.equals("cl"))
		{
			if (activeChar.getClan() == null)
			{
				return false;
			}
			
			L2PcInstance leader;
			leader = (L2PcInstance) L2World.getInstance().findObject(activeChar.getClan().getLeaderId());
			
			if (leader == null)
			{
				activeChar.sendMessage("Your partner is not online.");
				return false;
			}
			else if (GrandBossManager.getInstance().getZone(leader) != null)
			{
				activeChar.sendMessage("You are inside a Boss Zone.");
				return false;
			}
			else if (leader.isCombatFlagEquipped())
			{
				activeChar.sendMessage("While your leader are holding a Combat Flag or Territory Ward you can't go to your leader!");
				return false;
			}
			else if (leader.isCursedWeaponEquipped())
			{
				activeChar.sendMessage("While your leader are holding a Cursed Weapon you can't go to your leader!");
				return false;
			}
			else if (L2Event.isParticipant(leader))
			{
				activeChar.sendMessage("You are in an event.");
				return false;
			}
			else if (leader.isJailed())
			{
				activeChar.sendMessage("Your leader is in Jail.");
				return false;
			}
			else if (leader.isInOlympiadMode())
			{
				activeChar.sendMessage("Your leader is in the Olympiad now.");
				return false;
			}
			else if (leader.isInDuel())
			{
				activeChar.sendMessage("Your leader is in a duel.");
				return false;
			}
			else if (leader.isFestivalParticipant())
			{
				activeChar.sendMessage("Your leader is in a festival.");
				return false;
			}
			else if (leader.isInParty() && leader.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage("Your leader is in dimensional rift.");
				return false;
			}
			else if (leader.inObserverMode())
			{
				activeChar.sendMessage("Your leader is in the observation.");
			}
			else if ((leader.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(leader.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(leader.getClan()).getSiege().isInProgress())
			{
				activeChar.sendMessage("Your leader is in siege, you can't go to your leader.");
				return false;
			}
			
			else if (GrandBossManager.getInstance().getZone(activeChar) != null)
			{
				activeChar.sendMessage("You are inside a Boss Zone.");
				return false;
			}
			else if (activeChar.isCombatFlagEquipped())
			{
				activeChar.sendMessage("While you are holding a Combat Flag or Territory Ward you can't go to your leader!");
				return false;
			}
			else if (activeChar.isInCombat())
			{
				activeChar.sendMessage("You can't teleport while you are in combat.");
				return false;
			}
			else if (activeChar.isCursedWeaponEquipped())
			{
				activeChar.sendMessage("While you are holding a Cursed Weapon you can't go to your leader!");
				return false;
			}
			else if (L2Event.isParticipant(activeChar))
			{
				activeChar.sendMessage("You are in an event.");
				return false;
			}
			else if (activeChar.isJailed())
			{
				activeChar.sendMessage("You are in Jail!");
				return false;
			}
			else if (activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("You are in the Olympiad now.");
				return false;
			}
			else if (activeChar.isInDuel())
			{
				activeChar.sendMessage("You are in a duel!");
				return false;
			}
			else if (activeChar.inObserverMode())
			{
				activeChar.sendMessage("You are in the observation.");
			}
			else if ((activeChar.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null) && CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().isInProgress())
			{
				activeChar.sendMessage("You are in siege, you can't go to your leader.");
				return false;
			}
			else if (activeChar.isFestivalParticipant())
			{
				activeChar.sendMessage("You are in a festival.");
				return false;
			}
			else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
			{
				activeChar.sendMessage("You are in the dimensional rift.");
				return false;
			}
			else if (activeChar == leader)
			{
				activeChar.sendMessage("You cannot teleport to yourself.");
				return false;
			}
			if (activeChar.getInventory().getItemByItemId(6392) == null)
			{
				activeChar.sendMessage("You need 100 Event Medals to use the cl-teleport system.");
				return false;
			}
			
			int leaderx;
			int leadery;
			int leaderz;
			
			leaderx = leader.getX();
			leadery = leader.getY();
			leaderz = leader.getZ();
			
			activeChar.teleToLocation(leaderx, leadery, leaderz);
			activeChar.sendMessage("You have been teleported to your leader!");
			activeChar.getInventory().destroyItemByItemId("RessSystem", 6392, 100, activeChar, activeChar.getTarget());
			activeChar.sendMessage("100 Event Medals has dissapeared! Thank you!");
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}