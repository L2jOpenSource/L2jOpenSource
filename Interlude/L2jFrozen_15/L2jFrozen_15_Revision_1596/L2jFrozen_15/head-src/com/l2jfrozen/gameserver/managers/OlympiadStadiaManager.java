package com.l2jfrozen.gameserver.managers;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.xml.ZoneData;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.model.zone.type.L2OlympiadStadiumZone;

public class OlympiadStadiaManager
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadStadiaManager.class);
	private static OlympiadStadiaManager instance;
	
	public static final OlympiadStadiaManager getInstance()
	{
		if (instance == null)
		{
			instance = new OlympiadStadiaManager();
		}
		return instance;
	}
	
	public L2OlympiadStadiumZone getStadium(L2Character character)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2OlympiadStadiumZone)
			{
				L2OlympiadStadiumZone temp = (L2OlympiadStadiumZone) zone;
				
				if (temp.isInsideZone(character))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
	
	public L2OlympiadStadiumZone getStadiumByLoc(int x, int y, int z)
	{
		for (L2ZoneType zone : ZoneData.getInstance().getAllZones().values())
		{
			if (zone instanceof L2OlympiadStadiumZone)
			{
				L2OlympiadStadiumZone temp = (L2OlympiadStadiumZone) zone;
				
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		
		return null;
	}
}
