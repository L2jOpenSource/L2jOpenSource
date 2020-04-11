package com.l2jfrozen.gameserver.model.actor.position;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * This class permit to pass (x, y, z, heading) position data to method.
 */
public final class L2CharPosition
{
	/** The heading. */
	public final int x, y, z, heading;
	
	/**
	 * Constructor of L2CharPosition.<BR>
	 * <BR>
	 * @param pX       the p x
	 * @param pY       the p y
	 * @param pZ       the p z
	 * @param pHeading the heading
	 */
	public L2CharPosition(final int pX, final int pY, final int pZ, final int pHeading)
	{
		x = pX;
		y = pY;
		z = pZ;
		heading = pHeading;
	}
	
	/**
	 * Instantiates a new l2 char position.
	 * @param actor the actor
	 */
	public L2CharPosition(final L2Character actor)
	{
		x = actor.getX();
		y = actor.getY();
		z = actor.getZ();
		heading = actor.getHeading();
	}
	
}
