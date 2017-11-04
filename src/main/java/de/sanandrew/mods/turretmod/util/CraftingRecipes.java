/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

public final class CraftingRecipes
{
    public static IRecipe assemblyTable;
    public static IRecipe electrolyteGenerator;

    @SuppressWarnings("unchecked")
    public static void initialize() {
        assemblyTable = CraftingManager.func_77594_a().func_92103_a(new ItemStack(BlockRegistry.TURRET_ASSEMBLY, 1),
                "ROR", "IAI", "CFC",
                'R', new ItemStack(Items.REPEATER),
                'O', new ItemStack(Blocks.OBSIDIAN),
                'I', new ItemStack(Items.IRON_INGOT),
                'A', new ItemStack(Blocks.ANVIL, 1, 0),
                'C', new ItemStack(Blocks.COBBLESTONE),
                'F', new ItemStack(Blocks.FURNACE));

        electrolyteGenerator = new ShapedOreRecipe(BlockRegistry.ELECTROLYTE_GENERATOR,
                true,
                "IBG", "RCR", "BPB",
                'I', "ingotIron",
                'B', "ingotBrick",
                'G', "ingotGold",
                'R', "dustRedstone",
                'C', Items.CAULDRON,
                'P', Items.REPEATER);
        CraftingManager.func_77594_a().func_77592_b().add(electrolyteGenerator);

        CraftingManager.func_77594_a().func_77596_b(new ItemStack(ItemRegistry.TURRET_INFO, 1), Items.WRITABLE_BOOK, Blocks.DISPENSER);
    }
}