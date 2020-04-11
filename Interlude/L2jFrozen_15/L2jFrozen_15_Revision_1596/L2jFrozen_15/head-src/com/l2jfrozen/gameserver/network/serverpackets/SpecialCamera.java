package com.l2jfrozen.gameserver.network.serverpackets;

public class SpecialCamera extends L2GameServerPacket
{
	private final int id;
	private final int dist;
	private final int yaw;
	private final int pitch;
	private final int time;
	private final int duration;
	
	public SpecialCamera(final int id, final int dist, final int yaw, final int pitch, final int time, final int duration)
	{
		this.id = id;
		this.dist = dist;
		this.yaw = yaw;
		this.pitch = pitch;
		this.time = time;
		this.duration = duration;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xc7);
		writeD(id);
		writeD(dist);
		writeD(yaw);
		writeD(pitch);
		writeD(time);
		writeD(duration);
	}
	
	@Override
	public String getType()
	{
		return "[S] C7 SpecialCamera";
	}
}
