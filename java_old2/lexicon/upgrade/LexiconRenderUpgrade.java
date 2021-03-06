/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.lexicon.upgrade;

import de.sanandrew.mods.sanlib.api.client.lexicon.IGuiButtonEntry;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconEntry;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconGuiHelper;
import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.api.assembly.IAssemblyRecipe;
import de.sanandrew.mods.turretmod.api.turret.ITurret;
import de.sanandrew.mods.turretmod.api.upgrade.IUpgrade;
import de.sanandrew.mods.turretmod.client.gui.lexicon.assembly.LexiconRenderAssemblyRecipe;
import de.sanandrew.mods.turretmod.client.gui.lexicon.turret.LexiconGroupTurret;
import de.sanandrew.mods.turretmod.client.init.ClientProxy;
import de.sanandrew.mods.turretmod.registry.assembly.AssemblyManager;
import de.sanandrew.mods.turretmod.registry.upgrades.UpgradeRegistry;
import de.sanandrew.mods.turretmod.registry.Lang;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SideOnly(Side.CLIENT)
class LexiconRenderUpgrade
        extends LexiconRenderAssemblyRecipe
{
    static final String ID = TmrConstants.ID + ":upgrade";

    private static final int H2_COLOR = 0xFF202080;

    private IAssemblyRecipe recipe;
    private ItemStack upgradeStack;
    private int drawHeight;

    private List<IGuiButtonEntry> turretButtons;
    private IGuiButtonEntry prereqUpgradeButton;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void initPage(ILexiconEntry entry, ILexiconGuiHelper helper, List<GuiButton> deprecated1, List<GuiButton> deprecated2) {
        if( entry instanceof LexiconEntryUpgrade ) {
            IUpgrade upgrade = ((LexiconEntryUpgrade) entry).upgrade;
            this.upgradeStack = UpgradeRegistry.INSTANCE.getItem(upgrade.getId());
            this.recipe = AssemblyManager.INSTANCE.findRecipe(this.upgradeStack);
            this.turretButtons = new ArrayList<>();

            List<GuiButton> entryButtons = helper.getEntryButtonList();
            Stream.of(MiscUtils.defIfNull(upgrade.getApplicableTurrets(), () -> new ITurret[0])).forEach(t -> {
                IGuiButtonEntry entryBtn = helper.getNewEntryButton(entryButtons.size(), 4, 0, ClientProxy.lexiconInstance.getGroup(LexiconGroupTurret.NAME).getEntry(t.getId().toString()),
                                                                    helper.getFontRenderer());
                this.turretButtons.add(entryBtn);
                entryButtons.add(entryBtn.get());
            });

            IUpgrade prereq = upgrade.getDependantOn();
            if( prereq != null ) {
                this.prereqUpgradeButton = helper.getNewEntryButton(entryButtons.size(), 4, 0, ClientProxy.lexiconInstance.getGroup(LexiconGroupUpgrade.NAME).getEntry(prereq.getId().toString()),
                                                                    helper.getFontRenderer());
                entryButtons.add(this.prereqUpgradeButton.get());
            } else {
                this.prereqUpgradeButton = null;
            }
        }
    }

    @Override
    public void renderPageEntry(ILexiconEntry entry, ILexiconGuiHelper helper, int mouseX, int mouseY, int scrollY, float partTicks) {
        FontRenderer fr = helper.getFontRenderer();

        helper.drawTitleCenter(2, entry);
        this.drawHeight = 14;

        helper.drawItem(this.upgradeStack, (helper.getLexicon().getEntryWidth() - 36) / 2, this.drawHeight, 2.0D);
        this.drawHeight += 40;

        if( this.recipe != null ) {
            fr.drawString(LangUtils.translate(Lang.LEXICON_ASSEMBLY_RECIPE), 2, this.drawHeight, H2_COLOR);
            this.drawHeight += 9;
            this.drawHeight += this.renderRecipe(helper, this.recipe, 4, this.drawHeight, mouseX, mouseY, scrollY) + 3;
        }

        if( this.turretButtons.size() > 0 ) {
            fr.drawString(LangUtils.translate(Lang.LEXICON_UPGRADE_ITEM.get("turrets")), 2, this.drawHeight, H2_COLOR);
            this.drawHeight += 9;
            for( IGuiButtonEntry turretButton : this.turretButtons ) {
                turretButton.get().y = this.drawHeight;
                this.drawHeight += 14;
            }
            this.drawHeight += 2;
        }

        if( this.prereqUpgradeButton != null ) {
            fr.drawString(LangUtils.translate(Lang.LEXICON_UPGRADE_ITEM.get("prereq")), 2, this.drawHeight, H2_COLOR);
            this.drawHeight += 9;
            this.prereqUpgradeButton.get().y = this.drawHeight;
            this.drawHeight += 16;
        }

        fr.drawString(LangUtils.translate(Lang.LEXICON_DESCRIPTION), 2, this.drawHeight, H2_COLOR);
        this.drawHeight += 9;
        this.drawHeight += helper.drawContentString(4, this.drawHeight, entry, helper.getEntryButtonList());
    }

    @Override
    public int getEntryHeight(ILexiconEntry entry, ILexiconGuiHelper helper) {
        return this.drawHeight;
    }
}
