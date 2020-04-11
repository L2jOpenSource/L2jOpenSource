package hellbound.AI.NPC.Galate;

import l2r.gameserver.data.SpawnTable;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.enums.audio.IAudio;
import l2r.gameserver.enums.audio.Music;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Broadcast;

import ai.npc.AbstractNpcAI;
import hellbound.HellboundEngine;
import quests.Q00130_PathToHellbound.Q00130_PathToHellbound;
import quests.Q00133_ThatsBloodyHot.Q00133_ThatsBloodyHot;

public class Galate extends AbstractNpcAI
{
	private static final int Galate = 32292;
	
	private static final int BLUEWARPGATE = 32314;
	private static final int BLACKBLUEWARPGATE = 32315;
	private static final int PURPLEWARPGATE = 32316;
	private static final int YELLOWWARPGATE = 32317;
	private static final int GREENWARPGATEVERDE = 32318;
	private static final int FINALWARPGATE = 32319;
	
	// Warpgate Locations (Heine, Wastelands, Giran)
	private static final Location[] WARPGATE_LOCATIONS =
	{
		new Location(112080, 219568, -3664),
		new Location(-16899, 209827, -3640)
	};
	
	// Boolean variable for checking if Final Warpgate is Spawned
	private boolean checked = false;
	
	private int points = 0, nowLevel = 0, nowTrust = 0, showTrust = 0, firstTrust = 0;
	
	public Galate()
	{
		super(Galate.class.getSimpleName(), "hellbound/AI/NPC");
		addStartNpc(Galate);
		addTalkId(Galate);
		addFirstTalkId(Galate);
		int hellboundLevel = HellboundEngine.getInstance().getLevel();
		points = HellboundEngine.getInstance().getTrust();
		if (hellboundLevel >= 0)
		{
			startQuestTimer("firstWarpgate", 500, null, null, true);
			startQuestTimer("secondWarpgate", 500, null, null, true);
			startQuestTimer("thirdWarpgate", 500, null, null, true);
			startQuestTimer("fourthWarpgate", 500, null, null, true);
			startQuestTimer("fifthWarpgate", 500, null, null, true);
			startQuestTimer("lastWarpgate", 500, null, null, true);
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		final QuestState path_to_hellbound_st = player.getQuestState(Q00130_PathToHellbound.class.getSimpleName());
		final QuestState thats_bloody_hot_st = player.getQuestState(Q00133_ThatsBloodyHot.class.getSimpleName());
		int hellboundTrust = HellboundEngine.getInstance().getTrust();
		if (hellboundTrust <= 99999)
		{
			if (HellboundEngine.getInstance().isLocked())
			{
				if ((thats_bloody_hot_st != null) && thats_bloody_hot_st.isCompleted())
				{
					HellboundEngine.getInstance().setLevel(1);
					htmltext = "32292.htm";
				}
				else
				{
					htmltext = "32292-1.htm";
				}
			}
			else if (((path_to_hellbound_st != null) && path_to_hellbound_st.isCompleted()) || ((thats_bloody_hot_st != null) && thats_bloody_hot_st.isCompleted()))
			{
				htmltext = "32292.htm";
			}
			else
			{
				htmltext = "32292-1.htm";
			}
		}
		else
		{
			if (((path_to_hellbound_st != null) && path_to_hellbound_st.isCompleted()) || ((thats_bloody_hot_st != null) && thats_bloody_hot_st.isCompleted()))
			{
				htmltext = "32292-3.htm";
			}
			else
			{
				htmltext = "32292-1.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		int hellboundTrust = HellboundEngine.getInstance().getTrust();
		if (hellboundTrust <= 99999)
		{
			htmltext = "32292-2.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		points = HellboundEngine.getInstance().getTrust();
		
		if (event.equalsIgnoreCase("cf"))
		{
			long CF = player.getInventory().getInventoryItemCount(9693, 0);
			if (CF >= 1)
			{
				htmltext = increaseWarpgatePoints(npc, player, "Galate Crystal Fragment", 9693, 10);
			}
			else
			{
				playSound(player, QuestSound.SKILLSOUND_LIQUID_FAIL);
				htmltext = ("<html><body>Galate:<br>You dont have Crystal Fragments.<br><a action=\"bypass -h Quest Galate 32292-2.htm\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
		}
		else if (event.equalsIgnoreCase("bc"))
		{
			long BC = player.getInventory().getInventoryItemCount(9695, 0);
			if (BC >= 1)
			{
				htmltext = increaseWarpgatePoints(npc, player, "Galate Blue Crystal", 9695, 50);
			}
			else
			{
				playSound(player, QuestSound.SKILLSOUND_LIQUID_FAIL);
				htmltext = ("<html><body>Galate:<br>You dont have Blue Crystals.<br><a action=\"bypass -h Quest Galate 32292-2.htm\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
		}
		else if (event.equalsIgnoreCase("rc"))
		{
			long RC = player.getInventory().getInventoryItemCount(9696, 0);
			if (RC >= 1)
			{
				htmltext = increaseWarpgatePoints(npc, player, "Galate Red Crystal", 9696, 100);
			}
			else
			{
				playSound(player, QuestSound.SKILLSOUND_LIQUID_FAIL);
				htmltext = ("<html><body>Galate:<br>You dont have Red Crystals.<br><a action=\"bypass -h Quest Galate 32292-2.htm\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
		}
		else if (event.equalsIgnoreCase("cc"))
		{
			long CC = player.getInventory().getInventoryItemCount(9697, 0);
			if (CC >= 1)
			{
				htmltext = increaseWarpgatePoints(npc, player, "Galate Clear Crystal", 9697, 1000);
			}
			else
			{
				playSound(player, QuestSound.SKILLSOUND_LIQUID_FAIL);
				htmltext = ("<html><body>Galate:<br>You dont have Clear Crystals.<br><a action=\"bypass -h Quest Galate 32292-2.htm\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
		}
		else if (event.equalsIgnoreCase("firstWarpgate"))
		{
			if (!checked && (points >= 0) && (points < 20000))
			{
				spawnNewWarpgate("firstWarpgate", BLUEWARPGATE, 0, "", null, 0);
			}
		}
		else if (event.equalsIgnoreCase("secondWarpgate"))
		{
			if (!checked && (points >= 20000) && (points < 40000))
			{
				spawnNewWarpgate("secondWarpgate", BLACKBLUEWARPGATE, BLUEWARPGATE, "Warpgate is now 20% energized!", Sound.ITEMSOUND_QUEST_FINISH, 0);
			}
		}
		else if (event.equalsIgnoreCase("thirdWarpgate"))
		{
			if (!checked && (points >= 40000) && (points < 60000))
			{
				spawnNewWarpgate("thirdWarpgate", PURPLEWARPGATE, BLACKBLUEWARPGATE, "Warpgate is now 40% energized!", Sound.ITEMSOUND_QUEST_FINISH, 0);
			}
		}
		else if (event.equalsIgnoreCase("fourthWarpgate"))
		{
			if (!checked && (points >= 60000) && (points < 80000))
			{
				spawnNewWarpgate("fourthWarpgate", YELLOWWARPGATE, PURPLEWARPGATE, "Warpgate is now 60% energized!", Sound.ITEMSOUND_QUEST_FINISH, 0);
			}
		}
		else if (event.equalsIgnoreCase("fifthWarpgate"))
		{
			if (!checked && (points >= 80000) && (points < 100000))
			{
				spawnNewWarpgate("fifthWarpgate", GREENWARPGATEVERDE, YELLOWWARPGATE, "Warpgate is now 80% energized!", Sound.ITEMSOUND_QUEST_FINISH, FINALWARPGATE);
			}
		}
		else if (event.equalsIgnoreCase("lastWarpgate"))
		{
			if (!checked && (points >= 100000))
			{
				checked = true;
				spawnNewWarpgate("lastWarpgate", FINALWARPGATE, GREENWARPGATEVERDE, "Warpgate is now 100% energized!", Music.SSQ_Dawn_01, 0);
			}
		}
		else if (event.equalsIgnoreCase("cwp"))
		{
			firstTrust = HellboundEngine.getInstance().getTrust();
			if (firstTrust <= 99999)
			{
				
				htmltext = ("<html><body>Galate:<br><font color=\"CC0000\">Warpgate Energy Points:</font> <font color=\"CCCC00\">" + points + "</font><br><font color=\"CC0000\">Warpgate Required Energy:</font> <font color=\"CCCC00\">100000</font><br><a action=\"bypass -h npc_%objectId%_Chat 0\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
			}
			else
			{
				htmltext = "<html><body>Warpgate is already operational.</body></html>";
			}
		}
		else if (event.equalsIgnoreCase("cp"))
		{
			nowLevel = HellboundEngine.getInstance().getLevel();
			nowTrust = HellboundEngine.getInstance().getTrust();
			if (nowTrust >= 100000)
			{
				
				htmltext = "<html><body>Galate:<br><font color=\"CC0000\">Hellbound Stage:</font> <font color=\"CCCC00\">" + nowLevel + "</font><br><font color=\"CC0000\">Hellbound Trust:</font> <font color=\"CCCC00\">" + nowTrust + "</font></body></html>";
			}
			else
			{
				htmltext = "<html><body>Warpgate not yet operational.</body></html>";
			}
		}
		else if (event.equalsIgnoreCase("32292-2.htm"))
		{
			htmltext = "32292-2.htm";
		}
		
		return htmltext;
	}
	
	/**
	 * Increase Warpgate Points (Energizes it)
	 * @param npc
	 * @param player
	 * @param process
	 * @param crystalId
	 * @param pointsToAdd
	 * @return Galate thank you HTML Message
	 */
	private String increaseWarpgatePoints(L2Npc npc, L2PcInstance player, String process, int crystalId, int pointsToAdd)
	{
		player.destroyItemByItemId(process, crystalId, 1, player, true);
		int trustam = (pointsToAdd * 1);
		points += trustam;
		_log.info(getClass().getSimpleName() + ": Current points - " + points);
		HellboundEngine.getInstance().updateTrust(trustam, false);
		showTrust = HellboundEngine.getInstance().getTrust();
		_log.info(getClass().getSimpleName() + ": Current Hellbound Trust - " + showTrust);
		if ((showTrust == 20000) || (showTrust == 40000) || (showTrust == 60000) || (showTrust == 80000) || (showTrust == 100000))
		{
			playSound(player, QuestSound.SKILLSOUND_LIQUID_SUCCESS);
		}
		else
		{
			playSound(player, QuestSound.SKILLSOUND_LIQUID_MIX);
		}
		return ("<html><body>Galate:<br>Thank you for contributing, warpagate will be opened soon.<br>(You can only contribute 1 crystal each time)<br><a action=\"bypass -h Quest Galate 32292-2.htm\">Back</a></body></html>").replace("%objectId%", String.valueOf(npc.getObjectId()));
	}
	
	/**
	 * Spawns New Warpgates and deletes old ones, also Broadcasts a Global Announcement and Plays a Sound to every Player Online
	 * @param cancelQuestTimerString
	 * @param warpgateToBeSpawned
	 * @param deleteOldWarpgate
	 * @param message
	 * @param itemsoundQuestFinish
	 * @param deleteLastWarpgate
	 */
	private void spawnNewWarpgate(String cancelQuestTimerString, int warpgateToBeSpawned, int deleteOldWarpgate, String message, IAudio itemsoundQuestFinish, int deleteLastWarpgate)
	{
		cancelQuestTimer(cancelQuestTimerString, null, null);
		addWarpgateSpawns(warpgateToBeSpawned);
		
		if (deleteOldWarpgate != 0)
		{
			checkIfNpcSpawnedAndDelete(deleteOldWarpgate);
		}
		
		if (!message.isEmpty())
		{
			Broadcast.toAllOnlinePlayers(message);
		}
		
		if (itemsoundQuestFinish != null)
		{
			Broadcast.toAllOnlinePlayers(itemsoundQuestFinish.getPacket());
		}
		
		if (deleteLastWarpgate != 0)
		{
			checkIfNpcSpawnedAndDelete(deleteLastWarpgate);
		}
	}
	
	/**
	 * Method used for spawning Warpgate into various Locations
	 * @param npcId
	 */
	private void addWarpgateSpawns(int npcId)
	{
		for (Location loc : WARPGATE_LOCATIONS)
		{
			L2Spawn spawnDat = addSpawn(npcId, loc).getSpawn();
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		}
	}
	
	/**
	 * Method used for deleting old Warpgates before / after spawning new Warpgate into various Locations
	 * @param npcId
	 */
	public void checkIfNpcSpawnedAndDelete(int npcId)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
		{
			L2Npc spawnedWarpgate = spawn.getLastSpawn();
			if ((spawnedWarpgate != null))
			{
				spawnedWarpgate.deleteMe();
			}
		}
	}
}