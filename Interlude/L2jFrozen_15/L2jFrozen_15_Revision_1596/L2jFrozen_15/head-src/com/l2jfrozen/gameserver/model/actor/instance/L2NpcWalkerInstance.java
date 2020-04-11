package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.ai.L2NpcWalkerAI;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class manages some npcs can walk in the city. <br>
 * It inherits all methods from L2NpcInstance. <br>
 * <br>
 * @original author Rayan RPG
 * @since    819
 */
public class L2NpcWalkerInstance extends L2NpcInstance
{
	
	/**
	 * Constructor of L2NpcWalkerInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2NpcWalkerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		setAI(new L2NpcWalkerAI(new L2NpcWalkerAIAccessor()));
	}
	
	/**
	 * AI can't be deattached, npc must move always with the same AI instance.
	 * @param newAI AI to set for this L2NpcWalkerInstance
	 */
	@Override
	public void setAI(final L2CharacterAI newAI)
	{
		if (aiCharacter == null)
		{
			super.setAI(newAI);
		}
	}
	
	@Override
	public void onSpawn()
	{
		((L2NpcWalkerAI) getAI()).setHomeX(getX());
		((L2NpcWalkerAI) getAI()).setHomeY(getY());
		((L2NpcWalkerAI) getAI()).setHomeZ(getZ());
	}
	
	/**
	 * Sends a chat to all knowObjects.
	 * @param chat message to say
	 */
	public void broadcastChat(final String chat)
	{
		final Map<Integer, L2PcInstance> knownPlayers = getKnownList().getKnownPlayers();
		
		if (knownPlayers == null)
		{
			if (Config.DEVELOPER)
			{
				LOGGER.info("broadcastChat players == null");
			}
			return;
		}
		
		// we send message to known players only!
		if (knownPlayers.size() > 0)
		{
			CreatureSay cs = new CreatureSay(getObjectId(), 0, getName(), chat);
			
			// we interact and list players here
			for (final L2PcInstance players : knownPlayers.values())
			{
				// finally send packet :D
				players.sendPacket(cs);
			}
			
			cs = null;
		}
	}
	
	/**
	 * NPCs are immortal.
	 * @param i        ignore it
	 * @param attacker ignore it
	 * @param awake    ignore it
	 */
	@Override
	public void reduceCurrentHp(final double i, final L2Character attacker, final boolean awake)
	{
	}
	
	/**
	 * NPCs are immortal.
	 * @param  killer ignore it
	 * @return        false
	 */
	@Override
	public boolean doDie(final L2Character killer)
	{
		return false;
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		return super.getAI();
	}
	
	/**
	 * The Class L2NpcWalkerAIAccessor.
	 */
	protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor
	{
		/**
		 * AI can't be deattached.
		 */
		@Override
		public void detachAI()
		{
		}
	}
}
