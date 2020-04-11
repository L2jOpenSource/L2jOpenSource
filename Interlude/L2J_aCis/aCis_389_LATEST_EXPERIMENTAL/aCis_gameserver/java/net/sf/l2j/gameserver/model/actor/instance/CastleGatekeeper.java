package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;

/**
 * This class manages all Mass Gatekeepers, an entity linked to Castle system. It inherits from {@link Folk}.<br>
 * <br>
 * Mass Gatekeepers allow Castle Defenders Players to teleport back to battle, after 30 seconds. The time can increase to 480 seconds (8 minutes) during an active siege where all ControlTowers shattered.
 */
public class CastleGatekeeper extends Folk
{
	private Future<?> _teleportTask;
	
	public CastleGatekeeper(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("tele"))
		{
			if (_teleportTask == null)
				_teleportTask = ThreadPool.schedule(() -> oustPlayers(), getDelayInSeconds() * 1000);
			
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/castleteleporter/MassGK-1.htm");
			html.replace("%delay%", getDelayInSeconds());
			player.sendPacket(html);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (_teleportTask == null)
		{
			if (getCastle().getSiege().isInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
				html.setFile("data/html/castleteleporter/MassGK-2.htm");
			else
				html.setFile("data/html/castleteleporter/MassGK.htm");
		}
		else
		{
			html.setFile("data/html/castleteleporter/MassGK-1.htm");
			html.replace("%delay%", getDelayInSeconds());
		}
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	/**
	 * Oust all {@link Player}s and broadcast a message to everyone set into the region, during an active siege event.
	 */
	private final void oustPlayers()
	{
		// Region talk is only done during an active siege.
		if (getCastle().getSiege().isInProgress())
		{
			final NpcSay cs = new NpcSay(this, SayType.SHOUT, "The defenders of " + getCastle().getName() + " castle have been teleported to the inner castle.");
			final int region = MapRegionData.getInstance().getMapRegion(getX(), getY());
			
			for (Player player : World.getInstance().getPlayers())
			{
				if (region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY()))
					player.sendPacket(cs);
			}
		}
		
		// Oust all related players.
		getCastle().oustAllPlayers();
		
		// Reset the variable, in order to properly reuse it.
		_teleportTask = null;
	}
	
	/**
	 * Retrieve the teleport delay, as following :
	 * <ul>
	 * <li>30 seconds for regular teleport.</li>
	 * <li>480 seconds (8 minutes) during an active siege, and if all ControlTowers have been broken.</li>
	 * </ul>
	 * @return the teleport delay in seconds.
	 */
	private final int getDelayInSeconds()
	{
		return (getCastle().getSiege().isInProgress() && getCastle().getSiege().getControlTowerCount() == 0) ? 480 : 30;
	}
}