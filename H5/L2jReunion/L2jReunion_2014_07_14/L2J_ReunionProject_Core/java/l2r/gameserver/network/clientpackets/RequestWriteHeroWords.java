/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Hero;

/**
 * Format chS c (id) 0xD0 h (subid) 0x0C S the hero's words :)
 * @author -Wooden-
 */
public final class RequestWriteHeroWords extends L2GameClientPacket
{
	private static final String _C__D0_05_REQUESTWRITEHEROWORDS = "[C] D0:05 RequestWriteHeroWords";
	
	private String _heroWords;
	
	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if ((player == null) || !player.isHero())
		{
			return;
		}
		
		if ((_heroWords == null) || (_heroWords.length() > 300))
		{
			return;
		}
		
		Hero.getInstance().setHeroMessage(player, _heroWords);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_05_REQUESTWRITEHEROWORDS;
	}
}