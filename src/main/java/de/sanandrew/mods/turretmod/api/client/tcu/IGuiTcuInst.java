/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.turretmod.api.client.tcu;

import de.sanandrew.mods.turretmod.api.turret.ITurretInst;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

/**
 * A Turret Control Unit GUI instance.
 * @param <T> The type of GUI
 */
@SuppressWarnings("JavadocReference")
public interface IGuiTcuInst<T extends GuiScreen>
{
    /**
     * @return the {@link GuiScreen} instance of this GUI.
     */
    T getGui();

    /**
     * @return the turret instance associated with this GUI.
     */
    ITurretInst getTurretInst();

    /**
     * @return the X position of this GUI.
     */
    int getPosX();

    /**
     * @return the Y position of this GUI.
     */
    int getPosY();

    /**
     * @return the width of this GUI.
     */
    int getWidth();

    /**
     * @return the height of this GUI.
     */
    int getHeight();

    /**
     * <p>Returns wether the player opening this GUI has permission to do so.</p>
     *
     * @return <tt>true</tt>, if the player has the appropriate permission; <tt>false</tt> otherwise.
     */
    boolean hasPermision();

    /**
     * <p>Adds a new {@link GuiButton} instance to this GUI.</p>
     *
     * @param button The button to be added.
     * @param <U> The type of the button.
     * @return The button added to this GUI; This is the same as the parameter.
     */
    <U extends GuiButton> U addNewButton(U button);

    /**
     * <p>Fetches a new button ID to be used for {@link IGuiTcuInst#addNewButton(GuiButton)}.</p>
     *
     * @return a new button ID.
     */
    int getNewButtonId();

    /**
     * @return the font renderer associated with this GUI.
     */
    FontRenderer getFontRenderer();

    /**
     * <p>Draws a rectangle with a vertical gradient between the specified colors (ARGB format)</p>
     *
     * @param left The horizontal position of the left edge.
     * @param top The vertical position of the top edge.
     * @param right The horizontal position of the right edge.
     * @param bottom The vertical position of the bottom edge.
     * @param startColor The color at the beginning of the gradient.
     * @param endColor The color at the end of the gradient.
     * @see net.minecraft.client.gui.Gui#drawGradientRect(int, int, int, int, int, int)
     */
    void drawGradient(int left, int top, int right, int bottom, int startColor, int endColor);

    /**
     * @return the key of the current TCU GUI page.
     */
    ResourceLocation getCurrentEntryKey();
}
