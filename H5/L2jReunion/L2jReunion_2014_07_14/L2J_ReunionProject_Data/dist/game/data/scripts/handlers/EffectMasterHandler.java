/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import java.lang.reflect.Method;

import l2r.gameserver.handler.EffectHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handlers.effecthandlers.*;

/**
 * Effect Master handler.
 * @author BiggBoss
 */
public final class EffectMasterHandler
{
	private static Logger _log = LoggerFactory.getLogger(EffectMasterHandler.class);

	private static final Class<?> _loadInstances = EffectHandler.class;

	private static final Class<?>[] _effects =
	{
		AbortCast.class,
		RebalanceHP.class,
		Betray.class,
		BigHead.class,
		BlockAction.class,
		BlockChat.class,
		BlockParty.class,
		BlockResurrection.class,
		Bluff.class,
		Buff.class,
		CallParty.class,
		CallPc.class,
		Cancel.class,
		CancelAll.class,
		CancelDebuff.class,
		ChameleonRest.class,
		ChanceSkillTrigger.class,
		ChangeFace.class,
		ChangeHairColor.class,
		ChangeHairStyle.class,
		CharmOfCourage.class,
		CharmOfLuck.class,
		ClanGate.class,
		Confusion.class,
		ConsumeBody.class,
		ConvertItem.class,
		CpHeal.class,
		CpHealOverTime.class,
		CpHealPercent.class,
		CrystalGradeModify.class,
		CubicMastery.class,
		CpDamPercent.class,
		DamOverTime.class,
		DamOverTimePercent.class,
		Debuff.class,
		DispelBySlot.class,
		DispelOnlyOne.class,
		Disarm.class,
		EnemyCharge.class,
		EnlargeAbnormalSlot.class,
		FakeDeath.class,
		Flag.class,
		Fear.class,
		FocusSouls.class,
		Fusion.class,
		GetAgro.class,
		GiveSp.class,
		Grow.class,
		Harvesting.class,
		HeadquarterCreate.class,
		HealOverTime.class,
		HealPercent.class,
		Heal.class,
		Hide.class,
		HpByLevel.class,
		ImmobileBuff.class,
		IncreaseCharges.class,
		ImmobilePetBuff.class,
		Invincible.class,
		Lucky.class,
		ManaDamOverTime.class,
		ManaHeal.class,
		ManaHealByLevel.class,
		ManaHealOverTime.class,
		ManaHealPercent.class,
		MpConsumePerLevel.class,
		Mute.class,
		MySummonKill.class,
		Negate.class,
		NoblesseBless.class,
		OpenChest.class,
		OpenCommonRecipeBook.class,
		OpenDwarfRecipeBook.class,
		OutpostCreate.class,
		OutpostDestroy.class,
		Paralyze.class,
		Petrification.class,
		PhoenixBless.class,
		PhysicalAttackMute.class,
		PhysicalMute.class,
		ProtectionBlessing.class,
		RandomizeHate.class,
		RecoBonus.class,
		Recovery.class,
		RefuelAirship.class,
		Relax.class,
		ResistSkill.class,
		RemoveTarget.class,
		RestorationRandom.class,
		Root.class,
		ServitorShare.class,
		Signet.class,
		SignetAntiSummon.class,
		SignetMDam.class,
		SignetNoise.class,
		SilentMove.class,
		Sleep.class,
		SoulEating.class,
		Spoil.class,
		StealAbnormal.class,
		Stun.class,
		SummonAgathion.class,
		SummonCubic.class,
		SummonNpc.class,
		SummonPet.class,
		SummonTrap.class,
		ResistSkill.class,
		Sweeper.class,
		TakeCastle.class,
		TakeFort.class,
		TakeFortStart.class,
		TakeTerritoryFlag.class,
		TargetMe.class,
		ThrowUp.class,
		TransferDamage.class,
		Transformation.class,
		UnsummonAgathion.class,
		VitalityPointUp.class,
		Warp.class,
	};

	public static void main(String[] args)
	{
		Object loadInstance = null;
		Method method = null;

		try
		{
			method = _loadInstances.getMethod("getInstance");
			loadInstance = method.invoke(_loadInstances);
		}
		catch (Exception e)
		{
			_log.warn("Failed invoking getInstance method for handler: " + _loadInstances.getSimpleName(), e);
			return;
		}

		method = null; // Releasing variable for next method

		for (Class<?> c : _effects)
		{
			if (c == null)
			{
				continue; // Disabled handler
			}

			try
			{
				if (method == null)
				{
					method = loadInstance.getClass().getMethod("registerHandler", Class.class);
				}

				method.invoke(loadInstance, c);

			}
			catch (Exception e)
			{
				_log.warn("Failed loading effect handler: " + c.getSimpleName(), e);
				continue;
			}
		}

		// And lets try get size
		try
		{
			method = loadInstance.getClass().getMethod("size");
			Object returnVal = method.invoke(loadInstance);
			_log.info(loadInstance.getClass().getSimpleName() + ": Loaded " + returnVal + " Handlers");
		}
		catch (Exception e)
		{
			_log.warn("Failed invoking size method for handler: " + loadInstance.getClass().getSimpleName(), e);
		}
	}
}
