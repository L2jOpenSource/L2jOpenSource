package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.BaseStats;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
	
	final int chargeSkillId;
	
	public L2SkillChargeDmg(final StatsSet set)
	{
		super(set);
		chargeSkillId = set.getInteger("charge_skill_id");
	}
	
	@Override
	public boolean checkCondition(final L2Character activeChar, final L2Object target, final boolean itemOrWeapon)
	{
		if (activeChar instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) activeChar;
			final EffectCharge e = (EffectCharge) player.getFirstEffect(chargeSkillId);
			if (e == null || e.numCharges < getNumCharges())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(getId());
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(activeChar, target, itemOrWeapon);
	}
	
	@Override
	public void useSkill(final L2Character caster, final L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// get the effect
		final EffectCharge effect = (EffectCharge) caster.getFirstEffect(chargeSkillId);
		if (effect == null || effect.numCharges < getNumCharges())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(getId());
			caster.sendPacket(sm);
			return;
		}
		
		double modifier = 0;
		modifier = (effect.getLevel() - getNumCharges()) * 0.33;
		
		if (getTargetType() != SkillTargetType.TARGET_AREA && getTargetType() != SkillTargetType.TARGET_MULTIFACE)
		{
			effect.numCharges -= getNumCharges();
		}
		
		if (caster instanceof L2PcInstance)
		{
			caster.sendPacket(new EtcStatusUpdate((L2PcInstance) caster));
		}
		
		if (effect.numCharges == 0)
		{
			effect.exit(false);
		}
		
		final boolean ss = caster.checkSs();
		
		for (final L2Object target2 : targets)
		{
			final L2ItemInstance weapon = caster.getActiveWeaponInstance();
			final L2Character target = (L2Character) target2;
			
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// TODO: should we use dual or not?
			// because if so, damage are lowered but we dont do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			
			// boolean dual = caster.isUsingDualWeapon();
			final boolean shld = Formulas.calcShldUse(caster, target);
			final boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER);
			boolean crit = false;
			
			if (getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * BaseStats.STR.calcBonus(caster));
			}
			
			// damage calculation
			int damage = (int) Formulas.calcPhysDam(caster, target, this, shld, false, false, soul);
			
			// Like L2OFF damage calculation crit is static 2x
			if (crit)
			{
				damage *= 2;
			}
			
			if (damage > 0)
			{
				double finalDamage = damage;
				finalDamage = finalDamage + (modifier * finalDamage);
				target.reduceCurrentHp(finalDamage, caster);
				caster.sendDamageMessage(target, (int) finalDamage, false, crit, false);
			}
			else
			{
				caster.sendDamageMessage(target, 0, false, false, true);
			}
		}
		
		if (ss)
		{
			caster.removeSs();
		}
		
		// effect self :]
		final L2Effect seffect = caster.getFirstEffect(getId());
		if (seffect != null && seffect.isSelfEffect())
		{
			// Replace old effect with new one.
			seffect.exit(false);
		}
		// cast self effect if any
		getEffectsSelf(caster);
	}
}