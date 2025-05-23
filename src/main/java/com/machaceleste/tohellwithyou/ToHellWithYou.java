package com.machaceleste.tohellwithyou;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("tohellwithyou")
public class ToHellWithYou {

    public ToHellWithYou() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        ModEffects.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide) return;
    
        ServerPlayer player = (ServerPlayer) event.player;
        boolean isInNether = player.level().dimension() == Level.NETHER;
    
        if (isInNether) {
            if (!player.hasEffect(ModEffects.GO_TO_HELL.get())) {
                player.addEffect(new MobEffectInstance(ModEffects.GO_TO_HELL.get(), Integer.MAX_VALUE, 0, true, true));
            }
        } else {
            if (player.hasEffect(ModEffects.GO_TO_HELL.get())) {
                player.removeEffect(ModEffects.GO_TO_HELL.get());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if ((event.getEntity() instanceof ServerPlayer player) &&
            !player.hasEffect(ModEffects.GO_TO_HELL.get()) &&
            !hasTotem(player)) {
                event.setCanceled(true);
                teleportToNether(player);
            }
    }

    public boolean hasTotem(ServerPlayer player) {
        ItemStack[] items = new ItemStack[] {
            player.getMainHandItem(),
            player.getOffhandItem()
        };
        for (ItemStack item : items) {
            if (item.getItem() == Items.TOTEM_OF_UNDYING) {
                return true;
            }
        }
        return false;
    }

    private void teleportToNether(ServerPlayer player) {
        ServerLevel nether = player.server.getLevel(Level.NETHER);
        if (nether == null) return;
    
        BlockPos overworldPos = player.blockPosition();
        int posY = overworldPos.getY() >= 34 ? overworldPos.getY() : 34;
        BlockPos netherPos = new BlockPos((int)(overworldPos.getX()), posY, (int)(overworldPos.getZ()));
        BlockPos spawn = findSafeSpawn(nether, netherPos);
    
        if (spawn.equals(netherPos)) {
            spawn = emergencyShelter(nether, netherPos);
        }
    
        player.setHealth(6.0F);
        player.teleportTo(nether, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }
    
    private BlockPos findSafeSpawn(ServerLevel level, BlockPos center) {
        int radius = 1;
        int chunkSize = 16;
        BlockPos bestSpot = center;
        int minY = 34;
        int maxY = 120;
    
        outer:
        for (int cx = -radius; cx <= radius; cx++) {
            for (int cz = -radius; cz <= radius; cz++) {
                int baseX = center.getX() + (cx * chunkSize);
                int baseZ = center.getZ() + (cz * chunkSize);
                for (int x = 0; x < chunkSize; x++) {
                    for (int z = 0; z < chunkSize; z++) {
                        for (int y = maxY; y > minY; y--) {
                            BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                            if (isSafeSpawn(level, pos)) {
                                bestSpot = pos;
                                break outer;
                            }
                        }
                    }
                }
            }
        }
    
        return bestSpot;
    }
    
    private boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (level.getBlockState(pos.offset(dx, dy, dz)).getBlock() != Blocks.AIR) {
                        return false;
                    }
                }
            }
        }
    
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = pos.below().offset(dx, 0, dz);
                if (level.getBlockState(floorPos).getBlock() == Blocks.AIR ||
                    level.getBlockState(floorPos).getBlock() == Blocks.LAVA ||
                    level.getBlockState(floorPos).getBlock() == Blocks.MAGMA_BLOCK ||
                    level.getBlockState(floorPos).getBlock() == Blocks.FIRE) {
                    return false;
                }
            }
        }
    
        return true;
    }

    private BlockPos emergencyShelter(ServerLevel level, BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
    
                    boolean isWall = 
                        dx == -2 || dx == 2 || 
                        dy == -1 || dy == 3 || 
                        dz == -2 || dz == 2;
    
                    if (isWall) {
                        level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
                    } else {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    
        return center.above();
    }
}
