/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.model;

import de.sanandrew.mods.sanlib.lib.client.ModelJsonLoader;
import de.sanandrew.mods.turretmod.entity.turret.EntityTurretMinigun;
import de.sanandrew.mods.turretmod.util.Resources;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelTurretMinigun
		extends ModelTurretBase
{
	public ModelRenderer barrelBaseLeft;
	public ModelRenderer barrelBaseRight;

	public ModelTurretMinigun(float scale) {
		super(scale);
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partTicks) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partTicks);

		if( entity instanceof EntityTurretMinigun ) {
			EntityTurretMinigun shotgun = (EntityTurretMinigun) entity;
			float barrelDeltaL = shotgun.prevBarrelLeft + (shotgun.barrelLeft - shotgun.prevBarrelLeft) * partTicks;
			float barrelDeltaR = shotgun.prevBarrelRight + (shotgun.barrelRight - shotgun.prevBarrelRight) * partTicks;
			this.barrelBaseLeft.rotateAngleZ = barrelDeltaL / 180.0F * (float) Math.PI;
			this.barrelBaseRight.rotateAngleZ = barrelDeltaR / 180.0F * (float) Math.PI;
		}
	}

	@Override
	public List<String> getMandatoryBoxes() {
		return Stream.concat(super.getMandatoryBoxes().stream(), Stream.of("barrelBaseLeft", "barrelBaseRight")).collect(Collectors.toList());
	}

	@Override
	public void onReload(IResourceManager iResourceManager, ModelJsonLoader<ModelTurretBase, ModelJsonLoader.ModelJson> loader) {
		super.onReload(iResourceManager, loader);

		this.barrelBaseLeft = loader.getBox("barrelBaseLeft");
		this.barrelBaseRight = loader.getBox("barrelBaseRight");
	}

	@Override
	public ResourceLocation getModelLocation() {
		return Resources.TURRET_T2_MINIGUN_MODEL.getResource();
	}
}