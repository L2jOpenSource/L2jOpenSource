package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.util.database.L2DatabaseFactory;

public class ShortCuts
{
	private static Logger LOGGER = Logger.getLogger(ShortCuts.class);
	private static final String SELECT_CHARACTER_SHORTCUTS = "SELECT char_obj_id, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	private static final String REPLACE_CHARACTER_SHORTCUTS = "REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)";
	private static final String DELETE_CHARACTER_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?";
	
	private L2PcInstance owner;
	private Map<Integer, L2ShortCut> shortCuts = new TreeMap<>();
	
	public ShortCuts(L2PcInstance owner)
	{
		this.owner = owner;
	}
	
	public List<L2ShortCut> getAllShortCuts()
	{
		return shortCuts.values().stream().collect(Collectors.toList());
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		L2ShortCut sc = shortCuts.get(slot + page * 12);
		
		// verify shortcut
		if (sc != null && sc.getType() == L2ShortCut.TYPE_ITEM)
		{
			if (owner.getInventory().getItemByObjectId(sc.getId()) == null)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		return sc;
	}
	
	public synchronized void registerShortCut(L2ShortCut shortcut)
	{
		L2ShortCut oldShortCut = shortCuts.put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);
		registerShortCutInDb(shortcut, oldShortCut);
	}
	
	private void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REPLACE_CHARACTER_SHORTCUTS))
		{
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, owner.getClassIndex());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ShortCuts.registerShortCutInDb : Could not insert character shortcut on database", e);
		}
	}
	
	public synchronized void deleteShortCut(int slot, int page)
	{
		L2ShortCut old = shortCuts.remove(slot + page * 12);
		
		if (old == null || owner == null)
		{
			return;
		}
		
		deleteShortCutFromDb(old);
		
		if (old.getType() == L2ShortCut.TYPE_ITEM)
		{
			L2ItemInstance item = owner.getInventory().getItemByObjectId(old.getId());
			
			if (item != null && item.getItemType() == L2EtcItemType.SHOT)
			{
				owner.removeAutoSoulShot(item.getItemId());
				owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
			}
		}
		
		owner.sendPacket(new ShortCutInit(owner));
		
		for (int shotId : owner.getAutoSoulShot().values())
		{
			owner.sendPacket(new ExAutoSoulShot(shotId, 1));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		L2ShortCut toRemove = null;
		
		for (L2ShortCut shortcut : shortCuts.values())
		{
			if (shortcut.getType() == L2ShortCut.TYPE_ITEM && shortcut.getId() == objectId)
			{
				toRemove = shortcut;
				break;
			}
		}
		
		if (toRemove != null)
		{
			deleteShortCut(toRemove.getSlot(), toRemove.getPage());
		}
	}
	
	private void deleteShortCutFromDb(L2ShortCut shortcut)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_SHORTCUTS))
		{
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, owner.getClassIndex());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("ShortCuts.deleteShortCutFromDb : Could not delete character shortcut", e);
		}
	}
	
	public void restore()
	{
		shortCuts.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_SHORTCUTS))
		{
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, owner.getClassIndex());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int slot = rset.getInt("slot");
					int page = rset.getInt("page");
					int type = rset.getInt("type");
					int id = rset.getInt("shortcut_id");
					int level = rset.getInt("level");
					
					L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);
					shortCuts.put(slot + page * 12, sc);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("ShortCuts.restore : Could not restore character shortcuts", e);
		}
		
		// verify shortcuts
		for (L2ShortCut sc : getAllShortCuts())
		{
			if (sc.getType() == L2ShortCut.TYPE_ITEM)
			{
				if (owner.getInventory().getItemByObjectId(sc.getId()) == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
			}
		}
	}
}
