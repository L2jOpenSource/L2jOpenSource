package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncPDefMod extends Func
{
	static final FuncPDefMod _fpa_instance = new FuncPDefMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncPDefMod()
	{
		super(Stats.POWER_DEFENCE, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		if (env.getCharacter() instanceof Player)
		{
			final Player player = env.getPlayer();
			final boolean isMage = player.isMageClass();
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				env.subValue(12);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
				env.subValue((isMage) ? 15 : 31);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
				env.subValue((isMage) ? 8 : 18);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				env.subValue(8);
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				env.subValue(7);
		}
		
		env.mulValue(env.getCharacter().getLevelMod());
	}
}