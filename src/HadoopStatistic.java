import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.lang.model.SourceVersion;
import javax.tools.Tool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class HadoopStatistic extends Configured implements Tool{

	public static class Map 
	extends Mapper<LongWritable, Text, Text, Text>{

		public void map(LongWritable key, Text value, Context context
				) throws IOException, InterruptedException {
			context.write(new Text("numbers_input"), new Text(value.toString().trim()));
		}
	}

	public static class Combiner
	extends Reducer<Text,Text,Text,Text> {

		public void reduce(Text key, Iterable<Text> values, 
				Context context
				) throws IOException, InterruptedException {
			float min = 0;
			float sum = 0;
			int count = 0;
			float max = 0;
			float squreSum = 0;
			for (Text val : values) {
			
				if(count == 0){
					min = Float.parseFloat(val.toString());
					max = Float.parseFloat(val.toString());
				}
				sum += Float.parseFloat(val.toString()); 
				squreSum += (float) Math.pow(Double.parseDouble((val.toString())), 2);
				count++;
				if(min> Float.parseFloat(val.toString()))
					min = Float.parseFloat(val.toString());
				if(max < Float.parseFloat(val.toString()))
					max = Float.parseFloat(val.toString());
				System.out.println("count111:" + count);
			}
			
			context.write(new Text("sum_count_squresum"), new Text(sum+"_"+count+"_"+squreSum));
			context.write(new Text("min"), new Text(""+min));
			context.write(new Text("max"), new Text(""+max));
		}
	}

	public static class Reduce
	extends Reducer<Text,Text,Text,Text> {

		private FloatWritable result = new FloatWritable();

		public void reduce(Text key, Iterable<Text> values, 
				Context context
				) throws IOException, InterruptedException {
			if("min".equals(key.toString())){
				float min_value = caluculateMinMax(values , 0);
				context.write(new Text("min"), new Text(min_value+""));
				return;
			}else if("max".equals(key.toString())){
				float max_value = caluculateMinMax(values , 1);
				context.write(new Text("max"), new Text(max_value+""));
				return;
			}
			float value_sum = 0; 
			int count_sum = 1;
			float squre_sum = 0;
			float max = 0 ;
			String combVal="";
			for (Text val : values) {
				combVal= val.toString();
				String[] tokens = combVal.split("_");
				value_sum += Float.parseFloat(tokens[0]);
				count_sum += Float.parseFloat(tokens[1]) -1;
				squre_sum += Float.parseFloat(tokens[2]);
				}
			float variance = (squre_sum + (value_sum*value_sum/count_sum)- (2*value_sum*value_sum/count_sum))/count_sum;
			float standardDeviation = (float) Math.sqrt(variance);
			
			context.write(new Text("avg"), new Text(value_sum/count_sum+""));
			context.write(new Text("standardDeviation"), new Text(standardDeviation+""));
			
				
		}

		private float caluculateMinMax(Iterable<Text> values, int i) {
			// TODO Auto-generated method stub
			float minmax =0 , count =0;
			for (Text val : values) {
				String minVal= val.toString();
				if(count == 0 ){
					count++;
					minmax = Float.parseFloat(minVal);
				}
				if( i==0 &&minmax >Float.parseFloat(minVal))
					minmax = Float.parseFloat(minVal);
				else if(i==1 && minmax < Float.parseFloat(minVal))
					minmax = Float.parseFloat(minVal);
			}
			return minmax;
		}
		
	}

	// Driver program
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration(); 
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs(); // get all args
		if (otherArgs.length != 2) {
			System.err.println("Usage: HadoopStatComputation <in> <out>");
			System.exit(2);
		}
		
		// create a job with name "wordcount"
		Job job = new Job(conf, "hadoopstatistics”);
		job.setJarByClass(HadoopStatistic.class);
		job.setMapperClass(Map.class);
		job.setCombinerClass(Combiner.class);
		job.setReducerClass(Reduce.class);

		// Add a combiner here, not required to successfully run the statistics program  

		// set output key type   
		job.setOutputKeyClass(Text.class);
		// set output value type
		job.setOutputValueClass(Text.class);
		//set the HDFS path of the input data
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		// set the HDFS path for the output
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

		//Wait till job completion
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	@Override
	public Set<SourceVersion> getSourceVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int run(InputStream arg0, OutputStream arg1, OutputStream arg2,
			String... arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
}
