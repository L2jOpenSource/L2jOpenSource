package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Q227_TestOfTheReformer extends SecondClassQuest
{
	private static final String qn = "Q227_TestOfTheReformer";
	
	// Items
	private static final int BOOK_OF_REFORM = 2822;
	private static final int LETTER_OF_INTRODUCTION = 2823;
	private static final int SLA_LETTER = 2824;
	private static final int GREETINGS = 2825;
	private static final int OL_MAHUM_MONEY = 2826;
	private static final int KATARI_LETTER = 2827;
	private static final int NYAKURI_LETTER = 2828;
	private static final int UNDEAD_LIST = 2829;
	private static final int RAMUS_LETTER = 2830;
	private static final int RIPPED_DIARY = 2831;
	private static final int HUGE_NAIL = 2832;
	private static final int LETTER_OF_BETRAYER = 2833;
	private static final int BONE_FRAGMENT_4 = 2834;
	private static final int BONE_FRAGMENT_5 = 2835;
	private static final int BONE_FRAGMENT_6 = 2836;
	private static final int BONE_FRAGMENT_7 = 2837;
	private static final int BONE_FRAGMENT_8 = 2838;
	private static final int BONE_FRAGMENT_9 = 2839;
	private static final int KAKAN_LETTER = 3037;
	
	// Rewards
	private static final int MARK_OF_REFORMER = 2821;
	
	// NPCs
	private static final int PUPINA = 30118;
	private static final int SLA = 30666;
	private static final int RAMUS = 30667;
	private static final int KATARI = 30668;
	private static final int KAKAN = 30669;
	private static final int NYAKURI = 30670;
	private static final int OL_MAHUM_PILGRIM = 30732;
	
	// Monsters
	private static final int MISERY_SKELETON = 20022;
	private static final int SKELETON_ARCHER = 20100;
	private static final int SKELETON_MARKSMAN = 20102;
	private static final int SKELETON_LORD = 20104;
	private static final int SILENT_HORROR = 20404;
	private static final int NAMELESS_REVENANT = 27099;
	private static final int ARURAUNE = 27128;
	private static final int OL_MAHUM_INSPECTOR = 27129;
	private static final int OL_MAHUM_BETRAYER = 27130;
	private static final int CRIMSON_WEREWOLF = 27131;
	private static final int KRUDEL_LIZARDMAN = 27132;
	
	// Instances
	private Npc _olMahumPilgrim_Katari;
	private Npc _olMahumInspector;
	private Npc _olMahumBetrayer;
	private Npc _olMahumPilgrim_Kakan;
	private Npc _crimsonWerewolf;
	private Npc _olMahumPilgrim_Nyakuri;
	private Npc _krudelLizardman;
	
	// Allowed skills when attacking Crimson Werewolf
	private static final int[] ALLOWED_SKILLS =
	{
		1031,
		1069,
		1164,
		1168,
		1147,
		1177,
		1184,
		1201,
		1206
	};
	
	public Q227_TestOfTheReformer()
	{
		super(227, "Test Of The Reformer");
		
		setItemsIds(BOOK_OF_REFORM, LETTER_OF_INTRODUCTION, SLA_LETTER, GREETINGS, OL_MAHUM_MONEY, KATARI_LETTER, NYAKURI_LETTER, UNDEAD_LIST, RAMUS_LETTER, RIPPED_DIARY, HUGE_NAIL, LETTER_OF_BETRAYER, BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8, BONE_FRAGMENT_9, KAKAN_LETTER);
		
		addStartNpc(PUPINA);
		addTalkId(PUPINA, SLA, RAMUS, KATARI, KAKAN, NYAKURI, OL_MAHUM_PILGRIM);
		
		addAttackId(NAMELESS_REVENANT, CRIMSON_WEREWOLF);
		addKillId(MISERY_SKELETON, SKELETON_ARCHER, SKELETON_MARKSMAN, SKELETON_LORD, SILENT_HORROR, NAMELESS_REVENANT, ARURAUNE, OL_MAHUM_INSPECTOR, OL_MAHUM_BETRAYER, CRIMSON_WEREWOLF, KRUDEL_LIZARDMAN);
		addDecayId(OL_MAHUM_PILGRIM, OL_MAHUM_INSPECTOR, OL_MAHUM_BETRAYER, CRIMSON_WEREWOLF, KRUDEL_LIZARDMAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// PUPINA
		if (event.equalsIgnoreCase("30118-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BOOK_OF_REFORM, 1);
			
			if (giveDimensionalDiamonds39(st))
				htmltext = "30118-04b.htm";
		}
		else if (event.equalsIgnoreCase("30118-06.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(BOOK_OF_REFORM, 1);
			st.takeItems(HUGE_NAIL, 1);
			st.giveItems(LETTER_OF_INTRODUCTION, 1);
		}
		// SLA
		else if (event.equalsIgnoreCase("30666-04.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LETTER_OF_INTRODUCTION, 1);
			st.giveItems(SLA_LETTER, 1);
		}
		// KAKAN
		else if (event.equalsIgnoreCase("30669-03.htm"))
		{
			if (st.getInt("cond") != 12)
			{
				st.set("cond", "12");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			
			if (_olMahumPilgrim_Kakan == null)
				_olMahumPilgrim_Kakan = addSpawn(OL_MAHUM_PILGRIM, -9282, -89975, -2331, 0, false, 300000, true);
			
			if (_crimsonWerewolf == null)
				_crimsonWerewolf = addSpawn(CRIMSON_WEREWOLF, -9382, -89852, -2333, 0, false, 300000, true);
			
			((Attackable) _crimsonWerewolf).addDamageHate(_olMahumPilgrim_Kakan, 0, 99999);
			_crimsonWerewolf.getAI().tryTo(IntentionType.ATTACK, _olMahumPilgrim_Kakan, false);
			
			// TODO : make Npc be able to attack Attackable.
			// ((Attackable) _olMahumPilgrim_Kakan).addDamageHate(_crimsonWerewolf, 0, 99999);
			// _olMahumPilgrim_Kakan.getAI().setIntention(IntentionType.ATTACK, _crimsonWerewolf);
		}
		// NYAKURI
		else if (event.equalsIgnoreCase("30670-03.htm"))
		{
			st.set("cond", "15");
			st.playSound(QuestState.SOUND_MIDDLE);
			
			if (_olMahumPilgrim_Nyakuri == null)
				_olMahumPilgrim_Nyakuri = addSpawn(OL_MAHUM_PILGRIM, 125947, -180049, -1778, 0, false, 300000, true);
			
			if (_krudelLizardman == null)
				_krudelLizardman = addSpawn(KRUDEL_LIZARDMAN, 126019, -179983, -1781, 0, false, 300000, true);
			
			((Attackable) _krudelLizardman).addDamageHate(_olMahumPilgrim_Nyakuri, 0, 99999);
			_krudelLizardman.getAI().tryTo(IntentionType.ATTACK, _olMahumPilgrim_Nyakuri, false);
			
			// TODO : make Npc be able to attack Attackable.
			// ((Attackable) _olMahumPilgrim_Nyakuri).addDamageHate(_krudelLizardman, 0, 99999);
			// _olMahumPilgrim_Nyakuri.getAI().setIntention(IntentionType.ATTACK, _krudelLizardman);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		// Despawns Crimson Werewolf
		if (name.equalsIgnoreCase("werewolf_despawn"))
		{
			if (npc == _crimsonWerewolf)
			{
				npc.broadcastNpcSay("Cowardly guy!");
				npc.getAttack().stop();
				npc.deleteMe();
			}
		}
		
		return null;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getClassId() == ClassId.CLERIC || player.getClassId() == ClassId.SHILLIEN_ORACLE)
					htmltext = (player.getLevel() < 39) ? "30118-01.htm" : "30118-03.htm";
				else
					htmltext = "30118-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PUPINA:
						if (cond < 3)
							htmltext = "30118-04a.htm";
						else if (cond == 3)
							htmltext = "30118-05.htm";
						else if (cond > 3)
							htmltext = "30118-07.htm";
						break;
					
					case SLA:
						if (cond == 4)
							htmltext = "30666-01.htm";
						else if (cond > 4 && cond < 10)
							htmltext = "30666-05.htm";
						else if (cond == 10)
						{
							htmltext = "30666-06.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(OL_MAHUM_MONEY, 1);
							st.giveItems(GREETINGS, 3);
						}
						else if (cond > 10 && cond < 20)
							htmltext = "30666-06.htm";
						else if (cond == 20)
						{
							htmltext = "30666-07.htm";
							st.takeItems(KATARI_LETTER, 1);
							st.takeItems(KAKAN_LETTER, 1);
							st.takeItems(NYAKURI_LETTER, 1);
							st.takeItems(RAMUS_LETTER, 1);
							st.giveItems(MARK_OF_REFORMER, 1);
							st.rewardExpAndSp(164032, 17500);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case KATARI:
						if (cond == 5 || cond == 6 || cond == 7)
						{
							htmltext = "30668-01.htm";
							
							if (cond == 5)
							{
								st.set("cond", "6");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.takeItems(SLA_LETTER, 1);
							}
							
							if (_olMahumPilgrim_Katari == null)
								_olMahumPilgrim_Katari = addSpawn(OL_MAHUM_PILGRIM, -4015, 40141, -3664, 0, false, 300000, true);
							
							if (_olMahumInspector == null)
								_olMahumInspector = addSpawn(OL_MAHUM_INSPECTOR, -4034, 40201, -3665, 0, false, 300000, true);
							
							((Attackable) _olMahumInspector).addDamageHate(_olMahumPilgrim_Katari, 0, 99999);
							_olMahumInspector.getAI().tryTo(IntentionType.ATTACK, _olMahumPilgrim_Katari, false);
							
							// TODO : make Npc be able to attack Attackable.
							// ((Attackable) _olMahumPilgrim_Katari).addDamageHate(_olMahumInspector, 0, 99999);
							// _olMahumPilgrim_Katari.getAI().setIntention(IntentionType.ATTACK, _olMahumInspector);
						}
						else if (cond == 8)
						{
							htmltext = "30668-02.htm";
							
							if (_olMahumBetrayer == null)
							{
								_olMahumBetrayer = addSpawn(OL_MAHUM_BETRAYER, -4106, 40174, -3660, 0, false, 60000, true);
								_olMahumBetrayer.forceRunStance();
								_olMahumBetrayer.getAI().tryTo(IntentionType.MOVE_TO, new Location(-7732, 36787, -3709), null);
							}
						}
						else if (cond == 9)
						{
							htmltext = "30668-03.htm";
							st.set("cond", "10");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(LETTER_OF_BETRAYER, 1);
							st.giveItems(KATARI_LETTER, 1);
						}
						else if (cond > 9)
							htmltext = "30668-04.htm";
						break;
					
					case OL_MAHUM_PILGRIM:
						if (cond == 7)
						{
							htmltext = "30732-01.htm";
							st.set("cond", "8");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.giveItems(OL_MAHUM_MONEY, 1);
						}
						break;
					
					case KAKAN:
						if (cond == 11 || cond == 12)
							htmltext = "30669-01.htm";
						else if (cond == 13)
						{
							htmltext = "30669-04.htm";
							st.set("cond", "14");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(KAKAN_LETTER, 1);
						}
						else if (cond > 13)
							htmltext = "30669-04.htm";
						break;
					
					case NYAKURI:
						if (cond == 14 || cond == 15)
							htmltext = "30670-01.htm";
						else if (cond == 16)
						{
							htmltext = "30670-04.htm";
							st.set("cond", "17");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(NYAKURI_LETTER, 1);
						}
						else if (cond > 16)
							htmltext = "30670-04.htm";
						break;
					
					case RAMUS:
						if (cond == 17)
						{
							htmltext = "30667-01.htm";
							st.set("cond", "18");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREETINGS, 1);
							st.giveItems(UNDEAD_LIST, 1);
						}
						else if (cond == 18)
							htmltext = "30667-02.htm";
						else if (cond == 19)
						{
							htmltext = "30667-03.htm";
							st.set("cond", "20");
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(BONE_FRAGMENT_4, 1);
							st.takeItems(BONE_FRAGMENT_5, 1);
							st.takeItems(BONE_FRAGMENT_6, 1);
							st.takeItems(BONE_FRAGMENT_7, 1);
							st.takeItems(BONE_FRAGMENT_8, 1);
							st.takeItems(UNDEAD_LIST, 1);
							st.giveItems(RAMUS_LETTER, 1);
						}
						else if (cond > 19)
							htmltext = "30667-03.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case NAMELESS_REVENANT:
				if ((cond == 1 || cond == 2) && skill != null && skill.getId() == 1031)
					npc.setScriptValue(1);
				break;
			
			case CRIMSON_WEREWOLF:
				if (cond == 12 && npc.isScriptValue(0) && (skill == null || !ArraysUtil.contains(ALLOWED_SKILLS, skill.getId())))
				{
					npc.setScriptValue(1);
					startQuestTimer("werewolf_despawn", npc, null, 1000);
				}
				break;
		}
		
		return null;
	}
	
	@Override
	public String onDecay(Npc npc)
	{
		if (npc == _olMahumPilgrim_Katari)
		{
			_olMahumPilgrim_Katari = null;
		}
		else if (npc == _olMahumInspector)
		{
			_olMahumInspector = null;
		}
		else if (npc == _olMahumBetrayer)
		{
			_olMahumBetrayer = null;
		}
		else if (npc == _olMahumPilgrim_Kakan)
		{
			_olMahumPilgrim_Kakan = null;
		}
		else if (npc == _crimsonWerewolf)
		{
			_crimsonWerewolf = null;
		}
		else if (npc == _olMahumPilgrim_Nyakuri)
		{
			_olMahumPilgrim_Nyakuri = null;
		}
		else if (npc == _krudelLizardman)
		{
			_krudelLizardman = null;
		}
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		int cond = st.getInt("cond");
		switch (npc.getNpcId())
		{
			case NAMELESS_REVENANT:
				if ((cond == 1 || cond == 2) && npc.isScriptValue(1) && st.dropItemsAlways(RIPPED_DIARY, 1, 7))
				{
					st.set("cond", "2");
					st.takeItems(RIPPED_DIARY, -1);
					addSpawn(ARURAUNE, npc, false, 300000, true);
				}
				break;
			
			case ARURAUNE:
				if (cond == 2)
				{
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HUGE_NAIL, 1);
					npc.broadcastNpcSay("The concealed truth will always be revealed...!");
				}
				break;
			
			case OL_MAHUM_INSPECTOR:
				if (cond == 6)
				{
					st.set("cond", "7");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
			
			case OL_MAHUM_BETRAYER:
				if (cond == 8)
				{
					st.set("cond", "9");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(LETTER_OF_BETRAYER, 1);
				}
				break;
			
			case CRIMSON_WEREWOLF:
				if (cond == 12 && npc.isScriptValue(0))
				{
					st.set("cond", "13");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
			
			case KRUDEL_LIZARDMAN:
				if (cond == 15)
				{
					st.set("cond", "16");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				break;
			
			case SILENT_HORROR:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_4))
				{
					st.giveItems(BONE_FRAGMENT_4, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_LORD:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_5))
				{
					st.giveItems(BONE_FRAGMENT_5, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_MARKSMAN:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_6))
				{
					st.giveItems(BONE_FRAGMENT_6, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_7, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case MISERY_SKELETON:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_7))
				{
					st.giveItems(BONE_FRAGMENT_7, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_8))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case SKELETON_ARCHER:
				if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_8))
				{
					st.giveItems(BONE_FRAGMENT_8, 1);
					if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7))
					{
						st.set("cond", "19");
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
		}
		
		return null;
	}
}