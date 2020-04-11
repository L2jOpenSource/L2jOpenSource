/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.actor.instance.PcInstance;

import java.util.Arrays;

import l2r.gameserver.dao.factory.impl.DAOFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.model.events.impl.character.player.OnPlayerHennaRemove;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.HennaInfo;
import l2r.gameserver.network.serverpackets.SystemMessage;

import gr.sr.player.PcExtention;

/**
 * @author vGodFather
 */
public class L2PcHenna extends PcExtention
{
	private L2Henna[] _henna = new L2Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	public L2PcHenna(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * @return the number of Henna empty slot of the L2PcInstance.
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getChar().getClassId().level() == 1)
		{
			totalSlots = 2;
		}
		else
		{
			totalSlots = 3;
		}
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] != null)
			{
				totalSlots--;
			}
		}
		
		if (totalSlots <= 0)
		{
			return 0;
		}
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.
	 * @param slot
	 * @return
	 */
	public boolean removeHenna(int slot)
	{
		if ((slot < 1) || (slot > 3))
		{
			return false;
		}
		
		slot--;
		
		L2Henna henna = _henna[slot];
		if (henna == null)
		{
			return false;
		}
		
		_henna[slot] = null;
		
		DAOFactory.getInstance().getHennaDAO().delete(getChar(), slot + 1);
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send Server->Client HennaInfo packet to this L2PcInstance
		getChar().sendPacket(new HennaInfo(getChar()));
		
		// Send Server->Client UserInfo packet to this L2PcInstance
		getChar().sendUserInfo(true);
		// Add the recovered dyes to the player's inventory and notify them.
		getChar().getInventory().addItem("Henna", henna.getDyeItemId(), henna.getCancelCount(), getChar(), null);
		getChar().reduceAdena("Henna", henna.getCancelFee(), getChar(), false);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getDyeItemId());
		sm.addLong(henna.getCancelCount());
		getChar().sendPacket(sm);
		getChar().sendPacket(SystemMessageId.SYMBOL_DELETED);
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaRemove(getChar(), henna), getChar());
		return true;
	}
	
	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.
	 * @param henna the henna to add to the player.
	 * @return {@code true} if the henna is added to the player, {@code false} otherwise.
	 */
	public boolean addHenna(L2Henna henna)
	{
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				DAOFactory.getInstance().getHennaDAO().insert(getChar(), henna, i + 1);
				
				// Send Server->Client HennaInfo packet to this L2PcInstance
				getChar().sendPacket(new HennaInfo(getChar()));
				
				// Send Server->Client UserInfo packet to this L2PcInstance
				getChar().sendUserInfo(true);
				
				// Notify to scripts
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaRemove(getChar(), henna), getChar());
				return true;
			}
		}
		return false;
	}
	
	public void setHenna(L2Henna[] henna)
	{
		_henna = henna;
	}
	
	/**
	 * Calculate Henna modifiers of this L2PcInstance.
	 */
	public void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (L2Henna h : _henna)
		{
			if (h == null)
			{
				continue;
			}
			
			_hennaINT += ((_hennaINT + h.getStatINT()) > 5) ? 5 - _hennaINT : h.getStatINT();
			_hennaSTR += ((_hennaSTR + h.getStatSTR()) > 5) ? 5 - _hennaSTR : h.getStatSTR();
			_hennaMEN += ((_hennaMEN + h.getStatMEN()) > 5) ? 5 - _hennaMEN : h.getStatMEN();
			_hennaCON += ((_hennaCON + h.getStatCON()) > 5) ? 5 - _hennaCON : h.getStatCON();
			_hennaWIT += ((_hennaWIT + h.getStatWIT()) > 5) ? 5 - _hennaWIT : h.getStatWIT();
			_hennaDEX += ((_hennaDEX + h.getStatDEX()) > 5) ? 5 - _hennaDEX : h.getStatDEX();
		}
	}
	
	/**
	 * @param slot the character inventory henna slot.
	 * @return the Henna of this L2PcInstance corresponding to the selected slot.
	 */
	public L2Henna getHenna(int slot)
	{
		if ((slot < 1) || (slot > 3))
		{
			return null;
		}
		return _henna[slot - 1];
	}
	
	/**
	 * @return the henna holder for this player.
	 */
	public L2Henna[] getHennaList()
	{
		return _henna;
	}
	
	/**
	 * @return the INT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	/**
	 * @return the STR Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	/**
	 * @return the CON Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	/**
	 * @return the MEN Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	/**
	 * @return the WIT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	/**
	 * @return the DEX Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	public void emptyHennaArray()
	{
		Arrays.fill(_henna, null);
	}
}
