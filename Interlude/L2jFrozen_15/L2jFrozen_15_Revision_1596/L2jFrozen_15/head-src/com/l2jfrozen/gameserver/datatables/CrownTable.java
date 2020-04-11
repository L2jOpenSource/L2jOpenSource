package com.l2jfrozen.gameserver.datatables;

import java.util.Arrays;
import java.util.List;

/**
 * This class has just one simple function to return the item id of a crown regarding to castleid
 * @author evill33t
 */
public class CrownTable
{
	private static final List<Integer> CROWNLIST = Arrays.asList(6841, // Crown of the lord
		6834, // Innadril
		6835, // Dion
		6836, // Goddard
		6837, // Oren
		6838, // Gludio
		6839, // Giran
		6840, // Aden
		8182, // Rune
		8183 // Schuttgart
	);
	
	public static List<Integer> getCrownList()
	{
		return CROWNLIST;
	}
	
	public static int getCrownId(final int CastleId)
	{
		int CrownId = 0;
		switch (CastleId)
		{
			// Gludio
			case 1:
				CrownId = 6838;
				break;
			// Dion
			case 2:
				CrownId = 6835;
				break;
			// Giran
			case 3:
				CrownId = 6839;
				break;
			// Oren
			case 4:
				CrownId = 6837;
				break;
			// Aden
			case 5:
				CrownId = 6840;
				break;
			// Innadril
			case 6:
				CrownId = 6834;
				break;
			// Goddard
			case 7:
				CrownId = 6836;
				break;
			// Rune
			case 8:
				CrownId = 8182;
				break;
			// Schuttgart
			case 9:
				CrownId = 8183;
				break;
			default:
				CrownId = 0;
				break;
		}
		return CrownId;
	}
}
