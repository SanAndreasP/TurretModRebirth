/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.item;

import com.mojang.realmsclient.gui.ChatFormatting;
import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.registry.Resources;
import de.sanandrew.mods.turretmod.registry.TmrCreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ItemTurretInfo
        extends Item
{
    public static final boolean PATCHOULI_AVAILABLE = Loader.isModLoaded("patchouli");

    public ItemTurretInfo() {
        super();
        this.setCreativeTab(TmrCreativeTabs.MISC);
        this.setTranslationKey(TmrConstants.ID + ":turret_info");
        this.setRegistryName(TmrConstants.ID, "turret_info");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, List<String> list, ITooltipFlag advInfo) {
        String[] lines = LangUtils.translate(this.getTranslationKey() + ".desc").split("\\n");
        list.addAll(Arrays.asList(lines));

        if( !PATCHOULI_AVAILABLE ) {
            list.add(ChatFormatting.RED + "Patchouli is required in order to view this content!");
        }

        super.addInformation(stack, world, list, advInfo);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if( PATCHOULI_AVAILABLE ) {
            if( player instanceof EntityPlayerMP ) {
                vazkii.patchouli.api.PatchouliAPI.instance.openBookGUI((EntityPlayerMP) player, Resources.PATCHOULI.resource);
            }
        } else {

        }
//        if( world.isRemote ) {
//            TurretModRebirth.proxy.openGui(player, EnumGui.TINFO, -1, -1, 0);
//        }

        return super.onItemRightClick(world, player, hand);
    }
}
