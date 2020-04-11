/*
 * Copyright (C) 2004-2017 L2J DataPack
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
package custom.NewbieCoupons;

import l2r.gameserver.data.xml.impl.MultisellData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.variables.PlayerVariables;
import l2r.gameserver.network.NpcStringId;

import ai.npc.AbstractNpcAI;

/**
 * Newbie Weapon/Accesories Coupons for the Hellbound opening event.<br>
 * Original Jython script by Vice.
 * @author Nyaran
 */
public class NewbieCoupons extends AbstractNpcAI
{
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;
	
	private static final int[] NPCs =
	{
		30598,
		30599,
		30600,
		30601,
		30602,
		31076,
		31077,
		32135
	};
	
	private static final int WEAPON_MULTISELL = 305986001;
	private static final int ACCESORIES_MULTISELL = 305986002;
	
	// enable/disable coupon give
	private static final boolean NEWBIE_COUPONS_ENABLED = true;
	
	private static final int NEWBIE_WEAPON = 16;
	private static final int NEWBIE_ACCESORY = 32;
	
	public NewbieCoupons()
	{
		super(NewbieCoupons.class.getSimpleName(), "custom");
		addStartNpc(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (!NEWBIE_COUPONS_ENABLED)
		{
			return htmltext;
		}
		
		final QuestState st = getQuestState(player, true);
		int newbie = player.getNewbie();
		int level = player.getLevel();
		int occupation_level = player.getClassId().level();
		int pkkills = player.getPkKills();
		if (event.equals("newbie_give_weapon_coupon"))
		{
			if ((level >= 6) && (level <= 39) && (pkkills == 0) && (occupation_level == 0))
			{
				// check the player state against this quest newbie rewarding mark.
				if ((newbie | NEWBIE_WEAPON) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_WEAPON);
					st.giveItems(COUPON_ONE, 5);
					showOnScreenMsg(player, NpcStringId.ACQUISITION_OF_WEAPON_EXCHANGE_COUPON_FOR_BEGINNERS_COMPLETE_N_GO_SPEAK_WITH_THE_NEWBIE_GUIDE, 2, 5000, "");
					htmltext = "30598-2.htm"; // here's the coupon you requested
				}
				else
				{
					htmltext = "30598-1.htm"; // you got a coupon already!
				}
			}
			else
			{
				htmltext = "30598-3.htm"; // you're not eligible to get a coupon (level caps, pkkills or already changed class)
			}
		}
		else if (event.equals("newbie_give_armor_coupon"))
		{
			if ((level >= 6) && (level <= 39) && (pkkills == 0) && (occupation_level == 1))
			{
				// check the player state against this quest newbie rewarding mark.
				if ((newbie | NEWBIE_ACCESORY) != newbie)
				{
					player.setNewbie(newbie | NEWBIE_ACCESORY);
					st.giveItems(COUPON_TWO, 1);
					htmltext = "30598-5.htm"; // here's the coupon you requested
				}
				else
				{
					htmltext = "30598-4.htm"; // you got a coupon already!
				}
			}
			else
			{
				htmltext = "30598-6.htm"; // you're not eligible to get a coupon (level caps, pkkills or didnt change class yet)
			}
		}
		else if (event.equals("newbie_show_weapon"))
		{
			if ((level >= 6) && (level <= 39) && (pkkills == 0) && (occupation_level == 0))
			{
				MultisellData.getInstance().separateAndSend(WEAPON_MULTISELL, player, npc, false);
				return null;
			}
			htmltext = "30598-7.htm"; // you're not eligible to use warehouse
		}
		else if (event.equals("newbie_show_armor"))
		{
			if ((level >= 6) && (level <= 39) && (pkkills == 0) && (occupation_level > 0))
			{
				MultisellData.getInstance().separateAndSend(ACCESORIES_MULTISELL, player, npc, false);
				return null;
			}
			htmltext = "30598-8.htm"; // you're not eligible to use warehouse
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		
		final PlayerVariables vars = player.getVariables();
		
		if (vars.getBoolean("NEWBIE_LEVEL", true) && vars.getBoolean("NEWBIE_SHOTS", false))
		{
			if (player.getLevel() >= 9)
			{
				qs.giveAdena(5563, true);
				qs.addExpAndSp(16851, 711);
			}
			else if (player.getLevel() >= 8)
			{
				qs.giveAdena(9290, true);
				qs.addExpAndSp(28806, 1207);
			}
			else if (player.getLevel() >= 7)
			{
				qs.giveAdena(11567, true);
				qs.addExpAndSp(36942, 1541);
			}
			else
			{
				qs.giveAdena(12928, true);
				qs.addExpAndSp(42191, 1753);
			}
			
			vars.set("NEWBIE_LEVEL", false);
		}
		
		return "30598.htm";
	}
}
