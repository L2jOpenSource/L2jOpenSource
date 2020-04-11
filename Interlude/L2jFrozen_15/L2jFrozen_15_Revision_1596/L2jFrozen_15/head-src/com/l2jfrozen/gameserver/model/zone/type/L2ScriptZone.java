package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

/**
 * A scripted zone... Creation of such a zone should require somekind of jython script reference which can handle onEnter() / onExit()
 * @author durgus
 */
public class L2ScriptZone extends L2ZoneType
{
	public L2ScriptZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
	
}
