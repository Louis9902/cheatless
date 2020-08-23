package io.github.louis9902.cheatless.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanScreenMixin extends Screen {

    @Shadow
    private ButtonWidget buttonGameMode;
    @Shadow
    private ButtonWidget buttonAllowCommands;

    @Shadow
    private String gameMode;
    @Shadow
    private boolean allowCommands;

    protected OpenToLanScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void updateButtonText();

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        IntegratedServer server = client.getServer();
        // if a single player world is present we have a server
        if (server != null) {
            boolean hasCheats = server.getSaveProperties().areCommandsAllowed();

            this.buttonAllowCommands.active = hasCheats;
            this.allowCommands = hasCheats;

            if (!hasCheats) {
                this.buttonGameMode.active = false;
                this.gameMode = server.getDefaultGameMode().getName();
            }

            this.updateButtonText();
        }
    }

}
