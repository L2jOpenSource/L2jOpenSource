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

import java.util.StringTokenizer;

import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Vice, Zoey76
 */
public class L2FortLogisticsInstance extends L2MerchantInstance
{
	private static final int[] SUPPLY_BOX_IDS =
	{
		35665,
		35697,
		35734,
		35766,
		35803,
		35834,
		35866,
		35903,
		35935,
		35973,
		36010,
		36042,
		36080,
		36117,
		36148,
		36180,
		36218,
		36256,
		36293,
		36325,
		36363
	};
	
	public L2FortLogisticsInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2FortLogisticsInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
		{
			return;
		}
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (actualCommand.equalsIgnoreCase("rewards"))
		{
			if (isMyLord(player))
			{
				html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-rewards.htm");
				html.replace("%bloodoath%", String.valueOf(player.getClan().getBloodOathCount()));
			}
			else
			{
				html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-noprivs.htm");
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("blood"))
		{
			if (isMyLord(player))
			{
				final int blood = player.getClan().getBloodOathCount();
				if (blood > 0)
				{
					html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-blood.htm");
					player.addItem("Quest", 9910, blood, this, true);
					player.getClan().resetBloodOathCount();
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-noblood.htm");
				}
			}
			else
			{
				html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-noprivs.htm");
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("supplylvl"))
		{
			if ((player.getClan() != null) && (getFort().getOwnerClan() != null) && (player.getClan() == getFort().getOwnerClan()) && (getFort().getFortState() == 2))
			{
				if (player.isClanLeader())
				{
					html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-supplylvl.htm");
					html.replace("%supplylvl%", String.valueOf(getFort().getSupplyLvL()));
				}
				else
				{
					html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-noprivs.htm");
				}
			}
			else
			{
				html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-1.htm");
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("supply"))
		{
			if (isMyLord(player))
			{
				if (getFort().getSiege().isInProgress())
				{
					html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-siege.htm");
				}
				else
				{
					int level = getFort().getSupplyLvL();
					if (level > 0)
					{
						html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-supply.htm");
						// spawn box
						L2NpcTemplate BoxTemplate = NpcTable.getInstance().getTemplate(SUPPLY_BOX_IDS[level - 1]);
						L2MonsterInstance box = new L2MonsterInstance(IdFactory.getInstance().getNextId(), BoxTemplate);
						box.setCurrentHp(box.getMaxHp());
						box.setCurrentMp(box.getMaxMp());
						box.setHeading(0);
						box.spawnMe(getX() - 23, getY() + 41, getZ());
						
						getFort().setSupplyLvL(0);
						getFort().saveFortVariables();
					}
					else
					{
						html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-nosupply.htm");
					}
				}
			}
			else
			{
				html.setFile(player.getHtmlPrefix(), "data/html/fortress/logistics-noprivs.htm");
			}
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		showMessageWindow(player, 0);
	}
	
	private void showMessageWindow(L2PcInstance player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename;
		
		if (val == 0)
		{
			filename = "data/html/fortress/logistics.htm";
		}
		else
		{
			filename = "data/html/fortress/logistics-" + val + ".htm";
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getId()));
		if (getFort().getOwnerClan() != null)
		{
			html.replace("%clanname%", getFort().getOwnerClan().getName());
		}
		else
		{
			html.replace("%clanname%", "NPC");
		}
		player.sendPacket(html);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "logistics";
		}
		else
		{
			pom = "logistics-" + val;
		}
		
		return "data/html/fortress/" + pom + ".htm";
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}