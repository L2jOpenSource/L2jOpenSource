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
package com.l2jserver.gameserver.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.L2Item;

/**
 * Prime Shop data values.
 * @author U3Games
 */
public final class PrimeShop
{
	private final int _productId;
	private final int _category;
	private final int _points;
	private final int _item;
	private final int _count;
	
	private final int _weight;
	private final boolean _tradable;
	
	private long _sale_start_date;
	private long _sale_end_date;
	private int _startHour = 0;
	private int _startMin = 0;
	private int _endHour = 0;
	private int _endMin = 0;
	
	public PrimeShop(StatsSet set)
	{
		_productId = set.getInt("id");
		_category = set.getInt("category");
		_points = set.getInt("points");
		_item = set.getInt("item");
		_count = set.getInt("count");
		
		final L2Item itemTemplate = ItemTable.getInstance().getTemplate(_item);
		if (itemTemplate != null)
		{
			_weight = itemTemplate.getWeight();
			_tradable = itemTemplate.isTradeable();
		}
		else
		{
			_weight = 0;
			_tradable = true;
		}
		
		final DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		try
		{
			final Calendar calendar = Calendar.getInstance();
			final Date time_start = df.parse(set.getString("sale_start_date"));
			final Date time_end = df.parse(set.getString("sale_end_date"));
			
			// Start Time
			calendar.setTime(time_start);
			_sale_start_date = time_start.getTime();
			_startHour = calendar.get(Calendar.HOUR_OF_DAY);
			_startMin = calendar.get(Calendar.MINUTE);
			
			// End Time
			calendar.setTime(time_end);
			_sale_end_date = time_end.getTime();
			_endHour = calendar.get(Calendar.HOUR_OF_DAY);
			_endMin = calendar.get(Calendar.MINUTE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getProductId()
	{
		return _productId;
	}
	
	public int getCategory()
	{
		return _category;
	}
	
	public int getPrice()
	{
		return _points;
	}
	
	public int getItemId()
	{
		return _item;
	}
	
	public int getItemCount()
	{
		return _count;
	}
	
	public int getItemWeight()
	{
		return _weight;
	}
	
	public boolean isTradable()
	{
		return _tradable;
	}
	
	public long sale_start_date()
	{
		return _sale_start_date;
	}
	
	public long sale_end_date()
	{
		return _sale_end_date;
	}
	
	public int getStartHour()
	{
		return _startHour;
	}
	
	public int getStartMin()
	{
		return _startMin;
	}
	
	public int getEndHour()
	{
		return _endHour;
	}
	
	public int getEndMin()
	{
		return _endMin;
	}
}