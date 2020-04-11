package com.l2jfrozen.gameserver.geo.pathfinding.utils;

import java.util.Arrays;

import com.l2jfrozen.gameserver.geo.pathfinding.Node;
import com.l2jfrozen.gameserver.geo.util.L2FastSet;
import com.l2jfrozen.gameserver.geo.util.ObjectPool;

public final class BinaryNodeHeap
{
	protected final Node[] list = new Node[800 + 1];
	protected final L2FastSet<Node> set = new L2FastSet<>();
	protected int size = 0;
	
	protected BinaryNodeHeap()
	{
		
	}
	
	public void add(final Node n)
	{
		size++;
		int pos = size;
		list[pos] = n;
		set.add(n);
		while (pos != 1)
		{
			final int p2 = pos / 2;
			if (list[pos].getCost() <= list[p2].getCost())
			{
				final Node temp = list[p2];
				list[p2] = list[pos];
				list[pos] = temp;
				pos = p2;
			}
			else
			{
				break;
			}
		}
	}
	
	public Node removeFirst()
	{
		final Node first = list[1];
		list[1] = list[size];
		list[size] = null;
		size--;
		int pos = 1;
		int cpos;
		int dblcpos;
		Node temp;
		while (true)
		{
			cpos = pos;
			dblcpos = cpos * 2;
			if ((dblcpos + 1) <= size)
			{
				if (list[cpos].getCost() >= list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
				if (list[pos].getCost() >= list[dblcpos + 1].getCost())
				{
					pos = dblcpos + 1;
				}
			}
			else if (dblcpos <= size)
			{
				if (list[cpos].getCost() >= list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
			}
			
			if (cpos != pos)
			{
				temp = list[cpos];
				list[cpos] = list[pos];
				list[pos] = temp;
			}
			else
			{
				break;
			}
		}
		set.remove(first);
		return first;
	}
	
	public boolean contains(final Node n)
	{
		if (size == 0)
		{
			return false;
		}
		
		return set.contains(n);
	}
	
	public boolean isEmpty()
	{
		return size == 0;
	}
	
	public static BinaryNodeHeap newInstance()
	{
		return POOL.get();
	}
	
	public static void recycle(final BinaryNodeHeap heap)
	{
		POOL.store(heap);
	}
	
	private static final ObjectPool<BinaryNodeHeap> POOL = new ObjectPool<BinaryNodeHeap>()
	{
		@Override
		protected void reset(final BinaryNodeHeap heap)
		{
			Arrays.fill(heap.list, null);
			heap.set.clear();
			heap.size = 0;
		}
		
		@Override
		protected BinaryNodeHeap create()
		{
			return new BinaryNodeHeap();
		}
	};
}
