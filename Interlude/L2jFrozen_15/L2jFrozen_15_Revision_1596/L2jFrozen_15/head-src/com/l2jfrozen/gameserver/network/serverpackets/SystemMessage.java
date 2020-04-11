package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Vector;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.SystemMessageId;

public final class SystemMessage extends L2GameServerPacket
{
	// Packets d d (d S/d d/d dd) -> 0 - String 1-number 2-textref npcname (1000000-1002655) 3-textref itemname 4-textref skills 5-??
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private final int messageId;
	private final Vector<Integer> types = new Vector<>();
	private final Vector<Object> values = new Vector<>();
	private int skillLvL = 1;
	
	public SystemMessage(final SystemMessageId messageId)
	{
		if (Config.DEBUG && messageId == SystemMessageId.TARGET_IS_INCORRECT)
		{
			Thread.dumpStack();
		}
		
		this.messageId = messageId.getId();
	}
	
	public SystemMessage(final int messageId)
	{
		this.messageId = messageId;
	}
	
	public static SystemMessage sendString(final String msg)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		sm.addString(msg);
		
		return sm;
	}
	
	public SystemMessage addString(final String text)
	{
		types.add(TYPE_TEXT);
		values.add(text);
		
		return this;
	}
	
	public SystemMessage addNumber(final int number)
	{
		types.add(TYPE_NUMBER);
		values.add(number);
		return this;
	}
	
	public SystemMessage addNpcName(final int id)
	{
		types.add(TYPE_NPC_NAME);
		values.add(1000000 + id);
		
		return this;
	}
	
	public SystemMessage addItemName(final int id)
	{
		types.add(TYPE_ITEM_NAME);
		values.add(id);
		
		return this;
	}
	
	public SystemMessage addZoneName(final int x, final int y, final int z)
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
	
	public SystemMessage addSkillName(final int id)
	{
		return addSkillName(id, 1);
	}
	
	public SystemMessage addSkillName(final int id, final int lvl)
	{
		types.add(TYPE_SKILL_NAME);
		values.add(id);
		skillLvL = lvl;
		
		return this;
	}
	
	public void addSkillName(final L2Skill skill)
	{
	} // Check this
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x64);
		
		writeD(messageId);
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
	}
	
	public int getMessageID()
	{
		return messageId;
	}
	
	public static SystemMessage getSystemMessage(final SystemMessageId smId)
	{
		final SystemMessage sm = new SystemMessage(smId);
		return sm;
	}
	
	@Override
	public String getType()
	{
		return "[S] 64 SystemMessage";
	}
}