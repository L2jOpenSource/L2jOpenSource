/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.punishmenthandlers;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.util.IpPack;

import ai.npc.AbstractNpcAI;

/**
 * DualBox Protection based on WAN IP Address and tracert obtained directly from the client.
 * @author Sacrifice
 */
public final class DualBoxProtection extends AbstractNpcAI
{
	private static final Map<IpPack, Integer> _address = new HashMap<>();
	
	public DualBoxProtection()
	{
		super(DualBoxProtection.class.getSimpleName(), "DualBoxPC");
		setOnEnterWorld(true);
	}
	
	@Override
	public String onEnterWorld(L2PcInstance activeChar)
	{
		if (Config.DUALBOX_PROTECTION_ENABLED)
		{
			final L2GameClient client = activeChar.getClient();
			if (!DualBoxProtection.getInstance().registerConnection(client, activeChar))
			{
				DualBoxProtection.getInstance().removeConnection(client);
				activeChar.closeNetConnection(false);
			}
		}
		return super.onEnterWorld(activeChar);
	}
	
	public synchronized boolean registerConnection(L2GameClient client, L2PcInstance activeChar)
	{
		if (Config.DUALBOX_PROTECTION_ENABLED)
		{
			try
			{
				IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
				Integer count = _address.get(pack) == null ? 0 : _address.get(pack);
				if ((count < Config.DUALBOX_MAX_ALLOWED_PER_PC) && !activeChar.hasPremiumStatus())
				{
					count += 1;
					_address.put(pack, count);
					return true;
				}
				else if ((count < Config.DUALBOX_PREMIUM_MAX_ALLOWED_PER_PC) && activeChar.hasPremiumStatus())
				{
					_address.put(pack, count += 1);
					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public synchronized void removeConnection(L2GameClient client)
	{
		if (Config.DUALBOX_PROTECTION_ENABLED)
		{
			try
			{
				if ((client.getConnection().getInetAddress().getHostAddress() != null) || (client.getTrace() != null))
				{
					IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
					Integer count = _address.get(pack) != null ? _address.get(pack) : 0;
					if (count > 0)
					{
						_address.put(pack, count -= 1);
					}
					else
					{
						_address.remove(pack);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public int getConnectionCount(String address)
	{
		if (_address.containsKey(address))
		{
			return _address.get(address);
		}
		return 0;
	}
	
	public synchronized void clearAllConnections()
	{
		_address.clear();
	}
	
	public static DualBoxProtection getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DualBoxProtection _instance = new DualBoxProtection();
	}
	
}