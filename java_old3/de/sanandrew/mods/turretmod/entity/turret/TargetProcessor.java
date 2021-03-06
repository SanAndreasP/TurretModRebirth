/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.turret;

import de.sanandrew.mods.sanlib.lib.util.EntityUtils;
import de.sanandrew.mods.sanlib.lib.util.InventoryUtils;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.sanlib.lib.util.ReflectionUtils;
import de.sanandrew.mods.sanlib.lib.util.UuidUtils;
import de.sanandrew.mods.turretmod.api.ammo.IAmmunition;
import de.sanandrew.mods.turretmod.api.ammo.IProjectile;
import de.sanandrew.mods.turretmod.api.event.TargetingEvent;
import de.sanandrew.mods.turretmod.api.turret.ITargetProcessor;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.api.turret.TurretAttributes;
import de.sanandrew.mods.turretmod.item.ItemAmmoCartridge;
import de.sanandrew.mods.turretmod.registry.ammo.AmmunitionRegistry;
import de.sanandrew.mods.turretmod.world.PlayerList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TargetProcessor
        implements ITargetProcessor
{
    private final Map<ResourceLocation, Boolean> entityTargetList;
    private final Map<UUID, Boolean>             playerTargetList;
    private final ITurretInst                    turret;

    private int       ammoCount;
    @Nonnull
    private ItemStack ammoStack;
    private int       shootTicks;
    private int       initShootTicks;
    private Entity    entityToAttack;
    private UUID      entityToAttackUUID;
    private boolean   isShootingClt;
    private boolean   isBlacklistEntity = false;
    private boolean   isBlacklistPlayer = false;

    private long processTicks = 0;

    TargetProcessor(ITurretInst turret) {
        this.entityTargetList = new HashMap<>();
        this.playerTargetList = new HashMap<>();
        this.turret = turret;
        this.initShootTicks = 20;
        this.ammoStack = ItemStack.EMPTY;
    }

    public void init() {
        this.entityTargetList.putAll(TargetList.getStandardTargetList(this.turret.getAttackType()));
        this.playerTargetList.putAll(PlayerList.INSTANCE.getDefaultPlayerList());
    }

    @Override
    public boolean addAmmo(@Nonnull ItemStack stack) {
        return this.addAmmo(stack, null);
    }

    @Override
    public boolean addAmmo(@Nonnull ItemStack stack, ICapabilityProvider excessInv) {
        if( stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN) ) {
            return ItemAmmoCartridge.extractAmmoStacks(stack, this, true);
        } else if( this.isAmmoApplicable(stack) ) {
            IAmmunition type = AmmunitionRegistry.INSTANCE.getObject(stack);
            String subtype = MiscUtils.defIfNull(AmmunitionRegistry.INSTANCE.getSubtype(stack), "");

            if( !this.isAmmoTypeEqual(type, subtype) ) {
                if( excessInv != null ) {
                    this.putAmmoInInventory(excessInv);
                } else {
                    this.dropAmmo();
                }
            }

            int maxCapacity = this.getMaxAmmoCapacity() - this.ammoCount;
            if( maxCapacity > 0 ) {
                if( !this.hasAmmo() ) {
                    this.ammoStack = AmmunitionRegistry.INSTANCE.getItem(type.getId(), subtype);
                } else if( !AmmunitionRegistry.INSTANCE.isEqual(stack, this.ammoStack) ) {
                    return false;
                }

                int provided = type.getAmmoCapacity();
                int providedStack = stack.getCount() * provided; //provides 4*16=64, needs 56 = 56 / 64 * 4
                if( providedStack - maxCapacity > 0 ) {
                    int stackSub = MathHelper.floor(maxCapacity / (double) providedStack * stack.getCount());
                    if( stackSub > 0 ) {
                        this.ammoCount += stackSub * provided;
                        stack.shrink(stackSub);
                    } else {
                        return false;
                    }
                } else {
                    this.ammoCount += providedStack;
                    stack.setCount(0);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public int getAmmoCount() {
        return this.ammoCount;
    }

    @Override
    @Nonnull
    public ItemStack getAmmoStack() {
        if( ItemStackUtils.isValid(this.ammoStack) ) {
            return this.ammoStack.copy();
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void setAmmoStackInternal(ItemStack stack) {
        this.ammoStack = stack;
    }

    public void setAmmoStackInternal(ItemStack stack, int count) {
        this.ammoStack = stack;
        this.ammoCount = count;
    }

    @Override
    public boolean hasAmmo() {
        return ItemStackUtils.isValid(this.ammoStack) && this.ammoCount > 0;
    }

    @Override
    public void dropExcessAmmo() {
        if( this.hasAmmo() ) {
            int decrAmmo = this.ammoCount - this.getMaxAmmoCapacity();
            if( decrAmmo > 0 ) {
                NonNullList<ItemStack> items = NonNullList.create();
                IAmmunition type = AmmunitionRegistry.INSTANCE.getObject(this.ammoStack);
                int maxStackSize = this.ammoStack.getMaxStackSize();

                while( decrAmmo > 0 && type.isValid() ) {
                    ItemStack stack = this.ammoStack.copy();
                    stack.setCount(Math.min(decrAmmo / type.getAmmoCapacity(), maxStackSize));
                    decrAmmo -= stack.getCount() * type.getAmmoCapacity();
                    if( stack.getCount() <= 0 ) {
                        break;
                    }
                    items.add(stack);
                }

                this.ammoCount = this.getMaxAmmoCapacity();

                this.spawnItemEntities(items);
            }
        }
    }

    @Override
    public void decrAmmo() {
        TargetingEvent.ConsumeAmmo event = new TargetingEvent.ConsumeAmmo(this, this.ammoStack, 1);
        if( !TARGET_BUS.post(event) && event.getResult() != Event.Result.DENY ) {
            this.ammoCount -= event.consumeAmount;
            if( this.ammoCount < 0 ) {
                this.ammoCount = 0;
            }
        }
    }

    public NonNullList<ItemStack> extractAmmoItems() {
        NonNullList<ItemStack> items = NonNullList.create();
        int maxStackSize = this.ammoStack.getMaxStackSize();
        IAmmunition type = AmmunitionRegistry.INSTANCE.getObject(this.ammoStack);

        while( this.ammoCount > 0 && type.isValid() ) {
            ItemStack stack = this.ammoStack.copy();
            stack.setCount(Math.min(this.ammoCount / type.getAmmoCapacity(), maxStackSize));
            this.ammoCount -= stack.getCount() * type.getAmmoCapacity();
            if( stack.getCount() <= 0 ) {
                this.ammoCount = 0;
                break;
            }
            items.add(stack);
        }

        return items;
    }

    private void spawnItemEntities(NonNullList<ItemStack> stacks) {
        if( !stacks.isEmpty() ) {
            EntityLiving turretL = this.turret.get();
            stacks.forEach(stack -> {
                EntityItem item = new EntityItem(turretL.world, turretL.posX, turretL.posY, turretL.posZ, stack);
                turretL.world.spawnEntity(item);
            });
        }
    }

    void dropAmmo() {
        if( this.hasAmmo() ) {
            NonNullList<ItemStack> items = this.extractAmmoItems();
            this.ammoStack = ItemStack.EMPTY;
            this.spawnItemEntities(items);
        }
    }

    @Override
    public void putAmmoInInventory(ICapabilityProvider inventory) {
        if( this.hasAmmo() ) {
            NonNullList<ItemStack> items = this.extractAmmoItems();

            this.ammoStack = ItemStack.EMPTY;

            if( !items.isEmpty() ) {
                EntityLiving turretL = this.turret.get();
                for( ItemStack stack : items ) {
                    stack = InventoryUtils.addStackToCapability(stack, inventory, EnumFacing.UP, false);
                    if( ItemStackUtils.isValid(stack) ) {
                        EntityItem item = new EntityItem(turretL.world, turretL.posX, turretL.posY, turretL.posZ, stack);
                        turretL.world.spawnEntity(item);
                    }
                }
            }
        }
    }

    @Override
    public boolean isAmmoApplicable(@Nonnull ItemStack stack) {
        return getAmmoApplyType(stack) != ApplyType.NOT_COMPATIBLE;
    }

    @Override
    public ApplyType getAmmoApplyType(@Nonnull ItemStack stack) {
        if( ItemStackUtils.isValid(stack) ) {
            IAmmunition stackType = AmmunitionRegistry.INSTANCE.getObject(stack);
            if( stackType.isValid() ) {
                if( this.isAmmoTypeEqual(stackType, AmmunitionRegistry.INSTANCE.getSubtype(stack)) ) {
                    return this.ammoCount < this.getMaxAmmoCapacity() ? ApplyType.ADD : ApplyType.NOT_COMPATIBLE;
                } else {
                    Collection<IAmmunition> types = AmmunitionRegistry.INSTANCE.getObjects(this.turret.getTurret());
                    return types.contains(stackType) ? ApplyType.REPLACE : ApplyType.NOT_COMPATIBLE;
                }
            }
        }

        return ApplyType.NOT_COMPATIBLE;
    }

    private boolean isAmmoTypeEqual(IAmmunition ammo, String subtype) {
        subtype = subtype != null ? subtype : "";
        IAmmunition currType = AmmunitionRegistry.INSTANCE.getObject(this.ammoStack);
        String currSubtype = MiscUtils.defIfNull(AmmunitionRegistry.INSTANCE.getSubtype(this.ammoStack), "");

        return currType.getId().equals(ammo.getId()) && subtype.equals(currSubtype);
    }

    @Override
    public final int getMaxAmmoCapacity() {
        return MathHelper.ceil(this.turret.get().getEntityAttribute(TurretAttributes.MAX_AMMO_CAPACITY).getAttributeValue());
    }

    @Override
    public final int getMaxShootTicks() {
        return MathHelper.ceil(this.turret.get().getEntityAttribute(TurretAttributes.MAX_RELOAD_TICKS).getAttributeValue());
    }

    @Override
    public final boolean isShooting() {
        return this.initShootTicks <= 0 || this.isShootingClt;
    }

    @Override
    public boolean canShoot() {
        return this.initShootTicks <= 0 && this.shootTicks == 0;
    }

    @Override
    public void setShot(boolean success) {
        this.shootTicks = success ? this.getMaxShootTicks() : this.getMaxInitShootTicks();
    }

    @Override
    public void decrInitShootTicks() {
        this.initShootTicks--;
    }

    @Override
    public void resetInitShootTicks() {
        this.initShootTicks = this.getMaxInitShootTicks();
    }

    private int getMaxInitShootTicks() {
        return (int) Math.round(this.turret.get().getEntityAttribute(TurretAttributes.MAX_INIT_SHOOT_TICKS).getAttributeValue());
    }

    @Override
    public Entity getProjectile() {
        if( this.hasAmmo() ) {
            IAmmunition ammo = AmmunitionRegistry.INSTANCE.getObject(this.ammoStack);
            String ammoSubtype = AmmunitionRegistry.INSTANCE.getSubtype(this.ammoStack);
            IProjectile proj = ammo.getProjectile(this.turret);
            if( proj != null ) {
                double attackModifier = this.turret.get().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                if( this.entityToAttack != null ) {
                    return new EntityTurretProjectile(this.turret.get().world, proj, ammo, ammoSubtype, (EntityTurret) this.turret, this.entityToAttack, attackModifier);
                } else {
                    return new EntityTurretProjectile(this.turret.get().world, proj, ammo, ammoSubtype, (EntityTurret) this.turret, this.turret.get().getLookVec(), attackModifier);
                }
            }
        }

        return null;
    }

    @Override
    public double getRangeVal() {
        AxisAlignedBB aabb = getAdjustedRange(false);
        return Math.max(aabb.maxX - aabb.minX, Math.max(aabb.maxY - aabb.minY, aabb.maxZ - aabb.minZ)) / 2.0D;
    }

    @Override
    public AxisAlignedBB getAdjustedRange(boolean doOffset) {
        AxisAlignedBB aabb = this.turret.getRangeBB();
        EntityLiving turretL = this.turret.get();
        return doOffset ? aabb.offset(turretL.posX, turretL.posY, turretL.posZ) : aabb;
    }

    private boolean checkTargetListeners(Entity e) {
        TargetingEvent.TargetCheck event = new TargetingEvent.TargetCheck(this, e);
        return !TARGET_BUS.post(event) && event.getResult() != Event.Result.DENY;
    }

    @Override
    public boolean shootProjectile() {
        TargetingEvent.Shooting event = new TargetingEvent.Shooting(this);
        if( TARGET_BUS.post(event) ) {
            return event.getResult() != Event.Result.DENY;
        }

        Entity projectile = this.getProjectile();
        if( projectile != null ) {
            this.turret.get().world.spawnEntity(projectile);
            this.playSound(this.turret.getShootSound(), 1.8F);
            this.turret.setShooting();
            this.decrAmmo();
            return event.getResult() != Event.Result.DENY;
        } else {
            this.playSound(this.turret.getNoAmmoSound(), 1.0F);
            return event.getResult() == Event.Result.ALLOW;
        }
    }

    @Override
    public void playSound(SoundEvent sound, float volume) {
        EntityLiving turretL = this.turret.get();
        final float pitch = 1.0F / (turretL.getRNG().nextFloat() * 0.4F + 1.2F) + 0.5F;
        turretL.world.playSound(null, turretL.posX, turretL.posY, turretL.posZ, sound, SoundCategory.NEUTRAL, volume, pitch);
    }

    @Override
    public void onTick() {
        boolean changed = false;
        EntityLiving turretL = this.turret.get();

        if( !this.turret.isActive() ) {
            if( this.entityToAttack != null || this.entityToAttackUUID != null ) {
                this.resetInitShootTicks();
                this.entityToAttack = null;
                this.entityToAttackUUID = null;
                this.turret.updateState();
            }
            return;
        }

        if( this.shootTicks > 0 ) {
            this.shootTicks--;
        }

        if( this.entityToAttack == null && this.entityToAttackUUID != null ) {
            this.entityToAttack = EntityUtils.getEntityByUUID(turretL.world, this.entityToAttackUUID);
        }

        AxisAlignedBB aabb = this.getAdjustedRange(true);

        if( TARGET_BUS.post(new TargetingEvent.ProcessorTick(this, this.processTicks)) ) {
            return;
        }

        if( this.processTicks++ % 10 == 0 ) {
            if( this.entityToAttack == null ) {
                for( Entity entityObj : getValidTargetList(aabb) ) {
                    if( this.checkTargetListeners(entityObj) ) {
                        this.entityToAttack = entityObj;
                        this.entityToAttackUUID = entityObj.getUniqueID();
                        changed = true;
                        break;
                    }
                }
            }
        }

        if( this.entityToAttack != null ) {
            if( this.isEntityValidTarget(this.entityToAttack, aabb) && this.checkTargetListeners(this.entityToAttack) ) {
                if( this.canShoot() ) {
                    this.setShot(shootProjectile());
                    changed = true;
                } else if( this.initShootTicks > 0 ) {
                    this.decrInitShootTicks();
                }
            } else {
                this.resetInitShootTicks();
                this.entityToAttack = null;
                this.entityToAttackUUID = null;
                changed = true;
            }
        }

        if( changed ) {
            this.turret.updateState();
        }
    }

    @Override
    public void onTickClient() {
        if( this.entityToAttack != null && !this.entityToAttack.isEntityAlive() ) {
            this.entityToAttackUUID = null;
            this.entityToAttack = null;
        }
    }

    @Override
    public boolean isEntityBlacklist() {
        return this.isBlacklistEntity;
    }

    @Override
    public boolean isPlayerBlacklist() {
        return this.isBlacklistPlayer;
    }

    @Override
    public void setEntityBlacklist(boolean isBlacklist) {
        this.isBlacklistEntity = isBlacklist;
    }

    @Override
    public void setPlayerBlacklist(boolean isBlacklist) {
        this.isBlacklistPlayer = isBlacklist;
    }

    @Override
    public boolean isEntityValidTarget(Entity entity) {
        return this.isEntityValidTarget(entity, this.getAdjustedRange(true));
    }

    @Override
    public List<Entity> getValidTargetList() {
        return this.getValidTargetList(this.getAdjustedRange(true));
    }

    @Override
    public boolean isEntityTargeted(Entity entity) {
        if( entity instanceof EntityPlayer ) {
            UUID id = entity.getUniqueID();
            return this.playerTargetList.containsKey(id) && (this.isBlacklistPlayer ^ this.isPlayerTargeted(id));
        } else {
            ResourceLocation id = EntityList.getKey(entity.getClass());
            return this.entityTargetList.containsKey(id) && (this.isBlacklistEntity ^ this.isEntityTargeted(id));
        }
    }

    private List<Entity> getValidTargetList(AxisAlignedBB aabb) {
        return turret.get().world.getEntitiesInAABBexcluding(turret.get(), aabb, entity -> this.isEntityValidTarget(entity, aabb));
    }

    private boolean isEntityValidTarget(Entity entity, AxisAlignedBB aabb) {
        return isEntityTargeted(entity) && entity.isEntityAlive() && entity.getEntityBoundingBox().intersects(aabb)
               && (this.turret.getTurret().canSeeThroughBlocks() || this.turret.get().canEntityBeSeen(entity));
    }

    @Override
    public boolean isPlayerTargeted(UUID id) {
        return Boolean.TRUE.equals(this.playerTargetList.get(id));
    }

    @Override
    public boolean isEntityTargeted(ResourceLocation id) {
        return Boolean.TRUE.equals(this.entityTargetList.get(id));
    }

    @Override
    public ITurretInst getTurretInst() {
        return this.turret;
    }

    @Override
    public boolean hasTarget() {
        return this.entityToAttack != null;
    }

    @Override
    public Entity getTarget() {
        return this.entityToAttack;
    }

    @Override
    public void writeToNbt(NBTTagCompound nbt) {
        nbt.setInteger("ammoCount", this.ammoCount);
        if( ItemStackUtils.isValid(this.ammoStack) ) {
            NBTTagCompound stackTag = new NBTTagCompound();
            this.ammoStack.writeToNBT(stackTag);
            nbt.setTag("ammoStack", stackTag);
        }
        if( this.entityToAttackUUID != null ) {
            nbt.setString("targetUUID", this.entityToAttackUUID.toString());
        }

        nbt.setBoolean("entityBlacklist", this.isBlacklistEntity);
        nbt.setBoolean("playerBlacklist", this.isBlacklistPlayer);

        NBTTagList entityTargets = new NBTTagList();
        for( Map.Entry<ResourceLocation, Boolean> res : this.getEntityTargets().entrySet() ) {
            NBTTagCompound entityEntry = new NBTTagCompound();
            entityEntry.setString("ID", res.getKey().toString());
            entityEntry.setBoolean("Enabled", res.getValue());
            entityTargets.appendTag(entityEntry);
        }
        nbt.setTag("EntityTargetsCMP", entityTargets);

        NBTTagList playerTargets = new NBTTagList();
        for( Map.Entry<UUID, Boolean> res : this.getPlayerTargets().entrySet() ) {
            NBTTagCompound playerEntry = new NBTTagCompound();
            playerEntry.setString("ID", res.getKey().toString());
            playerEntry.setBoolean("Enabled", res.getValue());
            playerTargets.appendTag(playerEntry);
        }
        nbt.setTag("PlayerTargetsCMP", playerTargets);
    }

    @Override
    public void readFromNbt(NBTTagCompound nbt) {
        if( nbt == null ) {
            return;
        }
        this.ammoCount = nbt.getInteger("ammoCount");
        if( nbt.hasKey("ammoStack") ) {
            this.ammoStack = new ItemStack(nbt.getCompoundTag("ammoStack"));
        }
        if( nbt.hasKey("targetUUID") ) {
            this.entityToAttackUUID = UUID.fromString(nbt.getString("targetUUID"));
        }

        this.isBlacklistEntity = nbt.getBoolean("entityBlacklist");
        this.isBlacklistPlayer = nbt.getBoolean("playerBlacklist");

        if( nbt.hasKey("entityTargets") ) { // @deprecated
            List<Class<? extends Entity>> entityTgt = new ArrayList<>();
            NBTTagList list = nbt.getTagList("entityTargets", Constants.NBT.TAG_STRING);
            for( int i = 0; i < list.tagCount(); i++ ) {
                Class<? extends Entity> cls = ReflectionUtils.getClass(list.getStringTagAt(i));
                if( cls != null ) {
                    entityTgt.add(cls);
                }
            }
            this.updateEntityTargets(entityTgt.stream().map(EntityList::getKey).toArray(ResourceLocation[]::new));
        } else {
            NBTTagList list = nbt.getTagList("EntityTargetsCMP", Constants.NBT.TAG_COMPOUND);
            for( int i = 0; i < list.tagCount(); i++ ) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                this.updateEntityTarget(new ResourceLocation(tag.getString("ID")), tag.getBoolean("Enabled"));
            }
        }

        if( nbt.hasKey("playerTargets") ) { // @deprecated
            List<UUID> playerTgt = new ArrayList<>();
            NBTTagList list = nbt.getTagList("playerTargets", Constants.NBT.TAG_STRING);
            for( int i = 0; i < list.tagCount(); i++ ) {
                try {
                    UUID id = UUID.fromString(list.getStringTagAt(i));
                    if( id.equals(UuidUtils.EMPTY_UUID) ) {
                        this.isBlacklistPlayer = true;
                    } else {
                        playerTgt.add(id);
                    }
                } catch( IllegalArgumentException ignored ) {
                }
            }
            this.updatePlayerTargets(playerTgt.toArray(new UUID[0]));
        } else {
            NBTTagList list = nbt.getTagList("PlayerTargetsCMP", Constants.NBT.TAG_COMPOUND);
            for( int i = 0; i < list.tagCount(); i++ ) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                this.updatePlayerTarget(UUID.fromString(tag.getString("ID")), tag.getBoolean("Enabled"));
            }
        }
    }

    @Override
    public ResourceLocation[] getEnabledEntityTargets() {
        return this.entityTargetList.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toArray(ResourceLocation[]::new);
    }

    @Override
    public UUID[] getEnabledPlayerTargets() {
        return this.playerTargetList.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toArray(UUID[]::new);
    }

    @Override
    public Map<ResourceLocation, Boolean> getEntityTargets() {
        return new HashMap<>(this.entityTargetList);
    }

    @Override
    public Map<UUID, Boolean> getPlayerTargets() {
        return new HashMap<>(this.playerTargetList);
    }

    @Override
    public void updateEntityTarget(ResourceLocation res, boolean active) {
        if( TargetList.isEntityTargetable(res, this.turret.getAttackType()) ) {
            this.entityTargetList.put(res, active);
        }
    }

    @Override
    public void updatePlayerTarget(UUID uid, boolean active) {
        this.playerTargetList.put(uid, active);
    }

    @Override
    public void updateEntityTargets(ResourceLocation[] keys) {
        this.entityTargetList.entrySet().forEach(entry -> entry.setValue(false));

        Arrays.stream(keys).filter(r -> TargetList.isEntityTargetable(r, this.turret.getAttackType())).forEach(r -> this.entityTargetList.put(r, true));
    }

    @Override
    public void updatePlayerTargets(UUID[] uuids) {
        this.playerTargetList.entrySet().forEach(entry -> entry.setValue(false));

        for( UUID uuid : uuids ) {
            if( uuid != null ) {
                this.playerTargetList.put(uuid, true);
            }
        }
    }

    public void updateClientState(int targetId, int ammoCount, @Nonnull ItemStack ammoStack, boolean isShooting) {
        EntityLiving turretL = this.turret.get();
        if( turretL.world.isRemote ) {
            this.entityToAttack = targetId < 0 ? null : turretL.world.getEntityByID(targetId);
            this.ammoCount = ammoCount;
            this.ammoStack = ammoStack;
            this.isShootingClt = isShooting;
        }
    }

    @Override
    public String getTargetName() {
        return this.hasTarget() ? EntityList.getEntityString(this.entityToAttack) : "";
    }
}
