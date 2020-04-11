/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.SevenSignsFestival;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.util.StringUtil;

/**
 * Festival of Darkness Guide (Seven Signs)
 * @author Tempy
 */
public final class L2FestivalGuideInstance extends L2Npc
{
	private final int _festivalType;
	private final int _festivalOracle;
	private final int _blueStonesNeeded;
	private final int _greenStonesNeeded;
	private final int _redStonesNeeded;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2FestivalGuideInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2FestivalGiudeInstance);
		
		switch (getId())
		{
			case 31127:
			case 31132:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_31;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 900;
				_greenStonesNeeded = 540;
				_redStonesNeeded = 270;
				break;
			case 31128:
			case 31133:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_42;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 1500;
				_greenStonesNeeded = 900;
				_redStonesNeeded = 450;
				break;
			case 31129:
			case 31134:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_53;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 3000;
				_greenStonesNeeded = 1800;
				_redStonesNeeded = 900;
				break;
			case 31130:
			case 31135:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_64;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 4500;
				_greenStonesNeeded = 2700;
				_redStonesNeeded = 1350;
				break;
			case 31131:
			case 31136:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_NONE;
				_festivalOracle = SevenSigns.CABAL_DAWN;
				_blueStonesNeeded = 6000;
				_greenStonesNeeded = 3600;
				_redStonesNeeded = 1800;
				break;
			
			case 31137:
			case 31142:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_31;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 900;
				_greenStonesNeeded = 540;
				_redStonesNeeded = 270;
				break;
			case 31138:
			case 31143:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_42;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 1500;
				_greenStonesNeeded = 900;
				_redStonesNeeded = 450;
				break;
			case 31139:
			case 31144:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_53;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 3000;
				_greenStonesNeeded = 1800;
				_redStonesNeeded = 900;
				break;
			case 31140:
			case 31145:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_64;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 4500;
				_greenStonesNeeded = 2700;
				_redStonesNeeded = 1350;
				break;
			case 31141:
			case 31146:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_NONE;
				_festivalOracle = SevenSigns.CABAL_DUSK;
				_blueStonesNeeded = 6000;
				_greenStonesNeeded = 3600;
				_redStonesNeeded = 1800;
				break;
			default:
				_festivalType = SevenSignsFestival.FESTIVAL_LEVEL_MAX_NONE;
				_festivalOracle = SevenSigns.CABAL_NULL;
				_blueStonesNeeded = 0;
				_greenStonesNeeded = 0;
				_redStonesNeeded = 0;
		}
	}
	
	public int getFestivalType()
	{
		return _festivalType;
	}
	
	public int getFestivalOracle()
	{
		return _festivalOracle;
	}
	
	public int getStoneCount(int stoneType)
	{
		switch (stoneType)
		{
			case SevenSigns.SEAL_STONE_BLUE_ID:
				return _blueStonesNeeded;
			case SevenSigns.SEAL_STONE_GREEN_ID:
				return _greenStonesNeeded;
			case SevenSigns.SEAL_STONE_RED_ID:
				return _redStonesNeeded;
			default:
				return -1;
		}
	}
	
	public final void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
	{
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH + "festival/";
		filename += (isDescription) ? "desc_" : "festival_";
		filename += (suffix != null) ? val + suffix + ".htm" : val + ".htm";
		
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
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
			html.replace("%blueStoneNeeded%", String.valueOf(_blueStonesNeeded));
			html.replace("%greenStoneNeeded%", String.valueOf(_greenStonesNeeded));
			html.replace("%redStoneNeeded%", String.valueOf(_redStonesNeeded));
		}
		
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static final String getStatsTable()
	{
		final StringBuilder tableHtml = new StringBuilder(1000);
		
		// Get the scores for each of the festival level ranges (types).
		for (int i = 0; i < 5; i++)
		{
			int dawnScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DAWN, i);
			int duskScore = SevenSignsFestival.getInstance().getHighestScore(SevenSigns.CABAL_DUSK, i);
			String festivalName = SevenSignsFestival.getFestivalName(i);
			String winningCabal = "Children of Dusk";
			
			if (dawnScore > duskScore)
			{
				winningCabal = "Children of Dawn";
			}
			else if (dawnScore == duskScore)
			{
				winningCabal = "None";
			}
			
			StringUtil.append(tableHtml, "<tr><td width=\"100\" align=\"center\">", festivalName, "</td><td align=\"center\" width=\"35\">", String.valueOf(duskScore), "</td><td align=\"center\" width=\"35\">", String.valueOf(dawnScore), "</td><td align=\"center\" width=\"130\">", winningCabal, "</td></tr>");
		}
		
		return tableHtml.toString();
	}
	
	private static final String getBonusTable()
	{
		final StringBuilder tableHtml = new StringBuilder(500);
		
		// Get the accumulated scores for each of the festival level ranges (types).
		for (int i = 0; i < 5; i++)
		{
			int accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
			String festivalName = SevenSignsFestival.getFestivalName(i);
			
			StringUtil.append(tableHtml, "<tr><td align=\"center\" width=\"150\">", festivalName, "</td><td align=\"center\" width=\"150\">", String.valueOf(accumScore), "</td></tr>");
		}
		
		return tableHtml.toString();
	}
}
