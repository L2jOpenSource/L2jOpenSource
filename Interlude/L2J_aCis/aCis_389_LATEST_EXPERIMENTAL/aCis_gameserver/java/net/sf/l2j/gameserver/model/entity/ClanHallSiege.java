package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.logging.CLogger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.SiegeStatus;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;

public abstract class ClanHallSiege extends Quest implements Siegable
{
	protected static final CLogger LOGGER = new CLogger(ClanHallSiege.class.getName());
	
	private static final String SQL_LOAD_ATTACKERS = "SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?";
	private static final String SQL_SAVE_ATTACKERS = "INSERT INTO clanhall_siege_attackers VALUES (?,?)";
	private static final String SQL_LOAD_GUARDS = "SELECT * FROM clanhall_siege_guards WHERE clanHallId = ?";
	
	public static final int FORTRESS_OF_RESISTANCE = 21;
	public static final int DEVASTATED_CASTLE = 34;
	public static final int BANDIT_STRONGHOLD = 35;
	public static final int RAINBOW_SPRINGS = 62;
	public static final int BEAST_FARM = 63;
	public static final int FORTRESS_OF_DEAD = 64;
	
	private final Map<Integer, Clan> _attackers = new ConcurrentHashMap<>();
	private List<Spawn> _guards;
	
	public SiegableHall _hall;
	public ScheduledFuture<?> _siegeTask;
	public boolean _missionAccomplished = false;
	
	public ClanHallSiege(String descr, final int hallId)
	{
		super(-1, descr);
		
		_hall = ClanHallManager.getInstance().getSiegableHall(hallId);
		_hall.setSiege(this);
		
		_siegeTask = ThreadPool.schedule(() -> prepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		
		loadAttackers();
		
		LOGGER.info("{} siege scheduled for {}.", _hall.getName(), getSiegeDate().getTime());
	}
	
	public void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_LOAD_ATTACKERS))
		{
			ps.setInt(1, _hall.getId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("attacker_id");
					final Clan clan = ClanTable.getInstance().getClan(id);
					if (clan != null)
						_attackers.put(id, clan);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("{}: Could not load siege attackers!", getName(), e);
		}
	}
	
	public final void saveAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?"))
		{
			ps.setInt(1, _hall.getId());
			ps.execute();
			
			if (_attackers.size() > 0)
			{
				try (PreparedStatement insert = con.prepareStatement(SQL_SAVE_ATTACKERS))
				{
					for (Clan clan : _attackers.values())
					{
						insert.setInt(1, _hall.getId());
						insert.setInt(2, clan.getClanId());
						insert.addBatch();
					}
					insert.executeBatch();
				}
			}
			LOGGER.info("{}: Successfully saved attackers to database.", getName());
		}
		catch (Exception e)
		{
			LOGGER.warn("{}: Couldnt save attacker list!", getName(), e);
		}
	}
	
	public final void loadGuards()
	{
		if (_guards == null)
		{
			_guards = new ArrayList<>();
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SQL_LOAD_GUARDS))
			{
				ps.setInt(1, _hall.getId());
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						final Spawn spawn = new Spawn(rset.getInt("npcId"));
						spawn.setLoc(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), rset.getInt("heading"));
						spawn.setRespawnDelay(rset.getInt("respawnDelay"));
						
						_guards.add(spawn);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("{}: Couldn't load siege guards!", getName(), e);
			}
		}
	}
	
	private final void spawnSiegeGuards()
	{
		for (Spawn spawn : _guards)
		{
			spawn.setRespawnState(true);
			spawn.doSpawn(false);
		}
	}
	
	private final void unSpawnSiegeGuards()
	{
		if (_guards != null)
		{
			for (Spawn spawn : _guards)
			{
				spawn.setRespawnState(false);
				
				final Npc last = spawn.getNpc();
				if (last != null)
					last.deleteMe();
			}
		}
	}
	
	@Override
	public Npc getFlag(Clan clan)
	{
		return (clan != null) ? clan.getFlag() : null;
	}
	
	@Override
	public List<Clan> getAttackerClans()
	{
		return new ArrayList<>(_attackers.values());
	}
	
	@Override
	public boolean checkSide(Clan clan, SiegeSide type)
	{
		return true;
	}
	
	@Override
	public boolean checkSides(Clan clan, SiegeSide... types)
	{
		return true;
	}
	
	@Override
	public boolean checkSides(Clan clan)
	{
		return true;
	}
	
	public List<Player> getAttackersInZone()
	{
		final List<Player> attackers = new ArrayList<>();
		for (Player player : _hall.getSiegeZone().getKnownTypeInside(Player.class))
		{
			final Clan clan = player.getClan();
			if (clan != null && _attackers.containsKey(clan.getClanId()))
				attackers.add(player);
		}
		return attackers;
	}
	
	@Override
	public List<Clan> getDefenderClans()
	{
		return null;
	}
	
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			final Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
			if (clan != null)
				_attackers.put(clan.getClanId(), clan);
		}
		
		_hall.free();
		_hall.banishForeigners();
		
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED).addString(getName()));
		
		_hall.updateSiegeStatus(SiegeStatus.REGISTRATION_OVER);
		
		_siegeTask = ThreadPool.schedule(() -> startSiege(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if (_attackers.size() < 1 && _hall.getId() != 21) // Fortress of resistance don't have attacker list
		{
			onSiegeEnds();
			
			_attackers.clear();
			
			_hall.updateNextSiege();
			
			_siegeTask = ThreadPool.schedule(() -> prepareOwner(), _hall.getSiegeDate().getTimeInMillis());
			
			_hall.updateSiegeStatus(SiegeStatus.REGISTRATION_OVER);
			
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(_hall.getName()));
			return;
		}
		
		_hall.spawnDoor();
		
		loadGuards();
		spawnSiegeGuards();
		
		_hall.getSiegeZone().setActive(true);
		
		final byte state = 1;
		for (Clan clan : _attackers.values())
		{
			for (Player player : clan.getOnlineMembers())
			{
				player.setSiegeState(state);
				player.broadcastUserInfo();
				player.setInSiegableHallSiege(true);
			}
		}
		
		_hall.updateSiegeStatus(SiegeStatus.IN_PROGRESS);
		onSiegeStarts();
		_siegeTask = ThreadPool.schedule(new SiegeEnds(), _hall.getSiegeLength());
	}
	
	@Override
	public void endSiege()
	{
		World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED).addString(_hall.getName()));
		
		final Clan winner = getWinner();
		
		if (_missionAccomplished && (winner != null))
		{
			_hall.setOwner(winner);
			
			winner.setClanHallId(_hall.getId());
			
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE).addString(winner.getName()).addString(_hall.getName()));
		}
		else
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW).addString(_hall.getName()));
		
		_missionAccomplished = false;
		
		_hall.getSiegeZone().setActive(false);
		
		_hall.updateNextSiege();
		_hall.spawnDoor(false);
		_hall.banishForeigners();
		
		final byte state = 0;
		for (Clan clan : _attackers.values())
		{
			for (Player player : clan.getOnlineMembers())
			{
				player.setSiegeState(state);
				player.broadcastUserInfo();
				player.setInSiegableHallSiege(false);
			}
		}
		
		// Update pvp flag for winners when siege zone becomes inactive
		for (Player player : _hall.getSiegeZone().getKnownTypeInside(Player.class))
			player.updatePvPStatus();
		
		_attackers.clear();
		
		onSiegeEnds();
		
		_siegeTask = ThreadPool.schedule(() -> prepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		LOGGER.info("Siege of {} scheduled for {}.", _hall.getName(), _hall.getSiegeDate().getTime());
		
		_hall.updateSiegeStatus(SiegeStatus.REGISTRATION_OPENED);
		unSpawnSiegeGuards();
	}
	
	public void updateSiege()
	{
		cancelSiegeTask();
		
		_siegeTask = ThreadPool.schedule(() -> prepareOwner(), _hall.getNextSiegeTime() - 3600000);
		
		LOGGER.info("{} siege scheduled for {}.", _hall.getName(), _hall.getSiegeDate().getTime());
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
	
	public final static void broadcastNpcSay(final Npc npc, final SayType type, final String messageId)
	{
		final NpcSay npcSay = new NpcSay(npc, type, messageId);
		final int region = MapRegionData.getInstance().getMapRegion(npc.getX(), npc.getY());
		
		for (Player pc : World.getInstance().getPlayers())
		{
			if (MapRegionData.getInstance().getMapRegion(pc.getX(), pc.getY()) == region)
				pc.sendPacket(npcSay);
		}
	}
	
	public Location getInnerSpawnLoc(Player player)
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
	
	public abstract Clan getWinner();
	
	public class SiegeEnds implements Runnable
	{
		@Override
		public void run()
		{
			endSiege();
		}
	}
}