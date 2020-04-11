package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author vGodFather
 */
public class NetPingPacket extends L2GameServerPacket
{
	private final int _objId;
	
	public NetPingPacket(L2PcInstance cha)
	{
		_objId = cha.getObjectId();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xD9);
		writeD(_objId);
	}
}