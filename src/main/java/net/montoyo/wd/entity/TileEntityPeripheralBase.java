/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.montoyo.wd.core.IPeripheral;
import net.montoyo.wd.utilities.BlockSide;
import net.montoyo.wd.utilities.Log;
import net.montoyo.wd.utilities.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class TileEntityPeripheralBase extends BlockEntity implements IPeripheral {

    protected Vector3i screenPos;
    protected BlockSide screenSide;

    public TileEntityPeripheralBase(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);

        if(tag.contains("WDScreen", 10)) {
            CompoundTag scr = tag.getCompound("WDScreen");
            screenPos = new Vector3i(scr.getInt("X"), scr.getInt("Y"), scr.getInt("Z"));
            screenSide = BlockSide.values()[scr.getByte("Side")];
        } else {
            screenPos = null;
            screenSide = null;
        }
    }

    @Override
    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        super.serializeNBT();

        if(screenPos != null && screenSide != null) {
            CompoundTag scr = new CompoundTag();
            scr.putInt("X", screenPos.x);
            scr.putInt("Y", screenPos.y);
            scr.putInt("Z", screenPos.z);
            scr.putByte("Side", (byte) screenSide.ordinal());

            tag.put("WDScreen", scr);
        }

        return tag;
    }

    @Override
    public boolean connect(Level world_, BlockPos blockPos, BlockState blockState, Vector3i pos, BlockSide side) {
        BlockEntity te = world_.getBlockEntity(pos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen)) {
            Log.error("TileEntityPeripheralBase.connect(): Tile entity at %s is not a screen!", pos.toString());
            return false;
        }

        if(((TileEntityScreen) te).getScreen(side) == null) {
            Log.error("TileEntityPeripheralBase.connect(): There is no screen at %s on side %s!", pos.toString(), side.toString());
            return false;
        }

        screenPos = pos;
        screenSide = side;
        setChanged();
        return true;
    }

    public boolean isLinked() {
        return screenPos != null && screenSide != null;
    }

    public boolean isScreenChunkLoaded() {
        if (screenPos == null || screenSide == null)
            return true;

        LevelChunk chunk = Objects.requireNonNull(getLevel()).getChunkSource().getChunk(screenPos.x >> 4, screenPos.z >> 4, true);
        return chunk != null && !chunk.isEmpty();
    }

    @Nullable
    public TileEntityScreen getConnectedScreen() {
        if(screenPos == null || screenSide == null)
            return null;

        BlockEntity te = level.getBlockEntity(screenPos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen) || ((TileEntityScreen) te).getScreen(screenSide) == null) {
            screenPos = null;
            screenSide = null;
            setChanged();
            return null;
        }

        return (TileEntityScreen) te;
    }

    @Nullable
    public TileEntityScreen getConnectedScreenEx() {
        if(screenPos == null || screenSide == null)
            return null;

        BlockEntity te = level.getBlockEntity(screenPos.toBlock());
        if(te == null || !(te instanceof TileEntityScreen) || ((TileEntityScreen) te).getScreen(screenSide) == null)
            return null;

        return (TileEntityScreen) te;
    }

    @Nullable
    public Vector3i getScreenPos() {
        return screenPos;
    }

    @Nullable
    public BlockSide getScreenSide() {
        return screenSide;
    }

    public InteractionResult onRightClick(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public void onNeighborChange(Block neighborType, BlockPos neighborPos) {
    }

}
