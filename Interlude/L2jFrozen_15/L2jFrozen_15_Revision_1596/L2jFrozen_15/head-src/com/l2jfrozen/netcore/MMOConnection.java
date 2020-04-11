package com.l2jfrozen.netcore;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param  <T>
 * @author KenM
 */
public class MMOConnection<T extends MMOClient<?>>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(MMOConnection.class);
	private final SelectorThread<T> selectorThread;
	
	private final Socket socket;
	
	private final InputStream socket_is;
	
	private final InetAddress address;
	
	private final ReadableByteChannel readableByteChannel;
	
	private final WritableByteChannel writableByteChannel;
	
	private final int port;
	
	private final NioNetStackList<SendablePacket<T>> sendQueue;
	
	private final SelectionKey selectionKey;
	
	// private SendablePacket<T> closePacket;
	
	private ByteBuffer readBuffer;
	
	private ByteBuffer primaryWriteBuffer;
	
	private ByteBuffer secondaryWriteBuffer;
	
	private volatile boolean pendingClose;
	
	private T client;
	
	public MMOConnection(final SelectorThread<T> selectorThread, final Socket socket, final SelectionKey key) throws IOException
	{
		this.selectorThread = selectorThread;
		this.socket = socket;
		address = socket.getInetAddress();
		readableByteChannel = socket.getChannel();
		writableByteChannel = socket.getChannel();
		
		socket_is = socket.getInputStream();
		
		port = socket.getPort();
		selectionKey = key;
		
		sendQueue = new NioNetStackList<>();
	}
	
	final void setClient(final T client)
	{
		this.client = client;
	}
	
	public final T getClient()
	{
		return client;
	}
	
	public final void sendPacket(final SendablePacket<T> sp)
	{
		sp.client = client;
		
		if (pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			sendQueue.addLast(sp);
		}
		
		if (!sendQueue.isEmpty() && selectionKey.isValid())
		{
			try
			{
				selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
			}
			catch (final CancelledKeyException e)
			{
				LOGGER.warn("", e);
				
			}
		}
	}
	
	final SelectionKey getSelectionKey()
	{
		return selectionKey;
	}
	
	public final InetAddress getInetAddress()
	{
		return address;
	}
	
	public final int getPort()
	{
		return port;
	}
	
	final void close() throws IOException
	{
		socket.close();
	}
	
	final int read(final ByteBuffer buf) throws IOException
	{
		if (/*
			 * !isClosed() &&
			 */readableByteChannel != null && readableByteChannel.isOpen() && !socket.isInputShutdown())
		// && !_socket.isOutputShutdown())
		{
			return readableByteChannel.read(buf);
		}
		return -1;
	}
	
	final int write(final ByteBuffer buf) throws IOException
	{
		if (writableByteChannel != null && writableByteChannel.isOpen() && !socket.isOutputShutdown())
		{
			return writableByteChannel.write(buf);
		}
		return -1;
	}
	
	final void createWriteBuffer(final ByteBuffer buf)
	{
		if (primaryWriteBuffer == null)
		{
			primaryWriteBuffer = selectorThread.getPooledBuffer();
			primaryWriteBuffer.put(buf);
		}
		else
		{
			final ByteBuffer temp = selectorThread.getPooledBuffer();
			temp.put(buf);
			
			final int remaining = temp.remaining();
			primaryWriteBuffer.flip();
			final int limit = primaryWriteBuffer.limit();
			
			if (remaining >= primaryWriteBuffer.remaining())
			{
				temp.put(primaryWriteBuffer);
				selectorThread.recycleBuffer(primaryWriteBuffer);
				primaryWriteBuffer = temp;
			}
			else
			{
				primaryWriteBuffer.limit(remaining);
				temp.put(primaryWriteBuffer);
				primaryWriteBuffer.limit(limit);
				primaryWriteBuffer.compact();
				secondaryWriteBuffer = primaryWriteBuffer;
				primaryWriteBuffer = temp;
			}
		}
	}
	
	final boolean hasPendingWriteBuffer()
	{
		return primaryWriteBuffer != null;
	}
	
	final void movePendingWriteBufferTo(final ByteBuffer dest)
	{
		primaryWriteBuffer.flip();
		dest.put(primaryWriteBuffer);
		selectorThread.recycleBuffer(primaryWriteBuffer);
		primaryWriteBuffer = secondaryWriteBuffer;
		secondaryWriteBuffer = null;
	}
	
	final void setReadBuffer(final ByteBuffer buf)
	{
		readBuffer = buf;
	}
	
	final ByteBuffer getReadBuffer()
	{
		return readBuffer;
	}
	
	public final boolean isConnected()
	{
		return !socket.isClosed() && socket.isConnected();
	}
	
	public final boolean isChannelConnected()
	{
		boolean output = false;
		
		if (!socket.isClosed() && socket.getChannel() != null && socket.getChannel().isConnected() && socket.getChannel().isOpen() && !socket.isInputShutdown())
		{
			
			try
			{
				if (socket_is.available() > 0)
				{
					output = true;
				}
			}
			catch (final IOException e)
			{
				LOGGER.error("unhandled exception", e);
			}
			
		}
		return output;
	}
	
	public final boolean isClosed()
	{
		return pendingClose;
	}
	
	final NioNetStackList<SendablePacket<T>> getSendQueue()
	{
		return sendQueue;
	}
	
	/*
	 * final SendablePacket<T> getClosePacket() { return closePacket; }
	 */
	
	@SuppressWarnings("unchecked")
	public final void close(final SendablePacket<T> sp)
	{
		close(new SendablePacket[]
		{
			sp
		});
	}
	
	public final void close(final SendablePacket<T>[] closeList)
	{
		if (pendingClose)
		{
			return;
		}
		
		synchronized (getSendQueue())
		{
			if (!pendingClose)
			{
				pendingClose = true;
				sendQueue.clear();
				for (final SendablePacket<T> sp : closeList)
				{
					sendQueue.addLast(sp);
				}
			}
		}
		
		if (selectionKey.isValid())
		{
			
			try
			{
				selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
			}
			catch (final CancelledKeyException e)
			{
				// not useful LOGGER
			}
			
		}
		
		if (NetcoreConfig.getInstance().DUMP_CLOSE_CONNECTIONS)
		{
			Thread.dumpStack();
		}
		// closePacket = sp;
		selectorThread.closeConnection(this);
	}
	
	final void releaseBuffers()
	{
		if (primaryWriteBuffer != null)
		{
			selectorThread.recycleBuffer(primaryWriteBuffer);
			primaryWriteBuffer = null;
			
			if (secondaryWriteBuffer != null)
			{
				selectorThread.recycleBuffer(secondaryWriteBuffer);
				secondaryWriteBuffer = null;
			}
		}
		
		if (readBuffer != null)
		{
			selectorThread.recycleBuffer(readBuffer);
			readBuffer = null;
		}
	}
}
