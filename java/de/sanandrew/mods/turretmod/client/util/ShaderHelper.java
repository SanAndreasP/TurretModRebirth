/**
 * ****************************************************************************************************************
 * Authors:   Vazkii, modified by SanAndreasP
 * Copyright: Vazkii, SanAndreasP
 * License:   Botania License
 * http://botaniamod.net/license.php
 * *****************************************************************************************************************
 */
package de.sanandrew.mods.turretmod.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.sanandrew.mods.turretmod.client.event.ClientTickHandler;
import de.sanandrew.mods.turretmod.client.shader.ShaderCallback;
import de.sanandrew.mods.turretmod.util.Resources;
import de.sanandrew.mods.turretmod.util.TmrConfiguration;
import de.sanandrew.mods.turretmod.util.TurretModRebirth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.GL11;

public final class ShaderHelper
{
    private static final int VERT = ARBVertexShader.GL_VERTEX_SHADER_ARB;
    private static final int FRAG = ARBFragmentShader.GL_FRAGMENT_SHADER_ARB;

    public static int categoryButton = 0;
    public static int grayscaleItem = 0;
    public static int alphaOverride = 0;

    public static void initShaders() {
        if( !areShadersEnabled() ) {
            return;
        }

        categoryButton = createProgram(null, Resources.SHADER_CATEGORY_BUTTON_FRAG.getResource());
        grayscaleItem = createProgram(null, Resources.SHADER_GRAYSCALE_FRAG.getResource());
        alphaOverride = createProgram(null, Resources.SHADER_ALPHA_OVERRIDE_FRAG.getResource());
    }

    public static void useShader(int shader, ShaderCallback callback) {
        if( !areShadersEnabled() ) {
            return;
        }

        ARBShaderObjects.glUseProgramObjectARB(shader);

        if( shader != 0 ) {
            int time = ARBShaderObjects.glGetUniformLocationARB(shader, "time");
            ARBShaderObjects.glUniform1iARB(time, ClientTickHandler.ticksInGame);

            if( callback != null ) {
                callback.call(shader);
            }
        }
    }

    public static void useShader(int shader) {
        useShader(shader, null);
    }

    public static void releaseShader() {
        useShader(0);
    }

    public static boolean areShadersEnabled() {
        return OpenGlHelper.shadersSupported && TmrConfiguration.useShaders;
    }

    // Most of the code taken from the LWJGL wiki
    // http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL

    private static int createProgram(ResourceLocation vert, ResourceLocation frag) {
        int vertId = 0;
        int fragId = 0;
        int program;
        if( vert != null ) {
            vertId = createShader(vert, VERT);
        }
        if( frag != null ) {
            fragId = createShader(frag, FRAG);
        }

        program = ARBShaderObjects.glCreateProgramObjectARB();
        if( program == 0 ) {
            return 0;
        }

        if( vert != null ) {
            ARBShaderObjects.glAttachObjectARB(program, vertId);
        }
        if( frag != null ) {
            ARBShaderObjects.glAttachObjectARB(program, fragId);
        }

        ARBShaderObjects.glLinkProgramARB(program);
        if( ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE ) {
            TurretModRebirth.LOG.log(Level.ERROR, getLogInfo(program));
            return 0;
        }

        ARBShaderObjects.glValidateProgramARB(program);
        if( ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE ) {
            TurretModRebirth.LOG.log(Level.ERROR, getLogInfo(program));
            return 0;
        }

        return program;
    }

    private static int createShader(ResourceLocation file, int shaderType){
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

            if( shader == 0 ) {
                return 0;
            }

            ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(file));
            ARBShaderObjects.glCompileShaderARB(shader);

            if( ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE ) {
                throw new RuntimeException("Error creating shader: " + getLogInfo(shader));
            }

            return shader;
        } catch( IOException | NullPointerException e ) {
            ARBShaderObjects.glDeleteObjectARB(shader);
            TurretModRebirth.LOG.log(Level.ERROR, "Cannot create Shader!", e);
            return -1;
        }
    }

    private static String getLogInfo(int obj) {
        return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private static String readFileAsString(ResourceLocation file) throws IOException {
        StringBuilder source = new StringBuilder();
        try( IResource res = Minecraft.getMinecraft().getResourceManager().getResource(file); InputStream in = res.getInputStream() ) {
            try( BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8")) ) {
                String line;
                while( (line = reader.readLine()) != null ) {
                    source.append(line).append('\n');
                }
            }
        } catch( NullPointerException ex ) {
            return "";
        }

        return source.toString();
    }
}
