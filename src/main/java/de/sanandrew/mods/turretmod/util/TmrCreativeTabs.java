/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.util;

import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TmrCreativeTabs
{
    private static final Comparator<ItemStack> ITM_NAME_COMP = new ItemNameComparator();

    public static final CreativeTabs TURRETS = new CreativeTabs(TmrConstants.ID + ":turrets") {
        private List<ItemStack> tabIcons = null;

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return getIconItemStack().getItem();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getIconItemStack() {
            if( this.tabIcons == null ) {
                this.tabIcons = new ArrayList<>();
                ItemRegistry.TURRET_PLACER.getSubItems(ItemRegistry.TURRET_PLACER, this, this.tabIcons);
            }

            return this.tabIcons.get((int) (System.currentTimeMillis() / 4250L) % this.tabIcons.size());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(List<ItemStack> itmList) {
            super.displayAllRelevantItems(itmList);

            itmList.sort((itm1, itm2) -> {
                if( itm1 != null && itm1.getItem() == ItemRegistry.TURRET_PLACER ) {
                    return itm2 != null && itm2.getItem() == ItemRegistry.TURRET_PLACER ? 0 : -2;
                } else if( itm2 != null && itm2.getItem() == ItemRegistry.TURRET_PLACER ) {
                    return 2;
                } else if( itm1 != null && itm1.getItem() == ItemRegistry.TURRET_AMMO ) {
                    return itm2 != null && itm2.getItem() == ItemRegistry.TURRET_AMMO ? 0 : -1;
                } else if( itm2 != null && itm2.getItem() == ItemRegistry.TURRET_AMMO ) {
                    return 1;
                }

                return 0;
            });
        }
    };

    public static final CreativeTabs MISC = new CreativeTabs(TmrConstants.ID + ":misc") {
        private ItemStack currTabIcon = ItemStackUtils.getEmpty();

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            if( !ItemStackUtils.isValid(this.currTabIcon) ) {
                this.currTabIcon = new ItemStack(ItemRegistry.TURRET_CONTROL_UNIT, 1);
            }

            return this.currTabIcon.getItem();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(List<ItemStack> itmList) {
            super.displayAllRelevantItems(itmList);

            itmList.sort((itm1, itm2) -> {
                if( itm1 != null && itm1.getItem() instanceof ItemBlock ) {
                    return itm2 != null && itm2.getItem() instanceof ItemBlock ? 0 : -2;
                } else if( itm2 != null && itm2.getItem() instanceof ItemBlock ) {
                    return 2;
                } else if( itm1 != null && itm1.getItem() == ItemRegistry.REPAIR_KIT ) {
                    return itm2 != null && itm2.getItem() == ItemRegistry.REPAIR_KIT ? 0 : 1;
                } else if( itm2 != null && itm2.getItem() == ItemRegistry.REPAIR_KIT ) {
                    return -1;
                }

                return 0;
            });
        }
    };

    public static final CreativeTabs UPGRADES = new CreativeTabs(TmrConstants.ID + ":upgrades") {
        private List<ItemStack> tabIcons = null;

        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return getIconItemStack().getItem();
        }

        @Override
        @SideOnly(Side.CLIENT)
        public ItemStack getIconItemStack() {
            if( this.tabIcons == null ) {
                this.tabIcons = new ArrayList<>();
                ItemRegistry.TURRET_UPGRADE.getSubItems(ItemRegistry.TURRET_PLACER, this, this.tabIcons);
            }

            return this.tabIcons.get((int) (System.currentTimeMillis() / 4250L) % this.tabIcons.size());
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void displayAllRelevantItems(List<ItemStack> itmList) {
            super.displayAllRelevantItems(itmList);

            sortItemsByName(itmList);
            sortItemsBySubItems(itmList, this);
        }
    };

    protected static void sortItemsByName(List<ItemStack> items) {
        items.sort(ITM_NAME_COMP);
    }

    protected static void sortItemsBySubItems(final List<ItemStack> items, final CreativeTabs tab) {
        items.sort(new ItemSubComparator(tab));
    }

    private static class ItemNameComparator implements Comparator<ItemStack>
    {
        @Override
        public int compare(ItemStack stack1, ItemStack stack2) {
            return stack2.getUnlocalizedName().compareTo(stack1.getUnlocalizedName());
        }
    }

    private static class ItemSubComparator implements Comparator<ItemStack>
    {
        private final CreativeTabs tab;

        private ItemSubComparator(CreativeTabs thisTab) {
            this.tab = thisTab;
        }

        @Override
        public int compare(ItemStack o1, ItemStack o2) {
            if( o1.getItem() != o2.getItem() ) {
                return -1;
            }

            List<ItemStack> subItms = new ArrayList<>();
            o1.getItem().getSubItems(ItemRegistry.TURRET_PLACER, this.tab, subItms);

            return Integer.compare(getStackIndexInList(o1, subItms), getStackIndexInList(o2, subItms));
        }

        private static int getStackIndexInList(ItemStack stack, List<ItemStack> stackArray) {
            for( ItemStack stackElem : stackArray ) {
                if( ItemStackUtils.areEqual(stack, stackElem, true) ) {
                    return stackArray.indexOf(stackElem);
                }
            }

            return -1;
        }
    }
}
