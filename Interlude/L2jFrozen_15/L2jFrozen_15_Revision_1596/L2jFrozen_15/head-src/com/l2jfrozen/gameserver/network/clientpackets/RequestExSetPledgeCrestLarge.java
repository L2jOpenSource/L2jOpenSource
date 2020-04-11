package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.cache.CrestCache;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Format : chdb c (id) 0xD0 h (subid) 0x11 d data size b raw data (picture i think ;) )
 * @author -Wooden-
 */
public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestExSetPledgeCrestLarge.class);
	private static final String UPDATE_CLAN_CREST_LARGE = "UPDATE clan_data SET crest_large_id=? WHERE clan_id=?";
	
	private int size;
	private byte[] data;
	
	@Override
	protected void readImpl()
	{
		size = readD();
		
		if (size > 2176)
		{
			return;
		}
		
		if (size > 0) // client CAN send a RequestExSetPledgeCrestLarge with the size set to 0 then format is just chd
		{
			data = new byte[size];
			readB(data);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		if (data == null)
		{
			CrestCache.getInstance().removePledgeCrestLarge(clan.getCrestId());
			
			clan.setHasCrestLarge(false);
			activeChar.sendMessage("The insignia has been removed.");
			
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
			
			return;
		}
		
		if (size > 2176)
		{
			activeChar.sendMessage("The insignia file size is greater than 2176 bytes.");
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (clan.getCastleId() == 0 && clan.getHasHideout() == 0)
			{
				activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items"); // there is a system message for that but didnt found the id
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			
			final int newId = IdFactory.getInstance().getNextId();
			
			if (!crestCache.savePledgeCrestLarge(newId, data))
			{
				LOGGER.warn("Error loading large crest of clan:" + clan.getName());
				return;
			}
			
			if (clan.hasCrestLarge())
			{
				crestCache.removePledgeCrestLarge(clan.getCrestLargeId());
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_CREST_LARGE))
			{
				statement.setInt(1, newId);
				statement.setInt(2, clan.getClanId());
				statement.executeUpdate();
			}
			catch (final SQLException e)
			{
				LOGGER.error("RequestExSetPledgeCrestLarge.readImpl : Could not update clan crest large", e);
			}
			
			clan.setCrestLargeId(newId);
			clan.setHasCrestLarge(true);
			
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED));
			
			for (L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:11 RequestExSetPledgeCrestLarge";
	}
	
}
