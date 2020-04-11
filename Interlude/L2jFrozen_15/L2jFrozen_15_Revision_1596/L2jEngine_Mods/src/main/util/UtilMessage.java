package main.util;

import java.util.Collection;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;

import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class UtilMessage
{
	// XXX CreatureSay ----------------------------------------------------------------------------------------------------------
	
	public static void sendCreatureMsg(L2Character player, int say2, String name, String text)
	{
		player.sendPacket(new CreatureSay(player.getObjectId(), say2, name, text));
	}
	
	public static void sendCreatureMsg(CharacterHolder ph, int say2, String name, String text)
	{
		ph.getInstance().sendPacket(new CreatureSay(ph.getObjectId(), say2, name, text));
	}
	
	public static void sendClanMembersMsg(L2Clan clan, int say2, String name, String text)
	{
		for (L2ClanMember member : clan.getMembers())
		{
			sendCreatureMsg(member.getPlayerInstance(), say2, name, text);
		}
	}
	
	// XXX Announcements ----------------------------------------------------------------------------------------------------
	
	/**
	 * Send Message normal announcement<b>(SayType.ANNOUNCEMENT)</b>
	 * @param text
	 * @param list
	 */
	public static void sendAnnounceMsg(String text, Collection<L2PcInstance> list)
	{
		list.forEach(pc -> sendCreatureMsg(pc, Say2.ANNOUNCEMENT, "Server", text));
	}
	
	/**
	 * Send Message normal announcement<b>(SayType.ANNOUNCEMENT)</b>
	 * @param text
	 * @param list
	 */
	public static void sendAnnounceMsg(String text, PlayerHolder ph)
	{
		sendCreatureMsg(ph.getInstance(), Say2.ANNOUNCEMENT, "Server", text);
	}
	
	/**
	 * Send Message normal announcement<b>(SayType.ANNOUNCEMENT)</b>
	 * @param text
	 * @param list
	 */
	public static void sendAnnounceMsg(String text, List<PlayerHolder> list)
	{
		list.forEach(pc -> sendCreatureMsg(pc, Say2.ANNOUNCEMENT, "Server", text));
	}
	
	/**
	 * Send message to all players in server.
	 * @param say2
	 * @param text
	 */
	public static void toAllOnline(int say2, String text)
	{
		L2World.getInstance().getAllPlayers().forEach(p -> sendCreatureMsg(p, say2, "Server", text));
	}
	
	/**
	 * Send packet to all online players
	 * @param packet
	 */
	public static void toAllOnline(L2GameServerPacket packet)
	{
		L2World.getInstance().getAllPlayers().forEach(p -> p.sendPacket(packet));
	}
}
