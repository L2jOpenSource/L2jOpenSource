/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.data.xml.impl.OptionData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.options.Options;

/**
 * Used to store an augmentation and its bonuses.
 * @author durgus, UnAfraid
 */
public final class L2Augmentation
{
	private int _effectsId = 0;
	private AugmentationStatBoni _boni = null;
	
	public L2Augmentation(int effects)
	{
		_effectsId = effects;
		_boni = new AugmentationStatBoni(_effectsId);
	}
	
	public static class AugmentationStatBoni
	{
		private static final Logger _log = Logger.getLogger(AugmentationStatBoni.class.getName());
		private final List<Options> _options = new ArrayList<>();
		private boolean _active;
		
		public AugmentationStatBoni(int augmentationId)
		{
			_active = false;
			int[] stats = new int[2];
			stats[0] = 0x0000FFFF & augmentationId;
			stats[1] = (augmentationId >> 16);
			
			for (int stat : stats)
			{
				Options op = OptionData.getInstance().getOptions(stat);
				if (op != null)
				{
					_options.add(op);
				}
				else
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't find option: " + stat);
				}
			}
		}
		
		public void applyBonus(L2PcInstance player)
		{
			// make sure the bonuses are not applied twice..
			if (_active)
			{
				return;
			}
			
			for (Options op : _options)
			{
				op.apply(player);
			}
			
			_active = true;
		}
		
		public void removeBonus(L2PcInstance player)
		{
			// make sure the bonuses are not removed twice
			if (!_active)
			{
				return;
			}
			
			for (Options op : _options)
			{
				op.remove(player);
			}
			
			_active = false;
		}
	}
	
	public int getAttributes()
	{
		return _effectsId;
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return _effectsId;
	}
	
	/**
	 * Applies the bonuses to the player.
	 * @param player
	 */
	public void applyBonus(L2PcInstance player)
	{
		_boni.applyBonus(player);
	}
	
	/**
	 * Removes the augmentation bonuses from the player.
	 * @param player
	 */
	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);
	}
}
