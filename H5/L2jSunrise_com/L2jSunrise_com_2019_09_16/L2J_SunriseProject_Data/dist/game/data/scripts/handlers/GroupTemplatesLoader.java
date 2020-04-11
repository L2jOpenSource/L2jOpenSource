package handlers;

import gr.sr.handler.ABLoader;

import ai.group_template.BeastFarm;
import ai.group_template.DenOfEvil;
import ai.group_template.FeedableBeasts;
import ai.group_template.FleeMonsters;
import ai.group_template.FrozenLabyrinth;
import ai.group_template.GiantsCave;
import ai.group_template.HotSprings;
import ai.group_template.IsleOfPrayer;
import ai.group_template.MithrilMines;
import ai.group_template.MonasteryOfSilence;
import ai.group_template.PlainsOfDion;
import ai.group_template.PolymorphingAngel;
import ai.group_template.PolymorphingOnAttack;
import ai.group_template.PrisonGuards;
import ai.group_template.RandomSpawn;
import ai.group_template.Sandstorms;
import ai.group_template.SilentValley;
import ai.group_template.SummonMinions;
import ai.group_template.SummonPc;
import ai.group_template.TurekOrcs;
import ai.group_template.VarkaKetra;
import ai.group_template.WarriorFishingBlock;
import ai.group_template.extra.BrekaOrcOverlord;
import ai.group_template.extra.Chests;
import ai.group_template.extra.CryptsOfDisgrace;
import ai.group_template.extra.FieldOfWhispersSilence;
import ai.group_template.extra.KarulBugbear;
import ai.group_template.extra.LuckyPig;
import ai.group_template.extra.Mutation;
import ai.group_template.extra.OlMahumGeneral;
import ai.group_template.extra.TimakOrcOverlord;
import ai.group_template.extra.TimakOrcTroopLeader;
import ai.group_template.extra.TomlanKamos;
import ai.group_template.extra.WarriorMonk;
import ai.group_template.extra.ZombieGatekeepers;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class GroupTemplatesLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		BeastFarm.class,
		DenOfEvil.class,
		FeedableBeasts.class,
		FleeMonsters.class,
		FrozenLabyrinth.class,
		GiantsCave.class,
		HotSprings.class,
		IsleOfPrayer.class,
		MithrilMines.class,
		MonasteryOfSilence.class,
		PlainsOfDion.class,
		PolymorphingAngel.class,
		PolymorphingOnAttack.class,
		PrisonGuards.class,
		RandomSpawn.class,
		Sandstorms.class,
		SilentValley.class,
		SummonMinions.class,
		SummonPc.class,
		TurekOrcs.class,
		VarkaKetra.class,
		WarriorFishingBlock.class,
		
		// Extras
		BrekaOrcOverlord.class,
		Chests.class,
		CryptsOfDisgrace.class,
		FieldOfWhispersSilence.class,
		KarulBugbear.class,
		LuckyPig.class,
		Mutation.class,
		OlMahumGeneral.class,
		TimakOrcOverlord.class,
		TimakOrcTroopLeader.class,
		TomlanKamos.class,
		WarriorMonk.class,
		ZombieGatekeepers.class,
	};
	
	public GroupTemplatesLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
