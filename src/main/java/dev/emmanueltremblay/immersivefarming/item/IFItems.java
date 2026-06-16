package dev.emmanueltremblay.immersivefarming.item;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.fluid.IFFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IFItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveFarming.MOD_ID);

    public static final DeferredItem<BucketItem> TREATED_WATER_BUCKET = ITEMS.register(
            "treated_water_bucket",
            () -> new BucketItem(IFFluids.TREATED_WATER.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final DeferredItem<CompostItem> COMPOST = ITEMS.register("compost", () -> new CompostItem(new Item.Properties()));
    public static final DeferredItem<Item> WHEEL = ITEMS.registerSimpleItem("wheel");
    public static final DeferredItem<Item> PLOW = ITEMS.register("plow", () -> new IntegrationPlaceholderItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> SOWER = ITEMS.register("sower", () -> new IntegrationPlaceholderItem(new Item.Properties().stacksTo(1)));

    private IFItems() {
    }
}
