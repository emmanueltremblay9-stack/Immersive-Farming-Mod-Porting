package dev.emmanueltremblay.immersivefarming.util;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class IFTags {
    public static final class Blocks {
        public static final TagKey<Block> TILLABLE_BLOCK = TagKey.create(Registries.BLOCK, ImmersiveFarming.id("tillable_block"));

        private Blocks() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> COMPOSTABLES = TagKey.create(Registries.ITEM, ImmersiveFarming.id("compostables"));

        private Items() {
        }
    }

    private IFTags() {
    }
}
