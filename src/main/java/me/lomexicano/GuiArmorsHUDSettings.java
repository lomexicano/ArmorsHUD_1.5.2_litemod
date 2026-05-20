package me.lomexicano;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;
import org.lwjgl.input.Keyboard;

public class GuiArmorsHUDSettings extends GuiScreen {
    private GuiScreen parentScreen;

    private static final String[] POSITION_NAMES_PT = {
            "Canto Superior Esquerdo", "Centro Esquerdo", "Canto Inferior Esquerdo",
            "Canto Superior Direito", "Centro Direito", "Canto Inferior Direito"
    };

    private static final String[] POSITION_NAMES_EN = {
            "Upper Left", "Center Left", "Bottom Left",
            "Upper Right", "Center Right", "Bottom Right"
    };

    private boolean isPortuguese() {
        return this.mc.gameSettings.language.toLowerCase().startsWith("pt");
    }

    private String getPositionText() {
        if (isPortuguese()) {
            return "Posição: " + POSITION_NAMES_PT[LiteModArmorsHUDRevived.hudPosition];
        } else {
            return "Position: " + POSITION_NAMES_EN[LiteModArmorsHUDRevived.hudPosition];
        }
    }

    public GuiArmorsHUDSettings(GuiScreen parent) {
        this.parentScreen = parent;
    }

    private String getDamageText() {
        String state = LiteModArmorsHUDRevived.showArmorDamage ? "ON" : "OFF";
        return isPortuguese() ? "Durabilidade: " + state : "Durability: " + state;
    }
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        StringTranslate stringtranslate = StringTranslate.getInstance();
        int centerX = this.width / 2 - 100;
        int startY = this.height / 4 + 24;

        this.buttonList.add(new GuiButton(0, centerX, startY, getPositionText()));

        // Botão do HP
        this.buttonList.add(new GuiButton(2, centerX, startY + 24, getDamageText()));

        // Botão Concluído
        this.buttonList.add(new GuiButton(1, centerX, startY + 48, stringtranslate.translateKey("gui.done")));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id == 0) {
            // Avança a posição em +1. Se passar de 5, volta para 0 (fazendo um ciclo)
            LiteModArmorsHUDRevived.hudPosition++;
            if (LiteModArmorsHUDRevived.hudPosition > 5) {
                LiteModArmorsHUDRevived.hudPosition = 0;
            }
            // Atualiza o texto do botão instantaneamente
            button.displayString = getPositionText();
        }
        else if (button.id == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (button.id == 2) {
            LiteModArmorsHUDRevived.showArmorDamage = !LiteModArmorsHUDRevived.showArmorDamage;
            button.displayString = getDamageText();
        }

        // Salva sempre que houver alteração
        LiteModArmorsHUDRevived.saveConfig();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Escolhe o título de acordo com o idioma do jogo
        String title = isPortuguese() ? "Configurações do Armors HUD" : "Armors HUD Settings";
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 20, 16777215);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}