# ring-geoipviz

Ring middleware for visualizing incoming client requests on a map. This is
pretty hacked-together, so THAR BE DRAGONS!

## Usage

Wrap your Ring app like so:

    (-> your-ring-app
        (wrap-with-buffer :geoip "/geoviz" 100)
        (wrap-with-geoip [:remote-addr])
        (wrap-params))

`wrap-with-buffer` and `wrap-with-geoip` are included in this library, and
`wrap-params` is provided by ring-core.

That will "mount" this middleware at "/geoviz". Hitting "/geoviz/viz" will
display an HTML page that will display incoming requests on its map.

## License

Copyright © 2012 

Apache licensed.
