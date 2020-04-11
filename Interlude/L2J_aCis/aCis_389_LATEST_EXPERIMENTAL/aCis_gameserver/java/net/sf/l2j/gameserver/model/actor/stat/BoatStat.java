package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.Boat;

public class BoatStat extends CreatureStat
{
	private int _moveSpeed;
	private int _rotationSpeed;
	
	public BoatStat(Boat boat)
	{
		super(boat);
	}
	
	@Override
	public float getMoveSpeed()
	{
		return _moveSpeed;
	}
	
	public final void setMoveSpeed(int speed)
	{
		_moveSpeed = speed;
	}
	
	public final int getRotationSpeed()
	{
		return _rotationSpeed;
	}
	
	public final void setRotationSpeed(int speed)
	{
		_rotationSpeed = speed;
	}
}