package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.SayType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final SayType _textType;
	
	private String _charName;
	private String _text;
	
	private int _sysStringId; // from sysstring-e.dat
	private int _systemMessageId; // from systemmsg-e.dat
	
	/**
	 * The {@link Creature} says a message.<br>
	 * Display a {@link Creature}'s name. Show message above the {@link Creature} instance's head.
	 * @param creature : Telling {@link Creature}.
	 * @param type : Type of message.
	 * @param text : The message.
	 */
	public CreatureSay(Creature creature, SayType type, String text)
	{
		_objectId = creature.getObjectId();
		_textType = type;
		_charName = creature.getName();
		_text = text;
	}
	
	/**
	 * Announcement of a message.<br>
	 * Displays a defined character name.
	 * @param type : Type of message.
	 * @param charName : The name of char to be displayed in front of message.
	 * @param text : The message.
	 */
	public CreatureSay(SayType type, String charName, String text)
	{
		_objectId = 0;
		_textType = type;
		_charName = charName;
		_text = text;
	}
	
	/**
	 * A character says a message.<br>
	 * Display a defined character name. Show message above the {@link Creature} instance's head.
	 * @param objectId : The character instance, saying a message.
	 * @param type : Type of message.
	 * @param charName : The name of char to be displayed in front of message.
	 * @param text : The message.
	 */
	public CreatureSay(int objectId, SayType type, String charName, String text)
	{
		_objectId = objectId;
		_textType = type;
		_charName = charName;
		_text = text;
	}
	
	/**
	 * Announces a boat message.
	 * @param type : Type of message.
	 * @param sysStringId : The client's sysString ID (see sysstring-e.dat).
	 * @param sysString : The {@link SystemMessageId} to be shown.
	 */
	public CreatureSay(SayType type, int sysStringId, SystemMessageId sysString)
	{
		_objectId = 0;
		_textType = type;
		_sysStringId = sysStringId;
		_systemMessageId = sysString.getId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);
		writeD(_objectId);
		writeD(_textType.ordinal());
		if (_text != null)
		{
			writeS(_charName);
			writeS(_text);
		}
		else
		{
			writeD(_sysStringId);
			writeD(_systemMessageId);
		}
	}
}