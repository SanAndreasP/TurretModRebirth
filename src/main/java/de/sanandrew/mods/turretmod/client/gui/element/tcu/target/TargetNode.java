package de.sanandrew.mods.turretmod.client.gui.element.tcu.target;

import com.google.gson.JsonObject;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiDefinition;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import de.sanandrew.mods.turretmod.api.client.tcu.IGuiTcuInst;
import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import de.sanandrew.mods.turretmod.client.gui.element.tcu.shieldcolor.CheckBox;
import de.sanandrew.mods.turretmod.client.gui.tcu.TargetType;
import org.apache.commons.lang3.Range;

import java.io.IOException;

public class TargetNode<T>
        implements IGuiElement
{
    private int[] margins;

    private GuiElementInst checkbox;

    private final T             targetId;
    private final TargetType<T> targetType;
    private final int           width;

    private String name;

    public TargetNode(T id, TargetType<T> type, int width) {
        this.targetId = id;
        this.targetType = type;
        this.width = width;
    }

    @Override
    public void bakeData(IGui gui, JsonObject data) {
        GuiDefinition guiDef = gui.getDefinition();
        ITurretInst turretInst = ((IGuiTcuInst<?>) gui).getTurretInst();
        TargetType.EntityType type = this.targetType.getType(turretInst, this.targetId);
        JsonObject ckbData = MiscUtils.defIfNull(data.get("checkbox"), JsonObject::new).getAsJsonObject();

        this.margins = JsonUtils.getIntArray(data.get("margins"), new int[] {2, 2, 0, 2}, Range.is(4));
        this.name = this.targetType.getName(turretInst, this.targetId);

        CheckBox ckb = new CheckBox();
        this.checkbox = new GuiElementInst();
        this.checkbox.element = ckb;
        this.checkbox.data = ckbData;
        guiDef.initElement(this.checkbox);

        JsonObject lblData = MiscUtils.defIfNull(ckbData.getAsJsonObject("label"), JsonObject::new);
        JsonUtils.addDefaultJsonProperty(lblData, "text", this.name);
        JsonUtils.addDefaultJsonProperty(lblData, "color", getTextColor(lblData, type, ""));
        JsonUtils.addJsonProperty(lblData, "colorHover", getTextColor(lblData, type, "Hover"));
        JsonUtils.addJsonProperty(lblData, "colorDisabled", getTextColor(lblData, type, "Disabled"));

        JsonUtils.addDefaultJsonProperty(this.checkbox.data, "size", new int[] { 8, 8 });
        JsonUtils.addDefaultJsonProperty(this.checkbox.data, "uv", new int[] { 176, 12 });
        JsonUtils.addJsonProperty(this.checkbox.data, "fixedWidth", this.width);
        this.checkbox.data.add("label", lblData);

        ckb.bakeData(gui, ckbData);
        ckb.setOnCheckedChanged(byUser -> {
            if( byUser ) {
                this.targetType.updateTarget(turretInst, this.targetId, ckb.isChecked());
            }
        });
    }

    private static String getTextColor(JsonObject textData, TargetType.EntityType type, String suffix) {
        if( suffix.equalsIgnoreCase("disabled") ) {
            return JsonUtils.getStringVal(textData.get("colorDisabled"), "0xFF404040");
        }

        switch( type ) {
            case HOSTILE:
                return JsonUtils.getStringVal(textData.get("colorHostile" + suffix), suffix.equalsIgnoreCase("hover") ? "0xFFC00000" : "0xFF800000");
            case PEACEFUL:
                return JsonUtils.getStringVal(textData.get("colorPeaceful" + suffix), suffix.equalsIgnoreCase("hover") ? "0xFF00C000" : "0xFF008000");
            default:
                return JsonUtils.getStringVal(textData.get("color" + suffix), suffix.equalsIgnoreCase("hover") ? "0xFF0040A0" : "0xFF000000");
        }
    }

    @Override
    public void update(IGui gui, JsonObject data) {
        CheckBox ckb = this.checkbox.get(CheckBox.class);
        ckb.update(gui, this.checkbox.data);
        ckb.setChecked(this.targetType.isTargeted(((IGuiTcuInst<?>) gui).getTurretInst(), this.targetId), false);
    }

    @Override
    public void render(IGui gui, float partTicks, int x, int y, int mouseX, int mouseY, JsonObject data) {
        this.checkbox.get().render(gui, partTicks, x + this.margins[0], y + this.margins[3], mouseX, mouseY, data);
    }

    @Override
    public void handleMouseInput(IGui gui) throws IOException {
        this.checkbox.get().handleMouseInput(gui);
    }

    @Override
    public boolean mouseClicked(IGui gui, int mouseX, int mouseY, int mouseButton) throws IOException {
        return this.checkbox.get().mouseClicked(gui, mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(IGui gui, int mouseX, int mouseY, int state) {
        this.checkbox.get().mouseReleased(gui, mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(IGui gui, int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.checkbox.get().mouseClickMove(gui, mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void guiClosed(IGui gui) {
        this.checkbox.get().guiClosed(gui);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.checkbox.get().getHeight() + this.margins[0] + this.margins[2];
    }

    @Override
    public boolean keyTyped(IGui gui, char typedChar, int keyCode) throws IOException {
        return this.checkbox.get().keyTyped(gui, typedChar, keyCode);
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void setVisible(boolean visible) { }

    public String getName() {
        return this.name;
    }
}
