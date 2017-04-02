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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.nio.IntBuffer;
import java.util.Arrays;


/**
 * User: hansolo
 * Date: 02.04.17
 * Time: 09:41
 */
public class Main extends Application {
    private DotMatrix      matrix;
    private Image          src;
    private WritableImage  dst;
    private ImageView      imageView;
    private long           lastTimerCall;
    private AnimationTimer timer;

    @Override public void init() {
        matrix = DotMatrixBuilder.create()
                                 .colsAndRows(87, 65)
                                 .prefSize(400, 300)
                                 .dotShape(DotShape.SQUARE)
                                 .dotOnColor(Color.CYAN)
                                 .build();
        src = new Image(Main.class.getResourceAsStream("meise.jpg"), 800, 600, true, false);
        dst = new WritableImage(800, 600);

        imageView = new ImageView(src);
        imageView.setFitWidth(400);
        imageView.setFitHeight(300);

        pixelate();

        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall + 10_000_000) {
                    //matrix.shiftLeft();
                    //matrix.shiftRight();
                    //matrix.shiftUp();
                    matrix.shiftDown();
                    lastTimerCall = now;
                }
            }
        };
    }

    @Override public void start(Stage stage) {
        HBox pane = new HBox(imageView, matrix);
        pane.setSpacing(10);
        pane.setPadding(new Insets(10));
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

    private void pixelate() {
        PixelReader                    reader = src.getPixelReader();
        PixelWriter                    writer = dst.getPixelWriter();
        WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

        int width      = (int) src.getWidth();
        int height     = (int) src.getHeight();
        int kernelSize = 4;

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
                red   = red   / kernelWidth / kernelHeight;
                green = green / kernelWidth / kernelHeight;
                blue  = blue  / kernelWidth / kernelHeight;

                int pixelateColor = (alpha << 24) + (red << 16) + (green << 8) + blue;
                Arrays.fill(buffer, pixelateColor);
                writer.setPixels(x, y, kernelWidth, kernelHeight, format, buffer, 0, kernelWidth);

                matrix.setPixel(x / kernelWidth, y / kernelHeight, pixelateColor);
            }
        }
        matrix.setCharAtWithBackground('D', 0, 5);
        matrix.setCharAtWithBackground('o', 10, 5);
        matrix.setCharAtWithBackground('t', 20, 5);
        matrix.setCharAtWithBackground('M', 30, 5);
        matrix.setCharAtWithBackground('a', 40, 5);
        matrix.setCharAtWithBackground('t', 50, 5);
        matrix.setCharAtWithBackground('r', 60, 5);
        matrix.setCharAtWithBackground('i', 70, 5);
        matrix.setCharAtWithBackground('x', 80, 5);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
