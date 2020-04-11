package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket
{
	private final int _isRemoved;
	
	public ExDuelEnemyRelation(boolean isRemoved)
	{
		_isRemoved = isRemoved ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x58);
		writeD(_isRemoved);
	}
}