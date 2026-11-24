package dev.anvilcraft.skyland.integration.rg;

import dev.anvilcraft.rg.RollingGateServerRules;
import dev.anvilcraft.rg.api.event.RGRuleChangeEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * RollingGate 的 language 规则只用于生成服务端翻译 fallback，
 * 这里在客户端跟随该规则，真正切换客户端界面语言。
 */
public final class RGClientLanguageSync {
    private boolean tracking;
    private String lastSeen;
    private String pending;

    static void register() {
        NeoForge.EVENT_BUS.register(new RGClientLanguageSync());
    }

    /**
     * 单人游戏下客户端与集成服务器共用事件总线，命令执行时会直接收到该事件。
     */
    @SubscribeEvent
    public void onRuleChange(@NotNull RGRuleChangeEvent.Server<?> event) {
        if (!"language".equals(event.getRule().name())) return;
        if (event.getNewValue() instanceof String locale) schedule(locale);
    }

    /**
     * 多人游戏下客户端只会收到规则同步包，这里通过轮询发现同步后的值变化。
     */
    @SubscribeEvent
    public void onClientTick(@NotNull ClientTickEvent.Post event) {
        if (this.pending != null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.player == null) return;
            String locale = this.pending;
            this.pending = null;
            switchLanguage(locale);
        }
        if (!this.tracking) return;
        String current = RollingGateServerRules.language;
        if (current == null || current.equals(this.lastSeen)) return;
        this.lastSeen = current;
        switchLanguage(current);
    }

    @SubscribeEvent
    public void onLoggingIn(@NotNull ClientPlayerNetworkEvent.LoggingIn event) {
        this.tracking = true;
        this.lastSeen = RollingGateServerRules.language;
    }

    @SubscribeEvent
    public void onLoggingOut(@NotNull ClientPlayerNetworkEvent.LoggingOut event) {
        this.tracking = false;
        this.lastSeen = null;
        this.pending = null;
    }

    private void schedule(String locale) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || locale == null || locale.isEmpty()) return;
        minecraft.execute(() -> {
            // 世界加载期间不刷新资源，等玩家进入世界后再切换
            if (minecraft.player == null) {
                this.pending = locale;
            } else {
                switchLanguage(locale);
            }
        });
    }

    private static void switchLanguage(String locale) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !minecraft.getLanguageManager().getLanguages().containsKey(locale)) return;
        if (locale.equals(minecraft.getLanguageManager().getSelected())) return;
        minecraft.options.languageCode = locale;
        minecraft.getLanguageManager().setSelected(locale);
        minecraft.reloadResourcePacks();
    }
}
