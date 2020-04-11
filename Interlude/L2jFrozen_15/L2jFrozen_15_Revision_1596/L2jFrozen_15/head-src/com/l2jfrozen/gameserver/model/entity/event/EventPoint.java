package com.l2jfrozen.gameserver.model.entity.event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.CloseUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class EventPoint
{
	private final L2PcInstance activeChar;
	private Integer eventPoints = 0;
	
	public EventPoint(final L2PcInstance player)
	{
		activeChar = player;
		loadFromDB();
	}
	
	public L2PcInstance getActiveChar()
	{
		return activeChar;
	}
	
	public void savePoints()
	{
		saveToDb();
	}
	
	private void loadFromDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement st = con.prepareStatement("Select * From char_points where charId = ?");
			st.setInt(1, getActiveChar().getObjectId());
			final ResultSet rst = st.executeQuery();
			
			while (rst.next())
			{
				eventPoints = rst.getInt("points");
			}
			
			rst.close();
			st.close();
		}
		catch (final Exception ex)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				ex.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	private void saveToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement st = con.prepareStatement("Update char_points Set points = ? Where charId = ?");
			st.setInt(1, eventPoints);
			st.setInt(2, getActiveChar().getObjectId());
			st.execute();
			st.close();
		}
		catch (final Exception ex)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				ex.printStackTrace();
			}
		}
		finally
		{
			CloseUtil.close(con);
			con = null;
		}
	}
	
	public Integer getPoints()
	{
		return eventPoints;
	}
	
	public void setPoints(final Integer points)
	{
		eventPoints = points;
	}
	
	public void addPoints(final Integer points)
	{
		eventPoints += points;
	}
	
	public void removePoints(final Integer points)
	{
		// Don't know , do the calc or return. it's up to you
		if (eventPoints - points < 0)
		{
			return;
		}
		
		eventPoints -= points;
	}
	
	public boolean canSpend(final Integer value)
	{
		return eventPoints - value >= 0;
	}
	
}
