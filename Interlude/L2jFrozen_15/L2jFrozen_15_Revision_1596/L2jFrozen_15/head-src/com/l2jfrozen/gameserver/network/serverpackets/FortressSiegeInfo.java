package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = FortID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) Siege Time Select Related?
 * @author programmos
 */
public class FortressSiegeInfo extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(FortressSiegeInfo.class);
	private final Fort fortress;
	
	public FortressSiegeInfo(final Fort fort)
	{
		fortress = fort;
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
		writeD(fortress.getFortId());
		writeD(fortress.getOwnerId() == activeChar.getClanId() && activeChar.isClanLeader() ? 0x01 : 0x00);
		writeD(fortress.getOwnerId());
		if (fortress.getOwnerId() > 0)
		{
			final L2Clan owner = ClanTable.getInstance().getClan(fortress.getOwnerId());
			if (owner != null)
			{
				writeS(owner.getName()); // Clan Name
				writeS(owner.getLeaderName()); // Clan Leader Name
				writeD(owner.getAllyId()); // Ally ID
				writeS(owner.getAllyName()); // Ally Name
			}
			else
			{
				LOGGER.warn("Null owner for fort: " + fortress.getName());
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
		writeD((int) (fortress.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		writeD(0x00); // number of choices?
	}
	
	@Override
	public String getType()
	{
		return "[S] c9 SiegeInfo";
	}
	
}
