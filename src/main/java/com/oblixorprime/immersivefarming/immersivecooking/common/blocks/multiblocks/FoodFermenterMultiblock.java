package com.oblixorprime.immersivefarming.immersivecooking.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import net.minecraft.core.BlockPos;
import com.oblixorprime.immersivefarming.immersivecooking.client.utils.ICBasicClientProperties;
import com.oblixorprime.immersivefarming.immersivecooking.common.ICContent;
import com.oblixorprime.immersivefarming.immersivecooking.common.utils.Resource;

import java.util.function.Consumer;

public class FoodFermenterMultiblock extends ICTemplateMultiblock {
    public static final FoodFermenterMultiblock INSTANCE = new FoodFermenterMultiblock();

    public FoodFermenterMultiblock() {
        super(Resource.mod("multiblocks/food_fermenter"),
                new BlockPos(1, 1, 1), new BlockPos(1, 1, 1), new BlockPos(3, 3, 3),
                ICContent.Multiblock.FOOD_FERMENTER);
    }

    @Override
    public float getManualScale() {
        return 12;
    }


    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> data) {
        data.accept(new ICBasicClientProperties(this, 1.5, 1.5, 1.5));
    }
}
