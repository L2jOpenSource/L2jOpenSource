package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * An instance type extending {@link Doorman}, used by clan hall doorman.<br>
 * <br>
 * isOwnerClan() checks if the user is part of clan owning the clan hall.
 */
public class ClanHallDoorman extends Doorman
{
	public ClanHallDoorman(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (getClanHall() == null)
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		final Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
		if (isOwnerClan(player))
		{
			html.setFile("data/html/clanHallDoormen/doormen.htm");
			html.replace("%clanname%", owner.getName());
		}
		else
		{
			if (owner != null && owner.getLeader() != null)
			{
				html.setFile("data/html/clanHallDoormen/doormen-no.htm");
				html.replace("%leadername%", owner.getLeaderName());
				html.replace("%clanname%", owner.getName());
			}
			else
			{
				html.setFile("data/html/clanHallDoormen/emptyowner.htm");
				html.replace("%hallname%", getClanHall().getName());
			}
		}
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		showChatWindow(player);
	}
	
	@Override
	protected final void openDoors(Player player, String command)
	{
		getClanHall().openCloseDoors(true);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/clanHallDoormen/doormen-opened.htm");
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	@Override
	protected final void closeDoors(Player player, String command)
	{
		getClanHall().openCloseDoors(false);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/clanHallDoormen/doormen-closed.htm");
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
	
	@Override
	protected final boolean isOwnerClan(Player player)
	{
		return getClanHall() != null && player.getClan() != null && player.getClanId() == getClanHall().getOwnerId();
	}
}