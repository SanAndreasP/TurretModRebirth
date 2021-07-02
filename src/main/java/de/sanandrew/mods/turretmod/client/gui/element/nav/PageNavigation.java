package de.sanandrew.mods.turretmod.client.gui.element.nav;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiDefinition;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.client.gui.element.ButtonSL;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import de.sanandrew.mods.turretmod.api.Resources;
import de.sanandrew.mods.turretmod.api.TmrConstants;
import de.sanandrew.mods.turretmod.client.gui.tcu.TcuScreen;
import de.sanandrew.mods.turretmod.item.TurretControlUnit;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PageNavigation
        implements IGuiElement
{
    public static final ResourceLocation ID = new ResourceLocation(TmrConstants.ID, "tcu_page_nav");

    private final Map<GuiElementInst, ResourceLocation> pages = new TreeMap<>(new ComparatorTabButton());

    private GuiElementInst tabScrollL;
    private GuiElementInst tabScrollR;
    private int            maxTabsShown;
    private int            tabStartIdx = 0;

    Map<GuiElementInst, ResourceLocation> shownTabs = Collections.emptyMap();

    @Override
    public void bakeData(IGui gui, JsonObject data, GuiElementInst inst) {
        this.maxTabsShown = JsonUtils.getIntVal(data.get("tabsShown"), 7);

        int currIdx = 0;
        for( ResourceLocation pageKey : TurretControlUnit.PAGES ) {
            GuiElementInst btn = new GuiElementInst(new ButtonNav(pageKey, currIdx++), data.getAsJsonObject("buttonData")).initialize(gui);
            btn.get().bakeData(gui, btn.data, btn);

            this.pages.put(btn, pageKey);
        }

        this.tabScrollL = new GuiElementInst(new ButtonTabScroll(0), data.getAsJsonObject("tabScrollLeft")).initialize(gui);
        this.tabScrollL.get().bakeData(gui, this.tabScrollL.data, this.tabScrollL);
        this.tabScrollL.setVisible(false);

        this.tabScrollR = new GuiElementInst(new ButtonTabScroll(1), data.getAsJsonObject("tabScrollRight")).initialize(gui);
        this.tabScrollR.get().bakeData(gui, this.tabScrollR.data, this.tabScrollR);
        this.tabScrollR.setVisible(false);
    }

    @Override
    public void tick(IGui gui, JsonObject data) {
        final TcuScreen     tcu      = (TcuScreen) gui;
        final ResourceLocation currPage = tcu.getCurrPage();

        final int tabScrollElemLWidth = this.tabScrollL.get().getWidth();
        final int tabScrollElemRWidth = this.tabScrollR.get().getWidth();

        int cntAvailableTabs = 0;
        for( Map.Entry<GuiElementInst, ResourceLocation> entry : this.pages.entrySet() ) {
            GuiElementInst btn    = entry.getKey();
            ButtonNav      btnNav = btn.get(ButtonNav.class);
//            if( TurretControlUnit.PAGES.(btnNav.pageKey).showTab(tcu) ) {
                cntAvailableTabs++;

                if( btnNav.order >= this.tabStartIdx && btnNav.order <= this.tabStartIdx + this.maxTabsShown ) {
                    btn.setVisible(true);
                    btnNav.setActive(!currPage.equals(btnNav.pageKey));

                    continue;
                }
//            }

            btn.setVisible(false);
        }
        this.shownTabs = this.fetchShownPageButtons();

        int tabWidth = this.shownTabs.keySet().stream().map(elem -> elem.get().getWidth()).reduce((e1, e2) -> e1 + e2 + 2).orElse(2);
        int tabLeft  = (tcu.getDefinition().width - tabWidth - tabScrollElemLWidth - tabScrollElemRWidth - 4) / 2;

        this.tabScrollL.pos[0] = tabLeft;
        this.tabScrollL.setVisible(this.tabStartIdx > 0);
        this.tabScrollR.pos[0] = tabLeft + tabScrollElemLWidth + tabWidth + 4;
        this.tabScrollR.setVisible(this.tabStartIdx < cntAvailableTabs - shownTabs.size());

        int shownId = 0;
        for( Map.Entry<GuiElementInst, ResourceLocation> tab : this.shownTabs.entrySet() ) {
            tab.getKey().pos[0] = tabLeft + tabScrollElemLWidth + 2 + shownId++ * 18;
        }
    }

    private Map<GuiElementInst, ResourceLocation> fetchShownPageButtons() {
        return this.pages.entrySet().stream()
                         .filter(e -> e.getKey().isVisible())
                         .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (u, v) -> u, () -> new TreeMap<>(new ComparatorTabButton())));
    }

    @Override
    public void render(IGui gui, MatrixStack mStack, float partTicks, int x, int y, double mouseX, double mouseY, JsonObject data) {
        GuiDefinition.renderElement(gui, mStack, x + this.tabScrollL.pos[0], y, mouseX, mouseY, partTicks, this.tabScrollL);
        GuiDefinition.renderElement(gui, mStack, x + this.tabScrollR.pos[0], y, mouseX, mouseY, partTicks, this.tabScrollR);
        this.shownTabs.forEach((btn, page) -> GuiDefinition.renderElement(gui, mStack, x + btn.pos[0], y, mouseX, mouseY, partTicks, btn));
    }

    @Override
    public boolean mouseClicked(IGui gui, double mouseX, double mouseY, int mouseButton) {
        for( Map.Entry<GuiElementInst, ResourceLocation> e : this.shownTabs.entrySet() ) {
            if( e.getKey().get().mouseClicked(gui, mouseX, mouseY, mouseButton) ) {
                return true;
            }
        }

        if( this.tabScrollL.get(ButtonTabScroll.class).mouseClicked(gui, mouseX, mouseY, mouseButton) ) {
            return true;
        }

        return this.tabScrollR.get(ButtonTabScroll.class).mouseClicked(gui, mouseX, mouseY, mouseButton);
    }

    @Override
    public int getWidth() {
        return 18 * Math.min(this.maxTabsShown, this.pages.size()) - 2;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    private static final class ComparatorTabButton
            implements Comparator<GuiElementInst>
    {
        @Override
        public int compare(GuiElementInst o1, GuiElementInst o2) {
            return Integer.compare(o1.get(ButtonNav.class).order, o2.get(ButtonNav.class).order);
        }
    }

    private final class ButtonTabScroll
            extends ButtonSL
    {
        private final int direction;

        private ButtonTabScroll(int direction) {
            this.direction = direction;
        }

        @Override
        public void bakeData(IGui gui, JsonObject data, GuiElementInst inst) {
            JsonUtils.addDefaultJsonProperty(data, "size", new int[] { 16, 16 });
            JsonUtils.addDefaultJsonProperty(data, "uvSize", new int[] { 16, 16 });
            JsonUtils.addDefaultJsonProperty(data, "texture", Resources.TEXTURE_GUI_TCU_BUTTONS.toString());
            JsonUtils.addDefaultJsonProperty(data, "buttonFunction", -1);

            super.bakeData(gui, data, inst);

            this.setFunction(btn -> {
                if( PageNavigation.this.tabStartIdx > 0 && this.direction == 0 ) {
                    PageNavigation.this.tabStartIdx--;
                } else if( PageNavigation.this.tabStartIdx < PageNavigation.this.pages.size() - PageNavigation.this.maxTabsShown && this.direction == 1 ) {
                    PageNavigation.this.tabStartIdx++;
                }
            });
        }
    }
}
