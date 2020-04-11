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

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.serverpackets.StartRotation;
import l2r.gameserver.network.serverpackets.StopRotation;

/**
 * Bluff effect.
 * @author decad
 */
public class Bluff extends L2Effect
{
	private final int _chance;
	
	public Bluff(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_chance = template.getParameters().getInt("chance", 100);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!Formulas.calcProbability(_chance, getEffector(), getEffected(), getSkill()))
		{
			return false;
		}
		
		final L2Character effected = getEffected();
		// Headquarters NPC should not rotate
		if ((effected.getId() == 35062) || effected.isRaid() || effected.isRaidMinion())
		{
			return false;
		}
		
		final L2Character effector = getEffector();
		effected.broadcastPacket(new StartRotation(effected.getObjectId(), effected.getHeading(), 1, 65535));
		effected.broadcastPacket(new StopRotation(effected.getObjectId(), effector.getHeading(), 65535));
		effected.setHeading(effector.getHeading());
		return true;
	}
}
