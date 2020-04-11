package l2r.gameserver.enums;

/**
 * @author vGodFather
 */
public enum RestartPoint
{
	TO_VILLAGE(0),
	TO_CLANHALL(1),
	TO_CASTLE(2),
	TO_FORTRESS(3),
	TO_HQFLAG(4),
	FIXED(5),
	AGATHION(21),
	ITEM_FIXED(22),
	TO_JAIL(27);
	
	private int _id;
	
	private RestartPoint(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public static RestartPoint getType(int id)
	{
		for (RestartPoint t : RestartPoint.values())
		{
			if (t.getId() == id)
			{
				return t;
			}
		}
		return null;
	}
}
