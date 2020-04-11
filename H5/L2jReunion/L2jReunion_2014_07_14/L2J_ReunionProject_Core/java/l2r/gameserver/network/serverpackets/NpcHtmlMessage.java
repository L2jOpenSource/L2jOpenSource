/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.serverpackets;

import l2r.Config;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * <pre>
 * the HTML parser in the client knowns these standard and non-standard tags and attributes
 * VOLUMN
 * UNKNOWN
 * UL
 * U
 * TT
 * TR
 * TITLE
 * TEXTCODE
 * TEXTAREA
 * TD
 * TABLE
 * SUP
 * SUB
 * STRIKE
 * SPIN
 * SELECT
 * RIGHT
 * PRE
 * P
 * OPTION
 * OL
 * MULTIEDIT
 * LI
 * LEFT
 * INPUT
 * IMG
 * I
 * HTML
 * H7
 * H6
 * H5
 * H4
 * H3
 * H2
 * H1
 * FONT
 * EXTEND
 * EDIT
 * COMMENT
 * COMBOBOX
 * CENTER
 * BUTTON
 * BR
 * BR1
 * BODY
 * BAR
 * ADDRESS
 * A
 * SEL
 * LIST
 * VAR
 * FORE
 * READONL
 * ROWS
 * VALIGN
 * FIXWIDTH
 * BORDERCOLORLI
 * BORDERCOLORDA
 * BORDERCOLOR
 * BORDER
 * BGCOLOR
 * BACKGROUND
 * ALIGN
 * VALU
 * READONLY
 * MULTIPLE
 * SELECTED
 * TYP
 * TYPE
 * MAXLENGTH
 * CHECKED
 * SRC
 * Y
 * X
 * QUERYDELAY
 * NOSCROLLBAR
 * IMGSRC
 * B
 * FG
 * SIZE
 * FACE
 * COLOR
 * DEFFON
 * DEFFIXEDFONT
 * WIDTH
 * VALUE
 * TOOLTIP
 * NAME
 * MIN
 * MAX
 * HEIGHT
 * DISABLED
 * ALIGN
 * MSG
 * LINK
 * HREF
 * ACTION
 * </pre>
 */
public final class NpcHtmlMessage extends L2GameServerPacket
{
	private final int _npcObjId;
	private String _html;
	private int _itemId = 0;
	private boolean _validate = true;
	
	/**
	 * @param npcObjId
	 * @param itemId
	 */
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
	}
	
	/**
	 * @param npcObjId
	 * @param itemId
	 * @param text
	 */
	public NpcHtmlMessage(int npcObjId, int itemId, String text)
	{
		_npcObjId = npcObjId;
		_itemId = itemId;
		_html = text;
	}
	
	/**
	 * @param npcObjId
	 * @param text
	 */
	public NpcHtmlMessage(int npcObjId, String text)
	{
		_npcObjId = npcObjId;
		setHtml(text);
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		_npcObjId = npcObjId;
	}
	
	public NpcHtmlMessage(String html)
	{
		_npcObjId = 0;
		_itemId = 0;
		setHtml(html);
	}
	
	/**
	 * disable building bypass validation cache for this packet
	 */
	public void disableValidation()
	{
		_validate = false;
	}
	
	@Override
	public void runImpl()
	{
		if (Config.BYPASS_VALIDATION && _validate)
		{
			buildBypassCache(getClient().getActiveChar());
		}
	}
	
	public void setHtml(String text)
	{
		if (text.length() > 17200)
		{
			_log.warn("Html is too long! this will crash the client!", new Throwable());
			text = "<html><body>Html is too long...<br>Report this to Pegasus team!</body></html>";
		}
		
		if (!text.contains("<html"))
		{
			text = "<html><body>" + text + "</body></html>";
		}
		
		_html = text;
	}
	
	public boolean setFile(String prefix, String path)
	{
		String content = HtmCache.getInstance().getHtm(prefix, path);
		
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warn("missing html page " + path);
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	public boolean setFile(String path)
	{
		return setFile(null, path);
	}
	
	public final void replace(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
	}
	
	public final void replace(String pattern, boolean val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, int val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, long val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public final void replace(String pattern, double val)
	{
		replace(pattern, String.valueOf(val));
	}
	
	public void replace2(String pattern, String value)
	{
		_html = _html.replaceAll(pattern, value);
	}
	
	public boolean setEventHtml(String path)
	{
		String content = HtmCache.getInstance().getHtm(path);
		
		if (content == null)
		{
			return false;
		}
		
		setHtml(content);
		return true;
	}
	
	public String getText()
	{
		return _html;
	}
	
	public final String getHtml()
	{
		return _html;
	}
	
	private final void buildBypassCache(L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.clearBypass();
		int len = _html.length();
		for (int i = 0; i < len; i++)
		{
			int start = _html.indexOf("\"bypass ", i);
			int finish = _html.indexOf("\"", start + 1);
			if ((start < 0) || (finish < 0))
			{
				break;
			}
			
			if (_html.substring(start + 8, start + 10).equals("-h"))
			{
				start += 11;
			}
			else
			{
				start += 8;
			}
			
			i = finish;
			int finish2 = _html.indexOf("$", start);
			if ((finish2 < finish) && (finish2 > 0))
			{
				activeChar.addBypass2(_html.substring(start, finish2).trim());
			}
			else
			{
				activeChar.addBypass(_html.substring(start, finish).trim());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x19);
		
		writeD(_npcObjId);
		writeS(_html);
		if (_npcObjId != 0)
		{
			writeD(_itemId);
		}
	}
}
