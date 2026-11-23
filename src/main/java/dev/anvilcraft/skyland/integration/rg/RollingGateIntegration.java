package dev.anvilcraft.skyland.integration.rg;

import dev.anvilcraft.lib.v2.integration.Integration;
import dev.anvilcraft.skyland.integration.rg.event.RollingGateEventListener;
import net.neoforged.neoforge.common.NeoForge;

@Integration("rolling_gate")
public class RollingGateIntegration {
    public void apply() {
        NeoForge.EVENT_BUS.register(new RollingGateEventListener());
        RollingGateLanguageLoader.loadAll();
    }
}
