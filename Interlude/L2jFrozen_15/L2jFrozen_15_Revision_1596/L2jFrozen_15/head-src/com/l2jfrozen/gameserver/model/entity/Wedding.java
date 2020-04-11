package com.l2jfrozen.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author evill33t
 */
public class Wedding
{
	private static final Logger LOGGER = Logger.getLogger(Wedding.class);
	private static final String SELECT_WEDDING_INFO_BY_ID = "SELECT id, player1Id, player2Id, married, affianceDate, weddingDate, coupleType FROM mods_wedding WHERE id=?";
	private static final String INSERT_ENGAGE_COUPLE_DATA = "INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_COUPLE_WEDDING = "UPDATE mods_wedding SET married= ?,weddingDate=?, coupleType=? WHERE id=?";
	private static final String DELETE_COUPLE_WEDDING = "DELETE FROM mods_wedding WHERE id=?";
	
	private int id = 0;
	private int player1Id = 0;
	private int player2Id = 0;
	private boolean married = false;
	private Calendar affiancedDate;
	private Calendar weddingDate;
	private int weddingType = 0;
	
	public Wedding(int coupleId)
	{
		id = coupleId;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_WEDDING_INFO_BY_ID))
		{
			statement.setInt(1, id);
			
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					player1Id = rs.getInt("player1Id");
					player2Id = rs.getInt("player2Id");
					married = rs.getBoolean("married");
					
					affiancedDate = Calendar.getInstance();
					affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));
					
					weddingDate = Calendar.getInstance();
					weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
					
					weddingType = rs.getInt("coupleType");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Wedding.Wedding : Something wrong while getting couple data from mods_wedding table. ", e);
		}
	}
	
	public Wedding(L2PcInstance player1, L2PcInstance player2)
	{
		int tempPlayer1Id = player1.getObjectId();
		int tempPlayer2Id = player2.getObjectId();
		
		player1Id = tempPlayer1Id;
		player2Id = tempPlayer2Id;
		
		affiancedDate = Calendar.getInstance();
		affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		weddingDate = Calendar.getInstance();
		weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_ENGAGE_COUPLE_DATA))
		{
			id = IdFactory.getInstance().getNextId();
			// Couple just engaged
			married = false;
			
			statement.setInt(1, id);
			statement.setInt(2, player1Id);
			statement.setInt(3, player2Id);
			statement.setBoolean(4, married);
			statement.setLong(5, affiancedDate.getTimeInMillis());
			statement.setLong(6, weddingDate.getTimeInMillis());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Wedding.Wedding(param,param2) : Can not insert engage couple data into mods_wedding table. ", e);
		}
	}
	
	public void marry(int type)
	{
		weddingType = type;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_COUPLE_WEDDING))
		{
			married = true;
			statement.setBoolean(1, married);
			weddingDate = Calendar.getInstance();
			statement.setLong(2, weddingDate.getTimeInMillis());
			statement.setInt(3, weddingType);
			statement.setInt(4, id);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Wedding.marry : Can not update couple wedding data into mods_wedding table. ", e);
		}
	}
	
	public void divorce()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_COUPLE_WEDDING))
		{
			statement.setInt(1, id);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("Wedding.divorce : Can not delete couple wedding data from mods_wedding table. ", e);
		}
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getPlayer1Id()
	{
		return player1Id;
	}
	
	public int getPlayer2Id()
	{
		return player2Id;
	}
	
	public boolean getMaried()
	{
		return married;
	}
	
	public Calendar getAffiancedDate()
	{
		return affiancedDate;
	}
	
	public Calendar getWeddingDate()
	{
		return weddingDate;
	}
	
	public int getType()
	{
		return weddingType;
	}
}
