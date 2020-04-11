package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowQuestInfo extends L2GameServerPacket
{
	public static final ExShowQuestInfo STATIC_PACKET = new ExShowQuestInfo();
	
	private ExShowQuestInfo()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x19);
	}
}