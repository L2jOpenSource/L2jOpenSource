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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.Config;
import l2r.gameserver.datatables.xml.UIData;
import l2r.gameserver.model.ActionKey;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient.GameClientState;

/**
 * Request Save Key Mapping client packet.
 * @author mrTJO, Zoey76
 */
public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private static String _C__D0_22_REQUESTSAVEKEYMAPPING = "[C] D0:22 RequestSaveKeyMapping";
	
	private int _tabNum;
	private final Map<Integer, List<ActionKey>> _keyMap = new HashMap<>();
	private final Map<Integer, List<Integer>> _catMap = new HashMap<>();
	
	@Override
	protected void readImpl()
	{
		int category = 0;
		
		readD(); // Unknown
		readD(); // Unknown
		_tabNum = readD();
		for (int i = 0; i < _tabNum; i++)
		{
			int cmd1Size = readC();
			for (int j = 0; j < cmd1Size; j++)
			{
				UIData.addCategory(_catMap, category, readC());
			}
			category++;
			
			int cmd2Size = readC();
			for (int j = 0; j < cmd2Size; j++)
			{
				UIData.addCategory(_catMap, category, readC());
			}
			category++;
			
			int cmdSize = readD();
			for (int j = 0; j < cmdSize; j++)
			{
				int cmd = readD();
				int key = readD();
				int tgKey1 = readD();
				int tgKey2 = readD();
				int show = readD();
				UIData.addKey(_keyMap, i, new ActionKey(i, cmd, key, tgKey1, tgKey2, show));
			}
		}
		readD();
		readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (!Config.STORE_UI_SETTINGS || (player == null) || (getClient().getState() != GameClientState.IN_GAME))
		{
			return;
		}
		player.getUISettings().storeAll(_catMap, _keyMap);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_22_REQUESTSAVEKEYMAPPING;
	}
}
