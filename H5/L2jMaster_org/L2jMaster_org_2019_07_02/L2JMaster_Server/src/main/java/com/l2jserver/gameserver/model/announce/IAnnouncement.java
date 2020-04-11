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
package com.l2jserver.gameserver.model.announce;

import com.l2jserver.gameserver.model.interfaces.IDeletable;
import com.l2jserver.gameserver.model.interfaces.IStorable;
import com.l2jserver.gameserver.model.interfaces.IUpdatable;

/**
 * @author UnAfraid
 */
public interface IAnnouncement extends IStorable, IUpdatable, IDeletable
{
	public int getId();
	
	public AnnouncementType getType();
	
	public void setType(AnnouncementType type);
	
	public boolean isValid();
	
	public String getContent();
	
	public void setContent(String content);
	
	public String getAuthor();
	
	public void setAuthor(String author);
}
