package com.l2jfrozen.gameserver.model.entity.siege;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author MHard
 */
public abstract class ClanHallSiege
{
	private static Logger LOGGER = Logger.getLogger(ClanHallSiege.class);
	private static final String SELECT_SIEGE_DATA = "SELECT siege_data FROM clanhall_siege WHERE id=?";
	private static final String UPDATE_CLAN_HALL_SIEGE_DATA = "UPDATE clanhall_siege SET siege_data=? WHERE id = ?";
	
	private Calendar siegeDate;
	public Calendar siegeEndDate;
	private boolean isInProgress = false;
	
	public long restoreSiegeDate(int ClanHallId)
	{
		long res = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_SIEGE_DATA))
		{
			statement.setInt(1, ClanHallId);
			
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					res = rs.getLong("siege_data");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHallSiege.restoreSiegeDate : Can't get clanhall siege date", e);
		}
		return res;
	}
	
	public void setNewSiegeDate(long siegeDate, int ClanHallId, int hour)
	{
		Calendar tmpDate = Calendar.getInstance();
		if (siegeDate <= System.currentTimeMillis())
		{
			tmpDate.setTimeInMillis(System.currentTimeMillis());
			tmpDate.add(Calendar.DAY_OF_MONTH, 3);
			tmpDate.set(Calendar.DAY_OF_WEEK, 6);
			tmpDate.set(Calendar.HOUR_OF_DAY, hour);
			tmpDate.set(Calendar.MINUTE, 0);
			tmpDate.set(Calendar.SECOND, 0);
			
			setSiegeDate(tmpDate);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_CLAN_HALL_SIEGE_DATA))
			{
				statement.setLong(1, getSiegeDate().getTimeInMillis());
				statement.setInt(2, ClanHallId);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("Exception: can't save clanhall siege date: ");
			}
		}
	}
	
	public final Calendar getSiegeDate()
	{
		return siegeDate;
	}
	
	public final void setSiegeDate(final Calendar par)
	{
		siegeDate = par;
	}
	
	public final boolean getIsInProgress()
	{
		return isInProgress;
	}
	
	public final void setIsInProgress(final boolean par)
	{
		isInProgress = par;
	}
}
