package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestGetItemFromPet extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestGetItemFromPet.class);
	
	private int objectId;
	private int amount;
	@SuppressWarnings("unused")
	private int unknown;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		amount = readD();
		unknown = readD();// = 0 for most trades
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null || player.getPet() == null || !(player.getPet() instanceof L2PetInstance))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("getfrompet"))
		{
			player.sendMessage("You get items from pet too fast.");
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) player.getPet();
		
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit , And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		if (amount < 0)
		{
			player.setAccessLevel(-1);
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (amount == 0)
		{
			return;
		}
		
		if (player.getDistanceSq(pet) > 40000) // 200*200
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_TOO_FAR));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (pet.transferItem("Transfer", objectId, amount, player.getInventory(), player, pet) == null)
		{
			LOGGER.warn("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
		player.sendPacket(new ItemList(player, true));
	}
	
	@Override
	public String getType()
	{
		return "[C] 8C RequestGetItemFromPet";
	}
}
