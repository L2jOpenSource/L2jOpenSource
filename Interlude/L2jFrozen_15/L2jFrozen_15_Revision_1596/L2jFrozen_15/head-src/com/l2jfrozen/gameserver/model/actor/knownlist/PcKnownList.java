package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.L2CharacterAI;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfrozen.gameserver.network.serverpackets.CharInfo;
import com.l2jfrozen.gameserver.network.serverpackets.DeleteObject;
import com.l2jfrozen.gameserver.network.serverpackets.DoorInfo;
import com.l2jfrozen.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.DropItem;
import com.l2jfrozen.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jfrozen.gameserver.network.serverpackets.NpcInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetInfo;
import com.l2jfrozen.gameserver.network.serverpackets.PetItemList;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfrozen.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jfrozen.gameserver.network.serverpackets.RecipeShopMsg;
import com.l2jfrozen.gameserver.network.serverpackets.RelationChanged;
import com.l2jfrozen.gameserver.network.serverpackets.SpawnItem;
import com.l2jfrozen.gameserver.network.serverpackets.SpawnItemPoly;
import com.l2jfrozen.gameserver.network.serverpackets.StaticObject;
import com.l2jfrozen.gameserver.network.serverpackets.VehicleInfo;

import main.EngineModsManager;
import main.engine.mods.SellBuffs;

public class PcKnownList extends PlayableKnownList
{
	public PcKnownList(final L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * Add a visible L2Object to L2PcInstance knownObjects and knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR>
	 * <BR>
	 * <B><U> object is a L2ItemInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2DoorInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance</li>
	 * <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2NpcInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo to the L2PcInstance</li>
	 * <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2Summon </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance</li>
	 * <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2PcInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet CharInfo to the L2PcInstance</li>
	 * <li>If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance</li>
	 * <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * @param object The L2Object to add to knownObjects and knownPlayer
	 */
	@Override
	public boolean addKnownObject(final L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object, final L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		final L2PcInstance active_char = getActiveChar();
		if (active_char == null)
		{
			return false;
		}
		
		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
		{
			// if (object.getPolytype().equals("item"))
			active_char.sendPacket(new SpawnItemPoly(object));
			// else if (object.getPolytype().equals("npc"))
			// sendPacket(new NpcInfoPoly(object, this));
		}
		else
		{
			if (object instanceof L2ItemInstance)
			{
				if (dropper != null)
				{
					active_char.sendPacket(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
				}
				else
				{
					active_char.sendPacket(new SpawnItem((L2ItemInstance) object));
				}
			}
			else if (object instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) object;
				
				active_char.sendPacket(new DoorInfo(door));
				active_char.sendPacket(new DoorStatusUpdate(door));
			}
			else if (object instanceof L2BoatInstance)
			{
				if (!active_char.isInBoat())
				{
					if (object != active_char.getBoat())
					{
						active_char.sendPacket(new VehicleInfo((L2BoatInstance) object));
						((L2BoatInstance) object).sendVehicleDeparture(active_char);
					}
				}
			}
			else if (object instanceof L2StaticObjectInstance)
			{
				active_char.sendPacket(new StaticObject((L2StaticObjectInstance) object));
			}
			else if (object instanceof L2NpcInstance)
			{
				if (Config.CHECK_KNOWN)
				{
					active_char.sendMessage("Added NPC: " + ((L2NpcInstance) object).getName());
				}
				
				active_char.sendPacket(new NpcInfo((L2NpcInstance) object, active_char));
			}
			else if (object instanceof L2Summon)
			{
				L2Summon summon = (L2Summon) object;
				
				// Check if the L2PcInstance is the owner of the Pet
				if (active_char.equals(summon.getOwner()))
				{
					active_char.sendPacket(new PetInfo(summon));
					// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
					summon.updateEffectIcons(true);
					
					if (summon instanceof L2PetInstance)
					{
						active_char.sendPacket(new PetItemList((L2PetInstance) summon));
					}
				}
				else
				{
					active_char.sendPacket(new NpcInfo(summon, active_char));
				}
				
				summon = null;
			}
			else if (object instanceof L2PcInstance)
			{
				L2PcInstance otherPlayer = (L2PcInstance) object;
				if (otherPlayer.isInBoat())
				{
					otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
					active_char.sendPacket(new CharInfo(otherPlayer));
					
					final int relation = otherPlayer.getRelation(active_char);
					
					if (otherPlayer.getKnownList().getKnownRelations().get(active_char.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(active_char.getObjectId()) != relation)
					{
						active_char.sendPacket(new RelationChanged(otherPlayer, relation, active_char.isAutoAttackable(otherPlayer)));
					}
					
					active_char.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
					
				}
				else
				{
					active_char.sendPacket(new CharInfo(otherPlayer));
					
					final int relation = otherPlayer.getRelation(active_char);
					
					if (otherPlayer.getKnownList().getKnownRelations().get(active_char.getObjectId()) != null && otherPlayer.getKnownList().getKnownRelations().get(active_char.getObjectId()) != relation)
					{
						active_char.sendPacket(new RelationChanged(otherPlayer, relation, active_char.isAutoAttackable(otherPlayer)));
					}
				}
				
				if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL)
				{
					active_char.sendPacket(new PrivateStoreMsgSell(otherPlayer));
				}
				else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY)
				{
					active_char.sendPacket(new PrivateStoreMsgBuy(otherPlayer));
				}
				else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE)
				{
					active_char.sendPacket(new RecipeShopMsg(otherPlayer));
				}
				
				EngineModsManager.onEvent(active_char, SellBuffs.class.getSimpleName() + " sendPacketSellBuff");

				
				otherPlayer = null;
			}
			
			if (object instanceof L2Character)
			{
				// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
				L2Character obj = (L2Character) object;
				
				final L2CharacterAI obj_ai = obj.getAI();
				if (obj_ai != null)
				{
					obj_ai.describeStateToPlayer(active_char);
				}
				
				obj = null;
			}
		}
		
		return true;
	}
	
	/**
	 * Remove a L2Object from L2PcInstance knownObjects and knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR>
	 * <BR>
	 * @param object The L2Object to remove from knownObjects and knownPlayer
	 */
	@Override
	public boolean removeKnownObject(final L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		final L2PcInstance active_char = getActiveChar();
		
		L2PcInstance object_char = null;
		if (object instanceof L2PcInstance)
		{
			object_char = (L2PcInstance) object;
		}
		
		/*
		 * TEMP FIX: If player is not visible don't send packets broadcast to all his KnowList. This will avoid GM detection with l2net and olympiad's crash. We can now find old problems with invisible mode.
		 */
		if (object_char != null && !active_char.isGM())
		{ // GM has to receive remove however because he can see any invisible or inobservermode player
			
			if (!object_char.getAppearance().isInvisible() && !object_char.inObserverMode())
			{
				// Send Server-Client Packet DeleteObject to the L2PcInstance
				active_char.sendPacket(new DeleteObject(object));
			}
			else if (object_char.isGM() && object_char.getAppearance().isInvisible() && !object_char.isTeleporting())
			{
				// Send Server-Client Packet DeleteObject to the L2PcInstance
				active_char.sendPacket(new DeleteObject(object));
			}
		}
		else
		{ // All other objects has to be removed
			
			// Send Server-Client Packet DeleteObject to the L2PcInstance
			active_char.sendPacket(new DeleteObject(object));
		}
		
		if (Config.CHECK_KNOWN && object instanceof L2NpcInstance)
		{
			active_char.sendMessage("Removed NPC: " + ((L2NpcInstance) object).getName());
		}
		
		return true;
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(final L2Object object)
	{
		// when knownlist grows, the distance to forget should be at least
		// the same as the previous watch range, or it becomes possible that
		// extra charinfo packets are being sent (watch-forget-watch-forget)
		final int knownlistSize = getKnownObjects().size();
		
		if (knownlistSize <= 25)
		{
			return 4200;
		}
		
		if (knownlistSize <= 35)
		{
			return 3600;
		}
		
		if (knownlistSize <= 70)
		{
			return 2910;
		}
		return 2310;
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		final int knownlistSize = getKnownObjects().size();
		
		if (knownlistSize <= 25)
		{
			return 3500; // empty field
		}
		
		if (knownlistSize <= 35)
		{
			return 2900;
		}
		
		if (knownlistSize <= 70)
		{
			return 2300;
		}
		return 1700; // Siege, TOI, city
	}
}
