#!/bin/bash
bash ./fetchDataset.sh
bash ./fetchDependencies.sh
mvn clean compile
java -Xmx4G -cp ./jars/psl-core-1.3.1-SNAPSHOT.jar:./jars/psl-groovy-1.3.1-SNAPSHOT.jar:./jars/psl-archetype-groovy-1.3.1-SNAPSHOT.jar:./target/classes edu.umd.cs.onlineinfer.jester.Jester quad 100 10
java -Xmx4G -cp ./jars/psl-core-1.3.1-SNAPSHOT.jar:./jars/psl-groovy-1.3.1-SNAPSHOT.jar:./jars/psl-archetype-groovy-1.3.1-SNAPSHOT.jar:./target/classes edu.umd.cs.onlineinfer.jester.JesterApproximate quad 100 10 wtlagrangemax

