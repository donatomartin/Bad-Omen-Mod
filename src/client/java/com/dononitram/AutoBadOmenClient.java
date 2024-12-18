package com.dononitram;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBadOmenClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoBadOmenClient.class);
    private static final int CHECK_INTERVAL = 100; // 100 ticks = 5 seconds
    private int tickCounter = 0;
    private boolean consuming = false;
    private int consumeTickCounter = 0;
    private static final int CONSUME_DURATION = 40; // Assuming it takes 2 seconds (40 ticks) to consume

    @Override
    public void onInitializeClient() {
        LOGGER.info("AutoBadOmenClient initialized");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (consuming) {
                if (consumeTickCounter >= CONSUME_DURATION) {
                    client.options.useKey.setPressed(false);
                    consuming = false;
                    consumeTickCounter = 0;
                }
                consumeTickCounter++;
            } else {
                if (tickCounter % CHECK_INTERVAL == 0) {
                    checkBadOmenPotion(client);
                }
                tickCounter++;
            }
        });
    }

    private void checkBadOmenPotion(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        ItemStack offHandStack = client.player.getStackInHand(Hand.OFF_HAND);
        String offHandItemName = offHandStack.getItem().toString();
        StatusEffectInstance[] statusEffects = client.player.getStatusEffects().toArray(new StatusEffectInstance[0]);

        LOGGER.info("Off Hand Item: {}", offHandItemName);
        LOGGER.info("Status Effects:");
        for (StatusEffectInstance statusEffectInstance : statusEffects) {
            LOGGER.info("  - {}", statusEffectInstance.toString());
        }

        if (offHandItemName.equals("minecraft:ominous_bottle")) {
            LOGGER.info("Found ominous bottle in off hand");

            boolean badOmenFound = false;
			boolean raidOmenFound = false;
            for (StatusEffectInstance statusEffectInstance : statusEffects) {
                if (statusEffectInstance.getEffectType().equals(StatusEffects.BAD_OMEN)) {
                    badOmenFound = true;
                    break;
                }

				if (statusEffectInstance.getEffectType().equals(StatusEffects.RAID_OMEN)) {
					raidOmenFound = true;
					break;
				}
            }

            if (badOmenFound || raidOmenFound) {
				return;
			}

			LOGGER.info("Omen not found, consuming ominous bottle");
			client.execute(() -> {
				client.options.useKey.setPressed(true);
				consuming = true;
				consumeTickCounter = 0;
			});
        }
    }
}
