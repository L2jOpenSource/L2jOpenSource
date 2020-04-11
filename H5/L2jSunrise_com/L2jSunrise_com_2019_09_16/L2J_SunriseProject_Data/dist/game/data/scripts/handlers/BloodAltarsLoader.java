package handlers;

import gr.sr.handler.ABLoader;

import ai.npc.BloodAltars.AdenBloodAltar;
import ai.npc.BloodAltars.DarkElfBloodAltar;
import ai.npc.BloodAltars.DionBloodAltar;
import ai.npc.BloodAltars.DwarenBloodAltar;
import ai.npc.BloodAltars.ElvenBloodAltar;
import ai.npc.BloodAltars.GiranBloodAltar;
import ai.npc.BloodAltars.GludinBloodAltar;
import ai.npc.BloodAltars.GludioBloodAltar;
import ai.npc.BloodAltars.GoddardBloodAltar;
import ai.npc.BloodAltars.HeineBloodAltar;
import ai.npc.BloodAltars.KamaelBloodAltar;
import ai.npc.BloodAltars.OrcBloodAltar;
import ai.npc.BloodAltars.OrenBloodAltar;
import ai.npc.BloodAltars.PrimevalBloodAltar;
import ai.npc.BloodAltars.RuneBloodAltar;
import ai.npc.BloodAltars.SchutgartBloodAltar;
import ai.npc.BloodAltars.TalkingIslandBloodAltar;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class BloodAltarsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		AdenBloodAltar.class,
		DarkElfBloodAltar.class,
		DionBloodAltar.class,
		DwarenBloodAltar.class,
		ElvenBloodAltar.class,
		GiranBloodAltar.class,
		GludinBloodAltar.class,
		GludioBloodAltar.class,
		GoddardBloodAltar.class,
		HeineBloodAltar.class,
		KamaelBloodAltar.class,
		OrcBloodAltar.class,
		OrenBloodAltar.class,
		PrimevalBloodAltar.class,
		RuneBloodAltar.class,
		SchutgartBloodAltar.class,
		TalkingIslandBloodAltar.class,
	};
	
	public BloodAltarsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
