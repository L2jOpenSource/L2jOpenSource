package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = CastleID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) Siege Time Select Related?
 * @author KenM
 */
public class SiegeInfo extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(SiegeInfo.class);
	private final Castle castle;
	
	public SiegeInfo(final Castle castle)
	{
		this.castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		writeC(0xc9);
		writeD(castle.getCastleId());
		writeD(castle.getOwnerId() == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
		writeD(castle.getOwnerId());
		if (castle.getOwnerId() > 0)
		{
			final L2Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
			if (owner != null)
			{
				writeS(owner.getName()); // Clan Name
				writeS(owner.getLeaderName()); // Clan Leader Name
				writeD(owner.getAllyId()); // Ally ID
				writeS(owner.getAllyName()); // Ally Name
			}
			else
			{
				LOGGER.warn("Null owner for castle: " + castle.getName());
			}
		}
		else
		{
			writeS("NPC"); // Clan Name
			writeS(""); // Clan Leader Name
			writeD(0); // Ally ID
			writeS(""); // Ally Name
		}
		
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		writeD(0x00); // number of choices?
	}
	
	@Override
	public String getType()
	{
		return "[S] c9 SiegeInfo";
	}
	
}
