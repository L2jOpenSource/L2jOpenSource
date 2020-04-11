package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.CursedWeapon;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles cw admin command. Parameters are following :
 * <ul>
 * <li>info : displays cursed weapons status.</li>
 * <li>add : adds a cursed weapon into the world - item id or name must be provided. Current target will be the owner.</li>
 * <li>remove : removes a cursed weapon from the world - item id or name must be provided.</li>
 * <li>goto : teleports GM to the specified cursed weapon (item or player position).</li>
 * </ul>
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		try
		{
			final String type = st.nextToken();
			
			if (type.startsWith("info"))
			{
				final StringBuilder sb = new StringBuilder(2000);
				for (CursedWeapon cw : CursedWeaponManager.getInstance().getCursedWeapons())
				{
					StringUtil.append(sb, "<table width=280><tr><td>Name:</td><td>", cw.getName(), "</td></tr>");
					
					if (cw.isActive())
					{
						long milliToStart = cw.getTimeLeft();
						
						double numSecs = (milliToStart / 1000) % 60;
						double countDown = ((milliToStart / 1000) - numSecs) / 60;
						int numMins = (int) Math.floor(countDown % 60);
						countDown = (countDown - numMins) / 60;
						int numHours = (int) Math.floor(countDown % 24);
						int numDays = (int) Math.floor((countDown - numHours) / 24);
						
						if (cw.isActivated())
						{
							Player pl = cw.getPlayer();
							StringUtil.append(sb, "<tr><td>Owner:</td><td>", ((pl == null) ? "null" : pl.getName()), "</td></tr><tr><td>Stored values:</td><td>Karma=", cw.getPlayerKarma(), " PKs=", cw.getPlayerPkKills(), "</td></tr><tr><td>Current stage:</td><td>", cw.getCurrentStage(), "</td></tr><tr><td>Overall time:</td><td>", numDays, "d. ", numHours, "h. ", numMins, "m.</td></tr><tr><td>Hungry time:</td><td>", cw.getHungryTime(), "m.</td></tr><tr><td>Current kills:</td><td>", cw.getNbKills(), " / ", cw.getNumberBeforeNextStage(), "</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw remove ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw goto ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
						}
						else if (cw.isDropped())
							StringUtil.append(sb, "<tr><td>Position:</td><td>Lying on the ground</td></tr><tr><td>Overall time:</td><td>", numDays, "d. ", numHours, "h. ", numMins, "m.</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw remove ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw goto ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
					}
					else
						StringUtil.append(sb, "<tr><td>Position:</td><td>Doesn't exist.</td></tr><tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw add ", cw.getItemId(), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td></td></tr>");
					
					sb.append("</table><br>");
				}
				
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/admin/cwinfo.htm");
				html.replace("%cwinfo%", sb.toString());
				activeChar.sendPacket(html);
			}
			else
			{
				int id = 0;
				
				String parameter = st.nextToken();
				if (StringUtil.isDigit(parameter))
					id = Integer.parseInt(parameter);
				else
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : CursedWeaponManager.getInstance().getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id = cwp.getItemId();
							break;
						}
					}
				}
				
				final CursedWeapon cw = CursedWeaponManager.getInstance().getCursedWeapon(id);
				if (cw == null)
				{
					activeChar.sendMessage("Unknown cursed weapon ID.");
					return false;
				}
				
				if (type.startsWith("add"))
				{
					if (cw.isActive())
						activeChar.sendMessage("This cursed weapon is already active.");
					else
					{
						WorldObject target = activeChar.getTarget();
						if (target instanceof Player)
							((Player) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
						else
							activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
						
						// Start task
						cw.reActivate(true);
					}
				}
				else if (type.startsWith("remove"))
					cw.endOfLife();
				else if (type.startsWith("goto"))
					cw.goTo(activeChar);
				else
					activeChar.sendMessage("Unknown command.");
			}
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Usage: //cw info");
			activeChar.sendMessage("Usage: //cw add | remove | goto <itemid|name>");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}