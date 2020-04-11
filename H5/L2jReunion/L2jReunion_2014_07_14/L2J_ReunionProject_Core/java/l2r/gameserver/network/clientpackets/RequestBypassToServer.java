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
package l2r.gameserver.network.clientpackets;

import java.util.List;
import java.util.StringTokenizer;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.communitybbs.BoardsManager;
import l2r.gameserver.datatables.xml.AdminData;
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
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.scripting.scriptengine.events.RequestBypassToServerEvent;
import l2r.gameserver.scripting.scriptengine.listeners.talk.RequestBypassToServerListener;
import l2r.gameserver.util.GMAudit;
import l2r.gameserver.util.Util;
import gr.reunion.aioItem.AioItemNpcs;
import gr.reunion.configsEngine.AioBufferConfigs;
import gr.reunion.configsEngine.AioItemsConfigs;
import gr.reunion.configsEngine.IndividualVoteSystemConfigs;
import gr.reunion.interf.ReunionEvents;
import gr.reunion.javaBuffer.buffItem.AioItemBuffer;
import gr.reunion.voteEngine.old.VoteHandler;

/**
 * This class ...
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__23_REQUESTBYPASSTOSERVER = "[C] 23 RequestBypassToServer";
	private static final List<RequestBypassToServerListener> _listeners = new FastList<RequestBypassToServerListener>().shared();
	
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
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}
		
		if (_command.isEmpty())
		{
			_log.info(activeChar.getName() + " send empty requestbypass");
			activeChar.logout();
			return;
		}
		
		try
		{
			if (ReunionEvents.onBypass(activeChar, _command))
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
				
				if (ReunionEvents.adminCommandRequiresConfirm(_command))
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
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				activeChar.setIsUsingAioWh(false);
				activeChar.setIsUsingAioMultisell(false);
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
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
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(5, endOfId);
				}
				else
				{
					id = _command.substring(5);
				}
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
				final IBypassHandler manor = BypassHandler.getInstance().getHandler("manor_menu_select");
				if (manor != null)
				{
					manor.useBypass(_command, activeChar, null);
				}
			}
			else if (_command.startsWith("_bbs") || _command.startsWith("_maillist") || _command.startsWith("_friendlist"))
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
					String sub = _command.substring(8);
					AioItemNpcs.onBypassFeedback(activeChar, sub);
				}
			}
			else if (_command.startsWith("Aiobuff"))
			{
				if (AioBufferConfigs.ENABLE_AIO_BUFFER)
				{
					String sub = _command.substring(8);
					AioItemBuffer.onBypassFeedback(activeChar, sub);
				}
			}
			else if (_command.startsWith("Vote"))
			{
				if (IndividualVoteSystemConfigs.ENABLE_VOTE_SYSTEM)
				{
					String sub = _command.substring(5);
					VoteHandler.onBypassFeedback(activeChar, sub);
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
		
		fireBypassListeners();
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
	
	/**
	 * Fires the event when packet arrived.
	 */
	private void fireBypassListeners()
	{
		RequestBypassToServerEvent event = new RequestBypassToServerEvent();
		event.setActiveChar(getActiveChar());
		event.setCommand(_command);
		
		for (RequestBypassToServerListener listener : _listeners)
		{
			listener.onRequestBypassToServer(event);
		}
	}
	
	/**
	 * @param listener
	 */
	public static void addBypassListener(RequestBypassToServerListener listener)
	{
		if (!_listeners.contains(listener))
		{
			_listeners.add(listener);
		}
	}
	
	/**
	 * @param listener
	 */
	public static void removeBypassListener(RequestBypassToServerListener listener)
	{
		if (_listeners.contains(listener))
		{
			_listeners.remove(listener);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}
}
