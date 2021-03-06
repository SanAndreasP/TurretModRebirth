/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.api.turret;

import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTCU;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTcuInst;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IGuiTcuRegistry
{
    void registerPage(ResourceLocation key, int position, @Nullable ContainerFactory containerFactory);

    @SideOnly(Side.CLIENT)
    void registerPageGUI(ResourceLocation key, ItemStack icon, Supplier<IGuiTCU> factory, Function<IGuiTcuInst<?>, Boolean> canShowTabFunc);

    @SideOnly(Side.CLIENT)
    void registerPageGUI(ResourceLocation key, Supplier<ItemStack> iconSupplier, Supplier<IGuiTCU> factory, Function<IGuiTcuInst<?>, Boolean> canShowTabFunc);

    @FunctionalInterface
    interface ContainerFactory
    {
        Container get(EntityPlayer player, ITurretInst turretInst, boolean isRemote);
    }
}
