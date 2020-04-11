package handlers;

import gr.sr.handler.ABLoader;

import instances.CavernOfThePirateCaptain.CavernOfThePirateCaptain;
import instances.ChambersOfDelusion.ChamberOfDelusionEast;
import instances.ChambersOfDelusion.ChamberOfDelusionNorth;
import instances.ChambersOfDelusion.ChamberOfDelusionSouth;
import instances.ChambersOfDelusion.ChamberOfDelusionSquare;
import instances.ChambersOfDelusion.ChamberOfDelusionTower;
import instances.ChambersOfDelusion.ChamberOfDelusionWest;
import instances.CrystalCaverns.CrystalCaverns;
import instances.DarkCloudMansion.DarkCloudMansion;
import instances.DisciplesNecropolisPast.DisciplesNecropolisPast;
import instances.ElcadiaTent.ElcadiaTent;
import instances.FinalEmperialTomb.FinalEmperialTomb;
import instances.HideoutOfTheDawn.HideoutOfTheDawn;
import instances.IceQueensCastle.IceQueensCastle;
import instances.IceQueensCastleNormalBattle.IceQueensCastleNormalBattle;
import instances.IceQueensCastleUltimateBattle.IceQueensCastleUltimateBattle;
import instances.JiniaGuildHideout1.JiniaGuildHideout1;
import instances.JiniaGuildHideout2.JiniaGuildHideout2;
import instances.JiniaGuildHideout3.JiniaGuildHideout3;
import instances.JiniaGuildHideout4.JiniaGuildHideout4;
import instances.Kamaloka.Kamaloka;
import instances.LibraryOfSages.LibraryOfSages;
import instances.MithrilMine.MithrilMine;
import instances.NornilsGarden.NornilsGarden;
import instances.NornilsGardenQuest.NornilsGardenQuest;
import instances.PailakaDevilsLegacy.PailakaDevilsLegacy;
import instances.PailakaInjuredDragon.PailakaInjuredDragon;
import instances.PailakaSongOfIceAndFire.PailakaSongOfIceAndFire;
import instances.RimKamaloka.RimKamaloka;
import instances.SanctumOftheLordsOfDawn.SanctumOftheLordsOfDawn;
import instances.SecretAreaKeucereus.SecretAreaKeucereus;
import instances.ToTheMonastery.ToTheMonastery;
import instances.Zaken.Zaken;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class InstanceLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		CavernOfThePirateCaptain.class,
		ChamberOfDelusionEast.class,
		ChamberOfDelusionNorth.class,
		ChamberOfDelusionSouth.class,
		ChamberOfDelusionSquare.class,
		ChamberOfDelusionTower.class,
		ChamberOfDelusionWest.class,
		CrystalCaverns.class,
		DarkCloudMansion.class,
		DisciplesNecropolisPast.class,
		ElcadiaTent.class,
		FinalEmperialTomb.class,
		HideoutOfTheDawn.class,
		IceQueensCastle.class,
		IceQueensCastleNormalBattle.class,
		IceQueensCastleUltimateBattle.class,
		JiniaGuildHideout1.class,
		JiniaGuildHideout2.class,
		JiniaGuildHideout3.class,
		JiniaGuildHideout4.class,
		Kamaloka.class,
		LibraryOfSages.class,
		MithrilMine.class,
		NornilsGarden.class,
		NornilsGardenQuest.class,
		PailakaDevilsLegacy.class,
		PailakaInjuredDragon.class,
		PailakaSongOfIceAndFire.class,
		RimKamaloka.class,
		SanctumOftheLordsOfDawn.class,
		SecretAreaKeucereus.class,
		ToTheMonastery.class,
		Zaken.class,
	};
	
	public InstanceLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
