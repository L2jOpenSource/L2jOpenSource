package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;

/* Packet format: F3 XX000000 YY000000 ZZ000000 */

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final EffectCharge effect;
	
	public EtcStatusUpdate(final L2PcInstance activeChar)
	{
		this.activeChar = activeChar;
		effect = (EffectCharge) this.activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF3); // several icons to a separate line (0 = disabled)
		if (effect != null)
		{
			writeD(effect.getLevel()); // 1-7 increase force, lvl
		}
		else
		{
			writeD(0x00); // 1-7 increase force, lvl
		}
		writeD(activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD(activeChar.getMessageRefusal() || activeChar.isChatBanned() ? 1 : 0); // 1 = block all chat
		// writeD(0x00); // 1 = danger area
		writeD(activeChar.isInsideZone(L2Character.ZONE_DANGERAREA)/* || activeChar.isInDangerArea() */ ? 1 : 0); // 1 = danger area
		writeD(Math.min(activeChar.getExpertisePenalty() + activeChar.getMasteryPenalty() + activeChar.getMasteryWeapPenalty(), 1)); // 1 = grade penalty
		writeD(activeChar.getCharmOfCourage() ? 1 : 0); // 1 = charm of courage (no xp loss in siege..)
		writeD(activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] F3 EtcStatusUpdate";
	}
}
