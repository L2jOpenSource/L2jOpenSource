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

public final class RequestSetPledgeCrest extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestSetPledgeCrest.class);
	private static final String UPDATE_CLAN_CREST_BY_CLAN_ID = "UPDATE clan_data SET crest_id = ? WHERE clan_id = ?";
	
	private int length;
	private byte[] data;
	
	@Override
	protected void readImpl()
	{
		length = readD();
		if (length < 0 || length > 256)
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
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS));
			return;
		}
		
		if (length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (length > 256)
		{
			activeChar.sendMessage("The clan crest file size was too big (max 256 bytes).");
			return;
		}
		
		if (length == 0 || data.length == 0)
		{
			CrestCache.getInstance().removePledgeCrest(clan.getCrestId());
			
			clan.setHasCrest(false);
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED));
			
			for (final L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
			
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if (clan.getLevel() < 3)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST));
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			
			final int newId = IdFactory.getInstance().getNextId();
			
			if (clan.hasCrest())
			{
				crestCache.removePledgeCrest(newId);
			}
			
			if (!crestCache.savePledgeCrest(newId, data))
			{
				LOGGER.warn("Error loading crest of clan:" + clan.getName());
				return;
			}
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_CREST_BY_CLAN_ID))
			{
				statement.setInt(1, newId);
				statement.setInt(2, clan.getClanId());
				statement.executeUpdate();
			}
			catch (SQLException e)
			{
				LOGGER.error("RequestSetPledgeCrest.runImpl : Could not update the crest id", e);
			}
			
			clan.setCrestId(newId);
			clan.setHasCrest(true);
			
			for (L2PcInstance member : clan.getOnlineMembers(""))
			{
				member.broadcastUserInfo();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 53 RequestSetPledgeCrest";
	}
}
