package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Layane
 */
public class L2CabaleBufferInstance extends L2NpcInstance
{
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
			
			// Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private ScheduledFuture<?> aiTask;
	
	private class CabalaAI implements Runnable
	{
		private final L2CabaleBufferInstance caster;
		
		protected CabalaAI(final L2CabaleBufferInstance caster)
		{
			this.caster = caster;
		}
		
		@Override
		public void run()
		{
			boolean isBuffAWinner = false;
			boolean isBuffALoser = false;
			
			final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
			int losingCabal = SevenSigns.CABAL_NULL;
			
			if (winningCabal == SevenSigns.CABAL_DAWN)
			{
				losingCabal = SevenSigns.CABAL_DUSK;
			}
			else if (winningCabal == SevenSigns.CABAL_DUSK)
			{
				losingCabal = SevenSigns.CABAL_DAWN;
			}
			
			/**
			 * For each known player in range, cast either the positive or negative buff. <BR>
			 * The stats affected depend on the player type, either a fighter or a mystic. <BR>
			 * <BR>
			 * Curse of Destruction (Loser)<BR>
			 * - Fighters: -25% Accuracy, -25% Effect Resistance<BR>
			 * - Mystics: -25% Casting Speed, -25% Effect Resistance<BR>
			 * <BR>
			 * <BR>
			 * Blessing of Prophecy (Winner) - Fighters: +25% Max Load, +25% Effect Resistance<BR>
			 * - Mystics: +25% Magic Cancel Resist, +25% Effect Resistance<BR>
			 */
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				
				if (playerCabal == winningCabal && playerCabal != SevenSigns.CABAL_NULL && caster.getNpcId() == SevenSigns.ORATOR_NPC_ID)
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, 4364))
						{
							isBuffAWinner = true;
							continue;
						}
					}
					else
					{
						if (handleCast(player, 4365))
						{
							isBuffAWinner = true;
							continue;
						}
					}
				}
				else if (playerCabal == losingCabal && playerCabal != SevenSigns.CABAL_NULL && caster.getNpcId() == SevenSigns.PREACHER_NPC_ID)
				{
					if (!player.isMageClass())
					{
						if (handleCast(player, 4361))
						{
							isBuffALoser = true;
							continue;
						}
					}
					else
					{
						if (handleCast(player, 4362))
						{
							isBuffALoser = true;
							continue;
						}
					}
				}
				
				if (isBuffAWinner && isBuffALoser)
				{
					break;
				}
			}
		}
		
		private boolean handleCast(final L2PcInstance player, final int skillId)
		{
			final int skillLevel = player.getLevel() > 40 ? 1 : 2;
			
			if (player.isDead() || !player.isVisible() || !isInsideRadius(player, getDistanceToWatchObject(player), false, false))
			{
				return false;
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (player.getFirstEffect(skill) == null)
			{
				skill.getEffects(caster, player, false, false, false);
				broadcastPacket(new MagicSkillUser(caster, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(skillId);
				player.sendPacket(sm);
				skill = null;
				return true;
			}
			skill = null;
			
			return false;
		}
	}
	
	public L2CabaleBufferInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (aiTask != null)
		{
			aiTask.cancel(true);
		}
		
		aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CabalaAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (aiTask != null)
		{
			aiTask.cancel(true);
			aiTask = null;
		}
		
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		return 900;
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
