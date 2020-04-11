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
 package handlers.bypasshandlers;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author fissban
 */
public class AntiBot implements IBypassHandler
{
	@Override
	public String[] getBypassList()
	{
		return new String[]
		{
			"antibot"
		};
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (command.startsWith("antibot"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();// bypass
			
			if (st.hasMoreTokens())
			{
				activeChar.checkCode(st.nextToken());
				return true;
			}
			activeChar.checkCode("FAIL");
		}
		return false;
	}
}