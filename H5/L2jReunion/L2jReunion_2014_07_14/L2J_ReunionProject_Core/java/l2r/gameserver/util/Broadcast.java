/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.util;

import java.util.Collection;

import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CharInfo;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.RelationChanged;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class Broadcast
{
	private static Logger _log = LoggerFactory.getLogger(Broadcast.class);
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targeted.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player.getTarget() != character)
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
		
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 */
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (character instanceof L2PcInstance))
				{
					int relation = ((L2PcInstance) character).getRelation(player);
					Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
					if ((oldrelation != null) && (oldrelation != relation))
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
						if (character.hasSummon())
						{
							player.sendPacket(new RelationChanged(character.getSummon(), relation, character.isAutoAttackable(player)));
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				_log.warn(e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified radius) of the L2Character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 * @param radius
	 */
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * @param character
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		toKnownPlayers(character, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 600;
		}
		
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if ((player != null) && Util.checkIfInRange(radius, character, player, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance present in the world.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param packet
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket packet)
	{
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.isOnline())
			{
				player.sendPacket(packet);
			}
		}
	}
	
	public static void announceToOnlinePlayers(String text, boolean isCritical)
	{
		CreatureSay cs;
		
		if (isCritical)
		{
			cs = new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", text);
		}
		else
		{
			cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		}
		
		toAllOnlinePlayers(cs);
	}
	
	public static void toPlayersInInstance(L2GameServerPacket packet, int instanceId)
	{
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if (player.isOnline() && (player.getInstanceId() == instanceId))
			{
				player.sendPacket(packet);
			}
		}
	}
}
