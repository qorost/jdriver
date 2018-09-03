# jdriver

Automatic Driver Class Generation for AFL-based Fuzzing Tools


## Overview

Recent efforts such as [Kelinchi](https://github.com/isstac/kelinci) have been witnessed to port the most powerful fuzzing tool [Afl](http://lcamtuf.coredump.cx/) to fuzzing Java code. However, these tools don't address the problem of driver class generation and they need to write driver classes by the testers. Besides, Afl-based fuzzing tools generate file to store input data, which requires further opertions to fit for general methods. jdriver aimes to automate the driver class generation for general methods.

Given a Java program, Jdriver will perform the following steps:

1. **preprocessing**, collecting instantiating methods, build call graph, analyze class nodes.

2. **dependency analysis**, for each class, jdriver analysis it to get the knowledge of what fields are accessed, and what methods can be used to modify private field.

3. **method sequence building**, with the dependency analysis results, jdriver build method sequences that are able to change the fields invovled in branch statements.

4. **instance generation**, jdriver make instance with the collected methods as well as built-in helper methods.

5. **driver class assembling**, depending on whether the method under test process file directly or not, jdrivers assemble the method parameters in the method sequences and build statements to recover values for the parameters.


## Running

The options for jdriver:

* `-i`, to specify the path of software for test
* `-o`, to specify the path of generated driver classes



## Acknowledgements

This project makes use of the following libraries:

* [asm](http://asm.ow2.io/)
* [jgrapht](https://jgrapht.org/)
* [args4j](https://github.com/kohsuke/args4j)
* [commons-io](https://commons.apache.org/io/)

It builds with:
* [maven](https://maven.apache.org/)
