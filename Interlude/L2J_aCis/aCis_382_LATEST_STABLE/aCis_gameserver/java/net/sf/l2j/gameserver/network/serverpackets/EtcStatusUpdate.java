package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.model.actor.Player;

public class EtcStatusUpdate extends L2GameServerPacket
{
	private final Player _player;
	
	public EtcStatusUpdate(Player player)
	{
		_player = player;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF3);
		writeD(_player.getCharges());
		writeD(_player.getWeightPenalty());
		writeD((_player.isInRefusalMode() || _player.isChatBanned()) ? 1 : 0);
		writeD(_player.isInsideZone(ZoneId.DANGER_AREA) ? 1 : 0);
		writeD((_player.getExpertiseWeaponPenalty() || _player.getExpertiseArmorPenalty() > 0) ? 1 : 0);
		writeD(_player.isAffected(L2EffectFlag.CHARM_OF_COURAGE) ? 1 : 0);
		writeD(_player.getDeathPenaltyBuffLevel());
	}
}