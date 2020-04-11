package com.l2jfrozen.gameserver.network.gameserverpackets;

import java.util.Vector;

/**
 * @author -Wooden-
 */
public class ServerStatus extends GameServerBasePacket
{
	private final Vector<Attribute> attributes;
	
	public static final String[] STATUS_STRING =
	{
		"Auto",
		"Good",
		"Normal",
		"Full",
		"Down",
		"Gm Only"
	};
	
	public static final int SERVER_LIST_STATUS = 0x01;
	public static final int SERVER_LIST_CLOCK = 0x02;
	public static final int SERVER_LIST_SQUARE_BRACKET = 0x03;
	public static final int MAX_PLAYERS = 0x04;
	public static final int TEST_SERVER = 0x05;
	
	public static final int STATUS_AUTO = 0x00;
	public static final int STATUS_GOOD = 0x01;
	public static final int STATUS_NORMAL = 0x02;
	public static final int STATUS_FULL = 0x03;
	public static final int STATUS_DOWN = 0x04;
	public static final int STATUS_GM_ONLY = 0x05;
	
	public static final int ON = 0x01;
	public static final int OFF = 0x00;
	
	class Attribute
	{
		public int id;
		public int value;
		
		Attribute(final int pId, final int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public ServerStatus()
	{
		attributes = new Vector<>();
	}
	
	public void addAttribute(final int id, final int value)
	{
		attributes.add(new Attribute(id, value));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(0x06);
		writeD(attributes.size());
		for (int i = 0; i < attributes.size(); i++)
		{
			final Attribute temp = attributes.get(i);
			
			writeD(temp.id);
			writeD(temp.value);
		}
		return getBytes();
	}
}
