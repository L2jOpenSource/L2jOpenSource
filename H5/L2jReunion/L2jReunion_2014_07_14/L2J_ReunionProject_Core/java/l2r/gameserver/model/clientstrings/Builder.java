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
package l2r.gameserver.model.clientstrings;

import java.util.ArrayList;

/**
 * @author Forsaiken, Zoey76
 */
public abstract class Builder
{
	public abstract String toString(final Object param);
	
	public abstract String toString(final Object... params);
	
	public abstract int getIndex();
	
	public static final Builder newBuilder(final String text)
	{
		final ArrayList<Builder> builders = new ArrayList<>();
		
		int index1 = 0, index2 = 0, paramId, subTextLen;
		
		final char[] array = text.toCharArray();
		final int arrayLength = array.length;
		
		char c, c2, c3;
		LOOP:
		for (; index1 < arrayLength; index1++)
		{
			c = array[index1];
			if ((c == '$') && (index1 < (arrayLength - 2)))
			{
				c2 = array[index1 + 1];
				if ((c2 == 'c') || (c2 == 's') || (c2 == 'p') || (c2 == 'C') || (c2 == 'S') || (c2 == 'P'))
				{
					c3 = array[index1 + 2];
					if (Character.isDigit(c3))
					{
						paramId = Character.getNumericValue(c3);
						subTextLen = index1 - index2;
						if (subTextLen != 0)
						{
							builders.add(new BuilderText(new String(array, index2, subTextLen)));
						}
						
						builders.add(new BuilderObject(paramId));
						index1 += 2;
						index2 = index1 + 1;
						continue LOOP;
					}
				}
			}
		}
		
		if (arrayLength >= index1)
		{
			subTextLen = index1 - index2;
			if (subTextLen != 0)
			{
				builders.add(new BuilderText(new String(array, index2, subTextLen)));
			}
		}
		
		if (builders.size() == 1)
		{
			return builders.get(0);
		}
		return new BuilderContainer(builders.toArray(new Builder[builders.size()]));
	}
}