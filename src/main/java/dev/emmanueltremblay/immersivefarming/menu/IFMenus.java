package dev.emmanueltremblay.immersivefarming.menu;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ImmersiveFarming.MOD_ID);

    private IFMenus() {
    }
}
