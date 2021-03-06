/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.turret;

import de.sanandrew.mods.sanlib.lib.util.ItemStackUtils;
import de.sanandrew.mods.turretmod.api.Resources;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.turret.ITurretEntity;
import de.sanandrew.mods.turretmod.api.turret.ITurretRegistry;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.item.TurretItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.RegistryEvent;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TurretRegistry
        implements ITurretRegistry
{
    public static final TurretRegistry INSTANCE = new TurretRegistry();

    private static final ITurret EMPTY = new EmptyTurret();

    private static final Map<ResourceLocation, ITurret> TURRETS = new HashMap<>();
    private static final Collection<ITurret>            TURRETS_VIEW = Collections.unmodifiableCollection(TURRETS.values());

    private TurretRegistry() { }

    @Nonnull
    @Override
    public Collection<ITurret> getAll() {
        return TURRETS_VIEW;
    }

    @Nonnull
    @Override
    public ITurret get(ResourceLocation id) {
        return TURRETS.getOrDefault(id, EMPTY);
    }

    @Override
    public void register(@Nonnull ITurret obj) {
        ResourceLocation id = obj.getId();

        if( TURRETS.containsKey(id) ) {
            TmrConstants.LOG.log(Level.ERROR, String.format("The turret %s is already registered!", id), new InvalidParameterException());
            return;
        }

        TURRETS.put(id, obj);

        ItemRegistry.TURRET_PLACERS.put(id, new TurretItem(id));
    }

//    @Override
//    public void register(@Nonnull ResourceLocation id) {
//        if( this.turrets.containsKey(id) ) {
//            TmrConstants.LOG.log(Level.ERROR, String.format("The turret %s is already registered!", id), new InvalidParameterException());
//            return;
//        }
//
//        try( InputStream is = TurretModRebirth.class.getClassLoader().getResourceAsStream("./data/" + id.getNamespace() + "/turrets/" + id.getPath() + ".json") ) {
//            this.turrets.put(id, new JsonTurret(id, is));
//        } catch( IOException | NullPointerException ex ) {
//            this.turrets.put(id, EMPTY);
//        }
//
//        ItemRegistry.TURRET_PLACERS.put(id, new TurretItem(id));
//    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event, final String modId) {
        event.getRegistry().registerAll(ItemRegistry.TURRET_PLACERS.entrySet().stream()
                                                                   .filter(e -> e.getKey().getNamespace().equals(modId))
                                                                   .map(e -> e.getValue().setRegistryName(e.getKey())).toArray(Item[]::new));
    }

    @Nonnull
    @Override
    public ITurret getDefault() {
        return EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack getItem(ResourceLocation id) {
        if( !this.get(id).isValid() ) {
            throw new IllegalArgumentException("Cannot get turret item with invalid type!");
        }

        return new ItemStack(ItemRegistry.TURRET_PLACERS.get(id), 1);
    }

    @Override
    @Nonnull
    public ItemStack getItem(ITurretEntity turret) {
        ItemStack stack = this.getItem(turret.getDelegate().getId());
        new TurretItem.TurretStats(turret).updateData(stack);

        return stack;
    }

    @Nonnull
    @Override
    public ITurret get(@Nonnull ItemStack stack) {
        if( ItemStackUtils.isValid(stack) && stack.getItem() instanceof TurretItem ) {
            return this.get(((TurretItem) stack.getItem()).turretId);
        }

        return TurretRegistry.EMPTY;
    }

    private static class EmptyTurret
            implements ITurret
    {
        private static final AxisAlignedBB BB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

        @Nonnull @Override public ResourceLocation getId() { return Resources.NULL; }
        @Nonnull @Override public ResourceLocation getModelLocation() { return Resources.NULL; }
        
        @Override public ResourceLocation getBaseTexture(ITurretEntity turret) { return null; }
        @Override public ResourceLocation getGlowTexture(ITurretEntity turret) { return null; }
        @Override public SoundEvent getShootSound(ITurretEntity turret) { return null; }
        @Override public AxisAlignedBB getRangeBB(ITurretEntity turret) { return BB; }
        @Override public int getTier() { return 0; }
        @Override public float getHealth() { return 0; }
        @Override public int getAmmoCapacity() { return 0; }
        @Override public int getReloadTicks() { return 0; }
        @Override public boolean isValid() { return false; }
    }

}
