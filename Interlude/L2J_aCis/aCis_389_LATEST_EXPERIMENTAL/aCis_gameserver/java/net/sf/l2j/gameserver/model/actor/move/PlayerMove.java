package net.sf.l2j.gameserver.model.actor.move;

import java.awt.Color;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.MoveType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.WaterZone;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocation;

/**
 * This class groups all movement data related to a {@link Player}.
 */
public class PlayerMove extends CreatureMove
{
	protected final ExServerPrimitive _packet = new ExServerPrimitive("Debug");
	
	public PlayerMove(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public ExServerPrimitive getDebugMovePacket()
	{
		return _packet;
	}
	
	@Override
	public void moveToPawn(WorldObject pawn, int offset)
	{
		// Get the movement speed of the Creature.
		final float speed = _creature.getStat().getMoveSpeed();
		if (speed <= 0 || _creature.isMovementDisabled())
		{
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		int tx = pawn.getX();
		int ty = pawn.getY();
		int tz = pawn.getZ();
		
		// If a movement already exists with the exact destination and offset, don't bother calculate anything.
		if (_task != null && _destination.equals(tx, ty, tz) && _offset == offset)
			return;
		
		// Get the current position of the Creature.
		final int ox = _creature.getX();
		final int oy = _creature.getY();
		final int oz = _creature.getZ();
		
		// If already on the point, call ARRIVED event.
		if (_creature.isIn3DRadius(pawn, offset))
		{
			_creature.getAI().notifyEvent(AiEventType.ARRIVED, false, null);
			return;
		}
		
		// Set the current x/y.
		_xAccurate = ox;
		_yAccurate = oy;
		
		// Initialize variables.
		_geoPath.clear();
		
		// Set the pawn and offset.
		_pawn = pawn;
		_offset = offset;
		
		// Retain some informations fur future use.
		final MoveType moveType = getMoveType();
		final int oDestX = tx;
		final int oDestY = ty;
		final int oDestZ = tz;
		
		Location moveOk = Location.DUMMY_LOC;
		boolean isPathClear = true;
		
		// SWIM and FLY don't bother with Z checks.
		if (moveType == MoveType.GROUND) // TODO handle canMoveToTargetLoc for SWIM and FLY to avoid going through walls.
		{
			moveOk = GeoEngine.getInstance().getValidLocation(ox, oy, oz, tx, ty, tz);
			isPathClear = MathUtil.checkIfInRange(offset, pawn, moveOk, true);
			if (!isPathClear)
			{
				tx = moveOk.getX();
				ty = moveOk.getY();
				tz = moveOk.getZ();
			}
		}
		
		// Draw a debug of this movement if the config is activated.
		if (Config.DEBUG_MOVEMENT)
		{
			// Reset the packet lines and points.
			_packet.reset();
			
			// Add a WHITE line corresponding to the initial click release.
			_packet.addLine("MoveToPawn (" + _offset + "): " + oDestX + " " + oDestY + " " + oDestZ, Color.WHITE, true, ox, oy, oz, oDestX, oDestY, oDestZ);
			
			// Add a PURPLE line corresponding to the first encountered obstacle, if any.
			if (moveOk != Location.DUMMY_LOC)
				_packet.addLine(Color.MAGENTA, ox, oy, oz, moveOk);
			
			_creature.sendMessage("Moving from " + ox + " " + oy + " " + oz + " to " + tx + " " + ty + " " + tz);
		}
		
		// Set the destination.
		_destination.set(tx, ty, tz);
		
		// Calculate the heading.
		_creature.getPosition().setHeadingTo(tx, ty);
		
		// Start a follow task if no path found.
		if (!isPathClear)
		{
			// Start a new follow, on the pawn with correct offset.
			startFollow(_pawn, _offset);
		}
		else
		{
			registerMoveTask();
			
			// Broadcast the good packet giving the situation.
			_creature.broadcastPacket(findPacketToSend());
		}
	}
	
	@Override
	public void moveToLocation(int tx, int ty, int tz)
	{
		// If a movement already exists with the exact destination, don't bother calculate anything.
		if (_task != null && _destination.equals(tx, ty, tz))
			return;
		
		// Get the movement speed of the Creature.
		final float speed = _creature.getStat().getMoveSpeed();
		if (speed <= 0 || _creature.isMovementDisabled())
		{
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the current position of the Creature.
		final int ox = _creature.getX();
		final int oy = _creature.getY();
		final int oz = _creature.getZ();
		
		// If no distance to go through, the movement is canceled.
		if (ox == tx && oy == ty && oz == tz)
		{
			_creature.getAI().notifyEvent(AiEventType.ARRIVED, false, null);
			return;
		}
		
		// Set the current x/y.
		_xAccurate = ox;
		_yAccurate = oy;
		
		// Initialize variables.
		_geoPath.clear();
		_pawn = null;
		_offset = 0;
		
		// Retain some informations fur future use.
		final MoveType moveType = getMoveType();
		final int oDestX = tx;
		final int oDestY = ty;
		final int oDestZ = tz;
		
		// While flying we stop at the first encountered obstacle directly. No geopath involved.
		if (moveType == MoveType.FLY)
		{
			// TODO : needs to implement canFlyToTargetLoc
		}
		else
		{
			final boolean canBypassGeodata = (_creature instanceof Player && ((Player) _creature).getBoat() != null) || moveType != MoveType.GROUND;
			if (!canBypassGeodata)
			{
				// Calculate the path.
				final Location loc = calculatePath(ox, oy, oz, tx, ty, tz);
				if (loc != null)
				{
					tx = loc.getX();
					ty = loc.getY();
					tz = loc.getZ();
				}
			}
		}
		
		// Draw a debug of this movement if the config is activated.
		if (Config.DEBUG_MOVEMENT)
		{
			// Reset the packet lines and points.
			_packet.reset();
			
			// Add a WHITE line corresponding to the initial click release.
			_packet.addLine("MoveToLocation: " + oDestX + " " + oDestY + " " + oDestZ, Color.WHITE, true, ox, oy, oz, oDestX, oDestY, oDestZ);
			
			// Add BLUE lines corresponding to the geo path, if any. Add a single BLUE line if no geoPath encountered.
			if (!_geoPath.isEmpty())
			{
				// Add manually a segment, since poll() was executed.
				_packet.addLine("Segment #1", Color.ORANGE, true, ox, oy, oz, tx, ty, tz);
				
				// Initialize a Location based on target location.
				final Location curPos = new Location(tx, ty, tz);
				int i = 2;
				
				// Iterate geo path.
				for (final Location geoPos : _geoPath)
				{
					// Draw a blue line going from initial to geo path.
					_packet.addLine("Segment #" + i, Color.ORANGE, true, curPos, geoPos);
					
					// Set current path as geo path ; the draw will start from here.
					curPos.set(geoPos);
					i++;
				}
			}
			else
				_packet.addLine("No geopath", Color.ORANGE, true, ox, oy, oz, tx, ty, tz);
			
			_creature.sendMessage("Moving from " + ox + " " + oy + " " + oz + " to " + tx + " " + ty + " " + tz);
		}
		
		// Set the destination.
		_destination.set(tx, ty, tz);
		
		// Calculate the heading.
		_creature.getPosition().setHeadingTo(tx, ty);
		
		registerMoveTask();
		
		// Broadcast MoveToLocation packet to known objects.
		_creature.broadcastPacket(new MoveToLocation(_creature));
	}
	
	@Override
	public boolean updatePosition()
	{
		if (_task == null || !_creature.isVisible())
			return true;
		
		final MoveType type = getMoveType();
		final boolean canBypassZCheck = (_creature instanceof Player && ((Player) _creature).getBoat() != null) || type == MoveType.FLY;
		
		// Increment the timestamp.
		_moveTimeStamp++;
		
		final int curX = _creature.getX();
		final int curY = _creature.getY();
		final int curZ = _creature.getZ();
		
		if (type == MoveType.GROUND)
			_destination.setZ(GeoEngine.getInstance().getHeight(_destination));
		
		final double dx = _destination.getX() - _xAccurate;
		final double dy = _destination.getY() - _yAccurate;
		final double dz = _destination.getZ() - curZ;
		
		// We use Z for delta calculation only if different of GROUND MoveType.
		double leftDistance = dx * dx + dy * dy;
		leftDistance = (type == MoveType.GROUND) ? Math.sqrt(leftDistance) : Math.sqrt(leftDistance + dz * dz);
		
		final double passedDistance = _creature.getStat().getRealMoveSpeed(_moveTimeStamp <= 5) / 10;
		
		// Calculate the current distance fraction based on the delta.
		double fraction = 1;
		if (passedDistance < leftDistance)
			fraction = passedDistance / leftDistance;
		
		// Calculate the maximum Z. Only FLY is allowed to bypass Z check.
		int maxZ = World.WORLD_Z_MAX;
		if (canBypassZCheck)
		{
			final WaterZone waterZone = ZoneManager.getInstance().getZone(curX, curY, curZ, WaterZone.class);
			if (waterZone != null && GeoEngine.getInstance().getHeight(curX, curY, curZ) - waterZone.getWaterZ() < -20)
				maxZ = waterZone.getWaterZ();
		}
		
		final double nextX;
		final double nextY;
		final double nextZ;
		
		// Set the position only
		if (passedDistance < leftDistance)
		{
			nextX = _xAccurate += dx * fraction;
			nextY = _yAccurate += dy * fraction;
			nextZ = Math.min((type == MoveType.GROUND) ? GeoEngine.getInstance().getHeight((int) (_xAccurate), (int) (_yAccurate), curZ) : (curZ + (int) (dz * fraction + 0.5)), maxZ);
		}
		// Already there : set the position to the destination.
		else
		{
			nextX = _destination.getX();
			nextY = _destination.getY();
			nextZ = Math.min(_destination.getZ(), maxZ);
		}
		
		// Set the position of the Creature.
		_creature.setXYZ((int) nextX, (int) nextY, (int) nextZ);
		
		// Draw a debug of this movement if the config is activated.
		if (Config.DEBUG_MOVEMENT)
		{
			_packet.addPoint(Color.RED, curX, curY, curZ);
			_packet.addPoint(Color.GREEN, _creature.getPosition());
			
			_creature.sendPacket(_packet);
			
			// We are supposed to run, but the difference of Z is way too high.
			if (type == MoveType.GROUND && Math.abs(curZ - _creature.getPosition().getZ()) > 100)
				_creature.sendMessage("Falling/Climb bug found when moving from " + curX + ", " + curY + ", " + curZ + " to " + _creature.getPosition().toString());
		}
		
		_creature.revalidateZone(false);
		
		if (isOnLastPawnMoveGeoPath())
		{
			final int offset = (int) (_offset + _creature.getCollisionRadius() + ((_pawn instanceof Creature) ? ((Creature) _pawn).getCollisionRadius() : 0));
			return (type == MoveType.GROUND) ? _creature.isIn2DRadius(_destination, offset) : _creature.isIn3DRadius(_destination, offset);
		}
		return (passedDistance >= leftDistance);
	}
	
	@Override
	protected void followTask(WorldObject pawn, int offset)
	{
		if (_followTask == null)
			return;
		
		// Invalid pawn to follow, or the pawn isn't registered on knownlist.
		if (pawn == null || !_creature.knows(pawn))
		{
			_creature.getAI().tryTo(IntentionType.IDLE, null, null);
			return;
		}
		
		// Don't bother moving if already on radius.
		if (_creature.isIn3DRadius(pawn, offset))
			return;
		
		// If an obstacle is between the Player and the pawn, move to latest good location.
		final Location moveOk = GeoEngine.getInstance().getValidLocation(_creature, pawn);
		
		final boolean isPathClear = MathUtil.checkIfInRange(offset, pawn, moveOk, true);
		if (isPathClear)
			moveToPawn(pawn, offset);
		else
			moveToLocation(moveOk);
	}
}