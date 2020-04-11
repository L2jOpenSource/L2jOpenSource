package com.l2jfrozen.gameserver.model.actor.position;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.Point3D;

public class ObjectPosition
{
	private static final Logger LOGGER = Logger.getLogger(ObjectPosition.class);
	
	private final L2Object activeObject;
	private int heading = 0;
	private Point3D worldPosition;
	private L2WorldRegion worldRegion; // Object localization : Used for items/chars that are seen in the world
	private Boolean changingRegion = false;
	
	/**
	 * Instantiates a new object position.
	 * @param activeObject the active object
	 */
	public ObjectPosition(final L2Object activeObject)
	{
		this.activeObject = activeObject;
		setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
	}
	
	/**
	 * Set the x,y,z position of the L2Object and if necessary modify its worldRegion.<BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion != null</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Update position during and after movement, or after teleport</li><BR>
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setXYZ(final int x, final int y, final int z)
	{
		if (Config.ASSERT)
		{
			assert getWorldRegion() != null;
		}
		
		setWorldPosition(x, y, z);
		
		try
		{
			if (L2World.getInstance().getRegion(getWorldPosition()) != getWorldRegion())
			{
				updateWorldRegion();
			}
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.warn("Object Id at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
			
			if (getActiveObject() instanceof L2PcInstance)
			{
				// ((L2PcInstance)obj).deleteMe();
				((L2PcInstance) getActiveObject()).teleToLocation(0, 0, 0, false);
				((L2PcInstance) getActiveObject()).sendMessage("Error with your coords, Please ask a GM for help!");
				
			}
			else if (getActiveObject() instanceof L2Character)
			{
				getActiveObject().decayMe();
			}
			
		}
	}
	
	/**
	 * Set the x,y,z position of the L2Object and make it invisible.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldregion==null <I>(L2Object is invisible)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Create a Door</li>
	 * <li>Restore L2PcInstance</li><BR>
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setXYZInvisible(int x, int y, final int z)
	{
		if (Config.ASSERT)
		{
			assert getWorldRegion() == null;
		}
		if (x > L2World.MAP_MAX_X)
		{
			x = L2World.MAP_MAX_X - 5000;
		}
		
		if (x < L2World.MAP_MIN_X)
		{
			x = L2World.MAP_MIN_X + 5000;
		}
		
		if (y > L2World.MAP_MAX_Y)
		{
			y = L2World.MAP_MAX_Y - 5000;
		}
		
		if (y < L2World.MAP_MIN_Y)
		{
			y = L2World.MAP_MIN_Y + 5000;
		}
		
		setWorldPosition(x, y, z);
		getActiveObject().setIsVisible(false);
	}
	
	/**
	 * checks if current object changed its region, if so, update referencies.
	 */
	public void updateWorldRegion()
	{
		if (!getActiveObject().isVisible())
		{
			return;
		}
		
		L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());
		if (newRegion != getWorldRegion())
		{
			getWorldRegion().removeVisibleObject(getActiveObject());
			
			setWorldRegion(newRegion);
			
			// Add the L2Oject spawn to visibleObjects and if necessary to_allplayers of its L2WorldRegion
			getWorldRegion().addVisibleObject(getActiveObject());
		}
		
		newRegion = null;
	}
	
	/**
	 * Gets the active object.
	 * @return the active object
	 */
	public final L2Object getActiveObject()
	{
		return activeObject;
	}
	
	/**
	 * Gets the heading.
	 * @return the heading
	 */
	public final int getHeading()
	{
		return heading;
	}
	
	/**
	 * Sets the heading.
	 * @param value the new heading
	 */
	public final void setHeading(final int value)
	{
		heading = value;
	}
	
	/**
	 * Return the x position of the L2Object.
	 * @return the x
	 */
	public final int getX()
	{
		return getWorldPosition().getX();
	}
	
	/**
	 * Sets the x.
	 * @param value the new x
	 */
	public final void setX(final int value)
	{
		getWorldPosition().setX(value);
	}
	
	/**
	 * Return the y position of the L2Object.
	 * @return the y
	 */
	public final int getY()
	{
		return getWorldPosition().getY();
	}
	
	/**
	 * Sets the y.
	 * @param value the new y
	 */
	public final void setY(final int value)
	{
		getWorldPosition().setY(value);
	}
	
	/**
	 * Return the z position of the L2Object.
	 * @return the z
	 */
	public final int getZ()
	{
		return getWorldPosition().getZ();
	}
	
	/**
	 * Sets the z.
	 * @param value the new z
	 */
	public final void setZ(final int value)
	{
		getWorldPosition().setZ(value);
	}
	
	/**
	 * Gets the world position.
	 * @return the world position
	 */
	public final Point3D getWorldPosition()
	{
		if (worldPosition == null)
		{
			worldPosition = new Point3D(0, 0, 0);
		}
		
		return worldPosition;
	}
	
	/**
	 * Sets the world position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public final void setWorldPosition(final int x, final int y, final int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}
	
	/**
	 * Sets the world position.
	 * @param newPosition the new world position
	 */
	public final void setWorldPosition(final Point3D newPosition)
	{
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}
	
	/**
	 * Gets the world region.
	 * @return the world region
	 */
	public final L2WorldRegion getWorldRegion()
	{
		synchronized (changingRegion)
		{
			changingRegion = false;
			return worldRegion;
		}
	}
	
	/**
	 * Sets the world region.
	 * @param value the new world region
	 */
	public final void setWorldRegion(final L2WorldRegion value)
	{
		synchronized (changingRegion)
		{
			changingRegion = true;
			worldRegion = value;
		}
	}
}
