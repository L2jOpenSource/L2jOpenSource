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

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.effects.EffectFlag;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

/**
 * Servitor Share effect.<br>
 * Synchronizing effects on player and servitor if one of them gets removed for some reason the same will happen to another.
 * @author UnAfraid
 */
public class ServitorShare extends L2Effect
{
	private final Map<Stats, Double> stats = new HashMap<>(9);
	
	public ServitorShare(Env env, EffectTemplate template)
	{
		super(env, template);
		for (String key : template.getParameters().getSet().keySet())
		{
			stats.put(Stats.valueOfXml(key), template.getParameters().getDouble(key, 1.));
		}
	}
	
	@Override
	public boolean onStart()
	{
		super.onStart();
		getEffected().getActingPlayer().setServitorShare(stats);
		if (getEffected().getActingPlayer().getSummon() != null)
		{
			getEffected().getActingPlayer().getSummon().broadcastInfo();
			getEffected().getActingPlayer().getSummon().getStatus().startHpMpRegeneration();
		}
		return true;
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit()
	{
		getEffected().getActingPlayer().setServitorShare(null);
		if (getEffected().getSummon() != null)
		{
			if (getEffected().getSummon().getCurrentHp() > getEffected().getSummon().getMaxHp())
			{
				getEffected().getSummon().setCurrentHp(getEffected().getSummon().getMaxHp());
			}
			if (getEffected().getSummon().getCurrentMp() > getEffected().getSummon().getMaxMp())
			{
				getEffected().getSummon().setCurrentMp(getEffected().getSummon().getMaxMp());
			}
			getEffected().getSummon().broadcastInfo();
		}
	}
}
