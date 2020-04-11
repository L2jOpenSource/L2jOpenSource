package com.l2jfrozen.gameserver.skills.conditions;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerState extends Condition
{
	public enum CheckPlayerState
	{
		RESTING,
		MOVING,
		RUNNING,
		FLYING,
		BEHIND,
		FRONT,
		SIDE
	}
	
	private final CheckPlayerState check;
	private final boolean required;
	
	public ConditionPlayerState(final CheckPlayerState check, final boolean required)
	{
		this.check = check;
		this.required = required;
	}
	
	@Override
	public boolean testImpl(final Env env)
	{
		switch (check)
		{
			case RESTING:
				if (env.player instanceof L2PcInstance)
				{
					return ((L2PcInstance) env.player).isSitting() == required;
				}
				return !required;
			case MOVING:
				return env.player.isMoving() == required;
			case RUNNING:
				return env.player.isMoving() == required && env.player.isRunning() == required;
			case FLYING:
				return env.player.isFlying() == required;
			case BEHIND:
				return env.player.isBehindTarget() == required;
			case FRONT:
				return env.player.isFrontTarget() == required;
			case SIDE:
				return env.player.isSideTarget() == required;
		}
		return !required;
	}
}