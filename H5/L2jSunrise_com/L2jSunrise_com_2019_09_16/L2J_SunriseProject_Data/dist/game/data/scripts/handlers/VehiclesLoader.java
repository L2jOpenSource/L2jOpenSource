package handlers;

import gr.sr.handler.ABLoader;

import vehicles.BoatGiranTalking;
import vehicles.BoatGludinRune;
import vehicles.BoatInnadrilTour;
import vehicles.BoatRunePrimeval;
import vehicles.BoatTalkingGludin;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class VehiclesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BoatGiranTalking.class,
		BoatGludinRune.class,
		BoatInnadrilTour.class,
		BoatRunePrimeval.class,
		BoatTalkingGludin.class
	};
	
	public VehiclesLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
