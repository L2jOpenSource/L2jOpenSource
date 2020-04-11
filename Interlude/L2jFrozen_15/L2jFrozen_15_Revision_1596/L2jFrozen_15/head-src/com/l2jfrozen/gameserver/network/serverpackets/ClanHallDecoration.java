package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.entity.ClanHall;
import com.l2jfrozen.gameserver.model.entity.ClanHall.ClanHallFunction;

/**
 * @author Steuf
 */
public class ClanHallDecoration extends L2GameServerPacket
{
	private final ClanHall clanHall;
	private ClanHallFunction function;
	
	public ClanHallDecoration(final ClanHall clanHall)
	{
		this.clanHall = clanHall;
	}
	
	/*
	 * Packet send, must be confirmed writeC(0xf7); writeD(0); // clanhall id writeC(0); // FUNC_RESTORE_HP (Fireplace) writeC(0); // FUNC_RESTORE_MP (Carpet) writeC(0); // FUNC_RESTORE_MP (Statue) writeC(0); // FUNC_RESTORE_EXP (Chandelier) writeC(0); // FUNC_TELEPORT (Mirror) writeC(0); // Crytal
	 * writeC(0); // Curtain writeC(0); // FUNC_ITEM_CREATE (Magic Curtain) writeC(0); // FUNC_SUPPORT writeC(0); // FUNC_SUPPORT (Flag) writeC(0); // Front Platform writeC(0); // FUNC_ITEM_CREATE writeD(0); writeD(0);
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0xf7);
		writeD(clanHall.getId()); // clanhall id
		// FUNC_RESTORE_HP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_HP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 220 || clanHall.getGrade() == 1 && function.getLvl() < 160 || clanHall.getGrade() == 2 && function.getLvl() < 260 || clanHall.getGrade() == 3 && function.getLvl() < 300)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		
		// FUNC_RESTORE_MP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_MP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if ((clanHall.getGrade() == 0 || clanHall.getGrade() == 1) && function.getLvl() < 25 || clanHall.getGrade() == 2 && function.getLvl() < 30 || clanHall.getGrade() == 3 && function.getLvl() < 40)
		{
			writeC(1);
			writeC(1);
		}
		else
		{
			writeC(2);
			writeC(2);
		}
		
		// FUNC_RESTORE_EXP
		function = clanHall.getFunction(ClanHall.FUNC_RESTORE_EXP);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 25 || clanHall.getGrade() == 1 && function.getLvl() < 30 || clanHall.getGrade() == 2 && function.getLvl() < 40 || clanHall.getGrade() == 3 && function.getLvl() < 50)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		// FUNC_TELEPORT
		function = clanHall.getFunction(ClanHall.FUNC_TELEPORT);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() < 2)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		writeC(0);
		
		// CURTAINS
		function = clanHall.getFunction(ClanHall.FUNC_DECO_CURTAINS);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		
		// FUNC_ITEM_CREATE
		function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 || function.getLvl() < 3)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		
		// FUNC_SUPPORT
		function = clanHall.getFunction(ClanHall.FUNC_SUPPORT);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 || clanHall.getGrade() == 1 && function.getLvl() < 4 || clanHall.getGrade() == 2 && function.getLvl() < 5 || clanHall.getGrade() == 3 && function.getLvl() < 8)
		{
			writeC(1);
			writeC(1);
		}
		else
		{
			writeC(2);
			writeC(2);
		}
		// Front Plateform
		function = clanHall.getFunction(ClanHall.FUNC_DECO_FRONTPLATEFORM);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (function.getLvl() <= 1)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		
		// FUNC_ITEM_CREATE
		function = clanHall.getFunction(ClanHall.FUNC_ITEM_CREATE);
		if (function == null || function.getLvl() == 0)
		{
			writeC(0);
		}
		else if (clanHall.getGrade() == 0 && function.getLvl() < 2 || function.getLvl() < 3)
		{
			writeC(1);
		}
		else
		{
			writeC(2);
		}
		writeD(0);
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return "[S] F7 AgitDecoInfo";
	}
}
