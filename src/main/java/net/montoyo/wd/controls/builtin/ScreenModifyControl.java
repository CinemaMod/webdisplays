package net.montoyo.wd.controls.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.montoyo.wd.controls.ScreenControl;
import net.montoyo.wd.core.MissingPermissionException;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Rotation;
import net.montoyo.wd.utilities.Vector2i;

import java.util.function.Function;

public class ScreenModifyControl extends ScreenControl {
	public static final ResourceLocation id = new ResourceLocation("webdisplays:mod_screen");
	
	public static enum ControlType {
		RESOLUTION, ROTATION
	}
	
	ControlType type;
	Vector2i res;
	Rotation rotation;
	
	public ScreenModifyControl(Vector2i res) {
		super(id);
		this.type = ControlType.RESOLUTION;
		this.res = res;
	}
	
	public ScreenModifyControl(Rotation rotation) {
		super(id);
		this.type = ControlType.ROTATION;
		this.rotation = rotation;
	}
	
	public ScreenModifyControl(FriendlyByteBuf buf) {
		super(id);
		type = ControlType.values()[buf.readByte()];
		if (type.equals(ControlType.RESOLUTION))
			res = new Vector2i(buf);
		else rotation = Rotation.values()[buf.readByte()];
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeByte(type.ordinal());
		if (res != null) res.writeTo(buf);
		else if (rotation != null) buf.writeByte(rotation.ordinal());
	}
	
	@Override
	public void handleServer(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx, Function<Integer, Boolean> permissionChecker) throws MissingPermissionException {
		checkPerms(ScreenRights.MODIFY_SCREEN, permissionChecker, ctx.getSender());
		switch (type) {
			case RESOLUTION -> tes.setResolution(side, res);
			case ROTATION -> tes.setRotation(side, rotation);
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void handleClient(BlockPos pos, BlockSide side, TileEntityScreen tes, NetworkEvent.Context ctx) {
		throw new RuntimeException("TODO");
	}
}
