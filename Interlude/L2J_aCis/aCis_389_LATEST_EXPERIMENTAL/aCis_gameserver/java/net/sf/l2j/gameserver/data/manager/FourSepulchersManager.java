package net.sf.l2j.gameserver.data.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.SepulcherNpc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.QuestState;

public class FourSepulchersManager
{
	private static enum State
	{
		ENTRY,
		ATTACK,
		END
	}
	
	protected static final CLogger LOGGER = new CLogger(FourSepulchersManager.class.getName());
	
	private static final String QUEST_ID = "Q620_FourGoblets";
	
	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;
	
	private static final int[] GATEKEEPER_IDS =
	{
		31929,
		31934,
		31939,
		31944
	};
	
	private static final int[][][] SHADOW_LOCS =
	{
		{
			{
				25339,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25349,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25346,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25342,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25342,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25339,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25349,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25346,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25346,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25342,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25339,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25349,
				175591,
				-72744,
				-7215,
				49317
			}
		},
		{
			{
				25349,
				191231,
				-85574,
				-7216,
				33380
			},
			{
				25346,
				189534,
				-88969,
				-7216,
				32768
			},
			{
				25342,
				173195,
				-76560,
				-7215,
				49277
			},
			{
				25339,
				175591,
				-72744,
				-7215,
				49317
			}
		},
	};
	
	private final Map<Integer, Boolean> _archonSpawned = new HashMap<>();
	private final Map<Integer, Boolean> _hallInUse = new HashMap<>();
	private final Map<Integer, Location> _startHallSpawns = new HashMap<>();
	private final Map<Integer, Integer> _hallGateKeepers = new HashMap<>();
	private final Map<Integer, Integer> _keyBoxNpc = new HashMap<>();
	private final Map<Integer, Integer> _victim = new HashMap<>();
	private final Map<Integer, Spawn> _executionerSpawns = new HashMap<>();
	private final Map<Integer, Spawn> _keyBoxSpawns = new HashMap<>();
	private final Map<Integer, Spawn> _mysteriousBoxSpawns = new HashMap<>();
	private final Map<Integer, Spawn> _shadowSpawns = new HashMap<>();
	private final Map<Integer, List<Spawn>> _dukeFinalMobs = new HashMap<>();
	private final Map<Integer, List<Npc>> _dukeMobs = new HashMap<>();
	private final Map<Integer, List<Spawn>> _emperorsGraveNpcs = new HashMap<>();
	private final Map<Integer, List<Spawn>> _magicalMonsters = new HashMap<>();
	private final Map<Integer, List<Spawn>> _physicalMonsters = new HashMap<>();
	private final Map<Integer, List<Npc>> _viscountMobs = new HashMap<>();
	
	private final List<Spawn> _managers = new ArrayList<>();
	private final List<Npc> _allMobs = new ArrayList<>();
	
	protected State _state = State.ENTRY;
	
	protected FourSepulchersManager()
	{
		initFixedInfo();
		loadMysteriousBox();
		initKeyBoxSpawns();
		
		loadSpawnsByType(1);
		loadSpawnsByType(2);
		
		initShadowSpawns();
		initExecutionerSpawns();
		
		loadSpawnsByType(5);
		loadSpawnsByType(6);
		
		spawnManagers();
		
		launchCycle();
	}
	
	protected void initFixedInfo()
	{
		_startHallSpawns.put(31921, new Location(181632, -85587, -7218));
		_startHallSpawns.put(31922, new Location(179963, -88978, -7218));
		_startHallSpawns.put(31923, new Location(173217, -86132, -7218));
		_startHallSpawns.put(31924, new Location(175608, -82296, -7218));
		
		_hallInUse.put(31921, false);
		_hallInUse.put(31922, false);
		_hallInUse.put(31923, false);
		_hallInUse.put(31924, false);
		
		_hallGateKeepers.put(31925, 25150012);
		_hallGateKeepers.put(31926, 25150013);
		_hallGateKeepers.put(31927, 25150014);
		_hallGateKeepers.put(31928, 25150015);
		_hallGateKeepers.put(31929, 25150016);
		_hallGateKeepers.put(31930, 25150002);
		_hallGateKeepers.put(31931, 25150003);
		_hallGateKeepers.put(31932, 25150004);
		_hallGateKeepers.put(31933, 25150005);
		_hallGateKeepers.put(31934, 25150006);
		_hallGateKeepers.put(31935, 25150032);
		_hallGateKeepers.put(31936, 25150033);
		_hallGateKeepers.put(31937, 25150034);
		_hallGateKeepers.put(31938, 25150035);
		_hallGateKeepers.put(31939, 25150036);
		_hallGateKeepers.put(31940, 25150022);
		_hallGateKeepers.put(31941, 25150023);
		_hallGateKeepers.put(31942, 25150024);
		_hallGateKeepers.put(31943, 25150025);
		_hallGateKeepers.put(31944, 25150026);
		
		_keyBoxNpc.put(18120, 31455);
		_keyBoxNpc.put(18121, 31455);
		_keyBoxNpc.put(18122, 31455);
		_keyBoxNpc.put(18123, 31455);
		_keyBoxNpc.put(18124, 31456);
		_keyBoxNpc.put(18125, 31456);
		_keyBoxNpc.put(18126, 31456);
		_keyBoxNpc.put(18127, 31456);
		_keyBoxNpc.put(18128, 31457);
		_keyBoxNpc.put(18129, 31457);
		_keyBoxNpc.put(18130, 31457);
		_keyBoxNpc.put(18131, 31457);
		_keyBoxNpc.put(18149, 31458);
		_keyBoxNpc.put(18150, 31459);
		_keyBoxNpc.put(18151, 31459);
		_keyBoxNpc.put(18152, 31459);
		_keyBoxNpc.put(18153, 31459);
		_keyBoxNpc.put(18154, 31460);
		_keyBoxNpc.put(18155, 31460);
		_keyBoxNpc.put(18156, 31460);
		_keyBoxNpc.put(18157, 31460);
		_keyBoxNpc.put(18158, 31461);
		_keyBoxNpc.put(18159, 31461);
		_keyBoxNpc.put(18160, 31461);
		_keyBoxNpc.put(18161, 31461);
		_keyBoxNpc.put(18162, 31462);
		_keyBoxNpc.put(18163, 31462);
		_keyBoxNpc.put(18164, 31462);
		_keyBoxNpc.put(18165, 31462);
		_keyBoxNpc.put(18183, 31463);
		_keyBoxNpc.put(18184, 31464);
		_keyBoxNpc.put(18212, 31465);
		_keyBoxNpc.put(18213, 31465);
		_keyBoxNpc.put(18214, 31465);
		_keyBoxNpc.put(18215, 31465);
		_keyBoxNpc.put(18216, 31466);
		_keyBoxNpc.put(18217, 31466);
		_keyBoxNpc.put(18218, 31466);
		_keyBoxNpc.put(18219, 31466);
		
		_victim.put(18150, 18158);
		_victim.put(18151, 18159);
		_victim.put(18152, 18160);
		_victim.put(18153, 18161);
		_victim.put(18154, 18162);
		_victim.put(18155, 18163);
		_victim.put(18156, 18164);
		_victim.put(18157, 18165);
	}
	
	private void loadMysteriousBox()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE spawntype = 0 ORDER BY id");
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(rs.getInt("npc_templateid"));
				if (template == null)
				{
					LOGGER.warn("Data missing in NPC table for ID: {}.", rs.getInt("npc_templateid"));
					continue;
				}
				
				final Spawn spawn = new Spawn(template);
				spawn.setLoc(rs.getInt("locx"), rs.getInt("locy"), rs.getInt("locz"), rs.getInt("heading"));
				spawn.setRespawnDelay(rs.getInt("respawn_delay"));
				
				SpawnTable.getInstance().addSpawn(spawn, false);
				
				_mysteriousBoxSpawns.put(rs.getInt("key_npc_id"), spawn);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to initialize a spawn.", e);
		}
		LOGGER.info("Loaded {} Mysterious-Box.", _mysteriousBoxSpawns.size());
	}
	
	private void initKeyBoxSpawns()
	{
		for (Entry<Integer, Integer> keyNpc : _keyBoxNpc.entrySet())
		{
			try
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(keyNpc.getValue());
				if (template == null)
				{
					LOGGER.warn("Data missing in NPC table for ID: {}.", keyNpc.getValue());
					continue;
				}
				
				final Spawn spawn = new Spawn(template);
				SpawnTable.getInstance().addSpawn(spawn, false);
				_keyBoxSpawns.put(keyNpc.getKey(), spawn);
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to initialize a spawn.", e);
			}
		}
		LOGGER.info("Loaded {} Key-Box.", _keyBoxNpc.size());
	}
	
	private void loadSpawnsByType(int type)
	{
		int loaded = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id"))
		{
			ps.setInt(1, type);
			
			try (ResultSet rs = ps.executeQuery();
				PreparedStatement ps2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id"))
			{
				while (rs.next())
				{
					final int keyNpcId = rs.getInt("key_npc_id");
					
					// Feed the second statement.
					ps2.setInt(1, keyNpcId);
					ps2.setInt(2, type);
					
					try (ResultSet rs2 = ps2.executeQuery())
					{
						// Clear parameters of the second statement.
						ps2.clearParameters();
						
						// Generate a new List, which will be stored as value on the Map.
						final List<Spawn> spawns = new ArrayList<>();
						
						while (rs2.next())
						{
							final NpcTemplate template = NpcData.getInstance().getTemplate(rs2.getInt("npc_templateid"));
							if (template == null)
							{
								LOGGER.warn("Data missing in NPC table for ID: {}.", rs2.getInt("npc_templateid"));
								continue;
							}
							
							final Spawn spawn = new Spawn(template);
							spawn.setLoc(rs2.getInt("locx"), rs2.getInt("locy"), rs2.getInt("locz"), rs2.getInt("heading"));
							spawn.setRespawnDelay(rs2.getInt("respawn_delay"));
							
							SpawnTable.getInstance().addSpawn(spawn, false);
							spawns.add(spawn);
							
							loaded++;
						}
						
						if (type == 1)
							_physicalMonsters.put(keyNpcId, spawns);
						else if (type == 2)
							_magicalMonsters.put(keyNpcId, spawns);
						else if (type == 5)
						{
							_dukeFinalMobs.put(keyNpcId, spawns);
							_archonSpawned.put(keyNpcId, false);
						}
						else if (type == 6)
							_emperorsGraveNpcs.put(keyNpcId, spawns);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to initialize a spawn.", e);
		}
		
		if (type == 1)
			LOGGER.info("Loaded {} physical type monsters.", loaded);
		else if (type == 2)
			LOGGER.info("Loaded {} magical type monsters.", loaded);
		else if (type == 5)
			LOGGER.info("Loaded {} Duke's Hall Gatekeepers.", loaded);
		else if (type == 6)
			LOGGER.info("Loaded {} Emperor's Grave monsters.", loaded);
	}
	
	private void initShadowSpawns()
	{
		// Reset spawns.
		_shadowSpawns.clear();
		
		// Randomize one set of Rooms.
		final int locNo = Rnd.get(4);
		
		for (int i = 0; i <= 3; i++)
		{
			// Retrieve npcId.
			final int npcId = SHADOW_LOCS[locNo][i][0];
			
			// Retrieve template.
			final NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template == null)
			{
				LOGGER.warn("Data missing in NPC table for ID: {}.", npcId);
				continue;
			}
			
			try
			{
				// Generate the spawn and set the location.
				final Spawn spawn = new Spawn(template);
				spawn.setLoc(SHADOW_LOCS[locNo][i][1], SHADOW_LOCS[locNo][i][2], SHADOW_LOCS[locNo][i][3], SHADOW_LOCS[locNo][i][4]);
				
				// Add the spawn in SpawnTable.
				SpawnTable.getInstance().addSpawn(spawn, false);
				
				// Add the spawn in _shadowSpawns.
				_shadowSpawns.put(GATEKEEPER_IDS[i], spawn);
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to initialize a spawn.", e);
			}
		}
		LOGGER.info("Loaded {} Shadows of Halisha.", _shadowSpawns.size());
	}
	
	private void initExecutionerSpawns()
	{
		for (Entry<Integer, Integer> victimNpc : _victim.entrySet())
		{
			try
			{
				final NpcTemplate template = NpcData.getInstance().getTemplate(victimNpc.getValue());
				if (template == null)
				{
					LOGGER.warn("Data missing in NPC table for ID: {}.", victimNpc.getValue());
					continue;
				}
				
				final Spawn spawn = new Spawn(template);
				
				SpawnTable.getInstance().addSpawn(spawn, false);
				_executionerSpawns.put(victimNpc.getKey(), spawn);
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to initialize a spawn.", e);
			}
		}
	}
	
	private void spawnManagers()
	{
		for (int i = 31921; i <= 31924; i++)
		{
			final NpcTemplate template = NpcData.getInstance().getTemplate(i);
			if (template == null)
				continue;
			
			try
			{
				final Spawn spawn = new Spawn(template);
				spawn.setRespawnDelay(60);
				
				switch (i)
				{
					case 31921: // conquerors
						spawn.setLoc(181061, -85595, -7200, -32584);
						break;
					
					case 31922: // emperors
						spawn.setLoc(179292, -88981, -7200, -33272);
						break;
					
					case 31923: // sages
						spawn.setLoc(173202, -87004, -7200, -16248);
						break;
					
					case 31924: // judges
						spawn.setLoc(175606, -82853, -7200, -16248);
						break;
				}
				
				SpawnTable.getInstance().addSpawn(spawn, false);
				spawn.doSpawn(false);
				spawn.setRespawnState(true);
				
				_managers.add(spawn);
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to spawn managers.", e);
			}
		}
		LOGGER.info("Loaded {} managers.", _managers.size());
	}
	
	private void launchCycle()
	{
		// Get time in future (one minute cleaned of seconds/milliseconds).
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		// Calculate the time needed to reach that minute, then fire an event every minute.
		ThreadPool.scheduleAtFixedRate(new Cycle(), cal.getTimeInMillis() - System.currentTimeMillis(), 60000);
	}
	
	public boolean isEntryTime()
	{
		return _state == State.ENTRY;
	}
	
	public boolean isAttackTime()
	{
		return _state == State.ATTACK;
	}
	
	public Map<Integer, Integer> getHallGateKeepers()
	{
		return _hallGateKeepers;
	}
	
	public synchronized void tryEntry(Npc npc, Player player)
	{
		final int npcId = npc.getNpcId();
		
		switch (npcId)
		{
			case 31921:
			case 31922:
			case 31923:
			case 31924:
				break;
			
			default:
				return;
		}
		
		if (_hallInUse.get(npcId))
		{
			showHtmlFile(player, npcId + "-FULL.htm", npc, null);
			return;
		}
		
		final Party party = player.getParty();
		if (party == null || party.getMembersCount() < Config.FS_PARTY_MEMBER_COUNT)
		{
			showHtmlFile(player, npcId + "-SP.htm", npc, null);
			return;
		}
		
		if (!party.isLeader(player))
		{
			showHtmlFile(player, npcId + "-NL.htm", npc, null);
			return;
		}
		
		for (Player member : party.getMembers())
		{
			final QuestState qs = member.getQuestState(QUEST_ID);
			if (qs == null || (!qs.isStarted() && !qs.isCompleted()))
			{
				showHtmlFile(player, npcId + "-NS.htm", npc, member);
				return;
			}
			
			if (member.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId + "-SE.htm", npc, member);
				return;
			}
			
			if (member.getWeightPenalty() > 2)
			{
				member.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return;
			}
		}
		
		if (!isEntryTime())
		{
			showHtmlFile(player, npcId + "-NE.htm", npc, null);
			return;
		}
		
		showHtmlFile(player, npcId + "-OK.htm", npc, null);
		
		final Location loc = _startHallSpawns.get(npcId);
		
		// For every party player who isn't dead and who is near current player.
		for (Player member : party.getMembers().stream().filter(m -> !m.isDead() && MathUtil.checkIfInRange(700, player, m, true)).collect(Collectors.toList()))
		{
			// Allow zone timer.
			ZoneManager.getInstance().getZone(loc.getX(), loc.getY(), loc.getZ(), BossZone.class).allowPlayerEntry(member, 30);
			
			// Teleport the player.
			member.teleportTo(loc, 80);
			
			// Delete entrance pass.
			member.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, member, true);
			
			// Add Used Pass if Antique Brooch wasn't possessed.
			if (member.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				member.addItem("Quest", USED_PASS, 1, member, true);
			
			// Delete all instances of Chapel Key.
			final ItemInstance key = member.getInventory().getItemByItemId(CHAPEL_KEY);
			if (key != null)
				member.destroyItemByItemId("Quest", CHAPEL_KEY, key.getCount(), member, true);
		}
		
		// Set the hall as being used.
		_hallInUse.put(npcId, true);
	}
	
	public void spawnMysteriousBox(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final Spawn spawn = _mysteriousBoxSpawns.get(npcId);
		if (spawn != null)
		{
			_allMobs.add(spawn.doSpawn(false));
			
			// Single spawn only.
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnMonster(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final List<Spawn> monsterList = (Rnd.nextBoolean()) ? _physicalMonsters.get(npcId) : _magicalMonsters.get(npcId);
		final List<Npc> mobs = new ArrayList<>();
		
		boolean spawnKeyBoxMob = false;
		boolean spawnedKeyBoxMob = false;
		
		for (Spawn spawn : monsterList)
		{
			if (spawnedKeyBoxMob)
				spawnKeyBoxMob = false;
			else
			{
				switch (npcId)
				{
					case 31469:
					case 31474:
					case 31479:
					case 31484:
						if (Rnd.get(48) == 0)
							spawnKeyBoxMob = true;
						break;
					
					default:
						spawnKeyBoxMob = false;
				}
			}
			
			Npc mob = null;
			
			if (spawnKeyBoxMob)
			{
				try
				{
					NpcTemplate template = NpcData.getInstance().getTemplate(18149);
					if (template == null)
					{
						LOGGER.warn("Data missing in NPC table for ID: 18149.");
						continue;
					}
					
					final Spawn keyBoxMobSpawn = new Spawn(template);
					keyBoxMobSpawn.setLoc(spawn.getLoc());
					keyBoxMobSpawn.setRespawnDelay(3600);
					
					SpawnTable.getInstance().addSpawn(keyBoxMobSpawn, false);
					mob = keyBoxMobSpawn.doSpawn(false);
					
					// Single spawn only.
					keyBoxMobSpawn.setRespawnState(false);
				}
				catch (Exception e)
				{
					LOGGER.error("Failed to initialize a spawn.", e);
				}
				
				spawnedKeyBoxMob = true;
			}
			else
			{
				mob = spawn.doSpawn(false);
				
				// Single spawn only.
				spawn.setRespawnState(false);
			}
			
			if (mob != null)
			{
				mob.setScriptValue(npcId);
				switch (npcId)
				{
					case 31469:
					case 31474:
					case 31479:
					case 31484:
					case 31472:
					case 31477:
					case 31482:
					case 31487:
						mobs.add(mob);
				}
				_allMobs.add(mob);
			}
		}
		
		switch (npcId)
		{
			case 31469:
			case 31474:
			case 31479:
			case 31484:
				_viscountMobs.put(npcId, mobs);
				break;
			
			case 31472:
			case 31477:
			case 31482:
			case 31487:
				_dukeMobs.put(npcId, mobs);
				break;
		}
	}
	
	public synchronized void testViscountMobsAnnihilation(int npcId)
	{
		final List<Npc> mobs = _viscountMobs.get(npcId);
		if (mobs == null)
			return;
		
		for (Npc mob : mobs)
		{
			if (!mob.isDead())
				return;
		}
		
		spawnMonster(npcId);
	}
	
	public synchronized void testDukeMobsAnnihilation(int npcId)
	{
		final List<Npc> mobs = _dukeMobs.get(npcId);
		if (mobs == null)
			return;
		
		for (Npc mob : mobs)
		{
			if (!mob.isDead())
				return;
		}
		
		spawnArchonOfHalisha(npcId);
	}
	
	public void spawnKeyBox(Npc npc)
	{
		if (!isAttackTime())
			return;
		
		final Spawn spawn = _keyBoxSpawns.get(npc.getNpcId());
		if (spawn != null)
		{
			spawn.setLoc(npc.getPosition());
			spawn.setRespawnDelay(3600);
			
			_allMobs.add(spawn.doSpawn(false));
			
			// Single spawn only.
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnExecutionerOfHalisha(Npc npc)
	{
		if (!isAttackTime())
			return;
		
		final Spawn spawn = _executionerSpawns.get(npc.getNpcId());
		if (spawn != null)
		{
			spawn.setLoc(npc.getPosition());
			spawn.setRespawnDelay(3600);
			
			_allMobs.add(spawn.doSpawn(false));
			
			// Single spawn only.
			spawn.setRespawnState(false);
		}
	}
	
	public void spawnArchonOfHalisha(int npcId)
	{
		if (!isAttackTime())
			return;
		
		if (_archonSpawned.get(npcId))
			return;
		
		final List<Spawn> spawns = _dukeFinalMobs.get(npcId);
		if (spawns != null)
		{
			for (Spawn spawn : spawns)
			{
				final Npc mob = spawn.doSpawn(false);
				
				// Single spawn only.
				spawn.setRespawnState(false);
				
				if (mob != null)
				{
					mob.setScriptValue(npcId);
					_allMobs.add(mob);
				}
			}
			_archonSpawned.put(npcId, true);
		}
	}
	
	public void spawnEmperorsGraveNpc(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final List<Spawn> spawns = _emperorsGraveNpcs.get(npcId);
		if (spawns != null)
		{
			for (Spawn spawn : spawns)
			{
				_allMobs.add(spawn.doSpawn(false));
				
				// Single spawn only.
				spawn.setRespawnState(false);
			}
		}
	}
	
	/**
	 * Spawn a Shadow of Halisha based on current Manager npcId.
	 * @param npcId : the manager npcId.
	 */
	public void spawnShadow(int npcId)
	{
		if (!isAttackTime())
			return;
		
		final Spawn spawn = _shadowSpawns.get(npcId);
		if (spawn != null)
		{
			final Npc mob = spawn.doSpawn(false);
			
			// Single spawn only.
			spawn.setRespawnState(false);
			
			if (mob != null)
			{
				mob.setScriptValue(npcId);
				_allMobs.add(mob);
			}
		}
	}
	
	public void showHtmlFile(Player player, String file, Npc npc, Player member)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/sepulchers/" + file);
		if (member != null)
			html.replace("%member%", member.getName());
		
		player.sendPacket(html);
	}
	
	/**
	 * Make each manager shout, informing Players they can now teleport in.
	 */
	protected void onEntryEvent()
	{
		// Managers shout. Entrance is allowed.
		String msg1 = "You may now enter the Sepulcher.";
		String msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter.";
		for (Spawn temp : _managers)
		{
			((SepulcherNpc) temp.getNpc()).sayInShout(msg1);
			((SepulcherNpc) temp.getNpc()).sayInShout(msg2);
		}
	}
	
	/**
	 * Prepare all Mysterious Boxes and refresh Shadows of Halisha spawn points.
	 */
	protected void onAttackEvent()
	{
		// Generate new locations for the 4 shadows.
		final int newLocIndex = Rnd.get(4);
		
		// Refresh locations for all spawns.
		for (int i = 0; i <= 3; i++)
		{
			final Spawn spawn = _shadowSpawns.get(GATEKEEPER_IDS[i]);
			spawn.setLoc(SHADOW_LOCS[newLocIndex][i][1], SHADOW_LOCS[newLocIndex][i][2], SHADOW_LOCS[newLocIndex][i][3], SHADOW_LOCS[newLocIndex][i][4]);
		}
		
		// Spawn all Mysterious Boxes.
		spawnMysteriousBox(31921);
		spawnMysteriousBox(31922);
		spawnMysteriousBox(31923);
		spawnMysteriousBox(31924);
	}
	
	/**
	 * Reset all variables, teleport Players out of the zones, delete all Monsters, close all Doors.
	 */
	protected void onEndEvent()
	{
		// Managers shout. Game is ended.
		for (Spawn temp : _managers)
		{
			// Hall isn't used right now, so its manager will not shout.
			if (!_hallInUse.get(temp.getNpcId()))
				continue;
			
			((SepulcherNpc) temp.getNpc()).sayInShout("Game over. The teleport will appear momentarily.");
		}
		
		// Teleport out all players, and destroy
		for (Location loc : _startHallSpawns.values())
		{
			final List<Player> players = ZoneManager.getInstance().getZone(loc.getX(), loc.getY(), loc.getZ(), BossZone.class).oustAllPlayers();
			for (Player player : players)
			{
				// Delete all instances of Chapel Key.
				final ItemInstance key = player.getInventory().getItemByItemId(CHAPEL_KEY);
				if (key != null)
					player.destroyItemByItemId("Quest", CHAPEL_KEY, key.getCount(), player, false);
			}
		}
		
		// Delete all monsters.
		for (Npc mob : _allMobs)
		{
			if (mob.getSpawn() != null)
				mob.getSpawn().setRespawnState(false);
			
			mob.deleteMe();
		}
		_allMobs.clear();
		
		// Close all doors.
		for (int doorId : _hallGateKeepers.values())
		{
			final Door door = DoorData.getInstance().getDoor(doorId);
			if (door != null)
				door.closeMe();
		}
		
		// Reset maps.
		_hallInUse.replaceAll((npcId, used) -> used = false);
		_archonSpawned.replaceAll((npcId, spawned) -> spawned = false);
	}
	
	/**
	 * Make each manager shout during ATTACK State, every 5 minutes. No shout on first minute.
	 * @param currentMinute : the current minute.
	 */
	protected void managersShout(int currentMinute)
	{
		if (_state == State.ATTACK && currentMinute != 0)
		{
			final int modulo = currentMinute % 5;
			if (modulo == 0)
			{
				final String msg = currentMinute + " minute(s) have passed.";
				
				for (Spawn temp : _managers)
				{
					// Hall isn't used right now, so its manager will not shout.
					if (!_hallInUse.get(temp.getNpcId()))
						continue;
					
					((SepulcherNpc) temp.getNpc()).sayInShout(msg);
				}
			}
		}
	}
	
	protected class Cycle implements Runnable
	{
		@Override
		public void run()
		{
			final int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
			
			// Calculate the new State.
			State newState = State.ATTACK;
			if (currentMinute >= Config.FS_TIME_ENTRY)
				newState = State.ENTRY;
			else if (currentMinute >= Config.FS_TIME_END)
				newState = State.END;
			
			// A new State has been found. Fire the good event.
			if (newState != _state)
			{
				_state = newState;
				
				switch (_state)
				{
					case ENTRY:
						onEntryEvent();
						break;
					
					case ATTACK:
						onAttackEvent();
						break;
					
					case END:
						onEndEvent();
						break;
				}
				LOGGER.info("A new Four Sepulchers event has been announced ({}).", _state);
			}
			
			// Managers shout during ATTACK state, every 5min, if the hall is under use.
			managersShout(currentMinute);
		}
	}
	
	public static final FourSepulchersManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FourSepulchersManager INSTANCE = new FourSepulchersManager();
	}
}