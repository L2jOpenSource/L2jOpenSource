package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestGetItemFromPet.class);
	
	private int objectId;
	private int amount;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null || !(player.getPet() instanceof L2PetInstance))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("giveitemtopet"))
		{
			player.sendMessage("You give items to pet too fast.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
		{
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot exchange items while trading");
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		final L2ItemInstance item = player.getInventory().getItemByObjectId(objectId);
		
		if (item == null)
		{
			return;
		}
		
		if (item.isAugmented())
		{
			return;
		}
		
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) player.getPet();
		
		if (pet.isDead())
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}
		
		if (amount < 0)
		{
			return;
		}
		
		if (player.transferItem("Transfer", objectId, amount, pet.getInventory(), pet) == null)
		{
			LOGGER.warn("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 8B RequestGiveItemToPet";
	}
}
