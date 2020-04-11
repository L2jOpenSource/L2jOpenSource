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

import java.util.regex.Matcher;

import l2r.Config;
import l2r.gameserver.enums.HtmlActionScope;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.imageGeneratorEngine.GlobalImagesCache;

/**
 * NpcHtmlMessage server packet implementation.
 * @author HorridoJoho
 */
public final class NpcHtmlMessage extends AbstractHtmlPacket
{
	private final int _itemId;
	
	public NpcHtmlMessage()
	{
		_itemId = 0;
	}
	
	public NpcHtmlMessage(int npcObjId)
	{
		super(npcObjId);
		_itemId = 0;
	}
	
	public NpcHtmlMessage(String html)
	{
		super(html);
		_itemId = 0;
	}
	
	public NpcHtmlMessage(int npcObjId, String html)
	{
		super(npcObjId, html);
		_itemId = 0;
	}
	
	public NpcHtmlMessage(int npcObjId, int itemId)
	{
		super(npcObjId);
		
		if (itemId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_itemId = itemId;
	}
	
	public NpcHtmlMessage(int npcObjId, int itemId, String html)
	{
		super(npcObjId, html);
		
		if (itemId < 0)
		{
			throw new IllegalArgumentException();
		}
		
		_itemId = itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		writeC(0x19);
		
		String html = getHtml();
		
		Matcher m = GlobalImagesCache.HTML_PATTERN.matcher(html);
		while (m.find())
		{
			String imageName = m.group(1);
			int imageId = GlobalImagesCache.getInstance().getImageId(imageName);
			html = html.replaceAll("%image:" + imageName + "%", "Crest.crest_" + Config.SERVER_ID + "_" + imageId);
		}
		
		GlobalImagesCache.getInstance().sendUsedImages(html, player);
		
		writeD(getNpcObjId());
		writeS(html);
		writeD(_itemId);
	}
	
	@Override
	public HtmlActionScope getScope()
	{
		return _itemId == 0 ? HtmlActionScope.NPC_HTML : HtmlActionScope.NPC_ITEM_HTML;
	}
}
