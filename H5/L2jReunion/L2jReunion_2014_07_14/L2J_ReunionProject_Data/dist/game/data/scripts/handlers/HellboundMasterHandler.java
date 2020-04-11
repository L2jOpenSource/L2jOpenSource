/*
 * Copyright (C) 2004-2014 L2J DataPack
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

import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.handler.AdminCommandHandler;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.handler.VoicedCommandHandler;
import l2r.gameserver.scripts.handlers.admincommandhandlers.AdminHellbound;
import l2r.gameserver.scripts.handlers.voicedcommandhandlers.Hellbound;
import l2r.gameserver.scripts.hellbound.HellboundEngine;
import l2r.gameserver.scripts.hellbound.HellboundPointData;
import l2r.gameserver.scripts.hellbound.HellboundSpawns;
import l2r.gameserver.scripts.hellbound.AI.Amaskari;
import l2r.gameserver.scripts.hellbound.AI.Chimeras;
import l2r.gameserver.scripts.hellbound.AI.DemonPrince;
import l2r.gameserver.scripts.hellbound.AI.HellboundCore;
import l2r.gameserver.scripts.hellbound.AI.Keltas;
import l2r.gameserver.scripts.hellbound.AI.NaiaLock;
import l2r.gameserver.scripts.hellbound.AI.OutpostCaptain;
import l2r.gameserver.scripts.hellbound.AI.Ranku;
import l2r.gameserver.scripts.hellbound.AI.Slaves;
import l2r.gameserver.scripts.hellbound.AI.Typhoon;
import l2r.gameserver.scripts.hellbound.AI.NPC.Bernarde;
import l2r.gameserver.scripts.hellbound.AI.NPC.Budenka;
import l2r.gameserver.scripts.hellbound.AI.NPC.Buron;
import l2r.gameserver.scripts.hellbound.AI.NPC.Deltuva;
import l2r.gameserver.scripts.hellbound.AI.NPC.Falk;
import l2r.gameserver.scripts.hellbound.AI.NPC.Hude;
import l2r.gameserver.scripts.hellbound.AI.NPC.Jude;
import l2r.gameserver.scripts.hellbound.AI.NPC.Kanaf;
import l2r.gameserver.scripts.hellbound.AI.NPC.Kief;
import l2r.gameserver.scripts.hellbound.AI.NPC.Natives;
import l2r.gameserver.scripts.hellbound.AI.NPC.Quarry;
import l2r.gameserver.scripts.hellbound.AI.NPC.Shadai;
import l2r.gameserver.scripts.hellbound.AI.NPC.Solomon;
import l2r.gameserver.scripts.hellbound.AI.NPC.Warpgate;
import l2r.gameserver.scripts.hellbound.AI.Zones.AnomicFoundry;
import l2r.gameserver.scripts.hellbound.AI.Zones.BaseTower;
import l2r.gameserver.scripts.hellbound.AI.Zones.TowerOfInfinitum;
import l2r.gameserver.scripts.hellbound.AI.Zones.TowerOfNaia;
import l2r.gameserver.scripts.hellbound.AI.Zones.TullyWorkshop;
import l2r.gameserver.scripts.hellbound.Instances.DemonPrinceFloor;
import l2r.gameserver.scripts.hellbound.Instances.RankuFloor;
import l2r.gameserver.scripts.hellbound.Instances.UrbanArea;
import l2r.gameserver.scripts.quests.Q00130_PathToHellbound;
import l2r.gameserver.scripts.quests.Q00133_ThatsBloodyHot;

/**
 * Hellbound class-loader.
 * @author Zoey76
 */
public final class HellboundMasterHandler
{
	private static final Logger _log = Logger.getLogger(HellboundMasterHandler.class.getName());
	
	private static final Class<?>[] SCRIPTS =
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
		Slaves.class,
		Typhoon.class,
		// NPCs
		Bernarde.class,
		Budenka.class,
		Buron.class,
		Deltuva.class,
		Falk.class,
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
	
	public static void main(String[] args)
	{
		_log.info(HellboundMasterHandler.class.getSimpleName() + ": Loading Hellbound related scripts:");
		// Data
		HellboundPointData.getInstance();
		HellboundSpawns.getInstance();
		// Engine
		HellboundEngine.getInstance();
		for (Class<?> script : SCRIPTS)
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
				_log.severe(HellboundMasterHandler.class.getSimpleName() + ": Failed loading " + script.getSimpleName() + ":" + e.getMessage());
			}
		}
	}
}
