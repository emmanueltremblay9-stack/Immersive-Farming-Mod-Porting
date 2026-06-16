package dev.emmanueltremblay.immersivefarming.block;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.block.entity.IndustrialComposterBlockEntity;
import dev.emmanueltremblay.immersivefarming.block.entity.SprinklerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ImmersiveFarming.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            BLOCK_ENTITY_TYPES.register("sprinkler", () -> BlockEntityType.Builder
                    .of(SprinklerBlockEntity::new, IFBlocks.SPRINKLER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> HIGH_PRESSURE_SPRINKLER =
            BLOCK_ENTITY_TYPES.register("sprinkler_extended", () -> BlockEntityType.Builder
                    .of(SprinklerBlockEntity::new, IFBlocks.HIGH_PRESSURE_SPRINKLER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialComposterBlockEntity>> COMPOSTER =
            BLOCK_ENTITY_TYPES.register("composter", () -> BlockEntityType.Builder
                    .of(IndustrialComposterBlockEntity::new, IFBlocks.COMPOSTER.get())
                    .build(null));

    private IFBlockEntities() {
    }
}
