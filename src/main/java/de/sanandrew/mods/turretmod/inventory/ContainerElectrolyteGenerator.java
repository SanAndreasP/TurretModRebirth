/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.inventory;

import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteProcess;
import de.sanandrew.mods.turretmod.tileentity.electrolytegen.TileEntityElectrolyteGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class ContainerElectrolyteGenerator
        extends Container
{
    private final TileEntityElectrolyteGenerator tile;

    public ContainerElectrolyteGenerator(IInventory playerInv, TileEntityElectrolyteGenerator generator) {
        this.tile = generator;

        for( int i = 0; i < 9; i++ ) {
            this.addSlotToContainer(new SlotItemHandler(generator.containerItemHandler, i, 8 + i*18, 17));
        }
        for( int i = 0; i < 9; i++ ) {
            this.addSlotToContainer(new SlotProcessing(generator, i, 8 + i*18, 43));
        }
        for( int i = 0; i < 5; i++ ) {
            this.addSlotToContainer(new SlotItemHandler(generator.containerItemHandler, i+9, 44 + i*18, 76));
        }

        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 9; j++ ) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }

        for( int i = 0; i < 9; i++ ) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile.isUseableByPlayer(player);
    }

    protected boolean mergeItemStackInput(@Nonnull ItemStack stack, int beginSlot, int endSlot, boolean reverse) {
        boolean slotChanged = false;
        int start;
        Slot slot;

        if( reverse ) {
            start = endSlot - 1;
        } else {
            start = beginSlot;
        }

        while( !reverse && start < endSlot || reverse && start >= beginSlot ) {
            slot = this.inventorySlots.get(start);

            if( !ItemStackUtils.isValid(slot.getStack()) && slot.isItemValid(stack) ) {
                slot.putStack(stack.copy());
                slot.getStack().setCount(1);
                slot.onSlotChanged();
                stack.shrink(1);
                slotChanged = true;
                break;
            }

            if( reverse ) {
                start--;
            } else {
                start++;
            }
        }

        return slotChanged;
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int slotId) {
        ItemStack origStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotId);

        if( slot != null && slot.getHasStack() ) {
            ItemStack slotStack = slot.getStack();
            origStack = slotStack.copy();

            if( slotId < 23 ) { // if clicked stack is from TileEntity
                if( !this.mergeItemStack(slotStack, 22, 58, true) ) {
                    return ItemStack.EMPTY;
                }
            } else if( !this.mergeItemStackInput(slotStack, 0, 9, false) ) { // if clicked stack is from player and also merge to input slots is sucessful
                return ItemStack.EMPTY;
            }

            if( slotStack.getCount() == 0 ) { // if stackSize of slot got to 0
                slot.putStack(ItemStack.EMPTY);
            } else { // update changed slot stack state
                slot.onSlotChanged();
            }

            if( slotStack.getCount() == origStack.getCount() ) { // if nothing changed stackSize-wise
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return origStack;
    }

    public static class SlotProcessing
            extends Slot
    {
        private static final IInventory EMPTY_INV = new InventoryBasic("[Null]", true, 0);
        private final TileEntityElectrolyteGenerator generator;
        private final int index;

        public SlotProcessing(TileEntityElectrolyteGenerator generator, int id, int x, int y) {
            super(EMPTY_INV, id, x, y);
            this.generator = generator;
            this.index = id;
        }

        @Override
        public boolean isItemValid(@Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return false;
        }

        @Override
        @Nonnull
        public ItemStack getStack() {
            ElectrolyteProcess proc = this.generator.processes[this.index];
            return proc == null ? ItemStack.EMPTY : proc.processStack;
        }

        @Override
        public void putStack(@Nonnull ItemStack stack) { }

        @Override
        public void onSlotChange(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) { }

        @Override
        public int getItemStackLimit(@Nonnull ItemStack stack) {
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack decrStackSize(int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isSameInventory(Slot other) {
            return other instanceof SlotProcessing && ((SlotProcessing) other).generator == this.generator;
        }
    }
}
