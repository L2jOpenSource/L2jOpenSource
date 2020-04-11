/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.walk;

/**
 * @author vGodFather
 */
public enum RepeatType
{
	NO((byte) -1, ""),
	GO_BACK((byte) 0, "back"),
	GO_FIRST((byte) 1, "cycle"),
	TELE_FIRST((byte) 2, "conveyor"),
	RANDOM((byte) 3, "random");
	
	private byte _id;
	private String _name;
	
	RepeatType(byte id, String name)
	{
		_id = id;
		_name = name;
	}
	
	public byte getId()
	{
		return _id;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public static final RepeatType findByName(String name)
	{
		for (RepeatType type : values())
		{
			if (type.getName().equalsIgnoreCase(name))
			{
				return type;
			}
		}
		return null;
	}
}
