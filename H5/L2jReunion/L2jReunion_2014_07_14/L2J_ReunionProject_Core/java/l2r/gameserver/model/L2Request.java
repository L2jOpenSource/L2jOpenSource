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
package l2r.gameserver.model;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.L2GameClientPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages requests (transactions) between two L2PcInstance.
 * @author kriau
 */
public class L2Request
{
	private static final int REQUEST_TIMEOUT = 15; // in secs
	
	protected L2PcInstance _player;
	protected L2PcInstance _partner;
	protected boolean _isRequestor;
	protected boolean _isAnswerer;
	protected L2GameClientPacket _requestPacket;
	
	public L2Request(L2PcInstance player)
	{
		_player = player;
	}
	
	protected void clear()
	{
		_partner = null;
		_requestPacket = null;
		_isRequestor = false;
		_isAnswerer = false;
	}
	
	/**
	 * Set the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @param partner
	 */
	private synchronized void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}
	
	/**
	 * @return the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2PcInstance getPartner()
	{
		return _partner;
	}
	
	/**
	 * Set the packet incomed from requester.
	 * @param packet
	 */
	private synchronized void setRequestPacket(L2GameClientPacket packet)
	{
		_requestPacket = packet;
	}
	
	/**
	 * Return the packet originally incomed from requester.
	 * @return
	 */
	public L2GameClientPacket getRequestPacket()
	{
		return _requestPacket;
	}
	
	/**
	 * Checks if request can be made and in success case puts both PC on request state.
	 * @param partner
	 * @param packet
	 * @return
	 */
	public synchronized boolean setRequest(L2PcInstance partner, L2GameClientPacket packet)
	{
		if (partner == null)
		{
			_player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (partner.getRequest().isProcessingRequest())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			_player.sendPacket(sm);
			sm = null;
			return false;
		}
		if (isProcessingRequest())
		{
			_player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}
		
		_partner = partner;
		_requestPacket = packet;
		setOnRequestTimer(true);
		_partner.getRequest().setPartner(_player);
		_partner.getRequest().setRequestPacket(packet);
		_partner.getRequest().setOnRequestTimer(false);
		return true;
	}
	
	private void setOnRequestTimer(boolean isRequestor)
	{
		_isRequestor = isRequestor ? true : false;
		_isAnswerer = isRequestor ? false : true;
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				clear();
			}
		}, REQUEST_TIMEOUT * 1000);
		
	}
	
	/**
	 * Clears PC request state. Should be called after answer packet receive.
	 */
	public void onRequestResponse()
	{
		if (_partner != null)
		{
			_partner.getRequest().clear();
		}
		clear();
	}
	
	/**
	 * @return {@code true} if a transaction is in progress.
	 */
	public boolean isProcessingRequest()
	{
		return _partner != null;
	}
}
