package com.l2jfrozen.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.model.L2Object;

public class DesireTable
{
	public static final DesireType[] DEFAULT_DESIRES =
	{
		DesireType.FEAR,
		DesireType.DISLIKE,
		DesireType.HATE,
		DesireType.DAMAGE
	};
	
	public enum DesireType
	{
		FEAR,
		DISLIKE,
		HATE,
		DAMAGE
	}
	
	class DesireValue
	{
		private float value;
		
		DesireValue()
		{
			this(0f);
		}
		
		DesireValue(final Float pValue)
		{
			value = pValue;
		}
		
		public void addValue(final float pValue)
		{
			value += pValue;
		}
		
		public float getValue()
		{
			return value;
		}
	}
	
	class Desires
	{
		private final Map<DesireType, DesireValue> desireTable;
		
		public Desires(final DesireType... desireList)
		{
			desireTable = new HashMap<>();
			
			for (DesireType desire : desireList)
			{
				desireTable.put(desire, new DesireValue());
			}
		}
		
		public DesireValue getDesireValue(final DesireType type)
		{
			return desireTable.get(type);
		}
		
		public void addValue(final DesireType type, final float value)
		{
			DesireValue temp = getDesireValue(type);
			
			if (temp != null)
			{
				temp.addValue(value);
			}
			
			temp = null;
		}
		
		public void createDesire(final DesireType type)
		{
			desireTable.put(type, new DesireValue());
		}
		
		public void deleteDesire(final DesireType type)
		{
			desireTable.remove(type);
		}
	}
	
	private final Map<L2Object, Desires> objectDesireTable;
	private final Desires generalDesires;
	private final DesireType[] desireTypes;
	
	public DesireTable(final DesireType... desireList)
	{
		desireTypes = desireList;
		objectDesireTable = new HashMap<>();
		generalDesires = new Desires(desireTypes);
	}
	
	public float getDesireValue(final DesireType type)
	{
		return generalDesires.getDesireValue(type).getValue();
	}
	
	public float getDesireValue(final L2Object object, final DesireType type)
	{
		final Desires desireList = objectDesireTable.get(object);
		
		if (desireList == null)
		{
			return 0f;
		}
		
		return desireList.getDesireValue(type).getValue();
	}
	
	public void addDesireValue(final DesireType type, final float value)
	{
		generalDesires.addValue(type, value);
	}
	
	public void addDesireValue(final L2Object object, final DesireType type, final float value)
	{
		Desires desireList = objectDesireTable.get(object);
		
		if (desireList != null)
		{
			desireList.addValue(type, value);
		}
		
		desireList = null;
	}
	
	public void createDesire(final DesireType type)
	{
		generalDesires.createDesire(type);
	}
	
	public void deleteDesire(final DesireType type)
	{
		generalDesires.deleteDesire(type);
	}
	
	public void createDesire(final L2Object object, final DesireType type)
	{
		Desires desireList = objectDesireTable.get(object);
		
		if (desireList != null)
		{
			desireList.createDesire(type);
		}
		
		desireList = null;
	}
	
	public void deleteDesire(final L2Object object, final DesireType type)
	{
		Desires desireList = objectDesireTable.get(object);
		
		if (desireList != null)
		{
			desireList.deleteDesire(type);
		}
		
		desireList = null;
	}
	
	public void addKnownObject(final L2Object object)
	{
		if (object != null)
		{
			addKnownObject(object, DesireType.DISLIKE, DesireType.FEAR, DesireType.DAMAGE, DesireType.HATE);
		}
	}
	
	public void addKnownObject(final L2Object object, final DesireType... desireList)
	{
		if (object != null)
		{
			objectDesireTable.put(object, new Desires(desireList));
		}
	}
}
