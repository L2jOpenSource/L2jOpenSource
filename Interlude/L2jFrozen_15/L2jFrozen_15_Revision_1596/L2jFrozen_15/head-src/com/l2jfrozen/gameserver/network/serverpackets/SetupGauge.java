package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample 0000: 85 00 00 00 00 f0 1a 00 00
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	public static final int GREEN = 3;
	
	private final int dat1;
	private final int time;
	
	public SetupGauge(final int dat1, final int time)
	{
		this.dat1 = dat1;// color 0-blue 1-red 2-cyan 3-
		this.time = time;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6d);
		writeD(dat1);
		writeD(time);
		
		writeD(time); // c2
	}
	
	@Override
	public String getType()
	{
		return "[S] 6d SetupGauge";
	}
}
