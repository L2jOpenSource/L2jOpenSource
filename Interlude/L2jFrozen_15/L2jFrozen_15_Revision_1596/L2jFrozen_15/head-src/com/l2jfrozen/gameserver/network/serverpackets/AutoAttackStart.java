package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AutoAttackStart extends L2GameServerPacket
{
	private final int targetObjId;
	
	/**
	 * @param targetId
	 */
	public AutoAttackStart(final int targetId)
	{
		targetObjId = targetId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2b);
		writeD(targetObjId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 2B AutoAttackStart";
	}
}
