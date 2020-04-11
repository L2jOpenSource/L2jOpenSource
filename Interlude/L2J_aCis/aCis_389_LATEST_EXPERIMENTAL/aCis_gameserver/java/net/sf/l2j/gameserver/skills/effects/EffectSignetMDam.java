package net.sf.l2j.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;

public class EffectSignetMDam extends AbstractEffect
{
	private EffectPoint _actor;
	
	public EffectSignetMDam(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
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
		NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
			template = NpcData.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		else
			return false;
		
		final EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		
		Location worldPosition = null;
		if (getEffector() instanceof Player && getSkill().getTargetType() == SkillTargetType.GROUND)
			worldPosition = ((Player) getEffector()).getCurrentSkillWorldPosition();
		
		effectPoint.setInvul(true);
		effectPoint.spawnMe((worldPosition != null) ? worldPosition : getEffector().getPosition());
		
		_actor = effectPoint;
		return true;
		
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() >= getTemplate().getCounter() - 2)
			return true; // do nothing first 2 times
			
		final Player caster = (Player) getEffector();
		
		final int mpConsume = getSkill().getMpConsume();
		
		final boolean sps = caster.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		final List<Creature> targets = new ArrayList<>();
		
		for (Creature cha : _actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius()))
		{
			if (cha == caster)
				continue;
			
			if (cha instanceof Attackable || cha instanceof Playable)
			{
				if (cha.isAlikeDead())
					continue;
				
				if (mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				
				caster.reduceCurrentMp(mpConsume);
				
				if (cha instanceof Playable)
				{
					if (caster.canAttackCharacter(cha))
					{
						targets.add(cha);
						caster.updatePvPStatus(cha);
					}
				}
				else
					targets.add(cha);
			}
		}
		
		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new Creature[targets.size()])));
			for (Creature target : targets)
			{
				final boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				final byte shld = Formulas.calcShldUse(caster, target, getSkill());
				
				final int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), shld, sps, bsps, mcrit);
				
				if (target instanceof Summon)
					target.broadcastStatusUpdate();
				
				if (mdam > 0)
				{
					// Manage cast break of the target (calculating rate, sending message...)
					Formulas.calcCastBreak(target, mdam);
					
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
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