package net.sf.l2j.gameserver.network.serverpackets;

/**
 * Format: ch d
 * @author KenM
 */
public class ExRedSky extends L2GameServerPacket
{
	private final int _duration;
	
	public ExRedSky(int duration)
	{
		_duration = duration;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x40);
		writeD(_duration);
	}
}