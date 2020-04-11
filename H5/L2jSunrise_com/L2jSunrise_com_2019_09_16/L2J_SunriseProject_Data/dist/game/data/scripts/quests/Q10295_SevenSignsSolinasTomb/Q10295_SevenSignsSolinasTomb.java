package quests.Q10295_SevenSignsSolinasTomb;

import java.util.Arrays;
import java.util.List;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.serverpackets.ExStartScenePlayer;
import l2r.gameserver.network.serverpackets.OnEventTrigger;

import quests.Q10294_SevenSignToTheMonastery.Q10294_SevenSignToTheMonastery;

/**
 * @author vGodFather
 */
public class Q10295_SevenSignsSolinasTomb extends Quest
{
	private static final int Odd_Globe = 32815;
	private static final int ErisEvilThoughts = 32792;
	private static final int ElcardiaInzone1 = 32787;
	private static final int PowerfulDeviceStaff = 32838;
	private static final int PowerfulDeviceBook = 32839;
	private static final int PowerfulDeviceSword = 32840;
	private static final int PowerfulDeviceShield = 32841;
	private static final int AltarofHallowsStaff = 32857;
	private static final int AltarofHallowsSword = 32858;
	private static final int AltarofHallowsBook = 32859;
	private static final int AltarofHallowsShield = 32860;
	private static final int TeleportControlDevice = 32820;
	private static final int TeleportControlDevice2 = 32837;
	private static final int TeleportControlDevice3 = 32842;
	private static final int TomboftheSaintess = 32843;
	private static final int ScrollofAbstinence = 17228;
	private static final int ShieldofSacrifice = 17229;
	private static final int SwordofHolySpirit = 17230;
	private static final int StaffofBlessing = 17231;
	private static final int Solina = 32793;
	private static final List<Integer> SolinaGuardians = Arrays.asList(18952, 18953, 18954, 18955);
	private static final List<Integer> TombGuardians = Arrays.asList(18956, 18957, 18958, 18959);
	
	// Locations
	private static Location ENTER_LOC = new Location(45512, -249832, -6760);
	private static Location EXIT_LOC = new Location(120664, -86968, -3392);
	private static Location REAL_TOMB = new Location(56081, -250391, -6760);
	private static Location SOLINA_LOC = new Location(56033, -252944, -6760);
	
	public Q10295_SevenSignsSolinasTomb()
	{
		super(10295, Q10295_SevenSignsSolinasTomb.class.getSimpleName(), "Seven Signs Solinas Tomb");
		addStartNpc(ErisEvilThoughts);
		addTalkId(new int[]
		{
			ElcardiaInzone1,
			TeleportControlDevice,
			PowerfulDeviceStaff,
			PowerfulDeviceBook,
			PowerfulDeviceSword,
			PowerfulDeviceShield,
			ErisEvilThoughts
		});
		addTalkId(new int[]
		{
			AltarofHallowsStaff,
			AltarofHallowsSword,
			AltarofHallowsBook,
			AltarofHallowsShield,
			Odd_Globe
		});
		addTalkId(new int[]
		{
			TeleportControlDevice2,
			TomboftheSaintess,
			TeleportControlDevice3,
			Solina
		});
		registerQuestItems(ScrollofAbstinence, ShieldofSacrifice, SwordofHolySpirit, StaffofBlessing);
		addKillId(SolinaGuardians);
		addKillId(TombGuardians);
		addSpawnId(TombGuardians);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (TombGuardians.contains(npc.getId()))
		{
			L2MonsterInstance monster = (L2MonsterInstance) npc;
			monster.setIsNoRndWalk(true);
			monster.setIsImmobilized(true);
		}
		
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = event;
		int progress = st.getInt("progress");
		
		if (event.equalsIgnoreCase("eris_q10295_5.htm"))
		{
			st.set("cond", "1");
			st.setState((byte) 1);
			playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
		}
		else
		{
			if (event.equalsIgnoreCase("teleport_in"))
			{
				player.teleToLocation(ENTER_LOC);
				openDoor(21100008, player.getInstanceId());
				openDoor(21100012, player.getInstanceId());
				openDoor(21100016, player.getInstanceId());
				openDoor(21100003, player.getInstanceId());
				openDoor(21100005, player.getInstanceId());
				openDoor(21100007, player.getInstanceId());
				openDoor(21100002, player.getInstanceId());
				openDoor(21100004, player.getInstanceId());
				openDoor(21100015, player.getInstanceId());
				openDoor(21100013, player.getInstanceId());
				openDoor(21100011, player.getInstanceId());
				openDoor(21100009, player.getInstanceId());
				checkDoors(player, progress);
				
				if (st.getInt("entermovie") == 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(() -> player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_SOLINA_TOMB_OPENING), 3000L);
					st.set("entermovie", "1");
				}
				
				return null;
			}
			else if (event.equalsIgnoreCase("teleport_out"))
			{
				player.teleToLocation(EXIT_LOC);
				return null;
			}
			else if (event.equalsIgnoreCase("use_staff"))
			{
				if (st.getQuestItemsCount(StaffofBlessing) > 0L)
				{
					long count = st.getQuestItemsCount(StaffofBlessing);
					st.takeItems(StaffofBlessing, count);
					addSpawn(18952, 41838, -249630, -6761, 0, false, 0L, false, player.getInstanceId());
					return null;
				}
				htmltext = "powerful_q10295_0.htm";
			}
			else if (event.equalsIgnoreCase("use_book"))
			{
				if (st.getQuestItemsCount(ScrollofAbstinence) > 0L)
				{
					long count = st.getQuestItemsCount(ScrollofAbstinence);
					st.takeItems(ScrollofAbstinence, count);
					addSpawn(18953, 45391, -253186, -6761, 0, false, 0L, false, player.getInstanceId());
					return null;
				}
				htmltext = "powerful_q10295_0.htm";
			}
			else if (event.equalsIgnoreCase("use_sword"))
			{
				if (st.getQuestItemsCount(SwordofHolySpirit) > 0L)
				{
					long count = st.getQuestItemsCount(SwordofHolySpirit);
					st.takeItems(SwordofHolySpirit, count);
					addSpawn(18954, 48912, -249639, -6761, 0, false, 0L, false, player.getInstanceId());
					return null;
				}
				htmltext = "powerful_q10295_0.htm";
			}
			else if (event.equalsIgnoreCase("use_shield"))
			{
				if (st.getQuestItemsCount(ShieldofSacrifice) > 0L)
				{
					long count = st.getQuestItemsCount(ShieldofSacrifice);
					st.takeItems(ShieldofSacrifice, count);
					addSpawn(18955, 45396, -246124, -6761, 0, false, 0L, false, player.getInstanceId());
					return null;
				}
				htmltext = "powerful_q10295_0.htm";
			}
			else if (event.equalsIgnoreCase("altarstaff_q10295_2.htm"))
			{
				if ((st.getQuestItemsCount(StaffofBlessing) == 0) && (progress < 4))
				{
					st.giveItems(StaffofBlessing, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					checkDoors(player, progress);
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarbook_q10295_2.htm"))
			{
				if ((st.getQuestItemsCount(ScrollofAbstinence) == 0) && (progress < 4))
				{
					st.giveItems(ScrollofAbstinence, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					checkDoors(player, progress);
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarsword_q10295_2.htm"))
			{
				if ((st.getQuestItemsCount(SwordofHolySpirit) == 0) && (progress < 4))
				{
					st.giveItems(SwordofHolySpirit, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					checkDoors(player, progress);
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarshield_q10295_2.htm"))
			{
				if ((st.getQuestItemsCount(ShieldofSacrifice) == 0) && (progress < 4))
				{
					st.giveItems(ShieldofSacrifice, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					checkDoors(player, progress);
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else
			{
				if (event.equalsIgnoreCase("teleport_solina"))
				{
					player.teleToLocation(SOLINA_LOC);
					
					// Just in case we die or exit and door is closed after teleport
					if (st.getInt("tomb") >= 4)
					{
						openDoor(21100018, player.getInstanceId());
					}
					
					return null;
				}
				if (event.equalsIgnoreCase("tombsaintess_q10295_2.htm"))
				{
					if (st.getInt("spawned") == 0)
					{
						if (st.getInt("firstgroup") == 0)
						{
							spawnFirstGroup(player);
						}
						if (st.getInt("secondgroup") == 0)
						{
							spawnSecondGroup(player);
						}
						if (st.getInt("thirdgroup") == 0)
						{
							spawnThirdGroup(player);
						}
						if (st.getInt("fourthgroup") == 0)
						{
							spawnFourthGroup(player);
						}
						st.set("spawned", "1");
					}
					else
					{
						htmltext = "tombsaintess_q10295_3.htm";
					}
				}
				else
				{
					if (event.equalsIgnoreCase("teleport_realtomb"))
					{
						player.teleToLocation(REAL_TOMB);
						return null;
					}
					if (event.equalsIgnoreCase("solina_q10295_4.htm"))
					{
						st.set("cond", "2");
						playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
						player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_ELYSS_NARRATION);
					}
					else if (event.equalsIgnoreCase("solina_q10295_8.htm"))
					{
						st.set("cond", "3");
						playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return htmltext;
	}
	
	private void checkDoors(L2PcInstance player, int progress)
	{
		if (progress >= 4)
		{
			openDoor(21100001, player.getInstanceId());
			openDoor(21100010, player.getInstanceId());
			openDoor(21100014, player.getInstanceId());
			openDoor(21100006, player.getInstanceId());
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = getQuestState(player, true);
		
		if (st == null)
		{
			return htmltext;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		int solina = st.getInt("solina");
		
		if (player.getClassId().getId() != player.getBaseClass())
		{
			return "no_subclass_allowed.htm";
		}
		if (npcId == ErisEvilThoughts)
		{
			if (st.getState() == State.COMPLETED)
			{
				htmltext = "32792-02.html";
			}
			else if (cond == 0)
			{
				QuestState qs = player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName());
				if ((player.getLevel() >= 81) && (qs != null) && (qs.isCompleted()))
				{
					htmltext = "eris_q10295_1.htm";
				}
				else
				{
					htmltext = "eris_q10295_0a.htm";
					st.exitQuest(true);
				}
			}
			else if (cond == 1)
			{
				htmltext = "eris_q10295_6.htm";
			}
			else if (cond == 2)
			{
				htmltext = "eris_q10295_7.htm";
			}
			else if (cond == 3)
			{
				if (player.getLevel() >= 81)
				{
					htmltext = "eris_q10295_8.htm";
					st.addExpAndSp(125000000, 12500000);
					st.unset("cond");
					st.unset("progress");
					st.unset("solina");
					st.unset("tomb");
					st.unset("spawned");
					st.unset("firstgroup");
					st.unset("secondgroup");
					st.unset("thirdgroup");
					st.unset("fourthgroup");
					st.unset("entermovie");
					st.exitQuest(false);
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
				}
				else
				{
					htmltext = "eris_q10295_0.htm";
				}
			}
			else if (cond == 4)
			{
				htmltext = "eris_q10294_10.htm";
			}
		}
		else if (npcId == ElcardiaInzone1)
		{
			htmltext = "elcardia_q10295_1.htm";
		}
		else if (npcId == TeleportControlDevice)
		{
			if (solina != 4)
			{
				htmltext = "teleport_device_q10295_1.htm";
			}
			else
			{
				htmltext = "teleport_device_q10295_2.htm";
			}
		}
		else if (npc.getId() == Odd_Globe)
		{
			if (!player.getQuestState(getName()).isCompleted())
			{
				htmltext = "32815-01.html";
			}
		}
		else if (npcId == PowerfulDeviceStaff)
		{
			htmltext = "powerfulstaff_q10295_1.htm";
		}
		else if (npcId == PowerfulDeviceBook)
		{
			htmltext = "powerfulbook_q10295_1.htm";
		}
		else if (npcId == PowerfulDeviceSword)
		{
			htmltext = "powerfulsword_q10295_1.htm";
		}
		else if (npcId == PowerfulDeviceShield)
		{
			htmltext = "powerfulsheild_q10295_1.htm";
		}
		else if (npcId == AltarofHallowsStaff)
		{
			htmltext = "altarstaff_q10295_1.htm";
		}
		else if (npcId == AltarofHallowsSword)
		{
			htmltext = "altarsword_q10295_1.htm";
		}
		else if (npcId == AltarofHallowsBook)
		{
			htmltext = "altarbook_q10295_1.htm";
		}
		else if (npcId == AltarofHallowsShield)
		{
			htmltext = "altarshield_q10295_1.htm";
		}
		else if (npcId == TeleportControlDevice2)
		{
			htmltext = "teleportdevice2_q10295_1.htm";
		}
		else if (npcId == TomboftheSaintess)
		{
			htmltext = "tombsaintess_q10295_1.htm";
		}
		else if (npcId == TeleportControlDevice3)
		{
			htmltext = "teleportdevice3_q10295_1.htm";
		}
		else if (npcId == Solina)
		{
			if (cond == 1)
			{
				htmltext = "solina_q10295_1.htm";
			}
			else if (cond == 2)
			{
				htmltext = "solina_q10295_4.htm";
			}
			else if (cond == 3)
			{
				htmltext = "solina_q10295_8.htm";
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
			return super.onKill(npc, player, isPet);
		}
		
		int npcId = npc.getId();
		if (SolinaGuardians.contains(npcId))
		{
			int solina = st.getInt("solina");
			solina++;
			st.set("solina", String.valueOf(solina));
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			if (solina >= 4)
			{
				player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_SOLINA_TOMB_CLOSING);
				player.broadcastPacket(new OnEventTrigger(21100100, false));
				player.broadcastPacket(new OnEventTrigger(21100102, true));
			}
		}
		else if (TombGuardians.contains(npcId))
		{
			switch (npcId)
			{
				case 18956:
					st.set("firstgroup", "1");
					break;
				case 18957:
					st.set("secondgroup", "1");
					break;
				case 18958:
					st.set("thirdgroup", "1");
					break;
				case 18959:
					st.set("fourthgroup", "1");
					break;
			}
			
			int tomb = st.getInt("tomb");
			tomb++;
			st.set("tomb", String.valueOf(tomb));
			playSound(player, Sound.ITEMSOUND_QUEST_MIDDLE);
			if (tomb >= 4)
			{
				openDoor(21100018, player.getInstanceId());
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	// First Group
	private void spawnFirstGroup(L2PcInstance player)
	{
		openDoor(21100104, player.getInstanceId());
		addSpawn(18956, 56728, -252776, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 56504, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 56504, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 56392, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 56408, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
	}
	
	// Second Group
	private void spawnSecondGroup(L2PcInstance player)
	{
		openDoor(21100101, player.getInstanceId());
		addSpawn(18957, 55464, -252776, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 55672, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 55752, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 55768, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27403, 55752, -252712, -6760, 0, false, 0L, false, player.getInstanceId());
	}
	
	// Third Group
	private void spawnThirdGroup(L2PcInstance player)
	{
		openDoor(21100102, player.getInstanceId());
		addSpawn(18958, 55496, -252168, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 55672, -252120, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 55752, -252120, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 55656, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 55736, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
	}
	
	// Fourth Group
	private void spawnFourthGroup(L2PcInstance player)
	{
		openDoor(21100103, player.getInstanceId());
		addSpawn(18959, 56680, -252168, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 56520, -252232, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 56520, -252104, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 56424, -252104, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(27404, 56440, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
	}
}