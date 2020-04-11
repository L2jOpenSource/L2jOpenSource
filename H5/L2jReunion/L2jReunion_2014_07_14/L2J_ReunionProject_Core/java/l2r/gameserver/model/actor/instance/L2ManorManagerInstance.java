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
package l2r.gameserver.model.actor.instance;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;

public class L2ManorManagerInstance extends L2MerchantInstance
{
	public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ManorManagerInstance);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/manormanager/manager.htm";
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		if (CastleManorManager.getInstance().isDisabled())
		{
			showChatWindow(player, "data/html/npcdefault.htm");
			return;
		}
		
		if (!player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) && (getCastle() != null) && (getCastle().getResidenceId() > 0) && player.isClanLeader() && (getCastle().getOwnerId() == player.getClanId()))
		{
			showChatWindow(player, "data/html/manormanager/manager-lord.htm");
		}
		else
		{
			showChatWindow(player, "data/html/manormanager/manager.htm");
		}
	}
}