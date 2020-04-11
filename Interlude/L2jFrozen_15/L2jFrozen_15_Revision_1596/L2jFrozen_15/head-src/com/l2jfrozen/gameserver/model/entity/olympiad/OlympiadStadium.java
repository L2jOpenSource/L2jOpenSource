package com.l2jfrozen.gameserver.model.entity.olympiad;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.L2FastList;

/**
 * @author GodKratos
 */

class OlympiadStadium
{
	private boolean freeToUse = true;
	private final int[] coords = new int[3];
	private final L2FastList<L2PcInstance> spectators;
	
	public boolean isFreeToUse()
	{
		return freeToUse;
	}
	
	public void setStadiaBusy()
	{
		freeToUse = false;
	}
	
	public void setStadiaFree()
	{
		freeToUse = true;
		clearSpectators();
	}
	
	public int[] getCoordinates()
	{
		return coords;
	}
	
	public OlympiadStadium(final int x, final int y, final int z)
	{
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
		spectators = new L2FastList<>();
	}
	
	protected void addSpectator(final int id, final L2PcInstance spec, final boolean storeCoords)
	{
		spec.enterOlympiadObserverMode(getCoordinates()[0], getCoordinates()[1], getCoordinates()[2], id, storeCoords);
		spectators.add(spec);
	}
	
	protected L2FastList<L2PcInstance> getSpectators()
	{
		return spectators;
	}
	
	protected void removeSpectator(final L2PcInstance spec)
	{
		if (spectators != null && spectators.contains(spec))
		{
			spectators.remove(spec);
		}
	}
	
	private void clearSpectators()
	{
		spectators.clear();
	}
}
