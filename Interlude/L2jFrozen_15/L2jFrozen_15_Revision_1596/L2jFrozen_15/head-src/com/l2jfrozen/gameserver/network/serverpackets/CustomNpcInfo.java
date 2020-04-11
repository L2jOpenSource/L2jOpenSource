package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.CharTemplateTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;

public class CustomNpcInfo extends L2GameServerPacket
{
	private L2NpcInstance npcPlayer;
	
	public CustomNpcInfo(L2NpcInstance npc)
	{
		npcPlayer = npc;
		npcPlayer.setClientX(npcPlayer.getPosition().getX());
		npcPlayer.setClientY(npcPlayer.getPosition().getY());
		npcPlayer.setClientZ(npcPlayer.getPosition().getZ());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x03);
		writeD(npcPlayer.getX());
		writeD(npcPlayer.getY());
		writeD(npcPlayer.getZ());
		writeD(npcPlayer.getHeading());
		writeD(npcPlayer.getObjectId());
		writeS(npcPlayer.getCustomNpcInstance().getName());
		writeD(npcPlayer.getCustomNpcInstance().getRace());
		writeD(npcPlayer.getCustomNpcInstance().isFemaleSex() ? 1 : 0);
		writeD(npcPlayer.getCustomNpcInstance().getClassId());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_HAIR());
		writeD(0);
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_RHAND());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_LHAND());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_GLOVES());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_CHEST());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_LEGS());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_FEET());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_HAIR());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_RHAND());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_HAIR());
		writeD(npcPlayer.getCustomNpcInstance().PAPERDOLL_HAIR2());
		write('H', 0, 24);
		writeD(npcPlayer.getCustomNpcInstance().getPvpFlag() ? 1 : 0);
		writeD(npcPlayer.getCustomNpcInstance().getKarma());
		writeD(npcPlayer.getMAtkSpd());
		writeD(npcPlayer.getPAtkSpd());
		writeD(npcPlayer.getCustomNpcInstance().getPvpFlag() ? 1 : 0);
		writeD(npcPlayer.getCustomNpcInstance().getKarma());
		writeD(npcPlayer.getRunSpeed());
		writeD(npcPlayer.getRunSpeed() / 2);
		writeD(npcPlayer.getRunSpeed() / 3);
		writeD(npcPlayer.getRunSpeed() / 3);
		writeD(npcPlayer.getRunSpeed());
		writeD(npcPlayer.getRunSpeed());
		writeD(npcPlayer.getRunSpeed());
		writeD(npcPlayer.getRunSpeed());
		writeF(npcPlayer.getStat().getMovementSpeedMultiplier());
		writeF(npcPlayer.getStat().getAttackSpeedMultiplier());
		writeF(CharTemplateTable.getInstance().getTemplate(npcPlayer.getCustomNpcInstance().getClassId()).getCollisionRadius());
		writeF(CharTemplateTable.getInstance().getTemplate(npcPlayer.getCustomNpcInstance().getClassId()).getCollisionHeight());
		writeD(npcPlayer.getCustomNpcInstance().getHairStyle());
		writeD(npcPlayer.getCustomNpcInstance().getHairColor());
		writeD(npcPlayer.getCustomNpcInstance().getFace());
		writeS(npcPlayer.getCustomNpcInstance().getTitle());
		writeD(npcPlayer.getCustomNpcInstance().getClanId());
		writeD(npcPlayer.getCustomNpcInstance().getClanCrestId());
		writeD(npcPlayer.getCustomNpcInstance().getAllyId());
		writeD(npcPlayer.getCustomNpcInstance().getAllyCrestId());
		writeD(0);
		writeC(1);
		writeC(npcPlayer.isRunning() ? 1 : 0);
		writeC(npcPlayer.isInCombat() ? 1 : 0);
		writeC(npcPlayer.isAlikeDead() ? 1 : 0);
		write('C', 0, 3);
		writeH(0);
		writeC(0x00);
		writeD(npcPlayer.getAbnormalEffect());
		writeC(0);
		writeH(0);
		writeD(npcPlayer.getCustomNpcInstance().getClassId());
		writeD(npcPlayer.getMaxCp());
		writeD((int) npcPlayer.getStatus().getCurrentCp());
		writeC(npcPlayer.getCustomNpcInstance().getEnchantWeapon());
		writeC(0x00);
		writeD(0);// clan crest
		writeC(npcPlayer.getCustomNpcInstance().isNoble() ? 1 : 0);
		writeC(npcPlayer.getCustomNpcInstance().isHero() ? 1 : 0);
		writeC(0);
		write('D', 0, 3);
		writeD(npcPlayer.getCustomNpcInstance().nameColor());
		writeD(0);
		writeD(npcPlayer.getCustomNpcInstance().getPledgeClass());
		writeD(0);
		writeD(npcPlayer.getCustomNpcInstance().titleColor());
		writeD(0x00);
	}
	
	private void write(char type, int value, int times)
	{
		for (int i = 0; i < times; i++)
		{
			switch (type)
			{
				case 'C':
					writeC(value);
					break;
				case 'D':
					writeD(value);
					break;
				case 'F':
					writeF(value);
					break;
				case 'H':
					writeH(value);
					break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 03 CustomNpcInfo [dddddsddd dddddddddddd dddddddd hhhh d hhhhhhhhhhhh d hhhh hhhhhhhhhhhhhhhh dddddd dddddddd ffff ddd s ddddd ccccccc h c d c h ddd cc d ccc ddddddddddd]";
	}
}
