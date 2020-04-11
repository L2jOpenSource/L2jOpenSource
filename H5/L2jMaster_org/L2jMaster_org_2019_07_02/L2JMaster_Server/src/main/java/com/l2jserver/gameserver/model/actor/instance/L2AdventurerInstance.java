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
package com.l2jserver.gameserver.model.actor.instance;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author LBaldi
 */
public class L2AdventurerInstance extends L2NpcInstance
{
	public L2AdventurerInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2AdventurerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
			
			if (Config.PC_BANG_ENABLED)
			{
				return "data/html/adventurer_guildsman/" + pom + "-pcpoint.htm";
			}
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/adventurer_guildsman/" + pom + ".htm";
	}
}