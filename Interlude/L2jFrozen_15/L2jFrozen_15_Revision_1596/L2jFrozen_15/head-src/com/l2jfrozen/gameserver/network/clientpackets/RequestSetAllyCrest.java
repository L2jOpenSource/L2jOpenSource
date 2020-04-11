package com.l2jfrozen.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.cache.CrestCache;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public final class RequestSetAllyCrest extends L2GameClientPacket
{
	public static Logger LOGGER = Logger.getLogger(RequestSetAllyCrest.class);
	private static final String UPDATE_CLAN_ALLY_CREST = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id=?";
	
	private int length;
	private byte[] data;
	
	@Override
	protected void readImpl()
	{
		length = readD();
		if (length < 0 || length > 192)
		{
			return;
		}
		
		data = new byte[length];
		readB(data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (length > 192)
		{
			activeChar.sendMessage("The crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (activeChar.getAllyId() != 0)
		{
			final L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());
			
			if (activeChar.getClanId() != leaderclan.getClanId() || !activeChar.isClanLeader())
			{
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			
			final int newId = IdFactory.getInstance().getNextId();
			
			if (!crestCache.saveAllyCrest(newId, data))
			{
				LOGGER.warn("Error loading crest of ally:" + leaderclan.getAllyName());
				return;
			}
			
			if (leaderclan.getAllyCrestId() != 0)
			{
				crestCache.removeAllyCrest(leaderclan.getAllyCrestId());
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_ALLY_CREST))
			{
				statement.setInt(1, newId);
				statement.setInt(2, leaderclan.getAllyId());
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.error("RequestSetAllyCrest.runImpl : Could not update the ally crest id", e);
			}
			
			for (final L2Clan clan : ClanTable.getInstance().getClans())
			{
				if (clan.getAllyId() == activeChar.getAllyId())
				{
					clan.setAllyCrestId(newId);
					for (final L2PcInstance member : clan.getOnlineMembers(""))
					{
						member.broadcastUserInfo();
					}
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 87 RequestSetAllyCrest";
	}
}
