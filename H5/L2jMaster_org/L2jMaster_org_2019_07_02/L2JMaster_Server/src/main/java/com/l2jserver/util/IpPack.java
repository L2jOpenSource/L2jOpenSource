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
package com.l2jserver.util;

import java.util.Arrays;

public final class IpPack
{
	String ip;
	int[][] tracert;
	
	public IpPack(String ip, int[][] tracert)
	{
		this.ip = ip;
		this.tracert = tracert;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((ip == null) ? 0 : ip.hashCode());
		if (tracert == null)
		{
			return result;
		}
		
		for (int[] array : tracert)
		{
			result = (prime * result) + Arrays.hashCode(array);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		IpPack other = (IpPack) obj;
		if (ip == null)
		{
			if (other.ip != null)
			{
				return false;
			}
		}
		else if (!ip.equals(other.ip))
		{
			return false;
		}
		
		for (int i = 0; i < tracert.length; i++)
		{
			for (int o = 0; o < tracert[0].length; o++)
			{
				if (tracert[i][o] != other.tracert[i][o])
				{
					return false;
				}
			}
		}
		return true;
	}
}