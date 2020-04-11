/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package conquerablehalls.flagwar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.L2SpecialSiegeGuardAI;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.SiegeClanType;
import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2r.gameserver.model.entity.clanhall.SiegeStatus;
import l2r.gameserver.model.zone.type.L2ResidenceHallTeleportZone;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

/**
 * @author BiggBoss
 */
public abstract class FlagWar extends ClanHallSiegeEngine
{
	private static final String SQL_LOAD_ATTACKERS = "SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_SAVE_ATTACKER = "INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)";
	private static final String SQL_LOAD_MEMEBERS = "SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?";
	private static final String SQL_SAVE_CLAN = "INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE flag=?";
	private static final String SQL_SAVE_NPC = "UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?";
	private static final String SQL_CLEAR_CLAN = "DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_CLEAR_CLAN_ATTACKERS = "DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?";
	
	protected static int ROYAL_FLAG;
	protected static int FLAG_RED;
	protected static int FLAG_YELLOW;
	protected static int FLAG_GREEN;
	protected static int FLAG_BLUE;
	protected static int FLAG_PURPLE;
	
	protected static int ALLY_1;
	protected static int ALLY_2;
	protected static int ALLY_3;
	protected static int ALLY_4;
	protected static int ALLY_5;
	
	protected static int TELEPORT_1;
	
	protected static int MESSENGER;
	
	protected static int[] OUTTER_DOORS_TO_OPEN = new int[2];
	protected static int[] INNER_DOORS_TO_OPEN = new int[2];
	protected static Location[] FLAG_COORDS = new Location[7];
	
	protected static L2ResidenceHallTeleportZone[] TELE_ZONES = new L2ResidenceHallTeleportZone[6];
	
	protected static int QUEST_REWARD;
	
	protected static Location CENTER;
	
	protected Map<Integer, ClanData> _data = new ConcurrentHashMap<>(6);
	protected L2Clan _winner;
	private boolean _firstPhase = true;
	
	public FlagWar(String name, int hallId)
	{
		super(name, "conquerablehalls/flagwar", hallId);
		addStartNpc(MESSENGER);
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);
		
		for (int i = 0; i < 6; i++)
		{
			addFirstTalkId(TELEPORT_1 + i);
		}
		
		addKillId(ALLY_1);
		addKillId(ALLY_2);
		addKillId(ALLY_3);
		addKillId(ALLY_4);
		addKillId(ALLY_5);
		
		addSpawnId(ALLY_1);
		addSpawnId(ALLY_2);
		addSpawnId(ALLY_3);
		addSpawnId(ALLY_4);
		addSpawnId(ALLY_5);
		
		// If siege ends w/ more than 1 flag alive, winner is old owner
		_winner = ClanTable.getInstance().getClan(_hall.getOwnerId());
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String html = null;
		if (npc.getId() == MESSENGER)
		{
			if (!checkIsAttacker(player.getClan()))
			{
				L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
				String content = getHtm(player, player.getHtmlPrefix(), "messenger_initial.htm");
				content = content.replaceAll("%clanName%", (clan == null) ? "no owner" : clan.getName());
				content = content.replaceAll("%objectId%", String.valueOf(npc.getObjectId()));
				html = content;
			}
			else
			{
				html = "messenger_initial.htm";
			}
		}
		else
		{
			int index = npc.getId() - TELEPORT_1;
			if ((index == 0) && _firstPhase)
			{
				html = "teleporter_notyet.htm";
			}
			else
			{
				TELE_ZONES[index].checkTeleporTask();
				html = "teleporter.htm";
			}
		}
		return html;
	}
	
	@Override
	public synchronized String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String html = event;
		L2Clan clan = player.getClan();
		
		if (event.startsWith("register_clan")) // Register the clan for the siege
		{
			if (!_hall.isRegistering())
			{
				if (_hall.isInSiege())
				{
					html = "messenger_registrationpassed.htm";
				}
				else
				{
					sendRegistrationPageDate(player);
					return null;
				}
			}
			else if ((clan == null) || !player.isClanLeader())
			{
				html = "messenger_notclannotleader.htm";
			}
			else if (getAttackers().size() >= 5)
			{
				html = "messenger_attackersqueuefull.htm";
			}
			else if (checkIsAttacker(clan))
			{
				html = "messenger_clanalreadyregistered.htm";
			}
			else if (_hall.getOwnerId() == clan.getId())
			{
				html = "messenger_curownermessage.htm";
			}
			else
			{
				String[] arg = event.split(" ");
				if (arg.length >= 2)
				{
					// Register passing the quest
					if (arg[1].equals("wQuest"))
					{
						if (player.destroyItemByItemId(_hall.getName() + " Siege", QUEST_REWARD, 1, npc, false)) // Quest passed
						{
							registerClan(clan);
							html = getFlagHtml(_data.get(clan.getId()).flag);
						}
						else
						{
							html = "messenger_noquest.htm";
						}
					}
					// Register paying the fee
					else if (arg[1].equals("wFee") && canPayRegistration())
					{
						if (player.reduceAdena(getName() + " Siege", 200000, npc, false)) // Fee payed
						{
							registerClan(clan);
							html = getFlagHtml(_data.get(clan.getId()).flag);
						}
						else
						{
							html = "messenger_nomoney.htm";
						}
					}
				}
			}
		}
		// Select the flag to defend
		else if (event.startsWith("select_clan_npc"))
		{
			if (!player.isClanLeader())
			{
				html = "messenger_onlyleaderselectally.htm";
			}
			else if (!_data.containsKey(clan.getId()))
			{
				html = "messenger_clannotregistered.htm";
			}
			else
			{
				String[] var = event.split(" ");
				if (var.length >= 2)
				{
					int id = 0;
					try
					{
						id = Integer.parseInt(var[1]);
					}
					catch (Exception e)
					{
						_log.warn(getName() + "->select_clan_npc->Wrong mahum warrior id: " + var[1]);
					}
					if ((id > 0) && ((html = getAllyHtml(id)) != null))
					{
						_data.get(clan.getId()).npc = id;
						saveNpc(id, clan.getId());
					}
				}
				else
				{
					_log.warn(getName() + " Siege: Not enough parameters to save clan npc for clan: " + clan.getName());
				}
			}
		}
		// View (and change ? ) the current selected mahum warrior
		else if (event.startsWith("view_clan_npc"))
		{
			ClanData cd = null;
			if (clan == null)
			{
				html = "messenger_clannotregistered.htm";
			}
			else if ((cd = _data.get(clan.getId())) == null)
			{
				html = "messenger_notclannotleader.htm";
			}
			else if (cd.npc == 0)
			{
				html = "messenger_leaderdidnotchooseyet.htm";
			}
			else
			{
				html = getAllyHtml(cd.npc);
			}
		}
		// Register a clan member for the fight
		else if (event.equals("register_member"))
		{
			if (clan == null)
			{
				html = "messenger_clannotregistered.htm";
			}
			else if (!_hall.isRegistering())
			{
				html = "messenger_registrationpassed.htm";
			}
			else if (!_data.containsKey(clan.getId()))
			{
				html = "messenger_notclannotleader.htm";
			}
			else if (_data.get(clan.getId()).players.size() >= 18)
			{
				html = "messenger_clanqueuefull.htm";
			}
			else
			{
				ClanData data = _data.get(clan.getId());
				data.players.add(player.getObjectId());
				saveMember(clan.getId(), player.getObjectId());
				if (data.npc == 0)
				{
					html = "messenger_leaderdidnotchooseyet.htm";
				}
				else
				{
					html = "messenger_clanregistered.htm";
				}
			}
		}
		// Show cur attacker list
		else if (event.equals("view_attacker_list"))
		{
			if (_hall.isRegistering())
			{
				sendRegistrationPageDate(player);
			}
			else
			{
				html = getHtm(player, player.getHtmlPrefix(), "messenger_registeredclans.htm");
				int i = 0;
				for (Entry<Integer, ClanData> clanData : _data.entrySet())
				{
					L2Clan attacker = ClanTable.getInstance().getClan(clanData.getKey());
					if (attacker == null)
					{
						continue;
					}
					html = html.replaceAll("%clan" + i + "%", clan.getName());
					html = html.replaceAll("%clanMem" + i + "%", String.valueOf(clanData.getValue().players.size()));
					i++;
				}
				if (_data.size() < 5)
				{
					for (int c = _data.size(); c < 5; c++)
					{
						html = html.replaceAll("%clan" + c + "%", "Empty pos. ");
						html = html.replaceAll("%clanMem" + c + "%", "Empty pos. ");
					}
				}
			}
		}
		
		return html;
	}
	
	@Override
	public synchronized String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (_hall.isInSiege())
		{
			final int npcId = npc.getId();
			for (int keys : _data.keySet())
			{
				if (_data.get(keys).npc == npcId)
				{
					removeParticipant(keys, true);
				}
			}
			
			synchronized (this)
			{
				final List<Integer> clanIds = new ArrayList<>(_data.keySet());
				if (_firstPhase)
				{
					// Siege ends if just 1 flag is alive
					// Hall was free before battle or owner didn't set the ally npc
					if (((clanIds.size() == 1) && (_hall.getOwnerId() <= 0)) || (_data.get(clanIds.get(0)).npc == 0))
					{
						_missionAccomplished = true;
						_winner = ClanTable.getInstance().getClan(clanIds.get(0));
						removeParticipant(clanIds.get(0), false);
						cancelSiegeTask();
						endSiege();
					}
					else if ((_data.size() == 2) && (_hall.getOwnerId() > 0)) // Hall has defender (owner)
					{
						cancelSiegeTask(); // No time limit now
						_firstPhase = false;
						_hall.getSiegeZone().setIsActive(false);
						for (int doorId : INNER_DOORS_TO_OPEN)
						{
							_hall.openCloseDoor(doorId, true);
						}
						
						for (ClanData data : _data.values())
						{
							doUnSpawns(data);
						}
						
						ThreadPoolManager.getInstance().scheduleGeneral(() ->
						{
							for (int doorId : INNER_DOORS_TO_OPEN)
							{
								_hall.openCloseDoor(doorId, false);
							}
							
							for (Entry<Integer, ClanData> e : _data.entrySet())
							{
								doSpawns(e.getKey(), e.getValue());
							}
							
							_hall.getSiegeZone().setIsActive(true);
						} , 300000);
					}
				}
				else
				{
					_missionAccomplished = true;
					_winner = ClanTable.getInstance().getClan(clanIds.get(0));
					removeParticipant(clanIds.get(0), false);
					endSiege();
				}
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CENTER);
		return null;
	}
	
	@Override
	public L2Clan getWinner()
	{
		return _winner;
	}
	
	@Override
	public void prepareOwner()
	{
		if (_hall.getOwnerId() > 0)
		{
			registerClan(ClanTable.getInstance().getClan(_hall.getOwnerId()));
		}
		
		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Broadcast.toAllOnlinePlayers(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege()
	{
		if (getAttackers().size() < 2)
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Broadcast.toAllOnlinePlayers(sm);
			return;
		}
		
		// Open doors for challengers
		for (int door : OUTTER_DOORS_TO_OPEN)
		{
			_hall.openCloseDoor(door, true);
		}
		
		// Teleport owner inside
		if (_hall.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(_hall.getOwnerId());
			final Location loc = _hall.getZone().getSpawns().get(0); // Owner restart point
			for (L2ClanMember pc : owner.getMembers())
			{
				if (pc != null)
				{
					final L2PcInstance player = pc.getPlayerInstance();
					if ((player != null) && player.isOnline())
					{
						player.teleToLocation(loc, false);
					}
				}
			}
		}
		
		// Schedule open doors closement, banish non siege participants and<br>
		// siege start in 2 minutes
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			for (int door : OUTTER_DOORS_TO_OPEN)
			{
				_hall.openCloseDoor(door, false);
			}
			
			_hall.getZone().banishNonSiegeParticipants();
			
			super.startSiege();
		} , 300000);
	}
	
	@Override
	public void onSiegeStarts()
	{
		for (Entry<Integer, ClanData> clan : _data.entrySet())
		{
			// Spawns challengers flags and npcs
			try
			{
				ClanData data = clan.getValue();
				doSpawns(clan.getKey(), data);
				fillPlayerList(data);
			}
			catch (Exception e)
			{
				endSiege();
				_log.warn("{}: Problems in siege initialization!", getName(), e);
			}
		}
	}
	
	@Override
	public void endSiege()
	{
		if (_hall.getOwnerId() > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
			clan.setHideoutId(0);
			_hall.free();
		}
		super.endSiege();
	}
	
	@Override
	public void onSiegeEnds()
	{
		if (_data.size() > 0)
		{
			for (int clanId : _data.keySet())
			{
				if (_hall.getOwnerId() == clanId)
				{
					removeParticipant(clanId, false);
				}
				else
				{
					removeParticipant(clanId, true);
				}
			}
		}
		clearTables();
	}
	
	@Override
	public final Location getInnerSpawnLoc(final L2PcInstance player)
	{
		Location loc = null;
		if (player.getClanId() == _hall.getOwnerId())
		{
			loc = _hall.getZone().getSpawns().get(0);
		}
		else
		{
			ClanData cd = _data.get(player.getClanId());
			if (cd != null)
			{
				int index = cd.flag - FLAG_RED;
				if ((index >= 0) && (index <= 4))
				{
					loc = _hall.getZone().getChallengerSpawns().get(index);
				}
				else
				{
					throw new ArrayIndexOutOfBoundsException();
				}
			}
		}
		return loc;
	}
	
	@Override
	public final boolean canPlantFlag()
	{
		return false;
	}
	
	@Override
	public final boolean doorIsAutoAttackable()
	{
		return false;
	}
	
	void doSpawns(int clanId, ClanData data)
	{
		try
		{
			int index = 0;
			if (_firstPhase)
			{
				index = data.flag - ROYAL_FLAG;
			}
			else
			{
				index = clanId == _hall.getOwnerId() ? 5 : 6;
			}
			Location loc = FLAG_COORDS[index];
			
			data.flagInstance = new L2Spawn(data.flag);
			data.flagInstance.setLocation(loc);
			data.flagInstance.setRespawnDelay(10000);
			data.flagInstance.setAmount(1);
			data.flagInstance.init();
			
			if (data.npc > 0)
			{
				data.warrior = new L2Spawn(data.npc);
				data.warrior.setLocation(loc);
				data.warrior.setRespawnDelay(10000);
				data.warrior.setAmount(1);
				data.warrior.init();
				
				// vGodFather: small trick-hack for now!
				data.warrior.getLastSpawn().setAI(new L2SpecialSiegeGuardAI((L2DefenderInstance) data.warrior.getLastSpawn()));
				((L2SpecialSiegeGuardAI) data.warrior.getLastSpawn().getAI()).getAlly().addAll(data.players);
			}
		}
		catch (Exception e)
		{
			_log.warn("{}: Could not make clan spawns!", getName(), e);
		}
	}
	
	private void fillPlayerList(ClanData data)
	{
		for (int objId : data.players)
		{
			L2PcInstance plr = L2World.getInstance().getPlayer(objId);
			if (plr != null)
			{
				data.playersInstance.add(plr);
			}
		}
	}
	
	private void registerClan(L2Clan clan)
	{
		final int clanId = clan.getId();
		
		L2SiegeClan sc = new L2SiegeClan(clanId, SiegeClanType.ATTACKER);
		getAttackers().put(clanId, sc);
		
		ClanData data = new ClanData();
		
		if (_hall.getOwnerId() == clan.getId())
		{
			data.flag = ROYAL_FLAG;
		}
		else
		{
			data.flag = ROYAL_FLAG + _data.size() + 1;
		}
		
		data.players.add(clan.getLeaderId());
		_data.put(clanId, data);
		
		saveClan(clanId, data.flag);
		saveMember(clanId, clan.getLeaderId());
	}
	
	private final void doUnSpawns(ClanData data)
	{
		if (data.flagInstance != null)
		{
			data.flagInstance.stopRespawn();
			data.flagInstance.getLastSpawn().deleteMe();
		}
		if (data.warrior != null)
		{
			data.warrior.stopRespawn();
			data.warrior.getLastSpawn().deleteMe();
		}
	}
	
	private final void removeParticipant(int clanId, boolean teleport)
	{
		ClanData dat = _data.remove(clanId);
		
		if (dat != null)
		{
			// Destroy clan flag
			if (dat.flagInstance != null)
			{
				dat.flagInstance.stopRespawn();
				if (dat.flagInstance.getLastSpawn() != null)
				{
					dat.flagInstance.getLastSpawn().deleteMe();
				}
			}
			
			if (dat.warrior != null)
			{
				// Destroy clan warrior
				dat.warrior.stopRespawn();
				if (dat.warrior.getLastSpawn() != null)
				{
					dat.warrior.getLastSpawn().deleteMe();
				}
			}
			
			dat.players.clear();
			
			if (teleport)
			{
				// Teleport players outside
				for (L2PcInstance pc : dat.playersInstance)
				{
					if (pc != null)
					{
						pc.teleToLocation(TeleportWhereType.TOWN);
					}
				}
			}
			
			dat.playersInstance.clear();
		}
	}
	
	public boolean canPayRegistration()
	{
		return true;
	}
	
	private void sendRegistrationPageDate(L2PcInstance player)
	{
		final NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setHtml(getHtm(player, player.getHtmlPrefix(), "siege_date.htm"));
		msg.replace("%nextSiege%", _hall.getSiegeDate().getTime().toString());
		player.sendPacket(msg);
	}
	
	public abstract String getFlagHtml(int flag);
	
	public abstract String getAllyHtml(int ally);
	
	@Override
	public final void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_LOAD_ATTACKERS))
		{
			ps.setInt(1, _hall.getId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int clanId = rset.getInt("clan_id");
					
					if (ClanTable.getInstance().getClan(clanId) == null)
					{
						_log.warn("{}: Loaded an unexistent clan as attacker! Clan ID {}!", getName(), clanId);
						continue;
					}
					
					ClanData data = new ClanData();
					data.flag = rset.getInt("flag");
					data.npc = rset.getInt("npc");
					
					// vGodFather just in case
					if (_data == null)
					{
						_data = new HashMap<>();
					}
					_data.put(clanId, data);
					loadAttackerMembers(clanId);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("Could not load attackers for {}!", getName(), e);
		}
	}
	
	private final void loadAttackerMembers(int clanId)
	{
		final List<Integer> listInstance = _data.get(clanId).players;
		if (listInstance == null)
		{
			_log.warn(getName() + ": Tried to load unregistered clan with ID " + clanId);
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_LOAD_MEMEBERS))
		{
			ps.setInt(1, clanId);
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					listInstance.add(rset.getInt("object_id"));
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("{}: loadAttackerMembers", getName(), e);
		}
	}
	
	private final void saveClan(int clanId, int flag)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_SAVE_CLAN))
		{
			ps.setInt(1, _hall.getId());
			ps.setInt(2, flag);
			ps.setInt(3, 0);
			ps.setInt(4, clanId);
			
			ps.setInt(5, flag);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("{}: saveClan", getName(), e);
		}
	}
	
	private final void saveNpc(int npc, int clanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_SAVE_NPC))
		{
			ps.setInt(1, npc);
			ps.setInt(2, clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("{}: saveNpc()", getName(), e);
		}
	}
	
	private final void saveMember(int clanId, int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SQL_SAVE_ATTACKER))
		{
			ps.setInt(1, _hall.getId());
			ps.setInt(2, clanId);
			ps.setInt(3, objectId);
			ps.execute();
		}
		catch (Exception e)
		{
			_log.warn("{}: saveMember", getName(), e);
		}
	}
	
	private void clearTables()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(SQL_CLEAR_CLAN);
			PreparedStatement ps2 = con.prepareStatement(SQL_CLEAR_CLAN_ATTACKERS))
		{
			ps1.setInt(1, _hall.getId());
			ps1.execute();
			
			ps2.setInt(1, _hall.getId());
			ps2.execute();
		}
		catch (Exception e)
		{
			_log.warn("Unable to clear data tables for {}!", getName(), e);
		}
	}
	
	class ClanData
	{
		int flag = 0;
		int npc = 0;
		List<Integer> players = new ArrayList<>(18);
		List<L2PcInstance> playersInstance = new ArrayList<>(18);
		L2Spawn warrior = null;
		L2Spawn flagInstance = null;
	}
}
