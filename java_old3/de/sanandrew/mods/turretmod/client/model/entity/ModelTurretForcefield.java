package de.sanandrew.mods.turretmod.client.model.entity;

import de.sanandrew.mods.sanlib.lib.client.ModelJsonLoader;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.registry.Resources;
import de.sanandrew.mods.turretmod.registry.turret.forcefield.Forcefield;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.IResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ModelTurretForcefield<E extends LivingEntity & ITurretInst>
        extends ModelTurretBase<E>
{
    private ModelRenderer shieldBar;

    public ModelTurretForcefield() {
        super(Resources.TURRET_T2_FORCEFIELD_MODEL.resource);
    }

    @Override
    protected List<String> getMandatoryBoxes() {
        List<String> ret = new ArrayList<>(super.getMandatoryBoxes());
        ret.add("shieldBar");

        return ret;
    }

    @Override
    public void onReload(IResourceManager iResourceManager, ModelJsonLoader<ModelTurretBase<E>, ModelJsonLoader.JsonBase> loader) {
        super.onReload(iResourceManager, loader);

        this.shieldBar = loader.getBox("shieldBar");
    }

    @Override
    public void setRotationAngles(E turretInst, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(turretInst, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        Forcefield forcefieldTurret = turretInst.getRAM(null);

        if( forcefieldTurret != null ) {
            float maxShield = turretInst.isInGui() ? 2.0F : forcefieldTurret.getMaxValue();
            float shield    = turretInst.isInGui() ? 1.0F : forcefieldTurret.getValue();

            this.shieldBar.rotateAngleX = ((float) Math.PI / 2.0F) * (Math.max(0.0F, maxShield - shield) / maxShield);
        } else {
            this.shieldBar.rotateAngleX = 0.0F;
        }
    }
}
