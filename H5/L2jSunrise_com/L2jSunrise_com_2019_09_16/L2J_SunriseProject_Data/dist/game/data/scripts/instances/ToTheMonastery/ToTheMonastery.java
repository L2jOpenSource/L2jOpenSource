package instances.ToTheMonastery;

import java.util.List;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;
import quests.Q10294_SevenSignToTheMonastery.Q10294_SevenSignToTheMonastery;
import quests.Q10295_SevenSignsSolinasTomb.Q10295_SevenSignsSolinasTomb;
import quests.Q10296_SevenSignsPowerOfTheSeal.Q10296_SevenSignsPowerOfTheSeal;

/**
 * @author vGodFather
 */
public class ToTheMonastery extends AbstractNpcAI
{
	private static final int INSTANCE_ID = 151;
	
	private static int GLOBE = 32815;
	private static int EVIL = 32792;
	private static int ELCADIA_SUPPORT = 32787;
	private static int GUARDIAN = 32803;
	private static int WEST_WATCHER = 32804;
	private static int NORTH_WATCHER = 32805;
	private static int EAST_WATCHER = 32806;
	private static int SOUTH_WATCHER = 32807;
	private static int WEST_DEVICE = 32816;
	private static int NORTH_DEVICE = 32817;
	private static int EAST_DEVICE = 32818;
	private static int SOUTH_DEVICE = 32819;
	private static int SOLINA = 32793;
	private static int TELEPORT_DEVICE = 32820;
	private static int TELEPORT_DEVICE_2 = 32837;
	private static int TELEPORT_DEVICE_3 = 32842;
	
	private static int POWERFUL_DEVICE_1 = 32838;
	private static int POWERFUL_DEVICE_2 = 32839;
	private static int POWERFUL_DEVICE_3 = 32840;
	private static int POWERFUL_DEVICE_4 = 32841;
	
	private static int SCROLL_OF_ABSTINENCE = 17228;
	private static int SHIELD_OF_SACRIFICE = 17229;
	private static int SWORD_OF_HOLYSPIRIT = 17230;
	private static int STAFF_OF_BLESSING = 17231;
	
	private static int ETISETINA = 18949;
	
	private static final int[] NPCs =
	{
		GLOBE,
		EVIL,
		GUARDIAN,
		WEST_WATCHER,
		NORTH_WATCHER,
		EAST_WATCHER,
		SOUTH_WATCHER,
		WEST_DEVICE,
		NORTH_DEVICE,
		EAST_DEVICE,
		SOUTH_DEVICE,
		SOLINA,
		TELEPORT_DEVICE,
		TELEPORT_DEVICE_2,
		TELEPORT_DEVICE_3,
		POWERFUL_DEVICE_1,
		POWERFUL_DEVICE_2,
		POWERFUL_DEVICE_3,
		POWERFUL_DEVICE_4
	};
	
	private static final Location[] TELEPORTS =
	{
		new Location(120664, -86968, -3392),
		new Location(116324, -84994, -3397),
		new Location(85937, -249618, -8320),
		new Location(120727, -86868, -3392),
		new Location(85937, -249618, -8320),
		new Location(82434, -249546, -8320),
		new Location(85691, -252426, -8320),
		new Location(88573, -249556, -8320),
		new Location(85675, -246630, -8320),
		new Location(45512, -249832, -6760),
		new Location(120664, -86968, -3392),
		new Location(56033, -252944, -6760),
		new Location(56081, -250391, -6760),
		new Location(76736, -241021, -10832),
		new Location(76736, -241021, -10832)
	};
	
	public ToTheMonastery()
	{
		super(ToTheMonastery.class.getSimpleName(), "instances");
		addStartNpc(GLOBE);
		addTalkId(NPCs);
		addKillId(ETISETINA);
		
		questItemIds = new int[]
		{
			SCROLL_OF_ABSTINENCE,
			SHIELD_OF_SACRIFICE,
			SWORD_OF_HOLYSPIRIT,
			STAFF_OF_BLESSING
		};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player)
	{
		String htmltext = event;
		int npcId = npc.getId();
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if ((tmpworld instanceof ToTheMonasteryWorld))
		{
			ToTheMonasteryWorld world = (ToTheMonasteryWorld) tmpworld;
			
			if (npcId == EVIL)
			{
				if (event.equalsIgnoreCase("Enter3"))
				{
					teleportPlayer(player, TELEPORTS[2], player.getInstanceId());
					ThreadPoolManager.getInstance().scheduleGeneral(() -> player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_HOLY_BURIAL_GROUND_OPENING), 1000);
					return null;
				}
				else if (event.equalsIgnoreCase("teleport_in"))
				{
					teleportPlayer(player, TELEPORTS[9], player.getInstanceId());
					return null;
				}
				else if (event.equalsIgnoreCase("start_scene"))
				{
					QuestState check = player.getQuestState(Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName());
					
					check.set("cond", "2");
					ThreadPoolManager.getInstance().scheduleGeneral(() -> teleportPlayer(player, TELEPORTS[13], world.getInstanceId()), 60500L);
					player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_BOSS_OPENING);
					return null;
				}
				else if (event.equalsIgnoreCase("teleport_back"))
				{
					teleportPlayer(player, TELEPORTS[14], player.getInstanceId());
					return null;
				}
			}
			else if (npcId == GUARDIAN)
			{
				if (event.equalsIgnoreCase("ReturnToEris"))
				{
					teleportPlayer(player, TELEPORTS[3], player.getInstanceId());
					return null;
				}
			}
			else if ((npcId == TELEPORT_DEVICE) || (npcId == EVIL))
			{
				if (event.equalsIgnoreCase("teleport_solina"))
				{
					teleportPlayer(player, TELEPORTS[11], player.getInstanceId());
					return null;
				}
			}
		}
		
		if (event.equalsIgnoreCase("check_player"))
		{
			cancelQuestTimer("check_player", npc, player);
			
			if (player.getCurrentHp() < (player.getMaxHp() * 0.8D))
			{
				L2Skill skill = SkillData.getInstance().getInfo(6724, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
			
			if (player.getCurrentMp() < (player.getMaxMp() * 0.5D))
			{
				L2Skill skill = SkillData.getInstance().getInfo(6728, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
			
			if (player.getCurrentHp() < (player.getMaxHp() * 0.1D))
			{
				L2Skill skill = SkillData.getInstance().getInfo(6730, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
			
			if (player.isInCombat())
			{
				L2Skill skill = SkillData.getInstance().getInfo(6725, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
			return "";
		}
		
		if (event.equalsIgnoreCase("check_voice"))
		{
			cancelQuestTimer("check_voice", npc, player);
			
			QuestState qs = player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName());
			if ((qs != null) && (!qs.isCompleted()))
			{
				if (qs.getInt("cond") == 2)
				{
					if (Rnd.chance(5))
					{
						if (Rnd.chance(10))
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.IT_SEEMS_THAT_YOU_CANNOT_REMEMBER_TO_THE_ROOM_OF_THE_WATCHER_WHO_FOUND_THE_BOOK));
						}
						else
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.REMEMBER_THE_CONTENT_OF_THE_BOOKS_THAT_YOU_FOUND_YOU_CANT_TAKE_THEM_OUT_WITH_YOU));
						}
					}
				}
				else if ((qs.getInt("cond") == 3) && (Rnd.chance(8)))
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.YOUR_WORK_HERE_IS_DONE_SO_RETURN_TO_THE_CENTRAL_GUARDIAN));
				}
			}
			QuestState qs2 = player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName());
			if ((qs2 != null) && (!qs2.isCompleted()))
			{
				if (qs2.getInt("cond") == 1)
				{
					if (Rnd.chance(5))
					{
						if (Rnd.chance(10))
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE));
						}
						else if (Rnd.chance(15))
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED));
						}
						else
						{
							npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getId(), NpcStringId.THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER));
						}
					}
				}
			}
			startQuestTimer("check_voice", 100000L, npc, player);
			return "";
		}
		
		if (event.equalsIgnoreCase("check_follow"))
		{
			cancelQuestTimer("check_follow", npc, player);
			boolean decayed = false;
			if (!Util.checkIfInRange(300, npc, player, true))
			{
				if ((tmpworld instanceof ToTheMonasteryWorld) && (player.getInstanceId() > 0))
				{
					npc.sendPacket(new MagicSkillUse(npc, npc, 2036, 1, 500, 0));
					npc.teleToLocation(player.getLocation(), true);
				}
				else
				{
					cancelQuestTimer("check_player", npc, player);
					cancelQuestTimer("check_voice", npc, player);
					cancelQuestTimer("check_follow", npc, player);
					decayed = true;
					npc.decayMe();
				}
			}
			
			// vGodFather small trick-hack just in case of double elcadia npc
			// safety precaution
			if (player.getInstanceId() > 0)
			{
				List<L2Character> npcs = player.getKnownList().getKnownCharactersById(ELCADIA_SUPPORT);
				if (npcs.size() > 1)
				{
					for (L2Character newNpc : npcs)
					{
						cancelQuestTimer("check_player", (L2Npc) newNpc, player);
						cancelQuestTimer("check_voice", (L2Npc) newNpc, player);
						cancelQuestTimer("check_follow", (L2Npc) newNpc, player);
						newNpc.decayMe();
						break;
					}
				}
			}
			
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			
			if (player.isInCombat())
			{
				if (player.getCurrentHp() < (player.getMaxHp() * 0.8D))
				{
					L2Skill skill = SkillData.getInstance().getInfo(6724, 1);
					npc.setTarget(player);
					npc.doSimultaneousCast(skill);
				}
				
				if (player.getCurrentMp() < (player.getMaxMp() * 0.5D))
				{
					L2Skill skill = SkillData.getInstance().getInfo(6728, 1);
					npc.setTarget(player);
					npc.doSimultaneousCast(skill);
				}
				
				if (player.getCurrentHp() < (player.getMaxHp() * 0.1D))
				{
					L2Skill skill = SkillData.getInstance().getInfo(6730, 1);
					npc.setTarget(player);
					npc.doSimultaneousCast(skill);
				}
				
				L2Skill skill = SkillData.getInstance().getInfo(6725, 1);
				npc.setTarget(player);
				npc.doSimultaneousCast(skill);
			}
			
			if (!decayed)
			{
				startQuestTimer("check_follow", 5000L, npc, player);
			}
			return "";
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		String htmltext = getNoQuestMsg(player);
		int npcId = npc.getId();
		if (npcId == GLOBE)
		{
			//@formatter:off
			if (
				   ((player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()) != null) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() == 1))
				|| ((player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()) != null) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() == 2) && (player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName()) == null))
				|| ((player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName()) != null) && (player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName()).getState() != 2))
				|| ((player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName()) != null) && (player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName()).getState() == 2) && (player.getQuestState(Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName()) == null))
				|| ((player.getQuestState(Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName()) != null) && (player.getQuestState(Q10296_SevenSignsPowerOfTheSeal.class.getSimpleName()).getState() != 2)))
			{
				enterInstance(npc, player);
				return null;
			}
			//@formatter:on
			htmltext = "32815-00.htm";
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if ((tmpworld instanceof ToTheMonasteryWorld))
		{
			ToTheMonasteryWorld world = (ToTheMonasteryWorld) tmpworld;
			
			if (npcId == EVIL)
			{
				teleportPlayer(player, TELEPORTS[1], 0);
				player.setInstanceId(0);
				return null;
			}
			else if (npcId == WEST_DEVICE)
			{
				teleportPlayer(player, TELEPORTS[5], player.getInstanceId());
				return null;
			}
			else if (npcId == NORTH_DEVICE)
			{
				teleportPlayer(player, TELEPORTS[6], player.getInstanceId());
				return null;
			}
			else if (npcId == EAST_DEVICE)
			{
				teleportPlayer(player, TELEPORTS[7], player.getInstanceId());
				return null;
			}
			else if (npcId == SOUTH_DEVICE)
			{
				teleportPlayer(player, TELEPORTS[8], player.getInstanceId());
				return null;
			}
			else if ((npcId == WEST_WATCHER) || (npcId == NORTH_WATCHER) || (npcId == EAST_WATCHER) || (npcId == SOUTH_WATCHER))
			{
				teleportPlayer(player, TELEPORTS[4], player.getInstanceId());
				return null;
			}
			else if ((npcId == SOLINA) || (npcId == TELEPORT_DEVICE) || (npcId == TELEPORT_DEVICE_2))
			{
				teleportPlayer(player, TELEPORTS[10], player.getInstanceId());
				return null;
			}
			else if (npcId == TELEPORT_DEVICE_3)
			{
				teleportPlayer(player, TELEPORTS[12], player.getInstanceId());
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_ELYSS_NARRATION);
				return null;
			}
			else if (npcId == POWERFUL_DEVICE_1)
			{
				if (st.getQuestItemsCount(STAFF_OF_BLESSING) > 0L)
				{
					st.takeItems(STAFF_OF_BLESSING, -1L);
					addSpawn(18953, 45400, -246072, -6754, 49152, false, 0L, false, world.getInstanceId());
					return null;
				}
				
				htmltext = "no-item.htm";
			}
			else if (npcId == POWERFUL_DEVICE_2)
			{
				if (st.getQuestItemsCount(SCROLL_OF_ABSTINENCE) > 0L)
				{
					st.takeItems(SCROLL_OF_ABSTINENCE, -1L);
					addSpawn(18954, 48968, -249640, -6754, 32768, false, 0L, false, world.getInstanceId());
					return null;
				}
				
				htmltext = "no-item.htm";
			}
			else if (npcId == POWERFUL_DEVICE_3)
			{
				if (st.getQuestItemsCount(SWORD_OF_HOLYSPIRIT) > 0L)
				{
					st.takeItems(SWORD_OF_HOLYSPIRIT, -1L);
					addSpawn(18955, 45400, -253208, -6754, 16384, false, 0L, false, world.getInstanceId());
					return null;
				}
				
				htmltext = "no-item.htm";
			}
			else if (npcId == POWERFUL_DEVICE_4)
			{
				if (st.getQuestItemsCount(SHIELD_OF_SACRIFICE) > 0L)
				{
					st.takeItems(SHIELD_OF_SACRIFICE, -1L);
					addSpawn(18952, 41784, -249640, -6754, 0, false, 0L, false, world.getInstanceId());
					return null;
				}
				
				htmltext = "no-item.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		
		if ((tmpworld instanceof ToTheMonasteryWorld))
		{
			ToTheMonasteryWorld world = (ToTheMonasteryWorld) tmpworld;
			
			int npcId = npc.getId();
			if (npcId == ETISETINA)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() -> teleportPlayer(player, TELEPORTS[0], world.getInstanceId()), 60500L);
				return null;
			}
		}
		return "";
	}
	
	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof ToTheMonasteryWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
			if (inst != null)
			{
				teleportPlayer(player, TELEPORTS[0], world.getInstanceId());
				spawnElcadia(player, ((ToTheMonasteryWorld) world));
			}
			return;
		}
		
		int instanceId = InstanceManager.getInstance().createDynamicInstance("ToTheMonastery.xml");
		Instance inst = InstanceManager.getInstance().getInstance(instanceId);
		inst.setExitLoc(new Location(player));
		
		world = new ToTheMonasteryWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(INSTANCE_ID);
		InstanceManager.getInstance().addWorld(world);
		((ToTheMonasteryWorld) world).storeTime[0] = System.currentTimeMillis();
		world.addAllowed(player.getObjectId());
		teleportPlayer(player, TELEPORTS[0], instanceId);
		spawnElcadia(player, ((ToTheMonasteryWorld) world));
		
		// This will fix solina's tomb in case of fail kill tomb guards or server restart while killing them
		// But this will spawn only guards that didn't die ;)
		QuestState qs = player.getQuestState(Q10295_SevenSignsSolinasTomb.class.getSimpleName());
		if ((qs != null) && (qs.getInt("tomb") < 4))
		{
			qs.unset("spawned");
		}
		
		openDoor(21100002, world.getInstanceId());
		openDoor(21100003, world.getInstanceId());
		openDoor(21100004, world.getInstanceId());
		openDoor(21100005, world.getInstanceId());
		openDoor(21100007, world.getInstanceId());
		openDoor(21100008, world.getInstanceId());
		openDoor(21100009, world.getInstanceId());
		openDoor(21100011, world.getInstanceId());
		openDoor(21100012, world.getInstanceId());
		openDoor(21100013, world.getInstanceId());
		openDoor(21100015, world.getInstanceId());
		openDoor(21100016, world.getInstanceId());
	}
	
	protected void spawnElcadia(L2PcInstance player, ToTheMonasteryWorld world)
	{
		L2Npc npc = addSpawn(ELCADIA_SUPPORT, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
		startQuestTimer("check_follow", 3000, npc, player);
		startQuestTimer("check_player", 3000, npc, player);
		startQuestTimer("check_voice", 3000, npc, player);
	}
	
	private class ToTheMonasteryWorld extends InstanceWorld
	{
		public long[] storeTime =
		{
			0L,
			0L
		};
		
		public ToTheMonasteryWorld()
		{
		}
	}
}