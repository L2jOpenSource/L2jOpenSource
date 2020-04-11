package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.templates.L2Weapon;

/**
 * @author mkizub
 */
public class ConditionTargetUsesWeaponKind extends Condition
{
	private final int weaponMask;
	
	public ConditionTargetUsesWeaponKind(final int weaponMask)
	{
		this.weaponMask = weaponMask;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		
		if (env.target == null)
		{
			return false;
		}
		
		final L2Weapon item = env.target.getActiveWeaponItem();
		
		if (item == null)
		{
			return false;
		}
		
		return (item.getItemType().mask() & weaponMask) != 0;
	}
}
