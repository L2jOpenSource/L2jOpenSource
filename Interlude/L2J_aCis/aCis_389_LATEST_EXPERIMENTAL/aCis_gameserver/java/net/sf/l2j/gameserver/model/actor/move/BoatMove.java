package net.sf.l2j.gameserver.model.actor.move;

import net.sf.l2j.gameserver.enums.actors.MoveType;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.gameserver.network.serverpackets.VehicleStarted;

public class BoatMove extends CreatureMove
{
	private BoatLocation[] _currentPath;
	private int _pathIndex;
	
	public BoatMove(Boat boat)
	{
		super(boat);
		
		// Boats simply don't bother about other movements.
		addMoveType(MoveType.FLY);
	}
	
	private Boat getBoat()
	{
		return (Boat) _creature;
	}
	
	@Override
	public void stop()
	{
		if (_task == null)
			return;
		
		_task.cancel(false);
		_task = null;
		
		getBoat().broadcastPacket(new VehicleStarted(getBoat(), 0));
		getBoat().broadcastPacket(new VehicleInfo(getBoat()));
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		return false;
	}
	
	@Override
	public boolean updatePosition()
	{
		final boolean result = super.updatePosition();
		
		// Refresh all Players passengers positions.
		for (Player player : getBoat().getPassengers())
		{
			if (player.getBoat() == getBoat())
			{
				player.setXYZ(getBoat());
				player.revalidateZone(false);
			}
		}
		return result;
	}
	
	public void onArrival()
	{
		// Increment the path index.
		_pathIndex++;
		
		// We are still on path, move to the next BoatLocation.
		if (_pathIndex < _currentPath.length)
		{
			moveBoatTo(_currentPath[_pathIndex]);
			return;
		}
		
		// Stop the Boat.
		stop();
		
		// Renew Boat entrances when definitively stopped.
		getBoat().renewBoatEntrances();
		
		// We are out of path, continue to process the engine.
		getBoat().runEngine(10);
	}
	
	/**
	 * Move the {@link Boat} related to this {@link BoatMove} using a given {@link BoatLocation}.
	 * @param loc : The BoatLocation we send the Boat to.
	 */
	private void moveBoatTo(BoatLocation loc)
	{
		// Feed Boat move speed and rotation based on BoatLocation parameter.
		if (loc.getMoveSpeed() > 0)
			getBoat().getStat().setMoveSpeed(loc.getMoveSpeed());
		if (loc.getRotationSpeed() > 0)
			getBoat().getStat().setRotationSpeed(loc.getRotationSpeed());
		
		// Set the current x/y.
		_xAccurate = getBoat().getX();
		_yAccurate = getBoat().getY();
		
		// Set the destination.
		_destination.set(loc);
		
		// Reset the variables.
		_moveTimeStamp = 0;
		
		// Set the heading.
		getBoat().getPosition().setHeadingTo(loc);
		
		registerMoveTask();
		
		// Broadcast the movement (angle change, speed change, destination).
		getBoat().broadcastPacket(new VehicleDeparture(getBoat()));
	}
	
	/**
	 * Feed this {@link BoatMove} with a {@link BoatLocation} array, then trigger the {@link Boat} movement using the first BoatLocation of the array.
	 * @param path : The BoatLocation array used as path.
	 */
	public void executePath(BoatLocation[] path)
	{
		// Initialize values.
		_pathIndex = 0;
		_currentPath = path;
		
		// Move the Boat to first encountered BoatLocation.
		moveBoatTo(_currentPath[0]);
		
		// Broadcast the starting movement.
		getBoat().broadcastPacket(new VehicleStarted(getBoat(), 1));
	}
}