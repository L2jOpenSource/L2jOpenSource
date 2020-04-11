package com.l2jfrozen.gameserver.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.conditions.ConditionGameChance;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;

/**
 * This class is dedicated to the management of weapons.
 * @version $Revision: 1.4.2.3.2.5 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2Weapon extends L2Item
{
	private final int soulShotCount;
	private final int spiritShotCount;
	private final int pDam;
	private final int rndDam;
	private final int critical;
	private final double hitModifier;
	private final int avoidModifier;
	private final int shieldDef;
	private final double shieldDefRate;
	private final int atkSpeed;
	private final int atkReuse;
	private final int mpConsume;
	private final int mDam;
	private L2Skill itemSkill = null; // for passive skill
	private L2Skill enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	
	// Attached skills for Special Abilities
	protected L2Skill[] skillsOnCast;
	protected L2Skill[] skillsOnCrit;
	
	/**
	 * Constructor for Weapon.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>soulShotCount & spiritShotCount</LI>
	 * <LI>pDam & mDam & rndDam</LI>
	 * <LI>critical</LI>
	 * <LI>hitModifier</LI>
	 * <LI>avoidModifier</LI>
	 * <LI>shieldDes & shieldDefRate</LI>
	 * <LI>atkSpeed & atkReuse</LI>
	 * <LI>mpConsume</LI>
	 * @param type : L2ArmorType designating the type of armor
	 * @param set  : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see        L2Item constructor
	 */
	public L2Weapon(final L2WeaponType type, final StatsSet set)
	{
		super(type, set);
		soulShotCount = set.getInteger("soulshots");
		spiritShotCount = set.getInteger("spiritshots");
		pDam = set.getInteger("p_dam");
		rndDam = set.getInteger("rnd_dam");
		critical = set.getInteger("critical");
		hitModifier = set.getDouble("hit_modify");
		avoidModifier = set.getInteger("avoid_modify");
		shieldDef = set.getInteger("shield_def");
		shieldDefRate = set.getDouble("shield_def_rate");
		atkSpeed = set.getInteger("atk_speed");
		atkReuse = set.getInteger("atk_reuse", type == L2WeaponType.BOW ? 1500 : 0);
		mpConsume = set.getInteger("mp_consume");
		mDam = set.getInteger("m_dam");
		
		int sId = set.getInteger("item_skill_id");
		int sLv = set.getInteger("item_skill_lvl");
		if (sId > 0 && sLv > 0)
		{
			itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
		}
		
		sId = set.getInteger("enchant4_skill_id");
		sLv = set.getInteger("enchant4_skill_lvl");
		if (sId > 0 && sLv > 0)
		{
			enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);
		}
		
		sId = set.getInteger("onCast_skill_id");
		sLv = set.getInteger("onCast_skill_lvl");
		int sCh = set.getInteger("onCast_skill_chance");
		if (sId > 0 && sLv > 0 && sCh > 0)
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCast(skill);
		}
		
		sId = set.getInteger("onCrit_skill_id");
		sLv = set.getInteger("onCrit_skill_lvl");
		sCh = set.getInteger("onCrit_skill_chance");
		if (sId > 0 && sLv > 0 && sCh > 0)
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCrit(skill);
		}
	}
	
	/**
	 * Returns the type of Weapon
	 * @return L2WeaponType
	 */
	@Override
	public L2WeaponType getItemType()
	{
		return (L2WeaponType) super.type;
	}
	
	/**
	 * Returns the ID of the Etc item after applying the mask.
	 * @return int : ID of the Weapon
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Returns the quantity of SoulShot used.
	 * @return int
	 */
	public int getSoulShotCount()
	{
		return soulShotCount;
	}
	
	/**
	 * Returns the quatity of SpiritShot used.
	 * @return int
	 */
	public int getSpiritShotCount()
	{
		return spiritShotCount;
	}
	
	/**
	 * Returns the physical damage.
	 * @return int
	 */
	public int getPDamage()
	{
		return pDam;
	}
	
	/**
	 * Returns the random damage inflicted by the weapon
	 * @return int
	 */
	public int getRandomDamage()
	{
		return rndDam;
	}
	
	/**
	 * Returns the attack speed of the weapon
	 * @return int
	 */
	public int getAttackSpeed()
	{
		return atkSpeed;
	}
	
	/**
	 * Return the Attack Reuse Delay of the L2Weapon.<BR>
	 * <BR>
	 * @return int
	 */
	public int getAttackReuseDelay()
	{
		return atkReuse;
	}
	
	/**
	 * Returns the avoid modifier of the weapon
	 * @return int
	 */
	public int getAvoidModifier()
	{
		return avoidModifier;
	}
	
	/**
	 * Returns the rate of critical hit
	 * @return int
	 */
	public int getCritical()
	{
		return critical;
	}
	
	/**
	 * Returns the hit modifier of the weapon
	 * @return double
	 */
	public double getHitModifier()
	{
		return hitModifier;
	}
	
	/**
	 * Returns the magical damage inflicted by the weapon
	 * @return int
	 */
	public int getMDamage()
	{
		return mDam;
	}
	
	/**
	 * Returns the MP consumption with the weapon
	 * @return int
	 */
	public int getMpConsume()
	{
		return mpConsume;
	}
	
	/**
	 * Returns the shield defense of the weapon
	 * @return int
	 */
	public int getShieldDef()
	{
		return shieldDef;
	}
	
	/**
	 * Returns the rate of shield defense of the weapon
	 * @return double
	 */
	public double getShieldDefRate()
	{
		return shieldDefRate;
	}
	
	/**
	 * Returns passive skill linked to that weapon
	 * @return
	 */
	public L2Skill getSkill()
	{
		return itemSkill;
	}
	
	/**
	 * Returns skill that player get when has equiped weapon +4 or more (for duals SA)
	 * @return
	 */
	public L2Skill getEnchant4Skill()
	{
		return enchant4Skill;
	}
	
	/**
	 * Returns array of Func objects containing the list of functions used by the weapon
	 * @param  instance : L2ItemInstance pointing out the weapon
	 * @param  player   : L2Character pointing out the player
	 * @return          Func[] : array of functions
	 */
	@Override
	public Func[] getStatFuncs(final L2ItemInstance instance, final L2Character player)
	{
		final List<Func> funcs = new ArrayList<>();
		if (funcTemplates != null)
		{
			for (final FuncTemplate t : funcTemplates)
			{
				final Env env = new Env();
				env.player = player;
				env.item = instance;
				final Func f = t.getFunc(env, instance);
				if (f != null)
				{
					funcs.add(f);
				}
			}
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	/**
	 * Returns effects of skills associated with the item to be triggered onHit.
	 * @param  caster : L2Character pointing out the caster
	 * @param  target : L2Character pointing out the target
	 * @param  crit   : boolean tells whether the hit was critical
	 * @return        L2Effect[] : array of effects generated by the skill
	 */
	public L2Effect[] getSkillEffects(final L2Character caster, final L2Character target, final boolean crit)
	{
		if (skillsOnCrit == null || !crit)
		{
			return emptyEffectSet;
		}
		final List<L2Effect> effects = new ArrayList<>();
		
		for (final L2Skill skill : skillsOnCrit)
		{
			if (target.isRaid() && (skill.getSkillType() == SkillType.CONFUSION || skill.getSkillType() == SkillType.MUTE || skill.getSkillType() == SkillType.PARALYZE || skill.getSkillType() == SkillType.ROOT))
			{
				continue; // These skills should not work on RaidBoss
			}
			
			if (!skill.checkCondition(caster, target, true))
			{
				continue; // Skill condition not met
			}
			
			if (target.getFirstEffect(skill.getId()) != null)
			{
				target.getFirstEffect(skill.getId()).exit(false);
			}
			for (final L2Effect e : skill.getEffects(caster, target, false, false, false))
			{
				effects.add(e);
			}
		}
		if (effects.size() == 0)
		{
			return emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * Returns effects of skills associated with the item to be triggered onCast.
	 * @param  caster  : L2Character pointing out the caster
	 * @param  target  : L2Character pointing out the target
	 * @param  trigger : L2Skill pointing out the skill triggering this action
	 * @return         L2Effect[] : array of effects generated by the skill
	 */
	public boolean getSkillEffects(final L2Character caster, final L2Character target, final L2Skill trigger)
	{
		boolean output = false;
		
		if (skillsOnCast == null)
		{
			return output;
		}
		
		// return emptyEffectSet;
		// List<L2Effect> effects = new ArrayList<L2Effect>();
		
		for (final L2Skill skill : skillsOnCast)
		{
			if (trigger.isOffensive() != skill.isOffensive())
			{
				continue; // Trigger only same type of skill
			}
			
			if (trigger.getId() >= 1320 && trigger.getId() <= 1322)
			{
				continue; // No buff with Common and Dwarven Craft
			}
			
			if (trigger.isPotion())
			{
				continue; // No buff with potions
			}
			
			if (target.isRaid() && (skill.getSkillType() == SkillType.CONFUSION || skill.getSkillType() == SkillType.MUTE || skill.getSkillType() == SkillType.PARALYZE || skill.getSkillType() == SkillType.ROOT))
			{
				continue; // These skills should not work on RaidBoss
			}
			
			if (trigger.isToggle()/* && skill.getSkillType() == SkillType.BUFF */)
			{
				continue; // No buffing with toggle skills
			}
			
			if (!skill.checkCondition(caster, target, true)) // check skill condition and chance
			{
				continue; // Skill condition not met
			}
			
			try
			{
				// Get the skill handler corresponding to the skill type
				final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				
				final L2Character[] targets = new L2Character[1];
				targets[0] = target;
				
				// Launch the magic skill and calculate its effects
				if (handler != null)
				{
					handler.useSkill(caster, skill, targets);
				}
				else
				{
					skill.useSkill(caster, targets);
				}
				
				if (caster instanceof L2PcInstance && target instanceof L2NpcInstance)
				{
					for (final Quest quest : ((L2NpcInstance) target).getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_USE))
					{
						quest.notifySkillUse((L2NpcInstance) target, (L2PcInstance) caster, skill);
					}
				}
				
				output = true;
			}
			catch (final IOException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		// if(effects.size() == 0)
		// return emptyEffectSet;
		// return effects.toArray(new L2Effect[effects.size()]);
		
		return output;
	}
	
	/**
	 * Add the L2Skill skill to the list of skills generated by the item triggered by critical hit
	 * @param skill : L2Skill
	 */
	public void attachOnCrit(final L2Skill skill)
	{
		if (skillsOnCrit == null)
		{
			skillsOnCrit = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = skillsOnCrit.length;
			final L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(skillsOnCrit, 0, tmp, 0, len);
			tmp[len] = skill;
			skillsOnCrit = tmp;
		}
	}
	
	/**
	 * Add the L2Skill skill to the list of skills generated by the item triggered by casting spell
	 * @param skill : L2Skill
	 */
	public void attachOnCast(final L2Skill skill)
	{
		if (skillsOnCast == null)
		{
			skillsOnCast = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = skillsOnCast.length;
			final L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(skillsOnCast, 0, tmp, 0, len);
			tmp[len] = skill;
			skillsOnCast = tmp;
		}
	}
}
