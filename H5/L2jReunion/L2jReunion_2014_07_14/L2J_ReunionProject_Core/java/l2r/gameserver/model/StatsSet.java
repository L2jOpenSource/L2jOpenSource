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
package l2r.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub <BR>
 *         This class is used in order to have a set of couples (key,value).<BR>
 *         Methods deployed are accessors to the set (add/get value from its key) and addition of a whole set in the current one.
 */
public class StatsSet
{
	private static final Logger _log = LoggerFactory.getLogger(StatsSet.class);
	private final Map<String, Object> _set;
	
	public StatsSet()
	{
		this(new FastMap<String, Object>());
	}
	
	public StatsSet(Map<String, Object> map)
	{
		_set = map;
	}
	
	/**
	 * Returns the set of values
	 * @return HashMap
	 */
	public final Map<String, Object> getSet()
	{
		return _set;
	}
	
	/**
	 * Add a set of couple values in the current set
	 * @param newSet : StatsSet pointing out the list of couples to add in the current set
	 */
	public void add(StatsSet newSet)
	{
		Map<String, Object> newMap = newSet.getSet();
		for (Entry<String, Object> entry : newMap.entrySet())
		{
			_set.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Return the boolean associated to the key put in parameter ("name")
	 * @param name : String designating the key in the set
	 * @return boolean : value associated to the key
	 */
	public boolean getBool(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	/**
	 * Return the boolean associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : boolean designating the default value if value associated with the key is null
	 * @return boolean : value of the key
	 */
	public boolean getBool(String name, boolean deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Boolean)
		{
			return ((Boolean) val).booleanValue();
		}
		try
		{
			return Boolean.parseBoolean((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Boolean value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the int associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : byte designating the default value if value associated with the key is null
	 * @return byte : value associated to the key
	 */
	public byte getByte(String name, byte deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the byte associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return byte : value associated to the key
	 */
	public byte getByte(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).byteValue();
		}
		try
		{
			return Byte.parseByte((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Byte value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the byte[] associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param splitOn
	 * @return byte[] : value associated to the key
	 */
	public byte[] getByteArray(String name, String splitOn)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Byte value required, but not specified");
		}
		if (val instanceof Number)
		{
			byte[] result =
			{
				((Number) val).byteValue()
			};
			return result;
		}
		int c = 0;
		String[] vals = ((String) val).split(splitOn);
		byte[] result = new byte[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Byte.parseByte(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Byte value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Byte> getByteList(String name, String splitOn)
	{
		List<Byte> result = new ArrayList<>();
		for (Byte i : getByteArray(name, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	/**
	 * Returns the short associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : short designating the default value if value associated with the key is null
	 * @return short : value associated to the key
	 */
	public short getShort(String name, short deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the short associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return short : value associated to the key
	 */
	public short getShort(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Short value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).shortValue();
		}
		try
		{
			return Short.parseShort((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Short value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the int associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return int : value associated to the key
	 */
	public int getInteger(String name)
	{
		final Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified: " + name + "!");
		}
		
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val + "!");
		}
	}
	
	/**
	 * Returns the int associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : int designating the default value if value associated with the key is null
	 * @return int : value associated to the key
	 */
	public int getInteger(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).intValue();
		}
		try
		{
			return Integer.parseInt((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the int[] associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param splitOn
	 * @return int[] : value associated to the key
	 */
	public int[] getIntegerArray(String name, String splitOn)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			int[] result =
			{
				((Number) val).intValue()
			};
			return result;
		}
		int c = 0;
		String[] vals = ((String) val).split(splitOn);
		int[] result = new int[vals.length];
		for (String v : vals)
		{
			try
			{
				result[c++] = Integer.parseInt(v);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
		return result;
	}
	
	public List<Integer> getIntegerList(String name, String splitOn)
	{
		List<Integer> result = new ArrayList<>();
		for (int i : getIntegerArray(name, splitOn))
		{
			result.add(i);
		}
		return result;
	}
	
	/**
	 * Returns the long associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return long : value associated to the key
	 */
	public long getLong(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Integer value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the long associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : long designating the default value if value associated with the key is null
	 * @return long : value associated to the key
	 */
	public long getLong(String name, int deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		try
		{
			return Long.parseLong((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Integer value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the float associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return float : value associated to the key
	 */
	public float getFloat(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Double value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Double value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the float associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : float designating the default value if value associated with the key is null
	 * @return float : value associated to the key
	 */
	public float getFloat(String name, float deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).floatValue();
		}
		try
		{
			return (float) Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the double associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return double : value associated to the key
	 */
	public double getDouble(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Float value required, but not specified");
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the double associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : float designating the default value if value associated with the key is null
	 * @return double : value associated to the key
	 */
	public double getDouble(String name, double deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (val instanceof Number)
		{
			return ((Number) val).doubleValue();
		}
		try
		{
			return Double.parseDouble((String) val);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Float value required, but found: " + val);
		}
	}
	
	/**
	 * Returns the String associated to the key put in parameter ("name").
	 * @param name : String designating the key in the set
	 * @return String : value associated to the key
	 */
	public String getString(String name)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("String value required, but not specified");
		}
		return String.valueOf(val);
	}
	
	/**
	 * Returns the String associated to the key put in parameter ("name"). If the value associated to the key is null, this method returns the value of the parameter deflt.
	 * @param name : String designating the key in the set
	 * @param deflt : String designating the default value if value associated with the key is null
	 * @return String : value associated to the key
	 */
	public String getString(String name, String deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		return String.valueOf(val);
	}
	
	/**
	 * Returns an enumeration of &lt;T&gt; from the set
	 * @param <T> : Class of the enumeration returned
	 * @param name : String designating the key in the set
	 * @param enumClass : Class designating the class of the value associated with the key in the set
	 * @return Enum<T>
	 */
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but not specified");
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + " required, but found: " + val);
		}
	}
	
	/**
	 * Returns an enumeration of &lt;T&gt; from the set. If the enumeration is empty, the method returns the value of the parameter "deflt".
	 * @param <T> : Class of the enumeration returned
	 * @param name : String designating the key in the set
	 * @param enumClass : Class designating the class of the value associated with the key in the set
	 * @param deflt : <T> designating the value by default
	 * @return Enum<T>
	 */
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass, T deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		if (enumClass.isInstance(val))
		{
			return (T) val;
		}
		try
		{
			return Enum.valueOf(enumClass, String.valueOf(val));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public final <A> A getObject(String name, Class<A> type)
	{
		Object obj = _set.get(name);
		if ((obj == null) || !type.isAssignableFrom(obj.getClass()))
		{
			return null;
		}
		
		return (A) obj;
	}
	
	public long getLong(String name, long deflt)
	{
		Object val = _set.get(name);
		if (val == null)
		{
			return deflt;
		}
		else if (val instanceof Number)
		{
			return ((Number) val).longValue();
		}
		else
		{
			try
			{
				return Long.parseLong((String) val);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Integer value required, but found: " + val);
			}
		}
	}
	
	public void set(String name, Object value)
	{
		_set.put(name, value);
	}
	
	public void set(String key, boolean value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, byte value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, short value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, int value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, long value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, float value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, double value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, String value)
	{
		_set.put(key, value);
	}
	
	public void set(String key, Enum<?> value)
	{
		_set.put(key, value);
	}
	
	public void safeSet(String key, int value, int min, int max, String reference)
	{
		assert !(((min <= max) && ((value < min) || (value >= max))));
		if ((min <= max) && ((value < min) || (value >= max)))
		{
			_log.error("Incorrect value: " + value + "for: " + key + "Ref: " + reference);
		}
		
		set(key, value);
	}
	
	public void unset(String name)
	{
		_set.remove(name);
	}
}
