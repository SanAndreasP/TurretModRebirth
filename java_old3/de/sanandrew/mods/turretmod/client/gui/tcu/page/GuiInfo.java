/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.client.gui.tcu.page;

import de.sanandrew.mods.sanlib.lib.client.gui.GuiDefinition;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.client.gui.element.Button;
import de.sanandrew.mods.sanlib.lib.client.gui.element.TextField;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTCU;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTcuInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.client.gui.element.tcu.info.ErrorTooltip;
import de.sanandrew.mods.turretmod.network.PacketPlayerTurretAction;
import de.sanandrew.mods.turretmod.network.PacketRegistry;
import de.sanandrew.mods.turretmod.network.PacketTurretNaming;
import de.sanandrew.mods.turretmod.registry.Resources;
import net.minecraft.util.ResourceLocation;

public class GuiInfo
        implements IGuiTCU
{
    private static final int ACTION_DISMANTLE  = 0;
    private static final int ACTION_ACTIVATE   = 1;
    private static final int ACTION_DEACTIVATE = 2;
    private static final int ACTION_RANGE_SHOW = 3;
    private static final int ACTION_RANGE_HIDE = 4;

    private GuiElementInst dismantle;
    private GuiElementInst setActive;
    private GuiElementInst setDeactive;
    private GuiElementInst showRange;
    private GuiElementInst hideRange;

    private TextField turretName;

    private ErrorTooltip errorDismantle;

    @Override
    public void initialize(IGuiTcuInst<?> gui, GuiDefinition guiDefinition) {
        this.dismantle = guiDefinition.getElementById("dismantle");
        this.setActive = guiDefinition.getElementById("activate");
        this.setDeactive = guiDefinition.getElementById("deactivate");
        this.showRange = guiDefinition.getElementById("showRange");
        this.hideRange = guiDefinition.getElementById("hideRange");

        this.turretName = guiDefinition.getElementById("turretNameInput").get(TextField.class);

        this.errorDismantle = guiDefinition.getElementById("errorDismantle").get(ErrorTooltip.class);

        this.setActive.setVisible(false);
        this.hideRange.setVisible(false);

        this.turretName.setMaxStringLength(128);
        this.turretName.setText(gui.getTurretInst().get().hasCustomName() ? gui.getTurretInst().get().getCustomNameTag() : "");

        if( !gui.hasPermision() ) {
            this.dismantle.get(Button.class).setEnabled(false);
            this.setActive.get(Button.class).setEnabled(false);
            this.setDeactive.get(Button.class).setEnabled(false);
            this.showRange.get(Button.class).setEnabled(false);
            this.hideRange.get(Button.class).setEnabled(false);
            this.turretName.setEnabled(false);
        } else {
            if( gui.isRemote() && !gui.canRemoteTransfer() ) {
                this.dismantle.get(Button.class).setEnabled(false);
            }
        }
    }

    @Override
    public void updateScreen(IGuiTcuInst<?> gui) {
        ITurretInst turretInst = gui.getTurretInst();
        this.setDeactive.setVisible(turretInst.isActive());
        this.setActive.setVisible(!this.setDeactive.isVisible());
        this.hideRange.setVisible(turretInst.showRange());
        this.showRange.setVisible(!this.hideRange.isVisible());

        if( gui.isRemote() && !gui.canRemoteTransfer() ) { // prevent external upgrade removal from this staying enabled
            this.dismantle.get(Button.class).setEnabled(false);
        }
    }

    @Override
    public ResourceLocation getGuiDefinition() {
        return Resources.GUI_STRUCT_TCU_INFO.resource;
    }

    @Override
    public boolean onElementAction(IGuiTcuInst<?> gui, IGuiElement element, int action) {
        ITurretInst turretInst = gui.getTurretInst();
        switch( action ) {
            case ACTION_DISMANTLE:
                if( !PacketPlayerTurretAction.tryDismantle(gui.getGui().mc.player, turretInst) ) {
                    this.errorDismantle.activate();
                } else {
                    gui.getGui().mc.player.closeScreen();
                }
                return true;
            case ACTION_RANGE_SHOW:
                turretInst.setShowRange(true);
                turretInst.get().ignoreFrustumCheck = true;
                return true;
            case ACTION_RANGE_HIDE:
                turretInst.setShowRange(false);
                turretInst.get().ignoreFrustumCheck = false;
                return true;
            case ACTION_ACTIVATE:
                PacketRegistry.sendToServer(new PacketPlayerTurretAction(turretInst, PacketPlayerTurretAction.SET_ACTIVE));
                return true;
            case ACTION_DEACTIVATE:
                PacketRegistry.sendToServer(new PacketPlayerTurretAction(turretInst, PacketPlayerTurretAction.SET_DEACTIVE));
                return true;
        }

        return false;
    }

    @Override
    public void guiClosed(IGuiTcuInst<?> gui) {
        PacketRegistry.sendToServer(new PacketTurretNaming(gui.getTurretInst(), this.turretName.getText()));
    }
}
