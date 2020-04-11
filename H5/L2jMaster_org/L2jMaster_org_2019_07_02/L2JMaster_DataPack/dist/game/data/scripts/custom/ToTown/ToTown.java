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
package custom.ToTown;

import com.l2jserver.Config;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class ToTown implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"giran",
		"aden",
		"oren",
		"dion",
		"goddard",
		"floran",
		"gludin",
		"gludio",
		"rune",
		"heine",
		"dwarvenvillage",
		"darkelvenvillage",
		"elvenvillage",
		"orcvillage",
		"talkingisland",
		"schuttgart",
		"huntersvillage",
		"antharas",
		"valakas",
		"baium",
		"queenant",
		"zaken",
		"orfen",
		"core"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (!Config.TELEPORT_TO_TOWN_COMMNAND)
		{
			activeChar.sendMessage("This command is disabled!");
			return false;
		}
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("Can not use this acction at event!");
			return false;
		}
		else if (activeChar.isJailed())
		{
			activeChar.sendMessage("Can not use this acction at Jail");
			return false;
		}
		else if (activeChar.isDead())
		{
			activeChar.sendMessage("Can not use this acction Dead.");
			return false;
		}
		else if (activeChar.isInCombat())
		{
			activeChar.sendMessage("You can't teleport while you are in combat.");
			return false;
		}
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You can't teleport while you are doing a duel.");
			return false;
		}
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You can't teleport while you are in olympiad");
			return false;
		}
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can't teleport while you are in observer mode");
			return false;
		}
		else if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("While you are holding a Cursed Weapon you can't go to your leader!");
			return false;
		}
		else if ((Config.TELEPORT_TO_TOWN_COMMNAND) && (!activeChar.inObserverMode() && !activeChar.isInOlympiadMode() && !activeChar.isInDuel() && !activeChar.isInCombat() && !activeChar.isDead() && !activeChar.isJailed()))
		{
			if (command.startsWith("giran"))
			{
				activeChar.teleToLocation(82337, 148602, -3467);
			}
			else if (command.startsWith("dion"))
			{
				activeChar.teleToLocation(18492, 145386, -3118);
			}
			else if (command.startsWith("oren"))
			{
				activeChar.teleToLocation(82769, 53573, -1498);
			}
			else if (command.startsWith("gludio"))
			{
				activeChar.teleToLocation(-12864, 122716, -3117);
			}
			else if (command.startsWith("gludin"))
			{
				activeChar.teleToLocation(-80928, 150055, -3044);
			}
			else if (command.startsWith("aden"))
			{
				activeChar.teleToLocation(147361, 26953, -2205);
			}
			else if (command.startsWith("schuttgart"))
			{
				activeChar.teleToLocation(87359, -143224, -1293);
			}
			else if (command.startsWith("orcvillage"))
			{
				activeChar.teleToLocation(-44429, -113596, -220);
			}
			else if (command.startsWith("darkelvenvillage"))
			{
				activeChar.teleToLocation(11620, 16780, -4662);
			}
			else if (command.startsWith("elvenvillage"))
			{
				activeChar.teleToLocation(47050, 50767, -2996);
			}
			else if (command.startsWith("dwarvenvillage"))
			{
				activeChar.teleToLocation(115526, -178660, -945);
			}
			else if (command.startsWith("heine"))
			{
				activeChar.teleToLocation(111396, 219254, -3546);
			}
			else if (command.startsWith("huntersvillage"))
			{
				activeChar.teleToLocation(116440, 76320, -2730);
			}
			else if (command.startsWith("floran"))
			{
				activeChar.teleToLocation(17144, 170156, -3502);
			}
			else if (command.startsWith("goddard"))
			{
				activeChar.teleToLocation(147720, -55560, -2735);
			}
			else if (command.startsWith("rune"))
			{
				activeChar.teleToLocation(43848, -48033, -797);
			}
			else if (command.startsWith("valakas"))
			{
				activeChar.teleToLocation(183656, -114856, -3336);
			}
			else if (command.startsWith("baium"))
			{
				activeChar.teleToLocation(112638, 14112, 10078);
			}
			else if (command.startsWith("queenant"))
			{
				activeChar.teleToLocation(-21919, 184305, -5721);
			}
			else if (command.startsWith("zaken"))
			{
				activeChar.teleToLocation(52167, 219121, -3230);
			}
			else if (command.startsWith("orfen"))
			{
				activeChar.teleToLocation(57783, 18069, -5510);
			}
			else if (command.startsWith("core"))
			{
				activeChar.teleToLocation(17538, 115379, -6583);
			}
			else if (command.startsWith("antharas"))
			{
				activeChar.teleToLocation(154533, 121084, -3808);
			}
			else
			{
				activeChar.sendMessage("Only Premium Characters can use this command. Please contact a GM for more information!");
			}
			return false;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}