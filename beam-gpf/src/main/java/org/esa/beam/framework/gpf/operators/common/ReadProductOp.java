package org.esa.beam.framework.gpf.operators.common;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.TargetProduct;

import java.awt.Rectangle;
import java.io.IOException;

/**
 * The <code>LoadProductOperator</code> wrapps the BEAM {@link ProductIO} to load any kind of Product
 * from the file system into an <code>Operator</code>.</p>
 * <p/>
 * Configuration elements:
 * <ul>
 * <li><b>filePath:</b> The path of the file to read the Product from</th>
 * </ul>
 *
 * @author Maximilian Aulinger
 */
public class ReadProductOp extends Operator {

    private ProductReader beamReader;

    /**
     * The path to the data product file to open and read.
     */
    @Parameter
    private String filePath = null;
    @TargetProduct
    private Product targetProduct;

    @Override
    public Product initialize() throws OperatorException {
        try {
            targetProduct = ProductIO.readProduct(filePath, null);
            if (targetProduct == null) {
                throw new OperatorException("No product reader found for file " + filePath);
            }
            beamReader = targetProduct.getProductReader();
        } catch (IOException e) {
            throw new OperatorException(e);
        }
        return targetProduct;
    }

    @Override
    public void computeTile(Band band, Tile targetTile) throws OperatorException {

        ProductData dataBuffer = targetTile.getRawSamples();
        Rectangle rectangle = targetTile.getRectangle();
        try {
            beamReader.readBandRasterData(band, rectangle.x, rectangle.y, rectangle.width,
                                          rectangle.height, dataBuffer, createProgressMonitor());
            targetTile.setRawSamples(dataBuffer);
        } catch (IOException e) {
            throw new OperatorException(e);
        }
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(ReadProductOp.class, "ReadProduct");
        }
    }
}
