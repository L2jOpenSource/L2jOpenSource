/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author JosePC
 */
public class PlayerStats implements IVoicedCommandHandler
{
	
	private static final String[] _voicedCommands =
	{
		"stats"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("stats"))
		{
			if ((activeChar != null) && (activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				NpcHtmlMessage html = new NpcHtmlMessage();
				html.setFile(activeChar.getHtmlPrefix(), "data/html/mods/playerStats.htm");
				html.replace("%Patk%", activeChar.getTarget().getActingPlayer().getPAtk(null));
				html.replace("%Pdef%", activeChar.getTarget().getActingPlayer().getPDef(null));
				html.replace("%Matk%", activeChar.getTarget().getActingPlayer().getMAtk(null, null));
				html.replace("%Mdef%", activeChar.getTarget().getActingPlayer().getMDef(null, null));
				html.replace("%Acc%", activeChar.getTarget().getActingPlayer().getAccuracy());
				html.replace("%Eva%", activeChar.getTarget().getActingPlayer().getEvasionRate(null));
				html.replace("%CritHit%", activeChar.getTarget().getActingPlayer().getCriticalHit(null, null));
				html.replace("%CritDamage%", activeChar.getTarget().getActingPlayer().getCriticalDmg(null, 0));
				html.replace("%PatkSpeed%", activeChar.getTarget().getActingPlayer().getPAtkSpd());
				html.replace("%MatkSpeed%", activeChar.getTarget().getActingPlayer().getMAtkSpd());
				activeChar.sendPacket(html);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}