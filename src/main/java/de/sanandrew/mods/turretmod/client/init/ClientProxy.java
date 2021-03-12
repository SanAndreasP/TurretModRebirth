package de.sanandrew.mods.turretmod.client.init;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiDefinition;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.turretmod.client.gui.GuiElectrolyteGenerator;
import de.sanandrew.mods.turretmod.client.gui.element.ElectrolyteBar;
import de.sanandrew.mods.turretmod.init.IProxy;
import de.sanandrew.mods.turretmod.inventory.ContainerRegistry;
import de.sanandrew.mods.turretmod.world.PlayerList;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;
import java.util.UUID;

public class ClientProxy
        implements IProxy
{
    @Override
    public void setupClient(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ContainerRegistry.ELECTROLYTE_GENERATOR, GuiElectrolyteGenerator::new);

        GuiDefinition.TYPES.put(ElectrolyteBar.ID, ElectrolyteBar::new);
    }

    @Override
    public void fillPlayerListClient(Map<UUID, ITextComponent> map) {
        PlayerList.INSTANCE.putPlayersClient(map);
    }

    public static void initGuiDef(GuiDefinition guiDef, IGui gui) {
        if( guiDef == null ) {
            gui.get().getMinecraft().displayGuiScreen(null);
            return;
        }

        guiDef.initGui(gui);
    }

    public static void drawGDBackground(GuiDefinition guiDef, MatrixStack stack, IGui gui, float partTicks, int mouseX, int mouseY) {
        stack.push();
        stack.translate(gui.getScreenPosX(), gui.getScreenPosY(), 0.0F);
        guiDef.drawBackground(gui, stack, mouseX, mouseY, partTicks);
        stack.pop();
    }
}
