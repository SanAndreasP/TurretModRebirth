/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.turret;

import com.mojang.authlib.GameProfile;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.sanlib.lib.util.ReflectionUtils;
import de.sanandrew.mods.sanlib.lib.util.UuidUtils;
import de.sanandrew.mods.turretmod.api.EnumGui;
import de.sanandrew.mods.turretmod.api.repairkit.IRepairKit;
import de.sanandrew.mods.turretmod.api.turret.ITargetProcessor;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretRAM;
import de.sanandrew.mods.turretmod.api.turret.IUpgradeProcessor;
import de.sanandrew.mods.turretmod.api.turret.IVariant;
import de.sanandrew.mods.turretmod.api.turret.IVariantHolder;
import de.sanandrew.mods.turretmod.api.turret.TurretAttributes;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.init.TurretModRebirth;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.item.ItemRemapper;
import de.sanandrew.mods.turretmod.item.ItemTurret;
import de.sanandrew.mods.turretmod.item.ItemTurretControlUnit;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketUpdateTurretState;
import de.sanandrew.mods.turretmod.registry.Sounds;
import de.sanandrew.mods.turretmod.registry.repairkit.RepairKitRegistry;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.tileentity.TileEntityTurretCrate;
import de.sanandrew.mods.turretmod.client.util.TmrUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.function.Supplier;

public class EntityTurret
        extends EntityLiving
        implements IEntityAdditionalSpawnData, ITurretInst
{
    private static final AxisAlignedBB UPWARDS_BLOCK = new AxisAlignedBB(0.1D, 0.99D, 0.1D, 1.0D, 1.0D, 1.0D);

    private static final DataParameter<Boolean> SHOT_CHNG = EntityDataManager.createKey(EntityTurret.class, DataSerializers.BOOLEAN);
    private static final DataParameter<String> VARIANT = EntityDataManager.createKey(EntityTurret.class, DataSerializers.STRING);

    private boolean showRange;
    public boolean inGui;

    private final TargetProcessor targetProc;
    private final UpgradeProcessor upgProc;

    private UUID ownerUUID;
    private String ownerName;

    private DataWatcherBooleans<EntityTurret> dwBools;
    private boolean prevShotChng;

    private ITurretRAM turretRAM;

    @Nonnull
    private ITurret delegate;

    /** called when turret is loaded from disk **/
    public EntityTurret(World world) {
        super(world);
        this.targetProc = new TargetProcessor(this);
        this.upgProc = new UpgradeProcessor(this);
        this.rotationYaw = 0.0F;
        this.delegate = TurretRegistry.INSTANCE.getDefaultObject();
    }

    /** called when turret is rendered in a GUI or its placed down by {@link EntityTurret#EntityTurret(World, EntityPlayer, ITurret)} **/
    public EntityTurret(World world, ITurret delegate) {
        this(world);

        this.loadDelegate(delegate);

        this.setHealth(this.getMaxHealth());
    }

    /** called when turret is placed down **/
    public EntityTurret(World world, EntityPlayer owner, ITurret delegate) {
        this(world, delegate);

        this.ownerUUID = owner.getUniqueID();
        this.ownerName = owner.getName();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        this.getAttributeMap().registerAttribute(TurretAttributes.MAX_AMMO_CAPACITY);
        this.getAttributeMap().registerAttribute(TurretAttributes.MAX_RELOAD_TICKS);
        this.getAttributeMap().registerAttribute(TurretAttributes.MAX_INIT_SHOOT_TICKS);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        this.dwBools = new DataWatcherBooleans<>(this);
        this.dwBools.registerDwValue();

        this.dataManager.register(SHOT_CHNG, false);
        this.dataManager.register(VARIANT, "");

        this.setActive(true);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource dmgSrc) {
        return MiscUtils.defIfNull(this.delegate.getHurtSound(this), Sounds.HIT_TURRETHIT);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MiscUtils.defIfNull(this.delegate.getDeathSound(this), Sounds.HIT_TURRETDEATH);
    }

    private SoundEvent getCollectSound() {
        return MiscUtils.defIfNull(this.delegate.getCollectSound(this), Sounds.COLLECT_IA_GET);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void faceEntity(Entity entity, float yawSpeed, float pitchSpeed) {
        double deltaX = entity.posX - this.posX;
        double deltaZ = entity.posZ - this.posZ;
        double deltaY;

        if( entity instanceof EntityLivingBase ) {
            EntityLivingBase livingBase = (EntityLivingBase)entity;
            deltaY = (livingBase.posY + livingBase.getEyeHeight()) - (this.posY + this.getEyeHeight());
        } else {
            deltaY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0D - (this.posY + this.getEyeHeight());
        }

        double distVecXZ = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float yawRotation = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
        float pitchRotation = (float) -(Math.atan2(deltaY, distVecXZ) * 180.0D / Math.PI);
        this.rotationPitch = calcRotation(this.rotationPitch, pitchRotation);
        this.rotationYawHead = calcRotation(this.rotationYawHead, yawRotation);
    }

    @Override
    public float getEyeHeight() {
        return this.delegate.getEyeHeight(this.height);
    }

    /**
     * LEAVE EMPTY! Or else this causes visual glitches...
     */
    @Override
    public void setRotationYawHead(float rotation) { }

    private static float calcRotation(float prevRotation, float newRotation) {
        final float speed = 20.0F;
        float part = MathHelper.wrapDegrees(newRotation - prevRotation);

        if( part > speed ) {
            part = speed;
        }

        if( part < -speed ) {
            part = -speed;
        }

        return prevRotation + part;
    }

    @Override
    public void onUpdate() {
        if( !this.inGui ) {
            super.onUpdate();

            this.rotationYaw = 0.0F;
            this.renderYawOffset = 0.0F;

            this.delegate.onUpdate(this);
        }
    }

    @Override
    public boolean wasShooting() {
        boolean shot = this.dataManager.get(SHOT_CHNG) != this.prevShotChng;
        this.prevShotChng = this.dataManager.get(SHOT_CHNG);
        return shot;
    }

    public void setShooting() {
        this.dataManager.set(SHOT_CHNG, !this.dataManager.get(SHOT_CHNG));
    }

    private boolean isSubmergedInLiquid(double heightMod) {
        BlockPos pos = new BlockPos(this.posX, this.posY + heightMod, this.posZ);
        return this.world.getBlockState(pos).getMaterial().isLiquid();
    }

    @Override
    public void onLivingUpdate() {
        if( !this.delegate.isBuoy() ) {
            this.motionY -= 0.0325F;
        } else {
            if( this.isSubmergedInLiquid(this.height + 0.2F) ) {
                this.motionY += 0.0125F;
            } else if( this.isSubmergedInLiquid(this.height + 0.05F) ) {
                this.motionY += 0.005F;
                if( this.motionY > 0.025F ) {
                    this.motionY *= 0.75F;
                }
            } else if( !this.isSubmergedInLiquid(this.height - 0.2F) ) {
                this.motionY -= 0.0325F;
            } else {
                this.motionY -= 0.005F;
                if( this.motionY < -0.025F ) {
                    this.motionY *= 0.75F;
                }
            }
        }
        super.move(MoverType.SELF, 0.0F, this.motionY, 0.0F);

        this.world.profiler.startSection("ai");

        if( this.isMovementBlocked() ) {
            this.isJumping = false;
            this.moveStrafing = 0.0F;
            this.moveForward = 0.0F;
            this.randomYawVelocity = 0.0F;
        } else if( !this.world.isRemote ) {
            this.world.profiler.startSection("oldAi");
            this.updateMyEntityActionState();
            this.world.profiler.endSection();
        }

        this.upgProc.onTick();

        if( !this.world.isRemote ) {
            this.targetProc.onTick();
        } else {
            this.targetProc.onTickClient();
        }

        if( this.isActive() ) {
            if( this.targetProc.hasTarget() ) {
                this.faceEntity(this.targetProc.getTarget(), 10.0F, this.getVerticalFaceSpeed());
            } else if( this.world.isRemote && TmrUtils.INSTANCE.getPassengersOfClass(this, EntityPlayer.class).size() < 1 ) {
                this.rotationYawHead += 1.0F;
                this.rotationYawHead = MiscUtils.wrap360(this.rotationYawHead);
                this.prevRotationYawHead = MiscUtils.wrap360(this.prevRotationYawHead);

                if( this.rotationPitch < 0.0F ) {
                    this.rotationPitch += 5.0F;
                    if( this.rotationPitch > 0.0F ) {
                        this.rotationPitch = 0.0F;
                    }
                } else if( this.rotationPitch > 0.0F ) {
                    this.rotationPitch -= 5.0F;
                    if( this.rotationPitch < 0.0F ) {
                        this.rotationPitch = 0.0F;
                    }
                }
            }
        } else {
            this.rotationYawHead = MiscUtils.wrap360(this.rotationYawHead);
            this.prevRotationYawHead = MiscUtils.wrap360(this.prevRotationYawHead);
            @SuppressWarnings("IntegerDivisionInFloatingPointContext")
            float closestRot = (MathHelper.ceil(this.rotationYawHead) / 90) * 90.0F;
            if( this.rotationYawHead > closestRot ) {
                this.rotationYawHead -= 5.0F;
                if( this.rotationYawHead < closestRot ) {
                    this.rotationYawHead = closestRot;
                }
            } else if( this.rotationYawHead < closestRot ) {
                this.rotationYawHead += 5.0F;
                if( this.rotationYawHead > closestRot ) {
                    this.rotationYawHead = closestRot;
                }
            }

            final float lockedPitch = this.delegate.getDeactiveHeadPitch();
            if( this.rotationPitch < lockedPitch ) {
                this.rotationPitch += 1.0F;
                if( this.rotationPitch > lockedPitch ) {
                    this.rotationPitch = lockedPitch;
                }
            } else if( this.rotationPitch > lockedPitch ) {
                this.rotationPitch -= 1.0F;
                if( this.rotationPitch < lockedPitch ) {
                    this.rotationPitch = lockedPitch;
                }
            }
        }

        this.world.profiler.endSection();
    }

    private void onInteractSucceed(@Nonnull ItemStack heldItem, EntityPlayer player) {
        if( heldItem.getCount() == 0 ) {
            player.inventory.removeStackFromSlot(player.inventory.currentItem);
        } else {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, heldItem.copy());
        }

        this.updateState();
        player.inventoryContainer.detectAndSendChanges();
        this.world.playSound(null, this.posX, this.posY, this.posZ, this.getCollectSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if( this.world.isRemote ) {
            if( ItemStackUtils.isItem(stack, ItemRegistry.TURRET_CONTROL_UNIT) ) {
                if( !player.isSneaking() ) {
                    TmrUtils.INSTANCE.openGui(player, EnumGui.TCU, this.getEntityId(), 0, 0);
                }
                return true;
            } else if( !ItemStackUtils.isValid(stack) && hand == EnumHand.MAIN_HAND ) {
                TmrUtils.INSTANCE.openGui(player, EnumGui.TINFO, this.getEntityId(), 0, 0);
            }

            return false;
        } else if( ItemStackUtils.isValid(stack) ) {
            if( ItemStackUtils.isItem(stack, ItemRegistry.TURRET_CONTROL_UNIT) && player.isSneaking() ) {
                ItemTurretControlUnit.bindTurret(stack, this);
                return true;
            } else if( this.targetProc.addAmmo(stack, player) ) {
                this.onInteractSucceed(stack, player);
                return true;
            } else if( this.upgProc.tryApplyUpgrade(stack.copy()) ) {
                stack.shrink(1);
                this.onInteractSucceed(stack, player);
                return true;
            } else if( this.applyRepairKit(stack) ) {
                stack.shrink(1);
                this.onInteractSucceed(stack, player);
            }
        }

        return super.processInteract(player, hand);
    }

    @Override
    public boolean applyRepairKit(ItemStack stack) {
        IRepairKit repKit = RepairKitRegistry.INSTANCE.getObject(stack);

        if( repKit.isApplicable(this) ) {
            this.heal(repKit.getHealAmount());
            repKit.onHeal(this);

            return true;
        }

        return false;
    }

    @Override
    public void onDeath(DamageSource dmgSrc) {
        super.onDeath(dmgSrc);

        if( !this.world.isRemote ) {
            this.targetProc.dropAmmo();
            this.upgProc.dropUpgrades();
        }

        //just insta-kill it
        this.setDead();
    }

    private void updateMyEntityActionState() {
        this.idleTime++;
        this.moveStrafing = 0.0F;
        this.moveForward = 0.0F;
        this.rotationYaw = 0.0F;
    }

    public ITargetProcessor getTargetProcessor() {
        return this.targetProc;
    }

    public IUpgradeProcessor getUpgradeProcessor() {
        return this.upgProc;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.delegate.getId().toString());

        NBTTagCompound targetNbt = new NBTTagCompound();
        this.targetProc.writeToNbt(targetNbt);
        ByteBufUtils.writeTag(buffer, targetNbt);

        NBTTagCompound upgNbt = new NBTTagCompound();
        this.upgProc.writeToNbt(upgNbt);
        ByteBufUtils.writeTag(buffer, upgNbt);

        if( this.ownerUUID != null ) {
            buffer.writeBoolean(true);
            buffer.writeLong(this.ownerUUID.getMostSignificantBits());
            buffer.writeLong(this.ownerUUID.getLeastSignificantBits());
            ByteBufUtils.writeUTF8String(buffer, this.ownerName);
        } else {
            buffer.writeBoolean(false);
        }

        this.delegate.writeSpawnData(this, buffer);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.delegate = TurretRegistry.INSTANCE.getObject(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));

        this.targetProc.readFromNbt(ByteBufUtils.readTag(buffer));
        this.upgProc.readFromNbt(ByteBufUtils.readTag(buffer));

        if( buffer.readBoolean() ) {
            this.ownerUUID = new UUID(buffer.readLong(), buffer.readLong());
            this.ownerName = ByteBufUtils.readUTF8String(buffer);
        }

        this.delegate.readSpawnData(this, buffer);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        nbt.setString("turretId", this.delegate.getId().toString());

        this.targetProc.writeToNbt(nbt);
        this.upgProc.writeToNbt(nbt);
        this.dwBools.writeToNbt(nbt);

        if( this.ownerUUID != null ) {
            nbt.setString("ownerUUID", this.ownerUUID.toString());
            nbt.setString("ownerName", this.ownerName);
        }

        if( this.delegate instanceof IVariantHolder ) {
            nbt.setString("Variant", this.getVariant().getId().toString());
        }

        this.delegate.onSave(this, nbt);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        String turretId = nbt.getString("turretId");
        if( UuidUtils.isStringUuid(turretId) ) {
            loadDelegate(ItemRemapper.OLD_TURRET_MAPPINGS.get(UUID.fromString(turretId)));
        } else {
            ResourceLocation turretIdRL = new ResourceLocation(turretId);
            if( ItemRemapper.OLD_TURRET_ID_MAPPINGS.containsKey(turretIdRL) ) { // TODO: fix for new names, remove with new MC version (> 1.12.2)
                turretIdRL = ItemRemapper.OLD_TURRET_ID_MAPPINGS.get(turretIdRL);
            }

            loadDelegate(turretIdRL);
        }

        super.readEntityFromNBT(nbt);

        this.targetProc.readFromNbt(nbt);
        this.upgProc.readFromNbt(nbt);
        this.dwBools.readFromNbt(nbt);

        if( nbt.hasKey("ownerUUID") ) {
            this.ownerUUID = UUID.fromString(nbt.getString("ownerUUID"));
            this.ownerName = nbt.getString("ownerName");
        }

        if( nbt.hasKey("Variant", Constants.NBT.TAG_STRING) ) {
            this.setVariant(new ResourceLocation(nbt.getString("Variant")));
        }

        this.delegate.onLoad(this, nbt);
    }

    private void loadDelegate(ResourceLocation id) {
        this.loadDelegate(TurretRegistry.INSTANCE.getObject(id));
    }

    private void loadDelegate(ITurret turret) {
        this.delegate = turret;
        this.delegate.entityInit(this);
        this.delegate.applyEntityAttributes(this);

        this.getEntityAttribute(TurretAttributes.MAX_RELOAD_TICKS).setBaseValue(this.delegate.getReloadTicks());
        this.getEntityAttribute(TurretAttributes.MAX_AMMO_CAPACITY).setBaseValue(this.delegate.getAmmoCapacity());
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.delegate.getHealth());

        this.targetProc.init();
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 50;
    }

    /**turrets are machines, they aren't affected by potions*/
    @Override
    public boolean isPotionApplicable(PotionEffect effect) {
        return effect.getIsAmbient();
    }

    /**turrets are immobile, leave empty*/
    @Override
    public final void knockBack(Entity entity, float unknown, double motionXAmount, double motionZAmount) {}

    @Override
    public final void move(MoverType type, double motionX, double motionY, double motionZ) {
        if( type == MoverType.PISTON ) {
            super.move(type, motionX, motionY, motionZ);
        }
    }

    @Override
    public boolean isGlowing() {
        return super.isGlowing() || TurretModRebirth.proxy.checkTurretGlowing(this);
    }

    /**turrets are immobile, leave empty*/
    @Override
    public final void travel(float strafe, float vertical, float forward) {}

    public SoundEvent getShootSound() {
        return this.delegate.getShootSound(this);
    }

    public SoundEvent getNoAmmoSound() {
        return MiscUtils.defIfNull(this.delegate.getNoAmmoSound(this), SoundEvents.BLOCK_DISPENSER_FAIL);
    }

    public boolean isActive() {
        return this.dwBools.getBit(DataWatcherBooleans.Turret.ACTIVE.bit);
    }

    public void setActive(boolean isActive) {
        this.dwBools.setBit(DataWatcherBooleans.Turret.ACTIVE.bit, isActive);
    }

    @Override
    public boolean showRange() {
        return this.showRange;
    }

    @Override
    public void setShowRange(boolean showRange) {
        this.showRange = showRange;
    }

    private static boolean isAABBInside(AxisAlignedBB bb1) {
        return bb1.minX <= EntityTurret.UPWARDS_BLOCK.minX && bb1.minY <= EntityTurret.UPWARDS_BLOCK.minY && bb1.minZ <= EntityTurret.UPWARDS_BLOCK.minZ
                       && bb1.maxX >= EntityTurret.UPWARDS_BLOCK.maxX && bb1.maxY >= EntityTurret.UPWARDS_BLOCK.maxY && bb1.maxZ >= EntityTurret.UPWARDS_BLOCK.maxZ;
    }

    public static boolean canTurretBePlaced(ITurret delegate, World world, BlockPos pos, boolean doBlockCheckOnly) {
        AxisAlignedBB blockBB = world.getBlockState(pos).getCollisionBoundingBox(world, pos);
        boolean buoyant = delegate.isBuoy();
        if( !buoyant && (blockBB == null || !isAABBInside(blockBB)) ) {
            return false;
        }

        BlockPos posPlaced = pos.offset(EnumFacing.UP);
        BlockPos posPlaced2 = buoyant ? pos : pos.offset(EnumFacing.UP, 2);
        if( !world.getBlockState(posPlaced).getBlock().isReplaceable(world, posPlaced) || !world.getBlockState(posPlaced2).getBlock().isReplaceable(world, posPlaced2) ) {
            return false;
        }

        if( !doBlockCheckOnly ) {
            AxisAlignedBB aabb = new AxisAlignedBB(posPlaced.getX(), posPlaced.getY(), posPlaced.getZ(), posPlaced.getX() + 1.0D, posPlaced.getY() + (buoyant ? - 1.0D : 1.0D), posPlaced.getZ() + 1.0D);
            return world.getEntitiesWithinAABB(EntityTurret.class, aabb).isEmpty();
        }

        return true;
    }

    @Override
    public String getOwnerName() {
        return this.ownerName;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean hasPlayerPermission(EntityPlayer player) {
        if( player == null ) {
            return false;
        }

        if( this.ownerUUID == null ) {
            return true;
        }

        MinecraftServer mcSrv = this.world.getMinecraftServer();
        GameProfile profile = player.getGameProfile();

        if( mcSrv != null && mcSrv.isSinglePlayer() && mcSrv.getServerOwner().equals(profile.getName()) ) {
            return true;
        }

        if( TmrUtils.INSTANCE.canPlayerEditAll() || this.ownerUUID.equals(profile.getId()) ) {
            return true;
        }

        return player.canUseCommand(2, "") && TmrUtils.INSTANCE.canOpEditAll();
    }

    @Override
    public boolean isInGui() {
        return this.inGui;
    }

    @Override
    public <V extends ITurretRAM> V getRAM(Supplier<V> onNull) {
        if( this.turretRAM == null && onNull != null ) {
            this.turretRAM = onNull.get();
        }
        return ReflectionUtils.getCasted(this.turretRAM);
    }

    @Override
    public void updateState() {
        PacketRegistry.sendToAllAround(new PacketUpdateTurretState(this), this.dimension, this.posX, this.posY, this.posZ, 64.0D);
    }

    @Override
    @Nonnull
    public ItemStack getPickedResult(RayTraceResult target) {
        ItemStack pickedItem = TurretRegistry.INSTANCE.getItem(this.delegate.getId());
        if( this.delegate instanceof IVariantHolder ) {
            IVariant variant = this.getVariant();

            if( !((IVariantHolder) this.delegate).isDefaultVariant(variant) ) {
                new ItemTurret.TurretStats(null, null, variant).updateData(pickedItem);
            }
        }

        return pickedItem;
    }

    @Override
    public void onKillCommand() {
        this.attackEntityFrom(DamageSource.MAGIC, Float.MAX_VALUE);
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return entity instanceof EntityPlayer ? entity.getEntityBoundingBox() : null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    public AxisAlignedBB getRangeBB() {
        return this.delegate.getRangeBB(this);
    }

    @Override
    public boolean isBuoy() {
        return this.delegate.isBuoy();
    }

    @Override
    public EntityLiving get() {
        return this;
    }

    @Override
    public ITurret getTurret() {
        return this.delegate;
    }

    @Override
    public ITurret.AttackType getAttackType() {
        return this.delegate.getAttackType();
    }

    @Override
    public String getName() {
        if( this.hasCustomName() ) {
            return this.getCustomNameTag();
        } else {
            return LangUtils.translate(LangUtils.ENTITY_NAME.get(this.delegate.getId()));
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return this.delegate.isBuoy();
    }

    @Override
    public TileEntityTurretCrate dismantle() {
        BlockPos cratePos = this.getPosition();
        if( this.world.setBlockState(cratePos, BlockRegistry.TURRET_CRATE.getDefaultState(), 3) ) {
            TileEntity te = this.world.getTileEntity(cratePos);

            if( te instanceof TileEntityTurretCrate ) {
                TileEntityTurretCrate crate = (TileEntityTurretCrate) te;
                crate.insertTurret(this);

                this.onKillCommand();

                return crate;
            }
        }

        return null;
    }

    @Override
    public IVariant getVariant() {
        if( this.delegate instanceof IVariantHolder ) {
            return ((IVariantHolder) this.delegate).getVariant(new ResourceLocation(this.dataManager.get(VARIANT)));
        }

        return null;
    }

    @Override
    public void setVariant(ResourceLocation variantId) {
        if( this.delegate instanceof IVariantHolder ) {
            this.dataManager.set(VARIANT, variantId.toString());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPartBrightnessForRender(double partY) {
        BlockPos.MutableBlockPos bpos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));

        if( this.world.isBlockLoaded(bpos) ) {
            bpos.setY(MathHelper.floor(this.posY + partY));
            return this.world.getCombinedLight(bpos, 0);
        } else {
            return 0;
        }
    }
}
