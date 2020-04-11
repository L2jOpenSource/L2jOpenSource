/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package custom.AutoAdenaToGoldBar;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.character.player.inventory.OnPlayerItemAdd;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public class AutoAdenaToGoldBar extends AbstractNpcAI
{
	private final int ADENA_ID = 57;
	private final int GOLD_BAR_ID = 3470;
	private final long ADENA_COUNT_TO_GOLD_BAR = 1000000000; // this cannot be less or equal to 100000000
	
	public AutoAdenaToGoldBar()
	{
		super(AutoAdenaToGoldBar.class.getSimpleName(), "custom");
		setOnEnterWorld(true);
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_ITEM_ADD, (OnPlayerItemAdd event) -> OnPlayerItemAdd(event), this));
		return null;
	}
	
	private Object OnPlayerItemAdd(OnPlayerItemAdd event)
	{
		L2ItemInstance item = event.getItem();
		if (item.getId() == ADENA_ID)
		{
			L2PcInstance player = event.getActiveChar();
			if (player.getAdena() > ADENA_COUNT_TO_GOLD_BAR)
			{
				player.reduceAdena("goldbar", ADENA_COUNT_TO_GOLD_BAR - 100000000, player, true);
				player.addItem("goldbar", GOLD_BAR_ID, 1, player, true);
			}
		}
		return null;
	}
}