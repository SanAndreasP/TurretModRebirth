/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.compat.jei;

import de.sanandrew.mods.turretmod.api.assembly.IAssemblyRecipe;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.AmmunitionRegistry;
import de.sanandrew.mods.turretmod.registry.ammo.Ammunitions;
import de.sanandrew.mods.turretmod.registry.assembly.AssemblyManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JeiPlugin
    implements IModPlugin
{
    public JeiPlugin() { }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(AmmunitionRegistry.INSTANCE.getItem(Ammunitions.TIPPED_BOLT.getId()).getItem(),
                                                   AmmoSubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new AssemblyRecipeCategory<>(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.handleRecipes(IAssemblyRecipe.class, AssemblyRecipeWrapper::new, AssemblyRecipeCategory.UID);

        registry.addRecipes(AssemblyManager.INSTANCE.getRecipes(), AssemblyRecipeCategory.UID);
        registry.addRecipes(TippedBoltRecipeWrapper.getRecipes(), VanillaRecipeCategoryUid.CRAFTING);

        registry.addRecipeCatalyst(new ItemStack(BlockRegistry.TURRET_ASSEMBLY), AssemblyRecipeCategory.UID);
    }
}
