/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.inventory.container;

import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.tileentity.assembly.TileEntityTurretAssembly;
import de.sanandrew.mods.turretmod.util.TmrUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ContainerTurretAssembly
        extends Container
{
    private final IInventory inventory;
    private final TileEntityTurretAssembly tile;

    public ContainerTurretAssembly(IInventory playerInv, TileEntityTurretAssembly assembly) {
        this.tile = assembly;
        this.inventory = this.tile.getInventory();

        IInventory tileInv = assembly.getInventory();
        this.addSlotToContainer(new SlotOutput(tileInv, 0, 163, 10));
        this.addSlotToContainer(new SlotOutput(tileInv, 4, 181, 10));
        this.addSlotToContainer(new SlotAutoUpgrade());
        this.addSlotToContainer(new SlotSpeedUpgrade());
        this.addSlotToContainer(new SlotFilterUpgrade());

        for( int i = 0; i < 2; i++ ) {
            for( int j = 0; j < 9; j++ ) {
                this.addSlotToContainer(new SlotIngredients(j + i * 9 + 5, 36 + j * 18, 100 + i * 18));
            }
        }

        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 9; j++ ) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 36 + j * 18, 140 + i * 18));
            }
        }

        for( int i = 0; i < 9; i++ ) {
            this.addSlotToContainer(new Slot(playerInv, i, 36 + i * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.inventory.isUsableByPlayer(player);
    }

    private boolean transferUpgrade(Item desiredItm, @Nonnull ItemStack origStack, @Nonnull ItemStack slotStack, int upgSlot) {
        int origStackSize = desiredItm.getItemStackLimit(origStack);
        desiredItm.setMaxStackSize(1);
        slotStack.setCount(1);
        if( !super.mergeItemStack(slotStack, 1 + upgSlot, 2 + upgSlot, false) ) {
            slotStack.setCount(origStack.getCount());
            desiredItm.setMaxStackSize(origStackSize);
            return true;
        } else {
            slotStack.setCount(origStack.getCount() - 1);
        }
        desiredItm.setMaxStackSize(origStackSize);
        return false;
    }

    @Override
    protected boolean mergeItemStack(@Nonnull ItemStack stack, int beginSlot, int endSlot, boolean reverse) {
        return TmrUtils.mergeItemStack(this, stack, beginSlot, endSlot, reverse);
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        ItemStack origStack = ItemStackUtils.getEmpty();
        Slot slot = this.inventorySlots.get(slotId);

        if( slot != null && slot.getHasStack() ) {
            ItemStack slotStack = slot.getStack();
            origStack = slotStack.copy();

            if( slotId < 23 ) { // if clicked stack is from TileEntity
                if( !super.mergeItemStack(slotStack, 23, 59, true) ) {
                    return ItemStackUtils.getEmpty();
                }
            } else if( origStack.getItem() == ItemRegistry.ASSEMBLY_UPG_AUTO ) {
                if( transferUpgrade(ItemRegistry.ASSEMBLY_UPG_AUTO, origStack, slotStack, 0) ) {
                    return ItemStackUtils.getEmpty();
                }
            } else if( origStack.getItem() == ItemRegistry.ASSEMBLY_UPG_SPEED ) {
                if( transferUpgrade(ItemRegistry.ASSEMBLY_UPG_SPEED, origStack, slotStack, 1) ) {
                    return ItemStackUtils.getEmpty();
                }
            } else if( origStack.getItem() == ItemRegistry.ASSEMBLY_UPG_FILTER ) {
                if( transferUpgrade(ItemRegistry.ASSEMBLY_UPG_FILTER, origStack, slotStack, 2) ) {
                    return ItemStackUtils.getEmpty();
                }
            } else if( !this.mergeItemStack(slotStack, 5, 23, false) ) { // if clicked stack is from player and also merge to input slots is sucessful
                return ItemStackUtils.getEmpty();
            }

            if( TmrUtils.finishTransfer(player, origStack, slot, slotStack) ) {
                return ItemStackUtils.getEmpty();
            }
        }

        return origStack;
    }

    private class SlotIngredients
            extends Slot
    {
        SlotIngredients(int id, int x, int y) {
            super(ContainerTurretAssembly.this.inventory, id, x, y);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return super.isItemValid(stack) && ContainerTurretAssembly.this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
        }
    }

    private class SlotAutoUpgrade
            extends Slot
    {
        SlotAutoUpgrade() {
            super(ContainerTurretAssembly.this.inventory, 1, 14, 100);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return !ContainerTurretAssembly.this.tile.hasAutoUpgrade() && stack.getItem() == ItemRegistry.ASSEMBLY_UPG_AUTO;
        }
    }

    private class SlotSpeedUpgrade
            extends Slot
    {
        SlotSpeedUpgrade() {
            super(ContainerTurretAssembly.this.inventory, 2, 14, 118);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return !ContainerTurretAssembly.this.tile.hasSpeedUpgrade() && stack.getItem() == ItemRegistry.ASSEMBLY_UPG_SPEED;
        }
    }

    private class SlotFilterUpgrade
            extends Slot
    {
        SlotFilterUpgrade() {
            super(ContainerTurretAssembly.this.inventory, 3, 202, 100);
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return !ContainerTurretAssembly.this.tile.hasFilterUpgrade() && stack.getItem() == ItemRegistry.ASSEMBLY_UPG_FILTER;
        }
    }
}