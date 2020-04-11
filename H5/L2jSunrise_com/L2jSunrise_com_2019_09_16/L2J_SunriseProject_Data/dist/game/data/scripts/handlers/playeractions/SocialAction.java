/*
 * This file is part of the L2J Sunrise project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.playeractions;

import l2r.gameserver.ai.NextAction;
import l2r.gameserver.ai.NextAction.NextActionCallback;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExAskCoupleAction;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * Social Action player action handler.
 * @author Nik
 */
public final class SocialAction implements IPlayerActionHandler
{
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		switch (data.getOptionId())
		{
			case 2: // Greeting
			case 3: // Victory
			case 4: // Advance
			case 5: // No
			case 6: // Yes
			case 7: // Bow
			case 8: // Unaware
			case 9: // Social Waiting
			case 10: // Laugh
			case 11: // Applaud
			case 12: // Dance
			case 13: // Sorrow
			case 14: // Charm
			case 15: // Shyness
			case 28: // Propose
			case 29: // Provoke
				useSocial(activeChar, data.getOptionId());
				break;
			case 30: // Beauty Shop
				if (useSocial(activeChar, data.getOptionId()))
				{
					activeChar.broadcastInfo();
				}
				break;
			case 16: // Exchange Bows
			case 17: // High Five
			case 18: // Couple Dance
				useCoupleSocial(activeChar, data.getOptionId());
		}
	}
	
	private boolean useSocial(L2PcInstance activeChar, int id)
	{
		if (activeChar.getFishingEx().isFishing())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return false;
		}
		
		if (activeChar.canMakeSocialAction())
		{
			activeChar.broadcastPacket(new l2r.gameserver.network.serverpackets.SocialAction(activeChar.getObjectId(), id));
		}
		
		return true;
	}
	
	private void useCoupleSocial(L2PcInstance player, int id)
	{
		if (player == null)
		{
			return;
		}
		
		final L2Object target = player.getTarget();
		if ((target == null) || !target.isPlayer())
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		final int distance = (int) Math.sqrt(player.getPlanDistanceSq(target));
		if ((distance > 125) || (distance < 15) || (player.getObjectId() == target.getObjectId()))
		{
			player.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
			return;
		}
		
		SystemMessage sm;
		if (player.isProcessingRequest() || player.isProcessingTransaction())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isInStoreMode() || player.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isInCombat() || player.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getFishingEx().isFishing())
		{
			player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (player.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
		}
		
		if (player.isMounted() || player.isFlyingMounted() || player.isInBoat() || player.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(player);
			player.sendPacket(sm);
			return;
		}
		
		// Checks for partner.
		final L2PcInstance partner = target.getActingPlayer();
		if (partner.isInStoreMode() || partner.isInCraftMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isInCombat() || partner.isInDuel() || AttackStanceTaskManager.getInstance().hasAttackStanceTask(partner))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getMultiSociaAction() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getFishingEx().isFishing())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.getKarma() > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isInOlympiadMode())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isInHideoutSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isInSiege())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isMounted() || partner.isFlyingMounted() || partner.isInBoat() || partner.isInAirShip())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isTeleporting())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isTransformed())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (partner.isAlikeDead())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION);
			sm.addPcName(partner);
			player.sendPacket(sm);
			return;
		}
		
		if (player.isAllSkillsDisabled() || partner.isAllSkillsDisabled())
		{
			player.sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
			return;
		}
		
		player.onTransactionRequest(partner);
		player.setMultiSocialAction(id, partner.getObjectId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1);
		sm.addPcName(partner);
		player.sendPacket(sm);
		
		if ((player.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE) || (partner.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE))
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, (NextActionCallback) () -> partner.sendPacket(new ExAskCoupleAction(player.getObjectId(), id)));
			player.getAI().setNextAction(nextAction);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
		{
			final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, (NextActionCallback) () -> partner.sendPacket(new ExAskCoupleAction(player.getObjectId(), id)));
			player.getAI().setNextAction(nextAction);
			return;
		}
		
		partner.sendPacket(new ExAskCoupleAction(player.getObjectId(), id));
	}
}
