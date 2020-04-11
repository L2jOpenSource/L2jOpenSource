package com.l2jfrozen.gameserver.handler.voicedcommandhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IVoicedCommandHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.TradeList;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * Command .offline_shop
 * @author Nefer
 */
public class OfflineShop implements IVoicedCommandHandler
{
	private static String[] voicedCommands =
	{
		"offline_shop"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String target)
	{
		if (player == null)
		{
			return false;
		}
		
		if (!Config.OFFLINE_TRADE_ENABLE && !Config.OFFLINE_CRAFT_ENABLE)
		{
			return false;
		}
		
		if (player.isInFunEvent() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while in registered in an Event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			return false;
		}
		
		// Dont allow leaving if player is in combat
		if (player.isInCombat() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while is in Combat mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Dont allow leaving if player is teleporting
		if (player.isTeleporting() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while is Teleporting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (player.atEvent)
		{
			player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event."));
			return false;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can't Logout in Olympiad mode.");
			return false;
		}
		
		// Message like L2OFF
		if ((!player.isInStoreMode() && (!player.isInCraftMode())) || !player.isSitting())
		{
			player.sendMessage("You are not running a private store or private work shop.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		TradeList storeListBuy = player.getBuyList();
		TradeList storeListSell = player.getSellList();
		
		if (storeListBuy == null && storeListSell == null)
		{
			player.sendMessage("Your private store list is empty.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if ((storeListBuy != null && storeListBuy.getItemCount() == 0) && (storeListSell != null && storeListSell.getItemCount() == 0) && !player.isInCraftMode())
		{
			player.sendMessage("Your private store has no items.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		player.getInventory().updateDatabase();
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Prevent player from logging out if they are a festival participant nd it is in progress,
		// otherwise notify party members that the player is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot Logout while you are a participant in a Festival.");
				return false;
			}
			
			L2Party playerParty = player.getParty();
			if (playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming Festival."));
			}
		}
		
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		
		if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE))
		{
			// Sleep effect, not official feature but however L2OFF features (like offline trade)
			if (Config.OFFLINE_SLEEP_EFFECT)
			{
				player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_SLEEP);
			}
			
			player.logout();
			return true;
		}
		
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return voicedCommands;
	}
}