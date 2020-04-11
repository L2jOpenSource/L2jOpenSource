package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.serverpackets.Revive;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

import main.data.memory.ObjectData;
import main.engine.events.cooperative.AbstractCooperative;
import main.engine.events.cooperative.EventCooperativeManager;
import main.holders.objects.PlayerHolder;

/**
 * @author programmos
 */
public final class RequestRestartPoint extends L2GameClientPacket
{
	static final Logger LOGGER = Logger.getLogger(RequestRestartPoint.class);
	
	protected int requestedPointType;
	protected boolean continuation;
	
	@Override
	protected void readImpl()
	{
		requestedPointType = readD();
	}
	
	class DeathTask implements Runnable
	{
		L2PcInstance activeChar;
		
		DeathTask(final L2PcInstance activeChar)
		{
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (activeChar.inEventTvT && TvT.isStarted() || activeChar.inEventDM && DM.isStarted() || activeChar.inEventCTF && CTF.isStarted())
			{
				activeChar.sendMessage("You can't restart in Event!");
				return;
			}
			
			try
			{
				Location loc = null;
				Castle castle = null;
				Fort fort = null;
				
				if (activeChar.isInJail())
				{
					requestedPointType = 27;
				}
				else if (activeChar.isFestivalParticipant())
				{
					requestedPointType = 4;
				}
				
				if (activeChar.isPhoenixBlessed())
				{
					activeChar.stopPhoenixBlessing(null);
				}
				
				switch (requestedPointType)
				{
					case 1: // to clanhall
						
						if (activeChar.getClan() != null)
						{
							if (activeChar.getClan().getHasHideout() == 0)
							{
								// cheater
								activeChar.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
							
							loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
							
							if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
							{
								activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
							}
							
							break;
						}
						
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
						break;
					case 2: // to castle
						
						Boolean isInDefense = false;
						castle = CastleManager.getInstance().getCastle(activeChar);
						fort = FortManager.getInstance().getFort(activeChar);
						MapRegionTable.TeleportWhereType teleportWhere = MapRegionTable.TeleportWhereType.Town;
						
						if (castle != null && castle.getSiege().getIsInProgress())
						{
							// siege in progress
							if (castle.getSiege().checkIsDefender(activeChar.getClan()))
							{
								isInDefense = true;
							}
						}
						
						if (fort != null && fort.getSiege().getIsInProgress())
						{
							// siege in progress
							if (fort.getSiege().checkIsDefender(activeChar.getClan()))
							{
								isInDefense = true;
							}
						}
						
						if (activeChar.getClan().getCastleId() == 0 && activeChar.getClan().getHasFort() == 0 && !isInDefense)
						{
							// cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						
						if (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null)
						{
							teleportWhere = MapRegionTable.TeleportWhereType.Castle;
						}
						else if (FortManager.getInstance().getFortByOwner(activeChar.getClan()) != null)
						{
							teleportWhere = MapRegionTable.TeleportWhereType.Fortress;
						}
						
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, teleportWhere);
						break;
					
					case 3: // to siege HQ
						
						L2SiegeClan siegeClan = null;
						castle = CastleManager.getInstance().getCastle(activeChar);
						fort = FortManager.getInstance().getFort(activeChar);
						
						if (castle != null && castle.getSiege().getIsInProgress())
						{
							siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
						}
						else if (fort != null && fort.getSiege().getIsInProgress())
						{
							siegeClan = fort.getSiege().getAttackerClan(activeChar.getClan());
						}
						
						if (siegeClan == null || siegeClan.getFlag().size() == 0)
						{
							// cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
						break;
					
					case 4: // Fixed or Player is a festival participant
						
						if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
						{
							// cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						
						loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
						break;
					
					case 27: // to jail
						
						if (!activeChar.isInJail())
						{
							return;
						}
						
						loc = new Location(-114356, -249645, -2984);
						break;
					
					default:
						
						if (activeChar.getKarma() > 0 && Config.ALT_KARMA_TELEPORT_TO_FLORAN)
						{
							loc = new Location(17836, 170178, -3507);// Floran Village
							break;
						}
						
						loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
						break;
				}
				
				// Stand up and teleport, proof dvp video.
				activeChar.setIsIn7sDungeon(false);
				activeChar.setIsPendingRevive(true);
				activeChar.teleToLocation(loc, true);
			}
			catch (Exception e)
			{
				LOGGER.error("DeathTask.run : Error while trying to determinate a point to revive a player.", e);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		AbstractCooperative curEvent = EventCooperativeManager.getCurrentEvent();
		PlayerHolder ph = ObjectData.get(PlayerHolder.class, activeChar);
		if (curEvent != null && curEvent.playerInEvent(ph))
		{
			return;
		}
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(null);
			activeChar.broadcastPacket(new Revive(activeChar));
			return;
		}
		else if (!activeChar.isAlikeDead())
		{
			LOGGER.warn("Living player [" + activeChar.getName() + "] called RestartPointPacket! Ban this player!");
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
				return;
			}
		}
		// run immediately (no need to schedule)
		new DeathTask(activeChar).run();
	}
	
	@Override
	public String getType()
	{
		return "[C] 6d RequestRestartPoint";
	}
}