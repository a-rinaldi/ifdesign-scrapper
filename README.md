## Thread Safe Paginated Scraper
The scraper in this code is built to extract data from a website that displays its information in smaller segments, spread across different individual pages. Each of these pages consists of a distinct portion of the complete data set. To accomplish this, the scraper sends a series of HTTP POST requests to the API endpoint, where each request specifies a unique page number that corresponds to a different subset of the data.

The `IfDesignTask` class represents each individual request that is sent to the API. The constructor of this class takes a `page` parameter, which specifies the page number to retrieve. The `Scraper` class creates multiple instances of `IfDesignTask`, each with a different page number, and submits them to an `ExecutorService` to be executed in parallel.

When each `IfDesignTask` instance is executed, it sends an HTTP POST request to the API endpoint, specifying the page number to retrieve. The API responds with a JSON object containing a subset of the data, which is mapped to a `CompanyResponse` object using an `ObjectMapper`. The `CompanyResponse` object contains a list of `CompanyItem` objects, which represent the individual data records.

The `IfDesignTask` adds its `CompanyResponse` object to the `BlockingQueue` and increments a `totalItemsScanned` counter. The `totalItemsScanned` variable keeps track of the total number of data records that have been retrieved so far, while the `BlockingQueue` ensures that the `IfDesignFileWriterTask` has access to the data in a thread-safe manner.

The `Scraper` class creates an `IfDesignFileWriterTask` instance to write the data to a CSV file. This task reads `CompanyResponse` objects from the `BlockingQueue`, which were added by the `IfDesignTask` instances, and writes the data to a CSV file using a `CsvMapper` and `CsvSchema`. This process continues until all of the data has been downloaded and written to the file, or until a timeout is reached.

Overall, using multiple `IfDesignTask` instances and the `BlockingQueue` ensures that the scraper can efficiently download data from a paginated web page and write it to a file without overloading the system with excessive requests or data processing.
