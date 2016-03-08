package com.aggregate.screenshotmaker;

import io.vertx.core.AbstractVerticle;
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
        vertx.eventBus().consumer("cmd.makeScreenshot", m -> captureScreen());

    }

    private void captureScreen() {
        String fileName = "/Screenshot_" + new SimpleDateFormat("yyyy-mm-dd:hh-mm").format(new Date()) + ".jpg";
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.error(e);
            return;
        }

        BufferedImage screenShot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        File path;
        try {
            path = new File(TEMP_PATH, "ScreenShot");
        } catch (Exception e) {
            log.error(e);
            return;
        }
        File screenShotPathToFile;
        try {
            log.debug(path.getAbsolutePath());
            screenShotPathToFile = new File(path + fileName);
        } catch (Exception e) {
            log.error(e);
            return;
        }

        try {
            ImageIO.write(screenShot, "jpg", screenShotPathToFile);
        } catch (IOException e) {
            log.error(e);
            return;
        }

        openScreenShotFolder();

    }

    public void openScreenShotFolder() {
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        } else {
            log.warn("Desktop is not supported");
            return;
        }
        try {
            desktop.open(new File(TEMP_PATH + "ScreenShot"));
        } catch (IOException e) {
            log.error(e);
            return;
        }
    }
}
