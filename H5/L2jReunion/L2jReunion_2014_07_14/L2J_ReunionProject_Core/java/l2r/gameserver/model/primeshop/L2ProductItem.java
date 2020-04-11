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
package l2r.gameserver.model.primeshop;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by GodFather
 */
public class L2ProductItem
{
	public static final long NOT_LIMITED_START_TIME = 315547200000L;
	public static final long NOT_LIMITED_END_TIME = 2127445200000L;
	public static final int NOT_LIMITED_START_HOUR = 0;
	public static final int NOT_LIMITED_END_HOUR = 23;
	public static final int NOT_LIMITED_START_MIN = 0;
	public static final int NOT_LIMITED_END_MIN = 59;
	
	private final int _productId;
	private final int _category;
	private final int _points;
	private final int _tabId;
	
	private final long _startTimeSale;
	private final long _endTimeSale;
	private final int _startHour;
	private final int _endHour;
	private final int _startMin;
	private final int _endMin;
	
	private ArrayList<L2ProductItemComponent> _components;
	
	public L2ProductItem(int productId, int category, int points, int tabId, long startTimeSale, long endTimeSale)
	{
		_productId = productId;
		_category = category;
		_points = points;
		_tabId = tabId;
		
		Calendar calendar;
		if (startTimeSale > 0)
		{
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(startTimeSale);
			
			_startTimeSale = startTimeSale;
			_startHour = calendar.get(Calendar.HOUR_OF_DAY);
			_startMin = calendar.get(Calendar.MINUTE);
		}
		else
		{
			_startTimeSale = NOT_LIMITED_START_TIME;
			_startHour = NOT_LIMITED_START_HOUR;
			_startMin = NOT_LIMITED_START_MIN;
		}
		
		if (endTimeSale > 0)
		{
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(endTimeSale);
			
			_endTimeSale = endTimeSale;
			_endHour = calendar.get(Calendar.HOUR_OF_DAY);
			_endMin = calendar.get(Calendar.MINUTE);
		}
		else
		{
			_endTimeSale = NOT_LIMITED_END_TIME;
			_endHour = NOT_LIMITED_END_HOUR;
			_endMin = NOT_LIMITED_END_MIN;
		}
	}
	
	public void setComponents(ArrayList<L2ProductItemComponent> a)
	{
		_components = a;
	}
	
	public ArrayList<L2ProductItemComponent> getComponents()
	{
		if (_components == null)
		{
			_components = new ArrayList<>();
		}
		return _components;
	}
	
	public int getProductId()
	{
		return _productId;
	}
	
	public int getCategory()
	{
		return _category;
	}
	
	public int getPoints()
	{
		return _points;
	}
	
	public int getTabId()
	{
		return _tabId;
	}
	
	public long getStartTimeSale()
	{
		return _startTimeSale;
	}
	
	public int getStartHour()
	{
		return _startHour;
	}
	
	public int getStartMin()
	{
		return _startMin;
	}
	
	public long getEndTimeSale()
	{
		return _endTimeSale;
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