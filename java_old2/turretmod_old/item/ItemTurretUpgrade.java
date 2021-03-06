/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import de.sanandrew.core.manpack.util.helpers.SAPUtils;
import de.sanandrew.mods.turretmod.api.Turret;
import de.sanandrew.mods.turretmod.api.TurretInfo;
import de.sanandrew.mods.turretmod.api.TurretUpgrade;
import de.sanandrew.mods.turretmod.client.util.TmrCreativeTabs;
import de.sanandrew.mods.turretmod.client.util.TurretMod;
import de.sanandrew.mods.turretmod.client.util.TurretRegistry;
import de.sanandrew.mods.turretmod.api.registry.TurretUpgradeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.IIcon;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemTurretUpgrade
        extends Item
{
    @SideOnly(Side.CLIENT)
    public Map<TurretUpgrade, IIcon> upgIcons;
    @SideOnly(Side.CLIENT)
    public IIcon backgroundIcon;

    public ItemTurretUpgrade() {
        super();
        this.setTranslationKey(TurretMod.MOD_ID + ":turret_upgrade");
        this.setCreativeTab(TmrCreativeTabs.UPGRADES);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        List<TurretUpgrade> upgrades = TurretUpgradeRegistry.getAllUpgradesSorted();
        this.upgIcons = new HashMap<>(upgrades.size());

        this.itemIcon = iconRegister.registerIcon(TurretMod.MOD_ID + ":upgrades/empty");
        this.backgroundIcon = iconRegister.registerIcon(TurretMod.MOD_ID + ":upgrades/backg");
        for( TurretUpgrade upgrade : upgrades ) {
            this.upgIcons.put(upgrade, iconRegister.registerIcon(TurretUpgradeRegistry.getItemTextureLoc(upgrade)));
        }
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        TurretUpgrade upgrade = this.getUpgradeFromStack(stack);
        return upgrade == null
               ? this.itemIcon
               : pass == 0
                 ? this.backgroundIcon
                 : this.upgIcons.get(upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List items) {
        List<TurretUpgrade> upgrades = TurretUpgradeRegistry.getAllUpgradesSorted();
        items.add(new ItemStack(this, 1));
        for( TurretUpgrade upgrade : upgrades ) {
            items.add(this.getStackWithUpgrade(upgrade, 1));
        }
    }

    @Override
    public boolean requiresMultipleRenderPasses() {
        return true;
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advTooltip) {
        TurretUpgrade stackUpgrade = this.getUpgradeFromStack(stack);
        String unlocName = this.getTranslationKey(stack);
        String upgName = stackUpgrade == null ? "empty" : stackUpgrade.getName();

        lines.add(TextFormatting.AQUA + SAPUtils.translatePreFormat("%s.%s.name", unlocName, upgName));

        if( Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ) {
            for( Object line : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(SAPUtils.translatePreFormat("%s.%s.desc", unlocName, upgName), 200) ) {
                lines.add(line);
            }

            if( stackUpgrade != null ) {
                if( stackUpgrade.getDependantOn() != null ) {
                    lines.add(TextFormatting.YELLOW + SAPUtils.translatePreFormat("%s.requires", unlocName));
                    lines.add("  " + SAPUtils.translatePreFormat("%s.%s.name", unlocName, stackUpgrade.getDependantOn().getName()));
                }

                List<Class<? extends Turret>> applicables = stackUpgrade.getApplicableTurrets();
                if( applicables.size() > 0 ) {
                    lines.add(TextFormatting.RED + SAPUtils.translatePreFormat("%s.applicableTo", unlocName));
                    for( Class<? extends Turret> cls : applicables ) {
                        TurretInfo<?> info = TurretRegistry.getTurretInfo(cls);
                        if( info != null ) {
                            String entityName = cls != null ? (String) EntityList.classToStringMapping.get(cls) : "UNKNOWN";
                            lines.add("  " + SAPUtils.translatePreFormat("entity.%s.name", entityName));
                        }
                    }
                }
            }
        } else {
            lines.add(TextFormatting.ITALIC + SAPUtils.translatePreFormat("%s.shdetails", unlocName));
        }
    }

    public ItemStack getStackWithUpgrade(TurretUpgrade upgrade, int stackSize) {
        ItemStack stack = new ItemStack(this, stackSize);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("turretUpgrade", TurretUpgradeRegistry.getRegistrationName(upgrade));
        stack.setTagCompound(nbt);
        return stack;
    }

    public TurretUpgrade getUpgradeFromStack(ItemStack stack) {
        if( stack.getItem() == this && stack.hasTagCompound() ) {
            String turretUpgName = stack.getTagCompound().getString("turretUpgrade");
            if( !turretUpgName.isEmpty() ) {
                return TurretUpgradeRegistry.getUpgrade(turretUpgName);
            }
        }

        return null;
    }
}
