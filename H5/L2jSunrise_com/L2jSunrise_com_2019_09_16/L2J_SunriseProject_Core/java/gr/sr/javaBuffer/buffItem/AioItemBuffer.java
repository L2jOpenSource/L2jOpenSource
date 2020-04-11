package gr.sr.javaBuffer.buffItem;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.BufferConfigs;
import gr.sr.javaBuffer.AutoBuff;
import gr.sr.javaBuffer.BufferPacketCategories;
import gr.sr.javaBuffer.BufferPacketSender;
import gr.sr.javaBuffer.JavaBufferBypass;
import gr.sr.javaBuffer.PlayerMethods;
import gr.sr.javaBuffer.buffItem.dynamicHtmls.GenerateHtmls;
import gr.sr.javaBuffer.runnable.BuffDeleter;
import gr.sr.main.Conditions;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;

/**
 * @author vGodFather
 */
public class AioItemBuffer
{
	private AioItemBuffer()
	{
		// Dummy default
	}
	
	// Manages all bypasses for normal players
	public static void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if ((player.getInventory().getItemByItemId(BufferConfigs.DONATE_BUFF_ITEM_ID) == null) && !player.isPremium())
		{
			SecurityActions.startSecurity(player, SecurityType.AIO_ITEM_BUFFER);
			return;
		}
		// Restrictions added here
		if (!Conditions.checkItemBufferConditions(player))
		{
			return;
		}
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage msg = new NpcHtmlMessage();
			msg.setFile(player, player.getHtmlPrefix(), "/data/html/sunrise/ItemBuffer/" + subCommand[1]);
			player.sendPacket(msg);
		}
		// Method to remove all players buffs
		else if (command.startsWith("removebuff"))
		{
			player.stopAllEffects();
			BufferPacketSender.sendPacket(player, "functions.htm", BufferPacketCategories.FILE, 0);
		}
		// Method to restore HP/MP/CP
		else if (command.startsWith("healme"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneIdType.PEACE))
			{
				player.sendMessage("Cannot use this feature here with flag.");
				return;
			}
			
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			
			if (player.hasSummon())
			{
				player.getSummon().setCurrentHpMp(player.getSummon().getMaxHp(), player.getSummon().getMaxMp());
				player.getSummon().setCurrentCp(player.getSummon().getMaxCp());
			}
			
			BufferPacketSender.sendPacket(player, "functions.htm", BufferPacketCategories.FILE, 0);
		}
		// Method to give auto buffs depends on class
		else if (command.startsWith("autobuff"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneIdType.PEACE))
			{
				player.sendMessage("Cannot use this feature here with flag.");
				return;
			}
			
			AutoBuff.autoBuff(player);
			BufferPacketSender.sendPacket(player, "functions.htm", BufferPacketCategories.FILE, 0);
		}
		
		// Send buffs from profile to player or party or pet
		else if (command.startsWith("bufffor"))
		{
			if (command.startsWith("buffforpet"))
			{
				JavaBufferBypass.callPetBuffCommand(player, subCommand[1]);
			}
			else if (command.startsWith("buffforparty"))
			{
				JavaBufferBypass.callPartyBuffCommand(player, subCommand[1]);
			}
			else if (command.startsWith("buffforme"))
			{
				JavaBufferBypass.callSelfBuffCommand(player, subCommand[1]);
			}
		}
		
		// Method to give single buffs
		else if (command.startsWith("buff"))
		{
			JavaBufferBypass.callBuffCommand(player, subCommand[1], subCommand[0], 0);
		}
		// Scheme create new profile
		else if (command.startsWith("saveProfile"))
		{
			try
			{
				if (!PlayerMethods.createProfile(subCommand[1], player))
				{
					return;
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a valid profile name.");
				BufferPacketSender.sendPacket(player, "newSchemeProfile.htm", BufferPacketCategories.FILE, 0);
				return;
			}
		}
		else if (command.startsWith("showAvaliable"))
		{
			JavaBufferBypass.callAvailableCommand(player, subCommand[0], subCommand[1], 0);
		}
		else if (command.startsWith("add"))
		{
			JavaBufferBypass.callAddCommand(player, subCommand[0], subCommand[1], subCommand[2], 0);
		}
		// Method to delete player's selected profile
		else if (command.startsWith("deleteProfile"))
		{
			PlayerMethods.delProfile(subCommand[1], player);
			BufferPacketSender.sendPacket(player, "main.htm", BufferPacketCategories.FILE, 0);
		}
		else if (command.startsWith("showBuffsToDelete"))
		{
			GenerateHtmls.showBuffsToDelete(player, subCommand[1], "removeBuffs");
		}
		else if (command.startsWith("removeBuffs"))
		{
			ThreadPoolManager.getInstance().executeGeneral(new BuffDeleter(player, subCommand[1], Integer.parseInt(subCommand[2]), 0));
		}
		else if (command.startsWith("showProfiles"))
		{
			GenerateHtmls.showSchemeToEdit(player, subCommand[1]);
		}
	}
}