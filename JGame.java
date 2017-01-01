package jgame;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.Sys;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Timer;
import org.lwjgl.opengl.GL11;
import java.awt.Font;
import java.io.IOException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;



/**
 * Simple game for computer graphics class. Looks like 
 *
 * @author Luccas MEndes Rodrigues
 */

public class JGame {
    private static final List<Box> shapes = new ArrayList<Box>(16);
    private static final int DISPLAY_WIDTH = 800;
    private static final int DISPLAY_HEIGHT = 600;
    private static final int PIPE_WIDTH = 50;
    private static final int PIPE_GAP_SIZE = 140;
    private static int PIPE_SPAWN_TIME = 4;
    
    private static long lastColourChange;    
    private static long lastFrame;
    
    
    static Font font;
    static TrueTypeFont ttf;
    
    private static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private static double getDelta() {
        long currentTime = getTime();
        double delta = (double) (currentTime - lastFrame);
        lastFrame = getTime();
        return delta;
    }

    private static void spawnPipe(int gap_height){
        //pipe_height indicates the height the gap will be.
        shapes.add(new Box(DISPLAY_WIDTH - PIPE_WIDTH, DISPLAY_HEIGHT - gap_height, gap_height, PIPE_WIDTH));
        shapes.add(new Box(DISPLAY_WIDTH - PIPE_WIDTH, 0, DISPLAY_HEIGHT - gap_height - PIPE_GAP_SIZE, PIPE_WIDTH));
        for (final Box box : shapes) {
            box.speedX = -0.20;
        }
    }

    public static void main(String args[]) throws IOException {
        try {
            Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
            Display.setTitle("Luccas Malandro Porps Game");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        //draw player box        
        Random rand = new Random();
        Timer pipe_spawn_timer = new Timer();
        pipe_spawn_timer.set(0);
        double playerScore = 0;
        boolean gameOver = false;
        int playerX = DISPLAY_WIDTH / 20;
        int playerY = DISPLAY_HEIGHT / 2;
        Box playerBox = new Box(playerX, playerY, 40, 40);
        int random = (int)(Math.random() *(DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10)  + 1);
        spawnPipe((DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10) - random);
        
        
        
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, DISPLAY_WIDTH, DISPLAY_HEIGHT, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        lastFrame = getTime();


//        Font awtFont = new Font("Times New Roman", Font.ITALIC, 24);
//        TrueTypeFont font = new TrueTypeFont(awtFont, true);
        font = new Font("Verdana", Font.BOLD, 20);
        ttf = new TrueTypeFont(font, true);
        
        while (!Display.isCloseRequested() && !gameOver) {            
            glClear(GL_COLOR_BUFFER_BIT);
                       
            double delta = getDelta();     
            pipe_spawn_timer.tick();
            if(pipe_spawn_timer.getTime() >= PIPE_SPAWN_TIME){
                random = (int)(Math.random() *(DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10)  + 1);
                System.out.println(random);
                spawnPipe((DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10) - random);
                pipe_spawn_timer.reset();                
            }
            
            //Keys and event listening
            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE && Keyboard.getEventKeyState()) {
                    playerBox.speedY = -0.5;               
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                Display.destroy();
                System.exit(0);
            }
            
            //update pipes on screen, check for collision
            Iterator<Box> i = shapes.iterator();
            while (i.hasNext()) {
                Box currentBox = i.next();
                if(playerBox.x >= currentBox.x & playerBox.x <= currentBox.x + PIPE_WIDTH & playerBox.y >= currentBox.y & playerBox.y <= currentBox.y + currentBox.height){
                    System.out.println("COLLISION TOP LEFT CORNER");
                    gameOver = true;
                }
                if(playerBox.x + playerBox.width >= currentBox.x & playerBox.x + playerBox.width <= currentBox.x + PIPE_WIDTH & playerBox.y >= currentBox.y & playerBox.y <= currentBox.y + currentBox.height){
                    System.out.println("COLLISION TOP RIGH CORNER");
                    gameOver = true;
                }
                if(playerBox.x >= currentBox.x & playerBox.x <= currentBox.x + PIPE_WIDTH & playerBox.y + playerBox.height >= currentBox.y & playerBox.y + playerBox.height <= currentBox.y + currentBox.height){
                    System.out.println("COLLISION BOTTOM LEFT CORNER");
                    gameOver = true;
                }
                if(playerBox.x + playerBox.width >= currentBox.x & playerBox.x + playerBox.width <= currentBox.x + PIPE_WIDTH & playerBox.y + playerBox.height>= currentBox.y & playerBox.y + playerBox.height <= currentBox.y + currentBox.height){
                    System.out.println("COLLISION BOTTOM RIGH CORNER");
                    gameOver = true;
                }
                if(currentBox.x <= -DISPLAY_WIDTH){
                    playerScore += 0.5;
                    System.out.println(playerScore);
                    i.remove();
                }else{                    
                    currentBox.update(delta);
                    currentBox.draw();
                }                
            }

            //Update player on screen
            playerBox.speedY += 0.025;
            playerBox.update(delta);
            playerBox.draw();
            
            if (playerScore == 2){
                PIPE_SPAWN_TIME = 3;
            }
            if (playerScore == 5){
                PIPE_SPAWN_TIME = 2;
            }
            if (playerScore == 8){
                PIPE_SPAWN_TIME = 1;
            }
            Display.update();
            Display.sync(60);
        }
        
        while(!Display.isCloseRequested()){
            glClear(GL_COLOR_BUFFER_BIT);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ttf.drawString(40, 40, "FINAL SCORE: " + playerScore);
            Display.update();
        }        
        Display.destroy();
    }

    private static class Box {
        public int x, y, height, width;
        public double speedX, speedY;
        private float colorRed, colorBlue, colorGreen;

        Box(int x, int y, int height, int width) {
            this.x = x;
            this.y = y;
            this.height = height;
            this.width = width;
            this.speedX = this.speedY = 0;

            Random randomGenerator = new Random();
            colorRed = randomGenerator.nextFloat();
            colorBlue = randomGenerator.nextFloat();
            colorGreen = randomGenerator.nextFloat();
        }

        void update(double delta) {
            x += this.speedX * delta;
            y += this.speedY * delta;            
        }

        void randomiseColors() {
            Random randomGenerator = new Random();
            colorRed = randomGenerator.nextFloat();
            colorBlue = randomGenerator.nextFloat();
            colorGreen = randomGenerator.nextFloat();
        }

        void draw() {
            glColor3f(colorRed, colorGreen, colorBlue);
            glBegin(GL_QUADS);
            glVertex2f(x, y);
            glVertex2f(x + this.width, y);
            glVertex2f(x + this.width, y + this.height);
            glVertex2f(x, y + this.height);
            glEnd();
        }
    }   
}