1) The following commands may be used:

   - to compile
     % gradlew clean build

   - to run
     % gradlew run

2) The following app settings are in file config.json

   - "conn" block:

       - "isolationMode": read_uncommitted | read_committed | repeatable_read | serializable 
                          default = repeatable_read 

       - "concurrencyMode": default | optimistic | pessimistic 
                            default = default 

       - "batch_size": the size of each chunk of data to be sent to server

       - "useAutoCommit": false, true
                          default = false

       - "clear_graph": (OPTIONAL) IRI of named graph to be cleared before loading data

       - "max_threads": (OPTIONAL) max count of working threads
                        default = count of uploaded files

       - "data_dir": path to directory containing data files to be loaded

   - "data" block: The list of files comprising data to be loaded to a Virtuoso 
                   DBMS instance. By default, this app will start ONE DBMS 
                   connection (with a single thread) for each source file.

       - "file" : file name, including its path

       - "type" : content-type, which may be any of: 
                  "RDF/XML" | "TURTLE" | "TTL" | "N3" | "NTRIPLES" | "JSON-LD"
                  | "JSON-LD10" | "JSON-LD11" | "RDF/JSON" | "TRIG" | "NQUADS"
                  | "RDF-PROTO" | "RDF-THRIFT" | "SHACLC" | "TRIX"

       - "graph": (OPTIONAL) IRI of named graph to be loaded with data. May be left 
                  empty if the source data comprises quads.

       - "clear_graph": true - indicates that any existing data in the destination named 
                        graph should be cleared prior to commencement of new data load run.
                        default = false

       - "gzipped": (OPTIONAL) true - indicates that source file was compressed with GZip. 
                    It will be set automatically if the filename ends with extension ".gz" 
                    or ".z", such as "test.ttl.gz".
                    default = false
