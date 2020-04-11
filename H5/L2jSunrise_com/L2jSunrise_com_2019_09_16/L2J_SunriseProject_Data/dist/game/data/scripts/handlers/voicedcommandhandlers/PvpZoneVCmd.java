/**
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.util.Rnd;

import gr.sr.configsEngine.configs.impl.ChaoticZoneConfigs;

/**
 * @author -=DoctorNo=-
 */
public class PvpZoneVCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"return",
		"enter",
		"zone"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("zone"))
		{
			NpcHtmlMessage playerReply = new NpcHtmlMessage();
			StringBuilder replyMSG = new StringBuilder();
			replyMSG.append("<html><title>Zone Manager</title><body>");
			replyMSG.append("<br><font color=\"LEVEL\">Custom Zones...:</font><br><br>");
			replyMSG.append("    ... Name:&nbsp;<font color=\"00FF00\">Chaotic Zone.</font><br>");
			replyMSG.append("    ... Description:&nbsp;<font color=\"00FF00\">PvP Zone.</font><br>");
			replyMSG.append("    ... Info about zone:&nbsp;<font color=\"00FF00\">100% PvP zone, " + "when enter this zone game will automatically " + "change your PvP status to active. Private shops are not allowed in this zone " + "and the only way to leave or enter is via the below commands.</font><br>");
			replyMSG.append("    ... Commands:&nbsp;<font color=\"00FF00\">.enter  .return  .zone</font><br>");
			replyMSG.append("</body></html>");
			
			playerReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(playerReply);
			return false;
		}
		if (activeChar.isDead())
		{
			activeChar.sendMessage("You cannot use this feature if you're dead.");
			return false;
		}
		else if (activeChar.getKarma() > 0)
		{
			activeChar.sendMessage("You cannot use this feature while you have karma.");
			return false;
		}
		if (activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use this feature while in combat.");
			return false;
		}
		if (activeChar.isInOlympiadMode() || activeChar.inObserverMode())
		{
			activeChar.sendMessage("You cannot use this feature while in olympiad.");
			return false;
		}
		if (activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot use this feature while you are in Jail.");
			return false;
		}
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You cannot use this feature during Duel.");
			return false;
		}
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You cannot use this feature during a Festival.");
			return false;
		}
		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You cannot use this feature during Observer Mode.");
			return false;
		}
		if (activeChar.getInventory().getItemByItemId(57) == null)
		{
			activeChar.sendMessage("You don't have enough adena to use this command.");
			return false;
		}
		if (command.equalsIgnoreCase("return"))
		{
			if (activeChar.getWorldRegion().containsZone(42490))
			{
				activeChar.sendMessage("You will be send to nearest town!");
				activeChar.getInventory().destroyItemByItemId("root", 57, 500, activeChar, activeChar.getTarget());
				activeChar.broadcastUserInfo();
				Location loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
				activeChar.setInstanceId(0);
				activeChar.teleToLocation(loc, true);
			}
			else
			{
				activeChar.sendMessage("Cannot use this command here.");
			}
		}
		else if (activeChar.getPvpFlag() == 1)
		{
			activeChar.sendMessage("You cannot use this feature during PvP Mode.");
			return false;
		}
		else if (activeChar.isInsideZone(ZoneIdType.ZONE_CHAOTIC))
		{
			activeChar.sendMessage("Cannot use in chaotic zone.");
			return false;
		}
		else if (command.equalsIgnoreCase("enter"))
		{
			activeChar.sendMessage("You will be send to chaotic zone!");
			activeChar.getInventory().destroyItemByItemId("root", 57, 500, activeChar, activeChar.getTarget());
			activeChar.broadcastUserInfo();
			int r = Rnd.get(ChaoticZoneConfigs.CHAOTIC_ZONE_AUTO_RES_LOCS_COUNT);
			activeChar.teleToLocation(ChaoticZoneConfigs.xCoords[r], ChaoticZoneConfigs.yCoords[r], ChaoticZoneConfigs.zCoords[r]);
		}
		else
		{
			activeChar.sendMessage("Cannot enter right now, try again later.");
		}
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}