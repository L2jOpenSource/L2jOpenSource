/*
 * Copyright (C) 2004-2013 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2r.gameserver.GeoData;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.serverpackets.FlyToLocation;
import l2r.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2r.gameserver.network.serverpackets.ValidateLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrowUp extends L2Effect
{
	private static final Logger _log = LoggerFactory.getLogger(ThrowUp.class);
	
	public ThrowUp(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		// Get current position of the L2Character
		final int curX = getEffected().getX();
		final int curY = getEffected().getY();
		final int curZ = getEffected().getZ();
		
		// Calculate distance between effector and effected current position
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			_log.info("EffectThrow was going to use invalid coordinates for characters, getEffected: " + curX + "," + curY + " and getEffector: " + getEffector().getX() + "," + getEffector().getY());
			return false;
		}
		int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);
		
		double cos;
		double sin;
		
		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance
		if (distance < 1)
		{
			return false;
		}
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		int x = getEffector().getX() - (int) (offset * cos);
		int y = getEffector().getY() - (int) (offset * sin);
		int z = getEffected().getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), x, y, z, getEffected().getInstanceId());
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), destination, FlyType.THROW_UP));
		// TODO: Review.
		getEffected().setXYZ(destination);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
}
