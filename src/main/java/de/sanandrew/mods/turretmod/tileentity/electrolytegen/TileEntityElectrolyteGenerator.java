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
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketSyncTileEntity;
import de.sanandrew.mods.turretmod.network.TileClientSync;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteHelper;
import de.sanandrew.mods.turretmod.registry.electrolytegen.ElectrolyteProcess;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Arrays;

public class TileEntityElectrolyteGenerator
        extends TileEntity
        implements TileClientSync, ITickable
{
    public static final int MAX_FLUX_STORAGE = 500_000;
    public static final int MAX_FLUX_EXTRACT = 1_000;
    public static final int MAX_FLUX_GENERATED = 200;

    public final ElectrolyteProcess[] processes = new ElectrolyteProcess[9];

    public float effectiveness;

    boolean doSync;
    private String customName;
    private ElectrolyteInventoryHandler itemHandler = new ElectrolyteInventoryHandler(this);
    private ElectrolyteEnergyStorage energyStorage = new ElectrolyteEnergyStorage();

    public ElectrolyteContainerInventoryHandler containerItemHandler = new ElectrolyteContainerInventoryHandler(itemHandler);

    public int getGeneratedFlux() {
        return this.effectiveness < 0.1F ? 0 : Math.min(200, (int) Math.round(Math.pow(1.6D, this.effectiveness) / (68.0D + (127433.0D / 177119.0D)) * 80.0D));
    }

    @Override
    public void update() {
        if( !this.world.isRemote ) {
            this.energyStorage.resetFluxExtract();

            float prevEffective = this.effectiveness;

            this.energyStorage.emptyBuffer();

            if( this.energyStorage.isBufferEmpty() && this.energyStorage.fluxAmount < MAX_FLUX_STORAGE ) {
                int fluxEff = this.getGeneratedFlux();

                this.effectiveness = 0.0F;

                for( int i = 0, max = this.processes.length; i < max; i++ ) {
                    this.processSlot(i);
                }

                if( this.effectiveness > 0.1F ) {
                    this.energyStorage.fillBuffer(fluxEff);
                }
            }

            if( prevEffective < this.effectiveness - 0.01F || prevEffective > this.effectiveness + 0.01F ) {
                this.doSync = true;
            }

            this.transferEnergy();

            if( this.energyStorage.hasFluxChanged() ) {
                this.doSync = true;
            }

            if( this.doSync ) {
                PacketRegistry.sendToAllAround(new PacketSyncTileEntity(this), this.world.provider.getDimension(), this.pos, 64.0D);
            }

            this.energyStorage.updatePrevFlux();
        }
    }

    private void processSlot(int slot) {
        ElectrolyteProcess process = this.processes[slot];
        boolean markAsDirty = false;

        if( process != null ) {
            if( process.hasTrash() && !this.itemHandler.canAddExtraction(process.trashStack) ) {
                return;
            }

            if( process.hasTreasure() && !this.itemHandler.canAddExtraction(process.treasureStack) ) {
                return;
            }

            if( process.hasFinished() ) {
                if( process.hasTrash() && MiscUtils.RNG.randomInt(10) == 0 ) {
                    this.itemHandler.addExtraction(process.trashStack);
                }
                if( process.hasTreasure() && MiscUtils.RNG.randomInt(100) == 0 ) {
                    this.itemHandler.addExtraction(process.treasureStack);
                }

                this.processes[slot] = null;
                markAsDirty = true;
            } else {
                process.incrProgress();
            }

            this.effectiveness += process.effectivenes;
            this.doSync = true;
        }

        if( this.processes[slot] == null && ElectrolyteHelper.isFuel(this.itemHandler.extractInsertItem(slot, true)) ) {
            this.processes[slot] = new ElectrolyteProcess(this.itemHandler.extractInsertItem(slot, false));

            markAsDirty = true;
            this.doSync = true;
        }

        if( markAsDirty ) {
            this.markDirty();
        }
    }

    private void transferEnergy() {
        if( this.energyStorage.fluxExtractPerTick > 0 ) {
            for( EnumFacing direction : EnumFacing.VALUES ) {
                if( direction == EnumFacing.UP ) {
                    continue;
                }
                EnumFacing otherDir = direction.getOpposite();

                BlockPos adjPos = this.pos.add(direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ());
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

    private NBTTagCompound writeNbt(NBTTagCompound nbt) {
        NBTTagList progressesNbt = new NBTTagList();
        for( int i = 0, max = this.processes.length; i < max; i++ ) {
            if( this.processes[i] != null ) {
                NBTTagCompound progNbt = new NBTTagCompound();
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.readNbt(nbt);

        this.itemHandler.deserializeNBT(nbt.getCompoundTag("cap_inventory"));
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
        return this.hasCustomName() ? this.customName : BlockRegistry.electrolyte_generator.getUnlocalizedName() + ".name";
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
        buf.writeFloat(this.effectiveness);
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
        this.effectiveness = buf.readFloat();
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