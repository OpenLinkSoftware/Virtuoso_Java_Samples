export CLASSPATH=.:virtjdbc4_3.jar
/usr/local/jdk21/bin/javac -classpath $CLASSPATH TestPoolStat.java
/usr/local/jdk21/bin/java -classpath $CLASSPATH TestPoolStat
