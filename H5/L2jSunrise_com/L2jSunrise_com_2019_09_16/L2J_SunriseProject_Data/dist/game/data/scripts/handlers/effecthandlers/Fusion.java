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

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * @author Kerberos
 */
public class Fusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;
	
	public Fusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = SkillData.getInstance().getMaxLevel(getSkill().getId());
	}
	
	@Override
	public boolean onActionTime()
	{
		return true;
	}
	
	@Override
	public void increaseEffect()
	{
		if (_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}
	
	@Override
	public void decreaseForce()
	{
		_effect--;
		if (_effect < 1)
		{
			exit();
		}
		else
		{
			updateBuff();
		}
	}
	
	private void updateBuff()
	{
		exit();
		SkillData.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
}
