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

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.character.playable.OnPlayableExpChanged;
import l2r.gameserver.model.events.listeners.AbstractEventListener;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExSpawnEmitter;

public final class SoulEating extends L2Effect
{
	private final int _expNeeded;
	
	public SoulEating(Env env, EffectTemplate template)
	{
		super(env, template);
		_expNeeded = template.getParameters().getInt("expNeeded");
	}
	
	public SoulEating(Env env, L2Effect effect)
	{
		super(env, effect);
		_expNeeded = effect.getEffectTemplate().getParameters().getInt("expNeeded");
	}
	
	public void onExperienceReceived(L2Playable playable, long exp)
	{
		if (playable.isPlayer() && (exp >= _expNeeded))
		{
			final L2PcInstance player = playable.getActingPlayer();
			final int maxSouls = (int) player.calcStat(Stats.MAX_SOULS, 0, null, null);
			if (player.getChargedSouls() >= maxSouls)
			{
				playable.sendPacket(SystemMessageId.SOUL_CANNOT_BE_ABSORBED_ANYMORE);
				return;
			}
			
			player.increaseSouls(1);
			
			if ((player.getTarget() != null) && player.getTarget().isNpc())
			{
				final L2Npc npc = (L2Npc) playable.getTarget();
				player.broadcastPacket(new ExSpawnEmitter(player, npc), 500);
			}
		}
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isPlayer())
		{
			for (AbstractEventListener listener : getEffected().getListeners(EventType.ON_PLAYABLE_EXP_CHANGED))
			{
				if (listener.getOwner() == this)
				{
					listener.unregisterMe();
				}
			}
		}
		super.onExit();
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			getEffected().addListener(new ConsumerEventListener(getEffected(), EventType.ON_PLAYABLE_EXP_CHANGED, (OnPlayableExpChanged event) -> onExperienceReceived(event.getActiveChar(), (event.getNewExp() - event.getOldExp())), this));
		}
		return super.onStart();
	}
}