/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.lexicon.misc;

import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconGroup;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconInst;
import de.sanandrew.mods.sanlib.api.client.lexicon.LexiconGroup;
import de.sanandrew.mods.turretmod.block.BlockRegistry;
import de.sanandrew.mods.turretmod.registry.Resources;
import net.minecraft.item.ItemStack;

public final class LexiconGroupMisc
        extends LexiconGroup
{
    static final String NAME = "misc";

    private LexiconGroupMisc() {
        super(NAME, Resources.TINFO_GRP_MISC.resource);
    }

    public static void register(ILexiconInst registry) {
        registry.registerPageRender(new LexiconRenderCraftable());
        registry.registerPageRender(new LexiconRenderTCU());

        ILexiconGroup grp = new LexiconGroupMisc();
        registry.registerGroup(grp);

        grp.addEntry(new LexiconEntryCraftable("assembly", new ItemStack(BlockRegistry.TURRET_ASSEMBLY)));
        grp.addEntry(new LexiconEntryCraftable("generator", new ItemStack(BlockRegistry.ELECTROLYTE_GENERATOR)));
        grp.addEntry(new LexiconEntryTCU());
    }
}
