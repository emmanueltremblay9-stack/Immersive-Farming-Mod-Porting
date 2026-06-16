package dev.emmanueltremblay.immersivefarming.config;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ImmersiveFarming.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class IFConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_DISEASE = BUILDER
            .comment("Whether crop disease can start, spread, and kill crops.")
            .define("enableDisease", true);

    private static final ModConfigSpec.BooleanValue ENABLE_FARMLAND_TRAMPLE_PROTECTION = BUILDER
            .comment("Whether farmland and Immersive Farming soil resist trampling.")
            .define("enableFarmlandTrampleProtection", true);

    private static final ModConfigSpec.BooleanValue DISABLE_VANILLA_BONE_MEAL_ON_CROPS = BUILDER
            .comment("Whether vanilla bone meal use on crops is blocked.")
            .define("disableVanillaBoneMealOnCrops", true);

    private static final ModConfigSpec.IntValue SPRINKLER_RADIUS = BUILDER
            .comment("Chebyshev radius irrigated by regular sprinklers.")
            .defineInRange("sprinklerRadius", 6, 1, 64);

    private static final ModConfigSpec.IntValue HIGH_PRESSURE_SPRINKLER_RADIUS = BUILDER
            .comment("Chebyshev radius irrigated by high-pressure sprinklers.")
            .defineInRange("highPressureSprinklerRadius", 12, 1, 96);

    private static final ModConfigSpec.DoubleValue COMPOST_GROWTH_MULTIPLIER = BUILDER
            .comment("Growth multiplier applied to crops planted on compost-fertilized soil.")
            .defineInRange("compostGrowthMultiplier", 1.5D, 1.0D, 16.0D);

    private static final ModConfigSpec.BooleanValue ASTIKOR_REDUX_INTEGRATION = BUILDER
            .comment("Whether optional AstikorCarts Redux compatibility hooks should run when a compatible mod id is present.")
            .define("astikorReduxIntegration", true);

    private static final ModConfigSpec.DoubleValue START_DISEASE_CHANCE = BUILDER
            .comment("Random tick probability for a crop to become diseased.")
            .defineInRange("startDiseaseChance", 0.0001D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue PROXIMITY_DISEASE_CHANCE = BUILDER
            .comment("Random tick probability for disease to spread from a diseased crop to a neighbor.")
            .defineInRange("proximityDiseaseChance", 0.1D, 0.0D, 1.0D);

    private static final ModConfigSpec.DoubleValue DISEASE_LETHALITY_CHANCE = BUILDER
            .comment("Random tick probability for a diseased crop to die.")
            .defineInRange("diseaseLethalityChance", 0.02D, 0.0D, 1.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableDisease;
    public static boolean enableFarmlandTrampleProtection;
    public static boolean disableVanillaBoneMealOnCrops;
    public static int sprinklerRadius;
    public static int highPressureSprinklerRadius;
    public static double compostGrowthMultiplier;
    public static boolean astikorReduxIntegration;
    public static double startDiseaseChance;
    public static double proximityDiseaseChance;
    public static double diseaseLethalityChance;

    private IFConfig() {
    }

    @SubscribeEvent
    static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        enableDisease = ENABLE_DISEASE.get();
        enableFarmlandTrampleProtection = ENABLE_FARMLAND_TRAMPLE_PROTECTION.get();
        disableVanillaBoneMealOnCrops = DISABLE_VANILLA_BONE_MEAL_ON_CROPS.get();
        sprinklerRadius = SPRINKLER_RADIUS.get();
        highPressureSprinklerRadius = HIGH_PRESSURE_SPRINKLER_RADIUS.get();
        compostGrowthMultiplier = COMPOST_GROWTH_MULTIPLIER.get();
        astikorReduxIntegration = ASTIKOR_REDUX_INTEGRATION.get();
        startDiseaseChance = START_DISEASE_CHANCE.get();
        proximityDiseaseChance = PROXIMITY_DISEASE_CHANCE.get();
        diseaseLethalityChance = DISEASE_LETHALITY_CHANCE.get();
    }
}
