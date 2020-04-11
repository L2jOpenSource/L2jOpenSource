/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

public class ExShowScreenMessage2 extends L2GameServerPacket
{
	public static enum ScreenMessageAlign
	{
		TOP_LEFT,
		TOP_CENTER,
		TOP_RIGHT,
		MIDDLE_LEFT,
		MIDDLE_CENTER,
		MIDDLE_RIGHT,
		BOTTOM_CENTER,
		BOTTOM_RIGHT
	}
	
	private final int _type, _sysMessageId;
	private final boolean _big_font, _effect;
	private final ScreenMessageAlign _text_align;
	private final int _time;
	private final String _text;
	private final int _npcString;
	private final boolean _fade;
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align, boolean big_font)
	{
		_type = 1;
		_sysMessageId = -1;
		_fade = false;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = false;
		_npcString = -1;
	}
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align)
	{
		this(text, time, text_align, true);
	}
	
	public ExShowScreenMessage2(String text, int time)
	{
		this(text, time, ScreenMessageAlign.MIDDLE_CENTER);
	}
	
	public ExShowScreenMessage2(String text, int time, ScreenMessageAlign text_align, boolean big_font, int type, int messageId, boolean showEffect)
	{
		_type = type;
		_sysMessageId = messageId;
		_fade = false;
		_text = text;
		_time = time;
		_text_align = text_align;
		_big_font = big_font;
		_effect = showEffect;
		_npcString = -1;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x39);
		writeD(_type); // 0 - system messages, 1 - your defined text
		writeD(_sysMessageId); // system message id (_type must be 0 otherwise no effect)
		writeD(_text_align.ordinal() + 1); // placement of the text
		writeD(0x00); // ?
		writeD(_big_font ? 0 : 1); // text size
		writeD(0x00); // ?
		writeD(0x00); // ?
		writeD(_effect == true ? 1 : 0); // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
		writeD(_time); // the message is displayed in milliseconds
		writeD(_fade ? 0x01 : 0x00);
		writeD(_npcString);
		writeS(_text); // text messages
	}
}