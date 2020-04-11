package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private final Player _activeChar;
	private String _storeMsg;
	
	public PrivateStoreMsgBuy(Player player)
	{
		_activeChar = player;
		if (_activeChar.getBuyList() != null)
			_storeMsg = _activeChar.getBuyList().getTitle();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb9);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
}