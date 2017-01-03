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
            box.speedX = -0.20; //sets the pipe speed
            
        }
    }

    private static double game(){
        PIPE_SPAWN_TIME = 4;
        shapes.clear();
        Random rand = new Random();
        Timer pipe_spawn_timer = new Timer();
        pipe_spawn_timer.reset();
        double playerScore = 0;
        boolean gameOver = false;
        int playerX = DISPLAY_WIDTH / 20;
        int playerY = DISPLAY_HEIGHT / 2;
        Box playerBox = new Box(playerX, playerY, 40, 40);
        int random = (int)(Math.random() *(DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10)  + 1);
        spawnPipe((DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10) - random);       

        lastFrame = getTime();        
        //gameloop
        
        while (!mayClose() && !gameOver) {     
            
            glClear(GL_COLOR_BUFFER_BIT);
            //Gets delta hat is used by the library in order to have animations independent from framerate.
            double delta = getDelta();
            //tick the timer used for spawning pipes.
            pipe_spawn_timer.tick();
            //Check if its time to spawn a new pair of pipes.
            if(pipe_spawn_timer.getTime() >= PIPE_SPAWN_TIME){
                random = (int)(Math.random() *(DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10)  + 1);
                System.out.println(random);
                spawnPipe((DISPLAY_HEIGHT  - PIPE_GAP_SIZE - 10) - random);
                pipe_spawn_timer.reset();    
            }
            //Keys and event listening
            while (Keyboard.next()) {
                
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE && Keyboard.getEventKeyState()) {
                    playerBox.speedY = -0.5; //makes player jump               
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                Display.destroy();
                System.exit(0);
            }
            
            //update pipes on screen, check for collision with player.
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
                if(currentBox.x <= -PIPE_WIDTH){
                    playerScore += 0.5;
                    System.out.println(playerScore);
                    i.remove();
                if(playerBox.y + playerBox.height >=1000){
                   gameOver = true;
                }    
                }else{                    
                    currentBox.update(delta);
                    currentBox.draw();
                }                
            }

            //Update player on screen
            playerBox.speedY += 0.025; //"gravity"
            playerBox.update(delta);
            playerBox.draw();
            
            //Checks and changes difficulty settings according to scores.
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
         
        return playerScore;
    }
    
    private static void displayScores(double playerScore){
        font = new Font("Verdana", Font.BOLD, 20);
        ttf = new TrueTypeFont(font, true);
        boolean quitScoreScreen = false;
        //display scores.
        while(!quitScoreScreen){
            glClear(GL_COLOR_BUFFER_BIT);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ttf.drawString(40, 40, "FINAL SCORE: " + playerScore);
            ttf.drawString(40, 100, "PRESS SPACE TO GO BACK TO THE MAIN SCREEN");
            
            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE && Keyboard.getEventKeyState()) {
                    quitScoreScreen = true;              
                 }
            }
            mayClose();
            Display.update();
            Display.sync(60);
        }
        GL11.glDisable(GL11.GL_BLEND);
    }
    
    private static int chooseMode(){        
        int GAME_MODE = 0;
        font = new Font("Verdana", Font.BOLD, 20);
        ttf = new TrueTypeFont(font, true);
        boolean quitchooseMode = false;

        while(!quitchooseMode){
            glClear(GL_COLOR_BUFFER_BIT);
            GL11.glEnable(GL11.GL_BLEND);            
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ttf.drawString(40, 40, "CHOOSE YOUR DESTINY");
            ttf.drawString(40, 100, "PRESS SPACE TO START THE GAME");
            
            mayClose();
            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_SPACE && Keyboard.getEventKeyState()) {
                    quitchooseMode = true;
                    GAME_MODE = 1;
                }
            }
            Display.update();
            Display.sync(60);
        }
        GL11.glDisable(GL11.GL_BLEND);
        return GAME_MODE;
    }

    private static void showSettings(){
        
    }
    
    public static void main(String args[]) throws IOException {               
        //GAME_MODE 0 = choose game mode
        //GAME_MODE 1 = play the game
        //GAME_MODE 2 = settings/credits/whatever.
        //mode choice. (credits, start game, etc)
        
        do {
            int GAME_MODE = 0;
            try {
                Display.destroy();
                Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
                Display.setTitle("Luccas Malandro Porps Game");
                Display.create();
                glMatrixMode(GL_PROJECTION);
        glOrtho(0, DISPLAY_WIDTH, DISPLAY_HEIGHT, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);            
        double playerScore;

        GAME_MODE = chooseMode();

        switch(GAME_MODE){               
            case 1: playerScore = game();
                    displayScores(playerScore);
                    break;
            case 2: showSettings();
                    break;
        }
        
            } catch (LWJGLException e) {
                e.printStackTrace();
                Display.destroy();
                System.exit(1);
            }
        //Display.destroy();     
        
        }while (!Display.isCloseRequested());
        Display.destroy();
        System.exit(0);
    }

    public static boolean mayClose(){
        if (Display.isCloseRequested()){
             Display.destroy();
             System.exit(0);
        }
        
        return false;
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