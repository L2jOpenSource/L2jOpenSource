package handlers;

import gr.sr.handler.ABLoader;

import ai.individual.Anais;
import ai.individual.Ballista;
import ai.individual.CrimsonHatuOtis;
import ai.individual.DarkWaterDragon;
import ai.individual.DivineBeast;
import ai.individual.DrChaos;
import ai.individual.Epidos;
import ai.individual.EvasGiftBox;
import ai.individual.FrightenedRagnaOrc;
import ai.individual.Gordon;
import ai.individual.QueenShyeed;
import ai.individual.SinEater;
import ai.individual.SinWardens;
import ai.individual.Venom.Venom;
import ai.individual.extra.Aenkinel;
import ai.individual.extra.Barakiel;
import ai.individual.extra.BladeOtis;
import ai.individual.extra.EtisEtina;
import ai.individual.extra.FollowerOfAllosce;
import ai.individual.extra.FollowerOfMontagnar;
import ai.individual.extra.Gargos;
import ai.individual.extra.Hellenark;
import ai.individual.extra.HolyBrazier;
import ai.individual.extra.KaimAbigore;
import ai.individual.extra.Kechi;
import ai.individual.extra.KelBilette;
import ai.individual.extra.OlAriosh;
import ai.individual.extra.SelfExplosiveKamikaze;
import ai.individual.extra.ValakasMinions;
import ai.individual.extra.VenomousStorace;
import ai.individual.extra.WeirdBunei;
import ai.individual.extra.WhiteAllosce;
import ai.individual.extra.ToiRaids.Golkonda;
import ai.individual.extra.ToiRaids.Hallate;
import ai.individual.extra.ToiRaids.Kernon;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class IndividualLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		Anais.class,
		Ballista.class,
		CrimsonHatuOtis.class,
		DarkWaterDragon.class,
		DivineBeast.class,
		DrChaos.class,
		Epidos.class,
		EvasGiftBox.class,
		FrightenedRagnaOrc.class,
		Gordon.class,
		QueenShyeed.class,
		SinEater.class,
		SinWardens.class,
		
		// Extras
		Aenkinel.class,
		Barakiel.class,
		BladeOtis.class,
		EtisEtina.class,
		FollowerOfAllosce.class,
		FollowerOfMontagnar.class,
		Gargos.class,
		Hellenark.class,
		HolyBrazier.class,
		KaimAbigore.class,
		Kechi.class,
		KelBilette.class,
		OlAriosh.class,
		SelfExplosiveKamikaze.class,
		ValakasMinions.class,
		VenomousStorace.class,
		WeirdBunei.class,
		WhiteAllosce.class,
		
		// Extra Toi Raids
		Golkonda.class,
		Hallate.class,
		Kernon.class,
		
		// Other
		Venom.class,
	};
	
	public IndividualLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
