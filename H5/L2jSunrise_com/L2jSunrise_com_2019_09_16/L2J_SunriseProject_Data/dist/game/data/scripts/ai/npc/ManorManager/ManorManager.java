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
package ai.npc.ManorManager;

import l2r.Config;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MerchantInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.ListenerRegisterType;
import l2r.gameserver.model.events.annotations.Id;
import l2r.gameserver.model.events.annotations.RegisterEvent;
import l2r.gameserver.model.events.annotations.RegisterType;
import l2r.gameserver.model.events.impl.character.npc.OnNpcManorBypass;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.BuyListSeed;
import l2r.gameserver.network.serverpackets.ExShowCropInfo;
import l2r.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import l2r.gameserver.network.serverpackets.ExShowProcureCropDetail;
import l2r.gameserver.network.serverpackets.ExShowSeedInfo;
import l2r.gameserver.network.serverpackets.ExShowSellCropList;
import l2r.gameserver.network.serverpackets.SystemMessage;

import ai.npc.AbstractNpcAI;

/**
 * Manor manager AI.
 * @author malyelfik
 */
public final class ManorManager extends AbstractNpcAI
{
	private static final int[] NPC =
	{
		35644,
		35645,
		35319,
		35366,
		36456,
		35512,
		35558,
		35229,
		35230,
		35231,
		35277,
		35103,
		35145,
		35187
	};
	
	public ManorManager()
	{
		super(ManorManager.class.getSimpleName(), "ai/npc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "manager-help-01.htm":
			case "manager-help-02.htm":
			case "manager-help-03.htm":
				htmltext = event;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (Config.ALLOW_MANOR)
		{
			if (!player.canOverrideCond(PcCondOverride.CASTLE_CONDITIONS) && (npc.getCastle() != null) && (npc.getCastle().getResidenceId() > 0) && player.isClanLeader() && (npc.getCastle().getOwnerId() == player.getClanId()))
			{
				return "manager-lord.htm";
			}
			return "manager.htm";
		}
		return getHtm(player, player.getHtmlPrefix(), "data/html/npcdefault.htm");
	}
	
	// @formatter:off
	@RegisterEvent(EventType.ON_NPC_MANOR_BYPASS)
	@RegisterType(ListenerRegisterType.NPC)
	@Id({35644, 35645, 35319, 35366, 36456, 35512, 35558, 35229, 35230, 35231, 35277, 35103, 35145, 35187})
	// @formatter:on
	public final void onNpcManorBypass(OnNpcManorBypass evt)
	{
		final L2PcInstance player = evt.getActiveChar();
		if (CastleManorManager.getInstance().isUnderMaintenance())
		{
			player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
			return;
		}
		
		final L2Npc npc = evt.getTarget();
		final int templateId = npc.getCastle().getResidenceId();
		final int castleId = (evt.getManorId() == -1) ? templateId : evt.getManorId();
		switch (evt.getRequest())
		{
			case 1: // Seed purchase
			{
				if (templateId != castleId)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addCastleId(templateId));
					return;
				}
				player.sendPacket(new BuyListSeed(player.getAdena(), castleId));
				break;
			}
			case 2: // Crop sales
				player.sendPacket(new ExShowSellCropList(player.getInventory(), castleId));
				break;
			case 3: // Seed info
				player.sendPacket(new ExShowSeedInfo(castleId, evt.isNextPeriod(), false));
				break;
			case 4: // Crop info
				player.sendPacket(new ExShowCropInfo(castleId, evt.isNextPeriod(), false));
				break;
			case 5: // Basic info
				player.sendPacket(new ExShowManorDefaultInfo(false));
				break;
			case 6: // Buy harvester
				((L2MerchantInstance) npc).showBuyWindow(player, 300000 + npc.getId());
				break;
			case 9: // Edit sales (Crop sales)
				player.sendPacket(new ExShowProcureCropDetail(evt.getManorId()));
				break;
			default:
				_log.warn(getClass().getSimpleName() + ": Player " + player.getName() + " (" + player.getObjectId() + ") send unknown request id " + evt.getRequest() + "!");
		}
	}
}
