package net.sf.l2j.gameserver.scripting.scripts.conquerablehalls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.enums.SiegeStatus;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Rainbow Springs Chateau is located north of the Hot Springs area. A new style of siege war has been applied to it, which is different from previous contested clan hall war styles.<br>
 * <br>
 * In order to own the clan hall, winning is decided through a mini-game. Only a clan level 3 or higher is allowed to participate and the maximum number of clans is four. Participants may have 5 or more member in their party.<br>
 * <br>
 * <b>How to Participate</b><br>
 * <br>
 * A clan leader in possession of a 'Rainbow Springs Clan Hall War Decree' may register to participate through a messenger NPC up to one hour before the contest begins. The top four clans, owning the highest number of Rainbow Springs Clan Hall War Decrees amongst them, will be chosen to compete and
 * their clan leaders notified through a system message.<br>
 * <br>
 * Rainbow Springs Clan Hall War Decrees may be obtained by fishing in the Hot Springs area of Goddard. However, a player must use Hot Springs Bait for fishing bait.<br>
 * <br>
 * If a participating clan requests disorganization in the middle of a siege war registration period, the siege war participation registration is automatically cancelled. However, the certificates are not returned at this time.<br>
 * <br>
 * A participating clan may cancel its participation up until the end of the registration period. If this happens, the clan will only get back half of their submitted certificates.<br>
 * <br>
 * The clan that owns the Rainbow Springs Chateau is automatically registered for the next contest. In this case, the top three clans, owning the highest number of Rainbow Springs Clan Hall War Decrees amongst them, will be chosen to compete.<br>
 * <br>
 * <b>Battle Method</b><br>
 * <br>
 * A Rainbow Springs Chateau siege war is held at the east arena of Rainbow Springs.<br>
 * <br>
 * After clans are selected to compete, the contest is comprised of a one-hour waiting period, a contest time of 58 minutes, and an end time of 2 minutes.<br>
 * <br>
 * Registered clans must organize one party with five or more members, with their clan leader as the party leader. The party must move to the arena before the siege war starts and enter the arena through the Caretaker. The surrounding area of the arena is a Peaceful Zone.<br>
 * <br>
 * Participating clans enter into different game fields respectively and through the Caretaker.<br>
 * <br>
 * Upon entering the arena, all buffs that are active on all participating characters will be removed, and summoned pet/servitors will disappear and cannot be summoned back into the arena.<br>
 * <br>
 * Once the contest begins, a Hot Springs Yeti and a Hot Springs Gourd appear in the center of the arena and treasure chests appear randomly throughout the arena.<br>
 * <br>
 * The Hot Springs Yeti announces the game rules and helps guide players.<br>
 * <br>
 * All of the Hot Springs Yeti's shouts may be collected and then exchanged with/for various Hot Springs items. Text exchange or Hot Springs item use is only available to the clan leader and clan members.<br>
 * <br>
 * Participants can break the spawned treasure chests in the arena and can acquire text passages. Text items cannot be exchanged; therefore to give them to other characters, they should be dropped first and then picked up.<br>
 * <br>
 * Treasure Chests can only be damaged with your bare hands, however Mimics can be attacked with P. Atk and M. Atk.<br>
 * <br>
 * When using Hot Springs Nectar, an Enraged Yeti is spawned at certain intervals and interrupts the game. The Enraged Yeti can be attacked with P. Atk. and M. Atk.<br>
 * <br>
 * The Hot Springs items may be used when the Hot Springs Yeti is a target, and they can reduce the HP of the gourd, cast debuff on other teams, change the gourd, or recover.<br>
 * <br>
 * The team that first destroys the Hot Springs Gourd wins the game. All the participants are then teleported outside of the arena after a 2-minute delay.<br>
 * <br>
 * At the end of the game, a manager NPC appears and cleans the arena by picking up all the item drops.<br>
 * <br>
 * The game continues even if the server goes down in the middle of the mini-game.
 */
public final class RainbowSpringsChateau extends ClanHallSiege
{
	private static final int RAINBOW_SPRINGS = 62;
	
	private static final int WAR_DECREES = 8034;
	private static final int RAINBOW_NECTAR = 8030;
	private static final int RAINBOW_MWATER = 8031;
	private static final int RAINBOW_WATER = 8032;
	private static final int RAINBOW_SULFUR = 8033;
	
	private static final int MESSENGER = 35604;
	private static final int CARETAKER = 35603;
	private static final int CHEST = 35593;
	
	private static final int[] GOURDS =
	{
		35588,
		35589,
		35590,
		35591
	};
	
	private static final int[] YETIS =
	{
		35596,
		35597,
		35598,
		35599
	};
	
	private static final Location[] ARENAS = new Location[]
	{
		new Location(151562, -127080, -2214), // Arena 1
		new Location(153141, -125335, -2214), // Arena 2
		new Location(153892, -127530, -2214), // Arena 3
		new Location(155657, -125752, -2214), // Arena 4
	};
	
	protected static final int[] ARENA_ZONES =
	{
		112081,
		112082,
		112083,
		112084
	};
	
	private static final String[] TEXT_PASSAGES =
	{
		"Fight for Rainbow Springs!",
		"Are you a match for the Yetti?",
		"Did somebody order a knuckle sandwich?"
	};
	
	private static final L2Skill[] DEBUFFS = {};
	
	private final Map<Integer, Integer> _warDecreesCount = new HashMap<>();
	private final List<Clan> _acceptedClans = new ArrayList<>(4);
	private final Map<String, ArrayList<Clan>> _usedTextPassages = new HashMap<>();
	private final Map<Clan, Integer> _pendingItemToGet = new ConcurrentHashMap<>();
	
	private SiegableHall _rainbow;
	private ScheduledFuture<?> _nextSiege, _siegeEnd;
	private String _registrationEnds;
	private Spawn[] _gourds = new Spawn[4];
	
	public Clan _winner;
	
	public RainbowSpringsChateau()
	{
		super("conquerablehalls", RAINBOW_SPRINGS);
		
		_rainbow = ClanHallManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		if (_rainbow == null)
			return;
		
		addFirstTalkId(MESSENGER, CARETAKER);
		addTalkId(MESSENGER, CARETAKER);
		addFirstTalkId(YETIS);
		addTalkId(YETIS);
		
		loadAttackers();
		
		long delay = _rainbow.getNextSiegeTime();
		if (delay > -1)
		{
			setRegistrationEndString(delay - 3600000);
			
			_nextSiege = ThreadPool.schedule(new SetFinalAttackers(), delay);
		}
		else
			LOGGER.warn("No date was set for Rainbow Springs Chateau siege. Siege is canceled.");
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String html = "";
		
		final int npcId = npc.getNpcId();
		if (npcId == MESSENGER)
		{
			final String main = (_rainbow.getOwnerId() > 0) ? "messenger_yetti001.htm" : "messenger_yetti001a.htm";
			html = HtmCache.getInstance().getHtm("data/html/scripts/conquerablehalls/RainbowSpringsChateau/" + main);
			html = html.replace("%time%", _registrationEnds);
			
			if (_rainbow.getOwnerId() > 0)
				html = html.replace("%owner%", ClanTable.getInstance().getClan(_rainbow.getOwnerId()).getName());
		}
		else if (npcId == CARETAKER)
			html = (_rainbow.isInSiege()) ? "game_manager003.htm" : "game_manager001.htm";
		else if (ArraysUtil.contains(YETIS, npcId))
		{
			if (_rainbow.isInSiege())
			{
				if (!player.isClanLeader())
					html = "no_clan_leader.htm";
				else
				{
					final Clan clan = player.getClan();
					if (_acceptedClans.contains(clan))
					{
						int index = _acceptedClans.indexOf(clan);
						if (npcId == YETIS[index])
							html = "yeti_main.htm";
					}
				}
			}
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return html;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String html = event;
		
		final Clan clan = player.getClan();
		
		switch (npc.getNpcId())
		{
			case MESSENGER:
				switch (event)
				{
					case "register":
						if (!player.isClanLeader())
							html = "messenger_yetti010.htm";
						else if (clan.getCastleId() > 0 || clan.getClanHallId() > 0)
							html = "messenger_yetti012.htm";
						else if (!_rainbow.isRegistering())
							html = "messenger_yetti014.htm";
						else if (_warDecreesCount.containsKey(clan.getClanId()))
							html = "messenger_yetti013.htm";
						else if (clan.getLevel() < 3 || clan.getMembersCount() < 5)
							html = "messenger_yetti011.htm";
						else
						{
							final ItemInstance warDecrees = player.getInventory().getItemByItemId(WAR_DECREES);
							if (warDecrees == null)
								html = "messenger_yetti008.htm";
							else
							{
								int count = warDecrees.getCount();
								
								_warDecreesCount.put(clan.getClanId(), count);
								
								player.destroyItem("Rainbow Springs Registration", warDecrees, npc, true);
								
								addAttacker(clan.getClanId(), count);
								
								html = "messenger_yetti009.htm";
							}
						}
						break;
					
					case "cancel":
						if (!player.isClanLeader())
							html = "messenger_yetti010.htm";
						else if (!_warDecreesCount.containsKey(clan.getClanId()))
							html = "messenger_yetti016.htm";
						else if (!_rainbow.isRegistering())
							html = "messenger_yetti017.htm";
						else
						{
							removeAttacker(clan.getClanId());
							html = "messenger_yetti018.htm";
						}
						break;
					
					case "unregister":
						if (_rainbow.isRegistering())
						{
							if (_warDecreesCount.containsKey(clan.getClanId()))
							{
								player.addItem("Rainbow Spring unregister", WAR_DECREES, _warDecreesCount.get(clan.getClanId()) / 2, npc, true);
								_warDecreesCount.remove(clan.getClanId());
								html = "messenger_yetti019.htm";
							}
							else
								html = "messenger_yetti020.htm";
						}
						else if (_rainbow.isWaitingBattle())
						{
							_acceptedClans.remove(clan);
							
							html = "messenger_yetti020.htm";
						}
						break;
				}
				break;
			
			case CARETAKER:
				if (event.equals("portToArena"))
				{
					final Party party = player.getParty();
					if (clan == null)
						html = "game_manager009.htm";
					else if (!player.isClanLeader())
						html = "game_manager004.htm";
					else if (!player.isInParty())
						html = "game_manager005.htm";
					else if (party.getLeaderObjectId() != player.getObjectId())
						html = "game_manager006.htm";
					else
					{
						final int clanId = player.getClanId();
						boolean nonClanMemberInParty = false;
						for (Player member : party.getMembers())
						{
							if (member.getClanId() != clanId)
							{
								nonClanMemberInParty = true;
								break;
							}
						}
						
						if (nonClanMemberInParty)
							html = "game_manager007.htm";
						else if (party.getMembersCount() < 5)
							html = "game_manager008.htm";
						else if (clan.getCastleId() > 0 || clan.getClanHallId() > 0)
							html = "game_manager010.htm";
						else if (clan.getLevel() < Config.CH_MINIMUM_CLAN_LEVEL)
							html = "game_manager011.htm";
						// else if () // Something about the rules.
						// html = "game_manager012.htm";
						// else if () // Already registered.
						// html = "game_manager013.htm";
						else if (!_acceptedClans.contains(clan))
							html = "game_manager014.htm";
						// else if () // Not have enough cards to register.
						// html = "game_manager015.htm";
						else
							portToArena(player, _acceptedClans.indexOf(clan));
					}
				}
				break;
		}
		
		if (event.startsWith("enterText"))
		{
			// Shouldn't happen
			if (!_acceptedClans.contains(clan))
				return null;
			
			String[] split = event.split("_ ");
			if (split.length < 2)
				return null;
			
			final String passage = split[1];
			if (!isValidPassage(passage))
				return null;
			
			if (_usedTextPassages.containsKey(passage))
			{
				ArrayList<Clan> list = _usedTextPassages.get(passage);
				
				if (list.contains(clan))
					html = "yeti_passage_used.htm";
				else
				{
					list.add(clan);
					
					if (_pendingItemToGet.containsKey(clan))
					{
						int left = _pendingItemToGet.get(clan);
						++left;
						_pendingItemToGet.put(clan, left);
					}
					else
					{
						_pendingItemToGet.put(clan, 1);
					}
					
					html = "yeti_item_exchange.htm";
				}
			}
		}
		// TODO(Zoey76): Rewrite this to prevent exploits...
		// else if (event.startsWith("getItem"))
		// {
		// if (!_pendingItemToGet.containsKey(clan))
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		//
		// int left = _pendingItemToGet.get(clan);
		// if (left > 0)
		// {
		// int itemId = Integer.parseInt(event.split("_")[1]);
		// player.addItem("Rainbow Spring Chateau Siege", itemId, 1, npc, true);
		// --left;
		// _pendingItemToGet.put(clan, left);
		// html = "yeti_main.htm";
		// }
		// else
		// {
		// html = "yeti_cannot_exchange.htm";
		// }
		// }
		
		return html;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		if (!_rainbow.isInSiege() || !(killer instanceof Playable))
			return null;
		
		final Clan clan = killer.getActingPlayer().getClan();
		if (clan == null || !_acceptedClans.contains(clan))
			return null;
		
		final int npcId = npc.getNpcId();
		final int index = _acceptedClans.indexOf(clan);
		
		if (npcId == CHEST)
		{
			shoutRandomText(npc);
		}
		else if (npcId == GOURDS[index])
		{
			if (_siegeEnd != null)
			{
				_siegeEnd.cancel(false);
				_siegeEnd = null;
			}
			ThreadPool.execute(new SiegeEnd(clan));
		}
		return null;
	}
	
	@Override
	public String onItemUse(ItemInstance item, Player player, WorldObject target)
	{
		if (!_rainbow.isInSiege())
			return null;
		
		if (target == null || !(target instanceof Npc))
			return null;
		
		final int npcId = ((Npc) target).getNpcId();
		if (!ArraysUtil.contains(YETIS, npcId))
			return null;
		
		final Clan clan = player.getClan();
		if (clan == null || !_acceptedClans.contains(clan))
			return null;
			
		// Nectar must spawn the enraged yeti. Dunno if it makes any other thing
		// Also, the items must execute:
		// - Reduce gourd hp
		// - Cast debuffs on enemy clans
		// - Change arena gourds
		// - Increase gourd hp
		
		final int itemId = item.getItemId();
		if (itemId == RAINBOW_NECTAR)
		{
			final Spawn gourd = _gourds[_acceptedClans.indexOf(clan)];
			if (gourd.getNpc() != null)
				gourd.getNpc().reduceCurrentHp(1000, player, null);
		}
		else if (itemId == RAINBOW_MWATER)
		{
			final Spawn gourd = _gourds[_acceptedClans.indexOf(clan)];
			
			final Npc gourdNpc = gourd.getNpc();
			if (gourdNpc != null)
			{
				gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000);
			}
		}
		else if (itemId == RAINBOW_WATER)
		{
			Spawn[] tempArray = _gourds;
			int iterator = _acceptedClans.size();
			for (int i = 0; i < iterator; i++)
			{
				Spawn oldSpawn = _gourds[(iterator - 1) - i];
				Spawn curSpawn = tempArray[i];
				
				_gourds[(iterator - 1) - i] = curSpawn;
				
				if (curSpawn.getNpc() != null)
					curSpawn.getNpc().teleportTo(oldSpawn.getLoc(), 0);
			}
		}
		else if (itemId == RAINBOW_SULFUR)
		{
			for (int id : ARENA_ZONES)
			{
				if (id == _acceptedClans.indexOf(clan))
					continue;
				
				final ZoneType zone = ZoneManager.getInstance().getZoneById(id);
				for (Creature creature : zone.getCharacters())
				{
					for (L2Skill sk : DEBUFFS)
						sk.getEffects(creature, creature);
				}
			}
		}
		return null;
	}
	
	@Override
	public Clan getWinner()
	{
		return _winner;
	}
	
	private static void portToArena(Player leader, int arena)
	{
		if (arena < 0 || arena > 3)
			return;
		
		for (Player player : leader.getParty().getMembers())
		{
			player.stopAllEffects();
			
			if (player.getSummon() != null)
				player.getSummon().unSummon(player);
			
			player.teleToLocation(ARENAS[arena]);
		}
	}
	
	protected void spawnGourds()
	{
		for (int i = 0; i < _acceptedClans.size(); i++)
		{
			if (_gourds[i] == null)
			{
				try
				{
					_gourds[i] = new Spawn(GOURDS[i]);
					_gourds[i].setLoc(ARENAS[i].getX() + 150, ARENAS[i].getY() + 150, ARENAS[i].getZ(), 1);
				}
				catch (Exception e)
				{
					LOGGER.error("Failed to initialize a spawn.", e);
				}
			}
			SpawnTable.getInstance().addSpawn(_gourds[i], false);
			_gourds[i].doSpawn(false);
		}
	}
	
	private void shoutRandomText(Npc npc)
	{
		int length = TEXT_PASSAGES.length;
		
		if (_usedTextPassages.size() >= length)
			return;
		
		String message = Rnd.get(TEXT_PASSAGES);
		
		if (_usedTextPassages.containsKey(message))
			shoutRandomText(npc);
		else
		{
			_usedTextPassages.put(message, new ArrayList<Clan>());
			
			npc.broadcastPacket(new NpcSay(npc, SayType.SHOUT, message));
		}
	}
	
	private static boolean isValidPassage(String text)
	{
		for (String st : TEXT_PASSAGES)
		{
			if (st.equalsIgnoreCase(text))
				return true;
		}
		return false;
	}
	
	private static void removeAttacker(int clanId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE clanId = ?"))
		{
			ps.setInt(1, clanId);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't remove attacker.", e);
		}
	}
	
	private static void addAttacker(int clanId, int count)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)"))
		{
			ps.setInt(1, clanId);
			ps.setInt(2, count);
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't add attacker.", e);
		}
	}
	
	@Override
	public void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rset = s.executeQuery("SELECT * FROM rainbowsprings_attacker_list"))
		{
			while (rset.next())
				_warDecreesCount.put(rset.getInt("clan_id"), rset.getInt("decrees_count"));
		}
		catch (Exception e)
		{
			LOGGER.error("Couldn't load attackers.", e);
		}
	}
	
	protected void setRegistrationEndString(long time)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR);
		int mins = c.get(Calendar.MINUTE);
		
		_registrationEnds = year + "-" + month + "-" + day + " " + hour + (mins < 10 ? ":0" : ":") + mins;
	}
	
	public void launchSiege()
	{
		if (_nextSiege != null)
		{
			_nextSiege.cancel(false);
			_nextSiege = null;
		}
		ThreadPool.execute(new SiegeStart());
	}
	
	@Override
	public void endSiege()
	{
		if (_siegeEnd != null)
		{
			_siegeEnd.cancel(false);
			_siegeEnd = null;
		}
		ThreadPool.execute(new SiegeEnd(null));
	}
	
	protected class SetFinalAttackers implements Runnable
	{
		@Override
		public void run()
		{
			int spotLeft = 4;
			if (_rainbow.getOwnerId() > 0)
			{
				Clan owner = ClanTable.getInstance().getClan(_rainbow.getOwnerId());
				if (owner != null)
				{
					_rainbow.free();
					
					owner.setClanHallId(0);
					
					_acceptedClans.add(owner);
					
					--spotLeft;
				}
				
				for (int i = 0; i < spotLeft; i++)
				{
					long counter = 0;
					
					Clan clan = null;
					for (int clanId : _warDecreesCount.keySet())
					{
						Clan actingClan = ClanTable.getInstance().getClan(clanId);
						if (actingClan == null || actingClan.getDissolvingExpiryTime() > 0)
						{
							_warDecreesCount.remove(clanId);
							continue;
						}
						
						final long count = _warDecreesCount.get(clanId);
						if (count > counter)
						{
							counter = count;
							clan = actingClan;
						}
					}
					
					if (clan != null && _acceptedClans.size() < 4)
					{
						_acceptedClans.add(clan);
						
						final Player leader = clan.getLeader().getPlayerInstance();
						if (leader != null)
							leader.sendMessage("Your clan has been accepted to join the RainBow Srpings Chateau siege!");
					}
				}
				
				if (_acceptedClans.size() >= 2)
				{
					_nextSiege = ThreadPool.schedule(new SiegeStart(), 3600000);
					_rainbow.updateSiegeStatus(SiegeStatus.REGISTRATION_OVER);
				}
				else
					World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addString(_hall.getName()));
			}
		}
	}
	
	protected class SiegeStart implements Runnable
	{
		@Override
		public void run()
		{
			spawnGourds();
			
			_siegeEnd = ThreadPool.schedule(new SiegeEnd(null), _rainbow.getSiegeLength() - 120000);
		}
	}
	
	private class SiegeEnd implements Runnable
	{
		private final Clan _winner;
		
		protected SiegeEnd(Clan winner)
		{
			_winner = winner;
		}
		
		@Override
		public void run()
		{
			// Unspawn gourds.
			for (int i = 0; i < _acceptedClans.size(); i++)
			{
				if (_gourds[i].getNpc() != null)
					_gourds[i].getNpc().deleteMe();
				
				SpawnTable.getInstance().deleteSpawn(_gourds[i], false);
			}
			
			if (_winner != null)
			{
				_rainbow.setOwner(_winner);
			}
			
			// XXX _rainbow.siegeEnds();
			
			ThreadPool.schedule(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
			setRegistrationEndString((_rainbow.getNextSiegeTime() + System.currentTimeMillis()) - 3600000);
			
			// Teleport out of the arenas is made 2 mins after game ends
			ThreadPool.schedule(() ->
			{
				for (int arenaId : ARENA_ZONES)
				{
					for (Creature chr : ZoneManager.getInstance().getZoneById(arenaId).getCharacters())
						chr.teleportTo(TeleportType.TOWN);
				}
			}, 120000);
		}
	}
}