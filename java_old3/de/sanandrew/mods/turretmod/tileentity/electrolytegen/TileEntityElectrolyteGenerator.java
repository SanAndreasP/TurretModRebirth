/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.tileentity.electrolytegen;

import de.sanandrew.mods.sanlib.lib.power.EnergyHelper;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.electrolytegen.IElectrolyteRecipe;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.init.TurretModRebirth;
import de.sanandrew.mods.turretmod.inventory.ElectrolyteInventory;
import de.sanandrew.mods.turretmod.inventory.ElectrolyteItemStackHandler;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketSyncTileEntity;
import de.sanandrew.mods.turretmod.network.TileClientSync;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteManager;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteProcess;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Arrays;

public class TileEntityElectrolyteGenerator
        extends LockableTileEntity
        implements TileClientSync, ITickableTileEntity
{
    static final int MAX_FLUX_STORAGE = 500_000;
    static final int MAX_FLUX_EXTRACT = 1_000;
    static final int MAX_FLUX_GENERATED = 200;

    public final ElectrolyteProcess[] processes = new ElectrolyteProcess[9];

    public float efficiency;

    private boolean doSync;
    private String customName;

    private final ElectrolyteInventory itemHandler = new ElectrolyteInventory(this);
    private final ElectrolyteEnergyStorage energyStorage = new ElectrolyteEnergyStorage();
    public final ElectrolyteItemStackHandler containerItemHandler = new ElectrolyteItemStackHandler(this.itemHandler);

    public int getGeneratedFlux() {
        return this.efficiency < 0.1F ? 0 : Math.min(200, (int) Math.round(Math.pow(1.6D, this.efficiency) / (68.0D + (127433.0D / 177119.0D)) * 80.0D));
    }

    public TileEntityElectrolyteGenerator() {
        super(BlockRegistry.ELECTROLYTE_GENERATOR_ENTITY);
    }

    @Override
    public void tick() {
        if( this.world != null && !this.world.isRemote ) {
            this.energyStorage.resetFluxExtract();

            float prevEffective = this.efficiency;

            this.energyStorage.emptyBuffer();

            if( this.energyStorage.isBufferEmpty() && this.energyStorage.fluxAmount < MAX_FLUX_STORAGE ) {
                int fluxEff = this.getGeneratedFlux();

                this.efficiency = 0.0F;

                for( int i = 0, max = this.processes.length; i < max; i++ ) {
                    this.processSlot(i);
                }

                if( this.efficiency > 0.1F ) {
                    this.energyStorage.fillBuffer(fluxEff);
                }
            }

            if( prevEffective < this.efficiency - 0.01F || prevEffective > this.efficiency + 0.01F ) {
                this.doSync = true;
            }

            this.transferEnergy();

            if( this.energyStorage.hasFluxChanged() ) {
                this.doSync = true;
            }

            if( this.doSync ) {
                TurretModRebirth.network.sendToAllNear(new PacketSyncTileEntity(this),
                                                       new PacketDistributor.TargetPoint(this.pos.getX(), this.pos.getY(), this.pos.getZ(), 64.0D, this.world.getDimensionKey()));
            }

            this.energyStorage.updatePrevFlux();
        }
    }

    private void processSlot(int slot) {
        ElectrolyteProcess process = this.processes[slot];
        boolean markAsDirty = false;

        if( process != null ) {
            if( process.recipe == null ) {
                this.processes[slot] = null;
                return;
            }

            ItemStack trashStack = process.getTrashStack(this.itemHandler);
            if( ItemStackUtils.isValid(trashStack) && this.itemHandler.isOutputFull(trashStack) ) {
                return;
            }

            ItemStack treasureStack = process.getTreasureStack(this.itemHandler);
            if( ItemStackUtils.isValid(treasureStack) && this.itemHandler.isOutputFull(treasureStack) ) {
                return;
            }

            if( process.hasFinished() ) {
                if( ItemStackUtils.isValid(trashStack) ) {
                    this.itemHandler.addExtraction(trashStack);
                }
                if( ItemStackUtils.isValid(treasureStack) ) {
                    this.itemHandler.addExtraction(treasureStack);
                }

                this.processes[slot] = null;
                markAsDirty = true;
            } else {
                process.incrProgress();
            }

            this.efficiency += process.recipe.getEfficiency();
            this.doSync = true;
        }

        if( this.processes[slot] == null ) {
            IElectrolyteRecipe recipe = ElectrolyteManager.INSTANCE.getFuel(this.itemHandler.extractInsertItem(slot, true));
            if( recipe != null ) {
                this.processes[slot] = new ElectrolyteProcess(recipe, this.itemHandler.extractInsertItem(slot, false));

                markAsDirty = true;
                this.doSync = true;
            }
        }

        if( markAsDirty ) {
            this.markDirty();
        }
    }

    private void transferEnergy() {
        if( this.world != null && this.energyStorage.fluxExtractPerTick > 0 ) {
            for( Direction direction : Direction.values() ) {
                if( direction == Direction.UP ) {
                    continue;
                }
                Direction otherDir = direction.getOpposite();

                BlockPos adjPos = this.pos.add(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
                TileEntity te = this.world.getTileEntity(adjPos);

                if( te == null || !EnergyHelper.canConnectEnergy(te, otherDir) ) {
                    continue;
                }

                long extractable = EnergyHelper.extractEnergy(this, direction, MAX_FLUX_EXTRACT, true);
                long receivable = EnergyHelper.receiveEnergy(te, otherDir, extractable, false);

                EnergyHelper.extractEnergy(this, direction, receivable, false);

                if( this.energyStorage.fluxExtractPerTick <= 0 ) {
                    break;
                }
            }
        }
    }

    private CompoundNBT writeNbt(CompoundNBT nbt) {
        ListNBT progressesNbt = new ListNBT();
        for( int i = 0, max = this.processes.length; i < max; i++ ) {
            if( this.processes[i] != null ) {
                CompoundNBT progNbt = new CompoundNBT();
                progNbt.setByte("progressSlot", (byte) i);
                this.processes[i].writeToNBT(progNbt);
                progressesNbt.appendTag(progNbt);
            }
        }
        nbt.setTag("progress", progressesNbt);

        nbt.setTag("cap_energy", this.energyStorage.serializeNBT());

        if( this.hasCustomName() ) {
            nbt.setString("customName", this.customName);
        }

        return nbt;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        this.readNbt(nbt);

        this.itemHandler.deserializeNBT(nbt.getCompound("cap_inventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setTag("cap_inventory", this.itemHandler.serializeNBT());

        this.writeNbt(nbt);

        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeNbt(nbt);
        return new SPacketUpdateTileEntity(this.pos, 0, nbt);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeNbt(super.getUpdateTag());
    }

    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.getNbtCompound();
        this.readNbt(nbt);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.readNbt(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if( facing != EnumFacing.UP ) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) {
            if( facing != EnumFacing.UP ) {
                return (T) itemHandler;
            }
        } else if( capability == CapabilityEnergy.ENERGY ) {
            if( facing != EnumFacing.UP ) {
                return (T) energyStorage;
            }
        }

        return null;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    private void readNbt(NBTTagCompound nbt) {
        Arrays.fill(this.processes, null);
        NBTTagList progressesNbt = nbt.getTagList("progress", Constants.NBT.TAG_COMPOUND);
        for( int i = 0, max = progressesNbt.tagCount(); i < max; i++ ) {
            NBTTagCompound progNbt = progressesNbt.getCompoundTagAt(i);
            byte slot = progNbt.getByte("progressSlot");
            this.processes[slot] = new ElectrolyteProcess(progNbt);
        }

        this.energyStorage.deserializeNBT(nbt.getCompoundTag("cap_energy"));

        if( nbt.hasKey("customName", Constants.NBT.TAG_STRING) ) {
            this.customName = nbt.getString("customName");
        }
    }

    public String getName() {
        return this.hasCustomName() ? this.customName : BlockRegistry.ELECTROLYTE_GENERATOR.getTranslationKey() + ".name";
    }

    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.energyStorage.fluxAmount);
        buf.writeFloat(this.efficiency);
        for( ElectrolyteProcess process : this.processes ) {
            if( process != null ) {
                buf.writeBoolean(true);
                process.writeToByteBuf(buf);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.energyStorage.fluxAmount = buf.readInt();
        this.efficiency = buf.readFloat();
        for( int i = 0, max = this.processes.length; i < max; i++ ) {
            if( buf.readBoolean() ) {
                this.processes[i] = new ElectrolyteProcess(buf);
            } else {
                this.processes[i] = null;
            }
        }
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }
}
