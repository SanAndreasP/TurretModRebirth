package de.sanandrew.mods.turretmod.registry.turret.variant;

import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.api.turret.IVariant;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VariantContainer
{
    private IVariant                        defaultVariant;
    final   Map<ResourceLocation, IVariant> variants = new HashMap<>();

    public void register(IVariant variant) {
        this.variants.put(variant.getId(), variant);
        if( this.defaultVariant == null ) {
            this.defaultVariant = variant;
        }
    }

    public IVariant get(ResourceLocation id) {
        return this.variants.get(id);
    }

    public IVariant getOrDefault(ResourceLocation id) {
        return MiscUtils.defIfNull(this.variants.get(id), this.defaultVariant);
    }

    public boolean isDefaultVariant(IVariant variant) {
        return variant == null || this.defaultVariant.getId().equals(variant.getId());
    }

    public static class Variant
            implements IVariant
    {
        private final ResourceLocation id;
        private final ResourceLocation texture;

        Variant(ResourceLocation id, ResourceLocation texture) {
            this.id = id;
            this.texture = texture;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public ResourceLocation getTexture() {
            return this.texture;
        }

        @Override
        public String getTranslatedName() {
            return LangUtils.translate(String.format("turret_variant.%s", this.id.toString()));
        }
    }

    public static abstract class ItemVariants<T>
            extends VariantContainer
    {
        public final T variantMap = buildVariantMap();

        public abstract T buildVariantMap();

        public abstract IVariant get(IInventory inv);

        protected long getIdFromStack(ItemStack stack) {
            return ((long) (stack.getMetadata() & Integer.MAX_VALUE) << 32) | Objects.hashCode(stack.getItem());
        }

        public long checkType(long currType, long newType) {
            if( currType >= 0L && newType >= 0L && currType != newType ) {
                return -1L;
            }

            return newType >= 0L ? newType : currType;
        }
    }
}
