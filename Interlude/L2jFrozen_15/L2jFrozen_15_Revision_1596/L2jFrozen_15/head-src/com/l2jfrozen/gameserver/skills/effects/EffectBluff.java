package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.network.serverpackets.BeginRotation;
import com.l2jfrozen.gameserver.network.serverpackets.StopRotation;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author programmos, sword developers Implementation of the Bluff Effect
 */
public class EffectBluff extends L2Effect
{
	
	public EffectBluff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BLUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	/** Notify started */
	
	@Override
	public void onStart()
	{
		if (getEffected().isDead() || getEffected().isAfraid())
		{
			return;
		}
		
		if (getEffected() instanceof L2FolkInstance || getEffected() instanceof L2ControlTowerInstance || getEffected() instanceof L2ArtefactInstance || getEffected() instanceof L2EffectPointInstance || getEffected() instanceof L2SiegeFlagInstance || getEffected() instanceof L2SiegeSummonInstance)
		{
			return;
		}
		
		super.onStart();
		
		// break target
		getEffected().setTarget(null);
		// stop cast
		getEffected().breakCast();
		// stop attacking
		getEffected().breakAttack();
		// stop follow
		getEffected().getAI().stopFollow();
		// stop auto attack
		getEffected().getAI().clientStopAutoAttack();
		
		getEffected().broadcastPacket(new BeginRotation(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		// sometimes rotation didn't showed correctly ??
		getEffected().sendPacket(new ValidateLocation(getEffector()));
		getEffector().sendPacket(new ValidateLocation(getEffected()));
		onActionTime();
	}
}
