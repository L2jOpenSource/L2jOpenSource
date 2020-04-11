package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SocialAction extends L2GameServerPacket
{
	private final int charObjId;
	private final int actionId;
	
	/**
	 * 0x3d SocialAction dd
	 * @param playerId
	 * @param actionId
	 */
	public SocialAction(final int playerId, final int actionId)
	{
		charObjId = playerId;
		this.actionId = actionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2d);
		writeD(charObjId);
		writeD(actionId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 2D SocialAction";
	}
}
