package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.Vector;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 01 // Packet Identifier <BR>
 * c6 37 50 40 // ObjectId <BR>
 * <BR>
 * 01 00 // Number of Attribute Trame of the Packet <BR>
 * <BR>
 * c6 37 50 40 // Attribute Identifier : 01-Level, 02-Experience, 03-STR, 04-DEX, 05-CON, 06-INT, 07-WIT, 08-MEN, 09-Current HP, 0a, Max HP...<BR>
 * cd 09 00 00 // Attribute Value <BR>
 * format d d(dd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class StatusUpdate extends L2GameServerPacket
{
	public static final int LEVEL = 0x01;
	public static final int EXP = 0x02;
	public static final int STR = 0x03;
	public static final int DEX = 0x04;
	public static final int CON = 0x05;
	public static final int INT = 0x06;
	public static final int WIT = 0x07;
	public static final int MEN = 0x08;
	
	public static final int CUR_HP = 0x09;
	public static final int MAX_HP = 0x0a;
	public static final int CUR_MP = 0x0b;
	public static final int MAX_MP = 0x0c;
	
	public static final int SP = 0x0d;
	public static final int CUR_LOAD = 0x0e;
	public static final int MAX_LOAD = 0x0f;
	
	public static final int P_ATK = 0x11;
	public static final int ATK_SPD = 0x12;
	public static final int P_DEF = 0x13;
	public static final int EVASION = 0x14;
	public static final int ACCURACY = 0x15;
	public static final int CRITICAL = 0x16;
	public static final int M_ATK = 0x17;
	public static final int CAST_SPD = 0x18;
	public static final int M_DEF = 0x19;
	public static final int PVP_FLAG = 0x1a;
	public static final int KARMA = 0x1b;
	
	public static final int CUR_CP = 0x21;
	public static final int MAX_CP = 0x22;
	
	private L2PcInstance actor;
	
	private Vector<Attribute> attributes;
	public int objectId;
	
	class Attribute
	{
		// id values 09 - current health 0a - max health 0b - current mana 0c - max mana
		public int id;
		public int value;
		
		Attribute(final int pId, final int pValue)
		{
			id = pId;
			value = pValue;
		}
	}
	
	public StatusUpdate(final L2PcInstance actor)
	{
		this.actor = actor;
	}
	
	public StatusUpdate(final int objectId)
	{
		attributes = new Vector<>();
		this.objectId = objectId;
	}
	
	public void addAttribute(final int id, final int level)
	{
		attributes.add(new Attribute(id, level));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0e);
		
		if (actor != null)
		{
			writeD(actor.getObjectId());
			writeD(28); // all the attributes
			
			writeD(LEVEL);
			writeD(actor.getLevel());
			writeD(EXP);
			writeD((int) actor.getExp());
			writeD(STR);
			writeD(actor.getSTR());
			writeD(DEX);
			writeD(actor.getDEX());
			writeD(CON);
			writeD(actor.getCON());
			writeD(INT);
			writeD(actor.getINT());
			writeD(WIT);
			writeD(actor.getWIT());
			writeD(MEN);
			writeD(actor.getMEN());
			
			writeD(CUR_HP);
			writeD((int) actor.getCurrentHp());
			writeD(MAX_HP);
			writeD(actor.getMaxHp());
			writeD(CUR_MP);
			writeD((int) actor.getCurrentMp());
			writeD(MAX_MP);
			writeD(actor.getMaxMp());
			writeD(SP);
			writeD(actor.getSp());
			writeD(CUR_LOAD);
			writeD(actor.getCurrentLoad());
			writeD(MAX_LOAD);
			writeD(actor.getMaxLoad());
			
			writeD(P_ATK);
			writeD(actor.getPAtk(null));
			writeD(ATK_SPD);
			writeD(actor.getPAtkSpd());
			writeD(P_DEF);
			writeD(actor.getPDef(null));
			writeD(EVASION);
			writeD(actor.getEvasionRate(null));
			writeD(ACCURACY);
			writeD(actor.getAccuracy());
			writeD(CRITICAL);
			writeD(actor.getCriticalHit(null, null));
			writeD(M_ATK);
			writeD(actor.getMAtk(null, null));
			
			writeD(CAST_SPD);
			writeD(actor.getMAtkSpd());
			writeD(M_DEF);
			writeD(actor.getMDef(null, null));
			writeD(PVP_FLAG);
			writeD(actor.getPvpFlag());
			writeD(KARMA);
			writeD(actor.getKarma());
			writeD(CUR_CP);
			writeD((int) actor.getCurrentCp());
			writeD(MAX_CP);
			writeD(actor.getMaxCp());
			
		}
		else
		{
			
			writeD(objectId);
			writeD(attributes.size());
			
			for (int i = 0; i < attributes.size(); i++)
			{
				final Attribute temp = attributes.get(i);
				
				writeD(temp.id);
				writeD(temp.value);
			}
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 0e StatusUpdate";
	}
}
