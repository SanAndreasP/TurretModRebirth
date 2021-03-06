/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.item.ammo;

import com.google.common.base.Strings;
import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.ammo.IAmmunition;
import de.sanandrew.mods.turretmod.api.ammo.IAmmunitionRegistry;
import de.sanandrew.mods.turretmod.api.ammo.IProjectile;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretEntity;
import de.sanandrew.mods.turretmod.entity.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AmmunitionRegistry
        implements IAmmunitionRegistry
{
    public static final AmmunitionRegistry INSTANCE = new AmmunitionRegistry();

    private final Map<ResourceLocation, IAmmunition>  ammoTypes;
    private final Map<ITurret, UModList<IAmmunition>> ammoTypesFromTurret;
    private final Collection<IAmmunition>             uAmmoTypes;

    private static final IAmmunition EMPTY = new IAmmunition()
    {
        private final ResourceLocation id = new ResourceLocation("null");

        @Nonnull @Override public ResourceLocation getId()                               { return this.id; }
        @Nonnull @Override public ITurret getApplicableTurret()                           { return TurretRegistry.INSTANCE.getDefault(); }
        @Override          public int getCapacity()                     { return 0; }
        @Override          public IProjectile getProjectile(ITurretEntity turret) { return null; }
        @Override          public boolean          isValid()                             { return false; }
    };

    private AmmunitionRegistry() {
        this.ammoTypes = new HashMap<>();
        this.ammoTypesFromTurret = new HashMap<>();

        this.uAmmoTypes = Collections.unmodifiableCollection(this.ammoTypes.values());
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event, final String modId) {
        event.getRegistry().registerAll(ItemRegistry.TURRET_AMMO.entrySet().stream()
                                                                .filter(e -> e.getKey().getNamespace().equals(modId))
                                                                .map(e -> e.getValue().setRegistryName(e.getKey())).toArray(Item[]::new));
    }

    @Override
    @Nonnull
    public Collection<IAmmunition> getAll() {
        return this.uAmmoTypes;
    }

    @Override
    @Nonnull
    public IAmmunition get(ResourceLocation id) {
        return this.ammoTypes.getOrDefault(id, EMPTY);
    }

    @Override
    @Nonnull
    public IAmmunition get(@Nonnull ItemStack stack) {
        if( ItemStackUtils.isValid(stack) && stack.getItem() instanceof AmmoItem ) {
            return ((AmmoItem) stack.getItem()).getAmmo();
        }

        return EMPTY;
    }

    @Override
    @Nonnull
    public Collection<IAmmunition> getAll(ITurret turret) {
        return this.ammoTypesFromTurret.get(turret).umList;
    }

    @Override
    public void register(@Nonnull IAmmunition obj) {
        if( this.ammoTypes.containsKey(obj.getId()) ) {
            TmrConstants.LOG.log(Level.ERROR, String.format("The ammo ID %s is already registered!", obj.getId()), new InvalidParameterException());
            return;
        }

        if( obj.getCapacity() < 1 ) {
            TmrConstants.LOG.log(Level.ERROR, String.format("Ammo ID %s provides less than 1 round!", obj.getId()), new InvalidParameterException());
            return;
        }

        this.ammoTypes.put(obj.getId(), obj);
        this.ammoTypesFromTurret.computeIfAbsent(obj.getApplicableTurret(), (t) -> new UModList<>()).mList.add(obj);

        ItemRegistry.TURRET_AMMO.put(obj.getId(), new AmmoItem(obj.getId()));
    }

    @Override
    public String getSubtype(ItemStack stack) {
        if( stack.getItem() instanceof AmmoItem ) {
            CompoundNBT tmrStack = stack.getTagElement(TmrConstants.ID);
            if( tmrStack != null && tmrStack.contains("Subtype", Constants.NBT.TAG_STRING) ) {
                return tmrStack.getString("Subtype");
            }
        }

        return null;
    }

    @Override
    public ItemStack setSubtype(ItemStack stack, String type) {
        if( !Strings.isNullOrEmpty(type) ) {
            Item item = stack.getItem();
            if( item instanceof AmmoItem ) {
                String[] subtypes = ((AmmoItem) item).getAmmo().getSubtypes();
                if( subtypes != null && Arrays.asList(subtypes).contains(type) ) {
                    stack.getOrCreateTagElement(TmrConstants.ID).putString("Subtype", type);
                }
            }
        }

        return stack;
    }

    @Override
    @Nonnull
    public IAmmunition getDefault() {
        return EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack getItem(ResourceLocation id) {
        return this.getItem(id, null);
    }

    @Override
    @Nonnull
    public ItemStack getItem(ResourceLocation id, String subtype) {
        if( !this.get(id).isValid() ) {
            throw new IllegalArgumentException("Cannot get turret ammo item with invalid type!");
        }

        return setSubtype(new ItemStack(ItemRegistry.TURRET_AMMO.get(id), 1), subtype);
    }

    private static class UModList<T>
    {
        private final ArrayList<T>  mList  = new ArrayList<>();
        private final Collection<T> umList = Collections.unmodifiableList(this.mList);
    }
}
