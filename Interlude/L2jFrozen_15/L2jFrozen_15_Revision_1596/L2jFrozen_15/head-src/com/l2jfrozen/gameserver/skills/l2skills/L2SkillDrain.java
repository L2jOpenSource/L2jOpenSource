package com.l2jfrozen.gameserver.skills.l2skills;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill
{
	
	private final float absorbPart;
	private final int absorbAbs;
	
	public L2SkillDrain(final StatsSet set)
	{
		super(set);
		
		absorbPart = set.getFloat("absorbPart", 0.f);
		absorbAbs = set.getInteger("absorbAbs", 0);
	}
	
	@Override
	public void useSkill(final L2Character activeChar, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = activeChar.checkSps();
		final boolean bss = activeChar.checkBss();
		
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}
			
			// Like L2OFF no effect on invul object except Npcs
			if (activeChar != target && (target.isInvul() && !(target instanceof L2NpcInstance)))
			{
				continue; // No effect on invulnerable chars unless they cast it themselves.
			}
			
			/*
			 * L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance(); if(weaponInst != null) { if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) { bss = true; weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); } else if(weaponInst.getChargedSpiritshot() ==
			 * L2ItemInstance.CHARGED_SPIRITSHOT) { ss = true; weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); } } // If there is no weapon equipped, check for an active summon. else if(activeChar instanceof L2Summon) { L2Summon activeSummon = (L2Summon) activeChar;
			 * if(activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) { bss = true; activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); } else if(activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT) { ss = true;
			 * activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); } }
			 */
			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			final int damage = (int) Formulas.calcMagicDam(activeChar, target, this, sps, bss, mcrit);
			
			int drain = 0;
			final int currentCp = (int) target.getStatus().getCurrentCp();
			final int currentHp = (int) target.getStatus().getCurrentHp();
			
			if (currentCp > 0)
			{
				if (damage < currentCp)
				{
					drain = 0;
				}
				else
				{
					drain = damage - currentCp;
				}
			}
			else if (damage > currentHp)
			{
				drain = currentHp;
			}
			else
			{
				drain = damage;
			}
			
			final double hpAdd = absorbAbs + absorbPart * drain;
			final double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;
			
			activeChar.setCurrentHp(hp);
			
			final StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				
				if (hasEffects() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				{
					if (target.reflectSkill(this))
					{
						activeChar.stopSkillEffects(getId());
						getEffects(null, activeChar, false, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(getId());
						activeChar.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, this, false, sps, bss))
						{
							getEffects(activeChar, target, false, sps, bss);
						}
						else
						{
							final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
				}
				
				target.reduceCurrentHp(damage, activeChar);
			}
			
			// Check to see if we should do the decay right after the cast
			if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2NpcInstance)
			{
				((L2NpcInstance) target).endDecayTask();
			}
		}
		
		if (bss)
		{
			activeChar.removeBss();
		}
		else if (sps)
		{
			activeChar.removeSps();
		}
		
		// effect self :]
		final L2Effect effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit(false);
		}
		// cast self effect if any
		getEffectsSelf(activeChar);
	}
	
	public void useCubicSkill(final L2CubicInstance activeCubic, final L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("L2SkillDrain: useCubicSkill()");
		}
		
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			
			final int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit);
			if (Config.DEBUG)
			{
				LOGGER.info("L2SkillDrain: useCubicSkill() -> damage = " + damage);
			}
			
			final double hpAdd = absorbAbs + absorbPart * damage;
			final L2PcInstance owner = activeCubic.getOwner();
			final double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));
			
			owner.setCurrentHp(hp);
			
			final StatusUpdate suhp = new StatusUpdate(owner.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner());
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
}