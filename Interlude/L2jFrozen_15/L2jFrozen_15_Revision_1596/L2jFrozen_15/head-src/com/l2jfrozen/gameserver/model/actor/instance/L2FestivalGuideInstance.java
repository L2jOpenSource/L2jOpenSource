package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.StatsSet;

import javolution.text.TextBuilder;

/**
 * Festival of Darkness Guide (Seven Signs).
 * @author Tempy
 */
public final class L2FestivalGuideInstance extends L2FolkInstance
{
	protected int festivalType;
	protected int festivalOracle;
	protected int blueStonesNeeded;
	protected int greenStonesNeeded;
	protected int redStonesNeeded;
	
	/**
	 * Instantiates a new l2 festival guide instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2FestivalGuideInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		
		switch (getNpcId())
		{
			case 31127:
			case 31132:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_31;
				festivalOracle = SevenSigns.CABAL_DAWN;
				blueStonesNeeded = 900;
				greenStonesNeeded = 540;
				redStonesNeeded = 270;
				break;
			case 31128:
			case 31133:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_42;
				festivalOracle = SevenSigns.CABAL_DAWN;
				blueStonesNeeded = 1500;
				greenStonesNeeded = 900;
				redStonesNeeded = 450;
				break;
			case 31129:
			case 31134:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_53;
				festivalOracle = SevenSigns.CABAL_DAWN;
				blueStonesNeeded = 3000;
				greenStonesNeeded = 1800;
				redStonesNeeded = 900;
				break;
			case 31130:
			case 31135:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_64;
				festivalOracle = SevenSigns.CABAL_DAWN;
				blueStonesNeeded = 4500;
				greenStonesNeeded = 2700;
				redStonesNeeded = 1350;
				break;
			case 31131:
			case 31136:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_NONE;
				festivalOracle = SevenSigns.CABAL_DAWN;
				blueStonesNeeded = 6000;
				greenStonesNeeded = 3600;
				redStonesNeeded = 1800;
				break;
			
			case 31137:
			case 31142:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_31;
				festivalOracle = SevenSigns.CABAL_DUSK;
				blueStonesNeeded = 900;
				greenStonesNeeded = 540;
				redStonesNeeded = 270;
				break;
			case 31138:
			case 31143:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_42;
				festivalOracle = SevenSigns.CABAL_DUSK;
				blueStonesNeeded = 1500;
				greenStonesNeeded = 900;
				redStonesNeeded = 450;
				break;
			case 31139:
			case 31144:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_53;
				festivalOracle = SevenSigns.CABAL_DUSK;
				blueStonesNeeded = 3000;
				greenStonesNeeded = 1800;
				redStonesNeeded = 900;
				break;
			case 31140:
			case 31145:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_64;
				festivalOracle = SevenSigns.CABAL_DUSK;
				blueStonesNeeded = 4500;
				greenStonesNeeded = 2700;
				redStonesNeeded = 1350;
				break;
			case 31141:
			case 31146:
				festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_FOR_NONE;
				festivalOracle = SevenSigns.CABAL_DUSK;
				blueStonesNeeded = 6000;
				greenStonesNeeded = 3600;
				redStonesNeeded = 1800;
				break;
		}
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.startsWith("FestivalDesc"))
		{
			final int val = Integer.parseInt(command.substring(13));
			
			showChatWindow(player, val, null, true);
		}
		else if (command.startsWith("Festival"))
		{
			L2Party playerParty = player.getParty();
			final int val = Integer.parseInt(command.substring(9, 10));
			
			switch (val)
			{
				case 1: // Become a Participant
					// Check if the festival period is active, if not then don't allow registration.
					if (SevenSigns.getInstance().isSealValidationPeriod())
					{
						showChatWindow(player, 2, "a", false);
						return;
					}
					
					// Check if a festival is in progress, then don't allow registration yet.
					if (SevenSignsFestival.getInstance().isFestivalInitialized())
					{
						player.sendMessage("You cannot sign up while a festival is in progress.");
						return;
					}
					
					// Check if the player is in a formed party already.
					if (playerParty == null)
					{
						showChatWindow(player, 2, "b", false);
						return;
					}
					
					// Check if the player is the party leader.
					if (!playerParty.isLeader(player))
					{
						showChatWindow(player, 2, "c", false);
						return;
					}
					
					// Check to see if the party has at least 5 members.
					if (playerParty.getMemberCount() < Config.ALT_FESTIVAL_MIN_PLAYER)
					{
						showChatWindow(player, 2, "b", false);
						return;
					}
					
					// Check if all the party members are in the required level range.
					if (playerParty.getLevel() > SevenSignsFestival.getMaxLevelForFestival(festivalType))
					{
						showChatWindow(player, 2, "d", false);
						return;
					}
					
					// TODO: Check if the player has delevelled by comparing their skill levels.
					
					/*
					 * Check to see if the player has already signed up, if they are then update the participant list providing all the required criteria has been met.
					 */
					if (player.isFestivalParticipant())
					{
						SevenSignsFestival.getInstance().setParticipants(festivalOracle, festivalType, playerParty);
						showChatWindow(player, 2, "f", false);
						return;
					}
					
					showChatWindow(player, 1, null, false);
					break;
				case 2: // Festival 2 xxxx
					final int stoneType = Integer.parseInt(command.substring(11));
					int stonesNeeded = 0;
					
					switch (stoneType)
					{
						case SevenSigns.SEAL_STONE_BLUE_ID:
							stonesNeeded = blueStonesNeeded;
							break;
						case SevenSigns.SEAL_STONE_GREEN_ID:
							stonesNeeded = greenStonesNeeded;
							break;
						case SevenSigns.SEAL_STONE_RED_ID:
							stonesNeeded = redStonesNeeded;
							break;
					}
					
					if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true))
					{
						return;
					}
					
					SevenSignsFestival.getInstance().setParticipants(festivalOracle, festivalType, playerParty);
					SevenSignsFestival.getInstance().addAccumulatedBonus(festivalType, stoneType, stonesNeeded);
					
					showChatWindow(player, 2, "e", false);
					break;
				case 3: // Score Registration
					// Check if the festival period is active, if not then don't register the score.
					if (SevenSigns.getInstance().isSealValidationPeriod())
					{
						showChatWindow(player, 3, "a", false);
						return;
					}
					
					// Check if a festival is in progress, if it is don't register the score.
					if (SevenSignsFestival.getInstance().isFestivalInProgress())
					{
						player.sendMessage("You cannot register a score while a festival is in progress.");
						return;
					}
					
					// Check if the player is in a party.
					if (playerParty == null)
					{
						showChatWindow(player, 3, "b", false);
						return;
					}
					
					List<L2PcInstance> prevParticipants = SevenSignsFestival.getInstance().getPreviousParticipants(festivalOracle, festivalType);
					
					// Check if there are any past participants.
					if (prevParticipants == null)
					{
						return;
					}
					
					// Check if this player was among the past set of participants for this festival.
					if (!prevParticipants.contains(player))
					{
						showChatWindow(player, 3, "b", false);
						return;
					}
					
					// Check if this player was the party leader in the festival.
					if (player.getObjectId() != prevParticipants.get(0).getObjectId())
					{
						showChatWindow(player, 3, "b", false);
						return;
					}
					
					L2ItemInstance bloodOfferings = player.getInventory().getItemByItemId(SevenSignsFestival.FESTIVAL_OFFERING_ID);
					int offeringCount = 0;
					
					// Check if the player collected any blood offerings during the festival.
					if (bloodOfferings == null)
					{
						player.sendMessage("You do not have any blood offerings to contribute.");
						return;
					}
					
					offeringCount = bloodOfferings.getCount();
					
					final int offeringScore = offeringCount * SevenSignsFestival.FESTIVAL_OFFERING_VALUE;
					final boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(player, festivalOracle, festivalType, offeringScore);
					
					player.destroyItem("SevenSigns", bloodOfferings, this, false);
					
					// Send message that the contribution score has increased.
					SystemMessage sm = new SystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED);
					sm.addNumber(offeringScore);
					player.sendPacket(sm);
					sm = null;
					
					if (isHighestScore)
					{
						showChatWindow(player, 3, "c", false);
					}
					else
					{
						showChatWindow(player, 3, "d", false);
					}
					
					prevParticipants = null;
					bloodOfferings = null;
					break;
				case 4: // Current High Scores
					TextBuilder strBuffer = new TextBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
					
					final StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DAWN, festivalType);
					final StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(SevenSigns.CABAL_DUSK, festivalType);
					final StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(festivalType);
					
					final int dawnScore = dawnData.getInteger("score");
					final int duskScore = duskData.getInteger("score");
					int overallScore = 0;
					
					// If no data is returned, assume there is no record, or all scores are 0.
					if (overallData != null)
					{
						overallScore = overallData.getInteger("score");
					}
					
					strBuffer.append(SevenSignsFestival.getFestivalName(festivalType) + " festival.<br>");
					
					if (dawnScore > 0)
					{
						strBuffer.append("Dawn: " + calculateDate(dawnData.getString("date")) + ". Score " + dawnScore + "<br>" + dawnData.getString("members") + "<br>");
					}
					else
					{
						strBuffer.append("Dawn: No record exists. Score 0<br>");
					}
					
					if (duskScore > 0)
					{
						strBuffer.append("Dusk: " + calculateDate(duskData.getString("date")) + ". Score " + duskScore + "<br>" + duskData.getString("members") + "<br>");
					}
					else
					{
						strBuffer.append("Dusk: No record exists. Score 0<br>");
					}
					
					if ((overallScore > 0) && (overallData != null))
					{
						String cabalStr = "Children of Dusk";
						
						if (overallData.getString("cabal").equals("dawn"))
						{
							cabalStr = "Children of Dawn";
						}
						
						strBuffer.append("Consecutive top scores: " + calculateDate(overallData.getString("date")) + ". Score " + overallScore + "<br>Affilated side: " + cabalStr + "<br>" + overallData.getString("members") + "<br>");
					}
					else
					{
						strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
					}
					
					strBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_Chat 0\">Go back.</a></body></html>");
					
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml(strBuffer.toString());
					player.sendPacket(html);
					strBuffer = null;
					html = null;
					break;
				case 8: // Increase the Festival Challenge
					if (playerParty == null)
					{
						return;
					}
					
					if (!SevenSignsFestival.getInstance().isFestivalInProgress())
					{
						return;
					}
					
					if (!playerParty.isLeader(player))
					{
						showChatWindow(player, 8, "a", false);
						break;
					}
					
					if (SevenSignsFestival.getInstance().increaseChallenge(festivalOracle, festivalType))
					{
						showChatWindow(player, 8, "b", false);
					}
					else
					{
						showChatWindow(player, 8, "c", false);
					}
					break;
				case 9: // Leave the Festival
					if (playerParty == null)
					{
						return;
					}
					
					/**
					 * If the player is the party leader, remove all participants from the festival (i.e. set the party to null, when updating the participant list) otherwise just remove this player from the "arena", and also remove them from the party.
					 */
					final boolean isLeader = playerParty.isLeader(player);
					
					if (isLeader)
					{
						SevenSignsFestival.getInstance().updateParticipants(player, null);
					}
					else
					{
						SevenSignsFestival.getInstance().updateParticipants(player, playerParty);
						playerParty.removePartyMember(player);
					}
					break;
				case 0: // Distribute Accumulated Bonus
					if (!SevenSigns.getInstance().isSealValidationPeriod())
					{
						player.sendMessage("Bonuses cannot be paid during the competition period.");
						return;
					}
					
					if (SevenSignsFestival.getInstance().distribAccumulatedBonus(player) > 0)
					{
						showChatWindow(player, 0, "a", false);
					}
					else
					{
						showChatWindow(player, 0, "b", false);
					}
					break;
				default:
					showChatWindow(player, val, null, false);
			}
			playerParty = null;
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
	
	/**
	 * Show chat window.
	 * @param player        the player
	 * @param val           the val
	 * @param suffix        the suffix
	 * @param isDescription the is description
	 */
	private void showChatWindow(final L2PcInstance player, final int val, final String suffix, final boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH + "festival/";
		filename += isDescription ? "desc_" : "festival_";
		filename += suffix != null ? val + suffix + ".htm" : val + ".htm";
		
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalType%", SevenSignsFestival.getFestivalName(festivalType));
		html.replace("%cycleMins%", String.valueOf(SevenSignsFestival.getInstance().getMinsToNextCycle()));
		if (!isDescription && "2b".equals(val + suffix))
		{
			html.replace("%minFestivalPartyMembers%", String.valueOf(Config.ALT_FESTIVAL_MIN_PLAYER));
		}
		
		// If the stats or bonus table is required, construct them.
		if (val == 5)
		{
			html.replace("%statsTable%", getStatsTable());
		}
		if (val == 6)
		{
			html.replace("%bonusTable%", getBonusTable());
		}
		
		// festival's fee
		if (val == 1)
		{
			html.replace("%blueStoneNeeded%", String.valueOf(blueStonesNeeded));
			html.replace("%greenStoneNeeded%", String.valueOf(greenStonesNeeded));
			html.replace("%redStoneNeeded%", String.valueOf(redStonesNeeded));
		}
		
		player.sendPacket(html);
		filename = null;
		html = null;
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Gets the stats table.
	 * @return the stats table
	 */
	private final String getStatsTable()
	{
		final TextBuilder tableHtml = new TextBuilder();
		
		// Get the scores for each of the festival level ranges (types).
		for (int i = 0; i < 5; i++)
		{
			final int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);
			final int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
			final String festivalName = SevenSignsFestival.getFestivalName(i);
			String winningCabal = "Children of Dusk";
			
			if (dawnScore > duskScore)
			{
				winningCabal = "Children of Dawn";
			}
			else if (dawnScore == duskScore)
			{
				winningCabal = "None";
			}
			
			tableHtml.append("<tr><td width=\"100\" align=\"center\">" + festivalName + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>");
		}
		
		return tableHtml.toString();
	}
	
	/**
	 * Gets the bonus table.
	 * @return the bonus table
	 */
	private final String getBonusTable()
	{
		final TextBuilder tableHtml = new TextBuilder();
		
		// Get the accumulated scores for each of the festival level ranges (types).
		for (int i = 0; i < 5; i++)
		{
			final int accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
			final String festivalName = SevenSignsFestival.getFestivalName(i);
			
			tableHtml.append("<tr><td align=\"center\" width=\"150\">" + festivalName + "</td><td align=\"center\" width=\"150\">" + accumScore + "</td></tr>");
		}
		
		return tableHtml.toString();
	}
	
	/**
	 * Calculate date.
	 * @param  milliFromEpoch the milli from epoch
	 * @return                the string
	 */
	private final String calculateDate(final String milliFromEpoch)
	{
		final long numMillis = Long.valueOf(milliFromEpoch);
		final Calendar calCalc = Calendar.getInstance();
		
		calCalc.setTimeInMillis(numMillis);
		
		return calCalc.get(Calendar.YEAR) + "/" + calCalc.get(Calendar.MONTH) + "/" + calCalc.get(Calendar.DAY_OF_MONTH);
	}
}
