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
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;
import l2r.gameserver.model.actor.instance.L2FortCommanderInstance;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Implementation of the Fear Effect
 * @author littlecrow
 */
public class Fear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	
	private int _dX = -1;
	private int _dY = -1;
	
	public Fear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() instanceof L2NpcInstance) || (getEffected() instanceof L2DefenderInstance) || (getEffected() instanceof L2FortCommanderInstance) || (getEffected() instanceof L2SiegeFlagInstance) || (getEffected() instanceof L2SiegeSummonInstance))
		{
			return false;
		}
		
		if (!getEffected().isAfraid())
		{
			if (getEffected().isCastingNow() && getEffected().canAbortCast())
			{
				getEffected().abortCast();
			}
			
			if (getEffected().getX() > getEffector().getX())
			{
				_dX = 1;
			}
			if (getEffected().getY() > getEffector().getY())
			{
				_dY = 1;
			}
			
			getEffected().startFear();
			onActionTime();
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopFear(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		
		if (getEffected().getX() > getEffector().getX())
		{
			_dX = 1;
		}
		if (getEffected().getY() > getEffector().getY())
		{
			_dY = 1;
		}
		
		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;
		
		if (!getEffected().isPet())
		{
			getEffected().setRunning();
		}
		
		final Location destination = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destination);
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
}
