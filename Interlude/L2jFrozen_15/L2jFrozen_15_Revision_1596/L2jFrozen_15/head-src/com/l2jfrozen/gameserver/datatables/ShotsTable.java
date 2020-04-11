package com.l2jfrozen.gameserver.datatables;

import java.util.Arrays;
import java.util.List;

/**
 * @author ReynalDev
 */
public class ShotsTable
{
	private static ShotsTable instance = null;
	private static final List<Integer> SHOT_IDS = Arrays.asList(1463, // Soulshot: D-grade
		1464, // Soulshot: C-grade
		1465, // Soulshot: B-grade
		1466, // Soulshot: A-grade
		1467, // Soulshot: S-grade
		1835, // Soulshot: No Grade
		2509, // Spiritshot: No Grade
		2510, // Spiritshot: D-grade
		2511, // Spiritshot: C-grade
		2512, // Spiritshot: B-grade
		2513, // Spiritshot: A-grade
		2514, // Spiritshot: S-grade
		3947, // Blessed Spiritshot: No Grade
		3948, // Blessed Spiritshot: D-Grade
		3949, // Blessed Spiritshot: C-Grade
		3950, // Blessed Spiritshot: B-Grade
		3951, // Blessed Spiritshot: A-Grade
		3952, // Blessed Spiritshot: S Grade
		5789, // Soulshot: No Grade for Beginners
		5790 // Spiritshot: No Grade for Beginners
	);
	
	public static ShotsTable getInstance()
	{
		if (instance == null)
		{
			return new ShotsTable();
		}
		
		return instance;
	}
	
	public boolean isShot(int itemId)
	{
		return SHOT_IDS.contains(itemId);
	}
}
