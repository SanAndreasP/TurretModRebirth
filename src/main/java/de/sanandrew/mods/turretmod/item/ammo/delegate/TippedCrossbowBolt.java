package de.sanandrew.mods.turretmod.item.ammo.delegate;

import com.google.common.base.Strings;
import de.sanandrew.mods.turretmod.item.ammo.AmmunitionRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TippedCrossbowBolt
        extends CrossbowBolt
{
    private static final String[] SUBTYPES;
    static {
        final List<String> potionTypes = new ArrayList<>();
        ForgeRegistries.POTION_TYPES.forEach(p -> {
            ResourceLocation id = p.getRegistryName();
            if( id != null && !p.getEffects().isEmpty() ) {
                potionTypes.add(id.toString());
            }
        });
        SUBTYPES = potionTypes.toArray(new String[0]);
    }

    public TippedCrossbowBolt(ResourceLocation id) {
        super(id);
    }

    @Override
    public String[] getSubtypes() {
        return SUBTYPES;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        String potionTypeId = AmmunitionRegistry.INSTANCE.getSubtype(stack);
        if( !Strings.isNullOrEmpty(potionTypeId) ) {
            Potion    potion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation(potionTypeId));
            if( potion != null ) {
                ItemStack dummyStack = stack.copy();
                PotionUtils.setPotion(dummyStack, potion);
                PotionUtils.addPotionTooltip(dummyStack, tooltip, 0.125F);
            }
        }
    }
}
