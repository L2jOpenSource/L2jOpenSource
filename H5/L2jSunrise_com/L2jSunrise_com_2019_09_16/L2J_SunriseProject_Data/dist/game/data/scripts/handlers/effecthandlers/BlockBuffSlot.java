/*
 * Copyright (C) 2004-2015 L2J DataPack
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Block Buff Slot effect implementation.
 * @author Zoey76 reworked vGodFather
 */
public final class BlockBuffSlot extends L2Effect
{
	private final Set<String> _blockBuffSlots;
	
	public BlockBuffSlot(Env env, EffectTemplate template)
	{
		super(env, template);
		
		String blockBuffSlots = template.getParameters().getString("slot", null);
		if ((blockBuffSlots != null) && !blockBuffSlots.isEmpty())
		{
			_blockBuffSlots = new HashSet<>();
			for (String slot : blockBuffSlots.split(";"))
			{
				_blockBuffSlots.add(slot.toLowerCase());
			}
		}
		else
		{
			_blockBuffSlots = Collections.<String> emptySet();
		}
	}
	
	@Override
	public void onExit()
	{
		getEffected().getEffectList().removeBlockedBuffSlots(_blockBuffSlots);
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().getEffectList().addBlockedBuffSlots(_blockBuffSlots);
		return true;
	}
}
