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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * TODO: System messages:<br>
 * ADD: 3223: The previous name is being registered. Please try again later.<br>
 * DEL 3219: $s1 was successfully deleted from your Contact List.<br>
 * DEL 3217: The name is not currently registered.
 * @author UnAfraid, mrTJO
 */
public class L2ContactList
{
	private final Logger _log = Logger.getLogger(getClass().getName());
	private final L2PcInstance activeChar;
	private final List<String> _contacts = new CopyOnWriteArrayList<>();
	
	private static final String QUERY_ADD = "INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)";
	private static final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
	private static final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";
	
	public L2ContactList(L2PcInstance player)
	{
		activeChar = player;
		restore();
	}
	
	public void restore()
	{
		_contacts.clear();
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QUERY_LOAD))
		{
			ps.setInt(1, activeChar.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				int contactId;
				String contactName;
				while (rs.next())
				{
					contactId = rs.getInt(1);
					contactName = CharNameTable.getInstance().getNameById(contactId);
					if ((contactName == null) || contactName.equals(activeChar.getName()) || (contactId == activeChar.getObjectId()))
					{
						continue;
					}
					
					_contacts.add(contactName);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
	}
	
	public boolean add(String name)
	{
		SystemMessage sm;
		
		int contactId = CharNameTable.getInstance().getIdByName(name);
		if (_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
			return false;
		}
		else if (activeChar.getName().equals(name))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST);
			return false;
		}
		else if (_contacts.size() >= 100)
		{
			activeChar.sendPacket(SystemMessageId.CONTACT_LIST_LIMIT_REACHED);
			return false;
		}
		else if (contactId < 1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME);
			sm.addString(name);
			activeChar.sendPacket(sm);
			return false;
		}
		else
		{
			for (String contactName : _contacts)
			{
				if (contactName.equalsIgnoreCase(name))
				{
					activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
					return false;
				}
			}
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QUERY_ADD))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.setInt(2, contactId);
			ps.execute();
			
			_contacts.add(name);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		return true;
	}
	
	public void remove(String name)
	{
		int contactId = CharNameTable.getInstance().getIdByName(name);
		
		if (!_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_NOT_REGISTERED_ON_CONTACT_LIST);
			return;
		}
		else if (contactId < 1)
		{
			// TODO: Message?
			return;
		}
		
		_contacts.remove(name);
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QUERY_REMOVE))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.setInt(2, contactId);
			ps.execute();
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
	}
	
	public List<String> getAllContacts()
	{
		return _contacts;
	}
}
