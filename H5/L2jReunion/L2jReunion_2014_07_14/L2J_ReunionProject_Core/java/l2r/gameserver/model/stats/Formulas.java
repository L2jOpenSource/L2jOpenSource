/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.SevenSignsFestival;
import l2r.gameserver.datatables.xml.HitConditionBonusData;
import l2r.gameserver.datatables.xml.KarmaData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.ClanHall;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.Siege;
import l2r.gameserver.model.items.L2Armor;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.type.ArmorType;
import l2r.gameserver.model.items.type.WeaponType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.L2TraitType;
import l2r.gameserver.model.skills.funcs.formulas.FuncArmorSet;
import l2r.gameserver.model.skills.funcs.formulas.FuncAtkAccuracy;
import l2r.gameserver.model.skills.funcs.formulas.FuncAtkCritical;
import l2r.gameserver.model.skills.funcs.formulas.FuncAtkEvasion;
import l2r.gameserver.model.skills.funcs.formulas.FuncGatesMDefMod;
import l2r.gameserver.model.skills.funcs.formulas.FuncGatesPDefMod;
import l2r.gameserver.model.skills.funcs.formulas.FuncHenna;
import l2r.gameserver.model.skills.funcs.formulas.FuncMAtkCritical;
import l2r.gameserver.model.skills.funcs.formulas.FuncMAtkMod;
import l2r.gameserver.model.skills.funcs.formulas.FuncMAtkSpeed;
import l2r.gameserver.model.skills.funcs.formulas.FuncMDefMod;
import l2r.gameserver.model.skills.funcs.formulas.FuncMaxCpMul;
import l2r.gameserver.model.skills.funcs.formulas.FuncMaxHpMul;
import l2r.gameserver.model.skills.funcs.formulas.FuncMaxMpMul;
import l2r.gameserver.model.skills.funcs.formulas.FuncMoveSpeed;
import l2r.gameserver.model.skills.funcs.formulas.FuncPAtkMod;
import l2r.gameserver.model.skills.funcs.formulas.FuncPAtkSpeed;
import l2r.gameserver.model.skills.funcs.formulas.FuncPDefMod;
import l2r.gameserver.model.zone.type.L2CastleZone;
import l2r.gameserver.model.zone.type.L2ClanHallZone;
import l2r.gameserver.model.zone.type.L2FortZone;
import l2r.gameserver.model.zone.type.L2MotherTreeZone;
import l2r.gameserver.network.Debug;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;
import gr.reunion.balanceEngine.BalanceHandler;
import gr.reunion.configsEngine.FormulasConfigs;

/**
 * Global calculations.
 */
public final class Formulas
{
	private static final Logger _log = Logger.getLogger(Formulas.class.getName());
	
	/** Regen Task period. */
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	
	public static final byte SHIELD_DEFENSE_FAILED = 0; // no shield defense
	public static final byte SHIELD_DEFENSE_SUCCEED = 1; // normal shield defense
	public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2; // perfect block
	
	public static final byte SKILL_REFLECT_FAILED = 0; // no reflect
	public static final byte SKILL_REFLECT_SUCCEED = 1; // normal reflect, some damage reflected some other not
	public static final byte SKILL_REFLECT_VENGEANCE = 2; // 100% of the damage affect both
	
	private static final byte MELEE_ATTACK_RANGE = 80;
	
	/**
	 * Return the period between 2 regeneration task (3s for L2Character, 5 min for L2DoorInstance).
	 * @param cha
	 * @return
	 */
	public static int getRegeneratePeriod(L2Character cha)
	{
		return cha.isDoor() ? HP_REGENERATE_PERIOD * 100 : HP_REGENERATE_PERIOD;
	}
	
	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <br>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<br>
	 * @return
	 */
	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		
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
		
		std[Stats.MOVE_SPEED.ordinal()] = new Calculator();
		std[Stats.MOVE_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());
		
		return std;
	}
	
	public static Calculator[] getStdDoorCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		// SevenSigns PDEF Modifier
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncGatesPDefMod.getInstance());
		
		// SevenSigns MDEF Modifier
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncGatesMDefMod.getInstance());
		
		return std;
	}
	
	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<br>
	 * <B><U>Concept</U>:</B><br>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <br>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha.isPlayer())
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
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
			
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_STR));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_DEX));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_INT));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_MEN));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_CON));
			cha.addStatFunc(FuncHenna.getInstance(Stats.STAT_WIT));
			
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_STR));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_DEX));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_INT));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_MEN));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_CON));
			cha.addStatFunc(FuncArmorSet.getInstance(Stats.STAT_WIT));
		}
		else if (cha.isSummon())
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
		}
	}
	
	/**
	 * Calculate the HP regen rate (base + modifiers).
	 * @param cha
	 * @return
	 */
	public static final double calcHpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseHpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && cha.isChampion())
		{
			hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				hpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			else
			{
				double siegeModifier = calcSiegeRegenModifier(player);
				if (siegeModifier > 0)
				{
					hpRegenMultiplier *= siegeModifier;
				}
			}
			
			if (player.isInsideZone(ZoneIdType.CLAN_HALL) && (player.getClan() != null) && (player.getClan().getHideoutId() > 0))
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getResidenceId();
				int clanHallIndex = player.getClan().getHideoutId();
				if ((clanHallIndex > 0) && (clanHallIndex == posChIndex))
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneIdType.CASTLE) && (player.getClan() != null) && (player.getClan().getCastleId() > 0))
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getResidenceId();
				int castleIndex = player.getClan().getCastleId();
				if ((castleIndex > 0) && (castleIndex == posCastleIndex))
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) castle.getFunction(Castle.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneIdType.FORT) && (player.getClan() != null) && (player.getClan().getFortId() > 0))
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getResidenceId();
				int fortIndex = player.getClan().getFortId();
				if ((fortIndex > 0) && (fortIndex == posFortIndex))
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + ((double) fort.getFunction(Fort.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneIdType.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				hpRegenBonus += hpBonus;
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
			init *= cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
		}
		else if (cha.isPet())
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenHP() * Config.PET_HP_REGEN_MULTIPLIER;
		}
		
		return (cha.calcStat(Stats.REGENERATE_HP_RATE, Math.max(1, init), null, null) * hpRegenMultiplier) + hpRegenBonus;
	}
	
	/**
	 * Calculate the MP regen rate (base + modifiers).
	 * @param cha
	 * @return
	 */
	public static final double calcMpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseMpRegen(cha.getLevel()) : cha.getTemplate().getBaseMpReg();
		double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				mpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			
			// Mother Tree effect is calculated at last'
			if (player.isInsideZone(ZoneIdType.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				mpRegenBonus += mpBonus;
			}
			
			if (player.isInsideZone(ZoneIdType.CLAN_HALL) && (player.getClan() != null) && (player.getClan().getHideoutId() > 0))
			{
				L2ClanHallZone zone = ZoneManager.getInstance().getZone(player, L2ClanHallZone.class);
				int posChIndex = zone == null ? -1 : zone.getResidenceId();
				int clanHallIndex = player.getClan().getHideoutId();
				if ((clanHallIndex > 0) && (clanHallIndex == posChIndex))
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneIdType.CASTLE) && (player.getClan() != null) && (player.getClan().getCastleId() > 0))
			{
				L2CastleZone zone = ZoneManager.getInstance().getZone(player, L2CastleZone.class);
				int posCastleIndex = zone == null ? -1 : zone.getResidenceId();
				int castleIndex = player.getClan().getCastleId();
				if ((castleIndex > 0) && (castleIndex == posCastleIndex))
				{
					Castle castle = CastleManager.getInstance().getCastleById(castleIndex);
					if (castle != null)
					{
						if (castle.getFunction(Castle.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) castle.getFunction(Castle.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			if (player.isInsideZone(ZoneIdType.FORT) && (player.getClan() != null) && (player.getClan().getFortId() > 0))
			{
				L2FortZone zone = ZoneManager.getInstance().getZone(player, L2FortZone.class);
				int posFortIndex = zone == null ? -1 : zone.getResidenceId();
				int fortIndex = player.getClan().getFortId();
				if ((fortIndex > 0) && (fortIndex == posFortIndex))
				{
					Fort fort = FortManager.getInstance().getFortById(fortIndex);
					if (fort != null)
					{
						if (fort.getFunction(Fort.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + ((double) fort.getFunction(Fort.FUNC_RESTORE_MP).getLvl() / 100);
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
		else if (cha.isPet())
		{
			init = ((L2PetInstance) cha).getPetLevelData().getPetRegenMP() * Config.PET_MP_REGEN_MULTIPLIER;
		}
		
		return (cha.calcStat(Stats.REGENERATE_MP_RATE, Math.max(1, init), null, null) * mpRegenMultiplier) + mpRegenBonus;
	}
	
	/**
	 * Calculate the CP regen rate (base + modifiers).
	 * @param cha
	 * @return
	 */
	public static final double calcCpRegen(L2Character cha)
	{
		double init = cha.isPlayer() ? cha.getActingPlayer().getTemplate().getBaseCpRegen(cha.getLevel()) : cha.getTemplate().getBaseHpReg();
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		double cpRegenBonus = 0;
		
		if (cha.isPlayer())
		{
			L2PcInstance player = cha.getActingPlayer();
			
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
		return (cha.calcStat(Stats.REGENERATE_CP_RATE, Math.max(1, init), null, null) * cpRegenMultiplier) + cpRegenBonus;
	}
	
	@SuppressWarnings("deprecation")
	public static final double calcFestivalRegenModifier(L2PcInstance activeChar)
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
		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
		
		if (Config.DEBUG)
		{
			_log.info("Distance: " + distToCenter + ", RegenMulti: " + ((distToCenter * 2.5) / 50));
		}
		
		return 1.0 - (distToCenter * 0.0005); // Maximum Decreased Regen of ~ -65%;
	}
	
	public static final double calcSiegeRegenModifier(L2PcInstance activeChar)
	{
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			return 0;
		}
		
		Siege siege = SiegeManager.getInstance().getSiege(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if ((siege == null) || !siege.isInProgress())
		{
			return 0;
		}
		
		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getId());
		if ((siegeClan == null) || siegeClan.getFlag().isEmpty() || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}
		
		return 1.5; // If all is true, then modifier will be 50% more
	}
	
	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		double defence = target.getPDef(attacker);
		
		switch (shld)
		{
			case Formulas.SHIELD_DEFENSE_SUCCEED:
				defence += target.getShldDef();
				break;
			case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}
		
		boolean isPvP = attacker.isPlayable() && target.isPlayer();
		boolean isPvE = attacker.isPlayable() && target.isAttackable();
		double power = skill.getPower(isPvP, isPvE);
		double damage = 0;
		double proximityBonus = 1;
		double graciaPhysSkillBonus = skill.isMagic() ? 1 : 1.10113; // Gracia final physical skill bonus 10.113%
		double ssboost = ss ? (skill.getSSBoost() > 0 ? skill.getSSBoost() : 2.04) : 1; // 104% bonus with SS
		double pvpBonus = 1;
		
		if (attacker.isPlayable() && target.isPlayable())
		{
			// Dmg bonuses in PvP fight
			pvpBonus *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			// Def bonuses in PvP fight
			defence *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}
		
		// Behind: 20% - Front: 10% (TODO: values are unconfirmed, possibly custom, remove or update when confirmed)
		proximityBonus = attacker.isBehindTarget() ? 1.2 : attacker.isInFrontOfTarget() ? 1.1 : 1;
		
		damage *= calcValakasTrait(attacker, target, skill);
		double element = calcAttributeBonus(attacker, target, skill);
		
		// SSBoost > 0 have different calculation
		if (skill.getSSBoost() > 0)
		{
			damage += (((70. * graciaPhysSkillBonus * (attacker.getPAtk(target) + power)) / defence) * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill)) * (target.calcStat(Stats.DEFENCE_CRITICAL_DAMAGE, 1, target, skill)) * ssboost * proximityBonus * element * pvpBonus) + (((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70) / defence) * graciaPhysSkillBonus);
		}
		else
		{
			damage += (((70. * graciaPhysSkillBonus * (power + (attacker.getPAtk(target) * ssboost))) / defence) * (attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill)) * (target.calcStat(Stats.DEFENCE_CRITICAL_DAMAGE, 1, target, skill)) * proximityBonus * element * pvpBonus) + (((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1 * 70) / defence) * graciaPhysSkillBonus);
		}
		
		damage += target.calcStat(Stats.DEFENCE_CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.1;
		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		// Random weapon damage
		damage *= attacker.getRandomDamageMultiplier();
		
		if (target.isAttackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
			}
			else
			{
				damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
			}
			
		}
		
		return Math.max(damage, 1);
	}
	
	/**
	 * Calculated damage caused by ATTACK of attacker on target, called separately for each weapon, if dual-weapon is used.
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param skill
	 * @param shld
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was charged by soulshot
	 * @return
	 */
	public static final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean dual, boolean ss)
	{
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isAttackable();
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		damage *= calcValakasTrait(attacker, target, skill);
		
		// Defense bonuses in PvP fight
		if (isPvP)
		{
			defence *= (skill == null) ? target.calcStat(Stats.PVP_PHYSICAL_DEF, 1, null, null) : target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
		}
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				if (!Config.ALT_GAME_SHIELD_BLOCKS)
				{
					defence += target.getShldDef();
				}
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1.;
		}
		
		if (ss)
		{
			damage *= 2;
		}
		
		if (skill != null)
		{
			double skillpower = skill.getPower(attacker, target, isPvP, isPvE);
			float ssboost = skill.getSSBoost();
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
		
		// Defense modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		boolean isBow = false;
		if ((weapon != null) && !attacker.isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					isBow = true;
					stat = Stats.BOW_WPN_VULN;
					break;
				case CROSSBOW:
					isBow = true;
					stat = Stats.CROSSBOW_WPN_VULN;
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
				case DUALDAGGER:
					stat = Stats.DUALDAGGER_WPN_VULN;
					break;
				case RAPIER:
					stat = Stats.RAPIER_WPN_VULN;
					break;
				case ANCIENTSWORD:
					stat = Stats.ANCIENT_WPN_VULN;
					break;
			}
		}
		
		// for summon use pet weapon vuln, since they can't hold weapon
		if (attacker.isServitor())
		{
			stat = Stats.PET_WPN_VULN;
		}
		
		if (crit)
		{
			// Finally retail like formula
			damage = 2 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill) * target.calcStat(Stats.DEFENCE_CRITICAL_DAMAGE, 1, target, null) * ((70 * damage) / defence);
			// Crit dmg add is almost useless in normal hits...
			damage += ((attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 70) / defence);
			damage += target.calcStat(Stats.DEFENCE_CRITICAL_DAMAGE_ADD, 0, target, skill);
		}
		else
		{
			damage = (70 * damage) / defence;
		}
		
		if (stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		if ((shld > 0) && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if (target.isNpc())
		{
			switch (((L2Npc) target).getTemplate().getRace())
			{
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
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getPAtkGiants(target);
					break;
				case MAGICCREATURE:
					damage *= attacker.getPAtkMagicCreatures(target);
					break;
				default:
					// nothing
					break;
			}
		}
		
		if ((damage > 0) && (damage < 1))
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonuses in PvP fight
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
		
		// Physical skill dmg boost
		if (skill != null)
		{
			damage = attacker.calcStat(Stats.PHYSICAL_SKILL_POWER, damage, null, null);
		}
		
		damage *= calcAttributeBonus(attacker, target, skill);
		if (target.isAttackable())
		{
			if (isBow)
			{
				if (skill != null)
				{
					damage *= attacker.calcStat(Stats.PVE_BOW_SKILL_DMG, 1, null, null);
				}
				else
				{
					damage *= attacker.calcStat(Stats.PVE_BOW_DMG, 1, null, null);
				}
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVE_PHYSICAL_DMG, 1, null, null);
			}
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (skill != null)
				{
					if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
					{
						damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
					}
				}
				else if (crit)
				{
					if (lvlDiff >= Config.NPC_CRIT_DMG_PENALTY.size())
					{
						damage *= Config.NPC_CRIT_DMG_PENALTY.get(Config.NPC_CRIT_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_CRIT_DMG_PENALTY.get(lvlDiff);
					}
				}
				else
				{
					if (lvlDiff >= Config.NPC_DMG_PENALTY.size())
					{
						damage *= Config.NPC_DMG_PENALTY.get(Config.NPC_DMG_PENALTY.size() - 1);
					}
					else
					{
						damage *= Config.NPC_DMG_PENALTY.get(lvlDiff);
					}
				}
			}
		}
		
		if (target.isPlayer() && (weapon != null) && (skill != null))
		{
			// Calculates damage on attacker
			damage = BalanceHandler.getInstance().calcPhysBalancedDamage(((L2PcInstance) target), weapon.getItemType(), damage);
		}
		
		if (attacker.isPlayer())
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * FormulasConfigs.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * FormulasConfigs.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker.isSummon())
		{
			damage = damage * FormulasConfigs.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
		}
		else if (attacker.isNpc())
		{
			damage = damage * FormulasConfigs.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		}
		
		return damage;
	}
	
	public static final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean bss, boolean mcrit)
	{
		int mAtk = attacker.getMAtk(target, skill);
		int mDef = target.getMDef(attacker, skill);
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isAttackable();
		// Pvp bonuses for def
		if (isPvP)
		{
			if (skill.isMagic())
			{
				mDef *= target.calcStat(Stats.PVP_MAGICAL_DEF, 1, null, null);
			}
			else
			{
				mDef *= target.calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, null, null);
			}
		}
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}
		
		if (bss)
		{
			mAtk *= 4;
		}
		else if (ss)
		{
			mAtk *= 2;
		}
		// MDAM Formula.
		double damage = ((91 * Math.sqrt(mAtk)) / mDef) * skill.getPower(attacker, target, isPvP, isPvE);
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker.isPlayer())
			{
				if (calcMagicSuccess(attacker, target, skill) && ((target.getLevel() - attacker.getLevel()) <= 9))
				{
					if (skill.getSkillType() == L2SkillType.DRAIN)
					{
						attacker.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
					}
					else
					{
						attacker.sendPacket(SystemMessageId.ATTACK_FAILED);
					}
					
					damage /= 2;
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					
					damage = 1;
				}
			}
			
			if (target.isPlayer())
			{
				final SystemMessage sm = (skill.getSkillType() == L2SkillType.DRAIN) ? SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN) : SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
				sm.addCharName(attacker);
				target.sendPacket(sm);
			}
		}
		else if (mcrit)
		{
			damage *= attacker.isPlayer() && target.isPlayer() ? 2.5 : 3;
			damage *= attacker.calcStat(Stats.MAGIC_CRIT_DMG, 1, null, null);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		
		// PvP bonuses for damage
		if (isPvP)
		{
			Stats stat = skill.isMagic() ? Stats.PVP_MAGICAL_DMG : Stats.PVP_PHYS_SKILL_DMG;
			damage *= attacker.calcStat(stat, 1, null, null);
		}
		
		damage *= calcAttributeBonus(attacker, target, skill);
		
		if (target.isAttackable())
		{
			damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		
		if (attacker.isPlayer())
		{
			if (((L2PcInstance) attacker).getClassId().isMage())
			{
				damage = damage * FormulasConfigs.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * FormulasConfigs.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker.isSummon())
		{
			damage = damage * FormulasConfigs.ALT_PETS_MAGICAL_DAMAGE_MULTI;
		}
		else if (attacker.isNpc())
		{
			damage = damage * FormulasConfigs.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		}
		
		return damage;
	}
	
	public static final double calcMagicDam(L2CubicInstance attacker, L2Character target, L2Skill skill, boolean mcrit, byte shld)
	{
		int mDef = target.getMDef(attacker.getOwner(), skill);
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
				mDef += target.getShldDef(); // kamael
				break;
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
				return 1;
		}
		
		int mAtk = attacker.getCubicPower();
		final boolean isPvP = target.isPlayable();
		final boolean isPvE = target.isAttackable();
		
		// Cubics MDAM Formula (similar to PDAM formula, but using 91 instead of 70, also resisted by mDef).
		double damage = 91 * ((mAtk + skill.getPower(isPvP, isPvE)) / mDef);
		
		// Failure calculation
		L2PcInstance owner = attacker.getOwner();
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(owner, target, skill))
		{
			if (calcMagicSuccess(owner, target, skill) && ((target.getLevel() - skill.getMagicLevel()) <= 9))
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL);
				}
				else
				{
					owner.sendPacket(SystemMessageId.ATTACK_FAILED);
				}
				damage /= 2;
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				owner.sendPacket(sm);
				damage = 1;
			}
			
			if (target.isPlayer())
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_DRAIN);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.RESISTED_C1_MAGIC);
					sm.addCharName(owner);
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit)
		{
			damage *= 3;
		}
		
		damage *= calcAttributeBonus(owner, target, skill);
		
		if (target.isAttackable())
		{
			damage *= attacker.getOwner().calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getOwner() != null) && ((target.getLevel() - attacker.getOwner().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getOwner().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		
		return damage;
	}
	
	/**
	 * Returns true in case of critical hit
	 * @param rate
	 * @param skill
	 * @param target
	 * @return
	 */
	public static final boolean calcCrit(double rate, boolean skill, L2Character target)
	{
		double finalRate = target.getStat().calcStat(Stats.DEFENCE_CRITICAL_RATE, rate, null, null) + target.getStat().calcStat(Stats.DEFENCE_CRITICAL_RATE_ADD, 0, null, null);
		return finalRate > Rnd.get(1000);
	}
	
	public static final boolean calcLethalHit(L2Character activeChar, L2Character target, L2Skill skill)
	{
		if ((activeChar.isPlayer() && !activeChar.getAccessLevel().canGiveDamage()) || (((skill.getCondition() & L2Skill.COND_BEHIND) != 0) && !activeChar.isBehindTarget()))
		{
			return false;
		}
		if (target.isLethalable() && !target.isInvul())
		{
			// Lvl Bonus Modifier.
			double lethalStrikeRate = skill.getLethalStrikeRate() * calcLvlBonusMod(activeChar, target, skill);
			double halfKillRate = skill.getHalfKillRate() * calcLvlBonusMod(activeChar, target, skill);
			
			// Lethal Strike
			if (Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, lethalStrikeRate, target, null))
			{
				// for Players CP and HP is set to 1.
				if (target.isPlayer() && FormulasConfigs.ALLOW_LETHAL_STRIKES_ON_PLAYERS)
				{
					target.setCurrentCp(1);
					target.setCurrentHp(1);
					target.sendPacket(SystemMessageId.LETHAL_STRIKE);
				}
				// for Monsters HP is set to 1.
				else if (target.isMonster() && FormulasConfigs.ALLOW_LETHAL_STRIKES_ON_MOBS)
				{
					target.setCurrentHp(1);
				}
				else if (target.isSummon() && FormulasConfigs.ALLOW_LETHAL_STRIKES_ON_SUMMONS)
				{
					target.setCurrentHp(1);
				}
				activeChar.sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
			}
			// Half-Kill
			else if (Rnd.get(100) < activeChar.calcStat(Stats.LETHAL_RATE, halfKillRate, target, null))
			{
				// for Players CP is set to 1.
				if (target.isPlayer() && FormulasConfigs.ALLOW_HALF_KILL_ON_PLAYERS)
				{
					target.setCurrentCp(1);
					target.sendPacket(SystemMessageId.HALF_KILL);
					target.sendPacket(SystemMessageId.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
				}
				// for Monsters HP is set to 50%.
				else if (target.isMonster() && FormulasConfigs.ALLOW_HALF_KILL_ON_MOBS)
				{
					target.setCurrentHp(target.getCurrentHp() * 0.5);
				}
				else if (target.isSummon() && FormulasConfigs.ALLOW_HALF_KILL_ON_SUMMONS)
				{
					target.setCurrentHp(target.getCurrentHp() * 0.5);
				}
				activeChar.sendPacket(SystemMessageId.HALF_KILL);
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public static final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	
	/**
	 * @param target
	 * @param dmg
	 * @return true in case when ATTACK is canceled due to hit
	 */
	public static final boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target.getFusionSkill() != null)
		{
			return true;
		}
		
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if ((wpn != null) && (wpn.getItemType() == WeaponType.BOW))
			{
				init = 15;
			}
		}
		
		if (target.isRaid() || target.isInvul() || (init <= 0))
		{
			return false; // No attack break
		}
		
		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);
		
		// Chance is affected by target MEN
		init -= ((BaseStats.MEN.calcBonus(target) * 100) - 100);
		
		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		
		// Adjust the rate to be between 1 and 99
		rate = Math.max(Math.min(rate, 99), 1);
		
		return Rnd.get(100) < rate;
	}
	
	/**
	 * Calculate delay (in milliseconds) before next ATTACK
	 * @param attacker
	 * @param target
	 * @param rate
	 * @return
	 */
	public static final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
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
	 * @param attacker
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public static final int calcAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime / attacker.getMAtkSpd()) * 333);
		}
		return (int) ((skillTime / attacker.getPAtkSpd()) * 300);
	}
	
	/**
	 * Formula based on http://l2p.l2wh.com/nonskillattacks.html
	 * @param attacker
	 * @param target
	 * @return {@code true} if hit missed (target evaded), {@code false} otherwise.
	 */
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
		
		// Get additional bonus from the conditions when you are attacking
		chance *= HitConditionBonusData.getInstance().getConditionBonus(attacker, target);
		
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		
		return chance < Rnd.get(1000);
	}
	
	/**
	 * Returns:<br>
	 * 0 = shield defense doesn't succeed<br>
	 * 1 = shield defense succeed<br>
	 * 2 = perfect block<br>
	 * @param attacker
	 * @param target
	 * @param skill
	 * @param sendSysMsg
	 * @return
	 */
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill, boolean sendSysMsg)
	{
		if ((skill != null) && skill.ignoreShield())
		{
			return 0;
		}
		
		L2Item item = target.getSecondaryWeaponItem();
		if ((item == null) || !(item instanceof L2Armor) || (((L2Armor) item).getItemType() == ArmorType.SIGIL))
		{
			return 0;
		}
		
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);
		if (shldRate <= 1e-6)
		{
			return 0;
		}
		
		int degreeside = (int) target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 0, null, null) + 120;
		if ((degreeside < 360) && (!target.isFacing(attacker, degreeside)))
		{
			return 0;
		}
		
		byte shldSuccess = SHIELD_DEFENSE_FAILED;
		// if attacker
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		L2Weapon at_weapon = attacker.getActiveWeaponItem();
		if ((at_weapon != null) && (at_weapon.getItemType() == WeaponType.BOW))
		{
			shldRate *= 1.3;
		}
		
		if ((shldRate > 0) && ((100 - Config.ALT_PERFECT_SHLD_BLOCK) < Rnd.get(100)))
		{
			shldSuccess = SHIELD_DEFENSE_PERFECT_BLOCK;
		}
		else if (shldRate > Rnd.get(100))
		{
			shldSuccess = SHIELD_DEFENSE_SUCCEED;
		}
		
		if (sendSysMsg && target.isPlayer())
		{
			L2PcInstance enemy = target.getActingPlayer();
			
			switch (shldSuccess)
			{
				case SHIELD_DEFENSE_SUCCEED:
					enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
					break;
				case SHIELD_DEFENSE_PERFECT_BLOCK:
					enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
					break;
			}
		}
		
		return shldSuccess;
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target, L2Skill skill)
	{
		return calcShldUse(attacker, target, skill, true);
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, null, true);
	}
	
	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		double defence = 0;
		if (skill.isActive() && skill.isOffensive())
		{
			defence = target.getMDef(actor, skill);
		}
		
		double attack = 2 * actor.getMAtk(target, skill) * (1 + (calcSkillVulnerability(actor, target, skill) / 100));
		double d = (attack - defence) / (attack + defence);
		
		if (skill.isDebuff())
		{
			if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	public static double calcSkillVulnerability(L2Character attacker, L2Character target, L2Skill skill)
	{
		double multiplier = 0; // initialize...
		
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// Finally, calculate skill type vulnerabilities
			multiplier = calcSkillTraitVulnerability(multiplier, target, skill);
		}
		return multiplier;
	}
	
	public static double calcSkillTraitVulnerability(double multiplier, L2Character target, L2Skill skill)
	{
		if (skill == null)
		{
			return multiplier;
		}
		
		final L2TraitType trait = skill.getTraitType();
		// First check if skill have trait set
		// If yes, use correct vuln
		if ((trait != null) && (trait != L2TraitType.NONE))
		{
			switch (trait)
			{
				case BLEED:
					multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
					break;
				case BOSS:
					multiplier = target.calcStat(Stats.BOSS_VULN, multiplier, target, null);
					break;
				// case DEATH:
				case DERANGEMENT:
					multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
					break;
				// case ETC:
				case GUST:
					multiplier = target.calcStat(Stats.GUST_VULN, multiplier, target, null);
					break;
				case HOLD:
					multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
					break;
				case PHYSICAL_BLOCKADE:
					multiplier = target.calcStat(Stats.PHYSICALBLOCKADE_VULN, multiplier, target, null);
					break;
				case POISON:
					multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = target.calcStat(Stats.VALAKAS_VULN, multiplier, target, null);
					break;
			}
		}
		else
		{
			// Since not all traits are handled by trait parameter
			// rest is checked by skillType or isDebuff Boolean.
			final L2SkillType type = skill.getSkillType();
			if (type == L2SkillType.BUFF)
			{
				multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
			}
			else if ((type == L2SkillType.DEBUFF) || (skill.isDebuff()))
			{
				multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
			}
		}
		return multiplier;
	}
	
	public static double calcSkillProficiency(L2Skill skill, L2Character attacker, L2Character target)
	{
		double multiplier = 0;
		
		if (skill != null)
		{
			// Calculate trait-type vulnerabilities
			multiplier = calcSkillTraitProficiency(multiplier, attacker, target, skill);
		}
		
		return multiplier;
	}
	
	public static double calcSkillTraitProficiency(double multiplier, L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill == null)
		{
			return multiplier;
		}
		
		final L2TraitType trait = skill.getTraitType();
		// First check if skill have trait set
		// If yes, use correct vuln
		if ((trait != null) && (trait != L2TraitType.NONE))
		{
			switch (trait)
			{
				case BLEED:
					multiplier = attacker.calcStat(Stats.BLEED_PROF, multiplier, target, null);
					break;
				// case BOSS:
				// case DEATH:
				case DERANGEMENT:
					multiplier = attacker.calcStat(Stats.DERANGEMENT_PROF, multiplier, target, null);
					break;
				// case ETC:
				// case GUST:
				case HOLD:
					multiplier = attacker.calcStat(Stats.ROOT_PROF, multiplier, target, null);
					break;
				case PARALYZE:
					multiplier = attacker.calcStat(Stats.PARALYZE_PROF, multiplier, target, null);
					break;
				// case PHYSICAL_BLOCKADE:
				case POISON:
					multiplier = attacker.calcStat(Stats.POISON_PROF, multiplier, target, null);
					break;
				case SHOCK:
					multiplier = attacker.calcStat(Stats.STUN_PROF, multiplier, target, null);
					break;
				case SLEEP:
					multiplier = attacker.calcStat(Stats.SLEEP_PROF, multiplier, target, null);
					break;
				case VALAKAS:
					multiplier = attacker.calcStat(Stats.VALAKAS_PROF, multiplier, target, null);
					break;
			}
		}
		else
		{
			// Since not all traits are handled by skill parameter
			// rest is checked by skillType or isDebuff Boolean.
			final L2SkillType type = skill.getSkillType();
			if ((type == L2SkillType.DEBUFF) || (skill.isDebuff()))
			{
				multiplier = target.calcStat(Stats.DEBUFF_PROF, multiplier, target, null);
			}
		}
		return multiplier;
	}
	
	public static double calcLvlBonusMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
		double skillLvlBonusRateMod = 1 + (skill.getLvlBonusRate() / 100.);
		double lvlMod = 1 + ((attackerLvl - target.getLevel()) / 100.);
		return skillLvlBonusRateMod * lvlMod;
	}
	
	public static double calcElementMod(L2Character attacker, L2Character target, L2Skill skill)
	{
		final byte skillElement = skill.getElement();
		if (skillElement == Elementals.NONE)
		{
			return 1;
		}
		
		int attackerElement = attacker.getAttackElement() == skillElement ? attacker.getAttackElementValue(skillElement) + skill.getElementPower() : attacker.getAttackElementValue(skillElement);
		int targetElement = target.getDefenseElementValue(skillElement);
		double elementMod = 1 + ((attackerElement - targetElement) / 1000.);
		return elementMod;
	}
	
	public static boolean calcEffectSuccess(L2Character attacker, L2Character target, EffectTemplate effect, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		// Perfect Shield Block.
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " blocked by shield");
			}
			
			return false;
		}
		
		if (skill.isDebuff())
		{
			if (skill.getPower() == -1)
			{
				if (attacker.isDebug())
				{
					attacker.sendDebugMessage(skill.getName() + " ignoring resists");
				}
				return true;
			}
			
			if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, attacker, skill) > 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				attacker.sendPacket(sm);
				return false;
			}
		}
		
		final double activateRate = effect.effectPower;
		if ((activateRate == -1) || (skill.getBasicProperty() == BaseStats.NONE))
		{
			return true;
		}
		
		int magicLevel = skill.getMagicLevel();
		if (magicLevel <= -1)
		{
			magicLevel = target.getLevel() + 3;
		}
		
		int targetBaseStat = 0;
		switch (skill.getBasicProperty())
		{
			case STR:
				targetBaseStat = target.getSTR();
				break;
			case DEX:
				targetBaseStat = target.getDEX();
				break;
			case CON:
				targetBaseStat = target.getCON();
				break;
			case INT:
				targetBaseStat = target.getINT();
				break;
			case MEN:
				targetBaseStat = target.getMEN();
				break;
			case WIT:
				targetBaseStat = target.getWIT();
				break;
		}
		
		// Calculate BaseRate.
		final double baseMod = ((((((magicLevel - target.getLevel()) + 3) * skill.getLvlBonusRate()) + activateRate) + 30.0) - targetBaseStat);
		double rate = baseMod;
		
		// Resists.
		double vuln = calcSkillTraitVulnerability(0, target, skill);
		double prof = calcSkillTraitProficiency(0, attacker, target, skill);
		double resMod = 1 + ((vuln + prof) / 100);
		
		// Check ResMod Limits.
		rate *= Math.min(Math.max(resMod, 0.1), 1.9);
		
		// Lvl Bonus Modifier.
		double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
		rate *= lvlBonusMod;
		
		// Element Modifier.
		double elementMod = calcElementMod(attacker, target, skill);
		rate *= elementMod;
		
		// Add Matk/Mdef Bonus
		double mAtkMod = 1.0;
		if (skill.isMagic())
		{
			double mAtk = attacker.getMAtk(null, null);
			double val = 0;
			// TODO: FIX Bless spiritshot multiplier
			/*
			 * if (skill.isBlessedSpiritShot())// only blessed spiritshot! { val = mAtk * 3.0;// 3.0 is the blessed spiritshot multiplier }
			 */
			val += mAtk;
			val = (Math.sqrt(val) / target.getMDef(null, null)) * 11.0;
			mAtkMod = val;
		}
		
		rate *= mAtkMod;
		double finalRate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.isDebug() || Config.DEVELOPER)
		{
			final StatsSet set = new StatsSet();
			set.set("baseMod", baseMod);
			set.set("resMod", resMod);
			set.set("lvlBonusMod", lvlBonusMod);
			set.set("elementMod", elementMod);
			set.set("mAtkMod", mAtkMod);
			set.set("rate", rate);
			set.set("finalRate", finalRate);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		return (Rnd.get(100) < rate);
	}
	
	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss, boolean sps, boolean bss)
	{
		// Perfect Shield Block.
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			if (attacker.isDebug())
			{
				attacker.sendDebugMessage(skill.getName() + " blocked by shield");
			}
			
			return false;
		}
		
		if (skill.isDebuff())
		{
			if (skill.getPower() == -1)
			{
				if (attacker.isDebug())
				{
					attacker.sendDebugMessage(skill.getName() + " ignoring resists");
				}
				return true;
			}
			
			if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, attacker, skill) > 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				attacker.sendPacket(sm);
				return false;
			}
		}
		
		// Calculate BaseRate.
		double baseRate = skill.getPower();
		double statMod = skill.getBasicProperty().calcBonus(target);
		double rate = (baseRate / statMod);
		
		// Resists.
		double vuln = calcSkillTraitVulnerability(0, target, skill);
		double prof = calcSkillTraitProficiency(0, attacker, target, skill);
		double resMod = 1 + ((vuln + prof) / 100);
		
		// Check ResMod Limits.
		rate *= Math.min(Math.max(resMod, 0.1), 1.9);
		
		// Lvl Bonus Modifier.
		double lvlBonusMod = calcLvlBonusMod(attacker, target, skill);
		rate *= lvlBonusMod;
		
		// Element Modifier.
		double elementMod = calcElementMod(attacker, target, skill);
		rate *= elementMod;
		
		// Add Matk/Mdef Bonus
		double mAtkMod = 1.0;
		if (skill.isMagic())
		{
			double mAtk = attacker.getMAtk(null, null);
			double val = 0;
			// TODO: FIX Bless spiritshot multiplier
			/*
			 * if (skill.isBlessedSpiritShot())// only blessed spiritshot! { val = mAtk * 3.0;// 3.0 is the blessed spiritshot multiplier }
			 */
			val += mAtk;
			val = (Math.sqrt(val) / target.getMDef(null, null)) * 11.0;
			mAtkMod = val;
		}
		
		rate *= mAtkMod;
		double finalRate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("baseRate", baseRate);
			set.set("resMod", resMod);
			set.set("lvlBonusMod", lvlBonusMod);
			set.set("elementMod", elementMod);
			set.set("mAtkMod", mAtkMod);
			set.set("rate", rate);
			set.set("finalRate", finalRate);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		
		// Check the Rate Limits.
		
		return (Rnd.get(100) < finalRate);
	}
	
	public static boolean calcCubicSkillSuccess(L2CubicInstance attacker, L2Character target, L2Skill skill, byte shld)
	{
		if (skill.isDebuff())
		{
			if (skill.getPower() == -1)
			{
				return true;
			}
			else if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0)
			{
				return false;
			}
		}
		
		// Perfect Shield Block.
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			return false;
		}
		
		// if target reflect this skill then the effect will fail
		if (calcSkillReflect(target, skill) != SKILL_REFLECT_FAILED)
		{
			return false;
		}
		
		// Calculate BaseRate.
		double baseRate = skill.getPower();
		double statMod = skill.getBasicProperty().calcBonus(target);
		double rate = (baseRate / statMod);
		
		// Resists.
		double vuln = calcSkillVulnerability(attacker.getOwner(), target, skill);
		double prof = calcSkillProficiency(skill, attacker.getOwner(), target);
		double resMod = 1 + ((vuln + prof) / 100);
		
		// Check ResMod Limits.
		rate *= Math.min(Math.max(resMod, 0.1), 1.9);
		
		// Lvl Bonus Modifier.
		double lvlBonusMod = calcLvlBonusMod(attacker.getOwner(), target, skill);
		rate *= lvlBonusMod;
		
		// Element Modifier.
		double elementMod = calcElementMod(attacker.getOwner(), target, skill);
		rate *= elementMod;
		
		// Add Matk/Mdef Bonus (TODO: Pending)
		
		// Check the Rate Limits.
		double finalRate = Math.min(Math.max(rate, skill.getMinChance()), skill.getMaxChance());
		
		if (attacker.getOwner().isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("baseMod", baseRate);
			set.set("resMod", resMod);
			set.set("statMod", statMod);
			set.set("elementMod", elementMod);
			set.set("lvlBonusMod", lvlBonusMod);
			set.set("rate", rate);
			set.set("finalRate", finalRate);
			Debug.sendSkillDebug(attacker.getOwner(), target, skill, set);
		}
		
		return (Rnd.get(100) < finalRate);
	}
	
	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		if (skill.getPower() == -1)
		{
			return true;
		}
		
		// FIXME: Fix this LevelMod Formula.
		// int lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		int lvlDifference = target.getLevel() - attacker.getLevel();
		double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if (target.isAttackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 3))
		{
			int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
			if (lvlDiff >= Config.NPC_SKILL_CHANCE_PENALTY.size())
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size() - 1);
			}
			else
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
			}
		}
		// general magic resist
		final double resModifier = target.calcStat(Stats.MAGIC_SUCCESS_RES, 1, null, skill);
		int rate = 100 - Math.round((float) (lvlModifier * targetModifier * resModifier));
		
		if (attacker.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("lvlDifference", lvlDifference);
			set.set("lvlModifier", lvlModifier);
			set.set("resModifier", resModifier);
			set.set("targetModifier", targetModifier);
			set.set("rate", rate);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		
		return (Rnd.get(100) < rate);
	}
	
	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		// Mana Burn = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		final boolean isPvP = attacker.isPlayable() && target.isPlayable();
		final boolean isPvE = attacker.isPlayable() && target.isAttackable();
		double mp = target.getMaxMp();
		if (bss)
		{
			mAtk *= 4;
		}
		else if (ss)
		{
			mAtk *= 2;
		}
		
		double damage = (Math.sqrt(mAtk) * skill.getPower(attacker, target, isPvP, isPvE) * (mp / 97)) / mDef;
		damage *= (1 + (calcSkillVulnerability(attacker, target, skill) / 100));
		if (target.isAttackable())
		{
			damage *= attacker.calcStat(Stats.PVE_MAGICAL_DMG, 1, null, null);
			if (!target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 2))
			{
				int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
				if (lvlDiff >= Config.NPC_SKILL_DMG_PENALTY.size())
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(Config.NPC_SKILL_DMG_PENALTY.size() - 1);
				}
				else
				{
					damage *= Config.NPC_SKILL_DMG_PENALTY.get(lvlDiff);
				}
			}
		}
		
		return damage;
	}
	
	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if ((baseRestorePercent == 0) || (baseRestorePercent == 100))
		{
			return baseRestorePercent;
		}
		
		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if ((restorePercent - baseRestorePercent) > 20.0)
		{
			restorePercent += 20.0;
		}
		
		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);
		
		return restorePercent;
	}
	
	public static boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if ((skill.isMagic() && (skill.getSkillType() != L2SkillType.BLOW)) || skill.isDebuff())
		{
			return false;
		}
		
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}
	
	public static boolean calcSkillMastery(L2Character actor, L2Skill sk)
	{
		// Static Skills are not affected by Skill Mastery.
		if (sk.isStatic())
		{
			return false;
		}
		
		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);
		
		if (actor.isPlayer())
		{
			if (actor.getActingPlayer().isMageClass())
			{
				val *= BaseStats.INT.calcBonus(actor);
			}
			else
			{
				val *= BaseStats.STR.calcBonus(actor);
			}
		}
		
		return Rnd.get(100) < val;
	}
	
	public static double calcValakasTrait(L2Character attacker, L2Character target, L2Skill skill)
	{
		double calcPower = 0;
		double calcDefen = 0;
		
		if ((skill != null) && (skill.getTraitType() == L2TraitType.VALAKAS))
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
		}
		else
		{
			calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
			if (calcPower > 0)
			{
				calcPower = attacker.calcStat(Stats.VALAKAS_PROF, calcPower, target, skill);
				calcDefen = target.calcStat(Stats.VALAKAS_VULN, calcDefen, target, skill);
			}
		}
		return 1 + ((calcDefen + calcPower) / 100);
	}
	
	public static double calcAttributeBonus(L2Character attacker, L2Character target, L2Skill skill)
	{
		int attack_attribute;
		int defence_attribute;
		
		if (skill != null)
		{
			if (skill.getElement() == -1)
			{
				attack_attribute = 0;
				defence_attribute = target.getDefenseElementValue((byte) -1);
			}
			else
			{
				if (attacker.getAttackElement() == skill.getElement())
				{
					attack_attribute = attacker.getAttackElementValue(attacker.getAttackElement()) + skill.getElementPower();
					defence_attribute = target.getDefenseElementValue(attacker.getAttackElement());
				}
				else
				{
					attack_attribute = skill.getElementPower();
					defence_attribute = target.getDefenseElementValue(skill.getElement());
				}
			}
		}
		else
		{
			attack_attribute = attacker.getAttackElementValue(attacker.getAttackElement());
			defence_attribute = target.getDefenseElementValue(attacker.getAttackElement());
		}
		
		double attack_attribute_mod = 0;
		double defence_attribute_mod = 0;
		
		if (attack_attribute >= 450)
		{
			if (defence_attribute >= 450)
			{
				attack_attribute_mod = 0.06909;
				defence_attribute_mod = 0.078;
			}
			else if (attack_attribute >= 350)
			{
				attack_attribute_mod = 0.0887;
				defence_attribute_mod = 0.1007;
			}
			else
			{
				attack_attribute_mod = 0.129;
				defence_attribute_mod = 0.1473;
			}
		}
		else if (attack_attribute >= 300)
		{
			if (defence_attribute >= 300)
			{
				attack_attribute_mod = 0.0887;
				defence_attribute_mod = 0.1007;
			}
			else if (defence_attribute >= 150)
			{
				attack_attribute_mod = 0.129;
				defence_attribute_mod = 0.1473;
			}
			else
			{
				attack_attribute_mod = 0.25;
				defence_attribute_mod = 0.2894;
			}
		}
		else if (attack_attribute >= 150)
		{
			if (defence_attribute >= 150)
			{
				attack_attribute_mod = 0.129;
				defence_attribute_mod = 0.1473;
			}
			else if (defence_attribute >= 0)
			{
				attack_attribute_mod = 0.25;
				defence_attribute_mod = 0.2894;
			}
			else
			{
				attack_attribute_mod = 0.4;
				defence_attribute_mod = 0.55;
			}
		}
		else if (attack_attribute >= -99)
		{
			if (defence_attribute >= 0)
			{
				attack_attribute_mod = 0.25;
				defence_attribute_mod = 0.2894;
			}
			else
			{
				attack_attribute_mod = 0.4;
				defence_attribute_mod = 0.55;
			}
		}
		else
		{
			if (defence_attribute >= 450)
			{
				attack_attribute_mod = 0.06909;
				defence_attribute_mod = 0.078;
			}
			else if (defence_attribute >= 350)
			{
				attack_attribute_mod = 0.0887;
				defence_attribute_mod = 0.1007;
			}
			else
			{
				attack_attribute_mod = 0.129;
				defence_attribute_mod = 0.1473;
			}
		}
		
		int attribute_diff = attack_attribute - defence_attribute;
		double min;
		double max;
		if (attribute_diff >= 300)
		{
			max = 100.0;
			min = -50;
		}
		else if (attribute_diff >= 150)
		{
			max = 70.0;
			min = -50;
		}
		else if (attribute_diff >= -150)
		{
			max = 40.0;
			min = -50;
		}
		else if (attribute_diff >= -300)
		{
			max = 40.0;
			min = -60;
		}
		else
		{
			max = 40.0;
			min = -80;
		}
		
		attack_attribute += 100;
		attack_attribute *= attack_attribute;
		
		attack_attribute_mod = (attack_attribute / 144.0) * attack_attribute_mod;
		
		defence_attribute += 100;
		defence_attribute *= defence_attribute;
		
		defence_attribute_mod = (defence_attribute / 169.0) * defence_attribute_mod;
		
		double attribute_mod_diff = attack_attribute_mod - defence_attribute_mod;
		
		attribute_mod_diff = Math.min(Math.max(attribute_mod_diff, min), max);
		
		double result = (attribute_mod_diff / 100.0) + 1;
		
		if (attacker.isPlayer() && target.isPlayer() && (result < 1.0))
		{
			result = 1.0;
		}
		
		return result;
	}
	
	/**
	 * Calculate skill reflection according these three possibilities:<br>
	 * <ul>
	 * <li>Reflect failed</li>
	 * <li>Normal reflect (just effects). <U>Only possible for skilltypes: BUFF, REFLECT, HEAL_PERCENT, MANAHEAL_PERCENT, HOT, CPHOT, MPHOT</U></li>
	 * <li>vengEance reflect (100% damage reflected but damage is also dealt to actor). <U>This is only possible for skills with skilltype PDAM, BLOW, CHARGEDAM, MDAM or DEATHLINK</U></li>
	 * </ul>
	 * @param target
	 * @param skill
	 * @return SKILL_REFLECTED_FAILED, SKILL_REFLECT_SUCCEED or SKILL_REFLECT_VENGEANCE
	 */
	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		// Neither some special skills (like hero debuffs...) or those skills ignoring resistances can be reflected
		if (!skill.canBeReflected() || (skill.getPower() == -1))
		{
			return SKILL_REFLECT_FAILED;
		}
		
		// Only magic and melee skills can be reflected
		if (!skill.isMagic() && ((skill.getCastRange() == -1) || (skill.getCastRange() > MELEE_ATTACK_RANGE)))
		{
			return SKILL_REFLECT_FAILED;
		}
		
		byte reflect = SKILL_REFLECT_FAILED;
		// Check for non-reflected skilltypes, need additional retail check
		switch (skill.getSkillType())
		{
		// vGodfather fixes
			case FEAR:
			case ROOT:
			case STUN:
			case MUTE:
			case BLEED:
			case PARALYZE:
			case SLEEP:
			case DEBUFF:
				// vGodFather fixes End
			case PDAM:
			case MDAM:
			case BLOW:
			case DRAIN:
			case CHARGEDAM:
			case FATAL:
			case DEATHLINK:
			case MANADAM:
				final Stats stat = skill.isMagic() ? Stats.VENGEANCE_SKILL_MAGIC_DAMAGE : Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE;
				final double venganceChance = target.getStat().calcStat(stat, 0, target, skill);
				if (venganceChance > Rnd.get(100))
				{
					reflect |= SKILL_REFLECT_VENGEANCE;
				}
				break;
			default:
			{
				return SKILL_REFLECT_FAILED;
			}
		}
		
		final double reflectChance = target.calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, skill);
		if (Rnd.get(100) < reflectChance)
		{
			reflect |= SKILL_REFLECT_SUCCEED;
		}
		
		return reflect;
	}
	
	public static void calcDamageReflected(L2Character activeChar, L2Character target, L2Skill skill, double damage)
	{
		boolean reflect = true;
		if ((skill.getCastRange() == -1) || (skill.getCastRange() > MELEE_ATTACK_RANGE))
		{
			reflect = false;
		}
		
		if (reflect)
		{
			final double vengeanceChance = target.getStat().calcStat(Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0, target, skill);
			if (vengeanceChance > Rnd.get(100))
			{
				if (target.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
					sm.addCharName(activeChar);
					target.sendPacket(sm);
				}
				if (activeChar.isPlayer())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
					sm.addCharName(target);
					activeChar.sendPacket(sm);
				}
				
				// TODO: Is this retail like?
				double vegdamage = (damage / 100) * vengeanceChance;
				activeChar.reduceCurrentHp(vegdamage, target, skill);
			}
		}
	}
	
	/**
	 * Calculate damage caused by falling
	 * @param cha
	 * @param fallHeight
	 * @return damage
	 */
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if (!Config.ENABLE_FALLING_DAMAGE || (fallHeight < 0))
		{
			return 0;
		}
		final double damage = cha.calcStat(Stats.FALL, (fallHeight * cha.getMaxHp()) / 1000.0, null, null);
		return damage;
	}
	
	public static boolean calcBlowSuccess(L2Character activeChar, L2Character target, L2Skill skill)
	{
		double dexMod = BaseStats.DEX.calcBonus(activeChar);
		// Apply DEX Mod.
		double blowChance = skill.getBlowChance();
		// Apply Position Bonus (TODO: values are unconfirmed, possibly custom, remove or update when confirmed).
		double sideMod = (activeChar.isInFrontOfTarget()) ? 1 : (activeChar.isBehindTarget()) ? 2 : 1.5;
		// Apply all mods.
		double baseRate = blowChance * dexMod * sideMod;
		// Apply blow rates
		double rate = activeChar.calcStat(Stats.BLOW_RATE, baseRate, target, null);
		// Debug
		if (activeChar.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("dexMod", dexMod);
			set.set("blowChance", blowChance);
			set.set("sideMod", sideMod);
			set.set("baseRate", baseRate);
			set.set("rate", rate);
			Debug.sendSkillDebug(activeChar, target, skill, set);
		}
		
		return (skill.getDmgDirectlyToHP() && !activeChar.isBehindTarget() ? false : Rnd.get(100) < rate);
	}
	
	public static List<L2Effect> calcCancelStealEffects(L2Character activeChar, L2Character target, L2Skill skill, double power)
	{
		// Resists.
		int cancelMagicLvl = skill.getMagicLevel();
		int count = skill.getMaxNegatedEffects();
		int finalCount = Rnd.get(1, count);
		final double vuln = target.calcStat(Stats.CANCEL_VULN, 0, target, null);
		final double prof = activeChar.calcStat(Stats.CANCEL_PROF, 0, target, null);
		double resMod = 1 + ((vuln + prof) / 100);
		double rate = power / resMod;
		
		final L2Effect[] effects = target.getAllEffects();
		List<L2Effect> canceled = new ArrayList<>(count);
		
		if (activeChar.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("baseMod", rate);
			set.set("magicLevel", cancelMagicLvl);
			set.set("resMod", resMod);
			set.set("rate", rate);
			Debug.sendSkillDebug(activeChar, target, skill, set);
		}
		
		if (calcStealChance(activeChar, target))
		{
			// Cancel for Abnormals.
			if (skill.getNegateAbnormals() != null)
			{
				for (L2Effect eff : effects)
				{
					if (eff == null)
					{
						continue;
					}
					
					for (String negateAbnormalType : skill.getNegateAbnormals().keySet())
					{
						if (negateAbnormalType.equalsIgnoreCase(eff.getAbnormalType()) && (skill.getNegateAbnormals().get(negateAbnormalType) >= eff.getAbnormalLvl()))
						{
							eff.exit();
						}
					}
				}
			}
			else
			{
				List<L2Effect> _musicList = new ArrayList<>();
				List<L2Effect> _buffList = new ArrayList<>();
				
				for (L2Effect eff : effects)
				{
					// Just in case of NPE and remove effect if can't be stolen
					if ((eff == null) || !eff.canBeStolen())
					{
						continue;
					}
					
					// Only Dances/Songs.
					if (eff.getSkill().isDance())
					{
						_musicList.add(eff);
					}
					else
					{
						_buffList.add(eff);
					}
				}
				
				// Reversing lists and adding to a new list
				List<L2Effect> _effectList = new ArrayList<>();
				Collections.reverse(_musicList);
				Collections.reverse(_buffList);
				_effectList.addAll(_musicList);
				_effectList.addAll(_buffList);
				
				int negated = 0;
				if (!_effectList.isEmpty())
				{
					for (L2Effect e : _effectList)
					{
						if (negated < finalCount)
						{
							negated++;
							canceled.add(e);
						}
					}
				}
			}
		}
		
		return canceled;
	}
	
	/**
	 * Calculates karma lost upon death.
	 * @param player
	 * @param exp
	 * @return the amount of karma player has loosed.
	 */
	public static int calculateKarmaLost(L2PcInstance player, long exp)
	{
		double karmaLooseMul = KarmaData.getInstance().getMultiplier(player.getLevel());
		if (exp > 0) // Received exp
		{
			exp /= Config.RATE_KARMA_LOST;
		}
		return (int) ((Math.abs(exp) / karmaLooseMul) / 15);
	}
	
	/**
	 * Calculates karma gain upon player kill.
	 * @param pkCount
	 * @param isSummon
	 * @return karma points that will be added to the player.
	 */
	public static int calculateKarmaGain(int pkCount, boolean isSummon)
	{
		int result = 14400;
		if (pkCount < 100)
		{
			result = (int) (((((pkCount - 1) * 0.5) + 1) * 60) * 4);
		}
		else if (pkCount < 180)
		{
			result = (int) (((((pkCount + 1) * 0.125) + 37.5) * 60) * 4);
		}
		
		if (isSummon)
		{
			result = ((pkCount & 3) + result) >> 2;
		}
		
		return result;
	}
	
	private static boolean calcStealChance(L2Character effected, L2Character effector)
	{
		double cancel_res_multiplier = effected.calcStat(Stats.CANCEL_VULN, 1, null, null);
		int dml = effector.getLevel() - effected.getLevel(); // to check: magicLevel or player level? Since it's magic skill setting player level as default
		double prelimChance = (dml + 50) * (1 - (cancel_res_multiplier * .01)); // 50 is random reasonable constant which gives ~50% chance of steal success while else is equal
		
		if ((FormulasConfigs.DECREASE_CANCEL_SUCCESS_RATE > 0) && (prelimChance > FormulasConfigs.DECREASE_CANCEL_SUCCESS_RATE))
		{
			prelimChance -= FormulasConfigs.DECREASE_CANCEL_SUCCESS_RATE;
			if (prelimChance < 1)
			{
				return false;
			}
		}
		
		return Rnd.chance(prelimChance);
	}
}
