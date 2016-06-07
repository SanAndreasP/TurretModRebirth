/**
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.tileentity;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketSyncTileEntity;
import de.sanandrew.mods.turretmod.network.TileClientSync;
import de.sanandrew.mods.turretmod.util.TmrUtils;
import io.netty.buffer.ByteBuf;
import net.darkhax.bookshelf.lib.javatuples.Quartet;
import net.darkhax.bookshelf.lib.util.ItemStackUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class TileEntityPotatoGenerator
        extends TileEntity
        implements ISidedInventory, TileClientSync, IEnergyProvider
{
    public static final int MAX_FLUX_STORAGE = 500_000;
    public static final int MAX_FLUX_EXTRACT = 1_000;
    public static final int MAX_FLUX_GENERATED = 200;

    public int fluxExtractPerTick;
    public short[] progress = new short[9];
    public float effectiveness;

    private ItemStack[] invStacks = new ItemStack[23];
    private static final int[] SLOTS_INSERT = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] SLOTS_PROCESSING = new int[] {9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int[] SLOTS_EXTRACT = new int[] {18, 19, 20, 21, 22};
    private ItemStack[] progExcessComm = new ItemStack[this.progress.length];
    private ItemStack[] progExcessRare = new ItemStack[this.progress.length];

    private int fluxAmount;
    private int prevFluxAmount;
    private boolean doSync;

    private int fluxBuffer;

    private static final Map<Item, Fuel> FUELS = new HashMap<>(3);

    public static void initializeRecipes() {
        FUELS.put(Items.potato, new Fuel(1.0F, (short) 200, new ItemStack(Items.sugar, 1), new ItemStack(Items.baked_potato, 1)));
        FUELS.put(Items.carrot, new Fuel(1.0F, (short) 200, new ItemStack(Items.sugar, 1), new ItemStack(Items.redstone, 1)));
        FUELS.put(Items.poisonous_potato, new Fuel(1.2F, (short) 150, new ItemStack(Items.sugar, 1), new ItemStack(Items.nether_wart, 1)));
        FUELS.put(Items.apple, new Fuel(1.3F, (short) 220, new ItemStack(Items.wheat_seeds, 1), new ItemStack(Items.gold_nugget, 1)));
    }

    public static Map<Item, Fuel> getFuels() {
        return new HashMap<>(FUELS);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side == ForgeDirection.DOWN.ordinal() ? SLOTS_EXTRACT : side == ForgeDirection.UP.ordinal() ? new int[0] : SLOTS_INSERT;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return this.isItemValidForSlot(slot, stack) && side != ForgeDirection.DOWN.ordinal() && side != ForgeDirection.UP.ordinal();
    }

    public int getGeneratedFlux() {
        return this.effectiveness < 0.1F ? 0 : (int) Math.round(Math.pow(1.6D, this.effectiveness) / (68.0D + (127433.0D / 177119.0D)) * 80.0D);
    }

    @Override
    public void updateEntity() {
        if( !this.worldObj.isRemote ) {
            this.fluxExtractPerTick = Math.min(this.fluxAmount, MAX_FLUX_EXTRACT);

            float prevEffective = this.effectiveness;

            if( this.fluxBuffer > 0 ) {
                int fluxSubtracted = Math.min(MAX_FLUX_STORAGE - this.fluxAmount, Math.min(MAX_FLUX_GENERATED, this.fluxBuffer));
                this.fluxBuffer -= fluxSubtracted;
                this.fluxAmount += fluxSubtracted;
            }

            if( this.fluxBuffer <= MAX_FLUX_GENERATED && this.fluxAmount < MAX_FLUX_STORAGE ) {
                int fluxEff = this.getGeneratedFlux();

                this.effectiveness = 0.0F;

                for( int i = 0; i < SLOTS_PROCESSING.length; i++ ) {
                    if( this.invStacks[SLOTS_PROCESSING[i]] != null ) {
                        if( this.progExcessComm[i] != null && !TmrUtils.canStackFitInInventory(this.progExcessComm[i], this, true, 64, SLOTS_EXTRACT[0], SLOTS_EXTRACT[SLOTS_EXTRACT.length - 1]) ) {
                            continue;
                        }
                        if( this.progExcessRare[i] != null && !TmrUtils.canStackFitInInventory(this.progExcessRare[i], this, true, 64, SLOTS_EXTRACT[0], SLOTS_EXTRACT[SLOTS_EXTRACT.length - 1]) ) {
                            continue;
                        }

                        if( this.progress[i]-- < 0 ) {
                            if( this.progExcessComm[i] != null ) {
                                TmrUtils.addStackToInventory(this.progExcessComm[i], this, true, 64);
                            }
                            if( this.progExcessRare[i] != null ) {
                                TmrUtils.addStackToInventory(this.progExcessRare[i], this, true, 64);
                            }
                            this.invStacks[SLOTS_PROCESSING[i]] = null;
                            this.markDirty();
                        } else {
                            this.effectiveness += FUELS.get(this.invStacks[SLOTS_PROCESSING[i]].getItem()).effect;
                        }
                        this.doSync = true;
                    }

                    if( this.invStacks[SLOTS_PROCESSING[i]] == null && this.invStacks[SLOTS_INSERT[i]] != null ) {
                        this.invStacks[SLOTS_PROCESSING[i]] = this.invStacks[SLOTS_INSERT[i]].copy();
                        this.invStacks[SLOTS_PROCESSING[i]].stackSize = 1;
                        if( --this.invStacks[SLOTS_INSERT[i]].stackSize < 1 ) {
                            this.invStacks[SLOTS_INSERT[i]] = null;
                        }

                        Fuel fuel = FUELS.get(this.invStacks[SLOTS_PROCESSING[i]].getItem());
                        this.progress[i] = fuel.ticksProc;
                        this.progExcessComm[i] = TmrUtils.RNG.nextInt(10) == 0 ? fuel.trash.copy() : null;
                        this.progExcessRare[i] = TmrUtils.RNG.nextInt(100) == 0 ? fuel.treasure.copy() : null;

                        this.markDirty();
                        this.doSync = true;
                    }
                }

                if( this.effectiveness > 0.1F ) {
                    this.fluxBuffer += fluxEff;
                }
            }

            if( prevEffective < this.effectiveness - 0.01F || prevEffective > this.effectiveness + 0.01F ) {
                this.doSync = true;
            }

            if( this.fluxExtractPerTick > 0 ) {
                for( ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS ) {
                    if( direction == ForgeDirection.UP ) {
                        continue;
                    }

                    TileEntity te = this.worldObj.getTileEntity(this.xCoord + direction.offsetX, this.yCoord + direction.offsetY, this.zCoord + direction.offsetZ);

                    if( te instanceof IEnergyReceiver ) {
                        IEnergyReceiver receiver = (IEnergyReceiver) te;

                        if( !receiver.canConnectEnergy(direction) ) {
                            continue;
                        }

                        int extractable = this.extractEnergy(direction, MAX_FLUX_EXTRACT, true);
                        int receivable = receiver.receiveEnergy(direction.getOpposite(), extractable, false);

                        this.extractEnergy(direction, receivable, false);
                    }

                    if( this.fluxExtractPerTick <= 0 ) {
                        break;
                    }
                }
            }

            if( this.prevFluxAmount != this.fluxAmount ) {
                this.doSync = true;
            }

            if( this.doSync ) {
                PacketRegistry.sendToAllAround(new PacketSyncTileEntity(this), this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 64.0D);
            }

            this.prevFluxAmount = this.fluxAmount;
        }
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return side == ForgeDirection.DOWN.ordinal() && ArrayUtils.contains(SLOTS_EXTRACT, slot);
    }

    @Override
    public int getSizeInventory() {
        return this.invStacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.invStacks[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int size) {
        if( this.invStacks[slot] != null ) {
            ItemStack itemstack;

            if( this.invStacks[slot].stackSize <= size ) {
                itemstack = this.invStacks[slot];
                this.invStacks[slot] = null;
                return itemstack;
            } else {
                itemstack = this.invStacks[slot].splitStack(size);

                if( this.invStacks[slot].stackSize == 0 ) {
                    this.invStacks[slot] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readNbt(pkt.func_148857_g());
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeNbt(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        this.writeNbt(nbt);
    }

    private void writeNbt(NBTTagCompound nbt) {
        nbt.setInteger("fluxAmount", this.fluxAmount);
        nbt.setInteger("fluxBuffer", this.fluxBuffer);
        NBTTagList progress = new NBTTagList();
        for( short s : this.progress ) {
            progress.appendTag(new NBTTagShort(s));
        }
        nbt.setTag("progress", progress);

        nbt.setTag("inventory", TmrUtils.writeItemStacksToTag(this.invStacks, 64));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.readNbt(nbt);

        for( int i = 0; i < this.progress.length; i++ ) {
            if( this.invStacks[SLOTS_PROCESSING[i]] != null ) {
                Fuel fuel = FUELS.get(this.invStacks[SLOTS_PROCESSING[i]].getItem());
                this.progExcessComm[i] = TmrUtils.RNG.nextInt(100) == 0 ? fuel.trash.copy() : null;
                this.progExcessRare[i] = TmrUtils.RNG.nextInt(100) == 0 ? fuel.treasure.copy() : null;
            }
        }
    }

    private void readNbt(NBTTagCompound nbt) {
        this.fluxAmount = nbt.getInteger("fluxAmount");
        this.fluxBuffer = nbt.getInteger("fluxBuffer");
        NBTTagList progress = nbt.getTagList("progress", Constants.NBT.TAG_SHORT);
        for( int i = 0; i < this.progress.length; i++ ) {
            this.progress[i] = TmrUtils.getShortTagAt(progress, i);
        }

        TmrUtils.readItemStacksFromTag(this.invStacks, nbt.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if( this.invStacks[slot] != null ) {
            ItemStack itemstack = this.invStacks[slot];
            this.invStacks[slot] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.invStacks[slot] = stack;

        int stackLimit = ArrayUtils.contains(SLOTS_EXTRACT, slot) ? 64 : this.getInventoryStackLimit();
        if( stack != null && stack.stackSize > stackLimit ) {
            stack.stackSize = stackLimit;
        }
    }

    @Override
    public String getInventoryName() {
        return "";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if( !ItemStackUtils.isValidStack(stack) ) {
            return stack == null;
        }

        if( ArrayUtils.contains(SLOTS_INSERT, slot) ) {
            return FUELS.containsKey(stack.getItem()) && this.invStacks[slot] == null;
        }

        return ArrayUtils.contains(SLOTS_EXTRACT, slot);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.fluxAmount);
        buf.writeFloat(this.effectiveness);
        for( short s : this.progress ) {
            buf.writeShort(s);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.fluxAmount = buf.readInt();
        this.effectiveness = buf.readFloat();
        for( int i = 0; i < this.progress.length; i++ ) {
            this.progress[i] = buf.readShort();
        }
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(this.fluxExtractPerTick, Math.min(MAX_FLUX_EXTRACT, maxExtract));

        if( !simulate ) {
            this.fluxAmount -= energyExtracted;
            this.fluxExtractPerTick -= energyExtracted;
        }

        return energyExtracted;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return this.fluxAmount;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return MAX_FLUX_STORAGE;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return from != ForgeDirection.UP;
    }

    public static Fuel getFuel(Item item) {
        return FUELS.get(item);
    }

    public static final class Fuel
    {
        public final float effect;
        public final short ticksProc;
        public final ItemStack trash;
        public final ItemStack treasure;

        public Fuel(float effectiveness, int ticksProcessing, ItemStack trash, ItemStack treasure) {
            this.effect = effectiveness;
            this.ticksProc = (short) ticksProcessing;
            this.trash = trash;
            this.treasure = treasure;
        }
    }
}
