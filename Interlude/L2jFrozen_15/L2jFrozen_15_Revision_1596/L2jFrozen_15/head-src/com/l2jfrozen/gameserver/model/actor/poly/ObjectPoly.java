package com.l2jfrozen.gameserver.model.actor.poly;

import com.l2jfrozen.gameserver.model.L2Object;

public class ObjectPoly
{
	private final L2Object activeObject;
	private int polyId;
	private String polyType;
	
	public ObjectPoly(final L2Object activeObject)
	{
		this.activeObject = activeObject;
	}
	
	public void setPolyInfo(final String polyType, final String polyId)
	{
		setPolyId(Integer.parseInt(polyId));
		setPolyType(polyType);
	}
	
	public final L2Object getActiveObject()
	{
		return activeObject;
	}
	
	public final boolean isMorphed()
	{
		return getPolyType() != null;
	}
	
	public final int getPolyId()
	{
		return polyId;
	}
	
	public final void setPolyId(final int value)
	{
		polyId = value;
	}
	
	public final String getPolyType()
	{
		return polyType;
	}
	
	public final void setPolyType(final String value)
	{
		polyType = value;
	}
}
