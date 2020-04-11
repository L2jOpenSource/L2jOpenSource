package net.sf.l2j.gameserver.enums;

import net.sf.l2j.gameserver.model.group.CommandChannel;

/**
 * Related informations regarding boss.
 */
public enum BossInfoType
{
	AQ(29001, "[(Queen Ant)  # %s's Command Channel has looting rights.]", "[(Queen Ant) Looting rules are no longer active.]", 36),
	CORE(29006, "[(Core)  # %s's Command Channel has looting rights.]", "[(Core) Looting rules are no longer active.]", 36),
	ORFEN(29014, "[(Orfen)  # %s's Command Channel has looting rights.]", "[(Orfen) Looting rules are no longer active.]", 36),
	ZAKEN(29022, "[(Zaken)  # %s's Command Channel has looting rights.]", "[(Zaken) Looting rules are no longer active.]", 36),
	REGULAR(0, "[# %s's Command Channel has looting rights.]", "[Looting rules are no longer active.]", 18),
	BAIUM(29020, "[# %s's Command Channel has looting rights.]", "[Looting rules are no longer active.]", 36),
	ANTHARAS(29019, "[# %s's Command Channel has looting rights.]", "[Looting rules are no longer active.]", 225),
	VALAKAS(29028, "[# %s's Command Channel has looting rights.]", "[Looting rules are no longer active.]", 36);
	
	public static final BossInfoType[] VALUES = values();
	
	private final int _npcId;
	private final String _ccRightsMsg;
	private final String _ccNoRightsMsg;
	private final int _requiredMembersAmount;
	
	private BossInfoType(int npcId, String ccRightsMsg, String ccNoRightsMsg, int requiredMembersAmount)
	{
		_npcId = npcId;
		_ccRightsMsg = ccRightsMsg;
		_ccNoRightsMsg = ccNoRightsMsg;
		_requiredMembersAmount = requiredMembersAmount;
	}
	
	public int getNpcId()
	{
		return _npcId;
	}
	
	public String getCcRightsMsg()
	{
		return _ccRightsMsg;
	}
	
	public String getCcNoRightsMsg()
	{
		return _ccNoRightsMsg;
	}
	
	public int getRequiredMembersAmount()
	{
		return _requiredMembersAmount;
	}
	
	public static BossInfoType getBossInfo(int npcId)
	{
		for (BossInfoType bit : VALUES)
			if (bit.getNpcId() == npcId)
				return bit;
			
		return REGULAR;
	}
	
	public static boolean isCcMeetCondition(CommandChannel cc, int npcId)
	{
		return cc != null && cc.getMembersCount() > getBossInfo(npcId).getRequiredMembersAmount();
	}
}