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

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner, lambda);
	}
	
	@Override
	public void calc(Env env)
	{
		if ((cond != null) && !cond.test(env))
		{
			return;
		}
		L2ItemInstance item = (L2ItemInstance) funcOwner;
		
		int enchant = item.getEnchantLevel();
		
		if (enchant <= 0)
		{
			return;
		}
		
		int overenchant = 0;
		
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if (env.getPlayer() != null)
		{
			L2PcInstance player = env.getPlayer();
			if (player.isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && ((enchant + overenchant) > Config.ALT_OLY_ENCHANT_LIMIT))
			{
				if (Config.ALT_OLY_ENCHANT_LIMIT > 3)
				{
					overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
				}
				else
				{
					overenchant = 0;
					enchant = Config.ALT_OLY_ENCHANT_LIMIT;
				}
			}
		}
		
		if ((stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE))
		{
			env.addValue(enchant + (3 * overenchant));
			return;
		}
		
		if (stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getItemGradeSPlus())
			{
				case S:
					// M. Atk. increases by 4 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					env.addValue((4 * enchant) + (8 * overenchant));
					break;
				case A:
				case B:
				case C:
					// M. Atk. increases by 3 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					env.addValue((3 * enchant) + (6 * overenchant));
					break;
				case D:
				case NONE:
					// M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
					// Starting at +4, M. Atk. bonus double.
					env.addValue((2 * enchant) + (4 * overenchant));
					break;
			}
			return;
		}
		
		if (item.isWeapon())
		{
			final WeaponType type = (WeaponType) item.getItemType();
			switch (item.getItem().getItemGradeSPlus())
			{
				case S:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW))
						{
							// P. Atk. increases by 10 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((10 * enchant) + (20 * overenchant));
						}
						else
						{
							// P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((6 * enchant) + (12 * overenchant));
						}
					}
					else
					{
						// P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						env.addValue((5 * enchant) + (10 * overenchant));
					}
					break;
				case A:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW))
						{
							// P. Atk. increases by 8 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((8 * enchant) + (16 * overenchant));
						}
						else
						{
							// P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((5 * enchant) + (10 * overenchant));
						}
					}
					else
					{
						// P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						env.addValue((4 * enchant) + (8 * overenchant));
					}
					break;
				case B:
				case C:
					if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						if ((type == WeaponType.BOW) || (type == WeaponType.CROSSBOW))
						{
							// P. Atk. increases by 6 for bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((6 * enchant) + (12 * overenchant));
						}
						else
						{
							// P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((4 * enchant) + (8 * overenchant));
						}
					}
					else
					{
						// P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						env.addValue((3 * enchant) + (6 * overenchant));
					}
					break;
				case D:
				case NONE:
					switch (type)
					{
						case BOW:
						case CROSSBOW:
						{
							// Bows increase by 4.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((4 * enchant) + (8 * overenchant));
							break;
						}
						default:
							// P. Atk. increases by 2 for all weapons with the exception of bows.
							// Starting at +4, P. Atk. bonus double.
							env.addValue((2 * enchant) + (4 * overenchant));
							break;
					}
					break;
			}
		}
	}
}
