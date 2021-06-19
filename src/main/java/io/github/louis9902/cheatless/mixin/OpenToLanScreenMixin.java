package io.github.louis9902.cheatless.mixin;

import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanScreenMixin extends Screen {

    private static final Text ALLOW_COMMANDS_TEXT = new TranslatableText("selectWorld.allowCommands");
    private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");

    @Shadow
    private GameMode gameMode;
    @Shadow
    private boolean allowCommands;

    @Shadow
    @Final
    private Screen parent;

    public OpenToLanScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        // because we are on the client
        assert client != null;
        var server = client.getServer();
        assert server != null;

        var hasCheats = server.getSaveProperties().areCommandsAllowed();

        GameMode[] modes = GameMode.values();
        Boolean[] cheats = new Boolean[]{Boolean.TRUE, Boolean.FALSE};
        if (!hasCheats) {
            gameMode = server.getDefaultGameMode();
            allowCommands = false;
            modes = new GameMode[]{gameMode};
            cheats = new Boolean[]{Boolean.FALSE};
        }

        var mode = CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName)
                .values(modes)
                .initially(gameMode)
                .build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, value) -> gameMode = value);

        var cheat = CyclingButtonWidget.builder((Boolean value) -> value ? ScreenTexts.ON : ScreenTexts.OFF)
                .values(cheats)
                .initially(allowCommands)
                .build(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (button, value) -> {
                    if (hasCheats)
                        allowCommands = value;
                    else {
                        allowCommands = false;
                        button.setValue(false);
                    }
                });

        var open = new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("lanServer.start"), (button) -> {
            client.openScreen(null);
            int port = NetworkUtils.findLocalPort();

            TranslatableText message;
            if (client.getServer().openToLan(gameMode, allowCommands, port)) {
                message = new TranslatableText("commands.publish.started", port);
            } else {
                message = new TranslatableText("commands.publish.failed");
            }

            client.inGameHud.getChatHud().addMessage(message);
            client.updateWindowTitle();
        });

        var cancel = new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (button) -> {
            client.openScreen(parent);
        });

        clearChildren();

        addDrawableChild(mode);
        addDrawableChild(cheat);
        addDrawableChild(open);
        addDrawableChild(cancel);
    }


}
