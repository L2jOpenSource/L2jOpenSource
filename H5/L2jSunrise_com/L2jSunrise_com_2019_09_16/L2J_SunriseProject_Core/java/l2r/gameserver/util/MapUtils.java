package l2r.gameserver.util;

import l2r.gameserver.model.L2Object;

public class MapUtils
{
	private MapUtils()
	{
	}
	
	public static int regionX(L2Object o)
	{
		return regionX(o.getX());
	}
	
	public static int regionY(L2Object o)
	{
		return regionY(o.getY());
	}
	
	public static int regionX(int x)
	{
		return ((x - (-327680)) >> 15) + 10;
	}
	
	public static int regionY(int y)
	{
		return ((y - (-262144)) >> 15) + 10;
	}
}
