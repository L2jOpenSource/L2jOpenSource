package com.l2jfrozen.gameserver.skills.funcs;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2WeaponType;

public class FuncEnchant extends Func
{
	
	public FuncEnchant(final Stats pStat, final int pOrder, final Object owner, final Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}
	
	@Override
	public void calc(final Env env)
	{
		if (cond != null && !cond.test(env))
		{
			return;
		}
		final L2ItemInstance item = (L2ItemInstance) funcOwner;
		final int cristall = item.getItem().getCrystalType();
		final Enum<?> itemType = item.getItemType();
		
		if (cristall == L2Item.CRYSTAL_NONE)
		{
			return;
		}
		int enchant = item.getEnchantLevel();
		
		int overenchant = 0;
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if (env.player != null && env.player instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) env.player;
			if (player.isInOlympiadMode() && Config.ALT_OLY_ENCHANT_LIMIT >= 0 && enchant + overenchant > Config.ALT_OLY_ENCHANT_LIMIT)
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
		
		if (stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE)
		{
			env.value += enchant + 3 * overenchant;
			return;
		}
		
		if (stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalType())
			{
				case L2Item.CRYSTAL_S:
					env.value += 4 * enchant + 8 * overenchant;
					break;
				case L2Item.CRYSTAL_A:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_B:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_C:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_D:
					env.value += 2 * enchant + 4 * overenchant;
					break;
			}
			return;
		}
		
		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_A:
				if (itemType == L2WeaponType.BOW)
				{
					env.value += 8 * enchant + 16 * overenchant;
				}
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384)
				{
					env.value += 5 * enchant + 10 * overenchant;
				}
				else
				{
					env.value += 4 * enchant + 8 * overenchant;
				}
				break;
			case L2Item.CRYSTAL_B:
				if (itemType == L2WeaponType.BOW)
				{
					env.value += 6 * enchant + 12 * overenchant;
				}
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384)
				{
					env.value += 4 * enchant + 8 * overenchant;
				}
				else
				{
					env.value += 3 * enchant + 6 * overenchant;
				}
				break;
			case L2Item.CRYSTAL_C:
				if (itemType == L2WeaponType.BOW)
				{
					env.value += 6 * enchant + 12 * overenchant;
				}
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384)
				{
					env.value += 4 * enchant + 8 * overenchant;
				}
				else
				{
					env.value += 3 * enchant + 6 * overenchant;
				}
				
				break;
			case L2Item.CRYSTAL_D:
				if (itemType == L2WeaponType.BOW)
				{
					env.value += 4 * enchant + 8 * overenchant;
				}
				else
				{
					env.value += 2 * enchant + 4 * overenchant;
				}
				break;
			case L2Item.CRYSTAL_S:
				if (itemType == L2WeaponType.BOW)
				{
					env.value += 10 * enchant + 20 * overenchant;
				}
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384)
				{
					env.value += 4 * enchant + 12 * overenchant;
				}
				else
				{
					env.value += 4 * enchant + 10 * overenchant;
				}
				break;
		}
		return;
	}
}
