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
package l2r.gameserver.model.entity.clanhall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.Announcements;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.NpcTable;
import l2r.gameserver.enums.SiegeClanType;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.Siegable;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author BiggBoss
 */
public abstract class ClanHallSiegeEngine extends Quest implements Siegable
{
	private static final String SQL_LOAD_ATTACKERS = "SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?";
	private static final String SQL_SAVE_ATTACKERS = "INSERT INTO clanhall_siege_attackers VALUES (?,?)";
	private static final String SQL_LOAD_GUARDS = "SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?";
	
	public static final int FORTRESS_RESSISTANCE = 21;
	public static final int DEVASTATED_CASTLE = 34;
	public static final int BANDIT_STRONGHOLD = 35;
	public static final int RAINBOW_SPRINGS = 62;
	public static final int BEAST_FARM = 63;
	public static final int FORTRESS_OF_DEAD = 64;
	
	protected final Logger _log;
	
	private final FastMap<Integer, L2SiegeClan> _attackers = new FastMap<>();
	private FastList<L2Spawn> _guards;
	
	public SiegableHall _hall;
	public ScheduledFuture<?> _siegeTask;
	public boolean _missionAccomplished = false;
	
	public ClanHallSiegeEngine(int questId, String name, String descr, final int hallId)
	{
		super(questId, name, descr);
		_log = LoggerFactory.getLogger(getClass().getName());
		
		_hall = CHSiegeManager.getInstance().getSiegableHall(hallId);
		_hall.setSiege(this);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.info(_hall.getName() + " siege scheduled for: " + getSiegeDate().getTime());
		loadAttackers();
	}
	
	// XXX Load methods -------------------------------
	
	public void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_LOAD_ATTACKERS))
		{
			statement.setInt(1, _hall.getId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("attacker_id");
					L2SiegeClan clan = new L2SiegeClan(id, SiegeClanType.ATTACKER);
					_attackers.put(id, clan);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getName() + ": Could not load siege attackers!:");
		}
	}
	
	public final void saveAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement delStatement = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?"))
		{
			delStatement.setInt(1, _hall.getId());
			delStatement.execute();
			
			if (getAttackers().size() > 0)
			{
				try (PreparedStatement insert = con.prepareStatement(SQL_SAVE_ATTACKERS))
				{
					for (L2SiegeClan clan : getAttackers().values())
					{
						insert.setInt(1, _hall.getId());
						insert.setInt(2, clan.getClanId());
						insert.execute();
						insert.clearParameters();
					}
				}
			}
			_log.info(getName() + ": Sucessfully saved attackers down to database!");
		}
		catch (Exception e)
		{
			_log.warn(getName() + ": Couldnt save attacker list!");
		}
	}
	
	public final void loadGuards()
	{
		if (_guards == null)
		{
			_guards = new FastList<>();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SQL_LOAD_GUARDS))
			{
				statement.setInt(1, _hall.getId());
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						final int npcId = rset.getInt("npcId");
						final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
						L2Spawn spawn = new L2Spawn(template);
						spawn.setX(rset.getInt("x"));
						spawn.setY(rset.getInt("y"));
						spawn.setZ(rset.getInt("z"));
						spawn.setHeading(rset.getInt("heading"));
						spawn.setRespawnDelay(rset.getInt("respawnDelay"));
						spawn.setAmount(1);
						_guards.add(spawn);
					}
				}
			}
			catch (Exception e)
			{
				_log.warn(getName() + ": Couldnt load siege guards!:");
			}
		}
	}
	
	// XXX Npc Management methods ----------------------------
	
	private final void spawnSiegeGuards()
	{
		for (L2Spawn guard : _guards)
		{
			if (guard != null)
			{
				guard.init();
			}
		}
	}
	
	private final void unSpawnSiegeGuards()
	{
		if ((_guards != null) && (_guards.size() > 0))
		{
			for (L2Spawn guard : _guards)
			{
				if (guard != null)
				{
					guard.stopRespawn();
					guard.getLastSpawn().deleteMe();
				}
			}
		}
	}
	
	@Override
	public List<L2Npc> getFlag(L2Clan clan)
	{
		List<L2Npc> result = null;
		L2SiegeClan sClan = getAttackerClan(clan);
		if (sClan != null)
		{
			result = sClan.getFlag();
		}
		return result;
	}
	
	// XXX Attacker clans management -----------------------------
	
	public final FastMap<Integer, L2SiegeClan> getAttackers()
	{
		return _attackers;
	}
	
	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		if (clan == null)
		{
			return false;
		}
		
		return _attackers.containsKey(clan.getId());
	}
	
	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return false;
	}
	
	@Override
	public L2SiegeClan getAttackerClan(int clanId)
	{
		return _attackers.get(clanId);
	}
	
	@Override
	public L2SiegeClan getAttackerClan(L2Clan clan)
	{
		return getAttackerClan(clan.getId());
	}
	
	@Override
	public List<L2SiegeClan> getAttackerClans()
	{
		FastList<L2SiegeClan> result = new FastList<>();
		result.addAll(_attackers.values());
		return result;
	}
	
	@Override
	public List<L2PcInstance> getAttackersInZone()
	{
		final Collection<L2PcInstance> list = _hall.getSiegeZone().getPlayersInside();
		List<L2PcInstance> attackers = new FastList<>();
		
		for (L2PcInstance pc : list)
		{
			final L2Clan clan = pc.getClan();
			if ((clan != null) && getAttackers().containsKey(clan.getId()))
			{
				attackers.add(pc);
			}
		}
		
		return attackers;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}
	
	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	// XXX Siege execution --------------------------
	
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			final L2SiegeClan clan = new L2SiegeClan(_hall.getOwnerId(), SiegeClanType.ATTACKER);
			getAttackers().put(clan.getClanId(), new L2SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}
		
		_hall.free();
		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if ((getAttackers().size() < 1) && (_hall.getId() != 21)) // Fortress of resistance dont have attacker list
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getSiegeDate().getTimeInMillis());
			_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Announcements.getInstance().announceToAll(sm);
			return;
		}
		
		_hall.spawnDoor();
		loadGuards();
		spawnSiegeGuards();
		_hall.updateSiegeZone(true);
		
		final byte state = 1;
		for (L2SiegeClan sClan : getAttackerClans())
		{
			final L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if (clan == null)
			{
				continue;
			}
			
			for (L2PcInstance pc : clan.getOnlineMembers(0))
			{
				if (pc != null)
				{
					pc.setSiegeState(state);
					pc.broadcastUserInfo();
					pc.setIsInHideoutSiege(true);
				}
			}
		}
		
		_hall.updateSiegeStatus(SiegeStatus.RUNNING);
		onSiegeStarts();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEnds(), _hall.getSiegeLenght());
	}
	
	@Override
	public void endSiege()
	{
		SystemMessage end = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
		end.addString(_hall.getName());
		Announcements.getInstance().announceToAll(end);
		
		L2Clan winner = getWinner();
		SystemMessage finalMsg = null;
		if (_missionAccomplished && (winner != null))
		{
			_hall.setOwner(winner);
			winner.setHideoutId(_hall.getId());
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
			finalMsg.addString(winner.getName());
			finalMsg.addString(_hall.getName());
			Announcements.getInstance().announceToAll(finalMsg);
		}
		else
		{
			finalMsg = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
			finalMsg.addString(_hall.getName());
			Announcements.getInstance().announceToAll(finalMsg);
		}
		_missionAccomplished = false;
		
		_hall.updateSiegeZone(false);
		_hall.updateNextSiege();
		_hall.spawnDoor(false);
		_hall.banishForeigners();
		
		final byte state = 0;
		for (L2SiegeClan sClan : getAttackerClans())
		{
			final L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if (clan == null)
			{
				continue;
			}
			
			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				player.setSiegeState(state);
				player.broadcastUserInfo();
				player.setIsInHideoutSiege(false);
			}
		}
		
		// Update pvp flag for winners when siege zone becomes inactive
		for (L2Character chr : _hall.getSiegeZone().getCharactersInside())
		{
			if ((chr != null) && chr.isPlayer())
			{
				chr.getActingPlayer().startPvPFlag();
			}
		}
		
		getAttackers().clear();
		
		onSiegeEnds();
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.info("Siege of " + _hall.getName() + " scheduled for: " + _hall.getSiegeDate().getTime());
		
		_hall.updateSiegeStatus(SiegeStatus.REGISTERING);
		unSpawnSiegeGuards();
	}
	
	@Override
	public void updateSiege()
	{
		cancelSiegeTask();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - 3600000);
		_log.info(_hall.getName() + " siege scheduled for: " + _hall.getSiegeDate().getTime().toString());
	}
	
	public void cancelSiegeTask()
	{
		if (_siegeTask != null)
		{
			_siegeTask.cancel(false);
		}
	}
	
	@Override
	public Calendar getSiegeDate()
	{
		return _hall.getSiegeDate();
	}
	
	// XXX Fame settings ---------------------------
	
	@Override
	public boolean giveFame()
	{
		return Config.CHS_ENABLE_FAME;
	}
	
	@Override
	public int getFameAmount()
	{
		return Config.CHS_FAME_AMOUNT;
	}
	
	// XXX Misc methods -----------------------------
	
	@Override
	public int getFameFrequency()
	{
		return Config.CHS_FAME_FREQUENCY;
	}
	
	public final void broadcastNpcSay(final L2Npc npc, final int type, final NpcStringId messageId)
	{
		final NpcSay npcSay = new NpcSay(npc.getObjectId(), type, npc.getId(), messageId);
		int sourceRegion = MapRegionManager.getInstance().getMapRegionLocId(npc);
		final Collection<L2PcInstance> charsInside = L2World.getInstance().getPlayers();
		
		for (L2PcInstance pc : charsInside)
		{
			if ((pc != null) && (MapRegionManager.getInstance().getMapRegionLocId(pc) == sourceRegion))
			{
				pc.sendPacket(npcSay);
			}
		}
	}
	
	// XXX Siege task and abstract methods -------------------
	public Location getInnerSpawnLoc(L2PcInstance player)
	{
		return null;
	}
	
	public boolean canPlantFlag()
	{
		return true;
	}
	
	public boolean doorIsAutoAttackable()
	{
		return true;
	}
	
	public void onSiegeStarts()
	{
	}
	
	public void onSiegeEnds()
	{
	}
	
	public abstract L2Clan getWinner();
	
	public class PrepareOwner implements Runnable
	{
		@Override
		public void run()
		{
			prepareOwner();
		}
	}
	
	public class SiegeStarts implements Runnable
	{
		@Override
		public void run()
		{
			startSiege();
		}
	}
	
	public class SiegeEnds implements Runnable
	{
		@Override
		public void run()
		{
			endSiege();
		}
	}
}