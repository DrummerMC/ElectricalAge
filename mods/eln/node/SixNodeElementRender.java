package mods.eln.node;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;



import mods.eln.Eln;
import mods.eln.cable.CableRender;
import mods.eln.cable.CableRenderDescriptor;
import mods.eln.cable.CableRenderType;
import mods.eln.client.ClientProxy;
import mods.eln.electricasensor.ElectricalSensorElement;
import mods.eln.misc.Coordonate;
import mods.eln.misc.Direction;
import mods.eln.misc.LRDU;
import mods.eln.misc.LRDUMask;
import mods.eln.misc.Utils;
import mods.eln.misc.UtilsClient;
import mods.eln.sound.SoundCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;

public abstract class SixNodeElementRender {
	public SixNodeEntity tileEntity;
	public Direction side;
	//public SixNodeDescriptor descriptor;
	public LRDUMask connectedSide = new LRDUMask();
	int glList,cableList[];
	boolean cableListReady[] = {false,false,false,false};
	boolean glListReady = false;
	

	public SixNodeElementRender(SixNodeEntity tileEntity, Direction side,SixNodeDescriptor descriptor)
	{
		this.sixNodeDescriptor = descriptor;
		this.tileEntity = tileEntity;
		this.side = side;
		cableList = new int[4];
		//this.descriptor = descriptor;
		if(glListEnable())
		{
			glList = GL11.glGenLists(1);
		
		}	
		cableList[0] = GL11.glGenLists(1);
		cableList[1] = GL11.glGenLists(1);
		cableList[2] = GL11.glGenLists(1);
		cableList[3] = GL11.glGenLists(1);
	}
	
	public void needRedrawCable()
	{
		needRedraw = true;
	}

	boolean needRedraw;
	public void draw()
	{
		//Minecraft.getMinecraft().mcProfiler.startSection("SixNodeRender");
		if(needRedraw)
		{
			needRedraw = false;
			connectionType = CableRender.connectionType(this, side);
			if(drawCableAuto())
			{
				for(int idx = 0;idx<4;idx++)
				{
					CableRenderDescriptor render = getCableRender(LRDU.fromInt(idx));
					cableListReady[idx] = false;
					if(render != null && (connectedSide.mask & (1<<idx)) != 0)
					{
						GL11.glNewList(cableList[idx], GL11.GL_COMPILE);
							CableRender.drawCable(render, new LRDUMask(1<<idx),connectionType);
						GL11.glEndList();			
						cableListReady[idx] = true;
					}
				}
			}
		}

		for(int idx = 0;idx<4;idx++)
		{
			Utils.setGlColorFromDye(connectionType.otherdry[idx]);
			if(cableListReady[idx])
			{
				UtilsClient.bindTexture(getCableRender(LRDU.fromInt(idx)).cableTexture);
				GL11.glCallList(cableList[idx]);			
			}				
		}
	
		GL11.glColor3f(1f, 1f, 1f);	
		//Minecraft.getMinecraft().mcProfiler.endSection();
	}
	
	public boolean drawCableAuto()
	{
		return true;
	}

	
	public boolean glListEnable()
	{
		return true;
	}
	
	public void glListCall()
	{
		if(! glListEnable()) return;
		if( ! glListReady)
		{
			GL11.glNewList(glList, GL11.GL_COMPILE);
			glListDraw();
			GL11.glEndList();
			
			glListReady = true;
		}
		GL11.glCallList(glList);
	}
	
	public void glListDraw()
	{
		
	}
	public LRDU front;
	CableRenderType connectionType;
	public SixNodeDescriptor sixNodeDescriptor;
	public void publishUnserialize(DataInputStream stream)
	{
		try {
			byte b = stream.readByte();
			connectedSide.set(b & 0xF);
			front = front.fromInt((b >> 4) & 0x3);
			
			needRedraw = true;
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		glListReady = false;
	}
	public void singleUnserialize(DataInputStream stream)
	{

	}
	
	private int uuid = 0;

	public int getUuid() {
		if (uuid == 0) {
			uuid = UtilsClient.getUuid();
		}
		return uuid;
	}

	public boolean usedUuid() {
		return uuid != 0;
	}
	
	
	public void play(SoundCommand s) {
		s.addUuid(getUuid());
		s.set(tileEntity);
		s.play();
	}

	public void destructor()
	{
		if(usedUuid())
			ClientProxy.uuidManager.kill(uuid);
		
		if(glListEnable())
		{
			GL11.glDeleteLists(glList,1);
		}
		GL11.glDeleteLists(cableList[0],1);
		GL11.glDeleteLists(cableList[1],1);
		GL11.glDeleteLists(cableList[2],1);
		GL11.glDeleteLists(cableList[3],1);
	}
	
	public GuiScreen newGuiDraw(Direction side,EntityPlayer player)
	{
		return null;
	}
	public IInventory getInventory()
	{
		return null;
	}
    public void preparePacketForServer(DataOutputStream stream)
    {
    	try {
    		tileEntity.preparePacketForServer(stream);
    		
	    	stream.writeByte(side.getInt());
	    	stream.writeShort(tileEntity.elementRenderIdList[side.getInt()]);
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    	
    }
	
    public void sendPacketToServer(ByteArrayOutputStream bos)
    {
    	tileEntity.sendPacketToServer(bos);
    }
    
    public CableRenderDescriptor getCableRender(LRDU lrdu)
    {
    	return null;
    }
    public int getCableDry(LRDU lrdu)
    {
    	return 0;
    }
    
	public void clientSetFloat(int id,float value)
	{
        try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bos);   	
	
	        preparePacketForServer(stream);
			
			stream.writeByte(id);
			stream.writeFloat(value);
			
			sendPacketToServer(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        		
	}	
	public void clientSetFloat(int id,float value1,float value2)
	{
        try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bos);   	
	
	        preparePacketForServer(stream);
			
			stream.writeByte(id);
			stream.writeFloat(value1);
			stream.writeFloat(value2);
			
			sendPacketToServer(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        		
	}	

	public void clientSetString(byte id, String text) {
        try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bos);   	
	
	        preparePacketForServer(stream);
			
			stream.writeByte(id);
			stream.writeUTF(text);
			
			sendPacketToServer(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        		
	}	

	

	public void clientSetInt(byte id, int value) {
        try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bos);   	
	
	        preparePacketForServer(stream);
			
			stream.writeByte(id);
			stream.writeInt(value);

			sendPacketToServer(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    		
	}
	
	public void clientSetByte(byte id,byte value)
	{
        try {
	    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        DataOutputStream stream = new DataOutputStream(bos);   	
	
	        preparePacketForServer(stream);
			
			stream.writeByte(id);
			stream.writeByte(value);
			
			sendPacketToServer(bos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        		
	}
	
	public void clientSend(int id)
	{
    try {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bos);   	

        preparePacketForServer(stream);
		
		stream.writeByte(id);

		
		sendPacketToServer(bos);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}        
    		
}

	public boolean cameraDrawOptimisation() {
		// TODO Auto-generated method stub
		return true;
	}

	public void serverPacketUnserialize(DataInputStream stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void notifyNeighborSpawn() {
		needRedraw = true;
	}  
	
	
    
}
