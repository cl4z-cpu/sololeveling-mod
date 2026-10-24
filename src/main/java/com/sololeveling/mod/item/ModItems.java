package com.sololeveling.mod.item;

import com.sololeveling.mod.SoloLevelingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SoloLevelingMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoloLevelingMod.MODID);

    public static final RegistryObject<Item> KNIGHT_KILLER = ITEMS.register("knight_killer", KnightKillerItem::new);
    public static final RegistryObject<Item> DEMON_DAGGER = ITEMS.register("demon_dagger", DemonDaggerItem::new);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sololeveling"))
                    .icon(() -> new ItemStack(DEMON_DAGGER.get()))
                    .displayItems((params, out) -> {
                        out.accept(KNIGHT_KILLER.get());
                        out.accept(DEMON_DAGGER.get());
                    })
                    .build());
}
