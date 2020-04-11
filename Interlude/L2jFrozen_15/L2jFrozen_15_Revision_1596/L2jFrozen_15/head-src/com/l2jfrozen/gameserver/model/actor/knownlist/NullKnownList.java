package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;

public class NullKnownList extends ObjectKnownList
{
	
	/**
	 * @param activeObject
	 */
	public NullKnownList(final L2Object activeObject)
	{
		super(activeObject);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object, final L2Character dropper)
	{
		return false;
	}
	
	@Override
	public boolean addKnownObject(final L2Object object)
	{
		return false;
	}
	
	@Override
	public L2Object getActiveObject()
	{
		return super.getActiveObject();
	}
	
	@Override
	public int getDistanceToForgetObject(final L2Object object)
	{
		return 0;
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		return 0;
	}
	
	@Override
	public void removeAllKnownObjects()
	{
		// null
	}
	
	@Override
	public boolean removeKnownObject(final L2Object object)
	{
		return false;
	}
}
