package main.util;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.model.L2Character;

import main.holders.objects.CharacterHolder;
import main.holders.objects.ObjectHolder;

/**
 * @author fissban
 */
public class Util
{
	public static final String SEPARATOR = "-----------------------------------------------------------";
	
	/**
	 * Check if the objects belong to a particular instance.
	 * @param  type
	 * @param  objects
	 * @return
	 */
	public static <A> boolean areObjectType(Class<A> type, ObjectHolder... objects)
	{
		if ((objects == null) || (objects.length <= 0))
		{
			return false;
		}
		
		for (ObjectHolder o : objects)
		{
			if ((o == null) || (o.getInstance() == null) || !type.isAssignableFrom(o.getInstance().getClass()))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isNumber(String text)
	{
		try
		{
			Integer.parseInt(text);
			return true;
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
	}
	
	public static List<Integer> parseInt(String line, String split)
	{
		List<Integer> list = new ArrayList<>();
		
		for (String s : line.split(split))
		{
			list.add(Integer.parseInt(s));
		}
		
		return list;
	}
	
	/**
	 * Check the difference in level between 2 characters, return true if it is lower than lvl
	 * <li></li>
	 * <li></li>
	 * @param  ph
	 * @param  obj
	 * @param  lvl
	 * @return
	 */
	public static boolean checkLvlDifference(CharacterHolder ph, CharacterHolder obj, int lvl)
	{
		if (Math.abs(ph.getInstance().getLevel() - obj.getInstance().getLevel()) <= lvl)
		{
			return true;
		}
		
		return false;
	}
	
	public static String getClosestTownName(L2Character c)
	{
		int x = c.getX();
		int y = c.getY();
		
		return getClosestTownName(x, y);
	}
	
	public static String getClosestTownName(int x, int y)
	{
		
		int nearestTownId = MapRegionTable.getInstance().getMapRegion(x, y);
		
		switch (nearestTownId)
		{
			case 0:
				return "Talking Island Village";
			case 1:
				return "Elven Village";
			case 2:
				return "Dark Elven Village";
			case 3:
				return "Orc Village";
			case 4:
				return "Dwarven Village";
			case 5:
				return "Gludio Castle Town";
			case 6:
				return "Gludin Village";
			case 7:
				return "Dion Castle Town";
			case 8:
				return "Giran Castle Town";
			case 9:
				return "Oren Castle Town";
			case 10:
				return "Aden Castle Town";
			case 11:
				return "Hunters Village";
			case 12:
				return "Giran Harbor";
			case 13:
				return "Innadril Castle Town";
			case 14:
				return "Rune Castle Town";
			case 15:
				return "Goddard Castle Town";
			case 16:
				return "Floran Village";
			default:
				return "Aden Castle Town";
		}
	}
}
