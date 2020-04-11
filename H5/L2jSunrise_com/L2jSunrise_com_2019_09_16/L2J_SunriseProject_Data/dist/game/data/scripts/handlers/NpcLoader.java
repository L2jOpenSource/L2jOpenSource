package handlers;

import gr.sr.handler.ABLoader;

import ai.npc.Abercrombie.Abercrombie;
import ai.npc.Alarm.Alarm;
import ai.npc.Alexandria.Alexandria;
import ai.npc.ArenaManager.ArenaManager;
import ai.npc.Asamah.Asamah;
import ai.npc.AvantGarde.AvantGarde;
import ai.npc.BlackJudge.BlackJudge;
import ai.npc.BlackMarketeerOfMammon.BlackMarketeerOfMammon;
import ai.npc.CastleAmbassador.CastleAmbassador;
import ai.npc.CastleBlacksmith.CastleBlacksmith;
import ai.npc.CastleChamberlain.CastleChamberlain;
import ai.npc.CastleCourtMagician.CastleCourtMagician;
import ai.npc.CastleMercenaryManager.CastleMercenaryManager;
import ai.npc.CastleSiegeManager.CastleSiegeManager;
import ai.npc.CastleTeleporter.CastleTeleporter;
import ai.npc.CastleWarehouse.CastleWarehouse;
import ai.npc.ClanTrader.ClanTrader;
import ai.npc.DimensionalMerchant.DimensionalMerchant;
import ai.npc.Dorian.Dorian;
import ai.npc.DragonVortexRetail.DragonVortexRetail;
import ai.npc.EkimusMouth.EkimusMouth;
import ai.npc.FameManager.FameManager;
import ai.npc.Fisherman.Fisherman;
import ai.npc.ForgeOfTheGods.ForgeOfTheGods;
import ai.npc.ForgeOfTheGods.Rooney;
import ai.npc.ForgeOfTheGods.TarBeetle;
import ai.npc.FortressArcherCaptain.FortressArcherCaptain;
import ai.npc.FortressSiegeManager.FortressSiegeManager;
import ai.npc.FreyasSteward.FreyasSteward;
import ai.npc.Jinia.Jinia;
import ai.npc.Katenar.Katenar;
import ai.npc.KetraOrcSupport.KetraOrcSupport;
import ai.npc.ManorManager.ManorManager;
import ai.npc.MercenaryCaptain.MercenaryCaptain;
import ai.npc.Minigame.Minigame;
import ai.npc.MonumentOfHeroes.MonumentOfHeroes;
import ai.npc.NevitsHerald.NevitsHerald;
import ai.npc.NpcBuffers.NpcBuffers;
import ai.npc.NpcBuffers.impl.CabaleBuffer;
import ai.npc.PcBangPoint.PcBangPoint;
import ai.npc.PriestOfBlessing.PriestOfBlessing;
import ai.npc.Rafforty.Rafforty;
import ai.npc.Rignos.Rignos;
import ai.npc.Selina.Selina;
import ai.npc.Sirra.Sirra;
import ai.npc.Summons.MerchantGolem.GolemTrader;
import ai.npc.SupportUnitCaptain.SupportUnitCaptain;
import ai.npc.SymbolMaker.SymbolMaker;
import ai.npc.TerritoryManagers.TerritoryManagers;
import ai.npc.TownPets.TownPets;
import ai.npc.Trainers.HealerTrainer.HealerTrainer;
import ai.npc.Tunatun.Tunatun;
import ai.npc.VarkaSilenosSupport.VarkaSilenosSupport;
import ai.npc.WeaverOlf.WeaverOlf;
import ai.npc.WyvernManager.WyvernManager;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class NpcLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		Abercrombie.class,
		Alarm.class,
		Alexandria.class,
		ArenaManager.class,
		Asamah.class,
		AvantGarde.class,
		BlackJudge.class,
		BlackMarketeerOfMammon.class,
		CastleAmbassador.class,
		CastleBlacksmith.class,
		CastleChamberlain.class,
		CastleCourtMagician.class,
		CastleMercenaryManager.class,
		CastleSiegeManager.class,
		CastleTeleporter.class,
		CastleWarehouse.class,
		ClanTrader.class,
		DimensionalMerchant.class,
		Dorian.class,
		// DragonVortex.class,
		DragonVortexRetail.class,
		EkimusMouth.class,
		FameManager.class,
		Fisherman.class,
		ForgeOfTheGods.class,
		Rooney.class,
		TarBeetle.class,
		FortressArcherCaptain.class,
		FortressSiegeManager.class,
		FreyasSteward.class,
		Jinia.class,
		Katenar.class,
		KetraOrcSupport.class,
		ManorManager.class,
		MercenaryCaptain.class,
		Minigame.class,
		MonumentOfHeroes.class,
		NevitsHerald.class,
		NpcBuffers.class,
		CabaleBuffer.class,
		PcBangPoint.class,
		PriestOfBlessing.class,
		Rafforty.class,
		Rignos.class,
		Selina.class,
		Sirra.class,
		GolemTrader.class,
		SupportUnitCaptain.class,
		SymbolMaker.class,
		TerritoryManagers.class,
		TownPets.class,
		HealerTrainer.class,
		Tunatun.class,
		VarkaSilenosSupport.class,
		WeaverOlf.class,
		WyvernManager.class,
	};
	
	public NpcLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
