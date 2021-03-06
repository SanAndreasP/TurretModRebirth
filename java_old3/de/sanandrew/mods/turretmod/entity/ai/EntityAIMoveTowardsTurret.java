/*
 * ****************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 * http://creativecommons.org/licenses/by-nc-sa/4.0/
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.entity.ai;

import de.sanandrew.mods.sanlib.lib.util.EntityUtils;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.Vec3d;

public final class EntityAIMoveTowardsTurret
        extends EntityAIBase
{
    private final EntityCreature theEntity;
    private final double speed;
    private final float maxDistance;

    private ITurretInst targetTurret;
    private Path turretPath;

    public EntityAIMoveTowardsTurret(EntityCreature doer, ITurretInst target, double speed, float maxDistance) {
        this.theEntity = doer;
        this.speed = speed;
        this.maxDistance = maxDistance;
        this.targetTurret = target;
        this.setMutexBits(1);
    }

    public void setNewTurret(ITurretInst turret) {
        this.targetTurret = turret;
    }

    @Override
    public boolean shouldExecute() {
        if( this.targetTurret == null || !this.targetTurret.get().isEntityAlive() ) {
            return false;
        } else if( this.targetTurret.get().getDistanceSq(this.theEntity) > this.maxDistance * this.maxDistance ) {
            return false;
        } else {
            EntityLivingBase turretL = this.targetTurret.get();
            Vec3d targetPosVec = new Vec3d(turretL.posX, turretL.posY, turretL.posZ);
            Vec3d pathBlockVec = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 8, 7, targetPosVec);

            if( pathBlockVec == null ) {
                return false;
            } else {
                this.turretPath = this.theEntity.getNavigator().getPathToXYZ(pathBlockVec.x, pathBlockVec.y, pathBlockVec.z);

                return true;
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if( this.targetTurret != null ) {
            EntityLivingBase turretL = this.targetTurret.get();
            return !this.theEntity.getNavigator().noPath() && turretL.isEntityAlive() && turretL.getDistanceSq(this.theEntity) < this.maxDistance * this.maxDistance;
        }

        return false;
    }

    @Override
    public void resetTask() {
        this.targetTurret = null;
    }

    @Override
    public void startExecuting() {
        EntityUtils.tryApplyModifier(this.theEntity, SharedMonsterAttributes.FOLLOW_RANGE, new AttributeModifier("turretRangeMod", this.targetTurret.getTargetProcessor().getRangeVal(), 0));
        this.theEntity.getNavigator().setPath(this.turretPath, this.speed);
    }
}
