package dev.emmanueltremblay.immersivefarming.entity;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ImmersiveFarming.MOD_ID);

    private IFEntityTypes() {
    }
}
