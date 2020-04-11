/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.handler.EffectHandler;
import com.l2jserver.gameserver.model.effects.AbstractEffect;

import handlers.effecthandlers.consume.ConsumeChameleonRest;
import handlers.effecthandlers.consume.ConsumeFakeDeath;
import handlers.effecthandlers.consume.ConsumeHp;
import handlers.effecthandlers.consume.ConsumeMp;
import handlers.effecthandlers.consume.ConsumeMpByLevel;
import handlers.effecthandlers.consume.ConsumeRest;
import handlers.effecthandlers.custom.BlockAction;
import handlers.effecthandlers.custom.Buff;
import handlers.effecthandlers.custom.Debuff;
import handlers.effecthandlers.custom.Detection;
import handlers.effecthandlers.custom.Flag;
import handlers.effecthandlers.custom.Grow;
import handlers.effecthandlers.custom.ImmobileBuff;
import handlers.effecthandlers.custom.ImmobilePetBuff;
import handlers.effecthandlers.custom.Mute;
import handlers.effecthandlers.custom.OpenChest;
import handlers.effecthandlers.custom.OpenDoor;
import handlers.effecthandlers.custom.Paralyze;
import handlers.effecthandlers.custom.Recovery;
import handlers.effecthandlers.custom.Root;
import handlers.effecthandlers.custom.SilentMove;
import handlers.effecthandlers.custom.Sleep;
import handlers.effecthandlers.custom.Stun;
import handlers.effecthandlers.custom.ThrowUp;
import handlers.effecthandlers.instant.AddHate;
import handlers.effecthandlers.instant.Backstab;
import handlers.effecthandlers.instant.Blink;
import handlers.effecthandlers.instant.Bluff;
import handlers.effecthandlers.instant.CallParty;
import handlers.effecthandlers.instant.CallPc;
import handlers.effecthandlers.instant.CallSkill;
import handlers.effecthandlers.instant.ChangeFace;
import handlers.effecthandlers.instant.ChangeHairColor;
import handlers.effecthandlers.instant.ChangeHairStyle;
import handlers.effecthandlers.instant.ClanGate;
import handlers.effecthandlers.instant.Confuse;
import handlers.effecthandlers.instant.ConsumeBody;
import handlers.effecthandlers.instant.ConvertItem;
import handlers.effecthandlers.instant.Cp;
import handlers.effecthandlers.instant.DeathLink;
import handlers.effecthandlers.instant.DeleteHate;
import handlers.effecthandlers.instant.DeleteHateOfMe;
import handlers.effecthandlers.instant.DetectHiddenObjects;
import handlers.effecthandlers.instant.DispelAll;
import handlers.effecthandlers.instant.DispelByCategory;
import handlers.effecthandlers.instant.DispelBySlot;
import handlers.effecthandlers.instant.DispelBySlotProbability;
import handlers.effecthandlers.instant.EnergyAttack;
import handlers.effecthandlers.instant.Escape;
import handlers.effecthandlers.instant.FatalBlow;
import handlers.effecthandlers.instant.Fishing;
import handlers.effecthandlers.instant.FlySelf;
import handlers.effecthandlers.instant.FocusEnergy;
import handlers.effecthandlers.instant.FocusMaxEnergy;
import handlers.effecthandlers.instant.FocusSouls;
import handlers.effecthandlers.instant.FoodForPet;
import handlers.effecthandlers.instant.GetAgro;
import handlers.effecthandlers.instant.GiveRecommendation;
import handlers.effecthandlers.instant.GiveSp;
import handlers.effecthandlers.instant.Harvesting;
import handlers.effecthandlers.instant.HeadquarterCreate;
import handlers.effecthandlers.instant.Heal;
import handlers.effecthandlers.instant.Hp;
import handlers.effecthandlers.instant.HpByLevel;
import handlers.effecthandlers.instant.HpDrain;
import handlers.effecthandlers.instant.HpPerMax;
import handlers.effecthandlers.instant.Lethal;
import handlers.effecthandlers.instant.MagicalAttack;
import handlers.effecthandlers.instant.MagicalAttackByAbnormal;
import handlers.effecthandlers.instant.MagicalAttackMp;
import handlers.effecthandlers.instant.MagicalSoulAttack;
import handlers.effecthandlers.instant.ManaHealByLevel;
import handlers.effecthandlers.instant.Mp;
import handlers.effecthandlers.instant.MpPerMax;
import handlers.effecthandlers.instant.NevitsHourglass;
import handlers.effecthandlers.instant.OpenCommonRecipeBook;
import handlers.effecthandlers.instant.OpenDwarfRecipeBook;
import handlers.effecthandlers.instant.OutpostCreate;
import handlers.effecthandlers.instant.OutpostDestroy;
import handlers.effecthandlers.instant.PhysicalAttack;
import handlers.effecthandlers.instant.PhysicalAttackHpLink;
import handlers.effecthandlers.instant.PhysicalSoulAttack;
import handlers.effecthandlers.instant.Pumping;
import handlers.effecthandlers.instant.RandomizeHate;
import handlers.effecthandlers.instant.RebalanceHP;
import handlers.effecthandlers.instant.Reeling;
import handlers.effecthandlers.instant.RefuelAirship;
import handlers.effecthandlers.instant.Restoration;
import handlers.effecthandlers.instant.RestorationRandom;
import handlers.effecthandlers.instant.Resurrection;
import handlers.effecthandlers.instant.RunAway;
import handlers.effecthandlers.instant.SetSkill;
import handlers.effecthandlers.instant.SkillTurning;
import handlers.effecthandlers.instant.SoulBlow;
import handlers.effecthandlers.instant.Sow;
import handlers.effecthandlers.instant.Spoil;
import handlers.effecthandlers.instant.StaticDamage;
import handlers.effecthandlers.instant.StealAbnormal;
import handlers.effecthandlers.instant.Summon;
import handlers.effecthandlers.instant.SummonAgathion;
import handlers.effecthandlers.instant.SummonCubic;
import handlers.effecthandlers.instant.SummonNpc;
import handlers.effecthandlers.instant.SummonPet;
import handlers.effecthandlers.instant.SummonTrap;
import handlers.effecthandlers.instant.Sweeper;
import handlers.effecthandlers.instant.TakeCastle;
import handlers.effecthandlers.instant.TakeFort;
import handlers.effecthandlers.instant.TakeFortStart;
import handlers.effecthandlers.instant.TakeTerritoryFlag;
import handlers.effecthandlers.instant.TargetCancel;
import handlers.effecthandlers.instant.TargetMeProbability;
import handlers.effecthandlers.instant.Teleport;
import handlers.effecthandlers.instant.TeleportToTarget;
import handlers.effecthandlers.instant.TransferHate;
import handlers.effecthandlers.instant.TrapDetect;
import handlers.effecthandlers.instant.TrapRemove;
import handlers.effecthandlers.instant.Unsummon;
import handlers.effecthandlers.instant.UnsummonAgathion;
import handlers.effecthandlers.instant.VitalityPointUp;
import handlers.effecthandlers.pump.AttackTrait;
import handlers.effecthandlers.pump.Betray;
import handlers.effecthandlers.pump.BlockBuff;
import handlers.effecthandlers.pump.BlockBuffSlot;
import handlers.effecthandlers.pump.BlockChat;
import handlers.effecthandlers.pump.BlockDamage;
import handlers.effecthandlers.pump.BlockDebuff;
import handlers.effecthandlers.pump.BlockParty;
import handlers.effecthandlers.pump.BlockResurrection;
import handlers.effecthandlers.pump.ChangeFishingMastery;
import handlers.effecthandlers.pump.CrystalGradeModify;
import handlers.effecthandlers.pump.CubicMastery;
import handlers.effecthandlers.pump.DefenceTrait;
import handlers.effecthandlers.pump.Disarm;
import handlers.effecthandlers.pump.EnableCloak;
import handlers.effecthandlers.pump.Fear;
import handlers.effecthandlers.pump.Hide;
import handlers.effecthandlers.pump.Lucky;
import handlers.effecthandlers.pump.MaxCp;
import handlers.effecthandlers.pump.MaxHp;
import handlers.effecthandlers.pump.MaxMp;
import handlers.effecthandlers.pump.NoblesseBless;
import handlers.effecthandlers.pump.Passive;
import handlers.effecthandlers.pump.PhysicalAttackMute;
import handlers.effecthandlers.pump.PhysicalMute;
import handlers.effecthandlers.pump.ProtectionBlessing;
import handlers.effecthandlers.pump.ResistSkill;
import handlers.effecthandlers.pump.ResurrectionSpecial;
import handlers.effecthandlers.pump.ServitorShare;
import handlers.effecthandlers.pump.SingleTarget;
import handlers.effecthandlers.pump.SoulEating;
import handlers.effecthandlers.pump.TalismanSlot;
import handlers.effecthandlers.pump.TargetMe;
import handlers.effecthandlers.pump.TransferDamage;
import handlers.effecthandlers.pump.Transformation;
import handlers.effecthandlers.pump.TriggerSkillByAttack;
import handlers.effecthandlers.pump.TriggerSkillByAvoid;
import handlers.effecthandlers.pump.TriggerSkillByDamage;
import handlers.effecthandlers.pump.TriggerSkillBySkill;
import handlers.effecthandlers.ticks.TickHp;
import handlers.effecthandlers.ticks.TickHpFatal;
import handlers.effecthandlers.ticks.TickMp;

/**
 * Effect Master handler.
 * @author BiggBoss, Zoey76
 */
public final class EffectMasterHandler
{
	private static final Logger _log = Logger.getLogger(EffectMasterHandler.class.getName());
	
	private static final Class<?>[] EFFECTS =
	{
		AddHate.class,
		AttackTrait.class,
		Backstab.class,
		Betray.class,
		Blink.class,
		BlockAction.class,
		BlockBuff.class,
		BlockChat.class,
		BlockDamage.class,
		BlockDebuff.class,
		BlockParty.class,
		BlockBuffSlot.class,
		BlockResurrection.class,
		Bluff.class,
		Buff.class,
		CallParty.class,
		CallPc.class,
		CallSkill.class,
		ChangeFace.class,
		ChangeFishingMastery.class,
		ChangeHairColor.class,
		ChangeHairStyle.class,
		ClanGate.class,
		Confuse.class,
		ConsumeBody.class,
		ConsumeChameleonRest.class,
		ConsumeFakeDeath.class,
		ConsumeHp.class,
		ConsumeMp.class,
		ConsumeMpByLevel.class,
		ConsumeRest.class,
		ConvertItem.class,
		Cp.class,
		CrystalGradeModify.class,
		CubicMastery.class,
		DeathLink.class,
		Debuff.class,
		DefenceTrait.class,
		DeleteHate.class,
		DeleteHateOfMe.class,
		DetectHiddenObjects.class,
		Detection.class,
		Disarm.class,
		DispelAll.class,
		DispelByCategory.class,
		DispelBySlot.class,
		DispelBySlotProbability.class,
		EnableCloak.class,
		EnergyAttack.class,
		Escape.class,
		FatalBlow.class,
		Fear.class,
		Fishing.class,
		Flag.class,
		FlySelf.class,
		FocusEnergy.class,
		FocusMaxEnergy.class,
		FocusSouls.class,
		FoodForPet.class,
		GetAgro.class,
		GiveRecommendation.class,
		GiveSp.class,
		Grow.class,
		Harvesting.class,
		HeadquarterCreate.class,
		Heal.class,
		Hide.class,
		Hp.class,
		HpByLevel.class,
		HpDrain.class,
		HpPerMax.class,
		ImmobileBuff.class,
		ImmobilePetBuff.class,
		Lethal.class,
		Lucky.class,
		MagicalAttack.class,
		MagicalAttackByAbnormal.class,
		MagicalAttackMp.class,
		MagicalSoulAttack.class,
		ManaHealByLevel.class,
		MaxCp.class,
		MaxHp.class,
		MaxMp.class,
		Mp.class,
		MpPerMax.class,
		Mute.class,
		NevitsHourglass.class,
		NoblesseBless.class,
		OpenChest.class,
		Unsummon.class,
		OpenCommonRecipeBook.class,
		OpenDoor.class,
		OpenDwarfRecipeBook.class,
		OutpostCreate.class,
		OutpostDestroy.class,
		Paralyze.class,
		Passive.class,
		PhysicalAttack.class,
		PhysicalAttackHpLink.class,
		PhysicalAttackMute.class,
		PhysicalMute.class,
		PhysicalSoulAttack.class,
		Pumping.class,
		ProtectionBlessing.class,
		RandomizeHate.class,
		RebalanceHP.class,
		Recovery.class,
		Reeling.class,
		RefuelAirship.class,
		ResistSkill.class,
		Restoration.class,
		RestorationRandom.class,
		Resurrection.class,
		ResurrectionSpecial.class,
		Root.class,
		RunAway.class,
		ServitorShare.class,
		SetSkill.class,
		SilentMove.class,
		SingleTarget.class,
		SkillTurning.class,
		Sleep.class,
		SoulBlow.class,
		SoulEating.class,
		Sow.class,
		Spoil.class,
		StaticDamage.class,
		StealAbnormal.class,
		Stun.class,
		Summon.class,
		SummonAgathion.class,
		SummonCubic.class,
		SummonNpc.class,
		SummonPet.class,
		SummonTrap.class,
		Sweeper.class,
		TakeCastle.class,
		TakeFort.class,
		TakeFortStart.class,
		TakeTerritoryFlag.class,
		TalismanSlot.class,
		TargetCancel.class,
		TargetMe.class,
		TargetMeProbability.class,
		Teleport.class,
		TeleportToTarget.class,
		ThrowUp.class,
		TickHp.class,
		TickHpFatal.class,
		TickMp.class,
		TransferDamage.class,
		TransferHate.class,
		Transformation.class,
		TrapDetect.class,
		TrapRemove.class,
		TriggerSkillByAttack.class,
		TriggerSkillByAvoid.class,
		TriggerSkillByDamage.class,
		TriggerSkillBySkill.class,
		UnsummonAgathion.class,
		VitalityPointUp.class,
	};
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args)
	{
		for (Class<?> c : EFFECTS)
		{
			if (c == null)
			{
				continue; // Disabled handler
			}
			EffectHandler.getInstance().registerHandler((Class<? extends AbstractEffect>) c);
		}
		
		// And lets try get size
		try
		{
			_log.log(Level.INFO, EffectMasterHandler.class.getSimpleName() + ": Loaded " + EffectHandler.getInstance().size() + " effect handlers.");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Failed invoking size method for handler: " + EffectMasterHandler.class.getSimpleName(), e);
		}
	}
}
