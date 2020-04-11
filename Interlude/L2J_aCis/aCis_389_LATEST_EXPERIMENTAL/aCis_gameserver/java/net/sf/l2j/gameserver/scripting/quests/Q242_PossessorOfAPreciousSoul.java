package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q242_PossessorOfAPreciousSoul extends Quest
{
	private static final String qn = "Q242_PossessorOfAPreciousSoul";
	
	// NPCs
	private static final int VIRGIL = 31742;
	private static final int KASSANDRA = 31743;
	private static final int OGMAR = 31744;
	private static final int MYSTERIOUS_KNIGHT = 31751;
	private static final int ANGEL_CORPSE = 31752;
	private static final int KALIS = 30759;
	private static final int MATILD = 30738;
	private static final int CORNERSTONE = 31748;
	private static final int FALLEN_UNICORN = 31746;
	private static final int PURE_UNICORN = 31747;
	
	// Monsters
	private static final int RESTRAINER_OF_GLORY = 27317;
	
	// Items
	private static final int VIRGIL_LETTER = 7677;
	private static final int GOLDEN_HAIR = 7590;
	private static final int SORCERY_INGREDIENT = 7596;
	private static final int ORB_OF_BINDING = 7595;
	private static final int CARADINE_LETTER = 7678;
	
	private Npc _pureUnicorn;
	
	public Q242_PossessorOfAPreciousSoul()
	{
		super(242, "Possessor of a Precious Soul - 2");
		
		setItemsIds(GOLDEN_HAIR, SORCERY_INGREDIENT, ORB_OF_BINDING);
		
		addStartNpc(VIRGIL);
		addTalkId(VIRGIL, KASSANDRA, OGMAR, MYSTERIOUS_KNIGHT, ANGEL_CORPSE, KALIS, MATILD, CORNERSTONE, FALLEN_UNICORN, PURE_UNICORN);
		
		addKillId(RESTRAINER_OF_GLORY);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Kasandra
		if (event.equalsIgnoreCase("31743-05.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// Ogmar
		else if (event.equalsIgnoreCase("31744-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// Mysterious Knight
		else if (event.equalsIgnoreCase("31751-02.htm"))
		{
			st.set("cond", "4");
			st.set("angel", "0");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		// Kalis
		else if (event.equalsIgnoreCase("30759-02.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30759-05.htm"))
		{
			if (st.hasQuestItems(SORCERY_INGREDIENT))
			{
				st.set("orb", "0");
				st.set("cornerstone", "0");
				st.set("cond", "9");
				st.playSound(QuestState.SOUND_MIDDLE);
				st.takeItems(GOLDEN_HAIR, 1);
				st.takeItems(SORCERY_INGREDIENT, 1);
			}
			else
			{
				st.set("cond", "7");
				htmltext = "30759-02.htm";
			}
		}
		// Matild
		else if (event.equalsIgnoreCase("30738-02.htm"))
		{
			st.set("cond", "8");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SORCERY_INGREDIENT, 1);
		}
		// Cornerstone
		else if (event.equalsIgnoreCase("31748-03.htm"))
		{
			if (st.hasQuestItems(ORB_OF_BINDING))
			{
				npc.deleteMe();
				st.takeItems(ORB_OF_BINDING, 1);
				
				int cornerstones = st.getInt("cornerstone");
				cornerstones++;
				if (cornerstones == 4)
				{
					st.unset("orb");
					st.unset("cornerstone");
					st.set("cond", "10");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					st.set("cornerstone", Integer.toString(cornerstones));
			}
			else
				htmltext = null;
		}
		
		return htmltext;
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("fallen"))
		{
			if (_pureUnicorn == null)
			{
				// Despawn Fallen Unicorn (we rely on native respawn of the Npc)
				npc.getSpawn().setRespawnState(true);
				npc.deleteMe();
				
				// Spawn Pure Unicorn (spawn for 30 seconds)
				_pureUnicorn = addSpawn(PURE_UNICORN, npc, false, 30000, true);
			}
		}
		else if (name.equalsIgnoreCase("pure"))
		{
			if (_pureUnicorn != null)
			{
				// Despawn Pure Unicorn
				_pureUnicorn.deleteMe();
				_pureUnicorn = null;
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
				if (st.hasQuestItems(VIRGIL_LETTER))
				{
					if (!player.isSubClassActive() || player.getLevel() < 60)
						htmltext = "31742-02.htm";
					else
					{
						htmltext = "31742-03.htm";
						st.setState(STATE_STARTED);
						st.set("cond", "1");
						st.playSound(QuestState.SOUND_ACCEPT);
						st.takeItems(VIRGIL_LETTER, 1);
					}
				}
				break;
			
			case STATE_STARTED:
				if (!player.isSubClassActive())
					break;
				
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VIRGIL:
						if (cond == 1)
							htmltext = "31742-04.htm";
						else if (cond == 2)
							htmltext = "31742-05.htm";
						break;
					
					case KASSANDRA:
						if (cond == 1)
							htmltext = "31743-01.htm";
						else if (cond == 2)
							htmltext = "31743-06.htm";
						else if (cond == 11)
						{
							htmltext = "31743-07.htm";
							st.giveItems(CARADINE_LETTER, 1);
							st.rewardExpAndSp(455764, 0);
							player.broadcastPacket(new SocialAction(player, 3));
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case OGMAR:
						if (cond == 2)
							htmltext = "31744-01.htm";
						else if (cond == 3)
							htmltext = "31744-03.htm";
						break;
					
					case MYSTERIOUS_KNIGHT:
						if (cond == 3)
							htmltext = "31751-01.htm";
						else if (cond == 4)
							htmltext = "31751-03.htm";
						else if (cond == 5)
						{
							if (st.hasQuestItems(GOLDEN_HAIR))
							{
								htmltext = "31751-04.htm";
								st.set("cond", "6");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
							{
								htmltext = "31751-03.htm";
								st.set("cond", "4");
							}
						}
						else if (cond == 6)
							htmltext = "31751-05.htm";
						break;
					
					case ANGEL_CORPSE:
						if (cond == 4)
						{
							npc.deleteMe();
							int hair = st.getInt("angel");
							hair++;
							
							if (hair == 4)
							{
								htmltext = "31752-02.htm";
								st.unset("angel");
								st.set("cond", "5");
								st.playSound(QuestState.SOUND_MIDDLE);
								st.giveItems(GOLDEN_HAIR, 1);
							}
							else
							{
								st.set("angel", Integer.toString(hair));
								htmltext = "31752-01.htm";
							}
						}
						else if (cond == 5)
							htmltext = "31752-01.htm";
						break;
					
					case KALIS:
						if (cond == 6)
							htmltext = "30759-01.htm";
						else if (cond == 7)
							htmltext = "30759-03.htm";
						else if (cond == 8)
						{
							if (st.hasQuestItems(SORCERY_INGREDIENT))
								htmltext = "30759-04.htm";
							else
							{
								htmltext = "30759-03.htm";
								st.set("cond", "7");
							}
						}
						else if (cond == 9)
							htmltext = "30759-06.htm";
						break;
					
					case MATILD:
						if (cond == 7)
							htmltext = "30738-01.htm";
						else if (cond == 8)
							htmltext = "30738-03.htm";
						break;
					
					case CORNERSTONE:
						if (cond == 9)
						{
							if (st.hasQuestItems(ORB_OF_BINDING))
								htmltext = "31748-02.htm";
							else
								htmltext = "31748-01.htm";
						}
						break;
					
					case FALLEN_UNICORN:
						if (cond == 9)
							htmltext = "31746-01.htm";
						else if (cond == 10)
						{
							htmltext = "31746-02.htm";
							startQuestTimer("fallen", npc, null, 3000);
						}
						break;
					
					case PURE_UNICORN:
						if (cond == 10)
						{
							htmltext = "31747-01.htm";
							st.set("cond", "11");
							st.playSound(QuestState.SOUND_MIDDLE);
							startQuestTimer("pure", npc, null, 3000);
						}
						else if (cond == 11)
							htmltext = "31747-02.htm";
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
	public String onKill(Npc npc, Creature killer)
	{
		final Player player = killer.getActingPlayer();
		
		final QuestState st = checkPlayerCondition(player, npc, "cond", "9");
		if (st == null || !player.isSubClassActive())
			return null;
		
		int orbs = st.getInt("orb"); // check orbs internally, because player can use them before he gets them all
		if (orbs < 4)
		{
			orbs++;
			st.set("orb", Integer.toString(orbs));
			st.playSound(QuestState.SOUND_ITEMGET);
			st.giveItems(ORB_OF_BINDING, 1);
		}
		
		return null;
	}
}