package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Vector;

/**
 * @author kombat Format: cd d[d s/d/dd/ddd]
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private final int messageId;
	private int skillLvL = 1;
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private final Vector<Integer> types = new Vector<>();
	private final Vector<Object> values = new Vector<>();
	private int time = 0;
	private int requesterId = 0;
	
	public ConfirmDlg(final int messageId)
	{
		this.messageId = messageId;
	}
	
	public ConfirmDlg addString(final String text)
	{
		types.add(TYPE_TEXT);
		values.add(text);
		return this;
	}
	
	public ConfirmDlg addNumber(final int number)
	{
		types.add(TYPE_NUMBER);
		values.add(number);
		return this;
	}
	
	public ConfirmDlg addNpcName(final int id)
	{
		types.add(TYPE_NPC_NAME);
		values.add(1000000 + id);
		return this;
	}
	
	public ConfirmDlg addItemName(final int id)
	{
		types.add(TYPE_ITEM_NAME);
		values.add(id);
		return this;
	}
	
	public ConfirmDlg addZoneName(final int x, final int y, final int z)
	{
		types.add(TYPE_ZONE_NAME);
		final int[] coord =
		{
			x,
			y,
			z
		};
		values.add(coord);
		return this;
	}
	
	public ConfirmDlg addSkillName(final int id)
	{
		return addSkillName(id, 1);
	}
	
	public ConfirmDlg addSkillName(final int id, final int lvl)
	{
		types.add(TYPE_SKILL_NAME);
		values.add(id);
		skillLvL = lvl;
		return this;
	}
	
	public ConfirmDlg addTime(final int time)
	{
		this.time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(final int id)
	{
		requesterId = id;
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xed);
		writeD(messageId);
		if (types != null && types.size() > 0)
		{
			writeD(types.size());
			for (int i = 0; i < types.size(); i++)
			{
				final int t = types.get(i).intValue();
				writeD(t);
				switch (t)
				{
					case TYPE_TEXT:
					{
						writeS((String) values.get(i));
						break;
					}
					case TYPE_NUMBER:
					case TYPE_NPC_NAME:
					case TYPE_ITEM_NAME:
					{
						final int t1 = ((Integer) values.get(i)).intValue();
						writeD(t1);
						break;
					}
					case TYPE_SKILL_NAME:
					{
						final int t1 = ((Integer) values.get(i)).intValue();
						writeD(t1); // Skill Id
						writeD(skillLvL); // Skill lvl
						break;
					}
					case TYPE_ZONE_NAME:
					{
						final int t1 = ((int[]) values.get(i))[0];
						final int t2 = ((int[]) values.get(i))[1];
						final int t3 = ((int[]) values.get(i))[2];
						writeD(t1);
						writeD(t2);
						writeD(t3);
						break;
					}
				}
			}
			// timed dialog (Summon Friend skill request)
			if (time != 0)
			{
				writeD(time);
			}
			if (requesterId != 0)
			{
				writeD(requesterId);
			}
			
			if (time > 0)
			{
				getClient().getActiveChar().addConfirmDlgRequestTime(requesterId, time);
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] ed ConfirmDlg";
	}
}