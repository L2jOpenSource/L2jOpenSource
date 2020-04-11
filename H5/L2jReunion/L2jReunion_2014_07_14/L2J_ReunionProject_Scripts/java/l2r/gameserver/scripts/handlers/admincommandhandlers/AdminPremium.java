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

package l2r.gameserver.scripts.handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import l2r.L2DatabaseFactory;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE characters_premium SET premium_service=?,enddate=? WHERE account_name=?";
	private static final Logger _log = LoggerFactory.getLogger(AdminPremium.class);
	
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
				addPremiumServices(1, val);
				activeChar.sendMessage("Added premium status for 1 month, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add2"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(2, val);
				activeChar.sendMessage("Added premium status for 2 months, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_premium_add3"))
		{
			try
			{
				String val = command.substring(19);
				addPremiumServices(3, val);
				activeChar.sendMessage("Added premium status for 3 months, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		else if (command.startsWith("admin_clean_premium"))
		{
			try
			{
				String val = command.substring(20);
				cleanPremiumServices(val);
				activeChar.sendMessage("Premium successfully cleaned, account: " + val + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Err");
			}
		}
		return true;
	}
	
	private void addPremiumServices(int Months, String AccName)
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(System.currentTimeMillis());
		finishtime.set(13, 0);
		finishtime.add(2, Months);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, AccName);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService: Could not increase data.");
		}
	}
	
	private void cleanPremiumServices(String AccName)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 0);
			statement.setLong(2, 0);
			statement.setString(3, AccName);
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService: Could not clean data.");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}