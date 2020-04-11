package com.l2jfrozen.gameserver.managers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.l2jfrozen.logs.Log;

/**
 * @author Shyla
 */
public class PacketsLoggerManager
{
	private final List<String> monitored_characters = new ArrayList<>();
	private final Hashtable<String, List<String>> character_blocked_packets = new Hashtable<>();
	
	protected PacketsLoggerManager()
	{
		character_blocked_packets.clear();
		monitored_characters.clear();
	}
	
	public void startCharacterPacketsMonitoring(final String character)
	{
		
		if (!monitored_characters.contains(character))
		{
			monitored_characters.add(character);
		}
		
	}
	
	public void stopCharacterPacketsMonitoring(final String character)
	{
		
		if (monitored_characters.contains(character))
		{
			monitored_characters.remove(character);
		}
		
	}
	
	public void blockCharacterPacket(final String character, final String packet)
	{
		
		List<String> blocked_packets = character_blocked_packets.get(character);
		if (blocked_packets == null)
		{
			blocked_packets = new ArrayList<>();
		}
		
		if (!blocked_packets.contains(packet))
		{
			blocked_packets.add(packet);
		}
		character_blocked_packets.put(character, blocked_packets);
		
	}
	
	public void restoreCharacterPacket(final String character, final String packet)
	{
		
		final List<String> blocked_packets = character_blocked_packets.get(character);
		if (blocked_packets != null)
		{
			
			if (blocked_packets.contains(packet))
			{
				blocked_packets.remove(packet);
			}
			
			character_blocked_packets.put(character, blocked_packets);
			
		}
		
	}
	
	public boolean isCharacterMonitored(final String character)
	{
		return monitored_characters.contains(character);
	}
	
	public boolean isCharacterPacketBlocked(final String character, final String packet)
	{
		
		final List<String> blocked_packets = character_blocked_packets.get(character);
		if (blocked_packets != null)
		{
			
			if (blocked_packets.contains(packet))
			{
				return true;
			}
			
		}
		
		return false;
		
	}
	
	public void logCharacterPacket(final String character, final String packet)
	{
		
		Log.add("[Character: " + character + "] has sent [Packet: " + packet + "]", character + "_packets");
		
	}
	
	public static PacketsLoggerManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		public static final PacketsLoggerManager instance = new PacketsLoggerManager();
	}
}
