package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * format dddddd
 */
public class Earthquake extends L2GameServerPacket
{
	private final int x;
	private final int y;
	private final int z;
	private final int intensity;
	private final int duration;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param intensity
	 * @param duration
	 */
	public Earthquake(final int x, final int y, final int z, final int intensity, final int duration)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.intensity = intensity;
		this.duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xc4);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(intensity);
		writeD(duration);
		writeD(0x00); // Unknown
	}
	
	@Override
	public String getType()
	{
		return "[S] C4 Earthquake";
	}
}
