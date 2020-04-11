package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author Forsaiken
 */
public class EffectSignetAntiSummon extends L2Effect
{
	private EffectPoint _actor;
	
	public EffectSignetAntiSummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	public boolean onStart()
	{
		_actor = (EffectPoint) getEffected();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == getTotalCount() - 1)
			return true; // do nothing first time
			
		final int mpConsume = getSkill().getMpConsume();
		final Player caster = (Player) getEffector();
		
		for (Playable cha : _actor.getKnownTypeInRadius(Playable.class, getSkill().getSkillRadius()))
		{
			if (!caster.canAttackCharacter(cha))
				continue;
			
			final Player owner = cha.getActingPlayer();
			if (owner != null && owner.getSummon() != null)
			{
				if (mpConsume > getEffector().getCurrentMp())
				{
					getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
					return false;
				}
				getEffector().reduceCurrentMp(mpConsume);
				
				owner.getSummon().unSummon(owner);
				owner.getAI().notifyEvent(AiEventType.ATTACKED, getEffector());
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
			_actor.deleteMe();
	}
}