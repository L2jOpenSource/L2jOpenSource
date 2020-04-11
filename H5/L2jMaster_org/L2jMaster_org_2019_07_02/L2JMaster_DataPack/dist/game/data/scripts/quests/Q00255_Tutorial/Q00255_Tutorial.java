/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package quests.Q00255_Tutorial;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.enums.audio.Sound;
import com.l2jserver.gameserver.enums.audio.Voice;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureAttacked;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSit;
import com.l2jserver.gameserver.model.events.impl.character.player.inventory.OnPlayerItemPickup;
import com.l2jserver.gameserver.model.events.listeners.ConsumerEventListener;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;

import ZeuS.ZeuS;

/**
 * Tutorial (255)
 * @author Zealar
 * @since 2.6.0.0
 */
public class Q00255_Tutorial extends Quest
{
	// Npc
	private static final int ROIEN = 30008;
	private static final int NEWBIE_HELPER_HUMAN_FIGHTER = 30009;
	private static final int GALLINT = 30017;
	private static final int NEWBIE_HELPER_HUMAN_MAGE = 30019;
	private static final int MITRAELL = 30129;
	private static final int NEWBIE_HELPER_DARK_ELF = 30131;
	private static final int NERUPA = 30370;
	private static final int NEWBIE_HELPER_ELF = 30400;
	private static final int LAFERON = 30528;
	private static final int NEWBIE_HELPER_DWARF = 30530;
	private static final int VULKUS = 30573;
	private static final int NEWBIE_HELPER_ORC = 30575;
	private static final int PERWAN = 32133;
	private static final int NEWBIE_HELPER_KAMAEL = 32134;
	
	// Monster
	private static final int TUTORIAL_GREMLIN = 18342;
	
	// Items
	private static final int SOULSHOT_NO_GRADE_FOR_BEGINNERS = 5789;
	private static final int SPIRITSHOT_NO_GRADE_FOR_BEGINNERS = 5790;
	private static final int BLUE_GEMSTONE = 6353;
	private static final int TUTORIAL_GUIDE = 5588;
	
	// Quest items
	private static final int RECOMMENDATION_1 = 1067;
	private static final int RECOMMENDATION_2 = 1068;
	private static final int LEAF_OF_THE_MOTHER_TREE = 1069;
	private static final int BLOOD_OF_MITRAELL = 1070;
	private static final int LICENSE_OF_MINER = 1498;
	private static final int VOUCHER_OF_FLAME = 1496;
	private static final int DIPLOMA = 9881;
	
	// Territory wars
	private static final int TW_GLUDIO = 81;
	private static final int TW_DION = 82;
	private static final int TW_GIRAN = 83;
	private static final int TW_OREN = 84;
	private static final int TW_ADEN = 85;
	private static final int TW_HEINE = 86;
	private static final int TW_GODDARD = 87;
	private static final int TW_RUNE = 88;
	private static final int TW_SCHUTTGART = 89;
	
	// Connected quests
	private static final int Q10276_MUTATED_KANEUS_GLUDIO = 10276;
	private static final int Q10277_MUTATED_KANEUS_DION = 10277;
	private static final int Q10278_MUTATED_KANEUS_HEINE = 10278;
	private static final int Q10279_MUTATED_KANEUS_OREN = 10279;
	private static final int Q10280_MUTATED_KANEUS_SCHUTTGART = 10280;
	private static final int Q10281_MUTATED_KANEUS_RUNE = 10281;
	private static final int Q192_SEVEN_SIGNS_SERIES_OF_DOUBT = 192;
	private static final int Q10292_SEVEN_SIGNS_GIRL_OF_DOUBT = 10292;
	private static final int Q234_FATES_WHISPER = 234;
	private static final int Q128_PAILAKA_SONG_OF_ICE_AND_FIRE = 128;
	private static final int Q129_PAILAKA_DEVILS_LEGACY = 129;
	private static final int Q144_PAIRAKA_WOUNDED_DRAGON = 144;
	
	private static final int Q729_PROTECT_THE_TERRITORY_CATAPULT = 729;
	private static final int Q730_PROTECT_THE_SUPPLIES_SAFE = 730;
	private static final int Q731_PROTECT_THE_MILITARY_ASSOCIATION_LEADER = 731;
	private static final int Q732_PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER = 732;
	private static final int Q733_PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER = 733;
	
	private static final int Q201_TUTORIAL_HUMAN_FIGHTER = 201;
	private static final int Q202_TUTORIAL_HUMAN_MAGE = 202;
	private static final int Q203_TUTORIAL_ELF = 203;
	private static final int Q204_TUTORIAL_DARK_ELF = 204;
	private static final int Q205_TUTORIAL_ORC = 205;
	private static final int Q206_TUTORIAL_DWARF = 206;
	
	private static final int Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO = 717;
	private static final int Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION = 718;
	private static final int Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN = 719;
	private static final int Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN = 720;
	private static final int Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN = 721;
	private static final int Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL = 722;
	private static final int Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD = 723;
	private static final int Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE = 724;
	private static final int Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART = 725;
	private static final int Q728_TERRITORY_WAR = 728;
	
	// Quests
	public Q00255_Tutorial()
	{
		super(255, Q00255_Tutorial.class.getSimpleName(), "Tutorial");
		if (!Config.DISABLE_TUTORIAL)
		{
			setOnEnterWorld(true);
			registerTutorialEvent();
			registerTutorialClientEvent();
			registerTutorialQuestionMark();
			registerTutorialCmd();
			
			int[] list =
			{
				ROIEN,
				NEWBIE_HELPER_HUMAN_FIGHTER,
				GALLINT,
				NEWBIE_HELPER_HUMAN_MAGE,
				MITRAELL,
				NEWBIE_HELPER_DARK_ELF,
				NERUPA,
				NEWBIE_HELPER_ELF,
				LAFERON,
				NEWBIE_HELPER_DWARF,
				VULKUS,
				NEWBIE_HELPER_ORC,
				PERWAN,
				NEWBIE_HELPER_KAMAEL
			};
			addStartNpc(list);
			addFirstTalkId(list);
			addTalkId(list);
			addKillId(TUTORIAL_GREMLIN);
		}
	}
	
	/**
	 * Handle only tutorial_close_
	 */
	@Override
	public void onTutorialEvent(L2PcInstance player, String event)
	{
		// Prevent codes from custom class master
		if (event.startsWith("CO"))
		{
			return;
		}
		
		if (Config.ZEUS_ACTIVE && ZeuS.zeusByPass(player, event))
		{
			return;
		}
		
		int pass = Integer.parseInt(event.substring(15));
		if (pass < 302)
		{
			pass = -pass;
		}
		tutorialEvent(player, pass);
	}
	
	/**
	 * Handle client events 1, 2, 8
	 */
	@Override
	public void onTutorialClientEvent(L2PcInstance player, int event)
	{
		tutorialEvent(player, event);
	}
	
	@Override
	public String onTutorialQuestionMark(L2PcInstance player, int number)
	{
		questionMarkClicked(player, number);
		return super.onTutorialQuestionMark(player, number);
	}
	
	@Override
	public String onTutorialCmd(L2PcInstance player, String command)
	{
		selectFromMenu(player, Integer.parseInt(command));
		return super.onTutorialCmd(player, command);
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		userConnected(player);
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LEVEL_CHANGED, (OnPlayerLevelChanged event) ->
		{
			levelUp(event.getActiveChar(), event.getNewLevel());
		}, player));
		
		return super.onEnterWorld(player);
	}
	
	private void enableTutorialEvent(QuestState qs, int eventStatus)
	{
		L2PcInstance player = qs.getPlayer();
		
		if (((eventStatus & (1048576 | 2097152)) != 0))
		{
			if (!player.hasListener(EventType.ON_PLAYER_ITEM_PICKUP))
			{
				player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_ITEM_PICKUP, (OnPlayerItemPickup event) ->
				{
					if ((event.getItem().getId() == BLUE_GEMSTONE) && ((qs.getMemoState() & 1048576) != 0))
					{
						tutorialEvent(event.getActiveChar(), 1048576);
					}
					
					if ((event.getItem().getId() == 57) && ((qs.getMemoState() & 2097152) != 0))
					{
						tutorialEvent(event.getActiveChar(), 2097152);
					}
				}, player));
			}
		}
		else if (player.hasListener(EventType.ON_PLAYER_ITEM_PICKUP))
		{
			player.removeListenerIf(EventType.ON_PLAYER_ITEM_PICKUP, listener -> listener.getOwner() == player);
		}
		
		if ((eventStatus & 8388608) != 0)
		{
			if (!player.hasListener(EventType.ON_PLAYER_SIT))
			{
				player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_SIT, (OnPlayerSit event) ->
				{
					tutorialEvent(player, 8388608);
				}, player));
			}
		}
		else if (player.hasListener(EventType.ON_PLAYER_SIT))
		{
			player.removeListenerIf(EventType.ON_PLAYER_SIT, listener -> listener.getOwner() == player);
		}
		
		if ((eventStatus & 256) != 0)
		{
			if (!player.hasListener(EventType.ON_CREATURE_ATTACKED))
			{
				player.addListener(new ConsumerEventListener(player, EventType.ON_CREATURE_ATTACKED, (OnCreatureAttacked event) ->
				{
					L2PcInstance pp = event.getTarget().getActingPlayer();
					if ((pp != null) && (pp.getCurrentHp() <= (pp.getStat().getMaxHp() * 0.3)))
					{
						tutorialEvent(pp, 256);
					}
				}, player));
			}
		}
		else
		{
			player.removeListenerIf(EventType.ON_CREATURE_ATTACKED, listener -> listener.getOwner() == player);
		}
		qs.enableTutorialEvent(player, eventStatus);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance talker)
	{
		final QuestState qs = getQuestState(talker, true);
		switch (npc.getId())
		{
			case ROIEN:
				talkRoien(talker, qs);
				break;
			case NEWBIE_HELPER_HUMAN_FIGHTER:
				talkCarl(npc, talker, qs);
				break;
			case GALLINT:
				talkGallin(talker, qs);
				break;
			case NEWBIE_HELPER_HUMAN_MAGE:
				talkDoff(npc, talker, qs);
				break;
			case MITRAELL:
				talkJundin(talker, qs);
				break;
			case NEWBIE_HELPER_DARK_ELF:
				talkPoeny(npc, talker, qs);
				break;
			case NERUPA:
				talkNerupa(talker, qs);
				break;
			case NEWBIE_HELPER_ELF:
				talkMotherTemp(npc, talker, qs);
				break;
			case LAFERON:
				talkForemanLaferon(talker, qs);
				break;
			case NEWBIE_HELPER_DWARF:
				talkMinerMai(npc, talker, qs);
				break;
			case VULKUS:
				talkGuardianVullkus(talker, qs);
				break;
			case NEWBIE_HELPER_ORC:
			{
				talkShelaPriestess(npc, talker, qs);
				break;
			}
			case PERWAN:
				talkSubelderPerwan(talker, qs);
				break;
			case NEWBIE_HELPER_KAMAEL:
			{
				talkHelperKrenisk(npc, talker, qs);
				break;
			}
		}
		return "";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance talker)
	{
		final int eventId = Integer.parseInt(event);
		
		if (eventId > 1000000)
		{
			fireEvent(eventId, talker);
			return super.onAdvEvent(event, npc, talker);
		}
		
		if (talker.isDead())
		{
			return super.onAdvEvent(event, npc, talker);
		}
		
		final QuestState qs = getQuestState(talker, true);
		switch (npc.getId())
		{
			case NEWBIE_HELPER_HUMAN_FIGHTER:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						qs.playSound(Voice.TUTORIAL_VOICE_009A);
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010A);
				}
				break;
			}
			case NEWBIE_HELPER_HUMAN_MAGE:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						qs.playSound(Voice.TUTORIAL_VOICE_009B);
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010B);
				}
				break;
			}
			case NEWBIE_HELPER_DARK_ELF:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						if (!talker.isMageClass())
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009A);
						}
						else
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009B);
						}
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010D);
				}
				break;
			}
			case NEWBIE_HELPER_ELF:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						if (!talker.isMageClass())
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009A);
						}
						else
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009B);
						}
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010C);
				}
				break;
			}
			case NEWBIE_HELPER_DWARF:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						qs.playSound(Voice.TUTORIAL_VOICE_009A);
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010F);
				}
				break;
			}
			case NEWBIE_HELPER_ORC:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						if (!talker.isMageClass())
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009A);
						}
						else
						{
							qs.playSound(Voice.TUTORIAL_VOICE_009C);
						}
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010E);
				}
				break;
			}
			case NEWBIE_HELPER_KAMAEL:
			{
				switch (qs.getMemoStateEx(1))
				{
					case 0:
						qs.playSound(Voice.TUTORIAL_VOICE_009A);
						qs.setMemoStateEx(1, 1);
						break;
					case 3:
						qs.playSound(Voice.TUTORIAL_VOICE_010G);
				}
				break;
			}
			case ROIEN:
			{
				if (eventId == ROIEN)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventRoien(eventId, talker, npc, qs);
				break;
			}
			case GALLINT:
			{
				eventGallin(eventId, talker, npc, qs);
				break;
			}
			case MITRAELL:
			{
				if (eventId == MITRAELL)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventJundin(eventId, talker, npc, qs);
				break;
			}
			case NERUPA:
			{
				if (eventId == NERUPA)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventNerupa(eventId, talker, npc, qs);
				break;
			}
			case LAFERON:
			{
				if (eventId == LAFERON)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventForemanLaferon(eventId, talker, npc, qs);
				break;
			}
			case VULKUS:
			{
				if (eventId == VULKUS)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventGuardianVullkus(eventId, talker, npc, qs);
				break;
			}
			case PERWAN:
			{
				if (eventId == PERWAN)
				{
					if (qs.getMemoStateEx(1) >= 4)
					{
						qs.showQuestionMark(talker, 7);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.playSound(Voice.TUTORIAL_VOICE_025_1000);
					}
					break;
				}
				eventSubelderPerwan(eventId, talker, npc, qs);
				break;
			}
		}
		return "";
	}
	
	private void fireEvent(int timer_id, L2PcInstance talker)
	{
		if ((talker == null) || talker.isDead() || !talker.isPlayer() || (timer_id <= 1000000))
		{
			return;
		}
		
		final QuestState qs = talker.getQuestState(Q00255_Tutorial.class.getSimpleName());
		
		switch (qs.getMemoStateEx(1))
		{
			case -2:
			{
				switch (talker.getClassId())
				{
					case fighter:
						qs.playSound(Voice.TUTORIAL_VOICE_001A_2000);
						showTutorialHTML(talker, "tutorial-human-fighter-001.htm");
						break;
					case mage:
						qs.playSound(Voice.TUTORIAL_VOICE_001B_2000);
						showTutorialHTML(talker, "tutorial-human-mage-001.htm");
						break;
					case elvenFighter:
						qs.playSound(Voice.TUTORIAL_VOICE_001C_2000);
						showTutorialHTML(talker, "tutorial-elven-fighter-001.htm");
						break;
					case elvenMage:
						qs.playSound(Voice.TUTORIAL_VOICE_001D_2000);
						showTutorialHTML(talker, "tutorial-elven-mage-001.htm");
						break;
					case darkFighter:
						qs.playSound(Voice.TUTORIAL_VOICE_001E_2000);
						showTutorialHTML(talker, "tutorial-delf-fighter-001.htm");
						break;
					case darkMage:
						qs.playSound(Voice.TUTORIAL_VOICE_001F_2000);
						showTutorialHTML(talker, "tutorial-delf-mage-001.htm");
						break;
					case orcFighter:
						qs.playSound(Voice.TUTORIAL_VOICE_001G_2000);
						showTutorialHTML(talker, "tutorial-orc-fighter-001.htm");
						break;
					case orcMage:
						qs.playSound(Voice.TUTORIAL_VOICE_001H_2000);
						showTutorialHTML(talker, "tutorial-orc-mage-001.htm");
						break;
					case dwarvenFighter:
						qs.playSound(Voice.TUTORIAL_VOICE_001I_2000);
						showTutorialHTML(talker, "tutorial-dwarven-fighter-001.htm");
						break;
					case maleSoldier:
					case femaleSoldier:
						qs.playSound(Voice.TUTORIAL_VOICE_001K_2000);
						showTutorialHTML(talker, "tutorial-kamael-001.htm");
						break;
				}
				if (!qs.hasQuestItems(TUTORIAL_GUIDE))
				{
					qs.giveItems(TUTORIAL_GUIDE, 1);
				}
				qs.startQuestTimer((talker.getObjectId() + 1000000) + "", 30000);
				qs.setMemoStateEx(1, -3);
				break;
			}
			case -3:
				qs.playSound(Voice.TUTORIAL_VOICE_002_1000);
				break;
			case -4:
				qs.playSound(Voice.TUTORIAL_VOICE_008_1000);
				qs.setMemoStateEx(1, -5);
				
		}
	}
	
	private void tutorialEvent(L2PcInstance talker, int event_id)
	{
		final QuestState qs = talker.getQuestState(this.getClass().getSimpleName());
		if (qs == null)
		{
			return;
		}
		// TODO is custom!
		if (event_id == 0)
		{
			qs.closeTutorialHtml(talker);
			return;
		}
		
		int memoState = qs.getMemoState();
		int memoFlag = memoState & 2147483632;
		
		if (event_id < 0)
		{
			switch (Math.abs(event_id))
			{
				case 1:
					qs.closeTutorialHtml(talker);
					qs.playSound(Voice.TUTORIAL_VOICE_006_3500);
					qs.showQuestionMark(talker, 1);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.startQuestTimer((talker.getObjectId() + 1000000) + "", 30000);
					if (qs.getMemoStateEx(1) < 0)
					{
						qs.setMemoStateEx(1, -4);
					}
					break;
				case 2:
					qs.playSound(Voice.TUTORIAL_VOICE_003_2000);
					showTutorialHTML(talker, "tutorial-02.htm");
					enableTutorialEvent(qs, memoFlag | 1);
					
					if (qs.getMemoStateEx(1) < 0)
					{
						qs.setMemoStateEx(1, -5);
					}
					break;
				case 3:
					showTutorialHTML(talker, "tutorial-03.htm");
					enableTutorialEvent(qs, memoFlag | 2);
					break;
				case 4:
					showTutorialHTML(talker, "tutorial-04.htm");
					enableTutorialEvent(qs, memoFlag | 4);
					break;
				case 5:
					showTutorialHTML(talker, "tutorial-05.htm");
					enableTutorialEvent(qs, memoFlag | 8);
					break;
				case 6:
					showTutorialHTML(talker, "tutorial-06.htm");
					enableTutorialEvent(qs, memoFlag | 16);
					break;
				case 7:
					showTutorialHTML(talker, "tutorial-100.htm");
					enableTutorialEvent(qs, memoFlag);
					break;
				case 8:
					showTutorialHTML(talker, "tutorial-101.htm");
					enableTutorialEvent(qs, memoFlag);
					break;
				case 9:
					showTutorialHTML(talker, "tutorial-102.htm");
					enableTutorialEvent(qs, memoFlag);
					break;
				case 10:
					showTutorialHTML(talker, "tutorial-103.htm");
					enableTutorialEvent(qs, memoFlag);
					break;
				case 11:
					showTutorialHTML(talker, "tutorial-104.htm");
					enableTutorialEvent(qs, memoFlag);
					break;
				case 12:
					qs.closeTutorialHtml(talker);
					break;
			}
			return;
		}
		switch (event_id)
		{
			case 1:
				if (talker.getLevel() < 6)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_004_5000);
					showTutorialHTML(talker, "tutorial-03.htm");
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					enableTutorialEvent(qs, memoFlag | 2);
				}
				break;
			case 2:
				if (talker.getLevel() < 6)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_005_5000);
					showTutorialHTML(talker, "tutorial-05.htm");
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					enableTutorialEvent(qs, memoFlag | 8);
				}
				break;
			case 8:
				if (talker.getLevel() < 6)
				{
					showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					switch (talker.getClassId())
					{
						case fighter:
							qs.addRadar(-71424, 258336, -3109);
							break;
						case mage:
							qs.addRadar(-91036, 248044, -3568);
							break;
						case elvenFighter:
						case elvenMage:
							qs.addRadar(46112, 41200, -3504);
							break;
						case darkFighter:
						case darkMage:
							qs.addRadar(28384, 11056, -4233);
							break;
						case orcFighter:
						case orcMage:
							qs.addRadar(-56736, -113680, -672);
							break;
						case dwarvenFighter:
							qs.addRadar(108567, -173994, -406);
							break;
						case maleSoldier:
						case femaleSoldier:
							qs.addRadar(-125872, 38016, 1251);
							break;
					}
					qs.playSound(Voice.TUTORIAL_VOICE_007_3500);
					qs.setMemoState(memoFlag | 2);
					if (qs.getMemoStateEx(1) < 0)
					{
						qs.setMemoStateEx(1, -5);
					}
				}
				break;
			case 256:
				if (talker.getLevel() < 6)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_017_1000);
					qs.showQuestionMark(talker, 10);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~256);
					enableTutorialEvent(qs, (memoFlag & ~256) | 8388608);
				}
				break;
			case 512:
				qs.showQuestionMark(talker, 8);
				qs.playSound(Voice.TUTORIAL_VOICE_016_1000);
				qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				qs.setMemoState(memoState & ~512);
				break;
			case 1024:
				qs.setMemoState(memoState & ~1024);
				switch (talker.getClassId())
				{
					case fighter:
						qs.addRadar(-83020, 242553, -3718);
						break;
					case elvenFighter:
						qs.addRadar(45061, 52468, -2796);
						break;
					case darkFighter:
						qs.addRadar(10447, 14620, -4242);
						break;
					case orcFighter:
						qs.addRadar(-46389, -113905, -21);
						break;
					case dwarvenFighter:
						qs.addRadar(115271, -182692, -1445);
						break;
					case maleSoldier:
					case femaleSoldier:
						qs.addRadar(-118132, 42788, 723);
						break;
				}
				if (!talker.isMageClass())
				{
					qs.playSound(Voice.TUTORIAL_VOICE_014_1000);
					qs.showQuestionMark(talker, 9);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				}
				enableTutorialEvent(qs, memoFlag | 134217728);
				qs.setMemoState(memoState & ~1024);
				break;
			case 134217728:
				qs.showQuestionMark(talker, 24);
				qs.playSound(Voice.TUTORIAL_VOICE_020_1000);
				qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				enableTutorialEvent(qs, memoFlag & ~134217728);
				qs.setMemoState(memoState & ~134217728);
				enableTutorialEvent(qs, memoFlag | 2048);
				break;
			case 2048:
				if (talker.isMageClass())
				{
					qs.playSound(Voice.TUTORIAL_VOICE_019_1000);
					qs.showQuestionMark(talker, 11);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					switch (talker.getClassId())
					{
						case mage:
							qs.addRadar(-84981, 244764, -3726);
							break;
						case elvenMage:
							qs.addRadar(45701, 52459, -2796);
							break;
						case darkMage:
							qs.addRadar(10344, 14445, -4242);
							break;
						case orcMage:
							qs.addRadar(-46225, -113312, -21);
							break;
					}
					qs.setMemoState(memoState & ~2048);
				}
				enableTutorialEvent(qs, memoFlag | 268435456);
				break;
			case 268435456:
				if (talker.getClassId() == ClassId.fighter)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_021_1000);
					qs.showQuestionMark(talker, 25);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~268435456);
				}
				enableTutorialEvent(qs, memoFlag | 536870912);
				break;
			case 536870912:
				switch (talker.getClassId())
				{
					case dwarvenFighter:
					case mage:
					case elvenFighter:
					case elvenMage:
					case darkMage:
					case darkFighter:
					case maleSoldier:
					case femaleSoldier:
						qs.playSound(Voice.TUTORIAL_VOICE_021_1000);
						qs.showQuestionMark(talker, 25);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.setMemoState(memoState & ~536870912);
						break;
					default:
						qs.playSound(Voice.TUTORIAL_VOICE_030_1000);
						qs.showQuestionMark(talker, 27);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.setMemoState(memoState & ~536870912);
				}
				enableTutorialEvent(qs, memoFlag | 1073741824);
				break;
			case 1073741824:
				switch (talker.getClassId())
				{
					case orcFighter:
					case orcMage:
						qs.playSound(Voice.TUTORIAL_VOICE_021_1000);
						qs.showQuestionMark(talker, 25);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						qs.setMemoState(memoState & ~1073741824);
				}
				enableTutorialEvent(qs, memoFlag | 67108864);
				break;
			case 67108864:
				qs.showQuestionMark(talker, 17);
				qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				qs.setMemoState(memoState & ~67108864);
				enableTutorialEvent(qs, memoFlag | 4096);
				break;
			case 4096:
				qs.showQuestionMark(talker, 13);
				qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				qs.setMemoState(memoState & ~4096);
				enableTutorialEvent(qs, memoFlag | 16777216);
				break;
			case 16777216:
				if (talker.getClassId().getRace() != Race.KAMAEL)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_023_1000);
					qs.showQuestionMark(talker, 15);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~16777216);
				}
				enableTutorialEvent(qs, memoFlag | 32);
				break;
			case 16384:
			{
				if ((talker.getClassId().getRace() == Race.KAMAEL) && (talker.getClassId().level() == 1))
				{
					qs.playSound(Voice.TUTORIAL_VOICE_028_1000);
					qs.showQuestionMark(talker, 15);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~16384);
				}
				enableTutorialEvent(qs, memoFlag | 64);
				break;
			}
			case 33554432:
			{
				if (getOneTimeQuestFlag(talker, Q234_FATES_WHISPER) == 0)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_024_1000);
					qs.showQuestionMark(talker, 16);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~33554432);
				}
				enableTutorialEvent(qs, memoFlag | 32768);
				break;
			}
			case 32768:
			{
				if (getOneTimeQuestFlag(talker, Q234_FATES_WHISPER) == 1)
				{
					qs.showQuestionMark(talker, 29);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~32768);
				}
				break;
			}
			case 32:
			{
				if (getOneTimeQuestFlag(talker, Q128_PAILAKA_SONG_OF_ICE_AND_FIRE) == 0)
				{
					qs.showQuestionMark(talker, 30);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~32);
				}
				enableTutorialEvent(qs, memoFlag | 16384);
				break;
			}
			case 64:
			{
				if (getOneTimeQuestFlag(talker, Q129_PAILAKA_DEVILS_LEGACY) == 0)
				{
					qs.showQuestionMark(talker, 31);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~64);
				}
				enableTutorialEvent(qs, memoFlag | 128);
				break;
			}
			case 128:
			{
				if (getOneTimeQuestFlag(talker, Q144_PAIRAKA_WOUNDED_DRAGON) == 0)
				{
					qs.showQuestionMark(talker, 32);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~128);
				}
				enableTutorialEvent(qs, memoFlag | 33554432);
				break;
			}
			case 2097152:
			{
				if (talker.getLevel() < 6)
				{
					qs.showQuestionMark(talker, 23);
					qs.playSound(Voice.TUTORIAL_VOICE_012_1000);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~2097152);
				}
				break;
			}
			case 1048576:
			{
				if (talker.getLevel() < 6)
				{
					qs.showQuestionMark(talker, 5);
					qs.playSound(Voice.TUTORIAL_VOICE_013_1000);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					qs.setMemoState(memoState & ~1048576);
				}
				break;
			}
			case 8388608:
			{
				if (talker.getLevel() < 6)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_018_1000);
					showTutorialHTML(talker, "tutorial-21z.htm");
					qs.setMemoState(memoState & ~8388608);
					enableTutorialEvent(qs, (memoFlag & ~8388608));
				}
				break;
			}
		}
	}
	
	private void levelUp(L2PcInstance player, int level)
	{
		switch (level)
		{
			case 5:
				tutorialEvent(player, 1024);
				break;
			case 6:
				tutorialEvent(player, 134217728);
				break;
			case 7:
				tutorialEvent(player, 2048);
				break;
			case 9:
				tutorialEvent(player, 268435456);
				break;
			case 10:
				tutorialEvent(player, 536870912);
				break;
			case 12:
				tutorialEvent(player, 1073741824);
				break;
			case 15:
				tutorialEvent(player, 67108864);
				break;
			case 18:
				tutorialEvent(player, 4096);
				if (!haveMemo(player, Q10276_MUTATED_KANEUS_GLUDIO) || (getOneTimeQuestFlag(player, Q10276_MUTATED_KANEUS_GLUDIO) == 0))
				{
					showTutorialHTML(player, "tw-gludio.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, -13900, 123822, -3112, 2);
				}
				break;
			case 28:
				if (!haveMemo(player, Q10277_MUTATED_KANEUS_DION) || (getOneTimeQuestFlag(player, Q10277_MUTATED_KANEUS_DION) == 0))
				{
					showTutorialHTML(player, "tw-dion.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 18199, 146081, -3080, 2);
				}
				break;
			case 35:
				tutorialEvent(player, 16777216);
				break;
			case 36:
				tutorialEvent(player, 32);
				break;
			case 38:
				if (!haveMemo(player, Q10278_MUTATED_KANEUS_HEINE) || (getOneTimeQuestFlag(player, Q10278_MUTATED_KANEUS_HEINE) == 0))
				{
					showTutorialHTML(player, "tw-heine.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 108384, 221563, -3592, 2);
				}
				break;
			case 39:
				if (player.getRace() == Race.KAMAEL)
				{
					tutorialEvent(player, 16384);
				}
				break;
			case 48:
				if (!haveMemo(player, Q10279_MUTATED_KANEUS_OREN) || (getOneTimeQuestFlag(player, Q10279_MUTATED_KANEUS_OREN) == 0))
				{
					showTutorialHTML(player, "tw-oren.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 81023, 56456, -1552, 2);
				}
				break;
			case 58:
				if (!haveMemo(player, Q10280_MUTATED_KANEUS_SCHUTTGART) || (getOneTimeQuestFlag(player, Q10280_MUTATED_KANEUS_SCHUTTGART) == 0))
				{
					showTutorialHTML(player, "tw-schuttgart.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 85868, -142164, -1342, 2);
				}
				break;
			case 61:
				tutorialEvent(player, 64);
				break;
			case 68:
				if (!haveMemo(player, Q10281_MUTATED_KANEUS_RUNE) || (getOneTimeQuestFlag(player, Q10281_MUTATED_KANEUS_RUNE) == 0))
				{
					showTutorialHTML(player, "tw-rune.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 42596, -47988, -800, 2);
				}
				break;
			case 73:
				tutorialEvent(player, 128);
				break;
			case 79:
				if (!haveMemo(player, Q192_SEVEN_SIGNS_SERIES_OF_DOUBT) || (getOneTimeQuestFlag(player, Q192_SEVEN_SIGNS_SERIES_OF_DOUBT) == 0))
				{
					showTutorialHTML(player, "tutorial-ss-79.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 81655, 54736, -1509, 2);
				}
				break;
			case 81:
				if (!haveMemo(player, Q10292_SEVEN_SIGNS_GIRL_OF_DOUBT) || (getOneTimeQuestFlag(player, Q10292_SEVEN_SIGNS_GIRL_OF_DOUBT) == 0))
				{
					showTutorialHTML(player, "tutorial-ss-81.htm");
					playSound(player, Sound.ITEMSOUND_QUEST_TUTORIAL);
					showRadar(player, 146995, 23755, -1984, 2);
				}
				break;
		}
	}
	
	private void selectFromMenu(L2PcInstance talker, int reply)
	{
		switch (reply)
		{
			case 1:
				showTutorialHTML(talker, "tutorial-22g.htm");
				break;
			case 2:
				showTutorialHTML(talker, "tutorial-22w.htm");
				break;
			case 3:
				showTutorialHTML(talker, "tutorial-22ap.htm");
				break;
			case 4:
				showTutorialHTML(talker, "tutorial-22ad.htm");
				break;
			case 5:
				showTutorialHTML(talker, "tutorial-22bt.htm");
				break;
			case 6:
				showTutorialHTML(talker, "tutorial-22bh.htm");
				break;
			case 7:
				showTutorialHTML(talker, "tutorial-22cs.htm");
				break;
			case 8:
				showTutorialHTML(talker, "tutorial-22cn.htm");
				break;
			case 9:
				showTutorialHTML(talker, "tutorial-22cw.htm");
				break;
			case 10:
				showTutorialHTML(talker, "tutorial-22db.htm");
				break;
			case 11:
				showTutorialHTML(talker, "tutorial-22dp.htm");
				break;
			case 12:
				showTutorialHTML(talker, "tutorial-22et.htm");
				break;
			case 13:
				showTutorialHTML(talker, "tutorial-22es.htm");
				break;
			case 14:
				showTutorialHTML(talker, "tutorial-22fp.htm");
				break;
			case 15:
				showTutorialHTML(talker, "tutorial-22fs.htm");
				break;
			case 16:
				showTutorialHTML(talker, "tutorial-22gs.htm");
				break;
			case 17:
				showTutorialHTML(talker, "tutorial-22ge.htm");
				break;
			case 18:
				showTutorialHTML(talker, "tutorial-22ko.htm");
				break;
			case 19:
				showTutorialHTML(talker, "tutorial-22kw.htm");
				break;
			case 20:
				showTutorialHTML(talker, "tutorial-22ns.htm");
				break;
			case 21:
				showTutorialHTML(talker, "tutorial-22nb.htm");
				break;
			case 22:
				showTutorialHTML(talker, "tutorial-22oa.htm");
				break;
			case 23:
				showTutorialHTML(talker, "tutorial-22op.htm");
				break;
			case 24:
				showTutorialHTML(talker, "tutorial-22ps.htm");
				break;
			case 25:
				showTutorialHTML(talker, "tutorial-22pp.htm");
				break;
			case 26:
				switch (talker.getClassId())
				{
					case warrior:
						showTutorialHTML(talker, "tutorial-22.htm");
						break;
					case knight:
						showTutorialHTML(talker, "tutorial-22a.htm");
						break;
					case rogue:
						showTutorialHTML(talker, "tutorial-22b.htm");
						break;
					case wizard:
						showTutorialHTML(talker, "tutorial-22c.htm");
						break;
					case cleric:
						showTutorialHTML(talker, "tutorial-22d.htm");
						break;
					case elvenKnight:
						showTutorialHTML(talker, "tutorial-22e.htm");
						break;
					case elvenScout:
						showTutorialHTML(talker, "tutorial-22f.htm");
						break;
					case elvenWizard:
						showTutorialHTML(talker, "tutorial-22g.htm");
						break;
					case oracle:
						showTutorialHTML(talker, "tutorial-22h.htm");
						break;
					case orcRaider:
						showTutorialHTML(talker, "tutorial-22i.htm");
						break;
					case orcMonk:
						showTutorialHTML(talker, "tutorial-22j.htm");
						break;
					case orcShaman:
						showTutorialHTML(talker, "tutorial-22k.htm");
						break;
					case scavenger:
						showTutorialHTML(talker, "tutorial-22l.htm");
						break;
					case artisan:
						showTutorialHTML(talker, "tutorial-22m.htm");
						break;
					case palusKnight:
						showTutorialHTML(talker, "tutorial-22n.htm");
						break;
					case assassin:
						showTutorialHTML(talker, "tutorial-22o.htm");
						break;
					case darkWizard:
						showTutorialHTML(talker, "tutorial-22p.htm");
						break;
					case shillienOracle:
						showTutorialHTML(talker, "tutorial-22q.htm");
						break;
					default:
						showTutorialHTML(talker, "tutorial-22qe.htm");
				}
				break;
			case 27:
				showTutorialHTML(talker, "tutorial-29.htm");
				break;
			case 28:
				showTutorialHTML(talker, "tutorial-28.htm");
				break;
			case 29:
				showTutorialHTML(talker, "tutorial-07a.htm");
				break;
			case 30:
				showTutorialHTML(talker, "tutorial-07b.htm");
				break;
			case 31:
				switch (talker.getClassId())
				{
					case trooper:
						showTutorialHTML(talker, "tutorial-28a.htm");
						break;
					case warder:
						showTutorialHTML(talker, "tutorial-28b.htm");
						break;
				}
				break;
			case 32:
				showTutorialHTML(talker, "tutorial-22qa.htm");
				break;
			case 33:
				switch (talker.getClassId())
				{
					case trooper:
						showTutorialHTML(talker, "tutorial-22qb.htm");
						break;
					case warder:
						showTutorialHTML(talker, "tutorial-22qc.htm");
						break;
				}
				break;
			case 34:
				showTutorialHTML(talker, "tutorial-22qd.htm");
				break;
		}
	}
	
	private void questionMarkClicked(L2PcInstance talker, int question_id)
	{
		QuestState qs = talker.getQuestState(this.getClass().getSimpleName());
		
		final int memoFlag = qs.getMemoState() & 2147483392;
		switch (question_id)
		{
			case 1:
				qs.playSound(Voice.TUTORIAL_VOICE_007_3500);
				if (qs.getMemoStateEx(1) < 0)
				{
					qs.setMemoStateEx(1, -5);
				}
				switch (talker.getClassId())
				{
					case fighter:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(-71424, 258336, -3109);
						break;
					case mage:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(-91036, 248044, -3568);
						break;
					case elvenFighter:
					case elvenMage:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(-91036, 248044, -3568);
						break;
					case darkFighter:
					case darkMage:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(28384, 11056, -4233);
						break;
					case orcFighter:
					case orcMage:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(-56736, -113680, -672);
						break;
					case dwarvenFighter:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(108567, -173994, -406);
						break;
					case maleSoldier:
					case femaleSoldier:
						showTutorialHTML(talker, "tutorial-human-fighter-007.htm");
						qs.addRadar(-125872, 38016, 1251);
						break;
				}
				qs.setMemoState(memoFlag | 2);
				break;
			case 2:
				switch (talker.getClassId())
				{
					case fighter:
						showTutorialHTML(talker, "tutorial-human-fighter-008.htm");
						break;
					case mage:
						showTutorialHTML(talker, "tutorial-human-mage-008.htm");
						break;
					case elvenFighter:
					case elvenMage:
						showTutorialHTML(talker, "tutorial-elf-008.htm");
						break;
					case darkFighter:
					case darkMage:
						showTutorialHTML(talker, "tutorial-delf-008.htm");
						break;
					case orcFighter:
					case orcMage:
						showTutorialHTML(talker, "tutorial-orc-008.htm");
						break;
					case dwarvenFighter:
						showTutorialHTML(talker, "tutorial-dwarven-fighter-008.htm");
						break;
					case maleSoldier:
					case femaleSoldier:
						showTutorialHTML(talker, "tutorial-kamael-008.htm");
						break;
				}
				qs.setMemoState(memoFlag | 2);
				break;
			case 3:
				showTutorialHTML(talker, "tutorial-09.htm");
				enableTutorialEvent(qs, memoFlag | 1048576);
				qs.setMemoState(qs.getMemoState() | 1048576); // TODO find better way!
				break;
			case 4:
				showTutorialHTML(talker, "tutorial-10.htm");
				break;
			case 5:
				switch (talker.getClassId())
				{
					case fighter:
						qs.addRadar(-71424, 258336, -3109);
						break;
					case mage:
						qs.addRadar(-91036, 248044, -3568);
						break;
					case elvenFighter:
					case elvenMage:
						qs.addRadar(46112, 41200, -3504);
						break;
					case darkFighter:
					case darkMage:
						qs.addRadar(28384, 11056, -4233);
						break;
					case orcFighter:
					case orcMage:
						qs.addRadar(-56736, -113680, -672);
						break;
					case dwarvenFighter:
						qs.addRadar(108567, -173994, -406);
						break;
					case maleSoldier:
					case femaleSoldier:
						qs.addRadar(-125872, 38016, 1251);
						break;
				}
				showTutorialHTML(talker, "tutorial-11.htm");
				break;
			case 7:
				showTutorialHTML(talker, "tutorial-15.htm");
				qs.setMemoState(memoFlag | 5);
				break;
			case 8:
				showTutorialHTML(talker, "tutorial-18.htm");
				break;
			case 9:
				if (!talker.isMageClass())
				{
					switch (talker.getRace())
					{
						case HUMAN:
						case ELF:
						case DARK_ELF:
							showTutorialHTML(talker, "tutorial-fighter-017.htm");
							break;
						case DWARF:
							showTutorialHTML(talker, "tutorial-fighter-dwarf-017.htm");
							break;
						case ORC:
							showTutorialHTML(talker, "tutorial-fighter-orc-017.htm");
							break;
						case KAMAEL:
							showTutorialHTML(talker, "tutorial-kamael-017.htm");
					}
				}
				break;
			case 10:
				showTutorialHTML(talker, "tutorial-19.htm");
				break;
			case 11:
				switch (talker.getRace())
				{
					case HUMAN:
						showTutorialHTML(talker, "tutorial-mage-020.htm");
						break;
					case ELF:
					case DARK_ELF:
						showTutorialHTML(talker, "tutorial-mage-elf-020.htm");
						break;
					case ORC:
						showTutorialHTML(talker, "tutorial-mage-orc-020.htm");
						break;
				}
				break;
			case 12:
				showTutorialHTML(talker, "tutorial-15.htm");
				break;
			case 13:
				switch (talker.getClassId())
				{
					case fighter:
						showTutorialHTML(talker, "tutorial-21.htm");
						break;
					case mage:
						showTutorialHTML(talker, "tutorial-21a.htm");
						break;
					case elvenFighter:
						showTutorialHTML(talker, "tutorial-21b.htm");
						break;
					case elvenMage:
						showTutorialHTML(talker, "tutorial-21c.htm");
						break;
					case orcFighter:
						showTutorialHTML(talker, "tutorial-21d.htm");
						break;
					case orcMage:
						showTutorialHTML(talker, "tutorial-21e.htm");
						break;
					case dwarvenFighter:
						showTutorialHTML(talker, "tutorial-21f.htm");
						break;
					case darkFighter:
						showTutorialHTML(talker, "tutorial-21g.htm");
						break;
					case darkMage:
						showTutorialHTML(talker, "tutorial-21h.htm");
						break;
					case maleSoldier:
						showTutorialHTML(talker, "tutorial-21i.htm");
						break;
					case femaleSoldier:
						showTutorialHTML(talker, "tutorial-21j.htm");
						break;
				}
				break;
			case 15:
				if (talker.getRace() != Race.KAMAEL)
				{
					showTutorialHTML(talker, "tutorial-28.htm");
				}
				else if (talker.getClassId() == ClassId.trooper)
				{
					showTutorialHTML(talker, "tutorial-28a.htm");
				}
				else if (talker.getClassId() == ClassId.warder)
				{
					showTutorialHTML(talker, "tutorial-28b.htm");
				}
				break;
			case 16:
				showTutorialHTML(talker, "tutorial-30.htm");
				break;
			case 17:
				showTutorialHTML(talker, "tutorial-27.htm");
				break;
			case 19:
				showTutorialHTML(talker, "tutorial-07.htm");
				break;
			case 20:
				showTutorialHTML(talker, "tutorial-14.htm");
				break;
			case 21:
				showTutorialHTML(talker, "tutorial-newbie-001.htm");
				break;
			case 22:
				showTutorialHTML(talker, "tutorial-14.htm");
				break;
			case 23:
				showTutorialHTML(talker, "tutorial-24.htm");
				break;
			case 24:
				switch (talker.getRace())
				{
					case HUMAN:
						showTutorialHTML(talker, "tutorial-newbie-003a.htm");
						break;
					case ELF:
						showTutorialHTML(talker, "tutorial-newbie-003b.htm");
						break;
					case DARK_ELF:
						showTutorialHTML(talker, "tutorial-newbie-003c.htm");
						break;
					case ORC:
						showTutorialHTML(talker, "tutorial-newbie-003d.htm");
						break;
					case DWARF:
						showTutorialHTML(talker, "tutorial-newbie-003e.htm");
						break;
					case KAMAEL:
						showTutorialHTML(talker, "tutorial-newbie-003f.htm");
				}
				break;
			case 25:
				switch (talker.getClassId())
				{
					case fighter:
						showTutorialHTML(talker, "tutorial-newbie-002a.htm");
						break;
					case mage:
						showTutorialHTML(talker, "tutorial-newbie-002b.htm");
						break;
					case elvenFighter:
					case elvenMage:
						showTutorialHTML(talker, "tutorial-newbie-002c.htm");
						break;
					case darkMage:
						showTutorialHTML(talker, "tutorial-newbie-002d.htm");
						break;
					case darkFighter:
						showTutorialHTML(talker, "tutorial-newbie-002e.htm");
						break;
					case dwarvenFighter:
						showTutorialHTML(talker, "tutorial-newbie-002g.htm");
						break;
					case orcFighter:
					case orcMage:
						showTutorialHTML(talker, "tutorial-newbie-002f.htm");
						break;
					case maleSoldier:
					case femaleSoldier:
						showTutorialHTML(talker, "tutorial-newbie-002i.htm");
						break;
				}
				break;
			case 26:
				if (!talker.isMageClass() || (talker.getClassId() == ClassId.orcMage))
				{
					showTutorialHTML(talker, "tutorial-newbie-004a.htm");
				}
				else
				{
					showTutorialHTML(talker, "tutorial-newbie-004b.htm");
				}
				break;
			case 27:
				switch (talker.getClassId())
				{
					case fighter:
					case orcMage:
					case orcFighter:
						showTutorialHTML(talker, "tutorial-newbie-002h.htm");
				}
				break;
			case 28:
				showTutorialHTML(talker, "tutorial-31.htm");
				break;
			case 29:
				showTutorialHTML(talker, "tutorial-32.htm");
				break;
			case 30:
				showTutorialHTML(talker, "tutorial-33.htm");
				break;
			case 31:
				showTutorialHTML(talker, "tutorial-34.htm");
				break;
			case 32:
				showTutorialHTML(talker, "tutorial-35.htm");
				break;
			case 33:
				switch (talker.getLevel())
				{
					case 18:
						showTutorialHTML(talker, "tw-gludio.htm");
						break;
					case 28:
						showTutorialHTML(talker, "tw-dion.htm");
						break;
					case 38:
						showTutorialHTML(talker, "tw-heine.htm");
						break;
					case 48:
						showTutorialHTML(talker, "tw-oren.htm");
						break;
					case 58:
						showTutorialHTML(talker, "tw-shuttgart.htm");
						break;
					case 68:
						showTutorialHTML(talker, "tw-rune.htm");
						break;
				}
				break;
			case 34:
				if (talker.getLevel() == 79)
				{
					showTutorialHTML(talker, "tutorial-ss-79.htm");
				}
				break;
		}
	}
	
	private void userConnected(L2PcInstance talker)
	{
		final QuestState qs = getQuestState(talker, true);
		
		if (qs == null)
		{
			return;
		}
		if (!qs.isStarted())
		{
			qs.setState(State.STARTED);
		}
		
		if (talker.getLevel() < 6)
		{
			if (getOneTimeQuestFlag(talker, 255) != 0)
			{
				return;
			}
			int memoState = qs.getMemoState();
			int memoFlag;
			if (memoState == -1)
			{
				memoState = 0;
				memoFlag = 0;
			}
			else
			{
				memoFlag = memoState & 255;
				memoState = memoState & 2147483392;
			}
			
			switch (memoFlag)
			{
				case 0:
					qs.startQuestTimer((talker.getObjectId() + 1000000) + "", 10000);
					memoState = 2147483392 & ~(8388608 | 1048576);
					qs.setMemoState(1 | memoState);
					if (qs.getMemoStateEx(1) < 0)
					{
						qs.setMemoStateEx(1, -2);
					}
					break;
				case 1:
					qs.showQuestionMark(talker, 1);
					qs.playSound(Voice.TUTORIAL_VOICE_006_1000);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					break;
				case 2:
					if (haveMemo(talker, Q201_TUTORIAL_HUMAN_FIGHTER) || haveMemo(talker, Q202_TUTORIAL_HUMAN_MAGE) || haveMemo(talker, Q203_TUTORIAL_ELF) || haveMemo(talker, Q204_TUTORIAL_DARK_ELF) || haveMemo(talker, Q205_TUTORIAL_ORC) || haveMemo(talker, Q206_TUTORIAL_DWARF))
					{
						qs.showQuestionMark(talker, 6);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					else
					{
						qs.showQuestionMark(talker, 2);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 3:
					int stateMark = 1;
					if (qs.getQuestItemsCount(BLUE_GEMSTONE) == 1)
					{
						stateMark = 3;
					}
					else if (qs.getMemoStateEx(1) == 2)
					{
						stateMark = 2;
					}
					
					switch (stateMark)
					{
						case 1:
							qs.showQuestionMark(talker, 3);
							break;
						case 2:
							qs.showQuestionMark(talker, 4);
							break;
						case 3:
							qs.showQuestionMark(talker, 5);
							break;
					}
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					break;
				case 4:
					qs.showQuestionMark(talker, 12);
					qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					break;
			}
			enableTutorialEvent(qs, memoState);
		}
		else
		{
			switch (talker.getLevel())
			{
				case 18:
					if (haveMemo(talker, 10276) && (getOneTimeQuestFlag(talker, 10276) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 28:
					if (haveMemo(talker, 10277) && (getOneTimeQuestFlag(talker, 10277) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 38:
					if (haveMemo(talker, 10278) && (getOneTimeQuestFlag(talker, 10278) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 48:
					if (haveMemo(talker, 10279) && (getOneTimeQuestFlag(talker, 10279) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 58:
					if (haveMemo(talker, 10280) && (getOneTimeQuestFlag(talker, 10280) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 68:
					if (haveMemo(talker, 10281) && (getOneTimeQuestFlag(talker, 10281) == 0))
					{
						qs.showQuestionMark(talker, 33);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
				case 79:
					if (haveMemo(talker, 192) && (getOneTimeQuestFlag(talker, 192) == 0))
					{
						qs.showQuestionMark(talker, 34);
						qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					}
					break;
			}
			
			int territoryWarId = qs.getDominionSiegeID(talker);
			int territoryWarState = qs.getNRMemoStateEx(talker, 728, 1);
			
			if ((territoryWarId > 0) && (qs.getDominionWarState(territoryWarId) == 5))
			{
				if (!qs.haveNRMemo(talker, 728))
				{
					qs.setNRMemo(talker, 728);
					qs.setNRMemoState(talker, 728, 0);
					qs.setNRMemoStateEx(talker, 728, 1, territoryWarId);
				}
				else if (territoryWarId != territoryWarState)
				{
					qs.setNRMemoState(talker, 728, 0);
					qs.setNRMemoStateEx(talker, 728, 1, territoryWarId);
				}
				switch (territoryWarId)
				{
					case 81:
						if (qs.getDominionWarState(TW_GLUDIO) == 5)
						{
							if (!qs.haveNRMemo(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO))
							{
								qs.setNRMemo(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO);
								qs.setNRMemoState(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO, 0);
								qs.setNRFlagJournal(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO, 1);
								qs.showQuestionMark(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q717_FOR_THE_SAKE_OF_THE_TERRITORY_GLUDIO);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 82:
						if (qs.getDominionWarState(TW_DION) == 5)
						{
							if (!qs.haveNRMemo(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION))
							{
								qs.setNRMemo(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION);
								qs.setNRMemoState(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION, 0);
								qs.setNRFlagJournal(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION, 1);
								qs.showQuestionMark(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q718_FOR_THE_SAKE_OF_THE_TERRITORY_DION);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 83:
						if (qs.getDominionWarState(TW_GIRAN) == 5)
						{
							if (!qs.haveNRMemo(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN))
							{
								qs.setNRMemo(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN);
								qs.setNRMemoState(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN, 0);
								qs.setNRFlagJournal(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN, 1);
								qs.showQuestionMark(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q719_FOR_THE_SAKE_OF_THE_TERRITORY_GIRAN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 84:
						if (qs.getDominionWarState(TW_OREN) == 5)
						{
							if (!qs.haveNRMemo(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN))
							{
								qs.setNRMemo(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN);
								qs.setNRMemoState(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN, 0);
								qs.setNRFlagJournal(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN, 1);
								qs.showQuestionMark(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q720_FOR_THE_SAKE_OF_THE_TERRITORY_OREN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 85:
						if (qs.getDominionWarState(TW_ADEN) == 5)
						{
							if (!qs.haveNRMemo(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN))
							{
								qs.setNRMemo(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN);
								qs.setNRMemoState(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN, 0);
								qs.setNRFlagJournal(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN, 1);
								qs.showQuestionMark(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q721_FOR_THE_SAKE_OF_THE_TERRITORY_ADEN);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 86:
						if (qs.getDominionWarState(TW_HEINE) == 5)
						{
							if (!qs.haveNRMemo(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL))
							{
								qs.setNRMemo(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL);
								qs.setNRMemoState(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL, 0);
								qs.setNRFlagJournal(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL, 1);
								qs.showQuestionMark(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q722_FOR_THE_SAKE_OF_THE_TERRITORY_INNADRIL);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 87:
						if (qs.getDominionWarState(TW_GODDARD) == 5)
						{
							if (!qs.haveNRMemo(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD))
							{
								qs.setNRMemo(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD);
								qs.setNRMemoState(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD, 0);
								qs.setNRFlagJournal(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD, 1);
								qs.showQuestionMark(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q723_FOR_THE_SAKE_OF_THE_TERRITORY_GODDARD);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 88:
						if (qs.getDominionWarState(TW_RUNE) == 5)
						{
							if (!qs.haveNRMemo(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE))
							{
								qs.setNRMemo(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE);
								qs.setNRMemoState(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE, 0);
								qs.setNRFlagJournal(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE, 1);
								qs.showQuestionMark(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q724_FOR_THE_SAKE_OF_THE_TERRITORY_RUNE);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
					case 89:
						if (qs.getDominionWarState(TW_SCHUTTGART) == 5)
						{
							if (!qs.haveNRMemo(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART))
							{
								qs.setNRMemo(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART);
								qs.setNRMemoState(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART, 0);
								qs.setNRFlagJournal(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART, 1);
								qs.showQuestionMark(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
							else
							{
								qs.showQuestionMark(talker, Q725_FOR_THE_SAKE_OF_THE_TERRITORY_SCHUTTGART);
								qs.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
							}
						}
						break;
				}
			}
			else
			{
				if (qs.haveNRMemo(talker, Q728_TERRITORY_WAR))
				{
					if ((territoryWarState >= 81) && (territoryWarState <= 89))
					{
						int twNRState = qs.getNRMemoState(talker, Q728_TERRITORY_WAR);
						int twNRStateForCurrentWar = qs.getNRMemoState(talker, 636 + territoryWarState);
						if (twNRStateForCurrentWar >= 0)
						{
							qs.setNRMemoState(talker, Q728_TERRITORY_WAR, twNRStateForCurrentWar + twNRState);
							qs.removeNRMemo(talker, 636 + territoryWarState);
						}
					}
				}
				if (qs.haveNRMemo(talker, 739) && (qs.getNRMemoState(talker, 739) > 0))
				{
					qs.setNRMemoState(talker, 739, 0);
				}
				if (qs.haveNRMemo(talker, Q729_PROTECT_THE_TERRITORY_CATAPULT))
				{
					qs.removeNRMemo(talker, 729);
				}
				if (qs.haveNRMemo(talker, Q730_PROTECT_THE_SUPPLIES_SAFE))
				{
					qs.removeNRMemo(talker, 730);
				}
				if (qs.haveNRMemo(talker, Q731_PROTECT_THE_MILITARY_ASSOCIATION_LEADER))
				{
					qs.removeNRMemo(talker, 731);
				}
				if (qs.haveNRMemo(talker, Q732_PROTECT_THE_RELIGIOUS_ASSOCIATION_LEADER))
				{
					qs.removeNRMemo(talker, 732);
				}
				if (qs.haveNRMemo(talker, Q733_PROTECT_THE_ECONOMIC_ASSOCIATION_LEADER))
				{
					qs.removeNRMemo(talker, 733);
				}
			}
		}
	}
	
	// ---------------------------------- Event
	
	private void eventRoien(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(RECOMMENDATION_1))
				{
					if (!talker.isMageClass() && (qs.getMemoStateEx(1) <= 3))
					{
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						qs.addExpAndSp(0, 50);
						qs.setMemoStateEx(1, 4);
					}
					if (talker.isMageClass() && (qs.getMemoStateEx(1) <= 3))
					{
						if (talker.getClassId() == ClassId.orcMage)
						{
							qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
							qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						}
						else
						{
							qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
							qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
						}
						qs.addExpAndSp(0, 50);
						qs.setMemoStateEx(1, 4);
					}
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					qs.takeItems(RECOMMENDATION_1, 1);
					showPage(talker, "30008-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				showPage(talker, "30008-005.htm");
				break;
			case 42:
				qs.addRadar(-84081, 243277, -3723);
				showPage(talker, "30008-006.htm");
		}
	}
	
	private void eventGallin(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(RECOMMENDATION_2))
				{
					if (!talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200))
					{
						qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					if (talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200) && (qs.getQuestItemsCount(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS) <= 100))
					{
						if (talker.getClassId() == ClassId.orcMage)
						{
							qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
							qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						}
						else
						{
							qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
							qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						}
						qs.addExpAndSp(0, 50);
					}
					qs.takeItems(RECOMMENDATION_2, 1);
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					if (qs.getMemoStateEx(1) <= 3)
					{
						qs.setMemoStateEx(1, 4);
					}
					showPage(talker, "30017-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				qs.addRadar(-119692, 44504, 380);
				showPage(talker, "30017-005.htm");
				break;
			case 42:
				qs.addRadar(-84081, 243277, -3723);
				showPage(talker, "30017-006.htm");
		}
	}
	
	private void eventJundin(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(BLOOD_OF_MITRAELL))
				{
					if (!talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200))
					{
						qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					if (talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200) && (qs.getQuestItemsCount(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS) <= 100))
					{
						if (talker.getClassId() == ClassId.orcMage)
						{
							qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
							qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						}
						else
						{
							qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
							qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						}
						qs.addExpAndSp(0, 50);
					}
					qs.takeItems(BLOOD_OF_MITRAELL, 1);
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					if (qs.getMemoStateEx(1) <= 3)
					{
						qs.setMemoStateEx(1, 4);
					}
					showPage(talker, "30129-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				qs.addRadar(-119692, 44504, 380);
				showPage(talker, "30129-005.htm");
				break;
			case 42:
				qs.addRadar(17024, 13296, -3744);
				showPage(talker, "30129-006.htm");
		}
	}
	
	private void eventNerupa(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(LEAF_OF_THE_MOTHER_TREE))
				{
					if (!talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200))
					{
						
						qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					if (talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200) && (qs.getQuestItemsCount(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS) <= 100))
					{
						qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
						qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					qs.takeItems(LEAF_OF_THE_MOTHER_TREE, 1);
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					if (qs.getMemoStateEx(1) <= 3)
					{
						qs.setMemoStateEx(1, 4);
					}
					showPage(talker, "30370-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				qs.addRadar(-119692, 44504, 380);
				showPage(talker, "30370-005.htm");
				break;
			case 42:
				qs.addRadar(45475, 48359, -3060);
				showPage(talker, "30370-006.htm");
				break;
		}
	}
	
	private void eventForemanLaferon(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(LICENSE_OF_MINER))
				{
					if (!talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200))
					{
						
						qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					if (talker.isMageClass() && (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200) && (qs.getQuestItemsCount(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS) <= 100))
					{
						qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
						qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
						qs.addExpAndSp(0, 50);
					}
					qs.takeItems(LICENSE_OF_MINER, 1);
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					if (qs.getMemoStateEx(1) <= 3)
					{
						qs.setMemoStateEx(1, 4);
					}
					showPage(talker, "30528-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				qs.addRadar(-119692, 44504, 380);
				showPage(talker, "30528-005.htm");
				break;
			case 42:
				qs.addRadar(115632, -177996, -905);
				showPage(talker, "30528-006.htm");
				break;
		}
	}
	
	private void eventGuardianVullkus(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		switch (event)
		{
			case 31:
				if (qs.hasQuestItems(VOUCHER_OF_FLAME))
				{
					qs.takeItems(VOUCHER_OF_FLAME, 1);
					startQuestTimer(npc.getId() + "", 60000, npc, talker);
					if (qs.getMemoStateEx(1) <= 3)
					{
						qs.setMemoStateEx(1, 4);
					}
					if (qs.getQuestItemsCount(SOULSHOT_NO_GRADE_FOR_BEGINNERS) <= 200)
					{
						qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
						qs.addExpAndSp(0, 50);
					}
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					showPage(talker, "30573-002.htm");
				}
				break;
			case 41:
				teleportPlayer(talker, new Location(-120050, 44500, 360), 0);
				qs.addRadar(-119692, 44504, 380);
				showPage(talker, "30573-005.htm");
			case 42:
				qs.addRadar(-45032, -113598, -192);
				showPage(talker, "30573-006.htm");
		}
	}
	
	private void eventSubelderPerwan(int event, L2PcInstance talker, L2Npc npc, QuestState qs)
	{
		if ((event == 31) && qs.hasQuestItems(DIPLOMA))
		{
			if ((talker.getRace() == Race.KAMAEL) && (talker.getClassId().level() == 0) && (qs.getMemoStateEx(1) <= 3))
			{
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				qs.addExpAndSp(0, 50);
				qs.setMemoStateEx(1, 4);
			}
			qs.takeItems(DIPLOMA, -1);
			startQuestTimer(npc.getId() + "", 60000, npc, talker);
			qs.addRadar(-119692, 44504, 380);
			showPage(talker, "32133-002.htm");
		}
	}
	
	// ---------------------------------- Talks
	
	private void talkRoien(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(RECOMMENDATION_1))
		{
			showPage(talker, "30008-001.htm", true);
		}
		else
		{
			if (qs.getMemoStateEx(1) > 3)
			{
				showPage(talker, "30008-004.htm", true);
			}
			else if (qs.getMemoStateEx(1) <= 3)
			{
				showPage(talker, "30008-003.htm", true);
			}
		}
	}
	
	private void talkCarl(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if ((talker.getClassId() == ClassId.fighter) && (talker.getRace() == Race.HUMAN))
			{
				qs.removeRadar(-71424, 258336, -3109);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				showPage(talker, "30009-001.htm");
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			showPage(talker, "30009-002.htm");
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(RECOMMENDATION_1, 1);
			
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if (!talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				
			}
			if (talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS) && !qs.hasQuestItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
			}
			showPage(talker, "30009-003.htm");
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30009-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkGallin(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(RECOMMENDATION_2))
		{
			showPage(talker, "30017-001.htm", true);
		}
		else if (!qs.hasQuestItems(RECOMMENDATION_2) && (qs.getMemoStateEx(1) > 3))
		{
			showPage(talker, "30017-004.htm", true);
		}
		else if (!qs.hasQuestItems(RECOMMENDATION_2) && (qs.getMemoStateEx(1) <= 3))
		{
			showPage(talker, "30017-003.htm", true);
		}
	}
	
	private void talkDoff(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if ((talker.getClassId() == ClassId.mage) && (talker.getRace() == Race.HUMAN))
			{
				qs.removeRadar(-91036, 248044, -3568);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				showPage(talker, "30019-001.htm");
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			showPage(talker, "30019-002.htm");
		}
		if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(RECOMMENDATION_2, 1);
			
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if (!talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				
			}
			if (talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS) && !qs.hasQuestItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
			}
			showPage(talker, "30019-003.htm");
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30019-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkJundin(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(BLOOD_OF_MITRAELL))
		{
			showPage(talker, "30129-001.htm", true);
		}
		else if (!qs.hasQuestItems(BLOOD_OF_MITRAELL) && (qs.getMemoStateEx(1) > 3))
		{
			showPage(talker, "30129-004.htm", true);
		}
		else if (!qs.hasQuestItems(BLOOD_OF_MITRAELL) && (qs.getMemoStateEx(1) <= 3))
		{
			showPage(talker, "30129-003.htm", true);
		}
	}
	
	private void talkPoeny(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if (talker.getRace() == Race.DARK_ELF)
			{
				qs.removeRadar(28384, 11056, -4233);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				
				if (!talker.isMageClass())
				{
					showPage(talker, "30009-001.htm");
				}
				else
				{
					showPage(talker, "30019-001.htm");
				}
				
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			if (!talker.isMageClass())
			{
				showPage(talker, "30009-002.htm");
			}
			else
			{
				showPage(talker, "30019-002.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			
			if (!talker.isMageClass())
			{
				showPage(talker, "30131-003f.htm");
			}
			else
			{
				showPage(talker, "30131-003m.htm");
			}
			
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(BLOOD_OF_MITRAELL, 1);
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			
			if (!talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
			}
			if (talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS) && !qs.hasQuestItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
			}
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30131-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkNerupa(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(LEAF_OF_THE_MOTHER_TREE))
		{
			showPage(talker, "30370-001.htm", true);
		}
		else if (!qs.hasQuestItems(LEAF_OF_THE_MOTHER_TREE) && (qs.getMemoStateEx(1) > 3))
		{
			showPage(talker, "30370-004.htm", true);
		}
		else if (!qs.hasQuestItems(LEAF_OF_THE_MOTHER_TREE) && (qs.getMemoStateEx(1) <= 3))
		{
			showPage(talker, "30370-003.htm", true);
		}
	}
	
	private void talkMotherTemp(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if (talker.getRace() == Race.ELF)
			{
				qs.removeRadar(46112, 41200, -3504);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				if (!talker.isMageClass())
				{
					showPage(talker, "30009-001.htm");
				}
				else
				{
					showPage(talker, "30019-001.htm");
				}
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			if (!talker.isMageClass())
			{
				showPage(talker, "30009-002.htm");
			}
			else
			{
				showPage(talker, "30019-002.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(LEAF_OF_THE_MOTHER_TREE, 1);
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if (!talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
			}
			if (talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS) && !qs.hasQuestItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
			}
			
			if (!talker.isMageClass())
			{
				showPage(talker, "30400-003f.htm");
			}
			else
			{
				showPage(talker, "30400-003m.htm");
			}
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30400-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkForemanLaferon(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(LICENSE_OF_MINER))
		{
			showPage(talker, "30528-001.htm", true);
		}
		else if (!qs.hasQuestItems(LICENSE_OF_MINER) && (qs.getMemoStateEx(1) > 3))
		{
			showPage(talker, "30528-004.htm", true);
		}
		else if (!qs.hasQuestItems(LICENSE_OF_MINER) && (qs.getMemoStateEx(1) <= 3))
		{
			showPage(talker, "30528-003.htm", true);
		}
	}
	
	private void talkMinerMai(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if (talker.getRace() == Race.DWARF)
			{
				qs.removeRadar(108567, -173994, -406);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				if (!talker.isMageClass())
				{
					showPage(talker, "30009-001.htm");
				}
				else
				{
					showPage(talker, "30019-001.htm");
				}
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			showPage(talker, "30009-002.htm");
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(LICENSE_OF_MINER, 1);
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if (!talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
			}
			if (talker.isMageClass() && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS) && !qs.hasQuestItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				if (talker.getClassId() == ClassId.orcMage)
				{
					qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
					qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				}
				else
				{
					qs.playSound(Voice.TUTORIAL_VOICE_027_1000);
					qs.giveItems(SPIRITSHOT_NO_GRADE_FOR_BEGINNERS, 100);
				}
			}
			else
			{
				showPage(talker, "30530-003.htm");
			}
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30530-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkGuardianVullkus(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(VOUCHER_OF_FLAME))
		{
			showPage(talker, "30573-001.htm", true);
		}
		else
		{
			if (qs.getMemoStateEx(1) > 3)
			{
				showPage(talker, "30573-004.htm", true);
			}
			else
			{
				showPage(talker, "30573-003.htm", true);
			}
		}
	}
	
	private void talkShelaPriestess(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if (talker.getRace() == Race.ORC)
			{
				qs.removeRadar(-56736, -113680, -672);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				qs.setMemoStateEx(1, 0);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				if (!talker.isMageClass())
				{
					showPage(talker, "30009-001.htm");
				}
				else
				{
					showPage(talker, "30575-001.htm");
				}
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			if (!talker.isMageClass())
			{
				showPage(talker, "30009-002.htm");
			}
			else
			{
				showPage(talker, "30575-002.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			if (!talker.isMageClass())
			{
				showPage(talker, "30575-003f.htm");
			}
			else
			{
				showPage(talker, "30575-003m.htm");
			}
			
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(VOUCHER_OF_FLAME, 1);
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if (!qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
			}
			qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "30575-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "30009-005.htm");
		}
	}
	
	private void talkSubelderPerwan(L2PcInstance talker, QuestState qs)
	{
		if (qs.hasQuestItems(DIPLOMA))
		{
			showPage(talker, "32133-001.htm", true);
		}
		else
		{
			if ((qs.getMemoStateEx(1) > 3))
			{
				showPage(talker, "32133-004.htm", true);
			}
			else if (qs.getMemoStateEx(1) <= 3)
			{
				showPage(talker, "32133-003.htm", true);
			}
		}
	}
	
	private void talkHelperKrenisk(L2Npc npc, L2PcInstance talker, QuestState qs)
	{
		if (qs.getMemoStateEx(1) < 0)
		{
			if (talker.getRace() == Race.KAMAEL)
			{
				qs.removeRadar(-125872, 38016, 1251);
				qs.setMemoStateEx(1, 0);
				startQuestTimer(npc.getId() + "", 30000, npc, talker);
				enableTutorialEvent(qs, (qs.getMemoState() & 2147483392) | 1048576);
				showPage(talker, "32134-001.htm");
			}
			else
			{
				showPage(talker, "30009-006.htm");
			}
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && !qs.hasQuestItems(BLUE_GEMSTONE))
		{
			showPage(talker, "32134-002.htm");
		}
		else if (((qs.getMemoStateEx(1) == 0) || (qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2)) && qs.hasQuestItems(BLUE_GEMSTONE))
		{
			showPage(talker, "32134-003.htm");
			qs.takeItems(BLUE_GEMSTONE, -1);
			qs.setMemoStateEx(1, 3);
			qs.giveItems(DIPLOMA, 1);
			startQuestTimer(npc.getId() + "", 30000, npc, talker);
			qs.setMemoState((qs.getMemoState() & 2147483392) | 4);
			if ((talker.getRace() == Race.KAMAEL) && (talker.getClassId().level() == 0) && !qs.hasQuestItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS))
			{
				qs.giveItems(SOULSHOT_NO_GRADE_FOR_BEGINNERS, 200);
				qs.playSound(Voice.TUTORIAL_VOICE_026_1000);
			}
		}
		else if (qs.getMemoStateEx(1) == 3)
		{
			showPage(talker, "32134-004.htm");
		}
		else if (qs.getMemoStateEx(1) > 3)
		{
			showPage(talker, "32134-005.htm");
		}
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if (npc.getId() == TUTORIAL_GREMLIN)
		{
			if ((qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 0))
			{
				qs.playSound(Voice.TUTORIAL_VOICE_011_1000);
				qs.showQuestionMark(killer.getActingPlayer(), 3);
				qs.setMemoStateEx(1, 2);
			}
			
			if (((qs.getMemoStateEx(1) == 1) || (qs.getMemoStateEx(1) == 2) || (qs.getMemoStateEx(1) == 0)) && !qs.hasQuestItems(BLUE_GEMSTONE) && (getRandom(2) <= 1))
			{
				npc.dropItem(killer, BLUE_GEMSTONE, 1);
				qs.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public boolean isVisibleInQuestWindow()
	{
		return false;
	}
}
