/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.skills.funcs;

import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

/**
 * @author mkizub
 */
public final class LambdaRnd extends Lambda
{
	private final Lambda _max;
	private final boolean _linear;
	
	public LambdaRnd(Lambda max, boolean linear)
	{
		_max = max;
		_linear = linear;
	}
	
	@Override
	public double calc(Env env)
	{
		if (_linear)
		{
			return _max.calc(env) * Rnd.nextDouble();
		}
		return _max.calc(env) * Rnd.nextGaussian();
	}
}
