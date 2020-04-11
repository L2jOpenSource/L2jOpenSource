package com.l2jfrozen.gameserver.network.serverpackets;

public class CameraMode extends L2GameServerPacket
{
	private final int mode;
	
	/**
	 * Forces client camera mode change
	 * @param mode 0 - third person cam 1 - first person cam
	 */
	public CameraMode(final int mode)
	{
		this.mode = mode;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xf1);
		writeD(mode);
	}
	
	@Override
	public String getType()
	{
		return "[S] F1 CameraMode";
	}
}
