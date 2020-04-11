package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectSignetAntiSummon extends AbstractEffect
{
	private EffectPoint _actor;
	
	public EffectSignetAntiSummon(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
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
		if (getCount() == getTemplate().getCounter() - 1)
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