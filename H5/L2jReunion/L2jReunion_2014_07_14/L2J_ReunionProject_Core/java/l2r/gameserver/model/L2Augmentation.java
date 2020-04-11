/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.datatables.xml.OptionsData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.options.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to store an augmentation and its boni
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
		private static final Logger _log = LoggerFactory.getLogger(AugmentationStatBoni.class);
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
				Options op = OptionsData.getInstance().getOptions(stat);
				if (op != null)
				{
					_options.add(op);
				}
				else
				{
					_log.warn(getClass().getSimpleName() + ": Couldn't find option: " + stat);
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
