package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;

public class BoatAI extends CreatureAI
{
	public BoatAI(Boat boat)
	{
		super(boat);
	}
	
	@Override
	public void describeStateToPlayer(Player player)
	{
		if (getActor().isMoving())
			player.sendPacket(new VehicleDeparture(getActor()));
	}
	
	@Override
	public Boat getActor()
	{
		return (Boat) _actor;
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
	protected void onEvtArrived(Boolean forceStopped)
	{
		getActor().getMove().onArrival();
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
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
	
	@Override
	protected void onEvtFinishedCasting(Boolean success)
	{
	}
	
	@Override
	protected void clientActionFailed()
	{
	}
}