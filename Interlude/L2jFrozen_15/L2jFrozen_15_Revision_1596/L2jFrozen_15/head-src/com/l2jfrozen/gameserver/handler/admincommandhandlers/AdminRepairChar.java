package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.1.2.6.2.3 $ $Date: 2005/04/11 10:05:59 $
 */

public class AdminRepairChar implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminRepairChar.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_restore",
		"admin_repair"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		handleRepair(command);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRepair(final String command)
	{
		String[] parts = command.split(" ");
		
		if (parts.length != 2)
		{
			return;
		}
		
		String cmd = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";
		Connection connection = null;
		
		try
		{
			connection = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(cmd);
			statement.setString(1, parts[1]);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			statement = connection.prepareStatement("SELECT obj_id FROM characters where char_name=?");
			statement.setString(1, parts[1]);
			ResultSet rset = statement.executeQuery();
			
			int objId = 0;
			
			if (rset.next())
			{
				objId = rset.getInt(1);
			}
			
			DatabaseUtils.close(rset);
			DatabaseUtils.close(statement);
			rset = null;
			statement = null;
			
			if (objId == 0)
			{
				CloseUtil.close(connection);
				return;
			}
			
			statement = connection.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
			
			statement = connection.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("Could not repair char:", e);
		}
		finally
		{
			CloseUtil.close(connection);
			connection = null;
			cmd = null;
			parts = null;
		}
	}
}
