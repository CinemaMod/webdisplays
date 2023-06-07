/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.data.KeyboardData;
import net.montoyo.wd.init.TileInit;
import net.montoyo.wd.utilities.Util;

public class TileEntityKeyboard extends TileEntityPeripheralBase {

    private static final String RANDOM_CHARS = "AZERTYUIOPQSDFGHJKLMWXCVBNazertyuiopqsdfghjklmwxcvbn0123456789"; //Yes I have an AZERTY keyboard, u care?

    public TileEntityKeyboard(BlockPos arg2, BlockState arg3) {
        super(TileInit.KEYBOARD.get(), arg2, arg3);
    }

    @Override
    public InteractionResult onRightClick(Player player, InteractionHand hand) {
        if(level.isClientSide)
            return InteractionResult.SUCCESS;

        if(!isScreenChunkLoaded()) {
            Util.toast(player, "chunkUnloaded");
            return InteractionResult.SUCCESS;
        }

        TileEntityScreen tes = getConnectedScreen();
        if(tes == null) {
            Util.toast(player, "notLinked");
            return InteractionResult.SUCCESS;
        }

        TileEntityScreen.Screen scr = tes.getScreen(screenSide);
        if((scr.rightsFor(player) & ScreenRights.INTERACT) == 0) {
            Util.toast(player, "restrictions");
            return InteractionResult.SUCCESS;
        }

        (new KeyboardData(tes, screenSide, getBlockPos())).sendTo((ServerPlayer) player);
        return InteractionResult.SUCCESS;
    }

    public void simulateCat(Entity ent) {
        if(!isScreenChunkLoaded())
            return;
        
        TileEntityScreen tes = getConnectedScreen();

        if(tes != null) {
            TileEntityScreen.Screen scr = tes.getScreen(screenSide);
            boolean ok;

            if(ent instanceof Player)
                ok = (scr.rightsFor((Player) ent) & ScreenRights.INTERACT) != 0;
            else
                ok = (scr.otherRights & ScreenRights.INTERACT) != 0;

            if(ok) {
                char rnd = RANDOM_CHARS.charAt((int) (Math.random() * ((double) RANDOM_CHARS.length())));
                tes.type(screenSide, "t" + rnd, getBlockPos());

                Player owner = level.getPlayerByUUID(scr.owner.uuid);
                if(owner != null && owner instanceof ServerPlayer && ent instanceof Ocelot)
                    WebDisplays.INSTANCE.criterionKeyboardCat.trigger(((ServerPlayer) owner).getAdvancements());
            }
        }
    }

}
