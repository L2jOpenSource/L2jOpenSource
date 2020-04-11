package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.ai.type.TamedBeastAI;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;

/**
 * A tamed beast behaves a lot like a pet and has an owner. Some points :
 * <ul>
 * <li>feeding another beast to level 4 will vanish your actual tamed beast.</li>
 * <li>running out of spices will vanish your actual tamed beast. There's a 1min food check timer.</li>
 * <li>running out of the Beast Farm perimeter will vanish your tamed beast.</li>
 * <li>no need to force attack it, it's a normal monster.</li>
 * </ul>
 * This class handles the running tasks (such as skills use and feed) of the mob.
 */
public final class TamedBeast extends FeedableBeast
{
	protected int _foodId;
	protected Player _owner;
	
	public TamedBeast(int objectId, NpcTemplate template, Player owner, int foodId, Location loc)
	{
		super(objectId, template);
		
		disableCoreAi(true);
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
		setTitle(owner.getName());
		
		_owner = owner;
		owner.setTamedBeast(this);
		
		_foodId = foodId;
		
		spawnMe(loc);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Clean up actual trained beast.
		if (_owner != null)
			_owner.setTamedBeast(null);
		
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		stopHpMpRegeneration();
		getMove().stopFollow();
		
		// Clean up actual trained beast.
		if (_owner != null)
			_owner.setTamedBeast(null);
		
		super.deleteMe();
	}
	
	public int getFoodId()
	{
		return _foodId;
	}
	
	@Override
	public CreatureAI getAI()
	{
		CreatureAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
					_ai = ai = new TamedBeastAI(this);
			}
		}
		return ai;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
}