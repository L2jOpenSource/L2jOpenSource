/*
 * Copyright (C) 2004-2015 L2J Server
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

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.enums.HtmlActionScope;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.util.Util;

/**
 * @author HorridoJoho
 */
public abstract class AbstractHtmlPacket extends L2GameServerPacket
{
	public static final char VAR_PARAM_START_CHAR = '$';
	
	private final int _npcObjId;
	private String _html = null;
	private boolean _disabledValidation = false;
	
	protected AbstractHtmlPacket()
	{
		_npcObjId = 0;
	}
	
	protected AbstractHtmlPacket(int npcObjId)
	{
		if (npcObjId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_npcObjId = npcObjId;
	}
	
	protected AbstractHtmlPacket(String html)
	{
		_npcObjId = 0;
		setHtml(html);
	}
	
	protected AbstractHtmlPacket(int npcObjId, String html)
	{
		if (npcObjId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_npcObjId = npcObjId;
		setHtml(html);
	}
	
	public final void disableValidation()
	{
		_disabledValidation = true;
	}
	
	public final void setHtml(String html)
	{
		if (html.length() > 17200)
		{
			_log.warn("Html is too long! this will crash the client!");
			_html = html.substring(0, 17199);
		}
		
		if (!html.contains("<html"))
		{
			html = "<html><body>" + html + "</body></html>";
		}
		
		_html = html;
	}
	
	public final boolean setFile(L2PcInstance activeChar, String prefix, String path)
	{
		String content = HtmCache.getInstance().getHtm(activeChar, prefix, path);
		if (content == null)
		{
			setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
			_log.warn("missing html page " + path);
			return false;
		}
		
		setHtml(content);
		return true;
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
	
	@Override
	public final void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		player.clearHtmlActions(getScope());
		
		if (_disabledValidation)
		{
			return;
		}
		
		Util.buildHtmlActionCache(player, getScope(), _npcObjId, _html);
	}
	
	public final int getNpcObjId()
	{
		return _npcObjId;
	}
	
	public final String getHtml()
	{
		return _html;
	}
	
	public abstract HtmlActionScope getScope();
}
