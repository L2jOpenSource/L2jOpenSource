package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author Kerberos
 */
public class ExShowScreenMessage extends L2GameServerPacket
{
	public enum TYPE
	{
		SYSTEM_MESSAGE,
		TEXT
	}
	
	public static enum POSITION
	{
		DUMMY,
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
	}
	
	public static enum SIZE
	{
		BIG,
		SMALL
	}
	
	private final int type;
	private final int sysMessageId;
	private final boolean hide;
	private final int unk2;
	private final int unk3;
	private final boolean fade;
	private final int size;
	private final int position;
	private final boolean effect;
	private final String text;
	private final int time;
	
	/**
	 * @param text The TEXT message you want to display
	 * @param time in <b>miliseconds</b>
	 */
	public ExShowScreenMessage(String text, int time)
	{
		type = 1;
		sysMessageId = -1;
		hide = false;
		unk2 = 0;
		unk3 = 0;
		fade = false;
		position = POSITION.TOP_CENTER.ordinal();
		this.text = text;
		this.time = time;
		size = 0;
		effect = false;
	}
	
	/**
	 * @param text The TEXT message you want to display
	 * @param time in <b>miliseconds</b>
	 * @param pos  Position in the screen
	 * @param size Only 2 options, small or big
	 * @param fade at the end of the time, text should have <b>FADE</b> animation? true or false allowed
	 */
	public ExShowScreenMessage(String text, int time, POSITION pos, SIZE size, boolean fade)
	{
		type = 1;
		sysMessageId = -1;
		hide = false;
		unk2 = 0;
		unk3 = 0;
		this.fade = fade;
		position = pos.ordinal();
		this.text = text;
		this.time = time;
		this.size = size.ordinal();
		effect = false;
	}
	
	public ExShowScreenMessage(TYPE type, int messageId, POSITION position, boolean hide, SIZE size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text)
	{
		this.type = type.ordinal();
		sysMessageId = messageId;
		this.hide = hide;
		this.unk2 = 0;
		this.unk3 = 0;
		this.fade = fade;
		this.position = position.ordinal();
		this.text = text;
		this.time = time;
		this.size = size.ordinal();
		effect = showEffect;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x38);
		writeD(type); // 0 - system messages (TYPE.SYSTEM_MESSAGE), 1 - your defined text (TYPE.TEXT)
		writeD(sysMessageId); // system message id (type must be SYSTEM_MESSAGE otherwise no effect)
		writeD(position); // message position
		writeD(hide ? 1 : 0); // hide
		writeD(size); // font size 0 - normal, 1 - small
		writeD(unk2); // ?
		writeD(unk3); // ?
		writeD(effect ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - position must be 2 (POSITION.TOP_CENTER) otherwise no effect
		writeD(time); // time in miliseconds
		writeD(fade ? 1 : 0); // fade effect (0 - disabled, 1 enabled)
		writeS(text); // your text (type must be 1, otherwise no effect)
	}
	
	@Override
	public String getType()
	{
		return "[S]FE:39 ExShowScreenMessage";
	}
}