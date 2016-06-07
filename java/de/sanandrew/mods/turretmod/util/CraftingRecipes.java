/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.turretmod.block.BlockRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class CraftingRecipes
{
    public static IRecipe assemblyTable;
    public static IRecipe potatoGenerator;

    @SuppressWarnings("unchecked")
    public static void initialize() {
        assemblyTable = CraftingManager.getInstance().addRecipe(new ItemStack(BlockRegistry.assemblyTable, 1),
                "ROR", "IAI", "CFC",
                'R', new ItemStack(Items.repeater),
                'O', new ItemStack(Blocks.obsidian),
                'I', new ItemStack(Items.iron_ingot),
                'A', new ItemStack(Blocks.anvil, 1, 0),
                'C', new ItemStack(Blocks.cobblestone),
                'F', new ItemStack(Blocks.furnace));

        potatoGenerator = new ShapedOreRecipe(BlockRegistry.potatoGenerator,
                true,
                "IBG", "RCR", "BPB",
                'I', "ingotIron",
                'B', "ingotBrick",
                'G', "ingotGold",
                'R', "dustRedstone",
                'C', Items.cauldron,
                'P', Items.repeater);
        CraftingManager.getInstance().getRecipeList().add(potatoGenerator);
    }
}
