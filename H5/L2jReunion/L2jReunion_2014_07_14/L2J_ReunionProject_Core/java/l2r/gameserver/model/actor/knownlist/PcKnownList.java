/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.knownlist;

import l2r.Config;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2AirShipInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.DeleteObject;
import l2r.gameserver.network.serverpackets.SpawnItem;

public class PcKnownList extends PlayableKnownList
{
	public PcKnownList(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR>
	 * <BR>
	 * <B><U> object is a L2ItemInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2DoorInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2NpcInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2Summon </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * <B><U> object is a L2PcInstance </U> :</B><BR>
	 * <BR>
	 * <li>Send Server-Client Packet CharInfo to the L2PcInstance</li> <li>If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance</li> <li>Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance</li><BR>
	 * <BR>
	 * @param object The L2Object to add to _knownObjects and _knownPlayer
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
		{
			return false;
		}
		
		if (getActiveChar().isHideStores() && (object instanceof L2PcInstance))
		{
			if (object.getActingPlayer().getClient().isDetached() || (object.getActingPlayer().getPrivateStoreType() != PrivateStoreType.NONE))
			{
				return false;
			}
		}
		
		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
		{
			// if (object.getPolytype().equals("item"))
			getActiveChar().sendPacket(new SpawnItem(object));
			// else if (object.getPolytype().equals("npc"))
			// sendPacket(new NpcInfoPoly(object, this));
		}
		else
		{
			if (object.isVisibleFor(getActiveChar()))
			{
				object.sendInfo(getActiveChar());
				
				if (object instanceof L2Character)
				{
					// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
					final L2Character obj = (L2Character) object;
					if (obj.hasAI())
					{
						obj.getAI().describeStateToPlayer(getActiveChar());
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR>
	 * <BR>
	 * @param object The L2Object to remove from _knownObjects and _knownPlayer
	 */
	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (!super.removeKnownObject(object, forget))
		{
			return false;
		}
		
		if (object instanceof L2AirShipInstance)
		{
			if ((((L2AirShipInstance) object).getCaptainId() != 0) && (((L2AirShipInstance) object).getCaptainId() != getActiveChar().getObjectId()))
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getCaptainId()));
			}
			if (((L2AirShipInstance) object).getHelmObjectId() != 0)
			{
				getActiveChar().sendPacket(new DeleteObject(((L2AirShipInstance) object).getHelmObjectId()));
			}
		}
		
		// Send Server-Client Packet DeleteObject to the L2PcInstance
		getActiveChar().sendPacket(new DeleteObject(object));
		
		if (Config.CHECK_KNOWN && (object instanceof L2Npc) && getActiveChar().isGM())
		{
			getActiveChar().sendMessage("Removed NPC: " + ((L2Npc) object).getName());
		}
		
		return true;
	}
	
	@Override
	public final L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if (object.isWalker())
		{
			return 10000;
		}
		
		if (object.isRunner())
		{
			return 15000;
		}
		
		// when knownlist grows, the distance to forget should be at least
		// the same as the previous watch range, or it becomes possible that
		// extra charinfo packets are being sent (watch-forget-watch-forget)
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
		{
			return 4000;
		}
		if (knownlistSize <= 35)
		{
			return 3500;
		}
		if (knownlistSize <= 70)
		{
			return 2910;
		}
		return 2310;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object.isWalker())
		{
			return 9000;
		}
		
		if (object.isRunner())
		{
			return 14000;
		}
		
		final int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
		{
			return 3400; // empty field
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
