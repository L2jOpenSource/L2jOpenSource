package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private final L2StaticObjectInstance staticObject;
	
	/**
	 * [S]0x99 StaticObjectPacket dd
	 * @param StaticObject
	 */
	public StaticObject(final L2StaticObjectInstance StaticObject)
	{
		staticObject = StaticObject; // staticObjectId
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeD(staticObject.getStaticObjectId()); // staticObjectId
		writeD(staticObject.getObjectId()); // objectId
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 99 StaticObjectPacket";
	}
}
