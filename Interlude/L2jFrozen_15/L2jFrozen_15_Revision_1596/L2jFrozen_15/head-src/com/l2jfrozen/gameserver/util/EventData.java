package com.l2jfrozen.gameserver.util;

import java.util.LinkedList;
import java.util.List;

/**
 * @author SarEvoK
 */
public class EventData
{
	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventKarma;
	public int eventPvpKills;
	public int eventPkKills;
	public String eventTitle;
	public List<String> kills = new LinkedList<>();
	public boolean eventSitForced = false;
	
	public EventData(final int pEventX, final int pEventY, final int pEventZ, final int pEventkarma, final int pEventpvpkills, final int pEventpkkills, final String pEventTitle, final List<String> pKills, final boolean pEventSitForced)
	{
		eventX = pEventX;
		eventY = pEventY;
		eventZ = pEventZ;
		eventKarma = pEventkarma;
		eventPvpKills = pEventpvpkills;
		eventPkKills = pEventpkkills;
		eventTitle = pEventTitle;
		kills = pKills;
		eventSitForced = pEventSitForced;
	}
}
