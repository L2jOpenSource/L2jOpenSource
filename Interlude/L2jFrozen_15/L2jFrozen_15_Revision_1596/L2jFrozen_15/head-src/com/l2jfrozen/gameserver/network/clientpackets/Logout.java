package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;

import main.EngineModsManager;
import main.data.memory.ObjectData;
import main.holders.objects.PlayerHolder;

public final class Logout extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(Logout.class);
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		// Dont allow leaving if player is fighting
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.isInFunEvent() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while in registered in an Event.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			return;
		}
		
		if (EngineModsManager.onExitWorld(player))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.getInventory().updateDatabase();
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(getType() + ": Player " + player.getName() + " tried to logout while Fighting");
			}
			
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Dont allow leaving if player is in combat
		if (player.isInCombat() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while is in Combat mode.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Dont allow leaving if player is teleporting
		if (player.isTeleporting() && !player.isGM())
		{
			player.sendMessage("You cannot Logout while is Teleporting.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.atEvent)
		{
			player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event."));
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You can't Logout in Olympiad mode.");
			return;
		}
		
		// Prevent player from logging out if they are a festival participant nd it is in progress,
		// otherwise notify party members that the player is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot Logout while you are a participant in a Festival.");
				return;
			}
			
			final L2Party playerParty = player.getParty();
			if (playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming Festival."));
			}
		}
		
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		
		if (Config.OFFLINE_LOGOUT && player.isSitting())
		{
			if (player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE || player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE)
			{
				// Sleep effect, not official feature but however L2OFF features (like offline trade)
				if (Config.OFFLINE_SLEEP_EFFECT)
				{
					player.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_SLEEP);
				}
				
				player.setOfflineMode(true);
				player.store();
				player.closeNetConnection();
				
				if (player.getOfflineStartTime() == 0)
				{
					player.setOfflineStartTime(System.currentTimeMillis());
				}
				return;
			}
		}
		else if (player.isStored())
		{
			player.store();
			player.closeNetConnection();
			
			if (player.getOfflineStartTime() == 0)
			{
				player.setOfflineStartTime(System.currentTimeMillis());
			}
			return;
		}
		else if (ObjectData.get(PlayerHolder.class, player).isSellBuff())
		{
			getClient().close(LeaveWorld.STATIC_PACKET);
		}
		
		if (player.isCastingNow())
		{
			player.abortCast();
			player.sendPacket(new ActionFailed());
		}
		
		RegionBBSManager.getInstance().changeCommunityBoard();
		player.deleteMe();
	}
	
	@Override
	public String getType()
	{
		return "[C] 09 Logout";
	}
}