package com.sebbaindustries.advancedafk.engine;

import com.sebbaindustries.advancedafk.Core;
import com.sebbaindustries.advancedafk.engine.buffer.DataBuffer;

import java.util.concurrent.CompletableFuture;

public class DetectionEngine {

    private volatile boolean running = false;
    public DataBuffer dataBuffer;


    private void detectionLoop() {
        while (running) {
            long currentTime = System.currentTimeMillis();
            dataBuffer.update();
            dataBuffer.compute();
            dataBuffer.clean();
            long passedTime = System.currentTimeMillis();
            long delta = passedTime - currentTime;
            if (delta >= 1000L) {
                Core.gCore().logSevere("AdvancedAFK engine thread is running slow. Detection time took " + delta + "ms!");
                continue;
            }
            System.out.println("Delta: " + delta);
            try {
                Thread.sleep((1000L - delta));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize() {
        running = true;
        dataBuffer = new DataBuffer();
        CompletableFuture.supplyAsync(() -> {
            detectionLoop();
            return null;
        }).exceptionally(e -> {
            e.printStackTrace();
            terminate();
            return null;
        });
    }

    public void terminate() {
        if (!running) {
            Core.gCore().logSevere("There was an failed attempt to terminate detection engine!");
            return;
        }
        running = false;
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Core.gCore().log("Detection engine terminated successfully!");
    }

}
