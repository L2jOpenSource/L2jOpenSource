/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.communitybbs.CommunityBoard;
import com.l2jserver.gameserver.data.xml.impl.AdminData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.enums.PlayerAction;
import com.l2jserver.gameserver.handler.AdminCommandHandler;
import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.PageResult;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcManorBypass;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerBypass;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.GMAudit;
import com.l2jserver.gameserver.util.HtmlUtil;
import com.l2jserver.gameserver.util.Util;

import SellbuffOffline.SellbuffOfflineSQL;
import WDConfig.ConfigWD;
import ZeuS.ZeuS;
import main.EngineModsManager;

/**
 * RequestBypassToServer client packet implementation.
 * @author HorridoJoho
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__23_REQUESTBYPASSTOSERVER = "[C] 23 RequestBypassToServer";
	// FIXME: This is for compatibility, will be changed when bypass functionality got an overhaul by NosBit
	private static final String[] _possibleNonHtmlCommands =
	{
		"_bbs",
		"bbs",
		"_mail",
		"_friend",
		"_match",
		"_diary",
		"_olympiad?command",
		"manor_menu_select",
		"_bbsloc"
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
			_log.warning("Player " + activeChar.getName() + " sent empty bypass!");
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
				if (Config.ZEUS_ACTIVE && !ZeuS.isCMDFromZeuS(activeChar, _command))
				{
					_log.warning("Player " + activeChar.getName() + " sent non cached bypass: '" + _command + "'");
					return;
				}
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
					_log.warning(activeChar + " requested not registered admin command '" + command + "'");
					return;
				}
				
				if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command!");
					_log.warning("Character " + activeChar.getName() + " tried to use admin command " + command + ", without proper access level!");
					return;
				}
				
				if (AdminData.getInstance().requireConfirm(command))
				{
					activeChar.setAdminConfirmCmd(_command);
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
					dlg.addString("Are you sure you want execute command " + _command.substring(6) + " ?");
					activeChar.addAction(PlayerAction.ADMIN_COMMAND);
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
			else if (_command.startsWith("_bbs") || _command.startsWith("bbs") || _command.startsWith("_friend") || _command.startsWith("_block") || _command.startsWith("_mail") || _command.startsWith("_bbsloc"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.equals("come_here") && activeChar.isGM())
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("npc_"))
			{
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
					final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(Integer.parseInt(id));
					if ((item != null) && (endOfId > 0))
					{
						item.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe)
				{
					_log.log(Level.WARNING, "NFE for command [" + _command + "]", nfe);
				}
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
			else if (_command.startsWith("ZeuSNPC "))
			{
				ZeuS.talkNpc(activeChar, _command.substring(8));
			}
			else if (_command.startsWith("dressme"))
			{
				ZeuS.talkNpc(activeChar, "dressme");
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
			// TODO: Command Sell Buff
			else if (_command.startsWith("buff"))
			{
				String[] val = _command.split(" ");
				int id = Integer.parseInt(val[1]);
				int page = Integer.parseInt(val[2]);
				
				L2PcInstance target = null;
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					target = (L2PcInstance) activeChar.getTarget();
				}
				
				if (target == null)
				{
					activeChar.sendMessage("No target!");
					return;
				}
				
				if ((activeChar.getInventory().getItemByItemId(57) == null) || (activeChar.getInventory().getItemByItemId(57).getCount() < ((L2PcInstance) activeChar.getTarget()).getBuffPrize()))
				{
					activeChar.sendMessage("You do not have adenas to pay buffs!");
					return;
				}
				
				try
				{
					// Add skill to player
					Skill s = SkillData.getInstance().getSkill(id, target.getSkillLevel(id));
					s.applyEffects(activeChar, activeChar);
					activeChar.sendMessage("You have purchased the buff: " + s.getName());
					
					// TODO: Skill Effect
					activeChar.setTarget(activeChar);
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, id, target.getSkillLevel(id), 1000, 0));
					
					// Pay the buff and delete item to sell
					activeChar.getInventory().destroyItemByItemId("", 57, target.getBuffPrize(), activeChar, null);
					target.getInventory().addItem("", 57, target.getBuffPrize(), target, null);
					
					// Message
					activeChar.sendMessage("You have pay: " + target.getBuffPrize() + " adenas.");
					
					// Html
					activeChar.setTarget(target);
					buffList(activeChar, page);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else if (_command.startsWith("fb_show_list"))
			{
				String[] val = _command.split(" ");
				int page = 0;
				
				if (val.length >= 1)
				{
					page = Integer.parseInt(val[1]);
				}
				
				buffList(activeChar, page);
				
			}
			else if (_command.startsWith("actr"))
			{
				String l = _command.substring(5);
				int p = 0;
				
				p = Integer.parseInt(l);
				
				if (p < Config.COMMAND_SELL_BUFF_MIN_PRICE)
				{
					if (p == 0)
					{
						activeChar.sendMessage("The value can not be zero. The minimum value is " + Config.COMMAND_SELL_BUFF_MIN_PRICE + " adenas!");
						return;
					}
					
					activeChar.sendMessage("The indicated price is insufficient. The minimum price is " + Config.COMMAND_SELL_BUFF_MIN_PRICE + " adenas!");
					return;
				}
				
				if (p > Config.COMMAND_SELL_BUFF_MAX_PRICE)
				{
					activeChar.sendMessage("You have exceeded the limit price. The limit price is " + Config.COMMAND_SELL_BUFF_MAX_PRICE + " adenas!");
					return;
				}
				
				// Effects
				activeChar.setBuffPrize(p);
				activeChar.sitDown();
				activeChar.setSellBuff(true);
				SellbuffOfflineSQL.getInstance().storeSellbuffPlayers(activeChar.getObjectId(), activeChar.getAppearance().getNameColor(), activeChar.getTitle(), activeChar.getAppearance().getTitleColor(), activeChar.getBuffPrize());
				// Save
				activeChar.setOldTitle(activeChar.getTitle());
				activeChar.setOldNameColor(activeChar.getAppearance().getNameColor());
				activeChar.setOldTitleColor(activeChar.getAppearance().getTitleColor());
				// Changes
				// sellbuff offline
				activeChar.getAppearance().setNameColor(ConfigWD.WD_SELL_BUFF_OFFLINE_NAME_COLOR);
				activeChar.setTitle(ConfigWD.WD_SELL_BUFF_OFFLINE_TITLE);
				activeChar.getAppearance().setTitleColor(ConfigWD.WD_SELL_BUFF_OFFLINE_TITLE_COLOR);
				
				// sellbuff online
				// activeChar.getAppearance().setNameColor(Config.COMMAND_SELL_BUFF_NAME_COLOUR);
				// activeChar.setTitle(Config.COMMAND_SELL_BUFF_TITLE);
				// activeChar.getAppearance().setTitleColor(Config.COMMAND_SELL_BUFF_TITLE_COLOUR);
				
				// Update
				activeChar.broadcastUserInfo();
				activeChar.broadcastTitleInfo();
			}
			else if (_command.startsWith("Engine"))
			{
				EngineModsManager.onEvent(activeChar, activeChar.getLastNpcTalk(), _command.replace("Engine ", ""));
			}
			else
			{
				final IBypassHandler handler = BypassHandler.getInstance().getHandler(_command);
				if (handler != null)
				{
					if (bypassOriginId > 0)
					{
						L2Object bypassOrigin = activeChar.getKnownList().getKnownObjects().get(bypassOriginId);
						if ((bypassOrigin != null) && bypassOrigin.isInstanceTypes(InstanceType.L2Character))
						{
							handler.useBypass(_command, activeChar, (L2Character) bypassOrigin);
						}
						else
						{
							handler.useBypass(_command, activeChar, null);
						}
					}
					else
					{
						handler.useBypass(_command, activeChar, null);
					}
				}
				else
				{
					_log.warning(getClient() + " sent not handled RequestBypassToServer: [" + _command + "]");
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception processing bypass from player " + activeChar.getName() + ": " + _command, e);
			
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
				final NpcHtmlMessage msg = new NpcHtmlMessage(0, 1, sb.toString());
				msg.disableValidation();
				activeChar.sendPacket(msg);
			}
		}
		
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerBypass(activeChar, _command), activeChar);
	}
	
	/**
	 * @param activeChar
	 * @param page
	 */
	public static void buffList(L2PcInstance activeChar, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "data/html/custom/sellbuff/sellbufflist.htm");
		
		L2PcInstance target = null;
		if (activeChar.getTarget() instanceof L2PcInstance)
		{
			target = (L2PcInstance) activeChar.getTarget();
		}
		
		if ((target != null) && (target.isSellBuff()))
		{
			if (Config.SELLBUFF_ONLY_CLAN_ALY_MEMBERS)
			{
				boolean noAllyMember = true;
				if ((target.getClan() != null) && (activeChar.getClan() != null))
				{
					if (target.getClan() != activeChar.getClan())
					{
						if ((target.getClan().getAllyId() != -1) && (activeChar.getClan().getAllyId() != -1) && (target.getClan().getAllyId() == activeChar.getClan().getAllyId()))
						{
							noAllyMember = false;
						}
					}
					else
					{
						noAllyMember = false;
					}
				}
				if (noAllyMember)
				{
					activeChar.sendMessage("Only clan/ally member can use my buff");
					return;
				}
			}
			ArrayList<Skill> ba = new ArrayList<>();
			for (Skill skill : target.getAllSkills())
			{
				{
					{
						if (Config.ENABLE_LIST_SKILL_ALLOWED && (Arrays.binarySearch(Config.COMMAND_SELL_BUFF_LIST_SKILL_ON, skill.getId()) >= 0))
						{
							ba.add(skill);
							continue;
						}
						if (!Config.ENABLE_LIST_SKILL_ALLOWED && (Arrays.binarySearch(Config.COMMAND_SELL_BUFF_LIST_SKILL_OFF, skill.getId()) >= 0))
						{
							continue;
						}
						
						if (!Config.ENABLE_LIST_SKILL_ALLOWED)
						{
							ba.add(skill);
						}
					}
				}
			}
			
			final PageResult result = HtmlUtil.createPage(ba, page, 10, i ->
			{
				return "<td align=center><a action=\"bypass -h fb_show_list " + i + "\">Page " + (i + 1) + "</a></td>";
			}, skill ->
			{
				StringBuilder sb = new StringBuilder();
				sb.append("<tr>");
				sb.append("<td><img src=\"" + skill.getIcon() + "\" width=32 height=32></td>");
				sb.append("<td><button value=\"" + skill.getName() + "\" action=\"bypass -h buff " + skill.getId() + " " + page + "\" width=200 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				sb.append("<td><img src=\"" + skill.getIcon() + "\" width=32 height=32></td>");
				sb.append("</tr>");
				return sb.toString();
			});
			
			if (result.getPages() > 0)
			{
				html.replace("%pages%", "<table width=280 cellspacing=0><tr>" + result.getPagerTemplate() + "</tr></table>");
			}
			else
			{
				html.replace("%pages%", "");
			}
			
			html.replace("%buffprice%", target.getBuffPrize());
			html.replace("%bufflist%", result.getBodyTemplate().toString());
			activeChar.sendPacket(html);
		}
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
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, activeChar.getLocation());
		}
	}
	
	@Override
	public String getType()
	{
		return _C__23_REQUESTBYPASSTOSERVER;
	}
}
