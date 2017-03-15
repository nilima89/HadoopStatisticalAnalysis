if [ ! -d classes ]; then
        mkdir classes;
fi

# Compile HadoopStatistic
javac -classpath $HADOOP_HOME/hadoop-core-1.1.2.jar:$HADOOP_HOME/lib/commons-cli-1.2.jar -d ./classes src/HadoopStatistic.java

# Create the Jar
jar -cvf hadoopstat.jar -C ./classes/ .
 
# Copy the jar file to the Hadoop distributions
cp hadoopstat.jar $HADOOP_HOME/bin/ 

