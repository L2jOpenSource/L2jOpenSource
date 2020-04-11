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
package l2r.gameserver.network.clientpackets;

import java.util.Calendar;
import java.util.Date;

import l2r.Config;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SiegeInfo;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Broadcast;

/**
 * @author UnAfraid
 */
public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
	private int _castleId;
	private long _time;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_time = readD();
		_time *= 1000;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if ((activeChar == null) || (castle == null))
		{
			_log.warn(getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId);
			return;
		}
		if ((castle.getOwnerId() > 0) && (castle.getOwnerId() != activeChar.getClanId()))
		{
			_log.warn(getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date of not his own castle!");
			return;
		}
		else if (!activeChar.isClanLeader())
		{
			_log.warn(getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date but is not clan leader!");
			return;
		}
		else if (!castle.getIsTimeRegistrationOver() && !castle.getIsTimeRegistrationOver())
		{
			if (isSiegeTimeValid(castle.getSiegeDate().getTimeInMillis(), _time))
			{
				castle.getSiegeDate().setTimeInMillis(_time);
				castle.setIsTimeRegistrationOver(true);
				castle.getSiege().saveSiegeDate();
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME);
				msg.addCastleId(_castleId);
				Broadcast.toAllOnlinePlayers(msg);
				activeChar.sendPacket(new SiegeInfo(castle));
			}
			else
			{
				_log.warn(getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to an invalid time (" + new Date(_time) + " !");
			}
		}
		else
		{
			_log.warn(getType() + ": activeChar: " + activeChar + " castle: " + castle + " castleId: " + _castleId + " is trying to change siege date but currently not possible!");
		}
	}
	
	private static boolean isSiegeTimeValid(long siegeDate, long choosenDate)
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(siegeDate);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(choosenDate);
		
		for (int hour : Config.SIEGE_HOUR_LIST)
		{
			cal1.set(Calendar.HOUR_OF_DAY, hour);
			if (isEqual(cal1, cal2, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND))
			{
				return true;
			}
		}
		return false;
	}
	
	private static boolean isEqual(Calendar cal1, Calendar cal2, int... fields)
	{
		for (int field : fields)
		{
			if (cal1.get(field) != cal2.get(field))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
