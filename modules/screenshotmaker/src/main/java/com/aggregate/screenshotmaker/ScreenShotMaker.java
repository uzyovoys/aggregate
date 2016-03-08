package com.aggregate.screenshotmaker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by AntonPopov on 05.03.16.
 */
public class ScreenShotMaker extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(ScreenShotMaker.class);
    public static final String TEMP_PATH = System.getProperty("java.io.tmpdir");

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("cmd.makeScreenshot", m -> captureScreen());

    }

    private void captureScreen() {
        String fileName = "Screenshot_" + Math.random() + ".jpg";
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.error(e);
            return;
        }

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        createDir(TEMP_PATH + "ScreenShot/");
        File screenShotPathToFile = null;
        try {
            screenShotPathToFile = new File(TEMP_PATH + "ScreenShot/" + fileName);
        } catch (Exception e) {
            log.error(e);
            return;
        }

        try {
            ImageIO.write(screenShot, "jpg", screenShotPathToFile);
        }
        catch (IOException e) {
            log.error(e);
            return;
        }

        openScreenShotFolder();

}

    public void openScreenShotFolder() {
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }else {
            log.warn("Desktop is not supported");
            return;
        }
        try {
            desktop.open((new File(System.getProperty("java.io.tmpdir") + "ScreenShot/")));
        } catch (IOException e) {
            log.error(e);
            return;
        }
    }
    public static void createDir(String path){
        File f = new File(path);
        f.mkdirs();
    }
}
