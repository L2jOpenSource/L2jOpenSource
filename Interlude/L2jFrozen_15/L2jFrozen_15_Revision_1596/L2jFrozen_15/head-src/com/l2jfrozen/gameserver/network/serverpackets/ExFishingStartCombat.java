package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * Format (ch)dddcc
 * @author -Wooden-
 */
public class ExFishingStartCombat extends L2GameServerPacket
{
	private final L2Character activeChar;
	private final int time, hp;
	private final int lureType, deceptiveMode, mode;
	
	public ExFishingStartCombat(final L2Character character, final int time, final int hp, final int mode, final int lureType, final int deceptiveMode)
	{
		activeChar = character;
		this.time = time;
		this.hp = hp;
		this.mode = mode;
		this.lureType = lureType;
		this.deceptiveMode = deceptiveMode;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x15);
		
		writeD(activeChar.getObjectId());
		writeD(time);
		writeD(hp);
		writeC(mode); // mode: 0 = resting, 1 = fighting
		writeC(lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		writeC(deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:15 ExFishingStartCombat";
	}
	
}
