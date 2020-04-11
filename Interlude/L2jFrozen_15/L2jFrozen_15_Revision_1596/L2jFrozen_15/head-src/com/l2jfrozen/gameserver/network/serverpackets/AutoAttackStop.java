package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AutoAttackStop extends L2GameServerPacket
{
	private final int targetObjId;
	
	/**
	 * @param targetObjId
	 */
	public AutoAttackStop(final int targetObjId)
	{
		this.targetObjId = targetObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2c);
		writeD(targetObjId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 3C AutoAttackStop";
	}
}
