package me.lomexicano;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.Tickable;
import net.minecraft.client.Minecraft;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.RenderItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ScaledResolution;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import java.io.*;
import com.mumfrey.liteloader.util.ModUtilities;
import net.minecraft.src.*;
import org.lwjgl.input.Keyboard;

public class LiteModArmorsHUDRevived implements LiteMod, Tickable {

    public static boolean showArmorDamage = true;
    private RenderItem itemRenderer;

    // Variável para armazenar a posição (Padrão: 5 = Canto Inferior Direito)
    public static int hudPosition = 5;

    // Tecla de configuração (padrão: Y)
    public KeyBinding keyBindSettings = new KeyBinding("Configurações Armor HUD", Keyboard.KEY_Y);

    private static File settingsFile;

    @Override
    public String getName() {
        return "Armors HUD";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void init() {
        this.itemRenderer = new RenderItem();

        // Registra a tecla no menu de controles do jogo
        ModUtilities.registerKey(this.keyBindSettings);

        // Prepara o arquivo de configuração
        File modDir = new File(Minecraft.getMinecraftDir(), "mods/ArmorsHUD");
        if (!modDir.exists()) modDir.mkdirs();
        settingsFile = new File(modDir, "config.txt");
        loadConfig();
    }

    @Override
    public void onTick(Minecraft mc, float partialTicks, boolean inGame, boolean clock) {

        if (inGame && mc.currentScreen == null && this.keyBindSettings.isPressed()) {
            mc.displayGuiScreen(new GuiArmorsHUDSettings(null));
        }
        // O HUD aparece no jogo normal (janelas fechadas), no inventário e dentro da janela de configações do mod!
        boolean isScreenValid = mc.currentScreen == null || mc.currentScreen instanceof GuiArmorsHUDSettings || mc.currentScreen instanceof GuiInventory;

        if (inGame && mc.thePlayer != null && isScreenValid && !mc.gameSettings.hideGUI) {

            ScaledResolution sr = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderHelper.enableGUIStandardItemLighting();

            int renderX = 0;
            int renderY = 0;

            for (int i = 0; i < 4; ++i) {
                // Lógica de cálculo do Eixo X (Esquerda = 0, 1, 2 | Direita = 3, 4, 5)
                if (hudPosition <= 2) {
                    renderX = 4; // Margem na esquerda
                } else {
                    renderX = screenWidth - 20; // Margem na direita
                }

                // Ordem: Capacete no topo, botas na base;
                if (hudPosition == 0 || hudPosition == 3) {
                    // Topo: (3 - i) garante que o Capacete (3) não desça, e as Botas (0) desçam 60 pixels
                    renderY = 4 + ((3 - i) * 20);
                }
                else if (hudPosition == 1 || hudPosition == 4) {
                    // Centro: Mesma lógica de inversão para manter o capacete no topo do bloco
                    renderY = (screenHeight / 2) - 40 + ((3 - i) * 20);
                }
                else {
                    // Fundo;
                    renderY = screenHeight - 40 - (i * 20);
                }

                this.renderArmor(mc, i, renderX, renderY, partialTicks, mc.thePlayer);
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    private void renderArmor(Minecraft mc, int slotIndex, int x, int y, float partialTicks, EntityPlayer player) {
        ItemStack itemStack = player.inventory.armorInventory[slotIndex];

        if (itemStack != null) {
            float animation = itemStack.animationsToGo - partialTicks;

            // Se o item acabou de ser equipado/trocado, faz a animação de "pulo"
            if (animation > 0.0F) {
                GL11.glPushMatrix();
                float scale = 1.0F + animation / 5.0F;
                GL11.glTranslatef(x + 8, y + 12, 0.0F);
                GL11.glScalef(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef(-(x + 8), -(y + 12), 0.0F);
            }

            // Sintaxe da 1.5.2: Exige FontRenderer e RenderEngine
            this.itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, x, y);

            if (animation > 0.0F) {
                GL11.glPopMatrix();
            }

            // Sintaxe da 1.5.2: Desenha a barra de dano da armadura e a quantidade (se houver)
            this.itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemStack, x, y);

            if (showArmorDamage && itemStack.isItemStackDamageable()) {
                int maxDamage = itemStack.getMaxDamage();
                int currentDamage = itemStack.getItemDamage();
                int remainingHP = maxDamage - currentDamage;

                // Matemática exata do Minecraft para a transição de cor Verde -> Vermelho
                int colorRatio = (int)Math.round(255.0D - (double)currentDamage * 255.0D / (double)maxDamage);
                // Monta o formato ARGB para a fonte (Alpha: 255 | Red: Inverso do colorRatio | Green: colorRatio | Blue: 0)
                int color = (255 << 24) | ((255 - colorRatio) << 16) | (colorRatio << 8);

                String hpText = String.valueOf(remainingHP);
                int textY = y + 4; // Centraliza o texto com o ícone da armadura
                int textX;

                // Se a armadura estiver na ESQUERDA da tela (Posições 0, 1, 2)
                if (hudPosition <= 2) {
                    textX = x + 18; // Desenha à direita do ícone
                }
                // Se a armadura estiver na DIREITA da tela (Posições 3, 4, 5)
                else {
                    textX = x - mc.fontRenderer.getStringWidth(hpText) - 2; // Desenha à esquerda do ícone
                }

                mc.fontRenderer.drawStringWithShadow(hpText, textX, textY, color);
            }
        }
    }

    public static void loadConfig() {
        if (!settingsFile.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(settingsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Position=")) hudPosition = Integer.parseInt(line.split("=")[1]);
                if (line.startsWith("ShowHP=")) showArmorDamage = Boolean.parseBoolean(line.split("=")[1]);
            }
            reader.close();
        } catch (Exception e) {}
    }

    public static void saveConfig() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(settingsFile));
            writer.println("Position=" + hudPosition);
            writer.println("ShowHP=" + showArmorDamage);
            writer.close();
        } catch (Exception e) {}
    }
}