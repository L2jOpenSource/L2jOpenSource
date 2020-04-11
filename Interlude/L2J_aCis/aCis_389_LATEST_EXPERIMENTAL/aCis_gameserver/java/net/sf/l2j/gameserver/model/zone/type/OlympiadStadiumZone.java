package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.model.zone.type.subtype.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * A zone extending {@link SpawnZoneType}, used for olympiad event.<br>
 * <br>
 * Restart and the use of "summoning friend" skill aren't allowed. The zone is considered a pvp zone.
 */
public class OlympiadStadiumZone extends SpawnZoneType
{
	OlympiadGameTask _task = null;
	
	public OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	@Override
	protected final void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		character.setInsideZone(ZoneId.NO_RESTART, true);
		
		if (_task != null && _task.isBattleStarted())
		{
			character.setInsideZone(ZoneId.PVP, true);
			if (character instanceof Player)
			{
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				_task.getGame().sendOlympiadInfo(character);
			}
		}
		
		// Only participants, observers and GMs are allowed.
		final Player player = character.getActingPlayer();
		if (player != null && !player.isGM() && !player.isInOlympiadMode() && !player.isInObserverMode())
		{
			final Summon summon = player.getSummon();
			if (summon != null)
				summon.unSummon(player);
			
			player.teleportTo(TeleportType.TOWN);
		}
	}
	
	@Override
	protected final void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		character.setInsideZone(ZoneId.NO_RESTART, false);
		
		if (_task != null && _task.isBattleStarted())
		{
			character.setInsideZone(ZoneId.PVP, false);
			
			if (character instanceof Player)
			{
				character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
			}
		}
	}
	
	public final void updateZoneStatusForCharactersInside()
	{
		if (_task == null)
			return;
		
		final boolean battleStarted = _task.isBattleStarted();
		final SystemMessage sm = SystemMessage.getSystemMessage((battleStarted) ? SystemMessageId.ENTERED_COMBAT_ZONE : SystemMessageId.LEFT_COMBAT_ZONE);
		
		for (Creature character : _characters.values())
		{
			if (battleStarted)
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character instanceof Player)
					character.sendPacket(sm);
			}
			else
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character instanceof Player)
				{
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		_task = task;
	}
	
	public final void broadcastStatusUpdate(Player player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (Player plyr : getKnownTypeInside(Player.class))
		{
			if (plyr.isInObserverMode() || plyr.getOlympiadSide() != player.getOlympiadSide())
				plyr.sendPacket(packet);
		}
	}
	
	public final void broadcastPacketToObservers(L2GameServerPacket packet)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.isInObserverMode())
				player.sendPacket(packet);
		}
	}
}