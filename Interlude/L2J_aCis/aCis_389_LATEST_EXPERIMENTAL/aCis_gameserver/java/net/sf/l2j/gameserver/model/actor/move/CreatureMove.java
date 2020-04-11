package net.sf.l2j.gameserver.model.actor.move;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.MoveType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.WaterZone;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;

/**
 * This class groups all movement data related to a {@link Creature}.
 */
public class CreatureMove
{
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	protected final Creature _creature;
	
	protected WorldObject _pawn;
	protected int _offset;
	
	protected byte _moveTypes;
	
	protected int _moveTimeStamp;
	
	protected final Location _destination = new Location(0, 0, 0);
	
	protected double _xAccurate;
	protected double _yAccurate;
	
	protected final Queue<Location> _geoPath = new LinkedList<>();
	
	protected ScheduledFuture<?> _task;
	protected ScheduledFuture<?> _followTask;
	
	public CreatureMove(Creature creature)
	{
		_creature = creature;
	}
	
	public Location getDestination()
	{
		return _destination;
	}
	
	public MoveType getMoveType()
	{
		if ((_moveTypes & MoveType.SWIM.getMask()) != 0)
			return MoveType.SWIM;
		
		if ((_moveTypes & MoveType.FLY.getMask()) != 0)
			return MoveType.FLY;
		
		return MoveType.GROUND;
	}
	
	public void addMoveType(MoveType type)
	{
		_moveTypes |= type.getMask();
	}
	
	public void removeMoveType(MoveType type)
	{
		_moveTypes &= ~type.getMask();
	}
	
	public ScheduledFuture<?> getTask()
	{
		return _task;
	}
	
	public ExServerPrimitive getDebugMovePacket()
	{
		return null;
	}
	
	/**
	 * Used by players to describe current action of the {@link Creature} associated to this {@link CreatureMove}.
	 * @param player : The Player we send the packet.
	 */
	public void describeMovementTo(Player player)
	{
		player.sendPacket(findPacketToSend());
	}
	
	/**
	 * Allows the {@link Creature} to move to a {@link WorldObject} pawn.<br>
	 * <br>
	 * The pawn can freely move, and so {@link #updatePosition()} takes the offset in consideration (generally the collision radius of both characters + weapon or interaction range).
	 * @param pawn : The WorldObject used as destination.
	 * @param offset : The distance where onEvtArrived event can occur.
	 */
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
		
		// Calculate the path.
		final Location loc = calculatePath(ox, oy, oz, tx, ty, tz);
		if (loc != null)
		{
			tx = loc.getX();
			ty = loc.getY();
			tz = loc.getZ();
		}
		
		// Set the destination.
		_destination.set(tx, ty, tz);
		
		// Calculate the heading.
		_creature.getPosition().setHeadingTo(tx, ty);
		
		registerMoveTask();
		
		// Broadcast the good packet giving the situation.
		_creature.broadcastPacket(findPacketToSend());
	}
	
	public void moveToLocation(Location loc)
	{
		moveToLocation(loc.getX(), loc.getY(), loc.getZ());
	}
	
	/**
	 * Move the {@link Creature} associated to this {@link CreatureMove} to defined x/y/z coordinates.
	 * @param tx : The X position to reach.
	 * @param ty : The Y position to reach.
	 * @param tz : The Z position to reach.
	 */
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
		
		// Calculate the path.
		final Location loc = calculatePath(ox, oy, oz, tx, ty, tz);
		if (loc != null)
		{
			tx = loc.getX();
			ty = loc.getY();
			tz = loc.getZ();
		}
		
		// Set the destination.
		_destination.set(tx, ty, tz);
		
		// Calculate the heading.
		_creature.getPosition().setHeadingTo(tx, ty);
		
		registerMoveTask();
		
		// Broadcast MoveToLocation packet to known objects.
		_creature.broadcastPacket(new MoveToLocation(_creature));
	}
	
	/**
	 * Stop the movement of the {@link Creature}.
	 */
	public void stop()
	{
		if (_task == null)
			return;
		
		_task.cancel(false);
		_task = null;
		
		_creature.broadcastPacket(new StopMove(_creature));
		_creature.getAI().notifyEvent(AiEventType.ARRIVED, true, null);
	}
	
	/**
	 * Create and launch a follow task upon a {@link WorldObject} pawn, executed every 1s. It is used by onIntentionFollow.
	 * @param pawn : The WorldObject to follow.
	 */
	public void startFollow(WorldObject pawn)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		// Create and Launch an AI Follow Task to execute every 1s
		_followTask = ThreadPool.scheduleAtFixedRate(() -> followTask(pawn), 5, FOLLOW_INTERVAL);
	}
	
	/**
	 * Create and launch a follow task upon a {@link WorldObject} pawn, every 0.5s, following at specified range.
	 * @param pawn : The WorldObject to follow.
	 * @param range : The specific range to test.
	 */
	public void startFollow(WorldObject pawn, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		_followTask = ThreadPool.scheduleAtFixedRate(() -> followTask(pawn, range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	/**
	 * Stop the follow task.
	 */
	public void stopFollow()
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
	}
	
	public void registerMoveTask()
	{
		if (_task != null)
			return;
		
		_task = ThreadPool.scheduleAtFixedRate(() ->
		{
			if (updatePosition() && !moveToNextRoutePoint())
				ThreadPool.execute(() -> _creature.getAI().notifyEvent(AiEventType.ARRIVED, false, null));
		}, 100, 100);
	}
	
	public void cancelMoveTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		_moveTimeStamp = 0;
	}
	
	public boolean moveToNextRoutePoint()
	{
		// Creature is not on geodata path, return.
		if (_task == null || _geoPath.isEmpty())
			return false;
		
		// Movement is not allowed, return.
		if (_creature.getStat().getMoveSpeed() <= 0 || _creature.isMovementDisabled())
			return false;
		
		// Geopath is dry, return.
		final Location destination = _geoPath.poll();
		if (destination == null)
			return false;
		
		// Set the current x/y.
		_xAccurate = _creature.getX();
		_yAccurate = _creature.getY();
		
		// Set the destination.
		_destination.set(destination);
		
		// Set the heading.
		_creature.getPosition().setHeadingTo(destination);
		
		// Broadcast MoveToLocation packet to known objects.
		_creature.broadcastPacket(new MoveToLocation(_creature));
		
		return true;
	}
	
	/**
	 * Update the position of the Creature during a movement and return True if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties are stored. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started, this method will be called each 0.1 sec to estimate and update the Creature position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why, client send regularly a
	 * Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the Creature position is automatically set to the destination position even if the movement is not finished.
	 * @return true if the movement is finished.
	 */
	public boolean updatePosition()
	{
		if (_task == null || !_creature.isVisible())
			return true;
		
		final MoveType type = getMoveType();
		
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
		
		// Calculate the maximum Z.
		int maxZ = World.WORLD_Z_MAX;
		if (type == MoveType.SWIM)
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
		
		_creature.revalidateZone(false);
		
		if (isOnLastPawnMoveGeoPath())
		{
			final int offset = (int) (_offset + _creature.getCollisionRadius() + ((_pawn instanceof Creature) ? ((Creature) _pawn).getCollisionRadius() : 0));
			return (type == MoveType.GROUND) ? _creature.isIn2DRadius(_destination, offset) : _creature.isIn3DRadius(_destination, offset);
		}
		return (passedDistance >= leftDistance);
	}
	
	/**
	 * @param target : The Location we try to reach.
	 * @param offset : The interact area radius.
	 * @return true if a movement must be done to reach the {@link Location}, based on an offset.
	 */
	public boolean maybeMoveToPosition(Location target, int offset)
	{
		if (target == null || offset < 0)
			return false;
		
		if (!_creature.isIn2DRadius(target, (int) (offset + _creature.getCollisionRadius())))
		{
			if (_creature.isMovementDisabled())
				return true;
			
			if (!(_creature instanceof Playable))
				_creature.forceRunStance();
			
			int x = _creature.getX();
			int y = _creature.getY();
			
			final double dx = target.getX() - x;
			final double dy = target.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			final double sin = dy / dist;
			final double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveToLocation(x, y, target.getZ());
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param target : The WorldObject we try to reach.
	 * @param offset : The interact area radius.
	 * @param isShiftPressed
	 * @return true if a movement must be done to reach the {@link WorldObject}, based on an offset.
	 */
	public boolean maybeMoveToPawn(WorldObject target, int offset, boolean isShiftPressed)
	{
		if (target == null || offset < 0 || _creature == target)
			return false;
		
		double collisionOffset = _creature.getCollisionRadius();
		if (target instanceof Creature)
			collisionOffset += ((Creature) target).getCollisionRadius();
		
		if (!_creature.isIn2DRadius(target, offset + (int) collisionOffset))
		{
			if (_creature.isMovementDisabled())
				return true;
			
			if (!(_creature instanceof Playable))
				_creature.forceRunStance();
			
			if (!isShiftPressed)
				moveToPawn(target, offset);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * @return true if this {@link Creature} is under LAST geopath entry AND follow a pawn.
	 */
	protected boolean isOnLastPawnMoveGeoPath()
	{
		return _geoPath.isEmpty() && _pawn instanceof Creature;
	}
	
	/**
	 * If this {@link Creature} is under LAST geopath entry AND follow a pawn, send the {@link MoveToPawn} packet.<br>
	 * <br>
	 * Otherwise, it means it still follows a determined path, which means he uses {@link MoveToLocation} packet.
	 * @return the {@link L2GameServerPacket} packet to send.
	 */
	protected L2GameServerPacket findPacketToSend()
	{
		return (isOnLastPawnMoveGeoPath()) ? new MoveToPawn(_creature, _pawn, _offset) : new MoveToLocation(_creature);
	}
	
	protected void followTask(WorldObject pawn)
	{
		followTask(pawn, 70);
	}
	
	protected void followTask(WorldObject pawn, int offset)
	{
		if (_followTask == null)
			return;
		
		// Invalid pawn to follow, or the pawn isn't registered on knownlist.
		if (pawn == null || !_creature.knows(pawn))
		{
			if (_creature instanceof Summon)
				((Summon) _creature).setFollowStatus(false);
			
			_creature.getAI().tryTo(IntentionType.IDLE, null, null);
			return;
		}
		
		// Don't bother moving if already on radius.
		if (_creature.isIn3DRadius(pawn, offset))
			return;
		
		moveToPawn(pawn, offset);
	}
	
	/**
	 * @param ox : The original X coordinate we start from.
	 * @param oy : The original Y coordinate we start from.
	 * @param oz : The original Z coordinate we start from.
	 * @param tx : The target X coordinate we search to join.
	 * @param ty : The target Y coordinate we search to join.
	 * @param tz : The target Z coordinate we search to join.
	 * @return the new {@link Location} destination to set, or null if not needed. Feed the geopath if needed.
	 */
	protected Location calculatePath(int ox, int oy, int oz, int tx, int ty, int tz)
	{
		// We can process to next point without extra help ; return directly.
		if (GeoEngine.getInstance().canMoveToTarget(ox, oy, oz, tx, ty, tz))
			return null;
		
		// Calculate the path. If no path or too short, calculate the first valid location.
		final LinkedList<Location> path = GeoEngine.getInstance().findPath(ox, oy, oz, tx, ty, tz, _creature instanceof Playable);
		if (path == null || path.size() < 2)
			return GeoEngine.getInstance().getValidLocation(ox, oy, oz, tx, ty, tz);
		
		// Feed the geopath with whole path.
		_geoPath.addAll(path);
		
		// Retrieve first Location.
		return _geoPath.poll();
	}
}