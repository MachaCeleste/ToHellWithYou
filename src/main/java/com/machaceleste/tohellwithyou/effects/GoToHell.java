package com.machaceleste.tohellwithyou.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class GoToHell extends MobEffect {
    public GoToHell() {
        super(MobEffectCategory.HARMFUL, 0x5C0000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int aplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
