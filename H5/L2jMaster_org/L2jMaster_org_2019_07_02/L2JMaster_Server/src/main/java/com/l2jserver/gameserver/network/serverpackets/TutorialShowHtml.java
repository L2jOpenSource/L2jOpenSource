/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.enums.HtmlActionScope;

/**
 * TutorialShowHtml server packet implementation.
 * @author HorridoJoho
 */
public final class TutorialShowHtml extends AbstractHtmlPacket
{
	public TutorialShowHtml(String html)
	{
		super(html);
	}
	
	/**
	 * This constructor is just here to be able to show a tutorial html<br>
	 * window bound to an npc.
	 * @param npcObjId
	 * @param html
	 */
	public TutorialShowHtml(int npcObjId, String html)
	{
		super(npcObjId, html);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xA6);
		writeS(getHtml());
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return HtmlActionScope.TUTORIAL_HTML;
	}
}