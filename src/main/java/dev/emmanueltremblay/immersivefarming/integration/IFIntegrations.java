package dev.emmanueltremblay.immersivefarming.integration;

import dev.emmanueltremblay.immersivefarming.ImmersiveFarming;
import dev.emmanueltremblay.immersivefarming.config.IFConfig;

public final class IFIntegrations {
    private IFIntegrations() {
    }

    public static void bootstrap() {
        if (ImmersiveFarming.isModLoaded("immersiveengineering")) {
            ImmersiveFarming.LOGGER.info("Immersive Engineering detected; manual/data compatibility resources are enabled.");
        }
        if (IFConfig.astikorReduxIntegration && ImmersiveFarming.isAstikorReduxLoaded()) {
            ImmersiveFarming.LOGGER.info("AstikorCarts-compatible mod detected; use its native plow and sower implementations.");
        }
    }
}
