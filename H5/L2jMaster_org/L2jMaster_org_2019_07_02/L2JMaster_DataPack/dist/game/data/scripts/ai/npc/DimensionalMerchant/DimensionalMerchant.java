/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package ai.npc.DimensionalMerchant;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * Dimensional Merchant AI.
 * @author Mez
 */
public final class DimensionalMerchant extends AbstractNpcAI
{
	// NPC
	private static final int DIMENSIONAL_MERCHANT = 32478;
	// Items
	private static final int NORMAL_COUPON = 13273; // Hunting Helper Exchange Coupon - 5 hours
	private static final int HIGH_GRADE_COUPON = 14065; // High-Grade Hunting Helper Exchange Coupon
	private static final int FRIEND_RECOMMENDATION_PROOF = 15279; // Friend Recommendation Proof
	private static final int WHITE_WEASEL = 13017; // White Weasel Hunting Helper Necklace
	private static final int FAIRY_PRINCESS = 13018; // Fairy Princess Hunting Helper Necklace
	private static final int WILD_BEAST_FIGHTER = 13019; // Wild Beast Fighter Hunting Helper Necklace
	private static final int FOX_SHAMAN = 13020; // Fox Shaman Hunting Helper Necklace
	private static final int TOY_KNIGHT = 13548; // Toy Knight Hunting Helper Necklace
	private static final int SPIRIT_SHAMAN = 14062; // Spirit Shaman Summon Bracelet
	private static final int TURTLE_ASCETIC = 13551; // Turtle Ascetic Hunting Helper Necklace
	// Gifts
	private static final int[] GIFTS =
	{
		15213, // Friend Recommend Round 1 Reward Pack
		15215, // Friend Recommend Round 2 Reward Pack
		15217, // Friend Recommend Round 1 Reward Pack
		15219 // Friend Recommend Round 1 Reward Pack
	};
	private DimensionalMerchant()
	{
		super(DimensionalMerchant.class.getSimpleName(), "ai/npc");
		addFirstTalkId(DIMENSIONAL_MERCHANT);
		addStartNpc(DIMENSIONAL_MERCHANT);
		addTalkId(DIMENSIONAL_MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "32478-01.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-03.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-04.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-05.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-06.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-07.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-08.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-09.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-10.htm":
			{
				htmltext = event;
				break;
			}
			case "32478-11.htm":
			{
				htmltext = event;
				break;
			}
		}
		if (event.equalsIgnoreCase("WhiteWeasel"))
		{
			if ((player.getInventory().getItemByItemId(NORMAL_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, NORMAL_COUPON, 1);
				giveItems(player, WHITE_WEASEL, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("FairyPrincess"))
		{
			if ((player.getInventory().getItemByItemId(NORMAL_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, NORMAL_COUPON, 1);
				giveItems(player, FAIRY_PRINCESS, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("WildBeastFighter"))
		{
			if ((player.getInventory().getItemByItemId(NORMAL_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, NORMAL_COUPON, 1);
				giveItems(player, WILD_BEAST_FIGHTER, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("FoxShaman"))
		{
			if ((player.getInventory().getItemByItemId(NORMAL_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, NORMAL_COUPON, 1);
				giveItems(player, FOX_SHAMAN, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("ToyKnight"))
		{
			if ((player.getInventory().getItemByItemId(HIGH_GRADE_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, HIGH_GRADE_COUPON, 1);
				giveItems(player, TOY_KNIGHT, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("SpiritShaman"))
		{
			if ((player.getInventory().getItemByItemId(HIGH_GRADE_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, HIGH_GRADE_COUPON, 1);
				giveItems(player, SPIRIT_SHAMAN, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("TurtleAscetic"))
		{
			if ((player.getInventory().getItemByItemId(HIGH_GRADE_COUPON) == null))
			{
				htmltext = "no-coupons.htm";
			}
			else
			{
				takeItems(player, HIGH_GRADE_COUPON, 1);
				giveItems(player, TURTLE_ASCETIC, 1);
				htmltext = "done.htm";
			}
		}
		if (event.equalsIgnoreCase("Gifts"))
		{
			if ((player.getInventory().getItemByItemId(FRIEND_RECOMMENDATION_PROOF) == null))
			{
				htmltext = "no-proof.htm";
			}
			else
			{
				takeItems(player, FRIEND_RECOMMENDATION_PROOF, 1);
				rewardItems(player, GIFTS[getRandom(GIFTS.length)], 1);
				htmltext = "done.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32478.htm";
	}
	
	public static void main(String[] args)
	{
		new DimensionalMerchant();
	}
}