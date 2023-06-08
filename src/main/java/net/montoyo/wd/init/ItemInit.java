package net.montoyo.wd.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.block.item.KeyboardItem;
import net.montoyo.wd.core.CraftComponent;
import net.montoyo.wd.core.DefaultUpgrade;
import net.montoyo.wd.item.*;

import java.util.Locale;

@SuppressWarnings({"unchecked", "unused"})
public class ItemInit{
    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "webdisplays");

    protected static final RegistryObject<Item>[] COMP_CRAFT_ITEMS = new RegistryObject[CraftComponent.values().length];
    protected static final RegistryObject<Item>[] UPGRADE_ITEMS = new RegistryObject[DefaultUpgrade.values().length];

    public static final RegistryObject<Item> CONFIGURATOR = ITEMS.register("screencfg", () -> new ItemScreenConfigurator(new Item.Properties()));
    public static final RegistryObject<Item> OWNERSHIP_THEIF = ITEMS.register("ownerthief", () -> new ItemOwnershipThief(new Item.Properties()));
    public static final RegistryObject<Item> LINKER = ITEMS.register("linker", () -> new ItemLinker(new Item.Properties()));
    public static final RegistryObject<Item> MINEPAD = ITEMS.register("minepad", () -> new ItemMinePad2(new Item.Properties()));
    public static final RegistryObject<Item> LASER_POINTER = ITEMS.register("laserpointer", () -> new ItemLaserPointer(new Item.Properties()));

    static  {
        DefaultUpgrade[] defaultUpgrades = DefaultUpgrade.values();
        for (int i = 0; i < defaultUpgrades.length; i++) {
            DefaultUpgrade upgrade = defaultUpgrades[i];
            UPGRADE_ITEMS[i] = ITEMS.register("upgrade_" + upgrade.name().toLowerCase(Locale.ROOT), () -> new ItemUpgrade(upgrade));
        }
        
        CraftComponent[] components = CraftComponent.values();
        for (int i = 0; i < components.length; i++) {
            CraftComponent cc = components[i];
            COMP_CRAFT_ITEMS[i] = ITEMS.register("craftcomp_" + cc.name().toLowerCase(Locale.ROOT), () -> new ItemCraftComponent(new Item.Properties()));
        }
    }

    public static final RegistryObject<Item> SCREEN = ITEMS.register("screen", () -> new BlockItem(BlockInit.blockScreen.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));

    public static final RegistryObject<Item> KEYBOARD = ITEMS.register("keyboard", () -> new KeyboardItem(BlockInit.blockKeyBoard.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> REDSTONE_CONTROLLER = ITEMS.register("redctrl", () -> new BlockItem(BlockInit.blockRedControl.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> REMOTE_CONTROLLER = ITEMS.register("rctrl", () -> new BlockItem(BlockInit.blockRControl.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    public static final RegistryObject<Item> SERVER = ITEMS.register("server", () -> new BlockItem(BlockInit.blockServer.get(), new Item.Properties().tab(WebDisplays.CREATIVE_TAB)));
    
    public static RegistryObject<Item> getComputerCraftItem(int index) {
        return COMP_CRAFT_ITEMS[index];
    }
    
    public static RegistryObject<Item> getUpgradeItem(int index) {
        return UPGRADE_ITEMS[index];
    }
    
    public static boolean isCompCraftItem(Item item) {
        for (RegistryObject<Item> itemRegistryObject : COMP_CRAFT_ITEMS)
            if (item == itemRegistryObject.get())
                return true;
        return false;
    }
}
