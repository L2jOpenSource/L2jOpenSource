package com.l2jfrozen.gameserver.skills;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.managers.ClassDamageManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2SiegeClan;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.model.entity.siege.Siege;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.conditions.ConditionPlayerState;
import com.l2jfrozen.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import com.l2jfrozen.gameserver.skills.conditions.ConditionUsingItemType;
import com.l2jfrozen.gameserver.skills.effects.EffectTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.templates.L2PcTemplate;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.util.Util;
import com.l2jfrozen.util.StringUtil;
import com.l2jfrozen.util.random.Rnd;

/**
 * Global calculations, can be modified by server admins
 * @author L2JFrozen dev
 */
public final class Formulas
{
	/** Regen Task period */
	protected static final Logger LOGGER = Logger.getLogger(L2Character.class);
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	
	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] instancies = new FuncAddLevel3[Stats.NUM_STATS];
		
		static Func getInstance(final Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (instancies[pos] == null)
			{
				instancies[pos] = new FuncAddLevel3(stat);
			}
			return instancies[pos];
		}
		
		private FuncAddLevel3(final Stats pStat)
		{
			super(pStat, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}
	
	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] instancies = new FuncMultLevelMod[Stats.NUM_STATS];
		
		static Func getInstance(final Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (instancies[pos] == null)
			{
				instancies[pos] = new FuncMultLevelMod(stat);
			}
			return instancies[pos];
		}
		
		private FuncMultLevelMod(final Stats pStat)
		{
			super(pStat, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}
	
	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] instancies = new FuncMultRegenResting[Stats.NUM_STATS];
		
		/**
		 * @param  stat
		 * @return      the Func object corresponding to the state concerned.
		 */
		static Func getInstance(final Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (instancies[pos] == null)
			{
				instancies[pos] = new FuncMultRegenResting(stat);
			}
			
			return instancies[pos];
		}
		
		/**
		 * Constructor of the FuncMultRegenResting.<BR>
		 * <BR>
		 * @param pStat
		 */
		private FuncMultRegenResting(final Stats pStat)
		{
			super(pStat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}
		
		/**
		 * Calculate the modifier of the state concerned.<BR>
		 * <BR>
		 */
		@Override
		public void calc(final Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			
			env.value *= 1.45;
		}
	}
	
	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod fpa_instance = new FuncPAtkMod();
		
		static Func getInstance()
		{
			return fpa_instance;
		}
		
		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			if (env.player instanceof L2PetInstance)
			{
				if (env.player.getActiveWeaponInstance() != null)
				{
					env.value *= BaseStats.STR.calcBonus(env.player);
				}
			}
			else
			{
				env.value *= BaseStats.STR.calcBonus(env.player) * env.player.getLevelMod();
			}
		}
	}
	
	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod fma_instance = new FuncMAtkMod();
		
		static Func getInstance()
		{
			return fma_instance;
		}
		
		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final double intb = BaseStats.INT.calcBonus(env.player);
			final double lvlb = env.player.getLevelMod();
			env.value *= (lvlb * lvlb) * (intb * intb);
		}
	}
	
	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod fmm_instance = new FuncMDefMod();
		
		static Func getInstance()
		{
			return fmm_instance;
		}
		
		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
				final L2PcInstance p = (L2PcInstance) env.player;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				{
					env.value -= 13;
				}
			}
			env.value *= BaseStats.MEN.calcBonus(env.player) * env.player.getLevelMod();
		}
	}
	
	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod fmm_instance = new FuncPDefMod();
		
		static Func getInstance()
		{
			return fmm_instance;
		}
		
		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			if (env.player instanceof L2PcInstance)
			{
				final L2PcInstance p = (L2PcInstance) env.player;
				final boolean hasMagePDef = (p.getClassId().isMage() || p.getClassId().getId() == 0x31); // orc mystics are a special case
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				{
					env.value -= 12;
				}
				final L2ItemInstance chest = p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if (chest != null)
				{
					env.value -= hasMagePDef ? 15 : 31;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null || (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR))
				{
					env.value -= hasMagePDef ? 8 : 18;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				{
					env.value -= 8;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				{
					env.value -= 7;
				}
			}
			env.value *= env.player.getLevelMod();
		}
	}
	
	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange fbar_instance = new FuncBowAtkRange();
		
		static Func getInstance()
		{
			return fbar_instance;
		}
		
		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}
		
		@Override
		public void calc(final Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			env.value += 460;
		}
	}
	
	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy faa_instance = new FuncAtkAccuracy();
		
		static Func getInstance()
		{
			return faa_instance;
		}
		
		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final int level = env.player.getLevel();
			// [Square(DEX)]*6 + lvl + weapon hitbonus;
			
			final L2Character p = env.player;
			if (p instanceof L2PetInstance)
			{
				env.value += Math.sqrt(env.player.getDEX());
			}
			else
			{
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 77)
				{
					env.value += (level - 77);
				}
				if (level > 69)
				{
					env.value += (level - 69);
				}
				if (env.player instanceof L2Summon)
				{
					env.value += (level < 60) ? 4 : 5;
				}
			}
		}
	}
	
	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion fae_instance = new FuncAtkEvasion();
		
		static Func getInstance()
		{
			return fae_instance;
		}
		
		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final int level = env.player.getLevel();
			// [Square(DEX)]*6 + lvl;
			
			final L2Character p = env.player;
			if (p instanceof L2PetInstance)
			{
				env.value += Math.sqrt(env.player.getDEX());
			}
			else
			{
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 77)
				{
					env.value += (level - 77);
				}
				if (level > 69)
				{
					env.value += (level - 69);
				}
			}
		}
	}
	
	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical fac_instance = new FuncAtkCritical();
		
		static Func getInstance()
		{
			return fac_instance;
		}
		
		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x09, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
			
			final L2Character p = env.player;
			if (!(p instanceof L2PetInstance))
			{
				env.value *= 10;
			}
			
			env.baseValue = env.value;
		}
	}
	
	static class FuncMAtkCritical extends Func
	{
		static final FuncMAtkCritical fac_instance = new FuncMAtkCritical();
		
		static Func getInstance()
		{
			return fac_instance;
		}
		
		private FuncMAtkCritical()
		{
			super(Stats.MCRITICAL_RATE, 0x30, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final L2Character p = env.player;
			if (p instanceof L2Summon)
			{
				env.value = 8; // TODO: needs retail value
			}
			else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() != null)
			{
				env.value *= BaseStats.WIT.calcBonus(p);
			}
		}
	}
	
	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed fms_instance = new FuncMoveSpeed();
		
		static Func getInstance()
		{
			return fms_instance;
		}
		
		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}
	
	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed fas_instance = new FuncPAtkSpeed();
		
		static Func getInstance()
		{
			return fas_instance;
		}
		
		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}
	
	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed fas_instance = new FuncMAtkSpeed();
		
		static Func getInstance()
		{
			return fas_instance;
		}
		
		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.player);
		}
	}
	
	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR fh_instance = new FuncHennaSTR();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatSTR();
			}
		}
	}
	
	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX fh_instance = new FuncHennaDEX();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatDEX();
			}
		}
	}
	
	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT fh_instance = new FuncHennaINT();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatINT();
			}
		}
	}
	
	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN fh_instance = new FuncHennaMEN();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatMEN();
			}
		}
	}
	
	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON fh_instance = new FuncHennaCON();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatCON();
			}
		}
	}
	
	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT fh_instance = new FuncHennaWIT();
		
		static Func getInstance()
		{
			return fh_instance;
		}
		
		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			// L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			final L2PcInstance pc = (L2PcInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatWIT();
			}
		}
	}
	
	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd fmha_instance = new FuncMaxHpAdd();
		
		static Func getInstance()
		{
			return fmha_instance;
		}
		
		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double hpmod = t.lvlHpMod * lvl;
			final double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			final double hpmin = (t.lvlHpAdd * lvl) + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}
	
	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul fmhm_instance = new FuncMaxHpMul();
		
		static Func getInstance()
		{
			return fmhm_instance;
		}
		
		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}
	
	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd fmca_instance = new FuncMaxCpAdd();
		
		static Func getInstance()
		{
			return fmca_instance;
		}
		
		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double cpmod = t.lvlCpMod * lvl;
			final double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			final double cpmin = (t.lvlCpAdd * lvl) + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}
	
	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul fmcm_instance = new FuncMaxCpMul();
		
		static Func getInstance()
		{
			return fmcm_instance;
		}
		
		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}
	
	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd fmma_instance = new FuncMaxMpAdd();
		
		static Func getInstance()
		{
			return fmma_instance;
		}
		
		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			final L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double mpmod = t.lvlMpMod * lvl;
			final double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			final double mpmin = (t.lvlMpAdd * lvl) + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}
	
	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul fmmm_instance = new FuncMaxMpMul();
		
		static Func getInstance()
		{
			return fmmm_instance;
		}
		
		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}
		
		@Override
		public void calc(final Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.player);
		}
	}
	
	private static final Formulas instance = new Formulas();
	
	public static Formulas getInstance()
	{
		return instance;
	}
	
	private Formulas()
	{
	}
	
	/**
	 * @param  cha
	 * @return     the period between 2 regeneration task (3s for L2Character, 5 min for L2DoorInstance).
	 */
	public static int getRegeneratePeriod(final L2Character cha)
	{
		if (cha instanceof L2DoorInstance)
		{
			return HP_REGENERATE_PERIOD * 100; // 5 mins
		}
		
		return HP_REGENERATE_PERIOD; // 3s
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a Mathematics function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * @return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.
	 */
	public Calculator[] getStdNPCCalculators()
	{
		final Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		std[Stats.MAX_HP.ordinal()] = new Calculator();
		std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());
		
		std[Stats.MAX_MP.ordinal()] = new Calculator();
		std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());
		
		std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());
		
		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());
		
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());
		
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());
		
		std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());
		
		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());
		
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());
		
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());
		
		std[Stats.RUN_SPEED.ordinal()] = new Calculator();
		std[Stats.RUN_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());
		
		return std;
	}
	
	/*
	 * // Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator(); std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance()); // Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
	 * std[Stats.EVASION_RATE.ordinal()] = new Calculator(); std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance()); return std; }
	 */
	
	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(final L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			
			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
		else if (cha instanceof L2PetInstance)
		{
			cha.addStatFunc(FuncPAtkMod.getInstance());
			// cha.addStatFunc(FuncMAtkMod.getInstance());
			// cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
		}
		else if (cha instanceof L2Summon)
		{
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
		}
	}
	
	/**
	 * Calculate the HP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param  cha
	 * @return
	 */
	public final static double calcHpRegen(final L2Character cha)
	{
		double init = cha.getTemplate().baseHpReg;
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}
		
		if (cha instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) cha;
			
			// Calculate correct baseHpReg value for certain level of PC
			init += (player.getLevel() > 10) ? ((player.getLevel() - 1) / 10.0) : 0.5;
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				hpRegenMultiplier *= Formulas.calcFestivalRegenModifier(player);
			}
			else
			{
				final double siegeModifier = Formulas.calcSiegeRegenModifer(player);
				if (siegeModifier > 0)
				{
					hpRegenMultiplier *= siegeModifier;
				}
			}
			
			if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
			{
				final int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0)
				{
					final ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100;
						}
					}
				}
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				hpRegenBonus += 2;
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}
			
			// Add CON bonus
			// init *= cha.getLevelMod() * CONbonus[cha.getCON()];
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		
		if (init < 1)
		{
			init = 1;
		}
		
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}
	
	/**
	 * Calculate the MP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param  cha
	 * @return
	 */
	public final static double calcMpRegen(final L2Character cha)
	{
		double init = cha.getTemplate().baseMpReg;
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;
		
		if (cha instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) cha;
			
			// Calculate correct baseMpReg value for certain level of PC
			init += 0.3 * ((player.getLevel() - 1) / 10.0);
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				mpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(L2Character.ZONE_MOTHERTREE))
			{
				mpRegenBonus += 1;
			}
			
			if (player.isInsideZone(L2Character.ZONE_CLANHALL) && player.getClan() != null)
			{
				final int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0)
				{
					final ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100;
						}
					}
				}
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				mpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				mpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				mpRegenMultiplier *= 0.7; // Running
			}
			
			// Add MEN bonus
			init *= cha.getLevelMod() * BaseStats.MEN.calcBonus(cha);
		}
		
		if (init < 1)
		{
			init = 1;
		}
		
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}
	
	/**
	 * Calculate the CP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param  cha
	 * @return
	 */
	public final static double calcCpRegen(final L2Character cha)
	{
		double init = cha.getTemplate().baseHpReg;
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		final double cpRegenBonus = 0;
		
		if (cha instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) cha;
			
			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else
		{
			// Calculate Movement bonus
			if (!cha.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (cha.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		
		// Apply CON bonus
		init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		if (init < 1)
		{
			init = 1;
		}
		
		return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
	}
	
	@SuppressWarnings("deprecation")
	public final static double calcFestivalRegenModifier(final L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;
		
		// If the player isn't found in the festival, leave the regen rate as it is.
		if (festivalId < 0)
		{
			return 0;
		}
		
		// Retrieve the X and Y coords for the center of the festival arena the player is in.
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		}
		else
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
		}
		
		// Check the distance between the player and the player spawn point, in the center of the arena.
		final double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
		
		if (Config.DEBUG)
		{
			LOGGER.info("Distance: " + distToCenter + ", RegenMulti: " + distToCenter * 2.5 / 50);
		}
		
		return 1.0 - distToCenter * 0.0005; // Maximum Decreased Regen of ~ -65%;
	}
	
	public final static double calcSiegeRegenModifer(final L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null)
		{
			return 0;
		}
		
		final Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		if (siege == null || !siege.getIsInProgress())
		{
			return 0;
		}
		
		final L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().size() == 0 || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}
		
		return 1.5; // If all is true, then modifer will be 50% more
	}
	
	/**
	 * Calculate blow damage based on cAtk
	 * @param  attacker
	 * @param  target
	 * @param  skill
	 * @param  shld
	 * @param  crit
	 * @param  ss
	 * @return
	 */
	public static double calcBlowDamage(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean shld, final boolean crit, final boolean ss)
	{
		/*
		 * wtf is this shit -Nefer if((skill.getCondition() & L2Skill.COND_BEHIND) != 0 && !attacker.isBehind(target)) return 0;
		 */
		
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		if (ss)
		{
			damage *= 2.;
		}
		
		if (shld)
		{
			defence += target.getShldDef();
		}
		
		if (crit)
		{
			
			// double cAtkMultiplied = (damage) + attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
			final double improvedDamageByCriticalVuln = target.calcStat(Stats.CRIT_VULN, damage, target, skill);
			final double improvedDamageByCriticalVulnAndAdd = (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, improvedDamageByCriticalVuln, target, skill));
			
			if (Config.DEBUG)
			{
				LOGGER.info("Attacker '" + attacker.getName() + "' Dagger Critical Damage Debug:");
				LOGGER.info("	-	Initial Damage:  " + damage);
				LOGGER.info("	-	improvedDamageByCriticalVuln: " + improvedDamageByCriticalVuln);
				LOGGER.info("	-	improvedDamageByCriticalVulnAndAdd: " + improvedDamageByCriticalVulnAndAdd);
			}
			
			damage = improvedDamageByCriticalVulnAndAdd;
			
			/*
			 * damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, (damage+power), target, skill); damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.5; damage *= target.calcStat(Stats.CRIT_VULN, target.getTemplate().baseCritVuln, target, skill);
			 */
			
			final L2Effect vicious = attacker.getFirstEffect(312);
			if (vicious != null && damage > 1)
			{
				for (final Func func : vicious.getStatFuncs())
				{
					final Env env = new Env();
					env.player = attacker;
					env.target = target;
					env.skill = skill;
					env.value = damage;
					func.calc(env);
					damage = (int) env.value;
				}
			}
			
		}
		
		// skill add is not influenced by criticals improvements, so it's applied later
		double skillpower = skill.getPower(attacker);
		final float ssboost = skill.getSSBoost();
		if (ssboost <= 0)
		{
			damage += skillpower;
		}
		else if (ssboost > 0)
		{
			if (ss)
			{
				skillpower *= ssboost;
				damage += skillpower;
			}
			else
			{
				damage += skillpower;
			}
		}
		
		// possible skill power critical hit, based on Official Description:
		/*
		 * skill critical effects (skill damage x2) have been added
		 */
		if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.DEX.calcBonus(attacker)))
		{
			damage *= 2;
		}
		
		damage *= 70. / defence;
		
		// finally, apply the critical multiplier if present (it's not subjected to defense)
		if (crit)
		{
			damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
		}
		
		// Multiplier should be removed, it's false ??
		// damage += 1.5 * attacker.calcStat(Stats.CRITICAL_DAMAGE, damage + power, target, skill);
		// damage *= (double)attacker.getLevel()/target.getLevel();
		
		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		// get the natural vulnerability for the template
		if (target instanceof L2NpcInstance)
		{
			damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		
		// After C4 nobles make 4% more dmg in PvP.
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isNoble() && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			damage *= 1.04;
		}
		
		// Sami: Must be removed, after armor resistances are checked.
		// These values are a quick fix to balance dagger gameplay and give
		// armor resistances vs dagger. daggerWpnRes could also be used if a skill
		// was given to all classes. The values here try to be a compromise.
		// They were originally added in a late C4 rev (2289).
		if (target instanceof L2PcInstance)
		{
			final L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if (((L2PcInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if (((L2PcInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		{
			
			if (((L2PcInstance) attacker).isInOlympiadMode() && ((L2PcInstance) target).isInOlympiadMode())
			{
				
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				}
				
			}
			else
			{
				
				damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				
			}
		}
		
		return damage < 1 ? 1. : damage;
	}
	
	/**
	 * Calculated damage caused by ATTACK of attacker on target, called separatly for each weapon, if dual-weapon is used.
	 * @param  attacker player or NPC that makes ATTACK
	 * @param  target   player or NPC, target of ATTACK
	 * @param  skill
	 * @param  shld
	 * @param  crit     if the ATTACK have critical success
	 * @param  dual     if dual weapon is used
	 * @param  ss       if weapon item was charged by soulshot
	 * @return          damage points
	 */
	public final static double calcPhysDam(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean shld, final boolean crit, final boolean dual, final boolean ss)
	{
		if (attacker instanceof L2PcInstance)
		{
			final L2PcInstance pcInst = (L2PcInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if (ss)
		{
			damage *= 2;
		}
		
		if (skill != null)
		{
			double skillpower = skill.getPower(attacker);
			final float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
		}
		
		// In C5 summons make 10 % less dmg in PvP.
		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
		{
			damage *= 0.9;
		}
		
		// After C4 nobles make 4% more dmg in PvP.
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isNoble() && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			damage *= 1.04;
		}
		
		// defence modifier depending of the attacker weapon
		final L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
			}
		}
		
		if (crit)
		{
			// Finally retail like formula
			final double cAtkMultiplied = damage + attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
			final double cAtkVuln = target.calcStat(Stats.CRIT_VULN, 1, target, null);
			final double improvedDamageByCriticalMulAndVuln = cAtkMultiplied * cAtkVuln;
			final double improvedDamageByCriticalMulAndAdd = improvedDamageByCriticalMulAndVuln + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill);
			
			if (Config.DEBUG)
			{
				LOGGER.info("Attacker '" + attacker.getName() + "' Critical Damage Debug:");
				LOGGER.info("	-	Initial Damage:  " + damage);
				LOGGER.info("	-	Damage increased of mult:  " + cAtkMultiplied);
				LOGGER.info("	-	cAtkVuln Mult:  " + cAtkVuln);
				LOGGER.info("	-	improvedDamageByCriticalMulAndVuln: " + improvedDamageByCriticalMulAndVuln);
				LOGGER.info("	-	improvedDamageByCriticalMulAndAdd: " + improvedDamageByCriticalMulAndAdd);
			}
			
			damage = improvedDamageByCriticalMulAndAdd;
			
		}
		
		if (shld && !Config.ALT_GAME_SHIELD_BLOCKS)
		{
			defence += target.getShldDef();
		}
		
		damage = 70 * damage / defence;
		
		if (stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
			if (target instanceof L2NpcInstance)
			{
				// get the natural vulnerability for the template
				damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(stat);
			}
		}
		
		damage += Rnd.nextDouble() * damage / 10;
		// damage += rnd.nextDouble()* attacker.getRandomDamage(target);
		// }
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if (target instanceof L2PcInstance && weapon != null && weapon.getItemType() == L2WeaponType.DAGGER && skill != null)
		{
			final L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if (((L2PcInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if (((L2PcInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		
		if (attacker instanceof L2NpcInstance)
		{
			// Skill Race : Undead
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.UNDEAD)
			{
				damage /= attacker.getPDefUndead(target);
			}
			
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.PLANT)
			{
				damage /= attacker.getPDefPlants(target);
			}
			
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.BUG)
			{
				damage /= attacker.getPDefInsects(target);
			}
			
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.ANIMAL)
			{
				damage /= attacker.getPDefAnimals(target);
			}
			
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.BEAST)
			{
				damage /= attacker.getPDefMonsters(target);
			}
			
			if (((L2NpcInstance) attacker).getTemplate().getRace() == L2NpcTemplate.Race.DRAGON)
			{
				damage /= attacker.getPDefDragons(target);
			}
		}
		
		if (target instanceof L2NpcInstance)
		{
			switch (((L2NpcInstance) target).getTemplate().getRace())
			{
				case UNDEAD:
					damage *= attacker.getPAtkUndead(target);
					break;
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case ANGEL:
					damage *= attacker.getPAtkAngels(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				default:
					// nothing
					break;
			}
		}
		
		if (shld)
		{
			if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
			{
				damage = 1;
				target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
			}
		}
		
		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonusses in PvP fight
		if ((attacker instanceof L2PcInstance || attacker instanceof L2Summon) && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (attacker instanceof L2PcInstance)
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker instanceof L2Summon)
		{
			damage = damage * Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
		}
		else if (attacker instanceof L2NpcInstance)
		{
			damage = damage * Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		{
			
			if (((L2PcInstance) attacker).isInOlympiadMode() && ((L2PcInstance) target).isInOlympiadMode())
			{
				
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				}
				
			}
			else
			{
				
				damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				
			}
		}
		
		return damage;
	}
	
	public final static double calcMagicDam(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean ss, final boolean bss, final boolean mcrit)
	{
		// Add Matk/Mdef Bonus
		int ssModifier = 1;
		// Add Bonus for Sps/SS
		if (attacker instanceof L2Summon && !(attacker instanceof L2PetInstance))
		{
			
			if (bss)
			{
				// ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				ssModifier = 4;
			}
			else if (ss)
			{
				// ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				ssModifier = 2;
			}
			
		}
		else
		{
			final L2ItemInstance weapon = attacker.getActiveWeaponInstance();
			if (weapon != null)
			{
				if (bss)
				{
					// weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ssModifier = 4;
				}
				else if (ss)
				{
					// weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ssModifier = 2;
				}
			}
		}
		
		if (attacker instanceof L2PcInstance)
		{
			final L2PcInstance pcInst = (L2PcInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		double mAtk = attacker.getMAtk(target, skill);
		final double mDef = target.getMDef(attacker, skill);
		
		// apply ss bonus
		mAtk *= ssModifier;
		
		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);
		
		// In C5 summons make 10 % less dmg in PvP.
		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
		{
			damage *= 0.9;
		}
		
		// After C4 nobles make 4% more dmg in PvP.
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isNoble() && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			damage *= 1.04;
		}
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker instanceof L2PcInstance)
			{
				if (calcMagicSuccess(attacker, target, skill) && target.getLevel() - attacker.getLevel() <= 9)
				{
					if (skill.getSkillType() == SkillType.DRAIN)
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					}
					else
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					}
					
					damage /= 2;
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getId());
					attacker.sendPacket(sm);
					
					damage = 1;
				}
			}
			
			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == SkillType.DRAIN)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit)
		{
			// damage *= 4;
			damage *= Config.MAGIC_CRITICAL_POWER;
		}
		
		// Pvp bonusses for dmg
		if ((attacker instanceof L2PcInstance || attacker instanceof L2Summon) && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			if (skill.isMagic())
			{
				damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (attacker instanceof L2PcInstance)
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker instanceof L2Summon)
		{
			damage = damage * Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
		}
		else if (attacker instanceof L2NpcInstance)
		{
			damage = damage * Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		}
		
		if (target instanceof L2PlayableInstance)
		{
			damage *= skill.getPvpMulti();
		}
		
		if (skill.getSkillType() == SkillType.DEATHLINK)
		{
			damage = damage * (1.0 - attacker.getStatus().getCurrentHp() / attacker.getMaxHp()) * 2.0;
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		{
			
			if (((L2PcInstance) attacker).isInOlympiadMode() && ((L2PcInstance) target).isInOlympiadMode())
			{
				
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				}
				
			}
			else
			{
				
				damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				
			}
		}
		
		return damage;
	}
	
	public static final double calcMagicDam(final L2CubicInstance attacker, final L2Character target, final L2Skill skill, final boolean mcrit)
	{
		final double damage = calcMagicDam(attacker.getOwner(), target, skill, false, false, mcrit);
		return damage;
	}
	
	/**
	 * @param  rate
	 * @return      true in case of critical hit
	 */
	public final static boolean calcCrit(final double rate)
	{
		return rate > Rnd.get(1000);
	}
	
	/**
	 * Calcul value of blow success
	 * @param  activeChar
	 * @param  target
	 * @param  chance
	 * @return
	 */
	public final boolean calcBlow(final L2Character activeChar, final L2Character target, final int chance)
	{
		return activeChar.calcStat(Stats.BLOW_RATE, chance * (1.0 + (activeChar.getDEX() - 20) / 100), target, null) > Rnd.get(100);
	}
	
	/**
	 * Calcul value of lethal chance
	 * @param  activeChar
	 * @param  target
	 * @param  baseLethal
	 * @return
	 */
	public final static double calcLethal(final L2Character activeChar, final L2Character target, final int baseLethal)
	{
		double mult = 0.1 * target.calcStat(Stats.LETHAL_RATE, 100, target, null);
		mult *= baseLethal;
		return mult;
	}
	
	public static final boolean calcLethalHit(final L2Character activeChar, final L2Character target, final L2Skill skill)
	{
		final int chance = Rnd.get(1000);
		
		if ((target.isRaid() && Config.ALLOW_RAID_LETHAL) || (!target.isRaid() && !(target instanceof L2DoorInstance) && !(Config.ALLOW_LETHAL_PROTECTION_MOBS && target instanceof L2NpcInstance && (Config.LIST_LETHAL_PROTECTED_MOBS.contains(((L2NpcInstance) target).getNpcId())))))
		{
			if ((!target.isRaid() || Config.ALLOW_RAID_LETHAL) && !(target instanceof L2DoorInstance) && !(target instanceof L2NpcInstance && ((L2NpcInstance) target).getNpcId() == 35062) && !(Config.ALLOW_LETHAL_PROTECTION_MOBS && target instanceof L2NpcInstance && (Config.LIST_LETHAL_PROTECTED_MOBS.contains(((L2NpcInstance) target).getNpcId()))))
			{
				// 1nd lethal set CP to 1
				// 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
				if (skill.getLethalChance2() > 0 && chance < calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					if (target instanceof L2NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
					}
					else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
					{
						final L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
						{
							if (!(activeChar instanceof L2PcInstance && (((L2PcInstance) activeChar).isGM() && !((L2PcInstance) activeChar).getAccessLevel().canGiveDamage())))
							{
								player.setCurrentHp(1);
								player.setCurrentCp(1);
								player.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
							}
						}
					}
				}
				else if (skill.getLethalChance1() > 0 && chance < calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if (target instanceof L2PcInstance)
					{
						final L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
						{
							if (!(activeChar instanceof L2PcInstance && (((L2PcInstance) activeChar).isGM() && !((L2PcInstance) activeChar).getAccessLevel().canGiveDamage())))
							{
								player.setCurrentCp(1); // Set CP to 1
								player.sendPacket(SystemMessage.sendString("Combat points disappear when hit with a half kill skill"));
								activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
							}
						}
					}
					
					// TODO: remove half kill since SYSMsg got changed.
					/*
					 * else if (target instanceof L2Npc) // If is a monster remove first damage and after 50% of current hp target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar, skill);
					 */
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
	
	public final static boolean calcMCrit(final double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	
	/**
	 * @param  target
	 * @param  dmg
	 * @return        true in case when ATTACK is canceled due to hit
	 */
	public final static boolean calcAtkBreak(final L2Character target, final double dmg)
	{
		if (target instanceof L2PcInstance)
		{
			if (((L2PcInstance) target).getForceBuff() != null)
			{
				return true;
			}
			
			// if (target.isCastingNow()&& target.getLastSkillCast() != null)
			// if (target.getLastSkillCast().isCancelIfHit())
			// return true;
		}
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			final L2Weapon wpn = target.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW)
			{
				init = 15;
			}
		}
		
		if (target.isRaid() || target.isInvul() || init <= 0)
		{
			return false; // No attack break
		}
		
		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);
		
		// Chance is affected by target MEN
		init -= (BaseStats.MEN.calcBonus(target) * 100 - 100);
		
		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		
		// Adjust the rate to be between 1 and 99
		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}
		
		return Rnd.get(100) < rate;
	}
	
	/**
	 * Calculate delay (in milliseconds) before next ATTACK
	 * @param  attacker
	 * @param  target
	 * @param  rate
	 * @return
	 */
	public final int calcPAtkSpd(final L2Character attacker, final L2Character target, final double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if (rate < 2)
		{
			return 2700;
		}
		return (int) (470000 / rate);
	}
	
	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * @param  attacker
	 * @param  target
	 * @param  skill
	 * @param  skillTime
	 * @return
	 */
	public final int calcMAtkSpd(final L2Character attacker, final L2Character target, final L2Skill skill, final double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) (skillTime * 333 / attacker.getMAtkSpd());
		}
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}
	
	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * @param  attacker
	 * @param  skill
	 * @param  skillTime
	 * @return
	 */
	public final int calcMAtkSpd(final L2Character attacker, final L2Skill skill, final double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) (skillTime * 333 / attacker.getMAtkSpd());
		}
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}
	
	/**
	 * @param  attacker
	 * @param  target
	 * @return          true if hit missed (taget evaded)
	 */
	public static boolean calcHitMiss(final L2Character attacker, final L2Character target)
	{
		/*
		 * // OLD FORMULA // accuracy+dexterity => probability to hit in percents int acc_attacker; int evas_target; acc_attacker = attacker.getAccuracy(); evas_target = target.getEvasionRate(attacker); int d = 85 + acc_attacker - evas_target; return d < Rnd.get(100);
		 */
		
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
		// Get additional bonus from the conditions when you are attacking
		chance *= HitConditionBonus.getConditionBonus(attacker, target);
		
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		
		return chance < Rnd.get(1000);
	}
	
	/**
	 * @param  attacker
	 * @param  target
	 * @return          true if shield defence successfull
	 */
	public static boolean calcShldUse(final L2Character attacker, final L2Character target)
	{
		final L2Weapon at_weapon = attacker.getActiveWeaponItem();
		// double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * DEXbonus[target.getDEX()];
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);
		if (shldRate == 0.0)
		{
			return false;
		}
		// Check for passive skill Aegis (316) or Aegis Stance (318)
		// Like L2OFF you can't parry if your target is behind you
		if (target.getKnownSkill(316) == null && target.getFirstEffect(318) == null)
		{
			if (target.isBehind(attacker) || !target.isFront(attacker) || !attacker.isFront(target))
			{
				return false;
			}
		}
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		if (at_weapon != null && at_weapon.getItemType() == L2WeaponType.BOW)
		{
			shldRate *= 1.3;
		}
		return shldRate > Rnd.get(100);
	}
	
	public boolean calcMagicAffected(final L2Character actor, final L2Character target, final L2Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		final SkillType type = skill.getSkillType();
		double defence = 0;
		if (skill.isActive() && skill.isOffensive())
		{
			defence = target.getMDef(actor, skill);
		}
		
		final double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		double d = (attack - defence) / (attack + defence);
		if (target.isRaid() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.AGGDEBUFF))
		{
			if (d > 0 && Rnd.get(1000) == 1)
			{
				return true;
			}
			return false;
		}
		
		if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0 && skill.is_Debuff())
		{
			return false;
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	public static double calcSkillVulnerability(final L2Character target, final L2Skill skill)
	{
		double multiplier = 1; // initialize...
		
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// first, get the natural template vulnerability values for the target
			final Stats stat = skill.getStat();
			if (stat != null)
			{
				switch (stat)
				{
					case AGGRESSION:
						multiplier = target.getTemplate().baseAggressionVuln;
						break;
					case BLEED:
						multiplier = target.getTemplate().baseBleedVuln;
						break;
					case POISON:
						multiplier = target.getTemplate().basePoisonVuln;
						break;
					case STUN:
						multiplier = target.getTemplate().baseStunVuln;
						break;
					case ROOT:
						multiplier = target.getTemplate().baseRootVuln;
						break;
					case MOVEMENT:
						multiplier = target.getTemplate().baseMovementVuln;
						break;
					case CONFUSION:
						multiplier = target.getTemplate().baseConfusionVuln;
						break;
					case SLEEP:
						multiplier = target.getTemplate().baseSleepVuln;
						break;
					case FIRE:
						multiplier = target.getTemplate().baseFireVuln;
						break;
					case WIND:
						multiplier = target.getTemplate().baseWindVuln;
						break;
					case WATER:
						multiplier = target.getTemplate().baseWaterVuln;
						break;
					case EARTH:
						multiplier = target.getTemplate().baseEarthVuln;
						break;
					case HOLY:
						multiplier = target.getTemplate().baseHolyVuln;
						break;
					case DARK:
						multiplier = target.getTemplate().baseDarkVuln;
						break;
					default:
						multiplier = 1;
				}
			}
			
			// Next, calculate the elemental vulnerabilities
			switch (skill.getElement())
			{
				case L2Skill.ELEMENT_EARTH:
					multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_FIRE:
					multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WATER:
					multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WIND:
					multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_HOLY:
					multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_DARK:
					multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
					break;
			}
			
			// Finally, calculate skilltype vulnerabilities
			SkillType type = skill.getSkillType();
			
			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if (type != null && (type == SkillType.PDAM || type == SkillType.MDAM))
			{
				type = skill.getEffectType();
			}
			
			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE_CHAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case CONFUSION:
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					case DEBUFF:
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					case BUFF:
						multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
						break;
				}
			}
			
		}
		return multiplier;
	}
	
	/*
	 * public double calcSkillStatModifier(SkillType type, L2Character target) { double multiplier = 1; if(type == null) return multiplier; switch(type) { case STUN: case BLEED: multiplier = 2 - Math.sqrt(CONbonus[target.getCON()]); break; case POISON: case SLEEP: case DEBUFF: case WEAKNESS: case ERASE:
	 * case ROOT: case MUTE: case FEAR: case BETRAY: case CONFUSION: case AGGREDUCE_CHAR: case PARALYZE: multiplier = 2 - Math.sqrt(MENbonus[target.getMEN()]); break; default: return multiplier; } if(multiplier < 0) { multiplier = 0; } return multiplier; }
	 */
	
	public static double calcSkillStatModifier(final L2Skill skill, final L2Character target)
	{
		final BaseStats saveVs = skill.getSavevs();
		if (saveVs == null)
		{
			return 1;
		}
		
		return 1 / saveVs.calcBonus(target);
	}
	
	public static boolean calcCubicSkillSuccess(final L2CubicInstance attacker, final L2Character target, final L2Skill skill)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0 && skill.is_Debuff())
		{
			return false;
		}
		
		final SkillType type = skill.getSkillType();
		
		// these skills should not work on RaidBoss
		if (target.isRaid())
		{
			switch (type)
			{
				case CONFUSION:
				case ROOT:
				case STUN:
				case MUTE:
				case FEAR:
				case DEBUFF:
				case PARALYZE:
				case SLEEP:
				case AGGDEBUFF:
					return false;
			}
		}
		
		final int value = (int) skill.getPower();
		final double statModifier = calcSkillStatModifier(skill, target);
		int rate = (int) (value * statModifier);
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 0;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(attacker.getOwner(), skill);
			
			mAtkModifier = Math.pow(attacker.getMAtk() / mAtkModifier, 0.2);
			
			rate += (int) (mAtkModifier * 100) - 100;
		}
		
		// Resists
		final double vulnModifier = calcSkillVulnerability(target, skill);
		final double res = vulnModifier;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - 0.075 * res;
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				if ((resMod = res * x_factor) > 1)
				{
					resMod = res;
				}
				
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker.getOwner(), target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		if (Config.SKILLSDEBUG)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, skill.getName(), " calcCubicSkillSuccess: ", " type:", skill.getSkillType().toString(), " power:", String.valueOf(value), " stat:", String.format("%1.2f", statModifier), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", vulnModifier), ")", " mAtk:", String.format("%1.2f", mAtkModifier), " lvl:", String.valueOf(deltamod), " total:", String.valueOf(rate));
			final String result = stat.toString();
			LOGGER.info(result);
		}
		return (Rnd.get(100) < rate);
	}
	
	public boolean calcSkillSuccess(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean ss, final boolean sps, final boolean bss)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0 && skill.is_Debuff())
		{
			return false;
		}
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 1;
		int ssModifier = 1;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(target, skill);
			
			if (bss)
			{
				ssModifier = 4;
			}
			else if (sps)
			{
				ssModifier = 2;
			}
			/*
			 * // Add Bonus for Sps/SS if(attacker instanceof L2Summon && !(attacker instanceof L2PetInstance)){ if (bss){ ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); ssModifier = 4; }else if(sps){ ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE); ssModifier =
			 * 2; } }else{ L2ItemInstance weapon = attacker.getActiveWeaponInstance(); if(weapon!=null){ if (bss){ weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); ssModifier = 4; }else if (sps){ weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE); ssModifier = 2; } } }
			 */
			
			mAtkModifier = 14 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill)) / mAtkModifier;
			
		} /*
			 * else{ L2ItemInstance weapon = attacker.getActiveWeaponInstance(); if(weapon!=null){ if(ss){ weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE); } } //no soulshots influence over not magic attacks }
			 */
		
		final SkillType type = skill.getSkillType();
		
		if (target.isRaid() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.AGGDEBUFF))
		{
			return false; // these skills should not work on RaidBoss
		}
		
		if (target.isInvul() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP || type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.CANCEL || type == SkillType.NEGATE || type == SkillType.WARRIOR_BANE || type == SkillType.MAGE_BANE))
		{
			return false; // these skills should not work on Invulable persons
		}
		
		final int value = (int) skill.getPower();
		final double statModifier = calcSkillStatModifier(skill, target);
		
		// Calculate BaseRate.
		int rate = (int) (value * statModifier);
		
		// matk modifier
		rate = (int) (rate * mAtkModifier);
		
		// Resists
		final double vulnModifier = calcSkillVulnerability(target, skill);
		
		// double profModifier = calcSkillProficiency(skill, attacker, target);
		final double res = vulnModifier/* + profModifier */;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - 0.075 * res;
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				if ((resMod = res * x_factor) > 1)
				{
					resMod = res;
				}
				
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker, target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		// physics configuration addons
		final float physics_mult = getChanceMultiplier(skill);
		rate *= physics_mult;
		
		if (Config.SKILLSDEBUG)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, " calcSkillSuccess: ", skill.getName(), " ID:", ""
				+ skill.getId(), " type:", skill.getSkillType().toString(), " power:", String.valueOf(value), " stat:", String.format("%1.2f", statModifier), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", vulnModifier), " mAtk:", String.format("%1.2f", mAtkModifier), " ss:", String.valueOf(ssModifier), " lvl:", String.valueOf(deltamod), " physics configuration multiplier:", String.valueOf(physics_mult), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (Config.DEVELOPER)
			{
				LOGGER.info(result);
			}
			
		}
		
		if (attacker instanceof L2PcInstance && Config.SEND_SKILLS_CHANCE_TO_PLAYERS)
		{
			((L2PcInstance) attacker).sendMessage("Skill: " + skill.getName() + " Chance: " + rate + "%");
		}
		
		return Rnd.get(100) < rate;
	}
	
	public static boolean calcEffectSuccess(final L2Character attacker, final L2Character target, final EffectTemplate effect, final L2Skill skill, final boolean ss, final boolean sps, final boolean bss)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0 && skill.is_Debuff())
		{
			return false;
		}
		
		final SkillType type = effect.effectType;
		final int value = (int) effect.effectPower;
		if (type == null)
		{
			return Rnd.get(100) < value;
		}
		else if (type.equals(SkillType.CANCEL))
		{
			return true;
		}
		
		final double statModifier = calcSkillStatModifier(skill, target);
		
		// Calculate BaseRate.
		int rate = (int) (value * statModifier);
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 0;
		int ssModifier = 0;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(target, skill);
			// if (shld == SHIELD_DEFENSE_SUCCEED)
			// mAtkModifier += target.getShldDef();
			
			// Add Bonus for Sps/SS
			if (bss)
			{
				ssModifier = 4;
			}
			else if (sps)
			{
				ssModifier = 2;
			}
			else
			{
				ssModifier = 1;
			}
			
			mAtkModifier = 14 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill)) / mAtkModifier;
			
			rate = (int) (rate * mAtkModifier);
		}
		
		// Resists
		final double vulnModifier = calcSkillTypeVulnerability(1, target, type);
		// double profModifier = calcSkillTypeProficiency(0, attacker, target, type);
		
		final double res = vulnModifier;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - 0.075 * res;
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				if ((resMod = res * x_factor) > 1)
				{
					resMod = res;
				}
				
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		/*
		 * double res = vulnModifier; double resMod = 1; if (res != 0) { if (res < 0) { resMod = 1 - 0.075 * res; resMod = 1 / resMod; } else resMod = 1 + 0.02 * res; rate *= resMod; }
		 */
		
		// int elementModifier = calcElementModifier(attacker, target, skill);
		// rate += elementModifier;
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker, target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		// physics configuration addons
		final float physics_mult = getChanceMultiplier(skill);
		rate *= physics_mult;
		
		if (Config.SKILLSDEBUG)
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, " calcEffectSuccess: ", skill.getName(), " type:", skill.getSkillType().toString(), " power:", String.valueOf(value), " stat:", String.format("%1.2f", statModifier), " res:", String.format("%1.2f", resMod), "(", String.format("%1.2f", vulnModifier), " mAtk:", String.format("%1.2f", mAtkModifier), " ss:", String.valueOf(ssModifier), " lvl:", String.valueOf(deltamod), " physics configuration multiplier:", String.valueOf(physics_mult), " total:", String.valueOf(rate));
			final String result = stat.toString();
			if (Config.DEVELOPER)
			{
				LOGGER.info(result);
			}
			
		}
		
		if (attacker instanceof L2PcInstance && Config.SEND_SKILLS_CHANCE_TO_PLAYERS)
		{
			((L2PcInstance) attacker).sendMessage("EffectType " + effect.effectType + " Chance: " + rate + "%");
		}
		
		return (Rnd.get(100) < rate);
	}
	
	public static double calcSkillTypeVulnerability(double multiplier, final L2Character target, final SkillType type)
	{
		if (type != null)
		{
			switch (type)
			{
				case BLEED:
					multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
					break;
				case POISON:
					multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
					break;
				case STUN:
					multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
					break;
				case ROOT:
					multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
					break;
				case MUTE:
				case FEAR:
				case BETRAY:
				case AGGDEBUFF:
				case ERASE:
					multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
					break;
				case CONFUSION:
				case CONFUSE_MOB_ONLY:
					multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
					break;
				case DEBUFF:
					multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
					break;
				case BUFF:
					multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
					break;
				case CANCEL:
					multiplier = target.calcStat(Stats.CANCEL_VULN, multiplier, target, null);
					break;
				default:
			}
		}
		
		return multiplier;
	}
	
	public static int calcLvlDependModifier(final L2Character attacker, final L2Character target, final L2Skill skill)
	{
		if (attacker == null)
		{
			return 0;
		}
		
		if (skill.getLevelDepend() == 0)
		{
			return 0;
		}
		
		final int attackerMod;
		if (skill.getMagicLevel() > 0)
		{
			attackerMod = skill.getMagicLevel();
		}
		else
		{
			attackerMod = attacker.getLevel();
		}
		
		final int delta = attackerMod - target.getLevel();
		int deltamod = delta / 5;
		deltamod = deltamod * 5;
		if (deltamod != delta)
		{
			if (delta < 0)
			{
				deltamod -= 5;
			}
			else
			{
				deltamod += 5;
			}
		}
		
		return deltamod;
	}
	
	public static float getChanceMultiplier(final L2Skill skill)
	{
		
		float multiplier = 1;
		
		if (skill != null && skill.getSkillType() != null)
		{
			switch (skill.getSkillType())
			{
				case BLEED:
					multiplier = Config.BLEED_CHANCE_MODIFIER;
					break;
				case POISON:
					multiplier = Config.POISON_CHANCE_MODIFIER;
					break;
				case STUN:
					multiplier = Config.STUN_CHANCE_MODIFIER;
					break;
				case PARALYZE:
					multiplier = Config.PARALYZE_CHANCE_MODIFIER;
					break;
				case ROOT:
					multiplier = Config.ROOT_CHANCE_MODIFIER;
					break;
				case SLEEP:
					multiplier = Config.SLEEP_CHANCE_MODIFIER;
					break;
				case MUTE:
				case FEAR:
				case BETRAY:
				case AGGREDUCE_CHAR:
					multiplier = Config.FEAR_CHANCE_MODIFIER;
					break;
				case CONFUSION:
					multiplier = Config.CONFUSION_CHANCE_MODIFIER;
					break;
				case DEBUFF:
				case WEAKNESS:
				case WARRIOR_BANE:
				case MAGE_BANE:
					multiplier = Config.DEBUFF_CHANCE_MODIFIER;
					break;
				case BUFF:
					multiplier = Config.BUFF_CHANCE_MODIFIER;
					break;
			}
		}
		
		return multiplier;
		
	}
	
	public boolean calcBuffSuccess(final L2Character target, final L2Skill skill)
	{
		final int rate = 100 * (int) calcSkillVulnerability(target, skill);
		return Rnd.get(100) < rate;
	}
	
	public static boolean calcMagicSuccess(final L2Character attacker, final L2Character target, final L2Skill skill)
	{
		final double lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
		final int rate = Math.round((float) (Math.pow(1.3, lvlDifference) * 100));
		
		return Rnd.get(10000) > rate;
	}
	
	public boolean calculateUnlockChance(final L2Skill skill)
	{
		final int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
				chance = 30;
				break;
			
			case 2:
				chance = 50;
				break;
			
			case 3:
				chance = 75;
				break;
			
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		if (Rnd.get(120) > chance)
		{
			return false;
		}
		return true;
	}
	
	public double calcManaDam(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean ss, final boolean bss)
	{
		if (attacker == null || target == null)
		{
			return 0;
		}
		
		// Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		final double mDef = target.getMDef(attacker, skill);
		final double mp = target.getMaxMp();
		
		int ssModifier = 1;
		// Add Bonus for Sps/SS
		if (attacker instanceof L2Summon && !(attacker instanceof L2PetInstance))
		{
			
			if (bss)
			{
				// ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				ssModifier = 4;
			}
			else if (ss)
			{
				// ((L2Summon)attacker).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				ssModifier = 2;
			}
			
		}
		else
		{
			final L2ItemInstance weapon = attacker.getActiveWeaponInstance();
			if (weapon != null)
			{
				if (bss)
				{
					// weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ssModifier = 4;
				}
				else if (ss)
				{
					// weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ssModifier = 2;
				}
			}
		}
		
		mAtk *= ssModifier;
		
		double damage = Math.sqrt(mAtk) * skill.getPower(attacker) * mp / 97 / mDef;
		damage *= calcSkillVulnerability(target, skill);
		return damage;
	}
	
	public double calculateSkillResurrectRestorePercent(final double baseRestorePercent, final L2Character caster)
	{
		double restorePercent = baseRestorePercent;
		
		// double modifier = WITbonus[casterWIT];
		final double modifier = BaseStats.WIT.calcBonus(caster);
		
		if (restorePercent != 100 && restorePercent != 0)
		{
			
			restorePercent = baseRestorePercent * modifier;
			
			if (restorePercent - baseRestorePercent > 20.0)
			{
				restorePercent = baseRestorePercent + 20.0;
			}
		}
		
		if (restorePercent > 100)
		{
			restorePercent = 100;
		}
		if (restorePercent < baseRestorePercent)
		{
			restorePercent = baseRestorePercent;
		}
		
		return restorePercent;
	}
	
	/*
	 * public static double getSTRBonus(L2Character activeChar) { return STRbonus[activeChar.getSTR()]; }
	 */
	
	public static boolean calcPhysicalSkillEvasion(final L2Character target, final L2Skill skill)
	{
		if (skill.isMagic() || skill.getCastRange() > 40)
		{
			return false;
		}
		
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}
	
	public boolean calcSkillMastery(final L2Character actor)
	{
		if (actor == null)
		{
			return false;
		}
		
		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);
		
		if (actor instanceof L2PcInstance)
		{
			if (((L2PcInstance) actor).isMageClass())
			{
				
				// val *= INTbonus[actor.getINT()];
				val *= BaseStats.INT.calcBonus(actor);
			}
			else
			{
				// val *= STRbonus[actor.getSTR()];
				val *= BaseStats.STR.calcBonus(actor);
			}
		}
		
		return Rnd.get(100) < val;
	}
	
	/**
	 * Calculate damage caused by falling
	 * @param  cha
	 * @param  fallHeight
	 * @return            damage
	 */
	public static double calcFallDam(final L2Character cha, final int fallHeight)
	{
		if (!Config.FALL_DAMAGE || fallHeight < 0)
		{
			return 0;
		}
		final double damage = cha.calcStat(Stats.FALL, fallHeight * cha.getMaxHp() / 1000, null, null);
		return damage;
	}
	
	/**
	 * Calculated damage caused by charges skills types. - THX aCis The special thing is about the multiplier (56 and not 70), and about the fixed amount of damages
	 * @param  attacker   player or NPC that makes ATTACK
	 * @param  target     player or NPC, target of ATTACK
	 * @param  skill
	 * @param  shld
	 * @param  crit       if the ATTACK have critical success
	 * @param  ss         if weapon item was charged by soulshot
	 * @param  numCharges
	 * @return            damage points
	 */
	public static final double calcChargeSkillsDam(final L2Character attacker, final L2Character target, final L2Skill skill, final boolean shld, final boolean crit, final boolean ss, final int numCharges)
	{
		if (attacker instanceof L2PcInstance)
		{
			final L2PcInstance pcInst = (L2PcInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		final boolean isPvP = (attacker instanceof L2PlayableInstance) && (target instanceof L2PlayableInstance);
		double damage = attacker.getPAtk(target);
		final double defence = target.getPDef(attacker);
		
		if (ss)
		{
			damage *= 2;
		}
		
		if (crit)
		{
			// double cAtkMultiplied = (damage) + attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
			final double improvedDamageByCriticalVuln = target.calcStat(Stats.CRIT_VULN, damage, target, skill);
			final double improvedDamageByCriticalVulnAndAdd = (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, improvedDamageByCriticalVuln, target, skill));
			
			if (Config.DEBUG)
			{
				LOGGER.info("Attacker '" + attacker.getName() + "' Charge Skills Critical Damage Debug:");
				LOGGER.info("	-	Initial Damage:  " + damage);
				LOGGER.info("	-	improvedDamageByCriticalVuln: " + improvedDamageByCriticalVuln);
				LOGGER.info("	-	improvedDamageByCriticalVulnAndAdd: " + improvedDamageByCriticalVulnAndAdd);
			}
			
			damage = improvedDamageByCriticalVulnAndAdd;
			
			/*
			 * //Finally retail like formula double cAtkMultiplied = damage+attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill); double cAtkVuln = target.calcStat(Stats.CRIT_VULN, 1, target, null); double improvedDamageByCriticalMulAndVuln = cAtkMultiplied * cAtkVuln; double
			 * improvedDamageByCriticalMulAndAdd = improvedDamageByCriticalMulAndVuln + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill); if(Config.DEBUG){ LOGGER.info("Attacker '"+attacker.getName()+"' Critical Skill Damage Debug:"); LOGGER.info("	-	Initial Damage:  "+damage);
			 * LOGGER.info("	-	Damage increased of mult:  "+cAtkMultiplied); LOGGER.info("	-	cAtkVuln Mult:  "+cAtkVuln); LOGGER.info("	-	improvedDamageByCriticalMulAndVuln: "+improvedDamageByCriticalMulAndVuln);
			 * LOGGER.info("	-	improvedDamageByCriticalMulAndAdd: "+improvedDamageByCriticalMulAndAdd); } damage = improvedDamageByCriticalMulAndAdd;
			 */
			
			/*
			 * //Finally retail like formula damage = 2 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill) * target.calcStat(Stats.CRIT_VULN, 1, target, null) * (56 * damage / defence); //Crit dmg add is almost useless in normal hits... damage += (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0,
			 * target, skill) * 56 / defence);
			 */
		}
		
		if (skill != null) // skill add is not influenced by criticals improvements,
							// so it's applied later
		{
			double skillpower = skill.getPower(attacker);
			final float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
			
			// Charges multiplier, just when skill is used
			if (numCharges >= 1)
			{
				final double chargesModifier = 0.7 + (0.3 * numCharges);
				damage *= chargesModifier;
			}
			
		}
		
		damage = 56 * damage / defence;
		
		// finally, apply the critical multiplier if present (it's not subjected to defense)
		if (crit)
		{
			damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
		}
		
		// defence modifier depending of the attacker weapon
		final L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				case BIGBLUNT:
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
			}
		}
		
		if (stat != null)
		{
			damage = target.calcStat(stat, damage, target, null);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		
		// After C4 nobles make 4% more dmg in PvP.
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isNoble() && (target instanceof L2PcInstance || target instanceof L2Summon))
		{
			damage *= 1.04;
		}
		
		// LOGGER.info(" - Final damage: "+damage);
		
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if (target instanceof L2NpcInstance)
		{
			double multiplier;
			switch (((L2NpcInstance) target).getTemplate().getRace())
			{
				case BEAST:
					multiplier = 1 + ((attacker.getPAtkMonsters(target) - target.getPDefMonsters(target)) / 100);
					damage *= multiplier;
					break;
				case ANIMAL:
					multiplier = 1 + ((attacker.getPAtkAnimals(target) - target.getPDefAnimals(target)) / 100);
					damage *= multiplier;
					break;
				case PLANT:
					multiplier = 1 + ((attacker.getPAtkPlants(target) - target.getPDefPlants(target)) / 100);
					damage *= multiplier;
					break;
				case DRAGON:
					multiplier = 1 + ((attacker.getPAtkDragons(target) - target.getPDefDragons(target)) / 100);
					damage *= multiplier;
					break;
				case ANGEL:
					multiplier = 1 + ((attacker.getPAtkAngels(target) - target.getPDefAngels(target)) / 100);
					damage *= multiplier;
					break;
				case BUG:
					multiplier = 1 + ((attacker.getPAtkInsects(target) - target.getPDefInsects(target)) / 100);
					damage *= multiplier;
					break;
				case GIANT:
					multiplier = 1 + ((attacker.getPAtkGiants(target) - target.getPDefGiants(target)) / 100);
					damage *= multiplier;
					break;
				case MAGICCREATURE:
					multiplier = 1 + ((attacker.getPAtkMagicCreatures(target) - target.getPDefMagicCreatures(target)) / 100);
					damage *= multiplier;
					break;
				default:
					// nothing
					break;
			}
		}
		
		if (shld)
		{
			if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
			{
				damage = 1;
				target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
			}
		}
		
		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonusses in PvP fight
		if (isPvP)
		{
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && attacker instanceof L2PcInstance && target instanceof L2PcInstance)
		{
			
			if (((L2PcInstance) attacker).isInOlympiadMode() && ((L2PcInstance) target).isInOlympiadMode())
			{
				
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				}
				
			}
			else
			{
				
				damage = damage * ClassDamageManager.getDamageMultiplier((L2PcInstance) attacker, (L2PcInstance) target);
				
			}
		}
		
		return damage;
	}
	
}
