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

import l2r.gameserver.data.xml.impl.TransformData;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * Transformation effect.
 * @author nBd
 */
public class Transformation extends L2Effect
{
	private int _id;
	
	public Transformation(Env env, EffectTemplate template)
	{
		super(env, template);
		
		try
		{
			_id = template.getParameters().getInt("id", -1);
		}
		catch (Exception e)
		{
			_id = -1;
		}
	}
	
	public Transformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TRANSFORMATION;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isPlayer())
		{
			return false;
		}
		
		if (_id > 0)
		{
			TransformData.getInstance().transformPlayer(_id, getEffected().getActingPlayer());
		}
		else
		{
			TransformData.getInstance().transformPlayer(getSkill().getTransformId(), getEffected().getActingPlayer());
		}
		return true;
	}
}
