package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.StaticObject;

/**
 * format dd
 */
public class StaticObjectInfo extends L2GameServerPacket
{
	private final StaticObject _staticObject;
	
	public StaticObjectInfo(StaticObject StaticObject)
	{
		_staticObject = StaticObject;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x99);
		writeD(_staticObject.getStaticObjectId()); // staticObjectId
		writeD(_staticObject.getObjectId()); // objectId
	}
}