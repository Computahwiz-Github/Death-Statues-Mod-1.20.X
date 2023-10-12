package net.isaiah.deathstatues.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.isaiah.deathstatues.DeathStatues;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<DeathStatuesScreenHandler> DEATH_STATUE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(DeathStatues.MOD_ID, "death_statue_screen_handler"),
                    new ExtendedScreenHandlerType<>(DeathStatuesScreenHandler::new));

    public static void registerScreenHandlers() {
        DeathStatues.LOGGER.info("Registering Screen Handlers for: " + DeathStatues.MOD_ID);
    }
}
