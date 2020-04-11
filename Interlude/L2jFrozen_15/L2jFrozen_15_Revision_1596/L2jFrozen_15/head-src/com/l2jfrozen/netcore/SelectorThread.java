package com.l2jfrozen.netcore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javolution.util.FastList;

/**
 * @param  <T>
 * @author KenM<BR>
 *         Parts of design based on networkcore from WoodenGil
 */
public final class SelectorThread<T extends MMOClient<?>> extends Thread
{
	private static Logger LOGGER = LoggerFactory.getLogger(SelectorThread.class);
	// default BYTE_ORDER
	private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	// default HEADER_SIZE
	private static final int HEADER_SIZE = 2;
	// Selector
	private final Selector selector;
	// Implementations
	private final IPacketHandler<T> packetHandler;
	private final IMMOExecutor<T> executor;
	private final IClientFactory<T> clientFactory;
	private final IAcceptFilter acceptFilter;
	// Configurations
	private final int HELPER_BUFFER_SIZE;
	private final int HELPER_BUFFER_COUNT;
	private final int MAX_SEND_PER_PASS;
	private final int MAX_READ_PER_PASS;
	private final long SLEEP_TIME;
	// Main Buffers
	private final ByteBuffer DIRECT_WRITE_BUFFER;
	private final ByteBuffer WRITE_BUFFER;
	private final ByteBuffer READ_BUFFER;
	// String Buffer
	private final NioNetStringBuffer STRING_BUFFER;
	// ByteBuffers General Purpose Pool
	private final FastList<ByteBuffer> bufferPool;
	// Pending Close
	private final NioNetStackList<MMOConnection<T>> pendingClose;
	
	private boolean shutdown;
	
	public SelectorThread(final SelectorConfig sc, final IMMOExecutor<T> executor, final IPacketHandler<T> packetHandler, final IClientFactory<T> clientFactory, final IAcceptFilter acceptFilter) throws IOException
	{
		super.setName("SelectorThread-" + super.getId());
		
		HELPER_BUFFER_SIZE = sc.getHelperBufferSize();
		HELPER_BUFFER_COUNT = sc.getHelperBufferCount();
		MAX_SEND_PER_PASS = sc.getMaxSendPerPass();
		MAX_READ_PER_PASS = sc.getMaxReadPerPass();
		
		SLEEP_TIME = sc.getSleepTime();
		
		DIRECT_WRITE_BUFFER = ByteBuffer.allocateDirect(sc.getWriteBufferSize()).order(BYTE_ORDER);
		WRITE_BUFFER = ByteBuffer.wrap(new byte[sc.getWriteBufferSize()]).order(BYTE_ORDER);
		READ_BUFFER = ByteBuffer.wrap(new byte[sc.getReadBufferSize()]).order(BYTE_ORDER);
		
		STRING_BUFFER = new NioNetStringBuffer(64 * 1024);
		
		pendingClose = new NioNetStackList<>();
		bufferPool = new FastList<>();
		
		for (int i = 0; i < HELPER_BUFFER_COUNT; i++)
		{
			bufferPool.addLast(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER));
		}
		
		this.acceptFilter = acceptFilter;
		this.packetHandler = packetHandler;
		this.clientFactory = clientFactory;
		this.executor = executor;
		selector = Selector.open();
	}
	
	public final void openServerSocket(final InetAddress address, final int tcpPort) throws IOException
	{
		final ServerSocketChannel selectable = ServerSocketChannel.open();
		selectable.configureBlocking(false);
		
		final ServerSocket ss = selectable.socket();
		
		if (address == null)
		{
			ss.bind(new InetSocketAddress(tcpPort));
		}
		else
		{
			ss.bind(new InetSocketAddress(address, tcpPort));
		}
		
		selectable.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	final ByteBuffer getPooledBuffer()
	{
		if (bufferPool.isEmpty())
		{
			return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
		}
		
		return bufferPool.removeFirst();
	}
	
	final void recycleBuffer(final ByteBuffer buf)
	{
		if (bufferPool.size() < HELPER_BUFFER_COUNT)
		{
			buf.clear();
			bufferPool.addLast(buf);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void run()
	{
		int selectedKeysCount = 0;
		
		SelectionKey key;
		MMOConnection<T> con;
		
		Iterator<SelectionKey> selectedKeys;
		
		while (!shutdown)
		{
			try
			{
				selectedKeysCount = selector.selectNow();
			}
			catch (final IOException e)
			{
				LOGGER.error("unhandled exception", e);
			}
			
			if (selectedKeysCount > 0)
			{
				selectedKeys = selector.selectedKeys().iterator();
				
				while (selectedKeys.hasNext())
				{
					key = selectedKeys.next();
					selectedKeys.remove();
					
					con = (MMOConnection<T>) key.attachment();
					
					switch (key.readyOps())
					{
						case SelectionKey.OP_CONNECT:
							finishConnection(key, con);
							break;
						case SelectionKey.OP_ACCEPT:
							acceptConnection(key, con);
							break;
						case SelectionKey.OP_READ:
							readPacket(key, con);
							break;
						case SelectionKey.OP_WRITE:
							writePacket(key, con);
							break;
						case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
							writePacket(key, con);
							if (key.isValid())
							{
								readPacket(key, con);
							}
							break;
					}
				}
			}
			
			synchronized (pendingClose)
			{
				while (!pendingClose.isEmpty())
				{
					con = pendingClose.removeFirst();
					writeClosePacket(con);
					closeConnectionImpl(con.getSelectionKey(), con);
					
				}
			}
			
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (final InterruptedException e)
			{
				LOGGER.error("unhandled exception", e);
			}
		}
		closeSelectorThread();
	}
	
	private final void finishConnection(final SelectionKey key, final MMOConnection<T> con)
	{
		try
		{
			((SocketChannel) key.channel()).finishConnect();
		}
		catch (final IOException e)
		{
			LOGGER.warn("", e);
			
			con.getClient().onForcedDisconnection(true);
			closeConnectionImpl(key, con);
		}
		
		// key might have been invalidated on finishConnect()
		if (key.isValid())
		{
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
		}
	}
	
	private final void acceptConnection(final SelectionKey key, MMOConnection<T> con)
	{
		final ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc;
		
		try
		{
			while ((sc = ssc.accept()) != null)
			{
				if (acceptFilter == null || acceptFilter.accept(sc))
				{
					sc.configureBlocking(false);
					final SelectionKey clientKey = sc.register(selector, SelectionKey.OP_READ);
					con = new MMOConnection<>(this, sc.socket(), clientKey);
					con.setClient(clientFactory.create(con));
					clientKey.attach(con);
				}
				else
				{
					sc.socket().close();
				}
			}
		}
		catch (final IOException e)
		{
			LOGGER.error("unhandled exception", e);
		}
	}
	
	private final void readPacket(final SelectionKey key, final MMOConnection<T> con)
	{
		// if (!con.isClosed())
		// {
		ByteBuffer buf;
		if ((buf = con.getReadBuffer()) == null)
		{
			buf = READ_BUFFER;
		}
		
		// if we try to to do a read with no space in the buffer it will
		// read 0 bytes
		// going into infinite loop
		if (buf.position() == buf.limit())
		{
			System.exit(0);
		}
		
		int result = -2;
		
		boolean critical = true;
		
		try
		{
			result = con.read(buf);
		}
		catch (final IOException e)
		{
			if (!con.isConnected() || !con.isChannelConnected())
			{
				critical = false;
			}
			else
			{
				LOGGER.warn("", e);
			}
		}
		
		if (result > 0)
		{
			buf.flip();
			
			final T client = con.getClient();
			
			for (int i = 0; i < MAX_READ_PER_PASS; i++)
			{
				if (!tryReadPacket(key, client, buf, con))
				{
					return;
				}
			}
			
			// only reachable if maxReadPerPass has been reached
			// check if there are some more bytes in buffer
			// and allocate/compact to prevent content lose.
			if (buf.remaining() > 0)
			{
				// did we use the READ_BUFFER ?
				if (buf == READ_BUFFER)
				// move the pending byte to the connections READ_BUFFER
				{
					allocateReadBuffer(con);
				}
				else
				// move the first byte to the beginning :)
				{
					buf.compact();
				}
			}
		}
		else
		{
			switch (result)
			{
				case 0:
				case -1:
					closeConnectionImpl(key, con);
					break;
				case -2:
					con.getClient().onForcedDisconnection(critical);
					closeConnectionImpl(key, con);
					break;
			}
		}
		// }
	}
	
	private final boolean tryReadPacket(final SelectionKey key, final T client, final ByteBuffer buf, final MMOConnection<T> con)
	{
		switch (buf.remaining())
		{
			case 0:
				// buffer is full
				// nothing to read
				return false;
			case 1:
				// we don`t have enough data for header so we need to read
				key.interestOps(key.interestOps() | SelectionKey.OP_READ);
				
				// did we use the READ_BUFFER ?
				if (buf == READ_BUFFER)
				// move the pending byte to the connections READ_BUFFER
				{
					allocateReadBuffer(con);
				}
				else
				// move the first byte to the beginning :)
				{
					buf.compact();
				}
				return false;
			default:
				// data size excluding header size :>
				final int dataPending = (buf.getShort() & 0xFFFF) - HEADER_SIZE;
				
				// do we got enough bytes for the packet?
				if (dataPending <= buf.remaining())
				{
					boolean read = true;
					
					// avoid parsing dummy packets (packets without body)
					if (dataPending > 0)
					{
						final int pos = buf.position();
						
						if (!parseClientPacket(pos, buf, dataPending, client))
						{
							read = false;
						}
						
						buf.position(pos + dataPending);
						
					}
					
					// if we are done with this buffer
					if (!buf.hasRemaining())
					{
						if (buf != READ_BUFFER)
						{
							con.setReadBuffer(null);
							recycleBuffer(buf);
						}
						else
						{
							READ_BUFFER.clear();
						}
						read = false;
					}
					return read;
				}
				
				// we don`t have enough bytes for the dataPacket so we need
				// to read
				key.interestOps(key.interestOps() | SelectionKey.OP_READ);
				
				// did we use the READ_BUFFER ?
				if (buf == READ_BUFFER)
				{
					// move it`s position
					buf.position(buf.position() - HEADER_SIZE);
					// move the pending byte to the connections READ_BUFFER
					allocateReadBuffer(con);
				}
				else
				{
					buf.position(buf.position() - HEADER_SIZE);
					buf.compact();
				}
				return false;
		}
	}
	
	private final void allocateReadBuffer(final MMOConnection<T> con)
	{
		con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
		READ_BUFFER.clear();
	}
	
	private final boolean parseClientPacket(final int pos, final ByteBuffer buf, final int dataSize, final T client)
	{
		final boolean ret = client.decrypt(buf, dataSize);
		
		if (ret && buf.hasRemaining())
		{
			// apply limit
			final int limit = buf.limit();
			buf.limit(pos + dataSize);
			
			// check for flood action
			/*
			 * int opcode = buf.get() & 0xFF; int opcode2 = -1; if(opcode == 0xd0){ if(buf.remaining() >= 2) { opcode2 = buf.getShort() & 0xffff; } } if(!tryPerformAction(opcode,opcode2,client)){ return false; }
			 */
			final ReceivablePacket<T> cp = packetHandler.handlePacket(buf, client);
			
			if (cp != null)
			{
				cp.buf = buf;
				cp.sbuf = STRING_BUFFER;
				cp.client = client;
				
				if (cp.read())
				{
					executor.execute(cp);
				}
				
				cp.buf = null;
				cp.sbuf = null;
			}
			
			buf.limit(limit);
			
		}
		
		return true;
	}
	
	private final void writeClosePacket(final MMOConnection<T> con)
	{
		SendablePacket<T> sp;
		synchronized (con.getSendQueue())
		{
			if (con.getSendQueue().isEmpty())
			{
				return;
			}
			
			while ((sp = con.getSendQueue().removeFirst()) != null)
			{
				WRITE_BUFFER.clear();
				
				putPacketIntoWriteBuffer(con.getClient(), sp);
				
				WRITE_BUFFER.flip();
				
				try
				{
					con.write(WRITE_BUFFER);
				}
				catch (final IOException e)
				{
					LOGGER.warn("", e);
					
				}
			}
		}
	}
	
	protected final void writePacket(final SelectionKey key, final MMOConnection<T> con)
	{
		if (!prepareWriteBuffer(con))
		{
			key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
			return;
		}
		
		DIRECT_WRITE_BUFFER.flip();
		
		final int size = DIRECT_WRITE_BUFFER.remaining();
		
		int result = -1;
		
		try
		{
			result = con.write(DIRECT_WRITE_BUFFER);
		}
		catch (final IOException e)
		{
			LOGGER.warn("", e);
			
		}
		
		// check if no error happened
		if (result >= 0)
		{
			// check if we written everything
			if (result == size)
			{
				// complete write
				synchronized (con.getSendQueue())
				{
					if (con.getSendQueue().isEmpty() && !con.hasPendingWriteBuffer())
					{
						key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
					}
				}
			}
			else
			// incomplete write
			{
				con.createWriteBuffer(DIRECT_WRITE_BUFFER);
			}
		}
		else
		{
			con.getClient().onForcedDisconnection(true);
			closeConnectionImpl(key, con);
		}
	}
	
	private final boolean prepareWriteBuffer(final MMOConnection<T> con)
	{
		boolean hasPending = false;
		DIRECT_WRITE_BUFFER.clear();
		
		// if there is pending content add it
		if (con.hasPendingWriteBuffer())
		{
			con.movePendingWriteBufferTo(DIRECT_WRITE_BUFFER);
			hasPending = true;
		}
		
		if (DIRECT_WRITE_BUFFER.remaining() > 1 && !con.hasPendingWriteBuffer())
		{
			final NioNetStackList<SendablePacket<T>> sendQueue = con.getSendQueue();
			final T client = con.getClient();
			SendablePacket<T> sp;
			
			for (int i = 0; i < MAX_SEND_PER_PASS; i++)
			{
				synchronized (con.getSendQueue())
				{
					if (sendQueue.isEmpty())
					{
						sp = null;
					}
					else
					{
						sp = sendQueue.removeFirst();
					}
				}
				
				if (sp == null)
				{
					break;
				}
				
				hasPending = true;
				
				// put into WriteBuffer
				putPacketIntoWriteBuffer(client, sp);
				
				WRITE_BUFFER.flip();
				
				if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit())
				{
					DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
				}
				else
				{
					con.createWriteBuffer(WRITE_BUFFER);
					break;
				}
			}
		}
		return hasPending;
	}
	
	private final void putPacketIntoWriteBuffer(final T client, final SendablePacket<T> sp)
	{
		WRITE_BUFFER.clear();
		
		// reserve space for the size
		final int headerPos = WRITE_BUFFER.position();
		final int dataPos = headerPos + HEADER_SIZE;
		WRITE_BUFFER.position(dataPos);
		
		// set the write buffer
		sp.buf = WRITE_BUFFER;
		// write content to buffer
		sp.write();
		// delete the write buffer
		sp.buf = null;
		
		// size (inclusive header)
		int dataSize = WRITE_BUFFER.position() - dataPos;
		WRITE_BUFFER.position(dataPos);
		client.encrypt(WRITE_BUFFER, dataSize);
		
		// recalculate size after encryption
		dataSize = WRITE_BUFFER.position() - dataPos;
		
		WRITE_BUFFER.position(headerPos);
		// write header
		WRITE_BUFFER.putShort((short) (dataSize + HEADER_SIZE));
		WRITE_BUFFER.position(dataPos + dataSize);
	}
	
	final void closeConnection(final MMOConnection<T> con)
	{
		synchronized (pendingClose)
		{
			pendingClose.addLast(con);
		}
	}
	
	private final void closeConnectionImpl(final SelectionKey key, final MMOConnection<T> con)
	{
		try
		{
			
			/*
			 * TODO this code must be contains on game module if(con.getClient() instanceof L2GameClient){ if(!((L2GameClient)con.getClient()).is_forcedToClose()){ con.getClient().onDisconnection(); } }else{
			 */
			con.getClient().onDisconnection();
			// }
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				// close socket and the SocketChannel
				con.close();
			}
			catch (final IOException e)
			{
				LOGGER.warn("", e);
				
			}
			finally
			{
				con.releaseBuffers();
				// clear attachment
				key.attach(null);
				// cancel key
				key.cancel();
			}
		}
	}
	
	public final void shutdown()
	{
		shutdown = true;
	}
	
	public boolean isShutdown()
	{
		return shutdown;
	}
	
	protected void closeSelectorThread()
	{
		for (final SelectionKey key : selector.keys())
		{
			try
			{
				key.channel().close();
			}
			catch (final IOException e)
			{
				LOGGER.warn("", e);
				
			}
		}
		
		try
		{
			selector.close();
		}
		catch (final IOException e)
		{
			LOGGER.warn("", e);
			
		}
	}
}
