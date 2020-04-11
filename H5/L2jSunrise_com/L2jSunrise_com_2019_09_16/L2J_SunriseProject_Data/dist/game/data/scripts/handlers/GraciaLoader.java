package handlers;

import gr.sr.handler.ABLoader;

import gracia.AI.EnergySeeds;
import gracia.AI.Lindvior;
import gracia.AI.Maguen;
import gracia.AI.StarStones;
import gracia.AI.NPC.FortuneTelling.FortuneTelling;
import gracia.AI.NPC.GeneralDilios.GeneralDilios;
import gracia.AI.NPC.Lekon.Lekon;
import gracia.AI.NPC.Nemo.Nemo;
import gracia.AI.NPC.Nottingale.Nottingale;
import gracia.AI.NPC.Seyo.Seyo;
import gracia.AI.NPC.ZealotOfShilen.ZealotOfShilen;
import gracia.AI.SeedOfAnnihilation.SeedOfAnnihilation;
import gracia.instances.HallOfErosionAttack.HallOfErosionAttack;
import gracia.instances.HallOfErosionDefence.HallOfErosionDefence;
import gracia.instances.HallOfSufferingAttack.HallOfSufferingAttack;
import gracia.instances.HallOfSufferingDefence.HallOfSufferingDefence;
import gracia.instances.HeartInfinityAttack.HeartInfinityAttack;
import gracia.instances.HeartInfinityDefence.HeartInfinityDefence;
import gracia.instances.SecretArea.SecretArea;
import gracia.instances.SeedOfDestruction.SeedOfDestruction;
import gracia.vehicles.AirShipGludioGracia.AirShipGludioGracia;
import gracia.vehicles.KeucereusNorthController.KeucereusNorthController;
import gracia.vehicles.KeucereusSouthController.KeucereusSouthController;
import gracia.vehicles.SoDController.SoDController;
import gracia.vehicles.SoIController.SoIController;

/**
 * Gracia class-loader.
 * @author Pandragon
 */
public final class GraciaLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		// AIs
		EnergySeeds.class,
		Lindvior.class,
		Maguen.class,
		StarStones.class,
		// NPCs
		FortuneTelling.class,
		GeneralDilios.class,
		Lekon.class,
		Nemo.class,
		Nottingale.class,
		Seyo.class,
		ZealotOfShilen.class,
		// Seed of Annihilation
		SeedOfAnnihilation.class,
		// Instances
		HallOfErosionAttack.class,
		HallOfErosionDefence.class,
		HallOfSufferingAttack.class,
		HallOfSufferingDefence.class,
		HeartInfinityAttack.class,
		HeartInfinityDefence.class,
		SecretArea.class,
		SeedOfDestruction.class,
		// Vehicles
		AirShipGludioGracia.class,
		KeucereusNorthController.class,
		KeucereusSouthController.class,
		SoIController.class,
		SoDController.class,
	};
	
	public GraciaLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
