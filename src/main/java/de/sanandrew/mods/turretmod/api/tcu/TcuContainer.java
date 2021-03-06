package de.sanandrew.mods.turretmod.api.tcu;

import de.sanandrew.mods.turretmod.api.IRegistryObject;
import de.sanandrew.mods.turretmod.api.turret.ITurretEntity;
import de.sanandrew.mods.turretmod.inventory.ContainerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class TcuContainer
        extends Container
{
    public final ITurretEntity turret;
    public final boolean isRemote;
    public final ResourceLocation currPage;

    public TcuContainer(int windowId, PlayerInventory playerInventory, ITurretEntity turret, ResourceLocation currPage, boolean isRemote) {
        super(ContainerRegistry.TCU, windowId);

        this.turret = turret;
        this.currPage = currPage;
        this.isRemote = isRemote;
    }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity player) {
        return this.turret != null && this.turret.get().isAlive();
    }

    @FunctionalInterface
    public interface TcuContainerProvider {
        TcuContainer apply(int windowId, PlayerInventory playerInventory, ITurretEntity turret, ResourceLocation currPage, boolean isRemote);
    }
}
