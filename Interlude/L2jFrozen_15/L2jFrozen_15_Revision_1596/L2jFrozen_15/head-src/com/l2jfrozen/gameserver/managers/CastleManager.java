package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class CastleManager
{
	protected static final Logger LOGGER = Logger.getLogger(CastleManager.class);
	
	private static final String SELECT_CASTLES_ID = "SELECT id FROM castle ORDER by id";
	private static final String DELETE_CASTLE_CIRCLET = "DELETE FROM items WHERE owner_id = ? AND (item_id = ? OR item_id = ?)";
	
	public static final CastleManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private List<Castle> castles;
	
	private static final int castleCirclets[] =
	{
		0,
		6838,
		6835,
		6839,
		6837,
		6840,
		6834,
		6836,
		8182,
		8183
	};
	
	public CastleManager()
	{
		load();
	}
	
	public int findNearestCastlesIndex(L2Object obj)
	{
		int index = getCastleIndex(obj);
		if (index < 0)
		{
			double closestDistance = 99999999;
			double distance;
			Castle castle;
			for (int i = 0; i < getCastles().size(); i++)
			{
				castle = getCastles().get(i);
				
				if (castle == null)
				{
					continue;
				}
				
				distance = castle.getDistance(obj);
				
				if (closestDistance > distance)
				{
					closestDistance = distance;
					index = i;
				}
			}
		}
		return index;
	}
	
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CASTLES_ID);
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				getCastles().add(new Castle(rs.getInt("id")));
			}
		}
		catch (Exception e)
		{
			LOGGER.error("CastleManager.load : Could not select castle ids from castle table", e);
		}
	}
	
	public Castle getCastleById(int castleId)
	{
		for (Castle temp : getCastles())
		{
			if (temp.getCastleId() == castleId)
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public Castle getCastleByOwner(L2Clan clan)
	{
		if (clan == null)
		{
			return null;
		}
		
		for (Castle temp : getCastles())
		{
			if (temp != null && temp.getOwnerId() == clan.getClanId())
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public Castle getCastle(String name)
	{
		if (name == null || name.isEmpty())
		{
			return null;
		}
		
		for (Castle temp : getCastles())
		{
			if (temp.getName().equalsIgnoreCase(name.trim()))
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public Castle getCastle(int x, int y, int z)
	{
		for (Castle temp : getCastles())
		{
			if (temp.checkIfInZone(x, y, z))
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public Castle getCastle(L2Object activeObject)
	{
		if (activeObject == null)
		{
			return null;
		}
		
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public int getCastleIndex(int castleId)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getCastleId() == castleId)
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getCastleIndex(L2Object activeObject)
	{
		return getCastleIndex(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public int getCastleIndex(int x, int y, int z)
	{
		Castle castle;
		for (int i = 0; i < getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZone(x, y, z))
			{
				return i;
			}
		}
		return -1;
	}
	
	public List<Castle> getCastles()
	{
		if (castles == null)
		{
			castles = new ArrayList<>();
		}
		return castles;
	}
	
	public void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		
		switch (sealStrifeOwner)
		{
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			default: // no owner
				maxTax = 15;
				break;
		}
		
		for (Castle castle : castles)
		{
			if (castle.getTaxPercent() > maxTax)
			{
				castle.setTaxPercent(maxTax);
			}
		}
	}
	
	int castleId = 1; // from this castle
	
	public int getCirclet()
	{
		return getCircletByCastleId(castleId);
	}
	
	public int getCircletByCastleId(final int castleId)
	{
		if (castleId > 0 && castleId < 10)
		{
			return castleCirclets[castleId];
		}
		
		return 0;
	}
	
	// remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for (L2ClanMember member : clan.getMembers())
		{
			removeCirclet(member, castleId);
		}
	}
	
	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if (member == null)
		{
			return;
		}
		
		L2PcInstance player = member.getPlayerInstance();
		
		if (player == null)
		{
			return;
		}
		
		int circletId = getCircletByCastleId(castleId);
		
		if (circletId == 0)
		{
			return;
		}
		
		if (player.isOnline())
		{
			if (player.isClanLeader())
			{
				L2ItemInstance crown = player.getInventory().getItemByItemId(6841);
				
				if (crown != null)
				{
					if (crown.isEquipped())
					{
						player.getInventory().unEquipItemInSlotAndRecord(crown.getEquipSlot());
					}
					
					player.destroyItemByItemId("CastleCrownRemoval", 6841, 1, player, true);
				}
			}
			
			L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
			if (circlet != null)
			{
				if (circlet.isEquipped())
				{
					player.getInventory().unEquipItemInSlotAndRecord(circlet.getEquipSlot());
				}
				
				player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
			}
		}
		else
		{
			// offline-player circlet removal
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(DELETE_CASTLE_CIRCLET))
			{
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, 6841); // The Lord's Crown
				statement.setInt(3, circletId);
				statement.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.error("CastleManager.removeCirclet : Failed to remove castle circlets offline for player " + member.getName(), e);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final CastleManager instance = new CastleManager();
	}
}
