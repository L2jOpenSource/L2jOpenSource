package handlers;

import gr.sr.handler.ABLoader;

import ai.zone.DragonValley.BlackdaggerWing;
import ai.zone.DragonValley.BleedingFly;
import ai.zone.DragonValley.DragonValley;
import ai.zone.DragonValley.DrakosWarrior;
import ai.zone.DragonValley.DustRider;
import ai.zone.DragonValley.EmeraldHorn;
import ai.zone.DragonValley.MuscleBomber;
import ai.zone.DragonValley.NecromancerOfTheValley;
import ai.zone.DragonValley.ShadowSummoner;
import ai.zone.FantasyIsle.HandysBlockCheckerEvent;
import ai.zone.FantasyIsle.MC_Show;
import ai.zone.FantasyIsle.Parade;
import ai.zone.LairOfAntharas.LairOfAntharas;
import ai.zone.PavelRuins.PavelArchaic;
import ai.zone.PlainsOfLizardman.PlainsOfLizardman;
import ai.zone.PlainsOfLizardman.SeerFlouros;
import ai.zone.PlainsOfLizardman.SeerUgoros;
import ai.zone.PlainsOfLizardman.TantaLizardmanSummoner;
import ai.zone.PrimevalIsle.PrimevalIsle;
import ai.zone.SelMahums.SelMahumDrill;
import ai.zone.SelMahums.SelMahumSquad;
import ai.zone.StakatoNest.StakatoNest;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class ZonesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		// Dragon Valley
		BlackdaggerWing.class,
		BleedingFly.class,
		DragonValley.class,
		DrakosWarrior.class,
		DustRider.class,
		EmeraldHorn.class,
		MuscleBomber.class,
		NecromancerOfTheValley.class,
		ShadowSummoner.class,
		
		// Fantasy Island
		HandysBlockCheckerEvent.class,
		MC_Show.class,
		Parade.class,
		
		// Antharas Lair
		LairOfAntharas.class,
		
		// Pavel Ruins
		PavelArchaic.class,
		
		// Plains of Lizardman
		PlainsOfLizardman.class,
		SeerFlouros.class,
		SeerUgoros.class,
		TantaLizardmanSummoner.class,
		
		// Primeval Island
		PrimevalIsle.class,
		
		// Sel Mahums
		SelMahumDrill.class,
		SelMahumSquad.class,
		
		// Stakato Nest
		StakatoNest.class,
	};
	
	public ZonesLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
