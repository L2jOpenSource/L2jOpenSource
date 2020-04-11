package net.sf.l2j.gameserver.model.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.OperateType;
import net.sf.l2j.gameserver.model.actor.ai.type.BoatAI;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.move.BoatMove;
import net.sf.l2j.gameserver.model.actor.stat.BoatStat;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.BoatEntrance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.Point2D;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;

public class Boat extends Creature
{
	private final Set<Player> _passengers = ConcurrentHashMap.newKeySet();
	private final List<BoatEntrance> _entrances = new ArrayList<>();
	
	private Runnable _engine;
	
	private Future<?> _payTask;
	
	public Boat(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		
		setAI(new BoatAI(this));
	}
	
	@Override
	public BoatMove getMove()
	{
		return (BoatMove) _move;
	}
	
	@Override
	public void setMove()
	{
		_move = new BoatMove(this);
	}
	
	@Override
	public BoatStat getStat()
	{
		return (BoatStat) super.getStat();
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new BoatStat(this));
	}
	
	@Override
	public void teleportTo(int x, int y, int z, int randomOffset)
	{
		if (_payTask != null)
		{
			_payTask.cancel(false);
			_payTask = null;
		}
		
		getMove().stop();
		
		setTeleporting(true);
		
		getAI().tryTo(IntentionType.ACTIVE, null, null);
		
		for (Player player : _passengers)
			player.teleportTo(x, y, z, randomOffset);
		
		decayMe();
		setXYZ(x, y, z);
		
		onTeleported();
		revalidateZone(true);
	}
	
	@Override
	public void deleteMe()
	{
		if (_payTask != null)
		{
			_payTask.cancel(false);
			_payTask = null;
		}
		
		_engine = null;
		
		getMove().stop();
		
		// Oust all players.
		oustPlayers();
		
		// Decay the vehicle.
		decayMe();
		
		super.deleteMe();
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public void setAI(CreatureAI newAI)
	{
		if (_ai == null)
			_ai = newAI;
	}
	
	@Override
	public void detachAI()
	{
	}
	
	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new VehicleInfo(this));
	}
	
	@Override
	public boolean isFlying()
	{
		return true;
	}
	
	public void registerEngine(Runnable r)
	{
		_engine = r;
	}
	
	public void runEngine(int delay)
	{
		if (_engine != null)
			ThreadPool.schedule(_engine, delay);
	}
	
	/**
	 * Oust all {@Player}s set as passengers to TeleportType.TOWN.
	 */
	public void oustPlayers()
	{
		for (Player player : _passengers)
			oustPlayer(player, MapRegionData.getInstance().getLocationToTeleport(this, TeleportType.TOWN));
	}
	
	/**
	 * Oust a {@Player} out of this {@link Boat}.
	 * <ul>
	 * <li>Remove him from peace zone.</li>
	 * <li>Teleport him back to a valid zone, or Location set as parameter. setBoat(null) is embedded into teleportTo, no need to care about it.</li>
	 * </ul>
	 * In case he is offline, his position is forced to be edited, and setBoat(null) applies.
	 * @param player : The Player to oust.
	 * @param loc : The Location used as oust.
	 */
	public void oustPlayer(Player player, Location loc)
	{
		// Player in shop mode got their shop broken. They're still sit down.
		if (player.isInStoreMode())
		{
			player.setOperateType(OperateType.NONE);
			player.broadcastUserInfo();
		}
		
		player.setInsideZone(ZoneId.PEACE, false);
		player.sendPacket(SystemMessageId.EXIT_PEACEFUL_ZONE);
		
		if (player.isOnline())
			player.teleportTo(loc.getX(), loc.getY(), loc.getZ(), 0);
		else
		{
			player.setBoat(null);
			player.setXYZInvisible(loc);
		}
	}
	
	public Set<Player> getPassengers()
	{
		return _passengers;
	}
	
	/**
	 * Test and add a {@link Player} passenger to this {@link Boat} if conditions matched.
	 * @param player : The Player to test.
	 */
	public void addPassenger(Player player)
	{
		// Player is already set on another Boat, or isn't set on any Boat.
		if (player.getBoat() == null || (player.getBoat() != null && player.getBoat() != this))
			return;
		
		// Can't add the passenger.
		if (!_passengers.add(player))
			return;
		
		player.setInsideZone(ZoneId.PEACE, true);
		player.sendPacket(SystemMessageId.ENTER_PEACEFUL_ZONE);
	}
	
	public void broadcastToPassengers(L2GameServerPacket sm)
	{
		for (Player player : _passengers)
			player.sendPacket(sm);
	}
	
	/**
	 * Consume ticket(s) and teleport {@link Player}s located on this {@link Boat} if no correct ticket was found.<br>
	 * <br>
	 * Also clear {@link BoatEntrance} points. They will be refilled upon next Boat stop.
	 * @param itemId : The itemId to check.
	 * @param count : The amount of tickets to have.
	 * @param loc : The Location used as oust in case a Player can't pay.
	 */
	public void payForRide(int itemId, int count, Location loc)
	{
		// Stop task if already running.
		if (_payTask != null)
		{
			_payTask.cancel(false);
			_payTask = null;
		}
		
		// Retrieve initial List of Players.
		final List<Player> passengers = getKnownTypeInRadius(Player.class, 1000);
		
		// Test and potentially add Players matching this boat criterias as passengers.
		for (Player player : passengers)
			addPassenger(player);
		
		// Start task after 5sec.
		_payTask = ThreadPool.schedule(() ->
		{
			for (Player player : passengers)
			{
				// Valid passenger was found, check his ticket, if set on server.
				if (player.getBoat() == this && _passengers.contains(player))
				{
					// No ticket set on server, don't bother checking.
					if (itemId <= 0)
						continue;
					
					// Item could be destroyed, we test next passenger.
					if (player.destroyItemByItemId("Boat", itemId, count, this, false))
					{
						// Unsummon servitor/pet if existing (not retail, but simply logical, since they can't follow).
						if (player.getSummon() != null)
							player.getSummon().unSummon(player);
						
						if (count > 1)
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
						else
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
						
						continue;
					}
				}
				// The passenger isn't part of a Boat anymore, don't bother with him.
				else if (!player.isInBoat())
					continue;
				
				// Oust the Player out of the Boat.
				oustPlayer(player, loc);
				player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET);
			}
			
			// Clear entrances points, as they won't be needed during travel.
			_entrances.clear();
		}, 5000);
	}
	
	/**
	 * Reset and generate anew this {@link Boat} {@link BoatEntrance}s.<br>
	 * <br>
	 * Used once when Boat is first spawned, and then on any subsequent dock stop.
	 */
	public void renewBoatEntrances()
	{
		// Clear previous entries.
		_entrances.clear();
		
		final int deckZ = -46;
		final int absoluteZ = getZ() - 35;
		final int yOffset = -53;
		final int xOffset = 260;
		
		// Populating the dynamic list of boat entrances.
		for (int i = 0; i < 16; i++)
		{
			final int y = yOffset - i * 10;
			
			// Left side entrance.
			final Point2D leftPoint = getWorldPointFromBoatPoint(-xOffset, y);
			_entrances.add(new BoatEntrance(new Location(leftPoint.getX(), leftPoint.getY(), absoluteZ), new Location(-xOffset + 60, y, deckZ)));
			
			// Right side entrance.
			final Point2D rightPoint = getWorldPointFromBoatPoint(xOffset, y);
			_entrances.add(new BoatEntrance(new Location(rightPoint.getX(), rightPoint.getY(), absoluteZ), new Location(xOffset - 60, y, deckZ)));
		}
	}
	
	/**
	 * @param loc : The Location used as reference.
	 * @return the closest {@link BoatEntrance} from the {@link Player} {@link Location} used as parameter.
	 */
	public BoatEntrance getClosestEntrance(Location loc)
	{
		BoatEntrance closestEntrance = null;
		double dist = Double.MAX_VALUE;
		
		for (BoatEntrance entrance : _entrances)
		{
			final double distEntrance = loc.distance2D(entrance.getOuterLocation());
			if (distEntrance < dist)
			{
				closestEntrance = entrance;
				dist = distEntrance;
			}
		}
		return closestEntrance;
	}
	
	/**
	 * @param xOffset : The x offset used as reference.
	 * @param yOffset : The y offset used as reference.
	 * @return a {@link Point2D} based on this {@link Boat} position and heading, and from x/y offsets set as parameters.
	 */
	public Point2D getWorldPointFromBoatPoint(int xOffset, int yOffset)
	{
		final double boatHeadingDegree = MathUtil.convertHeadingToDegree(getHeading());
		
		final double xRadians = Math.toRadians(boatHeadingDegree + 90);
		final double yRadians = Math.toRadians(boatHeadingDegree + 180);
		
		final int deltaX = (int) Math.round(xOffset * Math.cos(xRadians) + yOffset * Math.cos(yRadians));
		final int deltaY = (int) Math.round(yOffset * Math.sin(yRadians) + xOffset * Math.sin(xRadians));
		
		return new Point2D(getX() + deltaX, getY() + deltaY);
	}
}