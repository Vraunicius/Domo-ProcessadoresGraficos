package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import br.pucpr.mage.FrameBuffer;
import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Mesh;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Shader;
import br.pucpr.mage.Texture;
import br.pucpr.mage.Window;
import br.pucpr.mage.phong.DirectionalLight;
import br.pucpr.mage.phong.MultiTextureMaterial;
import br.pucpr.mage.phong.SkyMaterial;
import br.pucpr.mage.postfx.PostFXMaterial;

public class MultiTexture implements Scene {
    private static final String PATH = "C:/Users/vieira.vinicius/IdeaProjects/Dome/img/opengl/";
    
    private Keyboard keys = Keyboard.getInstance();
    
    //Dados da cena
    private Camera camera = new Camera();
    private DirectionalLight light;

    //Dados da malha
    private Mesh mesh;
    private MultiTextureMaterial material; 
    
    private Mesh skydome;
    private SkyMaterial skyMaterial;    
        
    private float lookX = 0.0f;
    private float lookY = 0.0f;

    private Mesh canvas;
    private FrameBuffer fb;
    private PostFXMaterial postFX;

    private Vector2f cloud0Offset;
    private Vector2f cloud1Offset;
    private float time = 0;
    
    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glPolygonMode(GL_FRONT_FACE, GL_LINE);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        camera.getPosition().set(0.0f, 20.0f, 145.0f);        
        
        light = new DirectionalLight(
                new Vector3f( 1.0f, -1.0f, -1.0f),    //direction
                new Vector3f( 0.1f,  0.1f,  0.1f), //ambient
                new Vector3f( 1.0f,  1.0f,  1.0f),    //diffuse
                new Vector3f( 1.0f,  1.0f,  1.0f));   //specular

        try {
            mesh = MeshFactory.loadTerrain(new File(PATH + "heights/river.png"), 0.4f, 3);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(1);
        }
        
        
        material = new MultiTextureMaterial(
                new Vector3f(1.0f, 1.0f, 1.0f), //ambient
                new Vector3f(0.9f, 0.9f, 0.9f), //diffuse
                new Vector3f(0.0f, 0.0f, 0.0f), //specular
                0.0f);                          //specular power
        material.setTextures(
        		new Texture(PATH + "textures/sand.png"),
        		new Texture(PATH + "textures/grass.png"),
        		new Texture(PATH + "textures/rock.png"),
        		new Texture(PATH + "textures/snow.png"));
        
        canvas = MeshFactory.createCanvas();
        fb = FrameBuffer.forCurrentViewport();
        postFX = PostFXMaterial.defaultPostFX("fxNone", fb);
        
        //skydome = MeshFactory.createSphere(20, 20);
        skydome = MeshFactory.createDome(100,100, 1);
        skyMaterial = new SkyMaterial();
        skyMaterial.setCloud0(new Texture(PATH + "textures/cloud1.png"));
        skyMaterial.setCloud1(new Texture(PATH + "textures/cloud2.png"));

        cloud0Offset = new Vector2f(0.01f, 0.005f);
        cloud1Offset = new Vector2f(-0.02f, 0.005f);
    }

    @Override
    public void update(float secs) {
        float SPEED = 1000 * secs;
        
        if (keys.isPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(glfwGetCurrentContext(), GLFW_TRUE);
            return;
        }
        
        if (keys.isDown(GLFW_KEY_LEFT_SHIFT)) {
            SPEED *= 10;
        }
        
        if (keys.isDown(GLFW_KEY_LEFT)) {
            lookX -= SPEED * secs;
        } else if (keys.isDown(GLFW_KEY_RIGHT)) {
            lookX += SPEED * secs;
        }
        
        if (keys.isDown(GLFW_KEY_UP)) {
            lookY += SPEED * secs;
        } else if (keys.isDown(GLFW_KEY_DOWN)) {
            lookY -= SPEED * secs;
        }    
        
        if (keys.isDown(GLFW_KEY_SPACE)) {
            lookX = 0;
            lookY = 0;              
        }
        camera.getTarget().set(lookX, lookY, 0.0f);
        time += secs;
    }

    public void drawSky() {
        glDisable(GL_DEPTH_TEST);
        Shader shader = skyMaterial.getShader();
        shader.bind()
            .setUniform("uProjection", camera.getProjectionMatrix())
            .setUniform("uView", camera.getViewMatrix())
            .setUniform("uCloud0Offset", new Vector2f(cloud0Offset).mul(time))
            .setUniform("uCloud1Offset", new Vector2f(cloud1Offset).mul(time))
        .unbind();
                
        skydome.setUniform("uWorld", new Matrix4f().rotateX((float)Math.toRadians(180)).scale(300));
        skydome.draw(skyMaterial);
        glEnable(GL_DEPTH_TEST);
    }
    
    public void drawScene() {
        Shader shader = material.getShader();
        shader.bind()
            .setUniform("uProjection", camera.getProjectionMatrix())
            .setUniform("uView", camera.getViewMatrix())
            .setUniform("uCameraPosition", camera.getPosition());        
        light.apply(shader);        
        shader.unbind();
    
        mesh.setUniform("uWorld", new Matrix4f().rotateY((float)Math.toRadians(85)));
        mesh.draw(material);
    }
    
    @Override
    public void draw() {        
        fb.bind();
	        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	        drawSky();
	        drawScene();
        fb.unbind();
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);        
        canvas.draw(postFX);
    }

    @Override
    public void deinit() {
    }

    public static void main(String[] args) {
        new Window(new MultiTexture(), "Multi texturing", 1024, 748).show();
    }
}
