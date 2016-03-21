package com.popov.screenshotmaker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by AntonPopov on 05.03.16.
 */
public class ScreenShotMaker extends AbstractVerticle {
    public static final String TEMP_PATH = System.getProperty("java.io.tmpdir");
    private static Logger log = LoggerFactory.getLogger(ScreenShotMaker.class);

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("cmd.makeScreenshot", m -> captureScreen(m));

    }

    private void captureScreen(Message m) {

        String fileName = "ScreenShot_" + new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss").format(new Date()) + ".jpg";
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.error(e);
            return;
        }

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

        File path;
        path = new File(TEMP_PATH, "ScreenShot");
        File screenShotPathToFile;
        screenShotPathToFile = new File(path, fileName);


        path.mkdirs();
        if (!path.exists()){
            log.error("Can not create directory for screenshots");
            return;
        }
        try {
            ImageIO.write(screenShot, "jpg", screenShotPathToFile);
        } catch (IOException e) {
            log.error(e);
            return;
        }

        openScreenShotFolder(path);

    }

    public void openScreenShotFolder(File path) {
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        } else {
            log.warn("Desktop is not supported");
            return;
        }
        try {
            desktop.open(path);
        } catch (IOException e) {
            log.error(e);
            return;
        }
    }
}
