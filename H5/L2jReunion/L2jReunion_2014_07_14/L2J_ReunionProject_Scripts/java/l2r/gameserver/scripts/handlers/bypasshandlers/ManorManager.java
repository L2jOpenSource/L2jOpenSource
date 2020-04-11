/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package l2r.gameserver.scripts.handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2r.gameserver.model.actor.instance.L2MerchantInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.BuyListSeed;
import l2r.gameserver.network.serverpackets.ExShowCropInfo;
import l2r.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import l2r.gameserver.network.serverpackets.ExShowProcureCropDetail;
import l2r.gameserver.network.serverpackets.ExShowSeedInfo;
import l2r.gameserver.network.serverpackets.ExShowSellCropList;
import l2r.gameserver.network.serverpackets.SystemMessage;

public class ManorManager implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"manor_menu_select"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		final L2Npc manager = activeChar.getLastFolkNPC();
		if (!((manager instanceof L2ManorManagerInstance)))
		{
			return false;
		}
		
		if (!activeChar.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, true, false))
		{
			return false;
		}
		
		try
		{
			final Castle castle = manager.getCastle();
			if (CastleManorManager.getInstance().isUnderMaintenance())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				return true;
			}
			
			final StringTokenizer st = new StringTokenizer(command, "&");
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final int time = Integer.parseInt(st.nextToken().split("=")[1]);
			
			final int castleId;
			if (state < 0)
			{
				castleId = castle.getResidenceId(); // info for current manor
			}
			else
			{
				castleId = state; // info for requested manor
			}
			
			switch (ask)
			{
				case 1: // Seed purchase
					if (castleId != castle.getResidenceId())
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
						sm.addString(manager.getCastle().getName());
						activeChar.sendPacket(sm);
					}
					else
					{
						activeChar.sendPacket(new BuyListSeed(activeChar.getAdena(), castleId, castle.getSeedProduction(CastleManorManager.PERIOD_CURRENT)));
					}
					break;
				case 2: // Crop sales
					activeChar.sendPacket(new ExShowSellCropList(activeChar, castleId, castle.getCropProcure(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 3: // Current seeds (Manor info)
					if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						activeChar.sendPacket(new ExShowSeedInfo(castleId, null));
					}
					else
					{
						activeChar.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					}
					break;
				case 4: // Current crops (Manor info)
					if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						activeChar.sendPacket(new ExShowCropInfo(castleId, null));
					}
					else
					{
						activeChar.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					}
					break;
				case 5: // Basic info (Manor info)
					activeChar.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6: // Buy harvester
					((L2MerchantInstance) manager).showBuyWindow(activeChar, 300000 + manager.getId());
					break;
				case 9: // Edit sales (Crop sales)
					activeChar.sendPacket(new ExShowProcureCropDetail(state));
					break;
				default:
					return false;
			}
			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
