package l2f.gameserver.stats;

import l2f.gameserver.BalancerConfig;
import l2f.gameserver.model.Player;
import l2f.gameserver.templates.item.WeaponTemplate;
import l2f.gameserver.utils.Util;

public class DamageBalancer
{
	private static boolean isInOlympiad = false;

	public static double optimizer(Player attacker, Player target, double input_damage, boolean crit, boolean isMagicDamage)
	{
		if (attacker == null)
		{
			System.out.println("DamageBalancer: null attacker.");
			return 0;
		}
		double output_damage = input_damage;

		isInOlympiad = attacker.isInOlympiadMode();
		if (target != null)
		{
			if (BalancerConfig.DuelistDamageMapOly.containsKey(target.getClassId().getId()))
			{
				switch (attacker.getClassId().getId())
				{
					case 88:
						output_damage *= isInOlympiad ? BalancerConfig.DuelistDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.DuelistDamageMap.get(target.getClassId().getId());
						break;
					case 89:
						output_damage *= isInOlympiad ? BalancerConfig.DreadnoughtDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.DreadnoughtDamageMap.get(target.getClassId().getId());
						break;
					case 90:
						output_damage *= isInOlympiad ? BalancerConfig.PheonixKnightDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.PheonixKnightDamageMap.get(target.getClassId().getId());
						break;
					case 91:
						output_damage *= isInOlympiad ? BalancerConfig.HellKnightDamageMap.get(target.getClassId().getId()) : BalancerConfig.HellKnightDamageMapOly.get(target.getClassId().getId());
						break;
					case 92:
						output_damage *= isInOlympiad ? BalancerConfig.SagittariusDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.SagittariusDamageMap.get(target.getClassId().getId());
						break;
					case 93:
						output_damage *= isInOlympiad ? BalancerConfig.AdventurerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.AdventurerDamageMap.get(target.getClassId().getId());
						break;
					case 94:
						output_damage *= isInOlympiad ? BalancerConfig.ArchmageDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.ArchmageDamageMap.get(target.getClassId().getId());
						break;
					case 95:
						output_damage *= isInOlympiad ? BalancerConfig.SoultakerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.SoultakerDamageMap.get(target.getClassId().getId());
						break;
					case 96:
						output_damage *= isInOlympiad ? BalancerConfig.ArcanalordDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.ArcanalordDamageMap.get(target.getClassId().getId());
						break;
					case 97:
						output_damage *= isInOlympiad ? BalancerConfig.CardinalDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.CardinalDamageMap.get(target.getClassId().getId());
						break;
					case 98:
						output_damage *= isInOlympiad ? BalancerConfig.HierophantDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.HierophantDamageMap.get(target.getClassId().getId());
						break;
					case 99:
						output_damage *= isInOlympiad ? BalancerConfig.EvatemplarDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.EvatemplarDamageMap.get(target.getClassId().getId());
						break;
					case 100:
						output_damage *= isInOlympiad ? BalancerConfig.SwordmuseDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.SwordmuseDamageMap.get(target.getClassId().getId());
						break;
					case 101:
						output_damage *= isInOlympiad ? BalancerConfig.WindriderDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.WindriderDamageMap.get(target.getClassId().getId());
						break;
					case 102:
						output_damage *= isInOlympiad ? BalancerConfig.MoonlightDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.MoonlightDamageMap.get(target.getClassId().getId());
						break;
					case 103:
						output_damage *= isInOlympiad ? BalancerConfig.MysticmuseDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.MysticmuseDamageMap.get(target.getClassId().getId());
						break;
					case 104:
						output_damage *= isInOlympiad ? BalancerConfig.ElementalMasterDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.ElementalMasterDamageMap.get(target.getClassId().getId());
						break;
					case 105:
						output_damage *= isInOlympiad ? BalancerConfig.EvasaintMasterDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.EvasaintMasterDamageMap.get(target.getClassId().getId());
						break;
					case 106:
						output_damage *= isInOlympiad ? BalancerConfig.ShillienTemplarDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.ShillienTemplarDamageMap.get(target.getClassId().getId());
						break;
					case 107:
						output_damage *= isInOlympiad ? BalancerConfig.SpectralDancerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.SpectralDancerDamageMap.get(target.getClassId().getId());
						break;
					case 108:
						output_damage *= isInOlympiad ? BalancerConfig.GhostHunterDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.GhostHunterDamageMap.get(target.getClassId().getId());
						break;
					case 109:
						output_damage *= isInOlympiad ? BalancerConfig.GhostSentinelDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.GhostSentinelDamageMap.get(target.getClassId().getId());
						break;
					case 110:
						output_damage *= isInOlympiad ? BalancerConfig.StormscreamerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.StormscreamerDamageMap.get(target.getClassId().getId());
						break;
					case 111:
						output_damage *= isInOlympiad ? BalancerConfig.SpectralMasterDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.SpectralMasterDamageMap.get(target.getClassId().getId());
						break;
					case 112:
						output_damage *= isInOlympiad ? BalancerConfig.ShillenSaintDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.ShillenSaintDamageMap.get(target.getClassId().getId());
						break;
					case 113:
						output_damage *= isInOlympiad ? BalancerConfig.TitanDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.TitanDamageMap.get(target.getClassId().getId());
						break;
					case 114:
						output_damage *= isInOlympiad ? BalancerConfig.GrandKhauatariDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.GrandKhauatariDamageMap.get(target.getClassId().getId());
						break;
					case 115:
						output_damage *= isInOlympiad ? BalancerConfig.DominatorDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.DominatorDamageMap.get(target.getClassId().getId());
						break;
					case 116:
						output_damage *= isInOlympiad ? BalancerConfig.DoomcryerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.DoomcryerDamageMap.get(target.getClassId().getId());
						break;
					case 117:
						output_damage *= isInOlympiad ? BalancerConfig.FortuneSeekerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.FortuneSeekerDamageMap.get(target.getClassId().getId());
						break;
					case 118:
						output_damage *= isInOlympiad ? BalancerConfig.MaestroDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.MaestroDamageMap.get(target.getClassId().getId());
						break;
					case 131:
						output_damage *= isInOlympiad ? BalancerConfig.DoombringerDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.DoombringerDamageMap.get(target.getClassId().getId());
						break;
					case 132:
						output_damage *= isInOlympiad ? BalancerConfig.MaleSoulhoundDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.MaleSoulhoundDamageMap.get(target.getClassId().getId());
						break;
					case 133:
						output_damage *= isInOlympiad ? BalancerConfig.FemaleSoulhoundDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.FemaleSoulhoundDamageMap.get(target.getClassId().getId());
						break;
					case 134:
						output_damage *= isInOlympiad ? BalancerConfig.TricksterDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.TricksterDamageMap.get(target.getClassId().getId());
						break;
					case 136:
						output_damage *= isInOlympiad ? BalancerConfig.JudicatorDamageMapOly.get(target.getClassId().getId()) : BalancerConfig.JudicatorDamageMap.get(target.getClassId().getId());
						break;
				}
			}
		} else
		{
			System.out.println(attacker.getName() + ": null target.");
		}

		if (!isInOlympiad)
			output_damage *= generaloptimizedamage(output_damage, crit, isMagicDamage);

		if (isMagicDamage)
		{
			if (!crit)
				output_damage *= BalancerConfig.MAGE_DAMAGE;
		} else
		{
			if (attacker.getActiveWeaponItem() != null && attacker.getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW)
				output_damage *= BalancerConfig.BOW_DAMAGE_BOOST;

			if (attacker.isInOlympiadMode() && target.isInOlympiadMode() && target.isMageClass())
				output_damage *= BalancerConfig.FIGHTER_DAMAGE_VS_MAGE_OLYMPIAD;

			if (output_damage > 0 && output_damage < 1)
				output_damage = 1;
			else if (output_damage < 0)
				output_damage = 1;
		}

		switch (attacker.getClassId().getId())
		{
			case 88:
				if (output_damage > BalancerConfig.DUELIST_DAMAGE_LIMIT)
					output_damage = BalancerConfig.DUELIST_DAMAGE_LIMIT;

				break;
			case 89:
				if (output_damage > BalancerConfig.DREADNOUGHT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.DREADNOUGHT_DAMAGE_LIMIT;

				break;
			case 90:
				if (output_damage > BalancerConfig.PHEONIXKNIGHT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.PHEONIXKNIGHT_DAMAGE_LIMIT;

				break;
			case 91:
				if (output_damage > BalancerConfig.HELLKNIGHT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.HELLKNIGHT_DAMAGE_LIMIT;

				break;
			case 92:
				if (output_damage > BalancerConfig.SAGITTARIUS_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SAGITTARIUS_DAMAGE_LIMIT;

				break;
			case 93:
				if (output_damage > BalancerConfig.ADVENTURER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.ADVENTURER_DAMAGE_LIMIT;

				break;
			case 94:
				if (output_damage > BalancerConfig.ARCHMAGE_DAMAGE_LIMIT)
					output_damage = BalancerConfig.ARCHMAGE_DAMAGE_LIMIT;

				break;
			case 95:
				if (output_damage > BalancerConfig.SOULTAKER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SOULTAKER_DAMAGE_LIMIT;

				break;
			case 96:
				if (output_damage > BalancerConfig.ARCANALORD_DAMAGE_LIMIT)
					output_damage = BalancerConfig.ARCANALORD_DAMAGE_LIMIT;

				break;
			case 97:
				if (output_damage > BalancerConfig.CARDINAL_DAMAGE_LIMIT)
					output_damage = BalancerConfig.CARDINAL_DAMAGE_LIMIT;

				break;
			case 98:
				if (output_damage > BalancerConfig.HIEROPHANT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.HIEROPHANT_DAMAGE_LIMIT;

				break;
			case 99:
				if (output_damage > BalancerConfig.EVATEMPLAR_DAMAGE_LIMIT)
					output_damage = BalancerConfig.EVATEMPLAR_DAMAGE_LIMIT;

				break;
			case 100:
				if (output_damage > BalancerConfig.SWORDMUSE_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SWORDMUSE_DAMAGE_LIMIT;

				break;
			case 101:
				if (output_damage > BalancerConfig.WINDRIDER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.WINDRIDER_DAMAGE_LIMIT;

				break;
			case 102:
				if (output_damage > BalancerConfig.MOONLIGHTSENTINEL_DAMAGE_LIMIT)
					output_damage = BalancerConfig.MOONLIGHTSENTINEL_DAMAGE_LIMIT;

				break;
			case 103:
				if (output_damage > BalancerConfig.MYSTICMUSE_DAMAGE_LIMIT)
					output_damage = BalancerConfig.MYSTICMUSE_DAMAGE_LIMIT;

				break;
			case 104:
				if (output_damage > BalancerConfig.ELEMENTALMASTER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.ELEMENTALMASTER_DAMAGE_LIMIT;

				break;
			case 105:
				if (output_damage > BalancerConfig.EVASAINT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.EVASAINT_DAMAGE_LIMIT;

				break;
			case 106:
				if (output_damage > BalancerConfig.SHILLIENTEMPLAR_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SHILLIENTEMPLAR_DAMAGE_LIMIT;

				break;
			case 107:
				if (output_damage > BalancerConfig.SPECTRALDANCER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SPECTRALDANCER_DAMAGE_LIMIT;

				break;
			case 108:
				if (output_damage > BalancerConfig.GHOSTHUNTER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.GHOSTHUNTER_DAMAGE_LIMIT;

				break;
			case 109:
				if (output_damage > BalancerConfig.GHOSTSENTINEL_DAMAGE_LIMIT)
					output_damage = BalancerConfig.GHOSTSENTINEL_DAMAGE_LIMIT;
				break;
			case 110:
				if (output_damage > BalancerConfig.STORMSCREAMER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.STORMSCREAMER_DAMAGE_LIMIT;
				break;
			case 111:
				if (output_damage > BalancerConfig.SPECTRALMASTER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SPECTRALMASTER_DAMAGE_LIMIT;
				break;
			case 112:
				if (output_damage > BalancerConfig.SHILLENSAINT_DAMAGE_LIMIT)
					output_damage = BalancerConfig.SHILLENSAINT_DAMAGE_LIMIT;
				break;
			case 113:
				if (output_damage > BalancerConfig.TITAN_DAMAGE_LIMIT)
					output_damage = BalancerConfig.TITAN_DAMAGE_LIMIT;
				break;
			case 114:
				if (output_damage > BalancerConfig.GRANDKHAUATARI_DAMAGE_LIMIT)
					output_damage = BalancerConfig.GRANDKHAUATARI_DAMAGE_LIMIT;
				break;
			case 115:
				if (output_damage > BalancerConfig.DOMINATOR_DAMAGE_LIMIT)
					output_damage = BalancerConfig.DOMINATOR_DAMAGE_LIMIT;
				break;
			case 116:
				if (output_damage > BalancerConfig.DOOMCRYER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.DOOMCRYER_DAMAGE_LIMIT;
				break;
			case 117:
				if (output_damage > BalancerConfig.FORTUNESEEKER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.FORTUNESEEKER_DAMAGE_LIMIT;
				break;
			case 118:
				if (output_damage > BalancerConfig.MAESTRO_DAMAGE_LIMIT)
					output_damage = BalancerConfig.MAESTRO_DAMAGE_LIMIT;
				break;
			case 131:
				if (output_damage > BalancerConfig.DOOMBRINGER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.DOOMBRINGER_DAMAGE_LIMIT;
				break;
			case 132:
				if (output_damage > BalancerConfig.MALESOULHOUND_DAMAGE_LIMIT)
					output_damage = BalancerConfig.MALESOULHOUND_DAMAGE_LIMIT;
				break;
			case 133:
				if (output_damage > BalancerConfig.FEMALESOULHOUND_DAMAGE_LIMIT)
					output_damage = BalancerConfig.FEMALESOULHOUND_DAMAGE_LIMIT;
				break;
			case 134:
				if (output_damage > BalancerConfig.TRICKSTER_DAMAGE_LIMIT)
					output_damage = BalancerConfig.TRICKSTER_DAMAGE_LIMIT;
				break;
			case 136:
				if (output_damage > BalancerConfig.JUDICATOR_DAMAGE_LIMIT)
					output_damage = BalancerConfig.JUDICATOR_DAMAGE_LIMIT;
				break;
		}

		if (BalancerConfig.DEBUG)
			debugging(attacker, target, input_damage, output_damage, crit, isMagicDamage, isInOlympiad);

		return output_damage;
	}

	private static double generaloptimizedamage(double IncomingDamage, boolean Crit, boolean mage)
	{
		double damagerate = 1;

		if (BalancerConfig.DEBUG)
			System.out.println("Magic: " + mage);

		if (mage)
		{
			if (Crit)
			{
				if (IncomingDamage > 2500)
					damagerate = 0.92;
			} else
			{
				if (IncomingDamage >= 1 && IncomingDamage <= 400)
					damagerate = 1.11;
				else if (IncomingDamage >= 401 && IncomingDamage <= 600)
				{
					if (IncomingDamage <= 500)
						damagerate = 1.13;
					else
						damagerate = 1.11;
				} else if (IncomingDamage >= 601 && IncomingDamage <= 800)
				{
					if (IncomingDamage <= 700)
						damagerate = 1.09;
					else
						damagerate = 1.05;
				} else if (IncomingDamage >= 801 && IncomingDamage <= 1000)
				{
					if (IncomingDamage <= 900)
						damagerate = 1.04;
					else
						damagerate = 1.03;
				} else if (IncomingDamage > 1000)
				{
					damagerate = 1.02;
				}
			}
		} else
		{
			if (Crit)
			{
				if (IncomingDamage > 2800)
					damagerate = 0.92;
			} else
			{
				if (IncomingDamage >= 1 && IncomingDamage <= 400)
					damagerate = 1.13;
				else if (IncomingDamage >= 401 && IncomingDamage <= 600)
				{
					if (IncomingDamage <= 500)
						damagerate = 1.12;
					else
						damagerate = 1.10;
				} else if (IncomingDamage >= 601 && IncomingDamage <= 800)
				{
					if (IncomingDamage <= 700)
						damagerate = 1.10;
					else
						damagerate = 1.08;
				} else if (IncomingDamage >= 801 && IncomingDamage <= 1000)
				{
					if (IncomingDamage <= 900)
						damagerate = 1.07;
					else
						damagerate = 1.05;
				} else if (IncomingDamage >= 1000 && IncomingDamage <= 1200)
				{
					if (IncomingDamage <= 1100)
						damagerate = 1.05;
					else
						damagerate = 1.03;
				} else if (IncomingDamage >= 1200 && IncomingDamage <= 1400)
				{
					if (IncomingDamage <= 1300)
						damagerate = 1.03;
					else
						damagerate = 1.01;
				} else if (IncomingDamage >= 1401 && IncomingDamage <= 1800)
					damagerate = 0.96;
				else if (IncomingDamage >= 1801)
					damagerate = 0.93;
			}
		}

		return damagerate;
	}

	private static void debugging(Player attacker, Player target, double IncomingDamage, double outcomingDamage, boolean Crit, boolean isMagicDamage, boolean isInOlympiad)
	{
		double damagerate = outcomingDamage / IncomingDamage;

		System.out.println("	Attacker: " + attacker.getName());
		System.out.println("	Target: " + target.getName());
		System.out.println("	Class attacker: " + Util.getFullClassName(attacker.getClassId().getId()));
		System.out.println("	Class Target: " + Util.getFullClassName(target.getClassId().getId()));
		System.out.println("	Damage incoming: " + IncomingDamage);
		System.out.println("	Damageoutcoming: " + outcomingDamage);
		System.out.println("	Damagerate: " + damagerate);
		System.out.println("	Critical: " + Crit);
		System.out.println("	MagicDamage: " + isMagicDamage);
		System.out.println("	isInOlympiad: " + isInOlympiad);
	}
}