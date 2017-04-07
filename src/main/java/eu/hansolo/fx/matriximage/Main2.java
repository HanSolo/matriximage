/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.matriximage;

import eu.hansolo.fx.dotmatrix.DotMatrix;
import eu.hansolo.fx.dotmatrix.DotMatrix.DotShape;
import eu.hansolo.fx.dotmatrix.DotMatrixBuilder;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.IntBuffer;
import java.util.Arrays;


/**
 * Created by hansolo on 07.04.17.
 */
public class Main2 extends Application {
    private DotMatrix      matrix;
    private DotMatrix      greenMatrix;
    private DotMatrix      redMatrix;
    private Image          src;
    private WritableImage  dst;
    private long           lastTimerCall;
    private AnimationTimer timer;


    @Override public void init() {
        matrix = DotMatrixBuilder.create()
                                 .colsAndRows(200, 85)
                                 .prefSize(706, 300)
                                 .dotOnColor(Color.RED)
                                 .dotShape(DotShape.SQUARE)
                                 .build();

        greenMatrix = DotMatrixBuilder.create()
                                     .colsAndRows(100, 85)
                                     .prefSize(353, 300)
                                     .dotOnColor(Color.RED)
                                     .build();

        redMatrix = DotMatrixBuilder.create()
                                    .colsAndRows(100, 85)
                                    .prefSize(353, 300)
                                    .dotOnColor(Color.LIME)
                                    .build();

        src = new Image(Main.class.getResourceAsStream("han-solo-with-blaster.jpg"), 1408, 1198, true, false);
        dst = new WritableImage(1408, 1198);

        pixelate(greenMatrix, false, true, false);
        pixelate(redMatrix, true, false, false);

        addTextAt(greenMatrix, 5, 5);
        addTextAt(redMatrix, 5, 75);

        for (int y = 0 ; y < 85 ; y++) {
            for (int x = 0 ; x < 100 ; x++) {
                matrix.setPixel(x, y, greenMatrix.getMatrix()[x][y]);
                matrix.setPixel(x + 100, y, redMatrix.getMatrix()[x][y]);
            }
        }


        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 10_000_000) {
                    matrix.shiftLeft();
                    //matrix.shiftRight();
                    //matrix.shiftUp();
                    //matrix.shiftDown();
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        StackPane pane = new StackPane(matrix);
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(10, 10, 20), CornerRadii.EMPTY, Insets.EMPTY)));
        Scene scene = new Scene(pane);

        stage.setTitle("Matrix Image");
        stage.setScene(scene);
        stage.show();

        timer.start();
    }

    @Override public void stop() {
        System.exit(0);
    }

    private void pixelate(final DotMatrix MATRIX) {
        pixelate(MATRIX, true, true, true);
    }
    private void pixelate(final DotMatrix MATRIX, final boolean WITH_RED, final boolean WITH_GREEN, final boolean WITH_BLUE) {
        PixelReader                    reader = src.getPixelReader();
        PixelWriter                    writer = dst.getPixelWriter();
        WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

        int width      = (int) src.getWidth();
        int height     = (int) src.getHeight();
        int kernelSize = 6;

        for (int y = kernelSize; y < height - kernelSize * 2; y += kernelSize * 2 + 1) {
            for (int x = kernelSize; x < width - kernelSize * 2; x += kernelSize * 2 + 1) {
                int kernelWidth  = kernelSize * 2 + 1;
                int kernelHeight = kernelSize * 2 + 1;

                int[] buffer = new int[kernelWidth * kernelHeight];
                reader.getPixels(x, y, kernelWidth, kernelHeight, format, buffer, 0, kernelWidth);

                int alpha  = 0;
                int red    = 0;
                int green  = 0;
                int blue   = 0;

                for (int color : buffer) {
                    alpha += (color >>> 24);
                    red   += (color >>> 16 & 0xFF);
                    green += (color >>> 8 & 0xFF);
                    blue  += (color & 0xFF);
                }
                alpha = alpha / kernelWidth / kernelHeight;
                red   = WITH_RED   ? red   / kernelWidth / kernelHeight : 0;
                green = WITH_GREEN ? green / kernelWidth / kernelHeight : 0;
                blue  = WITH_BLUE  ? blue  / kernelWidth / kernelHeight : 0;

                int pixelateColor = (alpha << 24) + (red << 16) + (green << 8) + blue;
                Arrays.fill(buffer, pixelateColor);
                writer.setPixels(x, y, kernelWidth, kernelHeight, format, buffer, 0, kernelWidth);

                MATRIX.setPixel(x / kernelWidth, y / kernelHeight, pixelateColor);
            }
        }
    }

    private void addTextAt(final DotMatrix MATRIX, final int X, final int Y) {
        MATRIX.setCharAtWithBackground('@', X, Y);
        MATRIX.setCharAtWithBackground('h', X + 10, Y);
        MATRIX.setCharAtWithBackground('a', X + 20, Y);
        MATRIX.setCharAtWithBackground('n', X + 30, Y);
        MATRIX.setCharAtWithBackground('s', X + 40, Y);
        MATRIX.setCharAtWithBackground('o', X + 50, Y);
        MATRIX.setCharAtWithBackground('l', X + 60, Y);
        MATRIX.setCharAtWithBackground('o', X + 70, Y);
        MATRIX.setCharAtWithBackground('_', X + 80, Y);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
