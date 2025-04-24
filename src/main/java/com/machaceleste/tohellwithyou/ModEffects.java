package com.machaceleste.tohellwithyou;

import com.machaceleste.tohellwithyou.effects.GoToHell;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "tohellwithyou");

    public static final RegistryObject<MobEffect> GO_TO_HELL = EFFECTS.register("go_to_hell", GoToHell::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
