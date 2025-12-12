package org.example.ui;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Простой график японских свечей для JavaFX.
 * Ось X – NumberAxis (обычно индекс дня: 0,1,2,3...),
 * Ось Y – NumberAxis (значения курса).
 *
 * В Data.xValue кладём индекс,
 * в Data.yValue – цену ОТКРЫТИЯ,
 * в extraValue – объект OHLC (open, close, high, low).
 */
public class CandleStickChart extends XYChart<Number, Number> {

    /**
     * Дополнительные данные свечи.
     */
    public static class OHLC {
        private final double open;
        private final double close;
        private final double high;
        private final double low;

        public OHLC(double open, double close, double high, double low) {
            this.open = open;
            this.close = close;
            this.high = high;
            this.low = low;
        }

        public double getOpen() { return open; }
        public double getClose() { return close; }
        public double getHigh() { return high; }
        public double getLow()  { return low; }
    }

    public CandleStickChart(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        setAnimated(false);
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        setLegendVisible(false);
    }

    public CandleStickChart(Axis<Number> xAxis, Axis<Number> yAxis,
                            ObservableList<Series<Number, Number>> data) {
        this(xAxis, yAxis);
        setData(data);
    }

    @Override
    protected void layoutPlotChildren() {
        if (getData() == null) return;

        double candleWidth = 8; // ширина по умолчанию

        // если X – NumberAxis, подстраиваем ширину под расстояние между тиками
        if (getXAxis() instanceof NumberAxis) {
            NumberAxis xa = (NumberAxis) getXAxis();
            double tick = xa.getTickUnit();
            double x0 = xa.getDisplayPosition(0);
            double x1 = xa.getDisplayPosition(tick);
            candleWidth = Math.abs(x1 - x0) * 0.6; // 60% расстояния между тиками
            if (candleWidth < 4) candleWidth = 4;
        }

        for (Series<Number, Number> series : getData()) {
            for (Data<Number, Number> item : series.getData()) {
                Node node = item.getNode();
                if (!(node instanceof Candle)) continue;
                Object extra = item.getExtraValue();
                if (!(extra instanceof OHLC)) continue;

                OHLC ohlc = (OHLC) extra;

                double x = getXAxis().getDisplayPosition(item.getXValue());
                double yOpen  = getYAxis().getDisplayPosition(ohlc.getOpen());
                double yClose = getYAxis().getDisplayPosition(ohlc.getClose());
                double yHigh  = getYAxis().getDisplayPosition(ohlc.getHigh());
                double yLow   = getYAxis().getDisplayPosition(ohlc.getLow());

                Candle candle = (Candle) node;

                double baseY = yOpen;
                double closeOffset = yClose - baseY;
                double highOffset  = yHigh  - baseY;
                double lowOffset   = yLow   - baseY;

                candle.update(closeOffset, highOffset, lowOffset, candleWidth, ohlc);
                candle.setLayoutX(x);
                candle.setLayoutY(baseY);
            }
        }
    }

    @Override
    protected void dataItemChanged(Data<Number, Number> item) {
        // нам ничего не нужно делать
    }

    @Override
    protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number, Number> item) {
        Node candle = createCandle(series, item, itemIndex);
        getPlotChildren().add(candle);
    }

    @Override
    protected void dataItemRemoved(Data<Number, Number> item, Series<Number, Number> series) {
        final Node candle = item.getNode();
        getPlotChildren().remove(candle);
    }

    @Override
    protected void seriesAdded(Series<Number, Number> series, int seriesIndex) {
        for (int j = 0; j < series.getData().size(); j++) {
            Data<Number, Number> item = series.getData().get(j);
            Node candle = createCandle(series, item, j);
            getPlotChildren().add(candle);
        }
    }

    @Override
    protected void seriesRemoved(Series<Number, Number> series) {
        for (Data<Number, Number> d : series.getData()) {
            Node candle = d.getNode();
            getPlotChildren().remove(candle);
        }
    }

    @Override
    protected void updateAxisRange() {
        final Axis<Number> xa = getXAxis();
        final Axis<Number> ya = getYAxis();
        List<Number> xData = xa.isAutoRanging() ? new ArrayList<>() : null;
        List<Number> yData = ya.isAutoRanging() ? new ArrayList<>() : null;

        if (getData() == null) return;

        for (Series<Number, Number> series : getData()) {
            for (Data<Number, Number> data : series.getData()) {
                if (xData != null) {
                    xData.add(data.getXValue());
                }
                if (yData != null) {
                    Object extra = data.getExtraValue();
                    if (extra instanceof OHLC) {
                        OHLC ohlc = (OHLC) extra;
                        yData.add(ohlc.getHigh());
                        yData.add(ohlc.getLow());
                    } else {
                        yData.add(data.getYValue());
                    }
                }
            }
        }

        if (xData != null) xa.invalidateRange(xData);
        if (yData != null) ya.invalidateRange(yData);
    }

    /**
     * Создаёт или переиспользует ноду-свечу для точки.
     */
    private Node createCandle(Series<Number, Number> series, final Data<Number, Number> item, int itemIndex) {
        Node node = item.getNode();
        if (node instanceof Candle) {
            return node;
        } else {
            Candle candle = new Candle();
            item.setNode(candle);
            return candle;
        }
    }

    /**
     * Визуальное представление одной свечи.
     */
    private static class Candle extends Group {
        private final Line wick = new Line();
        private final Rectangle body = new Rectangle();

        Candle() {
            getChildren().addAll(wick, body);
            wick.setStrokeWidth(1.2);
            body.setStroke(Color.BLACK);
        }

        void update(double closeOffset, double highOffset, double lowOffset,
                    double candleWidth, OHLC ohlc) {

            // линия high-low
            wick.setStartX(0);
            wick.setEndX(0);
            wick.setStartY(highOffset);
            wick.setEndY(lowOffset);

            // тело свечи
            double height = Math.abs(closeOffset);
            if (height < 2) height = 2; // не даём телу исчезнуть

            double topY = Math.min(0, closeOffset); // если close выше open – тело вверх

            body.setX(-candleWidth / 2.0);
            body.setY(topY);
            body.setWidth(candleWidth);
            body.setHeight(height);

            // цвет: зелёный – рост (close > open), красный – падение
            if (ohlc.getClose() >= ohlc.getOpen()) {
                body.setFill(Color.rgb(0, 180, 0)); // зелёный
            } else {
                body.setFill(Color.rgb(220, 0, 0)); // красный
            }

            wick.setStroke(Color.DARKGRAY);
        }
    }
}
