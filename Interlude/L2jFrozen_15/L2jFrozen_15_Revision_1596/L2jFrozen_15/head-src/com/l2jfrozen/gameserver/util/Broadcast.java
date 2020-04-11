package com.l2jfrozen.gameserver.util;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;

/**
 * @author luisantonioa
 */
public final class Broadcast
{
	private static Logger LOGGER = Logger.getLogger(Broadcast.class);
	
	/**
	 * Send a packet to all L2PcInstance in the knownPlayers of the L2Character that have the Character targetted.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(final L2Character character, final L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		}
		
		for (final L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null || player.getTarget() != character)
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the knownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character
	 * @param mov
	 */
	public static void toKnownPlayers(final L2Character character, final L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		}
		
		final Collection<L2PcInstance> knownlist_players = character.getKnownList().getKnownPlayers().values();
		
		for (final L2PcInstance player : knownlist_players)
		{
			if (player == null)
			{
				continue;
			}
			
			/*
			 * TEMP FIX: If player is not visible don't send packets broadcast to all his KnowList. This will avoid GM detection with l2net and olympiad's crash. We can now find old problems with invisible mode.
			 */
			if (character instanceof L2PcInstance && !player.isGM() && (((L2PcInstance) character).getAppearance().isInvisible() || ((L2PcInstance) character).inObserverMode()))
			{
				return;
			}
			
			try
			{
				player.sendPacket(mov);
				if (mov instanceof CharInfo && character instanceof L2PcInstance)
				{
					final int relation = ((L2PcInstance) character).getRelation(player);
					
					if (character.getKnownList().getKnownRelations().get(player.getObjectId()) != null && character.getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, player.isAutoAttackable(character)));
					}
				}
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the knownPlayers (in the specified radius) of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just needs to go through knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param character
	 * @param mov
	 * @param radius
	 */
	public static void toKnownPlayersInRadius(final L2Character character, final L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		for (final L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the knownPlayers of the L2Character and to the specified character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * @param character
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(final L2Character character, final L2GameServerPacket mov)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		toKnownPlayers(character, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(final L2Character character, final L2GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
		{
			radiusSq = 360000;
		}
		
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		for (final L2PcInstance player : character.getKnownList().getKnownPlayers().values())
		{
			if (player != null && character.getDistanceSq(player) <= radiusSq)
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance present in the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through allPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * @param mov
	 */
	public static void toAllOnlinePlayers(final L2GameServerPacket mov)
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("Players to notify: " + L2World.getAllPlayersCount() + " (with packet " + mov.getType() + ")");
		}
		
		for (final L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}
			
			onlinePlayer.sendPacket(mov);
		}
	}
}
