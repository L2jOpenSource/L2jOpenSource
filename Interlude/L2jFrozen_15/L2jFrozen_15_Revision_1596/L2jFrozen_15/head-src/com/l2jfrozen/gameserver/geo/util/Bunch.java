package com.l2jfrozen.gameserver.geo.util;

import java.util.List;

import com.l2jfrozen.gameserver.geo.util.L2Collections.Filter;

public interface Bunch<E>
{
	public int size();
	
	public Bunch<E> add(E value);
	
	public Bunch<E> remove(E value);
	
	public void clear();
	
	public boolean isEmpty();
	
	public E get(int index);
	
	public E set(int index, E value);
	
	public E remove(int index);
	
	public boolean contains(E value);
	
	public Bunch<E> addAll(Iterable<? extends E> c);
	
	public Bunch<E> addAll(E[] array);
	
	public Object[] moveToArray();
	
	public <T> T[] moveToArray(T[] array);
	
	public <T> T[] moveToArray(Class<T> clazz);
	
	public List<E> moveToList(List<E> list);
	
	public Bunch<E> cleanByFilter(Filter<E> filter);
}
