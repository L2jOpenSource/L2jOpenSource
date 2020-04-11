package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class DungeonGatekeeper extends Folk
{
	public DungeonGatekeeper(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		final CabalType sealAvariceOwner = SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE);
		final CabalType sealGnosisOwner = SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS);
		final CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
		final CabalType winningCabal = SevenSignsManager.getInstance().getWinningCabal();
		
		if (actualCommand.startsWith("necro"))
		{
			boolean canPort = true;
			if (SevenSignsManager.getInstance().isSealValidationPeriod())
			{
				if (winningCabal == CabalType.DAWN && (playerCabal != CabalType.DAWN || sealAvariceOwner != CabalType.DAWN))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
					canPort = false;
				}
				else if (winningCabal == CabalType.DUSK && (playerCabal != CabalType.DUSK || sealAvariceOwner != CabalType.DUSK))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
					canPort = false;
				}
				else if (winningCabal == CabalType.NORMAL && playerCabal != CabalType.NORMAL)
					canPort = true;
				else if (playerCabal == CabalType.NORMAL)
					canPort = false;
			}
			else
			{
				if (playerCabal == CabalType.NORMAL)
					canPort = false;
			}
			
			if (!canPort)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "necro_no.htm");
				player.sendPacket(html);
			}
			else
			{
				doTeleport(player, Integer.parseInt(st.nextToken()));
				player.setIsIn7sDungeon(true);
			}
		}
		else if (actualCommand.startsWith("cata"))
		{
			boolean canPort = true;
			if (SevenSignsManager.getInstance().isSealValidationPeriod())
			{
				if (winningCabal == CabalType.DAWN && (playerCabal != CabalType.DAWN || sealGnosisOwner != CabalType.DAWN))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
					canPort = false;
				}
				else if (winningCabal == CabalType.DUSK && (playerCabal != CabalType.DUSK || sealGnosisOwner != CabalType.DUSK))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
					canPort = false;
				}
				else if (winningCabal == CabalType.NORMAL && playerCabal != CabalType.NORMAL)
					canPort = true;
				else if (playerCabal == CabalType.NORMAL)
					canPort = false;
			}
			else
			{
				if (playerCabal == CabalType.NORMAL)
					canPort = false;
			}
			
			if (!canPort)
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(SevenSignsManager.SEVEN_SIGNS_HTML_PATH + "cata_no.htm");
				player.sendPacket(html);
			}
			else
			{
				doTeleport(player, Integer.parseInt(st.nextToken()));
				player.setIsIn7sDungeon(true);
			}
		}
		else if (actualCommand.startsWith("exit"))
		{
			doTeleport(player, Integer.parseInt(st.nextToken()));
			player.setIsIn7sDungeon(false);
		}
		else if (actualCommand.startsWith("goto"))
		{
			doTeleport(player, Integer.parseInt(st.nextToken()));
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/teleporter/" + filename + ".htm";
	}
	
	private static void doTeleport(Player player, int val)
	{
		final TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(val);
		if (list != null && !player.isAlikeDead())
			player.teleportTo(list, 20);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}