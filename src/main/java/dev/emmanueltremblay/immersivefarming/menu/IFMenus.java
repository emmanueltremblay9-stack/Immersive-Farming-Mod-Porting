package dev.emmanueltremblay.immersivefarming.menu;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ComposterMenu>> COMPOSTER =
            MENUS.register("composter", () -> IMenuTypeExtension.create(ComposterMenu::fromNetwork));

    private IFMenus() {
    }
}
