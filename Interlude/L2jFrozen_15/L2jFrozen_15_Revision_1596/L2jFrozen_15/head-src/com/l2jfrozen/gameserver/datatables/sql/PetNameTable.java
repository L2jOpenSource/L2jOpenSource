package com.l2jfrozen.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class PetNameTable
{
	private final static Logger LOGGER = Logger.getLogger(PetNameTable.class);
	private static final String SELECT_PET_NAME = "SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)";
	
	private static PetNameTable instance;
	
	public static PetNameTable getInstance()
	{
		if (instance == null)
		{
			instance = new PetNameTable();
		}
		
		return instance;
	}
	
	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_PET_NAME))
		{
			statement.setString(1, name);
			
			String cond = "";
			for (final int it : L2PetDataTable.getPetItemsAsNpc(petNpcId))
			{
				if (cond != "")
				{
					cond += ", ";
				}
				
				cond += it;
			}
			statement.setString(2, cond);
			
			try (ResultSet rset = statement.executeQuery())
			{
				result = rset.next();
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("PetNameTable.doesPetNameExist : Could not check existing petname", e);
		}
		return result;
	}
	
	public boolean isValidPetName(final String name)
	{
		boolean result = true;
		
		if (!isAlphaNumeric(name))
		{
			return result;
		}
		
		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
		}
		catch (final PatternSyntaxException e) // case of illegal pattern
		{
			LOGGER.warn("ERROR : Pet name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		
		final Matcher regexp = pattern.matcher(name);
		
		if (!regexp.matches())
		{
			result = false;
		}
		
		return result;
	}
	
	private boolean isAlphaNumeric(final String text)
	{
		boolean result = true;
		final char[] chars = text.toCharArray();
		for (final char aChar : chars)
		{
			if (!Character.isLetterOrDigit(aChar))
			{
				result = false;
				break;
			}
		}
		
		return result;
	}
}
