# KE-DryRun-benchmark
**A benchmark tool for evaluate the performance of Ke-Query capacity**

## Build
```shell
mvn clean install
```

## Start up
1. Firstly, make sure the ke_node is reachable!
2. Run the scripts
    ```shell
    java \
    -Dfile.dir=./samples/query_history1 \
    -Dkylin.query.node=10.1.2.134:8899 \
    -Dkylin.userName=ADMIN \
    -Dkylin.passwd=KYLIN \
    -jar target/ke-dryrun-benchmark-1.0.0.jar 
    ```
3. Benchamark data will present in the console, and store as file.

### more
> - Set your file.dir、node、user、passwd
> - If it doesn't work, check the version of ke-dryrun-benchmark is right? 

## Configurations

| Setting                    | Default Value | Required | Description                                                                        |
|:---------------------------|:-------------:|:--------:|------------------------------------------------------------------------------------|
| file.dir                   |               |    √     | CSV file path to run benchmark                                                     |
| concurrency                |       4       |          | Number of concurrent threads                                                       |
| queue.size                 |     2048      |          | Thread pool queue size                                                             |
| rounds                     |       2       |          | Number of benchmark rounds                                                         |
| args.check.enabled         |     true      |          | Enable argument check <br/> It will check if ke_node is reachable & check userinfo |
| metadata.recover.enabled   |     false     |          | Enable metadata recovery                                                           |
| http.protocol              |     http      |          | HTTP protocol                                                                      |
| kylin.query.node           |               |    √     | Kylin query node                                                                   |
| kylin.userName             |     ADMIN     |    √     | Kylin username                                                                     |
| kylin.passwd               |     KYLIN     |    √     | Kylin password                                                                     |
| report.output.dir          |       .       |          | Report output directory                                                            |
| logging.level.io.kyligence |     info      |          | logging level                                                                      |


> enjoy the benchmark travel