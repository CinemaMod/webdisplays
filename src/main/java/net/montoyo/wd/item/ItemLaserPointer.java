/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.montoyo.wd.block.BlockScreen;
import net.montoyo.wd.client.ClientProxy;
import net.montoyo.wd.config.ClientConfig;
import net.montoyo.wd.controls.builtin.ClickControl;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.entity.TileEntityScreen;
import net.montoyo.wd.registry.BlockRegistry;
import net.montoyo.wd.net.WDNetworkRegistry;
import net.montoyo.wd.net.server_bound.C2SMessageScreenCtrl;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Multiblock;
import net.montoyo.wd.utilities.Vector2i;
import net.montoyo.wd.utilities.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemLaserPointer extends Item implements WDItem {
	
	public ItemLaserPointer(Properties properties) {
		super(properties
				.stacksTo(1)
//				.tab(WebDisplays.CREATIVE_TAB)
		);
	}
	
	//Laser pointer
	private static TileEntityScreen pointedScreen;
	private static BlockSide pointedScreenSide;
	private static long lastPointPacket;
	
	private static boolean mouseDown = false;
	private static boolean left;
	private static boolean middle;
	private static boolean right;
	
	public static void tick(Minecraft mc) {
		BlockHitResult result = ClientProxy.raycast(64.0); //TODO: Make that distance configurable
		
		BlockPos bpos = result.getBlockPos();
		
		if (result.getType() == HitResult.Type.BLOCK && mc.level.getBlockState(bpos).getBlock() == BlockRegistry.SCREEN_BLOCk.get()) {
			Vector3i pos = new Vector3i(result.getBlockPos());
			BlockSide side = BlockSide.values()[result.getDirection().ordinal()];
			
			Multiblock.findOrigin(mc.level, pos, side, null);
			TileEntityScreen te = (TileEntityScreen) mc.level.getBlockEntity(pos.toBlock());
			
			if (te != null && te.hasUpgrade(side, DefaultUpgrade.LASERMOUSE)) { //hasUpgrade returns false is there's no screen on side 'side'
				//Since rights aren't synchronized, let the server check them for us...
				TileEntityScreen.Screen scr = te.getScreen(side);
				
				if (scr.browser != null) {
					float hitX = ((float) result.getLocation().x) - (float) pos.x;
					float hitY = ((float) result.getLocation().y) - (float) pos.y;
					float hitZ = ((float) result.getLocation().z) - (float) pos.z;
					Vector2i tmp = new Vector2i();
					
					if (BlockScreen.hit2pixels(side, bpos, new Vector3i(result.getBlockPos()), scr, hitX, hitY, hitZ, tmp)) {
						laserClick(te, side, scr, tmp);
					}
				}
			}
		}
	}
	
	public static void deselect(Minecraft mc) {
		deselectScreen();
	}
	
	private static void laserClick(TileEntityScreen tes, BlockSide side, TileEntityScreen.Screen scr, Vector2i hit) {
		tes.handleMouseEvent(side, ClickControl.ControlType.MOVE, hit, -1);
		if (pointedScreen == tes && pointedScreenSide == side) {
			long t = System.currentTimeMillis();
			
			if (t - lastPointPacket >= 100) {
				lastPointPacket = t;
				WDNetworkRegistry.INSTANCE.sendToServer(C2SMessageScreenCtrl.laserMove(tes, side, hit));
			}
		} else {
			deselectScreen();
			pointedScreen = tes;
			pointedScreenSide = side;
		}
	}
	
	private static void deselectScreen() {
		if (pointedScreen != null && pointedScreenSide != null) {
			pointedScreen = null;
			pointedScreenSide = null;
		}
	}
	
	public static void press(boolean press, int button) {
		if (button <= 1 && ClientConfig.switchButtons)
			button = 1 - button;
		
		if (button == 0) left = press;
		else if (button == 1) right = press;
		else if (button == 2) middle = press;
		
		Minecraft mc = Minecraft.getInstance();
		
		BlockHitResult result = ClientProxy.raycast(64.0); //TODO: Make that distance configurable
		Vector3i pos = new Vector3i(result.getBlockPos());
		BlockSide side = BlockSide.values()[result.getDirection().ordinal()];
		Multiblock.findOrigin(mc.level, pos, side, null);
		
		float hitX = ((float) result.getLocation().x) - (float) pos.x;
		float hitY = ((float) result.getLocation().y) - (float) pos.y;
		float hitZ = ((float) result.getLocation().z) - (float) pos.z;
		Vector2i tmp = new Vector2i();
		
		BlockEntity be = mc.level.getBlockEntity(pos.toBlock());
		if (!(be instanceof TileEntityScreen)) return;
		
		//noinspection PatternVariableCanBeUsed
		TileEntityScreen te = (TileEntityScreen) be;
		
		if (te.hasUpgrade(side, DefaultUpgrade.LASERMOUSE)) { //hasUpgrade returns false is there's no screen on side 'side'
			//Since rights aren't synchronized, let the server check them for us...
			TileEntityScreen.Screen scr = te.getScreen(side);
			
			if (scr.browser != null) {
				if (BlockScreen.hit2pixels(side, result.getBlockPos(), new Vector3i(result.getBlockPos()), scr, hitX, hitY, hitZ, tmp)) {
					te.handleMouseEvent(side, ClickControl.ControlType.MOVE, tmp, -1);
					te.handleMouseEvent(side, press ? ClickControl.ControlType.DOWN : ClickControl.ControlType.UP, tmp, button);
					
					if (press)
						WDNetworkRegistry.INSTANCE.sendToServer(C2SMessageScreenCtrl.laserDown(te, side, tmp, button));
					else
						WDNetworkRegistry.INSTANCE.sendToServer(C2SMessageScreenCtrl.laserUp(te, side, button));
				}
			}
		}
	}
	
	public static boolean isOn() {
		return left || right || middle;
	}
	
	@Nullable
	@Override
	public String getWikiName(@Nonnull ItemStack is) {
		return is.getItem().getName(is).getString();
	}
}
