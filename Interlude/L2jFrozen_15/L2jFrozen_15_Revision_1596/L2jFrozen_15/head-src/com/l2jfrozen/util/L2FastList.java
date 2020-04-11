package com.l2jfrozen.util;

import javolution.util.FastList;

/**
 * @author Julian
 * @param  <T>
 */
public class L2FastList<T> extends FastList<T>
{
	static final long serialVersionUID = 1L;
	
	public interface I2ForEach<T>
	{
		public boolean ForEach(T obj);
		
		public FastList.Node<T> getNext(FastList.Node<T> priv);
	}
	
	public final boolean forEach(final I2ForEach<T> func, final boolean sync)
	{
		if (sync)
		{
			synchronized (this)
			{
				return forEachP(func);
			}
		}
		return forEachP(func);
	}
	
	private boolean forEachP(final I2ForEach<T> func)
	{
		for (FastList.Node<T> e = head(), end = tail(); (e = func.getNext(e)) != end;)
		{
			if (!func.ForEach(e.getValue()))
			{
				return false;
			}
		}
		return true;
	}
}
