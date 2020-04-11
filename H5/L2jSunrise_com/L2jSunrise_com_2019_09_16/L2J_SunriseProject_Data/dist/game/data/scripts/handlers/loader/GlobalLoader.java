package handlers.loader;

import gr.sr.handler.ABLoader;

import handlers.BloodAltarsLoader;
import handlers.ConquerableHallsLoader;
import handlers.CustomsLoader;
import handlers.EventsLoader;
import handlers.FeaturesLoader;
import handlers.GraciaLoader;
import handlers.GrandBossLoader;
import handlers.GroupTemplatesLoader;
import handlers.HellboundLoader;
import handlers.IndividualLoader;
import handlers.InstanceLoader;
import handlers.MasterHandler;
import handlers.ModifiersLoader;
import handlers.NpcLoader;
import handlers.QuestLoader;
import handlers.SunriseNpcsLoader;
import handlers.TeleportersLoader;
import handlers.VehiclesLoader;
import handlers.VillageMastersLoader;
import handlers.ZonesLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class GlobalLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BloodAltarsLoader.class,
		ConquerableHallsLoader.class,
		CustomsLoader.class,
		EventsLoader.class,
		FeaturesLoader.class,
		GraciaLoader.class,
		GrandBossLoader.class,
		GroupTemplatesLoader.class,
		IndividualLoader.class,
		HellboundLoader.class,
		InstanceLoader.class,
		MasterHandler.class,
		ModifiersLoader.class,
		NpcLoader.class,
		QuestLoader.class,
		SunriseNpcsLoader.class,
		TeleportersLoader.class,
		VehiclesLoader.class,
		VillageMastersLoader.class,
		ZonesLoader.class,
	};
	
	public GlobalLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
	
	public static void main(String[] args)
	{
		new GlobalLoader();
	}
}
