package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.1.6.2 $ $Date: 2005/03/27 15:29:39 $
 */
public class PlaySound extends L2GameServerPacket
{
	private final int unknown1;
	private final String soundFile;
	private final int unknown3;
	private final int unknown4;
	private final int unknown5;
	private final int unknown6;
	private final int unknown7;
	
	public PlaySound(final String soundFile)
	{
		unknown1 = 0;
		this.soundFile = soundFile;
		unknown3 = 0;
		unknown4 = 0;
		unknown5 = 0;
		unknown6 = 0;
		unknown7 = 0;
	}
	
	public PlaySound(final int unknown1, final String soundFile, final int unknown3, final int unknown4, final int unknown5, final int unknown6, final int unknown7)
	{
		this.unknown1 = unknown1;
		this.soundFile = soundFile;
		this.unknown3 = unknown3;
		this.unknown4 = unknown4;
		this.unknown5 = unknown5;
		this.unknown6 = unknown6;
		this.unknown7 = unknown7;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x98);
		writeD(unknown1); // unknown 0 for quest and ship;
		writeS(soundFile);
		writeD(unknown3); // unknown 0 for quest; 1 for ship;
		writeD(unknown4); // 0 for quest; objectId of ship
		writeD(unknown5); // x
		writeD(unknown6); // y
		writeD(unknown7); // z
	}
	
	@Override
	public String getType()
	{
		return "[S] 98 PlaySound";
	}
}
