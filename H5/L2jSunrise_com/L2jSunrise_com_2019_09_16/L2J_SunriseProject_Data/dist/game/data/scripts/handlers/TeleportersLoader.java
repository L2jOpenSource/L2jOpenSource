package handlers;

import gr.sr.handler.ABLoader;

import ai.npc.Teleports.CrumaTower.CrumaTower;
import ai.npc.Teleports.DelusionTeleport.DelusionTeleport;
import ai.npc.Teleports.ElrokiTeleporters.ElrokiTeleporters;
import ai.npc.Teleports.GatekeeperSpirit.GatekeeperSpirit;
import ai.npc.Teleports.GhostChamberlainOfElmoreden.GhostChamberlainOfElmoreden;
import ai.npc.Teleports.HuntingGroundsTeleport.HuntingGroundsTeleport;
import ai.npc.Teleports.Klein.Klein;
import ai.npc.Teleports.Klemis.Klemis;
import ai.npc.Teleports.MithrilMinesTeleporter.MithrilMinesTeleporter;
import ai.npc.Teleports.NewbieTravelToken.NewbieTravelToken;
import ai.npc.Teleports.NoblesseTeleport.NoblesseTeleport;
import ai.npc.Teleports.OracleTeleport.OracleTeleport;
import ai.npc.Teleports.PaganTeleporters.PaganTeleporters;
import ai.npc.Teleports.SeparatedSoul.SeparatedSoul;
import ai.npc.Teleports.StakatoNestTeleporter.StakatoNestTeleporter;
import ai.npc.Teleports.SteelCitadelTeleport.SteelCitadelTeleport;
import ai.npc.Teleports.StrongholdsTeleports.StrongholdsTeleports;
import ai.npc.Teleports.Survivor.Survivor;
import ai.npc.Teleports.TeleportToFantasy.TeleportToFantasy;
import ai.npc.Teleports.TeleportToRaceTrack.TeleportToRaceTrack;
import ai.npc.Teleports.TeleportToUndergroundColiseum.TeleportToUndergroundColiseum;
import ai.npc.Teleports.TeleportWithCharm.TeleportWithCharm;
import ai.npc.Teleports.ToIVortex.ToIVortex;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class TeleportersLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		CrumaTower.class,
		DelusionTeleport.class,
		ElrokiTeleporters.class,
		GatekeeperSpirit.class,
		GhostChamberlainOfElmoreden.class,
		HuntingGroundsTeleport.class,
		Klein.class,
		Klemis.class,
		MithrilMinesTeleporter.class,
		NewbieTravelToken.class,
		NoblesseTeleport.class,
		OracleTeleport.class,
		PaganTeleporters.class,
		SeparatedSoul.class,
		StakatoNestTeleporter.class,
		SteelCitadelTeleport.class,
		StrongholdsTeleports.class,
		Survivor.class,
		TeleportToFantasy.class,
		TeleportToRaceTrack.class,
		TeleportToUndergroundColiseum.class,
		TeleportWithCharm.class,
		ToIVortex.class,
	};
	
	public TeleportersLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
