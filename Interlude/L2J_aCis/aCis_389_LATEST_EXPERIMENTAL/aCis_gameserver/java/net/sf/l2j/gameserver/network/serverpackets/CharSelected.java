package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class CharSelected extends L2GameServerPacket
{
	private final Player _player;
	private final int _sessionId;
	
	public CharSelected(Player player, int sessionId)
	{
		_player = player;
		_sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x15);
		
		writeS(_player.getName());
		writeD(_player.getObjectId());
		writeS(_player.getTitle());
		writeD(_sessionId);
		writeD(_player.getClanId());
		
		writeD(0x00); // unknown
		
		writeD(_player.getAppearance().getSex().ordinal());
		writeD(_player.getRace().ordinal());
		writeD(_player.getClassId().getId());
		
		writeD(0x01);
		
		writeD(_player.getX());
		writeD(_player.getY());
		writeD(_player.getZ());
		writeF(_player.getCurrentHp());
		writeF(_player.getCurrentMp());
		writeD(_player.getSp());
		writeQ(_player.getExp());
		writeD(_player.getLevel());
		writeD(_player.getKarma());
		writeD(_player.getPkKills());
		writeD(_player.getINT());
		writeD(_player.getSTR());
		writeD(_player.getCON());
		writeD(_player.getMEN());
		writeD(_player.getDEX());
		writeD(_player.getWIT());
		
		for (int i = 0; i < 30; i++)
		{
			writeD(0x00);
		}
		
		writeD(0x00); // c3 work
		writeD(0x00); // c3 work
		
		writeD(GameTimeTaskManager.getInstance().getGameTime());
		
		writeD(0x00); // c3
		
		writeD(_player.getClassId().getId());
		
		writeD(0x00); // c3 InspectorBin
		writeD(0x00); // c3
		writeD(0x00); // c3
		writeD(0x00); // c3
	}
}