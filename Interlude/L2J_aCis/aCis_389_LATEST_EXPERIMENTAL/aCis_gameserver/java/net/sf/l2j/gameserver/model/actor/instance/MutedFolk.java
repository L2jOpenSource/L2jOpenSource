package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminNpc;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

/**
 * This class leads the behavior of muted NPCs.<br>
 * Their behaviors are the same than NPCs, they just can't talk to player.<br>
 * Some specials instances, such as CabaleBuffers or TownPets got their own muted onAction.
 */
public final class MutedFolk extends Folk
{
	public MutedFolk(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Check if the player is attackable (without a forced attack).
			if (isAutoAttackable(player))
				player.getAI().tryTo(IntentionType.ATTACK, this, false);
			else
			{
				// Calculate the distance between the Player and this instance.
				if (!canInteract(player))
					player.getAI().tryTo(IntentionType.INTERACT, this, false);
				else
				{
					// Stop moving if we're already in interact range.
					if (player.isMoving() || player.isInCombat())
						player.getAI().tryTo(IntentionType.IDLE, null, null);
					
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		// Check if the Player is a GM ; send him NPC infos if true.
		if (player.isGM())
			AdminNpc.sendGeneralInfos(player, this);
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player))
			{
				if (isIn3DRadius(player, player.getPhysicalAttackRange()) && GeoEngine.getInstance().canSeeTarget(player, this))
					player.getAI().tryTo(IntentionType.ATTACK, this, true);
				else
					player.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else
			{
				// Calculate the distance between the Player and the Npc.
				if (!canInteract(player))
					player.getAI().tryTo(IntentionType.INTERACT, this, true);
				else
				{
					// Stop moving if we're already in interact range.
					if (player.isMoving() || player.isInCombat())
						player.getAI().tryTo(IntentionType.IDLE, null, null);
					
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
}