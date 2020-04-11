package com.l2jfrozen.gameserver.managers;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.xml.ZoneData;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;

public class TownManager
{
	public static final int DARK_ELF_VILLAGE = 1;
	public static final int TALKIN_ISLAND_VILLAGE = 2;
	public static final int ELVEN_VILLAGE = 3;
	public static final int ORC_VILLAGE = 4;
	public static final int GLUDIN = 5;
	public static final int DWARVEN_VILLAGE = 6;
	public static final int GLUDIO = 7;
	public static final int DION = 8;
	public static final int GIRAN = 9;
	public static final int OREN = 10;
	public static final int HUNTERS_VILLAGE = 11;
	public static final int ADEN = 12;
	public static final int GODDARD = 13;
	public static final int RUNE = 14;
	public static final int HEINE = 15;
	public static final int FLORAN_VILLAGE = 16;
	public static final int SCHUTTGART = 17;
	public static final int PRIMAVERAL_ISLE = 18;
	
	private static TownManager instance;
	
	public static final TownManager getInstance()
	{
		if (instance == null)
		{
			instance = new TownManager();
		}
		return instance;
	}
	
	public final L2TownZone getClosestTown(final L2Object activeObject)
	{
		switch (MapRegionTable.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
			case 0:
				return getTown(TALKIN_ISLAND_VILLAGE); // TI
			case 1:
				return getTown(ELVEN_VILLAGE); // Elven
			case 2:
				return getTown(DARK_ELF_VILLAGE); // DE
			case 3:
				return getTown(ORC_VILLAGE); // Orc
			case 4:
				return getTown(DWARVEN_VILLAGE); // Dwarven
			case 5:
				return getTown(GLUDIO); // Gludio
			case 6:
				return getTown(GLUDIN); // Gludin
			case 7:
				return getTown(DION); // Dion
			case 8:
				return getTown(GIRAN); // Giran
			case 9:
				return getTown(OREN); // Oren
			case 10:
				return getTown(ADEN); // Aden
			case 11:
				return getTown(HUNTERS_VILLAGE); // HV
			case 12:
				return getTown(GIRAN); // Giran Harbor
			case 13:
				return getTown(HEINE); // Heine
			case 14:
				return getTown(RUNE); // Rune
			case 15:
				return getTown(GODDARD); // Goddard
			case 16:
				return getTown(SCHUTTGART); // Schuttgart
			case 17:
				return getTown(FLORAN_VILLAGE); // Floran
			case 18:
				return getTown(PRIMAVERAL_ISLE); // Primeval Isle
		}
		
		return getTown(FLORAN_VILLAGE); // Default to floran
	}
	
	public final static int getClosestLocation(final L2Object activeObject)
	{
		switch (MapRegionTable.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
			case 0:
				return 1; // TI
			case 1:
				return 4; // Elven
			case 2:
				return 3; // DE
			case 3:
				return 9; // Orc
			case 4:
				return 9; // Dwarven
			case 5:
				return 2; // Gludio
			case 6:
				return 2; // Gludin
			case 7:
				return 5; // Dion
			case 8:
				return 6; // Giran
			case 9:
				return 10; // Oren
			case 10:
				return 13; // Aden
			case 11:
				return 11; // HV
			case 12:
				return 6; // Giran Harbour
			case 13:
				return 12; // Heine
			case 14:
				return 14; // Rune
			case 15:
				return 15; // Goddard
			case 16:
				return 9; // Schuttgart
		}
		return 0;
	}
	
	public final boolean townHasCastleInSiege(final int townId)
	{
		// int[] castleidarray = {0,0,0,0,0,0,0,1,2,3,4,0,5,0,0,6,0};
		final int[] castleidarray =
		{
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			1,
			2,
			3,
			4,
			0,
			5,
			7,
			8,
			6,
			0,
			9,
			0
		};
		final int castleIndex = castleidarray[townId];
		
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}
	
	public final boolean townHasCastleInSiege(final int x, final int y)
	{
		final int curtown = MapRegionTable.getInstance().getMapRegion(x, y);
		// int[] castleidarray = {0,0,0,0,0,1,0,2,3,4,5,0,0,6,0,0,0,0};
		final int[] castleidarray =
		{
			0,
			0,
			0,
			0,
			0,
			1,
			0,
			2,
			3,
			4,
			5,
			0,
			0,
			6,
			8,
			7,
			9,
			0,
			0
		};
		// find an instance of the castle for this town.
		final int castleIndex = castleidarray[curtown];
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}
	
	public L2TownZone getTown(int townId)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2TownZone)
			{
				L2TownZone temp = (L2TownZone) zone;
				
				if (temp.getTownId() == townId)
				{
					return temp;
				}
			}
		}
		
		return null;
	}
	
	public L2TownZone getTown(int x, int y, int z)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2TownZone)
			{
				L2TownZone temp = (L2TownZone) zone;
				
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
}
