package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public final class KmlPrinter {
    private static final String KML_HEADER =
            """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <kml xmlns="http://www.opengis.net/kml/2.2"
                         xmlns:gx="http://www.google.com/kml/ext/2.2">
                      <Document>
                        <name>JaVelo</name>
                        <Style id="byBikeStyle">
                          <LineStyle>
                            <color>a00000ff</color>
                            <width>4</width>
                          </LineStyle>
                        </Style>
                        <Placemark>
                          <name>Path</name>
                          <styleUrl>#byBikeStyle</styleUrl>
                          <MultiGeometry>
                            <LineString>
                              <tessellate>1</tessellate>
                              <coordinates>""";

    private static final String KML_FOOTER =
            """
                              </coordinates>
                            </LineString>
                          </MultiGeometry>
                        </Placemark>
                      </Document>
                    </kml>""";

    public static void write(String fileName, Route route)
            throws IOException {
        try (PrintWriter w = new PrintWriter(fileName)) {
            w.println(KML_HEADER);
            for (PointCh p : route.points())
                w.printf(Locale.ROOT,
                        "            %.5f,%.5f\n",
                        Math.toDegrees(p.lon()),
                        Math.toDegrees(p.lat()));
            w.println(KML_FOOTER);
        }
    }
}