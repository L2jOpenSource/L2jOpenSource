package l2r.gameserver.scripts.quests;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.util.Rnd;

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
	public static boolean spawned = false;
	private static final int Solina = 32793;
	private static final int[] SolinaGuardians =
	{
		18952,
		18953,
		18954,
		18955
	};
	private static final int[] TombGuardians =
	{
		18956,
		18957,
		18958,
		18959
	};
	
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
		addKillId(SolinaGuardians);
		addKillId(TombGuardians);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, final L2PcInstance player)
	{
		if ((npc == null) || (player == null))
		{
			return null;
		}
		QuestState st = player.getQuestState(getName());
		String htmltext = event;
		int progress = st.getInt("progress");
		
		if (event.equalsIgnoreCase("eris_q10295_5.htm"))
		{
			st.set("cond", "1");
			st.set("solina", "0");
			st.set("tomb", "0");
			st.set("progress", "0");
			st.setState((byte) 1);
			st.playSound("ItemSound.quest_accept");
		}
		else
		{
			if (event.equalsIgnoreCase("teleport_in"))
			{
				player.teleToLocation(45512, -249832, -6760);
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
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						player.showQuestMovie(26);
					}
				}, 3000L);
				L2Npc moving = addSpawn(ElcardiaInzone1, player.getX() + Rnd.get(100), player.getY() + Rnd.get(100), player.getZ(), 0, false, 0L, false, player.getInstanceId());
				moving.setTarget(player);
				moving.setRunning();
				moving.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer());
				return null;
			}
			if (event.equalsIgnoreCase("teleport_out"))
			{
				player.teleToLocation(120664, -86968, -3392);
				return null;
			}
			if (event.equalsIgnoreCase("use_staff"))
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
				if (st.getQuestItemsCount(StaffofBlessing) == 0L)
				{
					st.giveItems(StaffofBlessing, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					st.playSound("ItemSound.quest_middle");
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarbook_q10295_2.htm"))
			{
				if (st.getQuestItemsCount(ScrollofAbstinence) == 0L)
				{
					st.giveItems(ScrollofAbstinence, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					st.playSound("ItemSound.quest_middle");
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarsword_q10295_2.htm"))
			{
				if (st.getQuestItemsCount(SwordofHolySpirit) == 0L)
				{
					st.giveItems(SwordofHolySpirit, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					st.playSound("ItemSound.quest_middle");
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
				}
				else
				{
					htmltext = "atlar_q10295_0.htm";
				}
			}
			else if (event.equalsIgnoreCase("altarshield_q10295_2.htm"))
			{
				if (st.getQuestItemsCount(ShieldofSacrifice) == 0L)
				{
					st.giveItems(ShieldofSacrifice, 1L);
					progress++;
					st.set("progress", String.valueOf(progress));
					st.playSound("ItemSound.quest_middle");
					if (progress == 4)
					{
						openDoor(21100001, player.getInstanceId());
						openDoor(21100010, player.getInstanceId());
						openDoor(21100014, player.getInstanceId());
						openDoor(21100006, player.getInstanceId());
					}
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
					player.teleToLocation(56033, -252944, -6760);
					L2Npc moving = addSpawn(ElcardiaInzone1, player.getX() + Rnd.get(100), player.getY() + Rnd.get(100), player.getZ(), 0, false, 0L, false, player.getInstanceId());
					moving.setTarget(player);
					moving.setRunning();
					moving.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer());
					return null;
				}
				if (event.equalsIgnoreCase("tombsaintess_q10295_2.htm"))
				{
					if (!spawned)
					{
						activateTombGuards(player);
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
						player.teleToLocation(56081, -250391, -6760);
						L2Npc moving = addSpawn(ElcardiaInzone1, player.getX() + Rnd.get(100), player.getY() + Rnd.get(100), player.getZ(), 0, false, 0L, false, player.getInstanceId());
						moving.setTarget(player);
						moving.setRunning();
						moving.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, st.getPlayer());
						return null;
					}
					if (event.equalsIgnoreCase("solina_q10295_4.htm"))
					{
						st.set("cond", "2");
						st.playSound("ItemSound.quest_middle");
						player.showQuestMovie(28);
					}
					else if (event.equalsIgnoreCase("solina_q10295_8.htm"))
					{
						st.set("cond", "3");
						st.playSound("ItemSound.quest_middle");
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		
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
			if (cond == 0)
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
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
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
		int npcId = npc.getId();
		QuestState st = player.getQuestState(getName());
		int solina = st.getInt("solina");
		int tomb = st.getInt("tomb");
		
		if ((SolinaGuardians[0] == npcId) || (SolinaGuardians[1] == npcId) || (SolinaGuardians[2] == npcId) || (SolinaGuardians[3] == npcId))
		{
			solina++;
			st.set("solina", String.valueOf(solina));
			st.playSound("ItemSound.quest_middle");
			if (solina == 4)
			{
				player.showQuestMovie(27);
			}
		}
		
		if ((TombGuardians[0] == npcId) || (TombGuardians[1] == npcId) || (TombGuardians[2] == npcId) || (TombGuardians[3] == npcId))
		{
			tomb++;
			st.set("tomb", String.valueOf(tomb));
			st.playSound("ItemSound.quest_middle");
			if (tomb == 16)
			{
				openDoor(21100018, player.getInstanceId());
			}
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	private void activateTombGuards(L2PcInstance player)
	{
		openDoor(21100104, player.getInstanceId());
		openDoor(21100101, player.getInstanceId());
		openDoor(21100102, player.getInstanceId());
		openDoor(21100103, player.getInstanceId());
		spawned = true;
		addSpawn(18956, 56504, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18956, 56504, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18956, 56392, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18956, 56408, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		
		addSpawn(18957, 55672, -252728, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18957, 55752, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18957, 55768, -252840, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18957, 55752, -252712, -6760, 0, false, 0L, false, player.getInstanceId());
		
		addSpawn(18958, 55672, -252120, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18958, 55752, -252120, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18958, 55656, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18958, 55736, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
		
		addSpawn(18959, 56520, -252232, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18959, 56520, -252104, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18959, 56424, -252104, -6760, 0, false, 0L, false, player.getInstanceId());
		addSpawn(18959, 56440, -252216, -6760, 0, false, 0L, false, player.getInstanceId());
	}
}