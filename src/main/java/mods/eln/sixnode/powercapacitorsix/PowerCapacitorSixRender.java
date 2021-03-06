package mods.eln.sixnode.powercapacitorsix;

import java.io.DataInputStream;

import org.lwjgl.opengl.GL11;

import mods.eln.cable.CableRenderType;
import mods.eln.misc.Direction;
import mods.eln.node.six.SixNodeDescriptor;
import mods.eln.node.six.SixNodeElementInventory;
import mods.eln.node.six.SixNodeElementRender;
import mods.eln.node.six.SixNodeEntity;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

public class PowerCapacitorSixRender extends SixNodeElementRender {

	public PowerCapacitorSixDescriptor descriptor;
	private CableRenderType renderPreProcess;

	public PowerCapacitorSixRender(SixNodeEntity tileEntity, Direction side,
			SixNodeDescriptor descriptor) {
		super(tileEntity, side, descriptor);
		this.descriptor = (PowerCapacitorSixDescriptor) descriptor;

	}

	@Override
	public void draw() {
		GL11.glRotatef(90, 1, 0, 0);
		front.glRotateOnX();
		descriptor.draw();
	}


	SixNodeElementInventory inventory = new SixNodeElementInventory(2, 64, this);

	@Override
	public GuiScreen newGuiDraw(Direction side, EntityPlayer player) {

		return new PowerCapacitorSixGui(player, inventory, this);
	}

}
