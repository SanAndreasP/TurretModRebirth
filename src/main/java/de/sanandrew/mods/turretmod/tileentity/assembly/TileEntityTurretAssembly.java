/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.tileentity.assembly;

import de.sanandrew.mods.sanlib.lib.Tuple;
import de.sanandrew.mods.sanlib.lib.util.InventoryUtils;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.assembly.IAssemblyRecipe;
import de.sanandrew.mods.turretmod.inventory.AssemblyInventory;
import de.sanandrew.mods.turretmod.network.PacketSyncTileEntity;
import de.sanandrew.mods.turretmod.network.TileClientSync;
import de.sanandrew.mods.turretmod.registry.assembly.AssemblyManager;
import de.sanandrew.mods.turretmod.util.EnumParticle;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import java.util.List;

public class TileEntityTurretAssembly
        extends TileEntity
        implements TileClientSync, ITickable
{
    static final int MAX_FLUX_STORAGE = 75_000;
    static final int MAX_FLUX_INSERT = 500;

    public float robotArmX;
    public float robotArmY;
    public float prevRobotArmX;
    public float prevRobotArmY;
    private float robotMotionX;
    private float robotMotionY;
    private float robotEndX;
    private float robotEndY;
    public Tuple spawnParticle;

    private boolean prevActive;
    private boolean automate;
    public boolean isActive;
    private boolean isActiveClient;

    public IAssemblyRecipe currRecipe;
    public int craftingAmount;

    private int ticksCrafted;
    private int maxTicksCrafted;
    private int fluxConsumption;

    private boolean doSync;
    private long ticksExisted;
    private String customName;

    private final AssemblyEnergyStorage energyStorage;
    private final AssemblyInventory invHandler;
    private final IItemHandler itemHandlerBottom;
    private final IItemHandler itemHandlerSide;

    private List<ItemStack> removedItems;

    public TileEntityTurretAssembly() {
        this.robotArmX = 2.0F;
        this.robotArmY = -9.0F;
        this.robotMotionX = 0.0F;
        this.robotMotionY = 0.0F;
        this.fluxConsumption = 0;
        this.ticksCrafted = 0;
        this.maxTicksCrafted = 0;
        this.ticksExisted = 0L;

        this.energyStorage = new AssemblyEnergyStorage();
        this.invHandler = new AssemblyInventory(this);
        this.itemHandlerBottom = new SidedInvWrapper(this.invHandler, EnumFacing.DOWN);
        this.itemHandlerSide = new SidedInvWrapper(this.invHandler, EnumFacing.WEST);
    }

    public void beginCrafting(IAssemblyRecipe recipe, int count) {
        if( this.currRecipe != null && recipe.getId().equals(this.currRecipe.getId()) && !this.automate ) {
            if( this.craftingAmount + count < 1 ) {
                this.cancelCrafting();
            } else {
                this.craftingAmount += count;
                this.doSync = true;
            }
        } else if( this.currRecipe == null ) {
            this.currRecipe = recipe;
            this.craftingAmount = this.automate ? 1 : count;
            this.doSync = true;
        }
    }

    public void cancelCrafting() {
        if( this.removedItems != null ) {
            this.removedItems.forEach(is -> {
                ItemStack remains = InventoryUtils.addStackToInventory(is, this.invHandler);
                if( ItemStackUtils.isValid(remains) ) {
                    EntityItem item = new EntityItem(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, remains);
                    this.world.spawnEntity(item);
                }
            });
            this.removedItems = null;
        }
        this.currRecipe = null;
        this.craftingAmount = 0;
        this.ticksCrafted = 0;
        this.fluxConsumption = 0;
        this.maxTicksCrafted = 0;
        this.isActive = false;
        this.isActiveClient = false;
        this.doSync = true;
        this.markDirty();
    }

    private void initCrafting() {
        if( this.currRecipe != null ) {
            ItemStack result = this.currRecipe.getCraftingResult(this.invHandler);
            if( this.invHandler.canFillOutput(result) ) {
                this.removedItems = AssemblyManager.INSTANCE.checkAndConsumeResources(this.invHandler, this.world, this.currRecipe);
                if( this.removedItems != null ) {
                    this.maxTicksCrafted = this.currRecipe.getProcessTime();
                    this.fluxConsumption = MathHelper.ceil(this.currRecipe.getFluxPerTick() * (this.hasSpeedUpgrade() ? 1.1F : 1.0F));
                    this.ticksCrafted = 0;
                    this.isActive = true;
                    this.doSync = true;
                }
            }
        }
    }

    public boolean hasAutoUpgrade() {
        return this.invHandler.hasAutoUpgrade();
    }

    public boolean hasSpeedUpgrade() {
        return this.invHandler.hasSpeedUpgrade();
    }

    public boolean hasFilterUpgrade() {
        return this.invHandler.hasFilterUpgrade();
    }

    public NonNullList<ItemStack> getFilterStacks() {
        return this.invHandler.getFilterStacks();
    }

    @Override
    public void update() {
        if( !this.world.isRemote ) {
            if( this.automate && !this.hasAutoUpgrade() ) {
                this.automate = false;
                this.cancelCrafting();
            }

            if( this.energyStorage.hasFluxChanged() ) {
                this.doSync = true;
            }
            this.energyStorage.updatePrevFlux();

            int maxLoop = this.hasSpeedUpgrade() ? 4 : 1;
            boolean markDirty = false;

            this.isActiveClient = this.isActive;
            if( this.isActive && this.currRecipe != null ) {
                for( int i = 0; i < maxLoop; i++ ) {
                    if( this.energyStorage.fluxAmount >= this.fluxConsumption && this.world.isBlockIndirectlyGettingPowered(this.pos) == 0 ) {
                        this.energyStorage.fluxAmount -= this.fluxConsumption;
                        if( ++this.ticksCrafted >= this.maxTicksCrafted ) {
                            ItemStack stack = this.currRecipe.getCraftingResult(this.invHandler);
                            if( !ItemStackUtils.isValid(stack) ) {
                                this.cancelCrafting();
                                return;
                            }

                            this.invHandler.fillOutput(stack);
                            this.removedItems = null;

                            if( !this.invHandler.canFillOutput(stack) ) {
                                this.isActive = false;
                                this.isActiveClient = false;
                            }

                            if( this.craftingAmount > 1 || this.automate ) {
                                if( !this.automate ) {
                                    this.craftingAmount--;
                                }

                                if( this.isActive ) {
                                    this.removedItems = this.craftingAmount >= 1 ? AssemblyManager.INSTANCE.checkAndConsumeResources(this.invHandler, this.world, this.currRecipe) : null;
                                    if( this.removedItems == null ) {
                                        this.isActive = false;
                                        this.isActiveClient = false;
                                    }
                                }
                            } else {
                                this.cancelCrafting();
                                return;
                            }
                            this.ticksCrafted = 0;

                            markDirty = true;
                        }

                        this.doSync = true;
                    } else {
                        this.isActiveClient = false;
                        this.doSync = true;
                    }

                    if( !this.isActive || this.currRecipe == null ) {
                        break;
                    }
                }
            } else {
                this.initCrafting();
                this.isActiveClient = false;
            }

            if( markDirty ) {
                this.markDirty();
            }

            if( this.doSync ) {
                PacketSyncTileEntity.sync(this);
                this.doSync = false;
            }
        } else {
            this.processRobotArm();
        }

        this.prevActive = this.isActive;
        this.ticksExisted++;
    }

    private void processRobotArm() {
        this.prevRobotArmX = this.robotArmX;
        this.prevRobotArmY = this.robotArmY;

        this.robotArmX += this.robotMotionX;
        this.robotArmY += this.robotMotionY;

        if( this.robotArmX > this.robotEndX && this.robotMotionX > 0.0F ) {
            this.robotArmX = this.robotEndX;
            this.robotMotionX = 0.0F;
        } else if( this.robotArmX < this.robotEndX && this.robotMotionX < 0.0F ) {
            this.robotArmX = this.robotEndX;
            this.robotMotionX = 0.0F;
        }

        if( this.robotArmY > this.robotEndY && this.robotMotionY > 0.0F ) {
            this.robotArmY = this.robotEndY;
            this.robotMotionY = 0.0F;
        } else if( this.robotArmY < this.robotEndY && this.robotMotionY < 0.0F ) {
            this.robotArmY = this.robotEndY;
            this.robotMotionY = 0.0F;
        }

        if( this.isActiveClient && (!this.prevActive || this.ticksExisted % 20 == 0) ) {
            this.animateRobotArmRng();
        } else if( this.prevActive && !this.isActiveClient ) {
            this.animateRobotArmReset();
            this.spawnParticle = null;
        }

        if( this.isActiveClient && this.spawnParticle != null ) {
            TurretModRebirth.proxy.spawnParticle(EnumParticle.ASSEMBLY_SPARK, spawnParticle.getValue(0), spawnParticle.<Double>getValue(1) + 0.05D, spawnParticle.getValue(2), null);
            this.spawnParticle = null;
        }
    }

    private void animateRobotArmRng() {
        this.animateRobotArm(4.0F + MiscUtils.RNG.randomFloat() * 6.0F, -3.5F + MiscUtils.RNG.randomFloat() * -6.0F);
    }

    private void animateRobotArmReset() {
        this.animateRobotArm(2.0F, -9.0F);
    }

    private void animateRobotArm(float x, float y) {
        float speedMulti = (this.hasSpeedUpgrade() ? 4.0F : 1.0F);
        this.robotMotionX = (0.1F + MiscUtils.RNG.randomFloat() * 0.1F) * (x > this.robotArmX ? 1.0F : -1.0F) * speedMulti;
        this.robotMotionY = (0.1F + MiscUtils.RNG.randomFloat() * 0.1F) * (y > this.robotArmY ? 1.0F : -1.0F) * speedMulti;
        this.robotEndX = x;
        this.robotEndY = y;
    }

    public IInventory getInventory() {
        return this.invHandler;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeNBT(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        this.readNBT(tag);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.writeNBT(new NBTTagCompound()));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        this.writeNBT(nbt);

        if( this.removedItems != null ) {
            nbt.setTag("RemovedItems", ItemStackUtils.writeItemStacksToTag(this.removedItems, this.invHandler.getInventoryStackLimit()));
        }

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.readNBT(nbt);

        if( nbt.hasKey("RemovedItems", Constants.NBT.TAG_LIST) ) {
            NBTTagList remItems = nbt.getTagList("RemovedItems", Constants.NBT.TAG_COMPOUND);
            this.removedItems = NonNullList.withSize(remItems.tagCount(), ItemStack.EMPTY);
            ItemStackUtils.readItemStacksFromTag(this.removedItems, remItems);
        }

        this.doSync = true;
    }

    private NBTTagCompound writeNBT(NBTTagCompound nbt) {
        nbt.setTag("inventory", this.invHandler.serializeNBT());

        if( this.currRecipe != null ) {
            nbt.setString("CraftingId", this.currRecipe.getId().toString());
            nbt.setInteger("CraftingAmount", this.craftingAmount);
        }

        nbt.setTag("cap_energy", this.energyStorage.serializeNBT());

        nbt.setBoolean("isActive", this.isActive);
        nbt.setInteger("ticksCrafted", this.ticksCrafted);
        nbt.setInteger("maxTicksCrafted", this.maxTicksCrafted);
        nbt.setInteger("fluxConsumption", this.fluxConsumption);
        nbt.setBoolean("automate", this.automate);

        if( this.hasCustomName() ) {
            nbt.setString("customName", this.customName);
        }

        return nbt;
    }

    private void readNBT(NBTTagCompound nbt) {
        this.invHandler.deserializeNBT(nbt.getCompoundTag("inventory"));

        if( nbt.hasKey("CraftingId") && nbt.hasKey("CraftingAmount") ) {
            this.currRecipe = AssemblyManager.INSTANCE.getRecipe(new ResourceLocation(nbt.getString("CraftingId")));
            this.craftingAmount = nbt.getInteger("CraftingAmount");
        }

        this.energyStorage.deserializeNBT(nbt.getCompoundTag("cap_energy"));

        this.isActive = nbt.getBoolean("isActive");
        this.ticksCrafted = nbt.getInteger("ticksCrafted");
        this.maxTicksCrafted = nbt.getInteger("maxTicksCrafted");
        this.fluxConsumption = nbt.getInteger("fluxConsumption");
        this.automate = nbt.getBoolean("automate");

        if( nbt.hasKey("customName") ) {
            this.customName = nbt.getString("customName");
        }
    }

    public ITextComponent getDisplayName() {
        return this.hasCustomName() ? new TextComponentString(this.getCustomName()) : new TextComponentTranslation(this.getCustomName());
    }

    public int getTicksCrafted() {
        return this.ticksCrafted;
    }

    public int getMaxTicksCrafted() {
        return this.maxTicksCrafted;
    }

    public int getFluxConsumption() {
        return this.fluxConsumption;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.energyStorage.fluxAmount);
        buf.writeInt(this.fluxConsumption);
        buf.writeBoolean(this.isActive);
        buf.writeInt(this.ticksCrafted);
        buf.writeInt(this.maxTicksCrafted);
        buf.writeBoolean(this.automate);
        buf.writeBoolean(this.isActiveClient);
        if( this.currRecipe != null ) {
            buf.writeBoolean(true);
            ByteBufUtils.writeUTF8String(buf, this.currRecipe.getId().toString());
            buf.writeInt(this.craftingAmount);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.energyStorage.fluxAmount = buf.readInt();
        this.fluxConsumption = buf.readInt();
        this.isActive = buf.readBoolean();
        this.ticksCrafted = buf.readInt();
        this.maxTicksCrafted = buf.readInt();
        this.automate = buf.readBoolean();
        this.isActiveClient = buf.readBoolean();
        if( buf.readBoolean() ) {
            this.currRecipe = AssemblyManager.INSTANCE.getRecipe(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
            this.craftingAmount = buf.readInt();
        } else {
            this.currRecipe = null;
            this.craftingAmount = 0;
        }
    }

    public void setAutomated(boolean b) {
        if( this.currRecipe == null ) {
            this.automate = b;
            this.doSync = true;
        } else if( !b ) {
            this.cancelCrafting();
            this.automate = false;
            this.doSync = true;
        }
    }

    public boolean isAutomated() {
        return this.automate;
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked", "ObjectEquality"})
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ) {
            if( facing == EnumFacing.DOWN ) {
                return (T) itemHandlerBottom;
            } else if( facing != EnumFacing.UP ) {
                return (T) itemHandlerSide;
            }
        } else if( facing != EnumFacing.UP && capability == CapabilityEnergy.ENERGY ) {
            return (T) energyStorage;
        }

        return null;
    }

    @Override
    @SuppressWarnings("ObjectEquality")
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if( facing != EnumFacing.UP ) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
        }

        return super.hasCapability(capability, facing);
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return this.hasCustomName() ? this.customName : TmrConstants.ID + ".container.assembly";
    }

    public boolean hasCustomName() {
        return this.customName != null;
    }
}
