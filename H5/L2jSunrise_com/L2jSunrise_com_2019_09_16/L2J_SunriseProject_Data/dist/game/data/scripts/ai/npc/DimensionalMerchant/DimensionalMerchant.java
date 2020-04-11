package ai.npc.DimensionalMerchant;

import java.util.HashMap;

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.handler.ItemHandler;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExGetPremiumItemList;

import ai.npc.AbstractNpcAI;

/**
 * Dimensional Merchant AI.
 * @author vGodFather
 */
public final class DimensionalMerchant extends AbstractNpcAI
{
	// NPC
	private static final int MERCHANT = 32478; // Dimensional Merchant
	// Items
	private static final int MINION_COUPON = 13273; // Minion Coupon (5-hour)
	private static final int MINION_COUPON_EV = 13383; // Minion Coupon (5-hour) (Event)
	private static final int SUP_MINION_COUPON = 14065; // Superior Minion Coupon - 5-hour
	private static final int SUP_MINION_COUPON_EV = 14074; // Superior Minion Coupon (Event) - 5-hour
	private static final int ENH_MINION_COUPON = 20914; // Enhanced Rose Spirit Coupon (5-hour)
	private static final int ENH_MINION_COUPON_EV = 22240; // Enhanced Rose Spirit Coupon (5-hour) - Event
	// Misc
	private static final HashMap<String, Integer> MINION_EXCHANGE = new HashMap<>();
	
	{
		// Normal
		MINION_EXCHANGE.put("whiteWeasel", 13017); // White Weasel Minion Necklace
		MINION_EXCHANGE.put("fairyPrincess", 13018); // Fairy Princess Minion Necklace
		MINION_EXCHANGE.put("wildBeast", 13019); // Wild Beast Fighter Minion Necklace
		MINION_EXCHANGE.put("foxShaman", 13020); // Fox Shaman Minion Necklace
		// Superior
		MINION_EXCHANGE.put("toyKnight", 14061); // Toy Knight Summon Whistle
		MINION_EXCHANGE.put("spiritShaman", 14062); // Spirit Shaman Summon Whistle
		MINION_EXCHANGE.put("turtleAscetic", 14064); // Turtle Ascetic Summon Necklace
		// Enhanced
		MINION_EXCHANGE.put("desheloph", 20915); // Enhanced Rose Necklace: Desheloph
		MINION_EXCHANGE.put("hyum", 20916); // Enhanced Rose Necklace: Hyum
		MINION_EXCHANGE.put("lekang", 20917); // Enhanced Rose Necklace: Lekang
		MINION_EXCHANGE.put("lilias", 20918); // Enhanced Rose Necklace: Lilias
		MINION_EXCHANGE.put("lapham", 20919); // Enhanced Rose Necklace: Lapham
		MINION_EXCHANGE.put("mafum", 20920); // Enhanced Rose Necklace: Mafum
	}
	
	public DimensionalMerchant()
	{
		super(DimensionalMerchant.class.getSimpleName(), "ai/npc");
		addStartNpc(MERCHANT);
		addFirstTalkId(MERCHANT);
		addTalkId(MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "32478.html":
			case "32478-01.html":
			case "32478-02.html":
			case "32478-03.html":
			case "32478-04.html":
			case "32478-05.html":
			case "32478-06.html":
			case "32478-07.html":
			case "32478-08.html":
			case "32478-09.html":
			case "32478-10.html":
			case "32478-11.html":
			case "32478-12.html":
			case "32478-13.html":
			case "32478-14.html":
			case "32478-15.html":
			case "32478-16.html":
			case "32478-17.html":
			case "32478-18.html":
			case "32478-19.html":
			case "32478-24.html":
			{
				htmltext = event;
				break;
			}
			case "getDimensonalItem":
			{
				if (player.getPremiumItemList().isEmpty())
				{
					player.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
				}
				else
				{
					player.sendPacket(new ExGetPremiumItemList(player));
				}
				break;
			}
			case "whiteWeasel":
			case "fairyPrincess":
			case "wildBeast":
			case "foxShaman":
			{
				htmltext = giveMinion(player, event, MINION_COUPON, MINION_COUPON_EV);
				break;
			}
			case "toyKnight":
			case "spiritShaman":
			case "turtleAscetic":
			{
				htmltext = giveMinion(player, event, SUP_MINION_COUPON, SUP_MINION_COUPON_EV);
				break;
			}
			case "desheloph":
			case "hyum":
			case "lekang":
			case "lilias":
			case "lapham":
			case "mafum":
			{
				htmltext = giveMinion(player, event, ENH_MINION_COUPON, ENH_MINION_COUPON_EV);
				break;
			}
		}
		return htmltext;
	}
	
	private String giveMinion(L2PcInstance player, String event, int couponId, int eventCouponId)
	{
		if (hasAtLeastOneQuestItem(player, couponId, eventCouponId))
		{
			takeItems(player, (hasQuestItems(player, eventCouponId) ? eventCouponId : couponId), 1);
			final int minionId = MINION_EXCHANGE.get(event);
			giveItems(player, minionId, 1);
			final L2ItemInstance summonItem = player.getInventory().getItemByItemId(minionId);
			final IItemHandler handler = ItemHandler.getInstance().getHandler(summonItem.getEtcItem());
			if ((handler != null) && !player.hasPet())
			{
				handler.useItem(player, summonItem, true);
			}
			return "32478-08.html";
		}
		return "32478-07.html";
	}
}