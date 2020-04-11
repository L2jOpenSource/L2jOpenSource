package com.l2jfrozen.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @author  Olympic
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public class StackIDFactory extends IdFactory
{
	private static Logger LOGGER = Logger.getLogger(IdFactory.class);
	
	private int curOID;
	private int tempOID;
	
	private final Stack<Integer> freeOIDStack = new Stack<>();
	
	protected StackIDFactory()
	{
		super();
		curOID = FIRST_OID;
		tempOID = FIRST_OID;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final int[] tmp_obj_ids = extractUsedObjectIDTable();
			if (tmp_obj_ids.length > 0)
			{
				curOID = tmp_obj_ids[tmp_obj_ids.length - 1];
			}
			LOGGER.info("Max Id = " + curOID);
			
			int N = tmp_obj_ids.length;
			for (int idx = 0; idx < N; idx++)
			{
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}
			
			curOID++;
			LOGGER.info("IdFactory: Next usable Object ID is: " + curOID);
			initialized = true;
		}
		catch (Exception e)
		{
			LOGGER.error("StackIDFactory.StackIDFactory : ID Factory could not be initialized correctly", e);
		}
	}
	
	private int insertUntil(final int[] tmp_obj_ids, final int idx, final int N, final java.sql.Connection con) throws SQLException
	{
		final int id = tmp_obj_ids[idx];
		if (id == tempOID)
		{
			tempOID++;
			return N;
		}
		// check these IDs not present in DB
		if (Config.BAD_ID_CHECKING)
		{
			for (final String check : ID_CHECKS)
			{
				final PreparedStatement ps = con.prepareStatement(check);
				ps.setInt(1, tempOID);
				// ps.setInt(1, curOID);
				ps.setInt(2, id);
				final ResultSet rs = ps.executeQuery();
				while (rs.next())
				{
					final int badId = rs.getInt(1);
					LOGGER.warn("Bad ID " + badId + " in DB found by: " + check);
					throw new RuntimeException();
				}
				rs.close();
				ps.close();
			}
		}
		
		// int hole = id - curOID;
		int hole = id - tempOID;
		if (hole > N - idx)
		{
			hole = N - idx;
		}
		for (int i = 1; i <= hole; i++)
		{
			// LOGGER.info("Free ID added " + (_tempOID));
			freeOIDStack.push(tempOID);
			tempOID++;
			// curOID++;
		}
		if (hole < N - idx)
		{
			tempOID++;
		}
		return N - hole;
	}
	
	public static IdFactory getInstance()
	{
		return instance;
	}
	
	@Override
	public synchronized int getNextId()
	{
		int id;
		if (!freeOIDStack.empty())
		{
			id = freeOIDStack.pop();
		}
		else
		{
			id = curOID;
			curOID = curOID + 1;
		}
		return id;
	}
	
	/**
	 * return a used Object ID back to the pool
	 * @param id
	 */
	@Override
	public synchronized void releaseId(final int id)
	{
		freeOIDStack.push(id);
	}
	
	@Override
	public int size()
	{
		return FREE_OBJECT_ID_SIZE - curOID + FIRST_OID + freeOIDStack.size();
	}
}