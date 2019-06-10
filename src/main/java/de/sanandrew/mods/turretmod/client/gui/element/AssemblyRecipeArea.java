/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import de.sanandrew.mods.turretmod.client.gui.assembly.GuiTurretAssemblyNEW;
import de.sanandrew.mods.turretmod.registry.assembly.AssemblyManager;
import de.sanandrew.mods.turretmod.util.TmrUtils;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssemblyRecipeArea
        implements IGuiElement
{
    public static final ResourceLocation ID = new ResourceLocation("assembly_recipes");

    private Map<String, GroupData> recipeGroups;
    private int width;
    private int height;
    private boolean updatedAll;

    GuiElementInst activeRecipeMarker;
    GuiElementInst recipeLabel;

    @Override
    public void bakeData(IGui gui, JsonObject data) {
        if( this.recipeGroups == null ) {
            this.recipeGroups = new HashMap<>();
            JsonObject dataArea = data.getAsJsonObject("scrollArea");
            for( String grp : AssemblyManager.INSTANCE.getGroups() ) {
                this.recipeGroups.put(grp, new GroupData(gui, grp, dataArea));

                if( GuiTurretAssemblyNEW.currGroup == null ) {
                    GuiTurretAssemblyNEW.currGroup = grp;
                }
            }

            JsonElement jActiveRecipeMarker = data.get("activeRecipeMarker");
            if( jActiveRecipeMarker != null ) {
                this.activeRecipeMarker = JsonUtils.GSON.fromJson(jActiveRecipeMarker, GuiElementInst.class);
                gui.getDefinition().initElement(this.activeRecipeMarker);
                this.activeRecipeMarker.get().bakeData(gui, this.activeRecipeMarker.data);
            }

            this.recipeLabel = JsonUtils.GSON.fromJson(data.get("recipeLabel"), GuiElementInst.class);
            gui.getDefinition().initElement(this.recipeLabel);
            TmrUtils.addJsonProperty(this.recipeLabel.data, "size", new int[] {18, 18});
            this.recipeLabel.get().bakeData(gui, this.recipeLabel.data);
        }
    }

    @Override
    public void update(IGui gui, JsonObject data) {
        GroupData grpData = this.recipeGroups.get(GuiTurretAssemblyNEW.currGroup);
        GuiTurretAssemblyNEW gta = (GuiTurretAssemblyNEW) gui;
        this.width = grpData.area.getWidth();
        this.height = grpData.area.getHeight();

        if( !this.updatedAll ) {
            this.recipeGroups.values().forEach(g -> g.area.update(gui, g.data));
            this.updatedAll = true;
        }

        if( gta.hoveredRecipe != null && gta.hoveredRecipeCoords != null ) {
            this.recipeLabel.get().update(gui, this.recipeLabel.data);
        }

        grpData.area.update(gui, grpData.data);
    }

    @Override
    public void render(IGui gui, float partTicks, int x, int y, int mouseX, int mouseY, JsonObject data) {
        GroupData grpData = this.recipeGroups.get(GuiTurretAssemblyNEW.currGroup);
        GuiTurretAssemblyNEW gta = (GuiTurretAssemblyNEW) gui;

        grpData.area.render(gui, partTicks, x, y, mouseX, mouseY, grpData.data);

        if( this.activeRecipeMarker != null ) {
            if( gta.currRecipeCoords != null ) {
                this.activeRecipeMarker.get().render(gui, partTicks,
                                                     this.activeRecipeMarker.pos[0] + gta.currRecipeCoords[0], this.activeRecipeMarker.pos[1] + gta.currRecipeCoords[1],
                                                     mouseX, mouseY,
                                                     data);
            }
        }

        if( gta.hoveredRecipe != null && gta.hoveredRecipeCoords != null ) {
            this.recipeLabel.get().render(gui, partTicks, gta.hoveredRecipeCoords[0], gta.hoveredRecipeCoords[1], mouseX, mouseY, this.recipeLabel.data);
        }
    }

    @Override
    public void handleMouseInput(IGui gui) {
        this.recipeGroups.get(GuiTurretAssemblyNEW.currGroup).area.handleMouseInput(gui);
    }

    @Override
    public void mouseClicked(IGui gui, int mouseX, int mouseY, int mouseButton) throws IOException {
        this.recipeGroups.get(GuiTurretAssemblyNEW.currGroup).area.mouseClicked(gui, mouseX, mouseY, mouseButton);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    private static final class GroupData
    {
        AssemblyRecipes area;
        JsonObject data;

        GroupData(IGui gui, String grp, JsonObject data) {
            this.area = new AssemblyRecipes(grp);
            this.data = deepCopy(data);
            this.area.bakeData(gui, this.data);
        }

        private static JsonObject deepCopy(JsonObject obj) {
            return JsonUtils.GSON.fromJson(JsonUtils.GSON.toJson(obj), JsonObject.class);
        }
    }
}