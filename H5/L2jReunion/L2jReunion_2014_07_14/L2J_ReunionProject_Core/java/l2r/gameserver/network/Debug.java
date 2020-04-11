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
package l2r.gameserver.network;

import java.util.Map.Entry;

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * @author UnAfraid
 */
public class Debug
{
	public static void sendSkillDebug(L2Character attacker, L2Character target, L2Skill skill, StatsSet set)
	{
		if (!attacker.isPlayer())
		{
			return;
		}
		
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : set.getSet().entrySet())
		{
			sb.append("<tr><td>" + entry.getKey() + "</td><td><font color=\"LEVEL\">" + entry.getValue() + "</font></td></tr>");
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(attacker.getActingPlayer().getHtmlPrefix(), "data/html/admin/skilldebug.htm");
		msg.replace("%patk%", target.getPAtk(target));
		msg.replace("%matk%", target.getMAtk(target, skill));
		msg.replace("%pdef%", target.getPDef(target));
		msg.replace("%mdef%", target.getMDef(target, skill));
		msg.replace("%acc%", target.getAccuracy());
		msg.replace("%evas%", target.getEvasionRate(target));
		msg.replace("%crit%", target.getCriticalHit(target, skill));
		msg.replace("%speed%", target.getRunSpeed());
		msg.replace("%pAtkSpd%", target.getPAtkSpd());
		msg.replace("%mAtkSpd%", target.getMAtkSpd());
		msg.replace("%str%", target.getSTR());
		msg.replace("%dex%", target.getDEX());
		msg.replace("%con%", target.getCON());
		msg.replace("%int%", target.getINT());
		msg.replace("%wit%", target.getWIT());
		msg.replace("%men%", target.getMEN());
		msg.replace("%atkElemType%", Elementals.getElementName(target.getAttackElement()));
		msg.replace("%atkElemVal%", target.getAttackElementValue(target.getAttackElement()));
		msg.replace("%fireDef%", target.getDefenseElementValue((byte) 0));
		msg.replace("%waterDef%", target.getDefenseElementValue((byte) 1));
		msg.replace("%windDef%", target.getDefenseElementValue((byte) 2));
		msg.replace("%earthDef%", target.getDefenseElementValue((byte) 3));
		msg.replace("%holyDef%", target.getDefenseElementValue((byte) 4));
		msg.replace("%darkDef%", target.getDefenseElementValue((byte) 5));
		msg.replace("%skill%", skill.toString());
		msg.replace("%details%", sb.toString());
		attacker.sendPacket(new TutorialShowHtml(msg.getHtml()));
	}
	
	public static void sendItemDebug(L2PcInstance player, L2ItemInstance item, StatsSet set)
	{
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : set.getSet().entrySet())
		{
			sb.append("<tr><td>" + entry.getKey() + "</td><td><font color=\"LEVEL\">" + entry.getValue() + "</font></td></tr>");
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(player.getHtmlPrefix(), "data/html/admin/itemdebug.htm");
		msg.replace("%itemName%", item.getName());
		msg.replace("%itemSlot%", getBodyPart(item.getItem().getBodyPart()));
		msg.replace("%itemType%", item.isArmor() ? "Armor" : item.isWeapon() ? "Weapon" : "Etc");
		msg.replace("%enchantLevel%", item.getEnchantLevel());
		msg.replace("%isMagicWeapon%", item.getItem().isMagicWeapon());
		msg.replace("%item%", item.toString());
		msg.replace("%details%", sb.toString());
		player.sendPacket(new TutorialShowHtml(msg.getHtml()));
	}
	
	private static String getBodyPart(int bodyPart)
	{
		for (Entry<String, Integer> entry : ItemData._slots.entrySet())
		{
			if ((entry.getValue() & bodyPart) == bodyPart)
			{
				return entry.getKey();
			}
		}
		return "Unknown";
	}
}
