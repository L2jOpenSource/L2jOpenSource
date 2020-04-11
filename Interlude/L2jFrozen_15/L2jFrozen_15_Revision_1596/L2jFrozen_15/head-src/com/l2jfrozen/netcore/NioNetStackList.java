package com.l2jfrozen.netcore;

/**
 * @author Forsaiken
 * @param  <E>
 */
public final class NioNetStackList<E>
{
	private final NioNetStackNode start = new NioNetStackNode();
	
	private final NioNetStackNodeBuf buf = new NioNetStackNodeBuf();
	
	private NioNetStackNode end = new NioNetStackNode();
	
	public NioNetStackList()
	{
		clear();
	}
	
	public final void addLast(final E elem)
	{
		final NioNetStackNode newEndNode = buf.removeFirst();
		end.value = elem;
		end.next = newEndNode;
		end = newEndNode;
	}
	
	public final E removeFirst()
	{
		final NioNetStackNode old = start.next;
		final E value = old.value;
		start.next = old.next;
		buf.addLast(old);
		return value;
	}
	
	public final boolean isEmpty()
	{
		return start.next == end;
	}
	
	public final void clear()
	{
		start.next = end;
	}
	
	protected final class NioNetStackNode
	{
		protected NioNetStackNode next;
		
		protected E value;
	}
	
	private final class NioNetStackNodeBuf
	{
		private final NioNetStackNode startNode = new NioNetStackNode();
		
		private NioNetStackNode endNode = new NioNetStackNode();
		
		NioNetStackNodeBuf()
		{
			startNode.next = endNode;
		}
		
		final void addLast(final NioNetStackNode node)
		{
			node.next = null;
			node.value = null;
			endNode.next = node;
			endNode = node;
		}
		
		final NioNetStackNode removeFirst()
		{
			if (startNode.next == endNode)
			{
				return new NioNetStackNode();
			}
			
			final NioNetStackNode old = startNode.next;
			startNode.next = old.next;
			return old;
		}
	}
}