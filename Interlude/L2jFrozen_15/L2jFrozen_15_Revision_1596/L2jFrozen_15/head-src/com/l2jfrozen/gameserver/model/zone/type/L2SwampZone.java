package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

/**
 * another type of zone where your speed is changed
 * @author kerberos
 */
public class L2SwampZone extends L2ZoneType
{
	private int move_bonus;
	
	public L2SwampZone(final int id)
	{
		super(id);
		
		// Setup default speed reduce (in %)
		move_bonus = -50;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		if (name.equals("move_bonus"))
		{
			move_bonus = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_SWAMP, true);
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_SWAMP, false);
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	public int getMoveBonus()
	{
		return move_bonus;
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character character)
	{
	}
	
}
