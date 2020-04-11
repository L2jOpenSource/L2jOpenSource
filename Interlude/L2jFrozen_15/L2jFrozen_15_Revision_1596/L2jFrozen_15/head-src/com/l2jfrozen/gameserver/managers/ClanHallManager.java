package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * @author Steuf
 */
public class ClanHallManager
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallManager.class);
	
	private static final String SELECT_CLAN_HALLS = "SELECT id,`name`,ownerId,lease,`desc`,location,paidUntil,Grade,paid FROM clanhall ORDER BY id";
	
	private static final Map<Integer, ClanHall> clanHall = new HashMap<>();
	private static final Map<Integer, ClanHall> freeClanHall = new HashMap<>();
	private static boolean loaded = false;
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public static boolean loaded()
	{
		return loaded;
	}
	
	public ClanHallManager()
	{
		load();
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_HALLS);
			ResultSet rs = statement.executeQuery())
		{
			int id, ownerId, lease, grade;
			String Name, Desc, Location;
			long paidUntil;
			boolean paid = false;
			
			while (rs.next())
			{
				id = rs.getInt("id");
				Name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				Desc = rs.getString("desc");
				Location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");
				
				final ClanHall ch = new ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid);
				if (ownerId == 0)
				{
					freeClanHall.put(id, ch);
				}
				else
				{
					final L2Clan clan = ClanTable.getInstance().getClan(ownerId);
					if (clan != null)
					{
						clanHall.put(id, ch);
						clan.setHasHideout(id);
					}
					else
					{
						freeClanHall.put(id, ch);
						ch.free();
						AuctionManager.getInstance().initNPC(id);
					}
				}
			}
			
			LOGGER.info("Loaded: " + getClanHalls().size() + " busy clan halls");
			LOGGER.info("Loaded: " + getFreeClanHalls().size() + " free clan halls");
			loaded = true;
		}
		catch (Exception e)
		{
			LOGGER.error("ClanHallManager.load : Could not select clan halls data from clanhall table", e);
		}
	}
	
	/**
	 * Get Map with all FreeClanHalls
	 * @return
	 */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return freeClanHall;
	}
	
	/**
	 * Get Map with all ClanHalls
	 * @return
	 */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return clanHall;
	}
	
	/**
	 * Check is free ClanHall
	 * @param  chId
	 * @return
	 */
	public final boolean isFree(final int chId)
	{
		return freeClanHall.containsKey(chId);
	}
	
	/**
	 * Free a ClanHall
	 * @param chId
	 */
	public final synchronized void setFree(final int chId)
	{
		freeClanHall.put(chId, clanHall.get(chId));
		ClanTable.getInstance().getClan(freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		freeClanHall.get(chId).free();
		clanHall.remove(chId);
	}
	
	/**
	 * Set ClanHallOwner
	 * @param chId
	 * @param clan
	 */
	public final synchronized void setOwner(final int chId, final L2Clan clan)
	{
		if (!clanHall.containsKey(chId))
		{
			clanHall.put(chId, freeClanHall.get(chId));
			freeClanHall.remove(chId);
		}
		else
		{
			clanHall.get(chId).free();
		}
		
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		clanHall.get(chId).setOwner(clan);
	}
	
	/**
	 * Get Clan Hall by Id
	 * @param  clanHallId
	 * @return
	 */
	public final ClanHall getClanHallById(final int clanHallId)
	{
		if (clanHall.containsKey(clanHallId))
		{
			return clanHall.get(clanHallId);
		}
		if (freeClanHall.containsKey(clanHallId))
		{
			return freeClanHall.get(clanHallId);
		}
		
		return null;
	}
	
	public final ClanHall getNearbyClanHall(final int x, final int y, final int maxDist)
	{
		
		for (final Integer ch_id : clanHall.keySet())
		{
			
			final ClanHall ch = clanHall.get(ch_id);
			
			if (ch == null)
			{
				LOGGER.warn("ATTENTION: Clah Hall " + ch_id + " is not defined.");
				clanHall.remove(ch_id);
				continue;
			}
			
			if (ch.getZone().getDistanceToZone(x, y) < maxDist)
			{
				return ch;
			}
			
		}
		
		for (final Integer ch_id : freeClanHall.keySet())
		{
			
			final ClanHall ch = freeClanHall.get(ch_id);
			
			if (ch == null)
			{
				LOGGER.warn("ATTENTION: Clah Hall " + ch_id + " is not defined.");
				freeClanHall.remove(ch_id);
				continue;
			}
			
			if (ch.getZone().getDistanceToZone(x, y) < maxDist)
			{
				return ch;
			}
			
		}
		
		return null;
	}
	
	/**
	 * Get Clan Hall by Owner
	 * @param  clan
	 * @return
	 */
	public final ClanHall getClanHallByOwner(final L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		
		for (final Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
		{
			
			if (ch == null || ch.getValue() == null)
			{
				return null;
			}
			
			if (clan.getClanId() == ch.getValue().getOwnerId())
			{
				return ch.getValue();
			}
		}
		
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallManager instance = new ClanHallManager();
	}
}
