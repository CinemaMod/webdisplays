/*
 * Copyright (C) 2018 BARBOTIN Nicolas
 */

package net.montoyo.wd.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.CraftComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ItemMinePad2 extends Item implements WDItem {

    public ItemMinePad2(Properties properties) {
        super(properties
                //setRegistryName("minepad");
                .stacksTo(1)
                //TODO what is Full3D();
                .defaultDurability(0)
                .tab(WebDisplays.CREATIVE_TAB));
    }

    private static String getURL(ItemStack is) {
        if(is.getTag() == null || !is.getTag().contains("PadURL"))
            return WebDisplays.INSTANCE.homePage;
        else
            return is.getTag().getString("PadURL");
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player ply, @Nonnull InteractionHand hand) {
        ItemStack is = ply.getItemInHand(hand);
        boolean ok;

        if(ply.isShiftKeyDown()) {
            if(world.isClientSide)
                WebDisplays.PROXY.displaySetPadURLGui(getURL(is));

            ok = true;
        } else if(is.getTag() != null && is.getTag().contains("PadID")) {
            if(world.isClientSide)
                WebDisplays.PROXY.openMinePadGui(is.getTag().getInt("PadID"));

            ok = true;
        } else
            ok = false;

        return new InteractionResultHolder<>(ok ? InteractionResult.SUCCESS : InteractionResult.PASS, is);
    }


    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity ent) {
        if(ent.isOnGround() && !ent.getLevel().isClientSide) {
            CompoundTag tag = ent.getItem().getTag();

            if(tag != null && tag.contains("ThrowHeight")) {
                //Delete it, it touched the ground
                double height = tag.getDouble("ThrowHeight");
                UUID thrower = null;

                if(tag.contains("ThrowerMSB") && tag.contains("ThrowerLSB"))
                    thrower = new UUID(tag.getLong("ThrowerMSB"), tag.getLong("ThrowerLSB"));

                if(tag.contains("PadID") || tag.contains("PadURL")) {
                    tag.remove("ThrowerMSB");
                    tag.remove("ThrowerLSB");
                    tag.remove("ThrowHeight");
                } else //We can delete the whole tag
                    ent.getItem().setTag(null);

                if(thrower != null && height - ent.getBlockY() >= 20.0) {
                    ent.getLevel().playSound(null, ent.getBlockX(), ent.getBlockY(), ent.getBlockZ(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 4.0f, 1.0f);
                    ent.getLevel().addFreshEntity(new ItemEntity(ent.getLevel(), ent.getBlockX(), ent.getBlockY(), ent.getBlockZ(), CraftComponent.EXTCARD.makeItemStack()));
                    ent.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);

                    Player ply = ent.getLevel().getPlayerByUUID(thrower);
                    if(ply != null && ply instanceof ServerPlayer)
                        WebDisplays.INSTANCE.criterionPadBreak.trigger(((ServerPlayer) ply).getAdvancements());
                }
            }
        }

        return false;
    }

    @Nullable
    @Override
    public String getWikiName(@Nonnull ItemStack is) {
        return is.getItem().getName(is).getString();
    }

}
