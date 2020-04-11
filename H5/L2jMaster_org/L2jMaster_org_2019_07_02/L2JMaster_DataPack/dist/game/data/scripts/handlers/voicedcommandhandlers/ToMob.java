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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * @author JoseM
 */
public class ToMob implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"mob"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (!Config.TELEPORT_TO_MOB_COMMAND)
		{
			activeChar.sendMessage("This command is disabled!");
			return false;
		}
		
		if (command.equalsIgnoreCase("mob"))
		{
			if (target == null)
			{
				activeChar.sendMessage("Usage: .mob <name>");
			}
			else
			{
				String name = null;
				try
				{
					name = target;
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("You must enter a name. Usage: .mob <name>");
					return false;
				}
				
				if (name == "")
				{
					return false;
				}
				
				List<L2NpcTemplate> mobs = NpcData.getInstance().getAllNpcOfClassType("L2Monster");
				List<Integer> xyz = new ArrayList<>();
				
				for (L2NpcTemplate mob : mobs)
				{
					if (mob.getName().equalsIgnoreCase(name))
					{
						final String SELECT_SPAWNS = "SELECT locx, locy, locz FROM spawnlist WHERE npc_templateid = " + mob.getId();
						try (Connection con = ConnectionFactory.getInstance().getConnection();
							Statement s = con.createStatement();
							ResultSet rs = s.executeQuery(SELECT_SPAWNS))
						{
							while (rs.next())
							{
								xyz.add(rs.getInt("locx"));
								xyz.add(rs.getInt("locy"));
								xyz.add(rs.getInt("locz"));
							}
						}
						catch (Exception e)
						{
							System.out.println(" === Error reading locs === ");
						}
					}
				}
				if (xyz.isEmpty())
				{
					activeChar.sendMessage("Any monster found");
					return false;
				}
				Random rnd = new Random();
				int mobRandomPos = (rnd.nextInt(xyz.size())) + 0;
				int posX;
				int posY;
				int posZ;
				
				if ((mobRandomPos % 3) == 0)
				{
					posX = xyz.get(mobRandomPos);
					posY = xyz.get(mobRandomPos + 1);
					posZ = xyz.get(mobRandomPos + 2);
				}
				else
				{
					posX = xyz.get(0);
					posY = xyz.get(1);
					posZ = xyz.get(2);
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
				
				activeChar.teleToLocation(posX, posY, posZ);
				activeChar.sendMessage("Teleported to " + name + "'s Zone");
			}
			
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}