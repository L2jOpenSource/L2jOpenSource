package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;

/**
 * @author Luca Baldi
 */
public class RelationChanged extends L2GameServerPacket
{
	public static final int RELATION_PVP_FLAG = 0x00002; // pvp ???
	public static final int RELATION_HAS_KARMA = 0x00004; // karma ???
	public static final int RELATION_LEADER = 0x00080; // leader
	public static final int RELATION_INSIEGE = 0x00200; // true if in siege
	public static final int RELATION_ATTACKER = 0x00400; // true when attacker
	public static final int RELATION_ALLY = 0x00800; // blue siege icon, cannot have if red
	public static final int RELATION_ENEMY = 0x01000; // true when red icon, doesn't matter with blue
	public static final int RELATION_MUTUAL_WAR = 0x08000; // double fist
	public static final int RELATION_1SIDED_WAR = 0x10000; // single fist
	
	private final int objId, relation, autoAttackable;
	private int karma;
	private int pvpFlag;
	
	public RelationChanged(final L2PlayableInstance activeChar, final int relation, final boolean autoattackable)
	{
		objId = activeChar.getObjectId();
		this.relation = relation;
		autoAttackable = autoattackable ? 1 : 0;
		if (activeChar instanceof L2PcInstance)
		{
			karma = ((L2PcInstance) activeChar).getKarma();
			pvpFlag = ((L2PcInstance) activeChar).getPvpFlag();
		}
		else if (activeChar instanceof L2SummonInstance)
		{
			karma = ((L2SummonInstance) activeChar).getOwner().getKarma();
			pvpFlag = ((L2SummonInstance) activeChar).getOwner().getPvpFlag();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xce);
		writeD(objId);
		writeD(relation);
		writeD(autoAttackable);
		writeD(karma);
		writeD(pvpFlag);
	}
	
	@Override
	public String getType()
	{
		return "[S] CE RelationChanged";
	}
}