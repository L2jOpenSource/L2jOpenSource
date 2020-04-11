package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;

/**
 * Support for "Chat with Friends" dialog. <BR>
 * Inform player about friend online status change <BR>
 * Format: cdSd<BR>
 * d: Online/Offline<BR>
 * S: Friend Name <BR>
 * d: Player Object ID <BR>
 * @author JIV
 */
public class FriendStatus extends L2GameServerPacket
{
	private final boolean _online;
	private final int _objid;
	private final String _name;
	
	public FriendStatus(int objId)
	{
		_objid = objId;
		_name = PlayerInfoTable.getInstance().getPlayerName(objId);
		_online = World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7b);
		writeD(_online ? 1 : 0);
		writeS(_name);
		writeD(_objid);
	}
}