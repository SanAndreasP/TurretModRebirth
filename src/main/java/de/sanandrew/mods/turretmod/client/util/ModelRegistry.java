/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.util;

import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.client.render.tileentity.RenderElectrolyteGenerator;
import de.sanandrew.mods.turretmod.client.render.tileentity.RenderTurretAssembly;
import de.sanandrew.mods.turretmod.item.ItemRegistry;
import de.sanandrew.mods.turretmod.item.ItemTurret;
import de.sanandrew.mods.turretmod.registry.ammo.TurretAmmoRegistry;
import de.sanandrew.mods.turretmod.api.ammo.ITurretAmmo;
import de.sanandrew.mods.turretmod.registry.repairkit.RepairKitRegistry;
import de.sanandrew.mods.turretmod.api.repairkit.TurretRepairKit;
import de.sanandrew.mods.turretmod.api.turret.ITurretInfo;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import de.sanandrew.mods.turretmod.api.upgrade.ITurretUpgrade;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.tileentity.electrolytegen.TileEntityElectrolyteGenerator;
import de.sanandrew.mods.turretmod.tileentity.assembly.TileEntityTurretAssembly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class ModelRegistry
{
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) throws Exception {
        setStandardModel(ItemRegistry.turret_control_unit);
        setStandardModel(ItemRegistry.turret_info);
        setStandardModel(ItemRegistry.assembly_upg_filter);
        setStandardModel(ItemRegistry.assembly_upg_auto);
        setStandardModel(ItemRegistry.assembly_upg_speed);
        setStandardModel(BlockRegistry.electrolyte_generator);
        setStandardModel(BlockRegistry.turret_assembly);

        setCustomMeshModel(ItemRegistry.turret_placer, new MeshDefUUID.Turret());
        setCustomMeshModel(ItemRegistry.turret_ammo, new MeshDefUUID.Ammo());
        setCustomMeshModel(ItemRegistry.turret_upgrade, new MeshDefUUID.Upgrade());
        setCustomMeshModel(ItemRegistry.repair_kit, new MeshDefUUID.Repkit());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretAssembly.class, new RenderTurretAssembly());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityElectrolyteGenerator.class, new RenderElectrolyteGenerator());
    }

    private static void setStandardModel(Item item) {
        ResourceLocation regName = item.getRegistryName();
        if( regName != null ) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(regName, "inventory"));
        }
    }

    private static void setStandardModel(Block item) {
        Item itm = Item.getItemFromBlock(item);
        if( itm != Items.AIR ) {
            setStandardModel(itm);
        }
    }

    private static void setCustomMeshModel(Item item, MeshDefUUID<?> mesher) {
        ModelLoader.setCustomMeshDefinition(item, mesher);
        ModelBakery.registerItemVariants(item, mesher.getResLocations());
    }

    private static abstract class MeshDefUUID<T>
            implements ItemMeshDefinition
    {
        public final Map<UUID, ModelResourceLocation> modelRes = new HashMap<>();

        @Override
        public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
            T type = getType(stack);
            ResourceLocation regName = stack.getItem().getRegistryName();
            if( regName != null ) {
                return type != null ? this.modelRes.get(getId(type)) : new ModelResourceLocation(regName, "inventory");
            } else {
                return null;
            }
        }

        public abstract T getType(@Nonnull ItemStack stack);
        public abstract UUID getId(T type);

        public ResourceLocation[] getResLocations() {
            return this.modelRes.values().toArray(new ModelResourceLocation[this.modelRes.size()]);
        }

        static final class Turret
                extends MeshDefUUID<ITurretInfo>
        {
            Turret() {
                for( ITurretInfo info : TurretRegistry.INSTANCE.getRegisteredInfos() ) {
                    ModelResourceLocation modelRes = new ModelResourceLocation(info.getModel(), "inventory");
                    this.modelRes.put(info.getUUID(), modelRes);
                }
            }

            @Override
            public ITurretInfo getType(@Nonnull ItemStack stack) { return ItemTurret.getTurretInfo(stack); }

            @Override
            public UUID getId(ITurretInfo type) { return type.getUUID(); }
        }

        static final class Ammo
                extends MeshDefUUID<ITurretAmmo>
        {
            public Ammo() {
                for( ITurretAmmo ammo : TurretAmmoRegistry.INSTANCE.getRegisteredTypes() ) {
                    ModelResourceLocation modelRes = new ModelResourceLocation(ammo.getModel(), "inventory");
                    this.modelRes.put(ammo.getId(), modelRes);
                }
            }

            @Override
            public ITurretAmmo getType(@Nonnull ItemStack stack) { return TurretAmmoRegistry.INSTANCE.getType(stack); }

            @Override
            public UUID getId(ITurretAmmo type) { return type.getId(); }
        }

        static final class Upgrade
                extends MeshDefUUID<ITurretUpgrade>
        {
            public Upgrade() {
                for( ITurretUpgrade upg : UpgradeRegistry.INSTANCE.getUpgrades() ) {
                    ModelResourceLocation modelRes = new ModelResourceLocation(upg.getModel(), "inventory");
                    this.modelRes.put(UpgradeRegistry.INSTANCE.getUpgradeId(upg), modelRes);
                }
            }

            @Override
            public ITurretUpgrade getType(@Nonnull ItemStack stack) { return UpgradeRegistry.INSTANCE.getUpgrade(stack); }

            @Override
            public UUID getId(ITurretUpgrade type) { return UpgradeRegistry.INSTANCE.getUpgradeId(type); }
        }

        static final class Repkit
                extends MeshDefUUID<TurretRepairKit>
        {
            public Repkit() {
                for( TurretRepairKit kit : RepairKitRegistry.INSTANCE.getRegisteredTypes() ) {
                    ModelResourceLocation modelRes = new ModelResourceLocation(kit.getModel(), "inventory");
                    this.modelRes.put(RepairKitRegistry.INSTANCE.getTypeId(kit), modelRes);
                }
            }

            @Override
            public TurretRepairKit getType(@Nonnull ItemStack stack) { return RepairKitRegistry.INSTANCE.getType(stack); }

            @Override
            public UUID getId(TurretRepairKit type) { return RepairKitRegistry.INSTANCE.getTypeId(type); }
        }
    }
}
