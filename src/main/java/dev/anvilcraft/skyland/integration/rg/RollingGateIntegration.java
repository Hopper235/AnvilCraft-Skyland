package dev.anvilcraft.skyland.integration.rg;

import dev.anvilcraft.lib.v2.integration.Integration;
import dev.anvilcraft.skyland.integration.rg.event.RollingGateEventListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Integration("rolling_gate")
public class RollingGateIntegration {
    public void apply() {
        NeoForge.EVENT_BUS.register(new RollingGateEventListener());
        // RollingGate 1.1.0 会把 loadLanguage 读到的内容全部写进 zh_cn，且加载时机早于本整合。
        // 因此在服务器启动时再加载一次，保证两种语言都被放在正确的 locale 下。
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent event) -> RollingGateLanguageLoader.loadAll());
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RGClientLanguageSync.register();
        }
    }
}
