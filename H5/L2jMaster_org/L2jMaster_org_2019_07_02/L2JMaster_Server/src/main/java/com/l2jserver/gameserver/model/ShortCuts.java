/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.enums.ShortcutType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.interfaces.IRestorable;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.EtcItemType;
import com.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jserver.gameserver.network.serverpackets.ShortCutInit;
import com.l2jserver.gameserver.network.serverpackets.ShortCutRegister;

public class ShortCuts implements IRestorable
{
	private static Logger _log = Logger.getLogger(ShortCuts.class.getName());
	private static final int MAX_SHORTCUTS_PER_BAR = 12;
	private final L2PcInstance _owner;
	private final Map<Integer, Shortcut> _shortCuts = new TreeMap<>();
	
	public ShortCuts(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public Shortcut[] getAllShortCuts()
	{
		return _shortCuts.values().toArray(new Shortcut[_shortCuts.values().size()]);
	}
	
	public Shortcut getShortCut(int slot, int page)
	{
		Shortcut sc = _shortCuts.get(slot + (page * MAX_SHORTCUTS_PER_BAR));
		// Verify shortcut
		if ((sc != null) && (sc.getType() == ShortcutType.ITEM))
		{
			if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
				sc = null;
			}
		}
		return sc;
	}
	
	public synchronized void registerShortCut(Shortcut shortcut)
	{
		// Verify shortcut
		if (shortcut.getType() == ShortcutType.ITEM)
		{
			final L2ItemInstance item = _owner.getInventory().getItemByObjectId(shortcut.getId());
			if (item == null)
			{
				return;
			}
			shortcut.setSharedReuseGroup(item.getSharedReuseGroup());
		}
		final Shortcut oldShortCut = _shortCuts.put(shortcut.getSlot() + (shortcut.getPage() * MAX_SHORTCUTS_PER_BAR), shortcut);
		registerShortCutInDb(shortcut, oldShortCut);
	}
	
	private void registerShortCutInDb(Shortcut shortcut, Shortcut oldShortCut)
	{
		if (oldShortCut != null)
		{
			deleteShortCutFromDb(oldShortCut);
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, shortcut.getSlot());
			ps.setInt(3, shortcut.getPage());
			ps.setInt(4, shortcut.getType().ordinal());
			ps.setInt(5, shortcut.getId());
			ps.setInt(6, shortcut.getLevel());
			ps.setInt(7, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not store character shortcut: " + e.getMessage(), e);
		}
	}
	
	/**
	 * @param slot
	 * @param page
	 */
	public synchronized void deleteShortCut(int slot, int page)
	{
		final Shortcut old = _shortCuts.remove(slot + (page * MAX_SHORTCUTS_PER_BAR));
		if ((old == null) || (_owner == null))
		{
			return;
		}
		deleteShortCutFromDb(old);
		if (old.getType() == ShortcutType.ITEM)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if ((item != null) && (item.getItemType() == EtcItemType.SHOT))
			{
				if (_owner.removeAutoSoulShot(item.getId()))
				{
					_owner.sendPacket(new ExAutoSoulShot(item.getId(), 0));
				}
			}
		}
		
		_owner.sendPacket(new ShortCutInit(_owner));
		
		for (int shotId : _owner.getAutoSoulShot())
		{
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
		}
	}
	
	public synchronized void deleteShortCutByObjectId(int objectId)
	{
		for (Shortcut shortcut : _shortCuts.values())
		{
			if ((shortcut.getType() == ShortcutType.ITEM) && (shortcut.getId() == objectId))
			{
				deleteShortCut(shortcut.getSlot(), shortcut.getPage());
				break;
			}
		}
	}
	
	/**
	 * @param shortcut
	 */
	private void deleteShortCutFromDb(Shortcut shortcut)
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, shortcut.getSlot());
			ps.setInt(3, shortcut.getPage());
			ps.setInt(4, _owner.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not delete character shortcut: " + e.getMessage(), e);
		}
	}
	
	@Override
	public boolean restoreMe()
	{
		_shortCuts.clear();
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT charId, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE charId=? AND class_index=?"))
		{
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, _owner.getClassIndex());
			
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int slot = rset.getInt("slot");
					int page = rset.getInt("page");
					int type = rset.getInt("type");
					int id = rset.getInt("shortcut_id");
					int level = rset.getInt("level");
					
					_shortCuts.put(slot + (page * MAX_SHORTCUTS_PER_BAR), new Shortcut(slot, page, ShortcutType.values()[type], id, level, 1));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore character shortcuts: " + e.getMessage(), e);
			return false;
		}
		
		// Verify shortcuts
		for (Shortcut sc : getAllShortCuts())
		{
			if (sc.getType() == ShortcutType.ITEM)
			{
				L2ItemInstance item = _owner.getInventory().getItemByObjectId(sc.getId());
				if (item == null)
				{
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
				else if (item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 * @param skillId the skill Id to search and update.
	 * @param skillLevel the skill level to update.
	 */
	public synchronized void updateShortCuts(int skillId, int skillLevel)
	{
		// Update all the shortcuts for this skill
		for (Shortcut sc : _shortCuts.values())
		{
			if ((sc.getId() == skillId) && (sc.getType() == ShortcutType.SKILL))
			{
				Shortcut newsc = new Shortcut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1);
				_owner.sendPacket(new ShortCutRegister(newsc));
				_owner.registerShortCut(newsc);
			}
		}
	}
}
