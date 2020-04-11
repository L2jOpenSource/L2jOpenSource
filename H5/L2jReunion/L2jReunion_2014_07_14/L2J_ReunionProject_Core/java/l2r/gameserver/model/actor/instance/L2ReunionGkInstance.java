package l2r.gameserver.model.actor.instance;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.reunion.datatables.CustomTable;
import gr.reunion.main.Conditions;
import gr.reunion.securityEngine.SecurityActions;
import gr.reunion.securityEngine.SecurityType;

/**
 * @author -=DoctorNo=-
 */
public final class L2ReunionGkInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2ReunionGkInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2ReunionGkInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static int getPlayersInZoneCount(int zoneId)
	{
		int playersCount = 0;
		
		for (L2ZoneType zone : ZoneManager.getInstance().getAllZones())
		{
			if (zone.getId() == zoneId)
			{
				for (L2Character character : zone.getCharactersInside())
				{
					if (character.isPlayer())
					{
						playersCount++;
					}
				}
				
				return playersCount;
			}
		}
		return playersCount;
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/ReunionGatekeeper/main.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param player
	 * @param command
	 */
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendMessage("Cannot use while hava karma.");
			return;
		}
		
		if (player.isEnchanting())
		{
			player.sendMessage("Cannot use while Enchanting.");
			return;
		}
		
		if (player.isAlikeDead())
		{
			player.sendMessage("Cannot use while Dead or Fake Death.");
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
			html.setFile(player.getHtmlPrefix(), "data/html/ReunionGatekeeper/" + subCommand[1]);
			html.replace("%players%", String.valueOf(getPlayersInZoneCount(ZoneIdType.ZONE_CHAOTIC.getId())));
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		// Teleport
		else if (command.startsWith("teleportTo"))
		{
			int itemIdToGet = 57;
			int price = 1000;
			
			if (!Conditions.checkPlayerItemCount(player, itemIdToGet, price))
			{
				return;
			}
			
			if (player.isTransformed())
			{
				if ((player.getTransformationId() == 9) || (player.getTransformationId() == 8))
				{
					player.untransform();
				}
			}
			
			if (command.startsWith("teleportToReunion"))
			{
				try
				{
					Integer[] c = new Integer[3];
					c[0] = CustomTable.getInstance().getGKCoords(Integer.parseInt(subCommand[1]))[0];
					c[1] = CustomTable.getInstance().getGKCoords(Integer.parseInt(subCommand[1]))[1];
					c[2] = CustomTable.getInstance().getGKCoords(Integer.parseInt(subCommand[1]))[2];
					player.destroyItemByItemId("Reunion Teleport", itemIdToGet, price, player, true);
					player.teleToLocation(c[0], c[1], c[2]);
				}
				catch (Exception e)
				{
					SecurityActions.startSecurity(player, SecurityType.REUNION_GATEKEEPER);
				}
			}
			else if (command.startsWith("teleportToGlobal"))
			{
				try
				{
					Integer[] c = new Integer[3];
					c[0] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[0];
					c[1] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[1];
					c[2] = CustomTable.getInstance().getCoords(Integer.parseInt(subCommand[1]))[2];
					player.destroyItemByItemId("Global Teleport", itemIdToGet, price, player, true);
					player.teleToLocation(c[0], c[1], c[2]);
				}
				catch (Exception e)
				{
					SecurityActions.startSecurity(player, SecurityType.REUNION_GATEKEEPER);
				}
			}
		}
	}
}