package com.l2jfrozen.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Olympic, luisantonioa
 */
public class CompactionIDFactory extends IdFactory
{
	private static Logger LOGGER = Logger.getLogger(CompactionIDFactory.class);
	private int curOID;
	private final int freeSize;
	
	protected CompactionIDFactory()
	{
		super();
		curOID = FIRST_OID;
		freeSize = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			final int[] tmp_obj_ids = extractUsedObjectIDTable();
			
			int N = tmp_obj_ids.length;
			for (int idx = 0; idx < N; idx++)
			{
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}
			curOID++;
			LOGGER.info("IdFactory: Next usable Object ID is: " + curOID);
			initialized = true;
		}
		catch (Exception e1)
		{
			LOGGER.error("CompactionIDFactory.CompactionIDFactory : ID Factory could not be initialized correctly", e1);
		}
	}
	
	private int insertUntil(final int[] tmp_obj_ids, final int idx, final int N, final java.sql.Connection con) throws SQLException
	{
		int id = tmp_obj_ids[idx];
		if (id == curOID)
		{
			curOID++;
			return N;
		}
		// check these IDs not present in DB
		if (Config.BAD_ID_CHECKING)
		{
			for (final String check : ID_CHECKS)
			{
				final PreparedStatement ps = con.prepareStatement(check);
				ps.setInt(1, curOID);
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
		
		int hole = id - curOID;
		if (hole > N - idx)
		{
			hole = N - idx;
		}
		for (int i = 1; i <= hole; i++)
		{
			id = tmp_obj_ids[N - i];
			LOGGER.info("Compacting DB object ID=" + id + " into " + (curOID));
			for (final String update : ID_UPDATES)
			{
				final PreparedStatement ps = con.prepareStatement(update);
				ps.setInt(1, curOID);
				ps.setInt(2, id);
				ps.execute();
				ps.close();
			}
			curOID++;
		}
		if (hole < N - idx)
		{
			curOID++;
		}
		return N - hole;
	}
	
	@Override
	public synchronized int getNextId()
	{
		/* if (_freeSize == 0) */return curOID++;
		/*
		 * else return freeOIDs[--_freeSize];
		 */
	}
	
	@Override
	public synchronized void releaseId(final int id)
	{
		// dont release ids until we are sure it isnt messing up
		/*
		 * if (freeSize >= freeOIDs.length) { int[] tmp = new int[freeSize + STACK_SIZE_INCREMENT]; System.arraycopy(_freeOIDs, 0, tmp, 0, freeSize); freeOIDs = tmp; } freeOIDs[_freeSize++] = id;
		 */
	}
	
	@Override
	public int size()
	{
		return freeSize + LAST_OID - FIRST_OID;
	}
}
