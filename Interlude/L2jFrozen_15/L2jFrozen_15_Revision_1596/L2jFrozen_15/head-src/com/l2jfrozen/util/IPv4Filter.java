package com.l2jfrozen.util;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.l2jfrozen.Config;
import com.l2jfrozen.netcore.IAcceptFilter;

/**
 * Formatted Forsaiken's IPv4 filter [DrHouse]
 * @author Forsaiken
 */
public class IPv4Filter implements IAcceptFilter, Runnable
{
	private final HashMap<Integer, Flood> ipFloodMap;
	private static final long SLEEP_TIME = 5000;
	
	public IPv4Filter()
	{
		ipFloodMap = new HashMap<>();
		final Thread t = new Thread(this);
		t.setName(getClass().getSimpleName());
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * @param  ip
	 * @return
	 */
	private static final int hash(final byte[] ip)
	{
		return ip[0] & 0xFF | ip[1] << 8 & 0xFF00 | ip[2] << 16 & 0xFF0000 | ip[3] << 24 & 0xFF000000;
	}
	
	protected static final class Flood
	{
		long lastAccess;
		int trys;
		
		Flood()
		{
			lastAccess = System.currentTimeMillis();
			trys = 0;
		}
	}
	
	@Override
	public boolean accept(final SocketChannel sc)
	{
		final InetAddress addr = sc.socket().getInetAddress();
		final int h = hash(addr.getAddress());
		
		final long current = System.currentTimeMillis();
		Flood f;
		synchronized (ipFloodMap)
		{
			f = ipFloodMap.get(h);
		}
		if (f != null)
		{
			if (f.trys == -1)
			{
				f.lastAccess = current;
				return false;
			}
			
			if (f.lastAccess + 1000 > current)
			{
				f.lastAccess = current;
				
				if (f.trys >= 3)
				{
					f.trys = -1;
					return false;
				}
				
				f.trys++;
			}
			else
			{
				f.lastAccess = current;
			}
		}
		else
		{
			synchronized (ipFloodMap)
			{
				ipFloodMap.put(h, new Flood());
			}
		}
		
		return true;
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			final long reference = System.currentTimeMillis() - (1000 * 300);
			synchronized (ipFloodMap)
			{
				final Iterator<Entry<Integer, Flood>> it = ipFloodMap.entrySet().iterator();
				while (it.hasNext())
				{
					final Flood f = it.next().getValue();
					if (f.lastAccess < reference)
					{
						it.remove();
					}
				}
			}
			
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (final InterruptedException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				return;
			}
		}
	}
	
}