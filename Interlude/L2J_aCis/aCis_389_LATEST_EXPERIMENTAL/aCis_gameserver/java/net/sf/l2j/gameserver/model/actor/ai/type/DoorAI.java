package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public class DoorAI extends CreatureAI
{
	public DoorAI(Door door)
	{
		super(door);
	}
	
	@Override
	protected void onIntentionIdle()
	{
	}
	
	@Override
	protected void onIntentionActive()
	{
	}
	
	@Override
	protected void onIntentionAttack(Creature target, Boolean isShiftPressed)
	{
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc, Boat boat)
	{
	}
	
	@Override
	protected void onIntentionFollow(Creature target, Boolean isShiftPressed)
	{
	}
	
	@Override
	protected void onIntentionPickUp(WorldObject object, Boolean isShiftPressed)
	{
	}
	
	@Override
	public void onEvtAttacked(Creature attacker)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
	
	@Override
	protected void onEvtFinishedAttack()
	{
	}
	
	@Override
	protected void onEvtArrived(Boolean forceStopped)
	{
	}
	
	@Override
	protected void onEvtArrivedBlocked(SpawnLocation loc)
	{
	}
	
	@Override
	protected void onEvtCancel()
	{
	}
	
	@Override
	protected void onEvtDead()
	{
	}
}