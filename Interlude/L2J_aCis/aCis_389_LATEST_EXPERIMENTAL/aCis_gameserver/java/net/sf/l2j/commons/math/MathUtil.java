package net.sf.l2j.commons.math;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.Point2D;

public class MathUtil
{
	public static final int[][] MATRICE_3X3_LINES =
	{
		{
			1,
			2,
			3
		}, // line 1
		{
			4,
			5,
			6
		}, // line 2
		{
			7,
			8,
			9
		}, // line 3
		{
			1,
			4,
			7
		}, // column 1
		{
			2,
			5,
			8
		}, // column 2
		{
			3,
			6,
			9
		}, // column 3
		{
			1,
			5,
			9
		}, // diagonal 1
		{
			3,
			5,
			7
		}, // diagonal 2
	};
	
	/**
	 * @param objectsSize : The overall elements size.
	 * @param pageSize : The number of elements per page.
	 * @return The number of pages, based on the number of elements and the number of elements we want per page.
	 */
	public static int countPagesNumber(int objectsSize, int pageSize)
	{
		return objectsSize / pageSize + (objectsSize % pageSize == 0 ? 0 : 1);
	}
	
	/**
	 * @param numToTest : The number to test.
	 * @param min : The minimum limit.
	 * @param max : The maximum limit.
	 * @return the number or one of the limit (mininum / maximum).
	 */
	public static int limit(int numToTest, int min, int max)
	{
		return (numToTest > max) ? max : ((numToTest < min) ? min : numToTest);
	}
	
	public static double calculateAngleFrom(WorldObject obj1, WorldObject obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}
	
	public static final double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		
		return angleTarget;
	}
	
	public static final double convertHeadingToDegree(int clientHeading)
	{
		return clientHeading / 182.04444444444444444444444444444;
	}
	
	public static final Point2D getNewLocationByDistanceAndHeading(int x, int y, int heading, int distance)
	{
		return getNewLocationByDistanceAndDegree(x, y, MathUtil.convertHeadingToDegree(heading), distance);
	}
	
	public static final Point2D getNewLocationByDistanceAndDegree(int x, int y, double degree, int distance)
	{
		final double radians = Math.toRadians(degree);
		final int deltaX = (int) (distance * Math.cos(radians));
		final int deltaY = (int) (distance * Math.sin(radians));
		
		return new Point2D(x + deltaX, y + deltaY);
	}
	
	public static final int calculateHeadingFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		
		return (int) (angleTarget * 182.04444444444444444444444444444);
	}
	
	/**
	 * This check includes collision radius of both characters.<br>
	 * Used for accurate checks (skill casts, knownlist, etc).
	 * @param range The range to use as check.
	 * @param obj1 The position 1 to make check on.
	 * @param obj2 The postion 2 to make check on.
	 * @param includeZAxis Include Z check or not.
	 * @return true if both objects are in the given radius.
	 */
	public static boolean checkIfInRange(int range, WorldObject obj1, WorldObject obj2, boolean includeZAxis)
	{
		if (obj1 == null || obj2 == null)
			return false;
		
		if (range == -1)
			return true; // not limited
			
		double rad = 0;
		if (obj1 instanceof Creature)
			rad += ((Creature) obj1).getCollisionRadius();
		
		if (obj2 instanceof Creature)
			rad += ((Creature) obj2).getCollisionRadius();
		
		double dx = obj1.getX() - obj2.getX();
		double dy = obj1.getY() - obj2.getY();
		
		if (includeZAxis)
		{
			double dz = obj1.getZ() - obj2.getZ();
			double d = dx * dx + dy * dy + dz * dz;
			
			return d <= range * range + 2 * range * rad + rad * rad;
		}
		
		double d = dx * dx + dy * dy;
		return d <= range * range + 2 * range * rad + rad * rad;
	}
	
	public static boolean checkIfInRange(int range, WorldObject obj, Location loc, boolean includeZAxis)
	{
		if (obj == null || loc.equals(Location.DUMMY_LOC))
			return false;
		
		if (range == -1)
			return true; // not limited
			
		double rad = 0;
		if (obj instanceof Creature)
			rad += ((Creature) obj).getCollisionRadius();
		
		double dx = obj.getX() - loc.getX();
		double dy = obj.getY() - loc.getY();
		
		if (includeZAxis)
		{
			double dz = obj.getZ() - loc.getZ();
			double d = dx * dx + dy * dy + dz * dz;
			
			return d <= range * range + 2 * range * rad + rad * rad;
		}
		
		double d = dx * dx + dy * dy;
		return d <= range * range + 2 * range * rad + rad * rad;
	}
	
	/**
	 * Returns the rounded value of val to specified number of digits after the decimal point.<BR>
	 * (Based on round() in PHP)
	 * @param val
	 * @param numPlaces
	 * @return float roundedVal
	 */
	public static float roundTo(float val, int numPlaces)
	{
		if (numPlaces <= 1)
			return Math.round(val);
		
		float exponent = (float) Math.pow(10, numPlaces);
		
		return (Math.round(val * exponent) / exponent);
	}
}