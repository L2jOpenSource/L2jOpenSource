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

import l2r.Config;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.serverpackets.ExRpItemLink;

/**
 * @author KenM
 */
public class RequestExRqItemLink extends L2GameClientPacket
{
	private static String _C__D0_1E_REQUESTEXRQITEMLINK = "[C] D0:1E RequestExRqItemLink";
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if (client != null)
		{
			L2Object object = L2World.getInstance().findObject(_objectId);
			if (object instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) object;
				if (item.isPublished())
				{
					client.sendPacket(new ExRpItemLink(item));
				}
				else
				{
					if (Config.DEBUG)
					{
						_log.info(getClient() + " requested item link for item which wasnt published! ID:" + _objectId);
					}
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1E_REQUESTEXRQITEMLINK;
	}
}
