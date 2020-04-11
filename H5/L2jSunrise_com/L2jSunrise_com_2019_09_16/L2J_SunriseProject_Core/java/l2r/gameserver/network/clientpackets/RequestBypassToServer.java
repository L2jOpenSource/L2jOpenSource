/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.network.clientpackets;

import java.util.StringTokenizer;

import l2r.Config;
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.data.xml.impl.AdminData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.BypassHandler;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.model.events.impl.character.npc.OnNpcManorBypass;
import l2r.gameserver.model.events.impl.character.player.OnPlayerBypass;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.GMAudit;
import l2r.gameserver.util.Util;

import gr.sr.aioItem.AioItemNpcs;
import gr.sr.configsEngine.configs.impl.AioItemsConfigs;
import gr.sr.configsEngine.configs.impl.BufferConfigs;
import gr.sr.configsEngine.configs.impl.IndividualVoteSystemConfigs;
import gr.sr.interf.SunriseEvents;
import gr.sr.javaBuffer.buffItem.AioItemBuffer;
import gr.sr.voteEngine.old.VoteHandler;

/**
 * This class ...
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__23_REQUESTBYPASSTOSERVER = "[C] 23 RequestBypassToServer";
	// FIXME: This is for compatibility, will be changed when bypass functionality got an overhaul by NosBit
	private static final String[] _possibleNonHtmlCommands =
	{
		"_bbs",
		"bbs",
		"nxs",
		"voice",
		"_mail",
		"_friend",
		"_match",
		"_diary",
		"_olympiad?command",
		"manor_menu_select",
		"admin_"
	};
	
	// S
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_command.isEmpty())
		{
			_log.info(activeChar.getName() + " send empty requestbypass");
			activeChar.logout();
			return;
		}
		
		boolean requiresBypassValidation = true;
		for (String possibleNonHtmlCommand : _possibleNonHtmlCommands)
		{
			if (_command.startsWith(possibleNonHtmlCommand))
			{
				requiresBypassValidation = false;
				break;
			}
		}
		
		int bypassOriginId = 0;
		if (requiresBypassValidation)
		{
			bypassOriginId = activeChar.validateHtmlAction(_command);
			if (bypassOriginId == -1)
			{
				// _log.warn("Player " + activeChar.getName() + " sent non cached bypass: '" + _command + "'");
				return;
			}
			
			if ((bypassOriginId > 0) && !Util.isInsideRangeOfObjectId(activeChar, bypassOriginId, L2Npc.INTERACTION_DISTANCE))
			{
				// No logging here, this could be a common case where the player has the html still open and run too far away and then clicks a html action
				return;
			}
		}
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}
		
		try
		{
			if (SunriseEvents.onBypass(activeChar, _command))
			{
				return;
			}
			
			if (_command.startsWith("admin_"))
			{
				String command = _command.split(" ")[0];
				
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				
				if (ach == null)
				{
					if (activeChar.isGM())
					{
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");
					}
					_log.warn(activeChar + " requested not registered admin command '" + command + "'");
					return;
				}
				
				if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					_log.warn("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}
				
				if (SunriseEvents.adminCommandRequiresConfirm(_command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.split(" ")[1] + " ?");
					activeChar.sendPacket(dlg);
					return;
				}
				
				if (AdminData.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.sendPacket(dlg);
				}
				else
				{
					if (Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
					}
					
					ach.useAdminCommand(_command, activeChar);
				}
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
				activeChar.setIsUsingAioWh(false);
				activeChar.setIsUsingAioMultisell(false);
				
				int endOfId = _command.indexOf('_', 5);
				String id = endOfId > 0 ? _command.substring(4, endOfId) : _command.substring(4);
				
				if (Util.isDigit(id))
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					
					if ((object != null) && object.isNpc() && (endOfId > 0) && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
					{
						((L2Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
				}
				
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("item_"))
			{
				int endOfId = _command.indexOf('_', 5);
				String id = endOfId > 0 ? _command.substring(5, endOfId) : _command.substring(5);
				
				try
				{
					L2ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
					
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe)
				{
					_log.warn("NFE for command [" + _command + "]", nfe);
				}
			}
			else if (_command.startsWith("manor_menu_select"))
			{
				final L2Npc lastNpc = activeChar.getLastFolkNPC();
				if (Config.ALLOW_MANOR && (lastNpc != null) && lastNpc.canInteract(activeChar))
				{
					final String[] split = _command.substring(_command.indexOf("?") + 1).split("&");
					final int ask = Integer.parseInt(split[0].split("=")[1]);
					final int state = Integer.parseInt(split[1].split("=")[1]);
					final boolean time = split[2].split("=")[1].equals("1");
					EventDispatcher.getInstance().notifyEventAsync(new OnNpcManorBypass(activeChar, lastNpc, ask, state, time), lastNpc);
				}
			}
			else if (_command.startsWith("_bbs") || _command.startsWith("bbs") || _command.startsWith("_maillist") || _command.startsWith("_friendlist"))
			{
				BoardsManager.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("Aioitem"))
			{
				if (AioItemsConfigs.ENABLE_AIO_NPCS)
				{
					AioItemNpcs.onBypassFeedback(activeChar, _command.substring(8));
				}
			}
			else if (_command.startsWith("Aiobuff"))
			{
				if (BufferConfigs.ENABLE_ITEM_BUFFER)
				{
					AioItemBuffer.onBypassFeedback(activeChar, _command.substring(8));
				}
			}
			else if (_command.startsWith("Vote"))
			{
				if (IndividualVoteSystemConfigs.ENABLE_VOTE_SYSTEM)
				{
					VoteHandler.onBypassFeedback(activeChar, _command.substring(5));
				}
			}
			else if (_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
				{
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
				}
			}
			else if (_command.startsWith("_olympiad?command"))
			{
				int arenaId = Integer.parseInt(_command.split("=")[2]);
				final IBypassHandler handler = BypassHandler.getInstance().getHandler("arenachange");
				if (handler != null)
				{
					handler.useBypass("arenachange " + (arenaId - 1), activeChar, null);
				}
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					handler.useBypass(_command, activeChar, null);
				}
				else
				{
					_log.warn(getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getClient() + " sent bad RequestBypassToServer: \"" + _command + "\"", e);
			if (activeChar.isGM())
			{
				StringBuilder sb = new StringBuilder(200);
				sb.append("<html><body>");
				sb.append("Bypass error: " + e + "<br1>");
				sb.append("Bypass command: " + _command + "<br1>");
				sb.append("StackTrace:<br1>");
				for (StackTraceElement ste : e.getStackTrace())
				{
					sb.append(ste.toString() + "<br1>");
				}
				sb.append("</body></html>");
				// item html
				NpcHtmlMessage msg = new NpcHtmlMessage(0, 12807);
				msg.setHtml(sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
		
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerBypass(activeChar, _command), activeChar);
	}
	
	/**
	 * @param activeChar
	 */
	private static void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null)
		{
			return;
		}
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}
}
