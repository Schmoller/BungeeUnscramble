package au.com.addstar.unscramble;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class MessageOutput
{
	private final ByteArrayOutputStream mStream;
	private final DataOutputStream mOutput;
	private final String mChannel;
	
	public MessageOutput(String channel, String subChannel)
	{
		mChannel = channel;
		mStream = new ByteArrayOutputStream();
		mOutput = new DataOutputStream(mStream);
		
		try
		{
			mOutput.writeUTF(subChannel);
		}
		catch(IOException ignored)
		{
		}
	}
	public MessageOutput write( int b )
	{
		try
		{
			mOutput.write(b);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput write( byte[] b )
	{
		try
		{
			mOutput.write(b);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput write( byte[] b, int off, int len )
	{
		try
		{
			mOutput.write(b, off, len);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeBoolean( boolean v )
	{
		try
		{
			mOutput.writeBoolean(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeByte( int v )
	{
		try
		{
			mOutput.writeByte(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeShort( int v )
	{
		try
		{
			mOutput.writeShort(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeChar( int v )
	{
		try
		{
			mOutput.writeChar(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeInt( int v )
	{
		try
		{
			mOutput.writeInt(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeLong( long v )
	{
		try
		{
			mOutput.writeLong(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeFloat( float v )
	{
		try
		{
			mOutput.writeFloat(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeDouble( double v )
	{
		try
		{
			mOutput.writeDouble(v);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeBytes( String s )
	{
		try
		{
			mOutput.writeBytes(s);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeChars( String s )
	{
		try
		{
			mOutput.writeChars(s);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public MessageOutput writeUTF( String s )
	{
		try
		{
			mOutput.writeUTF(s);
		}
		catch(IOException ignored)
		{
		}
		
		return this;
	}

	public byte[] toBytes()
	{
		return mStream.toByteArray();
	}
	
	public void send(ServerInfo target)
	{
		target.sendData(mChannel, mStream.toByteArray());
	}
	
	public void send()
	{
		byte[] data = mStream.toByteArray();
		for(ServerInfo server : ProxyServer.getInstance().getServers().values())
			server.sendData(mChannel, data);
	}
}
