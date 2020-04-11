package handlers;

import l2r.Config;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;

import gr.sr.handler.ABLoader;

import handlers.admincommandhandlers.AdminHellbound;
import handlers.voicedcommandhandlers.Hellbound;
import hellbound.HellboundEngine;
import hellbound.HellboundPointData;
import hellbound.HellboundSpawns;
import hellbound.AI.Amaskari;
import hellbound.AI.Chimeras;
import hellbound.AI.DemonPrince;
import hellbound.AI.HellboundCore;
import hellbound.AI.Keltas;
import hellbound.AI.NaiaLock;
import hellbound.AI.OutpostCaptain;
import hellbound.AI.Ranku;
import hellbound.AI.RuinsGhosts;
import hellbound.AI.Slaves;
import hellbound.AI.Typhoon;
import hellbound.AI.NPC.Bernarde.Bernarde;
import hellbound.AI.NPC.Budenka.Budenka;
import hellbound.AI.NPC.Buron.Buron;
import hellbound.AI.NPC.Deltuva.Deltuva;
import hellbound.AI.NPC.Falk.Falk;
import hellbound.AI.NPC.Galate.Galate;
import hellbound.AI.NPC.Hude.Hude;
import hellbound.AI.NPC.Jude.Jude;
import hellbound.AI.NPC.Kanaf.Kanaf;
import hellbound.AI.NPC.Kief.Kief;
import hellbound.AI.NPC.Natives.Natives;
import hellbound.AI.NPC.Quarry.Quarry;
import hellbound.AI.NPC.Shadai.Shadai;
import hellbound.AI.NPC.Solomon.Solomon;
import hellbound.AI.NPC.Warpgate.Warpgate;
import hellbound.AI.Zones.AnomicFoundry.AnomicFoundry;
import hellbound.AI.Zones.BaseTower.BaseTower;
import hellbound.AI.Zones.TowerOfInfinitum.TowerOfInfinitum;
import hellbound.AI.Zones.TowerOfNaia.TowerOfNaia;
import hellbound.AI.Zones.TullyWorkshop.TullyWorkshop;
import hellbound.Instances.DemonPrinceFloor.DemonPrinceFloor;
import hellbound.Instances.RankuFloor.RankuFloor;
import hellbound.Instances.UrbanArea.UrbanArea;
import quests.Q00130_PathToHellbound.Q00130_PathToHellbound;
import quests.Q00133_ThatsBloodyHot.Q00133_ThatsBloodyHot;

/**
 * Hellbound class-loader.
 * @author vGodFather
 */
public final class HellboundLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		// Commands
		AdminHellbound.class,
		Hellbound.class,
		// AIs
		Amaskari.class,
		Chimeras.class,
		DemonPrince.class,
		HellboundCore.class,
		Keltas.class,
		NaiaLock.class,
		OutpostCaptain.class,
		Ranku.class,
		RuinsGhosts.class,
		Slaves.class,
		Typhoon.class,
		// NPCs
		Bernarde.class,
		Budenka.class,
		Buron.class,
		Deltuva.class,
		Falk.class,
		Galate.class,
		Hude.class,
		Jude.class,
		Kanaf.class,
		Kief.class,
		Natives.class,
		Quarry.class,
		Shadai.class,
		Solomon.class,
		Warpgate.class,
		// Zones
		AnomicFoundry.class,
		BaseTower.class,
		TowerOfInfinitum.class,
		TowerOfNaia.class,
		TullyWorkshop.class,
		// Instances
		DemonPrinceFloor.class,
		UrbanArea.class,
		RankuFloor.class,
		// Quests
		Q00130_PathToHellbound.class,
		Q00133_ThatsBloodyHot.class,
	};
	
	@Override
	public void loadScripts()
	{
		final long startCache = System.currentTimeMillis();
		// Data
		HellboundPointData.getInstance();
		HellboundSpawns.getInstance();
		// Engine
		HellboundEngine.getInstance();
		for (Class<?> script : getScripts())
		{
			try
			{
				final Object instance = script.newInstance();
				if (instance instanceof IAdminCommandHandler)
				{
					AdminCommandHandler.getInstance().registerHandler((IAdminCommandHandler) instance);
				}
				else if (Config.L2JMOD_HELLBOUND_STATUS && (instance instanceof IVoicedCommandHandler))
				{
					VoicedCommandHandler.getInstance().registerHandler((IVoicedCommandHandler) instance);
				}
			}
			catch (Exception e)
			{
				_log.error(getClass().getSimpleName() + ": Failed loading " + script.getSimpleName() + ":" + e.getMessage());
			}
		}
		_log.info(getClass().getSimpleName() + " loaded. (GenTime: {} ms) ", (System.currentTimeMillis() - startCache));
	}
	
	public HellboundLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
