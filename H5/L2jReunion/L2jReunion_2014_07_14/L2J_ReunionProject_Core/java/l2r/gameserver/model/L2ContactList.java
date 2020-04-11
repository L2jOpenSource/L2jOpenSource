/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javolution.util.FastList;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.sql.CharNameTable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: System messages:<br>
 * ADD: 3223: The previous name is being registered. Please try again later.<br>
 * DEL 3219: $s1 was successfully deleted from your Contact List.<br>
 * DEL 3217: The name is not currently registered.
 * @author UnAfraid, mrTJO
 */
public class L2ContactList
{
	private final Logger _log = LoggerFactory.getLogger(getClass().getName());
	private final L2PcInstance activeChar;
	private final List<String> _contacts;
	
	private final String QUERY_ADD = "INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)";
	private final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
	private final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";
	
	public L2ContactList(L2PcInstance player)
	{
		activeChar = player;
		_contacts = new FastList<String>().shared();
		restore();
	}
	
	public void restore()
	{
		_contacts.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_LOAD))
		{
			statement.setInt(1, activeChar.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				int contactId;
				String contactName;
				while (rset.next())
				{
					contactId = rset.getInt(1);
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
			_log.warn("Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
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
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_ADD))
		{
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();
			
			_contacts.add(name);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			_log.warn("Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
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
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(QUERY_REMOVE))
		{
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST);
			sm.addString(name);
			activeChar.sendPacket(sm);
		}
		catch (Exception e)
		{
			_log.warn("Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
	}
	
	public List<String> getAllContacts()
	{
		return _contacts;
	}
}
