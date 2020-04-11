package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * Format (ch)dddcccd d: cahacter oid d: time left d: fish hp c: c: c: 00 if fish gets damage 02 if fish regens d:
 * @author -Wooden-
 */
public class ExFishingHpRegen extends L2GameServerPacket
{
	private final L2Character activeChar;
	private final int time, fishHP, hpMode, anim, goodUse, penalty, hpBarColor;
	
	public ExFishingHpRegen(final L2Character character, final int time, final int fishHP, final int hpMode, final int goodUse, final int anim, final int penalty, final int hpBarColor)
	{
		activeChar = character;
		this.time = time;
		this.fishHP = fishHP;
		this.hpMode = hpMode;
		this.goodUse = goodUse;
		this.anim = anim;
		this.penalty = penalty;
		this.hpBarColor = hpBarColor;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x16);
		
		writeD(activeChar.getObjectId());
		writeD(time);
		writeD(fishHP);
		writeC(hpMode); // 0 = HP stop, 1 = HP raise
		writeC(goodUse); // 0 = none, 1 = success, 2 = failed
		writeC(anim); // Anim: 0 = none, 1 = reeling, 2 = pumping
		writeD(penalty); // Penalty
		writeC(hpBarColor); // 0 = normal hp bar, 1 = purple hp bar
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:16 ExFishingHPRegen";
	}
	
}
