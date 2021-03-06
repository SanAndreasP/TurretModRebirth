package de.sanandrew.mods.turretmod.client.compat.patchouli;

import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.registry.turret.TurretRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariableProvider;

import java.text.NumberFormat;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ComponentTurretStatProcessor
        implements IComponentProcessor
{
    ITurret turret;

    @Override
    public void setup(IVariableProvider<String> provider) {
        this.turret = TurretRegistry.INSTANCE.getObject(new ResourceLocation(provider.get("turret_type")));
    }

    @Override
    public String process(String s) {
        String langCode = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();

        switch( s ) {
            case "tier": {
                return MiscUtils.getNumberFormat(0, false, langCode).format(turret.getTier());
            }
            case "health": {
                return MiscUtils.getNumberFormat(1, true, langCode).format(turret.getHealth() / 2F);
            }
            case "ammo": {
                return MiscUtils.getNumberFormat(0, true, langCode).format(turret.getAmmoCapacity());
            }
            case "reload": {
                return MiscUtils.getTimeFromTicks(turret.getReloadTicks());
            }
            default: {
                if( s.contains("range") ) {
                    AxisAlignedBB aabb = this.turret.getRangeBB(null);
                    NumberFormat nf = MiscUtils.getNumberFormat(0, true, langCode);
                    switch( s ) {
                        case "rangeLX": return nf.format(aabb.minX * -1.0D);
                        case "rangeLY": return nf.format(aabb.minY * -1.0D);
                        case "rangeLZ": return nf.format(aabb.minZ * -1.0D);
                        case "rangeHX": return nf.format(aabb.maxX);
                        case "rangeHY": return nf.format(aabb.maxY);
                        case "rangeHZ": return nf.format(aabb.maxZ);
                    }
                }
            }
        }

        return null;
    }
}
