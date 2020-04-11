package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.GameServer;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.L2GameClient.GameClientState;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jfrozen.gameserver.network.serverpackets.RestartResponse;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;

import main.EngineModsManager;

public final class RequestRestart extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestRestart.class);
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		// Check if player is == null
		if (player == null)
		{
			LOGGER.warn("[RequestRestart] activeChar null!?");
			return;
		}
		
		if (EngineModsManager.onExitWorld(player))
		{
			return;
		}
		
		// Check if player is enchanting
		if (player.getActiveEnchantItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Check if player are changing class
		if (player.isLocked())
		{
			LOGGER.warn("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		player.getInventory().updateDatabase();
		
		// Check if player is in private store
		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot restart while trading.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Check if player is in combat
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug("Player " + player.getName() + " tried to logout while fighting.");
			}
			
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_RESTART_WHILE_FIGHTING));
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Check if player is registred on olympiad
		if (player.getOlympiadGameId() > 0 || player.isInOlympiadMode())
		{
			player.sendMessage("You can't restart while in Olympiad.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Check if player is in away mode
		if (player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(RestartResponse.valueOf(false));
				return;
			}
			
			final L2Party playerParty = player.getParty();
			if (playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}
		
		// Check if player is in Event
		if (player.isInFunEvent())
		{
			player.sendMessage("You can't restart during Event.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Fix against exploit anti-target
		if (player.isCastingNow())
		{
			player.abortCast();
			player.sendPacket(new ActionFailed());
		}
		
		// Check if player is teleporting
		if (player.isTeleporting())
		{
			player.abortCast();
			player.setIsTeleporting(false);
		}
		
		// Check if player is trading
		if (player.getActiveRequester() != null)
		{
			player.getActiveRequester().onTradeCancel(player);
			player.onTradeCancel(player.getActiveRequester());
		}
		
		// Check if player are flying
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		
		if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null && player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).isAugmented())
		{
			player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getAugmentation().removeBoni(player);
		}
		
		// delete box from the world
		if (player.activeBoxesCount != -1)
		{
			player.decreaseBoxes();
		}
		
		final L2GameClient client = getClient();
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		
		// removing player from the world
		player.deleteMe();
		player.store();
		
		getClient().setActiveChar(null);
		
		// return the client to the authed status
		client.setState(GameClientState.AUTHED);
		
		// before the char selection, check shutdown status
		if (GameServer.getSelectorThread().isShutdown())
		{
			getClient().closeNow();
			return;
		}
		
		// Restart true
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectInfo cl = new CharSelectInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
	
	@Override
	public String getType()
	{
		return "[C] 46 RequestRestart";
	}
}
