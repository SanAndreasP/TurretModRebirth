package de.sanandrew.mods.turretmod.client.model.block;

import de.sanandrew.mods.sanlib.lib.client.ModelJsonHandler;
import de.sanandrew.mods.sanlib.lib.client.ModelJsonLoader;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.registry.Resources;
import de.sanandrew.mods.turretmod.tileentity.assembly.TileEntityTurretAssembly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.resources.IResourceManager;

import java.util.Arrays;

public class ModelTurretAssembly
        extends ModelBase
        implements ModelJsonHandler<ModelTurretAssembly, ModelJsonLoader.ModelJson>
{
    private ModelRenderer base;
    private ModelRenderer robotBinding;
    private ModelRenderer robotArm;

    private final ModelJsonLoader<ModelTurretAssembly, ModelJsonLoader.ModelJson> modelJson;

    public ModelTurretAssembly() {
        this.modelJson = ModelJsonLoader.create(this, Resources.TILE_TURRET_ASSEMBLY_MODEL.resource, "base", "robotBinding", "robotArm");
    }

    public void render(float scale, TileEntityTurretAssembly te, float armX, float armZ) {
        int meta = te.hasWorld() ? BlockRegistry.TURRET_ASSEMBLY.getDirection(te.getBlockMetadata()).getHorizontalIndex() : 0;
        this.base.rotateAngleY = (float)(90.0D * meta / 180.0D * Math.PI);

        this.robotBinding.rotationPointX = armX;
        this.robotArm.rotationPointZ = armZ;

        if( this.modelJson.isLoaded() ) {
            Arrays.asList(this.modelJson.getMainBoxes()).forEach((box) -> box.render(scale));
        }
    }

    @Override
    public void onReload(IResourceManager iResourceManager, ModelJsonLoader<ModelTurretAssembly, ModelJsonLoader.ModelJson> loader) {
        loader.load();

        this.base = loader.getBox("base");
        this.robotBinding = loader.getBox("robotBinding");
        this.robotArm = loader.getBox("robotArm");
    }

    @Override
    public void setTexture(String s) { }
}
