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
package com.l2jserver.gameserver.data.xml.impl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jserver.util.data.xml.IXmlReader;

/**
 * Secondary Auth data.
 * @author NosBit
 */
public class SecondaryAuthData implements IXmlReader
{
	private final Set<String> _forbiddenPasswords = new HashSet<>();
	private boolean _enabled = false;
	private int _maxAttempts = 5;
	private int _banTime = 480;
	private String _recoveryLink = "";
	
	protected SecondaryAuthData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_forbiddenPasswords.clear();
		parseFile(new File("config/SecondaryAuth.xml"));
		LOG.info("{}: Loaded {} forbidden passwords.", getClass().getSimpleName(), _forbiddenPasswords.size());
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		try
		{
			for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("list".equalsIgnoreCase(node.getNodeName()))
				{
					for (Node list_node = node.getFirstChild(); list_node != null; list_node = list_node.getNextSibling())
					{
						if ("enabled".equalsIgnoreCase(list_node.getNodeName()))
						{
							_enabled = Boolean.parseBoolean(list_node.getTextContent());
						}
						else if ("maxAttempts".equalsIgnoreCase(list_node.getNodeName()))
						{
							_maxAttempts = Integer.parseInt(list_node.getTextContent());
						}
						else if ("banTime".equalsIgnoreCase(list_node.getNodeName()))
						{
							_banTime = Integer.parseInt(list_node.getTextContent());
						}
						else if ("recoveryLink".equalsIgnoreCase(list_node.getNodeName()))
						{
							_recoveryLink = list_node.getTextContent();
						}
						else if ("forbiddenPasswords".equalsIgnoreCase(list_node.getNodeName()))
						{
							for (Node forbiddenPasswords_node = list_node.getFirstChild(); forbiddenPasswords_node != null; forbiddenPasswords_node = forbiddenPasswords_node.getNextSibling())
							{
								if ("password".equalsIgnoreCase(forbiddenPasswords_node.getNodeName()))
								{
									_forbiddenPasswords.add(forbiddenPasswords_node.getTextContent());
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOG.warn("Failed to load secondary auth data from xml.", e);
		}
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public int getMaxAttempts()
	{
		return _maxAttempts;
	}
	
	public int getBanTime()
	{
		return _banTime;
	}
	
	public String getRecoveryLink()
	{
		return _recoveryLink;
	}
	
	public Set<String> getForbiddenPasswords()
	{
		return _forbiddenPasswords;
	}
	
	public boolean isForbiddenPassword(String password)
	{
		return _forbiddenPasswords.contains(password);
	}
	
	public static SecondaryAuthData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SecondaryAuthData _instance = new SecondaryAuthData();
	}
}
