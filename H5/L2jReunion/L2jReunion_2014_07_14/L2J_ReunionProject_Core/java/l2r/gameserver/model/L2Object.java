/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastMap;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.ShotType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.ActionHandler;
import l2r.gameserver.handler.ActionShiftHandler;
import l2r.gameserver.handler.IActionHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.knownlist.ObjectKnownList;
import l2r.gameserver.model.actor.poly.ObjectPoly;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.interfaces.ILocational;
import l2r.gameserver.model.interfaces.INamable;
import l2r.gameserver.model.interfaces.IPositionable;
import l2r.gameserver.model.interfaces.IUniqueId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.DeleteObject;
import l2r.gameserver.network.serverpackets.ExSendUIEvent;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.util.Util;

/**
 * Mother class of all objects in the world which ones is it possible to interact (PC, NPC, Item...)<BR>
 * <BR>
 * L2Object :<BR>
 * <BR>
 * <li>L2Character</li> <li>L2ItemInstance</li>
 */
public abstract class L2Object implements IIdentifiable, INamable, IUniqueId, IPositionable
{
	private boolean _isVisible;
	private boolean _isInvisible;
	private ObjectKnownList _knownList;
	/** Name */
	private String _name;
	/** Object ID */
	private int _objectId;
	/** World Region */
	private L2WorldRegion _worldRegion;
	/** Instance type */
	private InstanceType _instanceType = null;
	private volatile Map<String, Object> _scripts;
	/** X coordinate */
	private final AtomicInteger _x = new AtomicInteger(0);
	/** Y coordinate */
	private final AtomicInteger _y = new AtomicInteger(0);
	/** Z coordinate */
	private final AtomicInteger _z = new AtomicInteger(0);
	/** Orientation */
	private final AtomicInteger _heading = new AtomicInteger(0);
	/** Instance id of object. 0 - Global */
	private final AtomicInteger _instanceId = new AtomicInteger(0);
	
	public L2Object(int objectId)
	{
		setInstanceType(InstanceType.L2Object);
		_objectId = objectId;
		initKnownList();
	}
	
	/**
	 * Gets the instance type of object.
	 * @return the instance type
	 */
	public final InstanceType getInstanceType()
	{
		return _instanceType;
	}
	
	/**
	 * Sets the instance type.
	 * @param newInstanceType the instance type to set
	 */
	protected final void setInstanceType(InstanceType newInstanceType)
	{
		_instanceType = newInstanceType;
	}
	
	/**
	 * Verifies if object is of any given instance types.
	 * @param instanceTypes the instance types to verify
	 * @return {@code true} if object is of any given instance types, {@code false} otherwise
	 */
	public final boolean isInstanceTypes(InstanceType... instanceTypes)
	{
		return _instanceType.isTypes(instanceTypes);
	}
	
	public void onAction(L2PcInstance player)
	{
		onAction(player, true);
	}
	
	public void onAction(L2PcInstance player, boolean interact)
	{
		IActionHandler handler = ActionHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, interact);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(L2PcInstance player)
	{
		IActionHandler handler = ActionShiftHandler.getInstance().getHandler(getInstanceType());
		if (handler != null)
		{
			handler.action(player, this, true);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Do Nothing.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li>L2GuardInstance : Set the home location of its L2GuardInstance</li> <li>L2Attackable : Reset the Spoiled flag</li><BR>
	 * <BR>
	 */
	public void onSpawn()
	{
		
	}
	
	public void decayMe()
	{
		assert getWorldRegion() != null;
		
		L2WorldRegion reg = getWorldRegion();
		
		synchronized (this)
		{
			_isVisible = false;
			setWorldRegion(null);
		}
		
		// this can synchronize on others instances, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2Object from the world
		L2World.getInstance().removeVisibleObject(this, reg);
		L2World.getInstance().removeObject(this);
	}
	
	public void refreshID()
	{
		L2World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	public final boolean spawnMe()
	{
		assert (getWorldRegion() == null) && (getLocation().getX() != 0) && (getLocation().getY() != 0) && (getLocation().getZ() != 0);
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			setWorldRegion(L2World.getInstance().getRegion(getLocation()));
			
			// Add the L2Object spawn in the _allobjects of L2World
			L2World.getInstance().storeObject(this);
			
			// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			getWorldRegion().addVisibleObject(this);
		}
		
		// this can synchronize on others instances, so it's out of synchronized, to avoid deadlocks
		// Add the L2Object spawn in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getWorldRegion());
		
		onSpawn();
		
		return true;
	}
	
	public final void spawnMe(int x, int y, int z)
	{
		assert getWorldRegion() == null;
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_isVisible = true;
			
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
			
			setXYZ(x, y, z);
			setWorldRegion(L2World.getInstance().getRegion(getLocation()));
			
			// Add the L2Object spawn in the _allobjects of L2World
		}
		
		L2World.getInstance().storeObject(this);
		
		// these can synchronize on others instances, so they're out of
		// synchronized, to avoid deadlocks
		
		// Add the L2Object spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
		getWorldRegion().addVisibleObject(this);
		
		// Add the L2Object spawn in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getWorldRegion());
		
		onSpawn();
	}
	
	/**
	 * Verify if object can be attacked.
	 * @return {@code true} if object can be attacked, {@code false} otherwise
	 */
	public boolean canBeAttacked()
	{
		return false;
	}
	
	public abstract boolean isAutoAttackable(L2Character attacker);
	
	public boolean isMarker()
	{
		return false;
	}
	
	/**
	 * Return the visibility state of the L2Object. <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Object is visible if <B>__IsVisible</B>=true and <B>_worldregion</B>!=null <BR>
	 * <BR>
	 * @return
	 */
	public final boolean isVisible()
	{
		return getWorldRegion() != null;
	}
	
	public final void setIsVisible(boolean value)
	{
		_isVisible = value;
		if (!_isVisible)
		{
			setWorldRegion(null);
		}
	}
	
	public void toggleVisible()
	{
		if (isVisible())
		{
			decayMe();
		}
		else
		{
			spawnMe();
		}
	}
	
	public ObjectKnownList getKnownList()
	{
		return _knownList;
	}
	
	/**
	 * Initializes the KnownList of the L2Object, is overwritten in classes that require a different knownlist Type. Removes the need for instanceof checks.
	 */
	public void initKnownList()
	{
		_knownList = new ObjectKnownList(this);
	}
	
	public final void setKnownList(ObjectKnownList value)
	{
		_knownList = value;
	}
	
	@Override
	public final String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	@Override
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final ObjectPoly getPoly()
	{
		final ObjectPoly poly = getScript(ObjectPoly.class);
		return (poly == null) ? addScript(new ObjectPoly(this)) : poly;
	}
	
	public abstract void sendInfo(L2PcInstance activeChar);
	
	public void sendPacket(L2GameServerPacket mov)
	{
	}
	
	public void sendPacket(SystemMessageId id)
	{
	}
	
	public L2PcInstance getActingPlayer()
	{
		return null;
	}
	
	/**
	 * Verify if object is instance of L2Attackable.
	 * @return {@code true} if object is instance of L2Attackable, {@code false} otherwise
	 */
	public boolean isAttackable()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Character.
	 * @return {@code true} if object is instance of L2Character, {@code false} otherwise
	 */
	public boolean isCharacter()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2DoorInstance.
	 * @return {@code true} if object is instance of L2DoorInstance, {@code false} otherwise
	 */
	public boolean isDoor()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2MonsterInstance.
	 * @return {@code true} if object is instance of L2MonsterInstance, {@code false} otherwise
	 */
	public boolean isMonster()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Npc.
	 * @return {@code true} if object is instance of L2Npc, {@code false} otherwise
	 */
	public boolean isNpc()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2PetInstance.
	 * @return {@code true} if object is instance of L2PetInstance, {@code false} otherwise
	 */
	public boolean isPet()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2PcInstance.
	 * @return {@code true} if object is instance of L2PcInstance, {@code false} otherwise
	 */
	public boolean isPlayer()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Playable.
	 * @return {@code true} if object is instance of L2Playable, {@code false} otherwise
	 */
	public boolean isPlayable()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2ServitorInstance.
	 * @return {@code true} if object is instance of L2ServitorInstance, {@code false} otherwise
	 */
	public boolean isServitor()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2Summon.
	 * @return {@code true} if object is instance of L2Summon, {@code false} otherwise
	 */
	public boolean isSummon()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2TrapInstance.
	 * @return {@code true} if object is instance of L2TrapInstance, {@code false} otherwise
	 */
	public boolean isTrap()
	{
		return false;
	}
	
	/**
	 * Verify if object is instance of L2ItemInstance.
	 * @return {@code true} if object is instance of L2ItemInstance, {@code false} otherwise
	 */
	public boolean isItem()
	{
		return false;
	}
	
	/**
	 * @return {@code true} if object Npc Walker or Vehicle
	 */
	public boolean isWalker()
	{
		return false;
	}
	
	public boolean isRunner()
	{
		return false;
	}
	
	/**
	 * @return {@code true} if object Can be targeted
	 */
	public boolean isTargetable()
	{
		return true;
	}
	
	/**
	 * Check if the object is in the given zone Id.
	 * @param zone the zone Id to check
	 * @return {@code true} if the object is in that zone Id
	 */
	public boolean isInsideZone(ZoneIdType zone)
	{
		return false;
	}
	
	/**
	 * Check if current object has charged shot.
	 * @param type of the shot to be checked.
	 * @return {@code true} if the object has charged shot
	 */
	public boolean isChargedShot(ShotType type)
	{
		return false;
	}
	
	/**
	 * Charging shot into the current object.
	 * @param type of the shot to be charged.
	 * @param charged
	 */
	public void setChargedShot(ShotType type, boolean charged)
	{
	}
	
	/**
	 * Try to recharge a shot.
	 * @param physical skill are using Soul shots.
	 * @param magical skill are using Spirit shots.
	 */
	public void rechargeShots(boolean physical, boolean magical)
	{
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	public final <T> T addScript(T script)
	{
		if (_scripts == null)
		{
			// Double-checked locking
			synchronized (this)
			{
				if (_scripts == null)
				{
					_scripts = new FastMap<String, Object>().shared();
				}
			}
		}
		_scripts.put(script.getClass().getName(), script);
		return script;
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T removeScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.remove(script.getName());
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getScript(Class<T> script)
	{
		if (_scripts == null)
		{
			return null;
		}
		return (T) _scripts.get(script.getName());
	}
	
	protected void badCoords()
	{
		if (isCharacter())
		{
			decayMe();
		}
		else if (isPlayer())
		{
			((L2Character) this).teleToLocation(new Location(0, 0, 0), false);
			((L2Character) this).sendMessage("Error with your coords, Please ask a GM for help!");
		}
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		assert getWorldRegion() == null;
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
		
		setXYZ(x, y, z);
		setIsVisible(false);
	}
	
	public final void setLocationInvisible(ILocational loc)
	{
		setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public void updateWorldRegion()
	{
		if (!isVisible())
		{
			return;
		}
		
		L2WorldRegion newRegion = L2World.getInstance().getRegion(getLocation());
		if (newRegion != getWorldRegion())
		{
			getWorldRegion().removeVisibleObject(this);
			
			setWorldRegion(newRegion);
			
			// Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			getWorldRegion().addVisibleObject(this);
		}
	}
	
	public final L2WorldRegion getWorldRegion()
	{
		return _worldRegion;
	}
	
	public void setWorldRegion(L2WorldRegion value)
	{
		if ((getWorldRegion() != null) && isCharacter()) // confirm revalidation of old region's zones
		{
			if (value != null)
			{
				getWorldRegion().revalidateZones((L2Character) this); // at world region change
			}
			else
			{
				getWorldRegion().removeFromZones((L2Character) this); // at world region change
			}
		}
		
		_worldRegion = value;
	}
	
	/**
	 * Gets the X coordinate.
	 * @return the X coordinate
	 */
	@Override
	public int getX()
	{
		return _x.get();
	}
	
	/**
	 * Gets the Y coordinate.
	 * @return the Y coordinate
	 */
	@Override
	public int getY()
	{
		return _y.get();
	}
	
	/**
	 * Gets the Z coordinate.
	 * @return the Z coordinate
	 */
	@Override
	public int getZ()
	{
		return _z.get();
	}
	
	/**
	 * Gets the heading.
	 * @return the heading
	 */
	@Override
	public int getHeading()
	{
		return _heading.get();
	}
	
	/**
	 * Gets the instance ID.
	 * @return the instance ID
	 */
	@Override
	public int getInstanceId()
	{
		return _instanceId.get();
	}
	
	/**
	 * Gets the location object.
	 * @return the location object
	 */
	@Override
	public Location getLocation()
	{
		return new Location(getX(), getY(), getZ(), getHeading(), getInstanceId());
	}
	
	/**
	 * Sets the X coordinate
	 * @param newX the X coordinate
	 */
	@Override
	public void setX(int newX)
	{
		_x.set(newX);
	}
	
	/**
	 * Sets the Y coordinate
	 * @param newY the Y coordinate
	 */
	@Override
	public void setY(int newY)
	{
		_y.set(newY);
	}
	
	/**
	 * Sets the Z coordinate
	 * @param newZ the Z coordinate
	 */
	@Override
	public void setZ(int newZ)
	{
		_z.set(newZ);
	}
	
	/**
	 * Sets the x, y, z coordinate.
	 * @param newX the X coordinate
	 * @param newY the Y coordinate
	 * @param newZ the Z coordinate
	 */
	@Override
	public final void setXYZ(int newX, int newY, int newZ)
	{
		assert getWorldRegion() != null;
		
		setX(newX);
		setY(newY);
		setZ(newZ);
		
		try
		{
			if (L2World.getInstance().getRegion(getLocation()) != getWorldRegion())
			{
				updateWorldRegion();
			}
		}
		catch (Exception e)
		{
			badCoords();
		}
	}
	
	/**
	 * Sets the x, y, z coordinate.
	 * @param loc the location object
	 */
	@Override
	public void setXYZ(ILocational loc)
	{
		setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Sets heading of object.
	 * @param newHeading the new heading
	 */
	@Override
	public void setHeading(int newHeading)
	{
		_heading.set(newHeading);
	}
	
	/**
	 * Sets the instance ID of object.<br>
	 * 0 - Global<br>
	 * TODO: Add listener here.
	 * @param instanceId the ID of the instance
	 */
	@Override
	public void setInstanceId(int instanceId)
	{
		if ((instanceId < 0) || (getInstanceId() == instanceId))
		{
			return;
		}
		
		Instance oldI = InstanceManager.getInstance().getInstance(getInstanceId());
		Instance newI = InstanceManager.getInstance().getInstance(instanceId);
		if (newI == null)
		{
			return;
		}
		
		if (isPlayer())
		{
			final L2PcInstance player = getActingPlayer();
			if ((getInstanceId() > 0) && (oldI != null))
			{
				oldI.removePlayer(getObjectId());
				if (oldI.isShowTimer())
				{
					sendInstanceUpdate(oldI, true);
				}
			}
			if (instanceId > 0)
			{
				newI.addPlayer(getObjectId());
				if (newI.isShowTimer())
				{
					sendInstanceUpdate(newI, false);
				}
			}
			if (player.hasSummon())
			{
				player.getSummon().setInstanceId(instanceId);
			}
		}
		else if (isNpc())
		{
			final L2Npc npc = (L2Npc) this;
			if ((getInstanceId() > 0) && (oldI != null))
			{
				oldI.removeNpc(npc);
			}
			if (instanceId > 0)
			{
				newI.addNpc(npc);
			}
		}
		
		_instanceId.set(instanceId);
		if (_isVisible && (_knownList != null))
		{
			// We don't want some ugly looking disappear/appear effects, so don't update
			// the knownlist here, but players usually enter instancezones through teleporting
			// and the teleport will do the revalidation for us.
			if (!isPlayer())
			{
				decayMe();
				spawnMe();
			}
		}
	}
	
	/**
	 * Sets location of object.
	 * @param loc the location object
	 */
	@Override
	public void setLocation(Location loc)
	{
		_x.set(loc.getX());
		_y.set(loc.getY());
		_z.set(loc.getZ());
		_heading.set(loc.getHeading());
		_instanceId.set(loc.getInstanceId());
	}
	
	/**
	 * Calculates distance between this L2Object and given x, y , z.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @param z the Z coordinate
	 * @param includeZAxis if {@code true} Z axis will be included
	 * @param squared if {@code true} return will be squared
	 * @return distance between object and given x, y, z.
	 */
	public final double calculateDistance(int x, int y, int z, boolean includeZAxis, boolean squared)
	{
		final double distance = Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2) + (includeZAxis ? Math.pow(z - getZ(), 2) : 0);
		return (squared) ? distance : Math.sqrt(distance);
	}
	
	/**
	 * Calculates distance between this L2Object and given location.
	 * @param loc the location object
	 * @param includeZAxis if {@code true} Z axis will be included
	 * @param squared if {@code true} return will be squared
	 * @return distance between object and given location.
	 */
	public final double calculateDistance(ILocational loc, boolean includeZAxis, boolean squared)
	{
		return calculateDistance(loc.getX(), loc.getY(), loc.getZ(), includeZAxis, squared);
	}
	
	/**
	 * Calculates the angle in degrees from this object to the given object.<br>
	 * The return value can be described as how much this object has to turn<br>
	 * to have the given object directly in front of it.
	 * @param target the object to which to calculate the angle
	 * @return the angle this object has to turn to have the given object in front of it
	 */
	public final double calculateDirectionTo(ILocational target)
	{
		int heading = Util.calculateHeadingFrom(this, target) - this.getHeading();
		if (heading < 0)
		{
			heading = 65535 + heading;
		}
		return Util.convertHeadingToDegree(heading);
	}
	
	/**
	 * Sends an instance update for player.
	 * @param instance the instance to update
	 * @param hide if {@code true} hide the player
	 */
	private final void sendInstanceUpdate(Instance instance, boolean hide)
	{
		final int startTime = (int) ((System.currentTimeMillis() - instance.getInstanceStartTime()) / 1000);
		final int endTime = (int) ((instance.getInstanceEndTime() - instance.getInstanceStartTime()) / 1000);
		if (instance.isTimerIncrease())
		{
			sendPacket(new ExSendUIEvent(getActingPlayer(), hide, true, startTime, endTime, instance.getTimerText()));
		}
		else
		{
			sendPacket(new ExSendUIEvent(getActingPlayer(), hide, false, endTime - startTime, 0, instance.getTimerText()));
		}
	}
	
	/**
	 * @return {@code true} if this object is invisible, {@code false} otherwise.
	 */
	public boolean isInvisible()
	{
		return _isInvisible;
	}
	
	/**
	 * Sets this object as invisible or not
	 * @param invis
	 */
	public void setInvisible(boolean invis)
	{
		_isInvisible = invis;
		if (invis)
		{
			final DeleteObject deletePacket = new DeleteObject(this);
			for (L2Object obj : getKnownList().getKnownObjects().values())
			{
				if ((obj != null) && obj.isPlayer())
				{
					final L2PcInstance player = obj.getActingPlayer();
					if (!isVisibleFor(player))
					{
						obj.sendPacket(deletePacket);
					}
				}
			}
		}
		
		// Broadcast information regarding the object to those which are suppose to see.
		broadcastInfo();
	}
	
	/**
	 * @param player
	 * @return {@code true} if player can see an invisible object if it's invisible, {@code false} otherwise.
	 */
	public boolean isVisibleFor(L2PcInstance player)
	{
		return !isInvisible() || player.canOverrideCond(PcCondOverride.SEE_ALL_PLAYERS);
	}
	
	/**
	 * Broadcasts describing info to known players.
	 */
	public void broadcastInfo()
	{
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if ((obj != null) && obj.isPlayer() && isVisibleFor(obj.getActingPlayer()))
			{
				sendInfo(obj.getActingPlayer());
			}
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return ((obj instanceof L2Object) && (((L2Object) obj).getObjectId() == getObjectId()));
	}
	
	@Override
	public String toString()
	{
		return (getClass().getSimpleName() + ":" + getName() + "[" + getObjectId() + "]");
	}
}