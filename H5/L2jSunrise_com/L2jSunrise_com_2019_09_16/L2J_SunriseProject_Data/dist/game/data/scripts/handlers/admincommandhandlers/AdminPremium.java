/*
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

package handlers.admincommandhandlers;

import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.premiumEngine.PremiumHandler;

public class AdminPremium implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_premium_menu",
		"admin_premium_add1",
		"admin_premium_add2",
		"admin_premium_add3",
		"admin_clean_premium"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_premium_menu"))
		{
			AdminHtml.showAdminHtml(activeChar, "premium_menu.htm");
		}
		else if (command.startsWith("admin_premium_add1"))
		{
			try
			{
				String val = command.substring(19);
				PremiumHandler.addPremiumServices(1, val);
				activeChar.sendMessage("Added premium status for 1 month, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Invalid account.");
			}
		}
		else if (command.startsWith("admin_premium_add2"))
		{
			try
			{
				String val = command.substring(19);
				PremiumHandler.addPremiumServices(2, val);
				activeChar.sendMessage("Added premium status for 2 months, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Invalid account.");
			}
		}
		else if (command.startsWith("admin_premium_add3"))
		{
			try
			{
				String val = command.substring(19);
				PremiumHandler.addPremiumServices(3, val);
				activeChar.sendMessage("Added premium status for 3 months, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Invalid account.");
			}
		}
		else if (command.startsWith("admin_clean_premium"))
		{
			try
			{
				String val = command.substring(20);
				PremiumHandler.removePremiumServices(val);
				activeChar.sendMessage("Premium successfully cleaned, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Invalid account.");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}