package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

/**
 * @see Func
 */
public class FuncMDefMod extends Func
{
	private static final FuncMDefMod INSTANCE = new FuncMDefMod();
	
	private FuncMDefMod()
	{
		super(null, Stats.MAGIC_DEFENCE, 10, 0, null);
	}
	
	@Override
	public double calc(Creature effector, Creature effected, L2Skill skill, double base, double value)
	{
		if (effector instanceof Player)
		{
			final Player player = (Player) effector;
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				value -= 5;
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				value -= 5;
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				value -= 9;
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				value -= 9;
			
			if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				value -= 13;
		}
		return value * Formulas.MEN_BONUS[effector.getMEN()] * effector.getLevelMod();
	}
	
	public static Func getInstance()
	{
		return INSTANCE;
	}
}