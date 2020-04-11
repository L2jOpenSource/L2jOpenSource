package net.sf.l2j.gameserver.model.item.kind;

import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatsSet;

import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameChance;

/**
 * This class is dedicated to the management of weapons.
 */
public final class Weapon extends Item
{
	private final WeaponType _type;
	private final int _rndDam;
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _mpConsume;
	private final int _mpConsumeReduceRate;
	private final int _mpConsumeReduceValue;
	private final boolean _isMagical;
	
	private IntIntHolder _enchant4Skill;
	
	private IntIntHolder _skillOnMagic;
	private Condition _skillOnMagicCondition;
	
	private IntIntHolder _skillOnCrit;
	private Condition _skillOnCritCondition;
	
	private final int _reuseDelay;
	
	private final int _reducedSoulshot;
	private final int _reducedSoulshotChance;
	
	public Weapon(StatsSet set)
	{
		super(set);
		
		_type = set.getEnum("weapon_type", WeaponType.class, WeaponType.NONE);
		_type1 = Item.TYPE1_WEAPON_RING_EARRING_NECKLACE;
		_type2 = Item.TYPE2_WEAPON;
		
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_rndDam = set.getInteger("random_damage", 0);
		_mpConsume = set.getInteger("mp_consume", 0);
		
		String[] reduce = set.getString("mp_consume_reduce", "0,0").split(",");
		_mpConsumeReduceRate = Integer.parseInt(reduce[0]);
		_mpConsumeReduceValue = Integer.parseInt(reduce[1]);
		
		_reuseDelay = set.getInteger("reuse_delay", 0);
		_isMagical = set.getBool("is_magical", false);
		
		String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
		_reducedSoulshotChance = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[0]) : 0;
		_reducedSoulshot = (reduced_soulshots.length == 2) ? Integer.parseInt(reduced_soulshots[1]) : 0;
		
		if (set.containsKey("enchant4_skill"))
			_enchant4Skill = set.getIntIntHolder("enchant4_skill");
		
		if (set.containsKey("oncast_skill"))
		{
			_skillOnMagic = set.getIntIntHolder("oncast_skill");
			
			if (set.containsKey("oncast_chance"))
				_skillOnMagicCondition = new ConditionGameChance(set.getInteger("oncast_chance"));
		}
		
		if (set.containsKey("oncrit_skill"))
		{
			_skillOnCrit = set.getIntIntHolder("oncrit_skill");
			
			if (set.containsKey("oncrit_chance"))
				_skillOnCritCondition = new ConditionGameChance(set.getInteger("oncrit_chance"));
		}
	}
	
	@Override
	public WeaponType getItemType()
	{
		return _type;
	}
	
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return the quantity of used SoulShot.
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	/**
	 * @return the quantity of used SpiritShot.
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	/**
	 * @return the reduced quantity of SoulShot used.
	 */
	public int getReducedSoulShot()
	{
		return _reducedSoulshot;
	}
	
	/**
	 * @return the chance to use reduced SoulShot.
	 */
	public int getReducedSoulShotChance()
	{
		return _reducedSoulshotChance;
	}
	
	/**
	 * @return the random damage inflicted by the {@link Weapon}.
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	/**
	 * @return the reuse delay of the {@link Weapon}.
	 */
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	/**
	 * @return true if the {@link Weapon} is considered as a mage weapon, false otherwise.
	 */
	public final boolean isMagical()
	{
		return _isMagical;
	}
	
	/**
	 * @return the MP consumption of the {@link Weapon}.
	 */
	public int getMpConsume()
	{
		if (_mpConsumeReduceRate > 0 && Rnd.get(100) < _mpConsumeReduceRate)
			return _mpConsumeReduceValue;
		
		return _mpConsume;
	}
	
	/**
	 * @return the passive {@link L2Skill} when a {@link Weapon} owner equips a weapon +4 (used for duals SA).
	 */
	public L2Skill getEnchant4Skill()
	{
		return (_enchant4Skill == null) ? null : _enchant4Skill.getSkill();
	}
	
	/**
	 * Cast a {@link L2Skill} upon critical hit.
	 * @param caster : The Creature caster.
	 * @param target : The Creature target.
	 */
	public void castSkillOnCrit(Creature caster, Creature target)
	{
		if (_skillOnCrit == null)
			return;
		
		final L2Skill skillOnCrit = _skillOnCrit.getSkill();
		if (skillOnCrit == null)
			return;
		
		if (_skillOnCritCondition != null && !_skillOnCritCondition.test(caster, target, skillOnCrit))
			return;
		
		final byte shld = Formulas.calcShldUse(caster, target, skillOnCrit);
		if (!Formulas.calcSkillSuccess(caster, target, skillOnCrit, shld, false))
			return;
		
		final AbstractEffect effect = target.getFirstEffect(skillOnCrit.getId());
		if (effect != null)
			effect.exit();
		
		skillOnCrit.getEffects(caster, target, shld, false);
	}
	
	/**
	 * Cast a {@link L2Skill} upon magic use.
	 * @param caster : The Creature caster.
	 * @param target : The Creature target.
	 * @param trigger : The L2Skill triggering this action.
	 */
	public void castSkillOnMagic(Creature caster, Creature target, L2Skill trigger)
	{
		if (_skillOnMagic == null)
			return;
		
		final L2Skill skillOnMagic = _skillOnMagic.getSkill();
		if (skillOnMagic == null)
			return;
		
		// Trigger only same type of skill.
		if (trigger.isOffensive() != skillOnMagic.isOffensive())
			return;
		
		// No buffing with toggle or potions.
		if (trigger.isToggle() || trigger.isPotion())
			return;
		
		if (_skillOnMagicCondition != null && !_skillOnMagicCondition.test(caster, target, skillOnMagic))
			return;
		
		final byte shld = Formulas.calcShldUse(caster, target, skillOnMagic);
		if (skillOnMagic.isOffensive() && !Formulas.calcSkillSuccess(caster, target, skillOnMagic, shld, false))
			return;
		
		// Send message before resist attempt.
		if (caster instanceof Player)
			caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED).addSkillName(skillOnMagic));
		
		final Creature[] targets = new Creature[1];
		targets[0] = target;
		
		// Get the skill handler corresponding to the skill type - Launch the magic skill and calculate its effects.
		final ISkillHandler handler = SkillHandler.getInstance().getHandler(skillOnMagic.getSkillType());
		if (handler != null)
			handler.useSkill(caster, skillOnMagic, targets);
		else
			skillOnMagic.useSkill(caster, targets);
		
		// notify quests of a skill use
		if (caster instanceof Player)
		{
			// Mobs in range 1000 see spell
			for (Npc npcMob : caster.getKnownTypeInRadius(Npc.class, 1000))
			{
				final List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
				if (scripts != null)
					for (Quest quest : scripts)
						quest.notifySkillSee(npcMob, (Player) caster, skillOnMagic, targets, false);
			}
		}
	}
}