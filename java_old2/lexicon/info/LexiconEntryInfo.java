/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.lexicon.info;

import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconEntry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

final class LexiconEntryInfo
        implements ILexiconEntry
{
    private static final String ID = "info";
    private final ItemStack icon;

    LexiconEntryInfo() {
        this.icon = new ItemStack(Blocks.BARRIER, 1);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getGroupId() {
        return LexiconGroupInfo.NAME;
    }

    @Override
    public String getPageRenderId() {
        return LexiconRenderInfo.ID;
    }

    @Nonnull
    @Override
    public ItemStack getEntryIcon() {
        return this.icon;
    }

    @Nonnull
    @Override
    public String getSrcTitle() {
        return "";
    }

    @Nonnull
    @Override
    public String getSrcText() {
        return "";
    }
}
