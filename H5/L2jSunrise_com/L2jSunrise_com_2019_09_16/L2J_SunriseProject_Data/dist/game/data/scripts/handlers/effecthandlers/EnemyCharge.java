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
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.serverpackets.FlyToLocation;
import l2r.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2r.gameserver.network.serverpackets.ValidateLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnemyCharge extends L2Effect
{
	static final Logger _log = LoggerFactory.getLogger(EnemyCharge.class);
	
	private int _x, _y, _z;
	
	public EnemyCharge(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isMovementDisabled())
		{
			return false;
		}
		
		// Get current position of the L2Character
		final int curX = getEffector().getX();
		final int curY = getEffector().getY();
		final int curZ = getEffector().getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		double dx = getEffected().getX() - curX;
		double dy = getEffected().getY() - curY;
		double dz = getEffected().getZ() - curZ;
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			_log.info("EffectEnemyCharge was going to use invalid coordinates for characters, getEffector: " + curX + "," + curY + " and getEffected: " + getEffected().getX() + "," + getEffected().getY());
			return false;
		}
		int offset = Math.max((int) distance - getSkill().getFlyRadius(), 30);
		
		double cos;
		double sin;
		
		// approximation for moving closer when z coordinates are different
		// TODO: handle Z axis movement better
		offset -= Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance
		if ((distance < 1) || ((distance - offset) <= 0))
		{
			return false;
		}
		
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;
		
		// Calculate the new destination with offset included
		_x = curX + (int) ((distance - offset) * cos);
		_y = curY + (int) ((distance - offset) * sin);
		_z = getEffected().getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(getEffector().getX(), getEffector().getY(), getEffector().getZ(), _x, _y, _z, getEffector().getInstanceId());
		
		getEffector().broadcastPacket(new FlyToLocation(getEffector(), destination, FlyType.CHARGE));
		
		// maybe is need force set X,Y,Z
		getEffector().setXYZ(destination);
		getEffector().broadcastPacket(new ValidateLocation(getEffector()));
		
		return true;
	}
}
