package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class manages requests (transactions) between two L2PcInstance.
 * @author kriau
 */
public class L2Request
{
	private static final int REQUEST_TIMEOUT = 15; // in secs
	
	protected L2PcInstance player;
	protected L2PcInstance playerPartner;
	protected boolean isRequestor;
	protected boolean isAnswerer;
	protected L2GameClientPacket requestPacket;
	
	public L2Request(final L2PcInstance player)
	{
		this.player = player;
	}
	
	protected void clear()
	{
		playerPartner = null;
		requestPacket = null;
		isRequestor = false;
		isAnswerer = false;
	}
	
	/**
	 * Set the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR>
	 * <BR>
	 * @param partner
	 */
	private synchronized void setPartner(final L2PcInstance partner)
	{
		playerPartner = partner;
	}
	
	/**
	 * @return the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2PcInstance getPartner()
	{
		return playerPartner;
	}
	
	/**
	 * Set the packet incomed from requester.
	 * @param packet
	 */
	private synchronized void setRequestPacket(final L2GameClientPacket packet)
	{
		requestPacket = packet;
	}
	
	/**
	 * @return the packet originally incomed from requester.
	 */
	public L2GameClientPacket getRequestPacket()
	{
		return requestPacket;
	}
	
	/**
	 * Checks if request can be made and in success case puts both PC on request state.
	 * @param  partner
	 * @param  packet
	 * @return
	 */
	public synchronized boolean setRequest(final L2PcInstance partner, final L2GameClientPacket packet)
	{
		if (partner == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return false;
		}
		
		if (partner.getRequest().isProcessingRequest())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			sm = null;
			
			return false;
		}
		
		if (isProcessingRequest())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.WAITING_FOR_ANOTHER_REPLY));
			return false;
		}
		
		playerPartner = partner;
		requestPacket = packet;
		setOnRequestTimer(true);
		playerPartner.getRequest().setPartner(player);
		playerPartner.getRequest().setRequestPacket(packet);
		playerPartner.getRequest().setOnRequestTimer(false);
		
		return true;
	}
	
	private void setOnRequestTimer(final boolean isRequestor)
	{
		this.isRequestor = isRequestor ? true : false;
		isAnswerer = isRequestor ? false : true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(() -> clear(), REQUEST_TIMEOUT * 1000);
		
	}
	
	/**
	 * Clears PC request state. Should be called after answer packet receive.<BR>
	 * <BR>
	 */
	public void onRequestResponse()
	{
		if (playerPartner != null)
		{
			playerPartner.getRequest().clear();
		}
		
		clear();
	}
	
	/**
	 * @return true if a transaction is in progress.
	 */
	public boolean isProcessingRequest()
	{
		return playerPartner != null;
	}
}
