package l2r.gameserver.model.actor.instance;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.reunion.javaBuffer.AutoBuff;
import gr.reunion.javaBuffer.BufferPacketCategories;
import gr.reunion.javaBuffer.BufferPacketSender;
import gr.reunion.javaBuffer.JavaBufferBypass;
import gr.reunion.javaBuffer.PlayerMethods;
import gr.reunion.javaBuffer.buffNpc.dynamicHtmls.GenerateHtmls;
import gr.reunion.javaBuffer.runnable.BuffDeleter;
import gr.reunion.main.Conditions;

public class L2BufferInstance extends L2Npc
{
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/NpcBuffer/" + getTemplate().getId() + ".htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2BufferInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	// Manages all bypasses for normal players
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if (!Conditions.checkPlayerConditions(player))
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
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/NpcBuffer/" + subCommand[1]);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
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
		
		// Buffer
		else if (command.startsWith("removebuff"))
		{
			player.stopAllEffects();
			BufferPacketSender.sendPacket(player, "555-2.htm", BufferPacketCategories.FILE, getObjectId());
		}
		else if (command.startsWith("healme"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			BufferPacketSender.sendPacket(player, "555-2.htm", BufferPacketCategories.FILE, getObjectId());
		}
		else if (command.startsWith("autobuff"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneIdType.PEACE))
			{
				player.sendMessage("Cannot use this feature here with flag.");
				return;
			}
			
			AutoBuff.autoBuff(player);
			BufferPacketSender.sendPacket(player, "555-2.htm", BufferPacketCategories.FILE, getObjectId());
		}
		else if (command.startsWith("buff"))
		{
			JavaBufferBypass.callBuffCommand(player, subCommand[1], subCommand[0], getObjectId());
		}
		
		else if (command.startsWith("saveProfile"))
		{
			try
			{
				JavaBufferBypass.callSaveProfile(player, subCommand[1], getObjectId());
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a valid profile name.");
				BufferPacketSender.sendPacket(player, "555-11.htm", BufferPacketCategories.FILE, getObjectId());
				return;
			}
		}
		else if (command.startsWith("showAvaliable"))
		{
			JavaBufferBypass.callAvailableCommand(player, subCommand[0], subCommand[1], getObjectId());
		}
		else if (command.startsWith("add"))
		{
			JavaBufferBypass.callAddCommand(player, subCommand[0], subCommand[1], subCommand[2], getObjectId());
		}
		
		// Scheme removals
		else if (command.startsWith("deleteProfile"))
		{
			PlayerMethods.delProfile(subCommand[1], player);
		}
		else if (command.startsWith("showBuffsToDelete"))
		{
			GenerateHtmls.showBuffsToDelete(player, subCommand[1], "removeBuffs", getObjectId());
		}
		else if (command.startsWith("removeBuffs"))
		{
			ThreadPoolManager.getInstance().executeGeneral(new BuffDeleter(player, subCommand[1], Integer.parseInt(subCommand[2]), getObjectId()));
		}
		else if (command.startsWith("showProfiles"))
		{
			GenerateHtmls.showSchemeToEdit(player, subCommand[1], getObjectId());
		}
	}
}