package net.montoyo.wd.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.entity.*;

public class TileRegistry {
    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, "webdisplays");

    //Register tile entities
    public static final RegistryObject<BlockEntityType<ScreenBlockEntity>> SCREEN_BLOCK_ENTITY = TILE_TYPES
            .register("screen", () -> BlockEntityType.Builder
                    .of(ScreenBlockEntity::new, BlockRegistry.SCREEN_BLOCk.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> KEYBOARD = TILE_TYPES.register("kb_left", () -> BlockEntityType.Builder
            .of(KeyboardBlockEntity::new, BlockRegistry.KEYBOARD_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> REMOTE_CONTROLLER = TILE_TYPES.register("rctrl",
            () -> BlockEntityType.Builder.of(RemoteControlBlockEntity::new, BlockRegistry.REMOTE_CONTROLLER_BLOCK.get()).build(null));         //WITHOUT FACING (>= 3)

    public static final RegistryObject<BlockEntityType<?>> REDSTONE_CONTROLLER = TILE_TYPES.register("redctrl",
            () -> BlockEntityType.Builder.of(RedstoneControlBlockEntity::new, BlockRegistry.REDSTONE_CONTROL_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> SERVER = TILE_TYPES.register("server",
            () -> BlockEntityType.Builder.of(ServerBlockEntity::new, BlockRegistry.SERVER_BLOCK.get()).build(null));

    public static void init(IEventBus bus) {
        TILE_TYPES.register(bus);
    }
}
