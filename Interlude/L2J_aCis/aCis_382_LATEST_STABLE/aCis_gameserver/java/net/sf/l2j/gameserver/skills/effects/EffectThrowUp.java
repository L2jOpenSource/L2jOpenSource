package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.FlyType;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Env;

public class EffectThrowUp extends L2Effect
{
	private int _x, _y, _z;
	
	public EffectThrowUp(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.THROW_UP;
	}
	
	@Override
	public boolean onStart()
	{
		// Get current position of the Creature
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();
		
		// Get the difference between effector and effected positions
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		
		// Calculate distance between effector and effected current position
		double distance = Math.sqrt(dx * dx + dy * dy);
		if (distance < 1 || distance > 2000)
			return false;
		
		int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);
		double cos, sin;
		
		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
			offset = 5;
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		_x = getEffector().getX() - (int) (offset * cos);
		_y = getEffector().getY() - (int) (offset * sin);
		_z = getEffected().getZ();
		
		Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, _z);
		_x = destiny.getX();
		_y = destiny.getY();
		
		getEffected().startStunning();
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.THROW_UP));
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopStunning(false);
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.STUNNED.getMask();
	}
}