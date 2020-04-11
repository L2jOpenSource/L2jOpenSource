package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.util.Point3D;

/**
 * Format: (ch) d[ddddd].
 * @author -Wooden-
 */
public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private final List<CursedWeaponInfo> cursedWeaponInfo;
	
	/**
	 * Instantiates a new ex cursed weapon location.
	 * @param cursedWeaponInfo the cursed weapon info
	 */
	public ExCursedWeaponLocation(final List<CursedWeaponInfo> cursedWeaponInfo)
	{
		this.cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x46);
		
		if (!cursedWeaponInfo.isEmpty())
		{
			writeD(cursedWeaponInfo.size());
			for (final CursedWeaponInfo w : cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);
				
				writeD(w.pos.getX());
				writeD(w.pos.getY());
				writeD(w.pos.getZ());
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}
	
	/**
	 * The Class CursedWeaponInfo.
	 */
	public static class CursedWeaponInfo
	{
		
		public Point3D pos;
		public int id;
		public int activated; // 0 - not activated ? 1 - activated
		
		/**
		 * Instantiates a new cursed weapon info.
		 * @param p      the p
		 * @param ID     the iD
		 * @param status the status
		 */
		public CursedWeaponInfo(final Point3D p, final int ID, final int status)
		{
			pos = p;
			id = ID;
			activated = status;
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:46 ExCursedWeaponLocation";
	}
}
