package org.esa.snap.dataio.geotiff;

import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.image.AbstractMatrixMosaicSubsetMultiLevelSource;
import org.esa.snap.core.image.ImageReadBoundsSupport;
import org.esa.snap.core.image.MosaicMatrix;
import org.esa.snap.core.image.UncompressedTileOpImageCallback;

import javax.media.jai.ImageLayout;
import javax.media.jai.SourcelessOpImage;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * Created by jcoravu on 7/1/2020.
 */
public class GeoTiffMatrixMultiLevelSource extends AbstractMatrixMosaicSubsetMultiLevelSource implements UncompressedTileOpImageCallback<GeoTiffMatrixCell>, GeoTiffBandSource {

    private final int bandIndex;
    private final Double noDataValue;
    private final Dimension defaultJAIReadTileSize;

    public GeoTiffMatrixMultiLevelSource(MosaicMatrix spotBandMatrix, Rectangle imageMatrixReadBounds, int bandIndex,
                                         GeoCoding geoCoding, Double noDataValue, Dimension defaultJAIReadTileSize) {

        super(spotBandMatrix, imageMatrixReadBounds, defaultJAIReadTileSize, geoCoding);

        this.defaultJAIReadTileSize = defaultJAIReadTileSize;
        this.bandIndex = bandIndex;
        this.noDataValue = noDataValue;
    }

    @Override
    public SourcelessOpImage buildTileOpImage(ImageReadBoundsSupport imageReadBoundsSupport, int tileWidth, int tileHeight,
                                              int tileOffsetFromReadBoundsX, int tileOffsetFromReadBoundsY, GeoTiffMatrixCell geoTiffMatrixCell) {

        return new GeoTiffTileOpImage(geoTiffMatrixCell, this, geoTiffMatrixCell.getDataBufferType(), tileWidth, tileHeight,
                                      tileOffsetFromReadBoundsX, tileOffsetFromReadBoundsY, imageReadBoundsSupport, this.defaultJAIReadTileSize);
    }

    @Override
    protected java.util.List<RenderedImage> buildMatrixCellTileImages(int level, Rectangle imageCellReadBounds, float cellTranslateLevelOffsetX, float cellTranslateLevelOffsetY,
                                                                      MosaicMatrix.MatrixCell matrixCell) {

        GeoTiffMatrixCell geoTiffMatrixCell = (GeoTiffMatrixCell)matrixCell;
        return buildUncompressedTileImages(level, imageCellReadBounds, geoTiffMatrixCell.getCellWidth(), geoTiffMatrixCell.getCellHeight(),
                                           cellTranslateLevelOffsetX, cellTranslateLevelOffsetY, this, geoTiffMatrixCell);
    }

    @Override
    protected ImageLayout builMosaicImageLayout(int level) {
        return null; // no image layout to configure the mosaic image since the tile images are configured
    }

    @Override
    protected double[] getMosaicOpBackgroundValues() {
        if (this.noDataValue == null) {
            return super.getMosaicOpBackgroundValues();
        }
        return new double[] { this.noDataValue.doubleValue() };
    }

    @Override
    public synchronized void reset() {
        super.reset();

        for (int rowIndex = 0; rowIndex < this.mosaicMatrix.getRowCount(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < this.mosaicMatrix.getColumnCount(); columnIndex++) {
                GeoTiffMatrixCell geoTiffMatrixCell = (GeoTiffMatrixCell)this.mosaicMatrix.getCellAt(rowIndex, columnIndex);
                synchronized (geoTiffMatrixCell) {
                    try {
                        geoTiffMatrixCell.close();
                    } catch (IOException e) {
                        // do nothing
                    }
                }
            }
        }
    }

    @Override
    public boolean isGlobalShifted180() {
        return false;
    }

    @Override
    public int getBandIndex() {
        return this.bandIndex;
    }
}
