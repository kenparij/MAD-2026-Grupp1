package com.example.townexplorer.map

object FogGeometryGenerator {

    /**
     * Generates fog overlay with GeoJSON.
     *
     * Coordinates use GeoJSON order:
     * [longitude, latitude]
     *
     * The returned geometry contains the playable boundary
     * and interior rings representing explored areas.
     *
     * Sources:
     *   https://maplibre.org/maplibre-native/android/examples/geojson-guide/
     *   https://datatracker.ietf.org/doc/html/rfc7946
     */

    fun demoGeoJson(): String {

        return """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "properties": {
                "type": "fog"
              },
              "geometry": {
                "type": "Polygon",
                "coordinates": [

                  [
                    [26.704, 58.369],
                    [26.744, 58.369],
                    [26.744, 58.389],
                    [26.704, 58.389],
                    [26.704, 58.369]
                  ],

                  [
                    [26.7275, 58.3770],
                    [26.7275, 58.3790],
                    [26.7305, 58.3790],
                    [26.7305, 58.3770],
                    [26.7275, 58.3770]
                  ]

                ]
              }
            }
          ]
        }
        """.trimIndent()
    }
}
