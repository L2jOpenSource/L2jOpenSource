package handlers.effecthandlers;

import java.util.Collection;
import java.util.Optional;

import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Take Territory Flag effect implementation.
 * @author vGodFather
 */
public final class TakeTerritoryFlag extends L2Effect
{
	private static final int FLAG_NPC_ID = 35062;
	
	public TakeTerritoryFlag(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		final L2PcInstance player = getEffector().getActingPlayer();
		if (!player.isClanLeader())
		{
			return false;
		}
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			//@formatter:off
			final L2SiegeFlagInstance tOoutpost = TerritoryWarManager.getInstance().getHQForClan(player.getClan());
			final Collection<L2Character> outposts = player.getKnownList().getKnownCharactersByIdInRadius(36590, 1000);
			final Optional<L2Character> optional = outposts.stream()
				.filter(outpost -> tOoutpost.getObjectId() == outpost.getObjectId())
				.findFirst();
			//@formatter:on
			
			if (!optional.isPresent())
			{
				return false;
			}
			
			if (TerritoryWarManager.getInstance().getHQForClan(player.getClan()) != player.getTarget())
			{
				return false;
			}
			
			// Spawn a new flag
			final L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, NpcTable.getInstance().getTemplate(FLAG_NPC_ID), false, false);
			flag.setTitle(player.getClan().getName());
			flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
		}
		return true;
	}
}
