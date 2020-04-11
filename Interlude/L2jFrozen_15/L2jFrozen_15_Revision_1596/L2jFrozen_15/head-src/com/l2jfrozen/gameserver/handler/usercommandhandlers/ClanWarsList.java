
package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.DatabaseUtils;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Support for /clanwarlist command
 * @author Tempy
 */
public class ClanWarsList implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		88,
		89,
		90
	};
	
	@Override
	public boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
		{
			return false;
		}
		
		L2Clan clan = activeChar.getClan();
		
		if (clan == null)
		{
			activeChar.sendMessage("You are not in a clan.");
			return false;
		}
		
		SystemMessage sm;
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			if (id == 88)
			{
				// Attack List
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON));
				statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan1=? and clan_id=clan2 and clan2 not in (select clan1 from clan_wars where clan2=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			else if (id == 89)
			{
				// Under Attack List
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU));
				statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan2=? and clan_id=clan1 and clan1 not in (select clan2 from clan_wars where clan1=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			else
			// ID = 90
			{
				// War List
				activeChar.sendPacket(new SystemMessage(SystemMessageId.WAR_LIST));
				statement = con.prepareStatement("select clan_name,clan_id,ally_id,ally_name from clan_data,clan_wars where clan1=? and clan_id=clan2 and clan2 in (select clan1 from clan_wars where clan2=?)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, clan.getClanId());
			}
			
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				final String clanName = rset.getString("clan_name");
				final int ally_id = rset.getInt("ally_id");
				
				if (ally_id > 0)
				{
					// Target With Ally
					sm = new SystemMessage(SystemMessageId.S1_S2_ALLIANCE);
					sm.addString(clanName);
					sm.addString(rset.getString("ally_name"));
				}
				else
				{
					// Target Without Ally
					sm = new SystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS);
					sm.addString(clanName);
				}
				
				activeChar.sendPacket(sm);
			}
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
			
			DatabaseUtils.close(rset);
			rset = null;
			DatabaseUtils.close(statement);
			statement = null;
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
		
		sm = null;
		clan = null;
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
