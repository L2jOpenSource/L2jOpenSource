package com.l2jfrozen.gameserver.skills.effects;

import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Env;

public final class EffectSignetAntiSummon extends L2Effect
{
	private L2EffectPointInstance actor;
	
	public EffectSignetAntiSummon(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public void onStart()
	{
		actor = (L2EffectPointInstance) getEffected();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
		{
			return true; // do nothing first time
		}
		final int mpConsume = getSkill().getMpConsume();
		
		for (final L2Character cha : actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
			{
				continue;
			}
			
			if (cha instanceof L2PlayableInstance)
			{
				final L2PcInstance owner = (L2PcInstance) cha;
				if (owner.getPet() != null)
				{
					if (mpConsume > getEffector().getStatus().getCurrentMp())
					{
						getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
						return false;
					}
					
					getEffector().reduceCurrentMp(mpConsume);
					
					owner.getPet().unSummon(owner);
					owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (actor != null)
		{
			actor.deleteMe();
		}
	}
}
