package com.xycode.crawlerdefender.persistence.mongo;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.xycode.crawlerdefender.config.Config;
import com.xycode.crawlerdefender.config.ConfigItem;
import com.xycode.crawlerdefender.logcache.LogCachePool;
import com.xycode.crawlerdefender.persistence.IPersistence;
import com.xycode.crawlerdefender.persistence.LogObject;
import com.xycode.crawlerdefender.persistence.mongo.dbobject.DayLogDBObject;
import com.xycode.crawlerdefender.persistence.mongo.dbobject.HourLogDBObject;
import com.xycode.crawlerdefender.persistence.mongo.dbobject.LogDBObject;
import com.xycode.crawlerdefender.persistence.mongo.dbobject.MinuteLogDBObject;

/**
 * mongodb 持久化容器
 * @author xiaoyang
 */
public class MongoPersistence implements IPersistence {
	
	@SuppressWarnings("unused")
	private static MongoClient mongoClient;
	
	 
	
	private static class ClientHolder{
			
		static MongoClient mongoClient;
		
		//连接mongo
		static{
			try {
				
				ConfigItem.MongoConfig mongoConfig=Config.getInstance().getConfigItem().getMongoConfig();
				
				ServerAddress serverAddr=new ServerAddress(mongoConfig.getServerAddress());
				MongoCredential credential = MongoCredential.createMongoCRCredential(mongoConfig.getUsername(), mongoConfig.getDbname(), mongoConfig.getPassword().toCharArray());
				mongoClient = new MongoClient(serverAddr, Arrays.asList(credential));
				
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
			
	}
	
	private volatile static MongoPersistence instance;
	
	private MongoPersistence(){}
	
	public static MongoPersistence getInstance(){
		
		if(instance==null){
			synchronized (MongoPersistence.class) {
				if(instance==null){
					instance=new MongoPersistence();
				}
			}
		}
		
		return instance;
	}
	
	private boolean isInited=false;//是否初始化
	
	@Override
	public synchronized void init()  {
		
		if(!isInited){
			
			DB db= getMongoClient().getDB("crawler_defender");
			
			Date time=new Date();
			
			//创建统计主表
			if(!db.collectionExists("visit_log")){
				System.out.println("colletion visit_log not fonud ");
				db.createCollection("visit_log",null);
				System.out.println("colletion visit_log created ");
				db.getCollection("visit_log").insert(new BasicDBObject("create_time", time));
			}
			
			//创建天统计维度表
			if(!db.collectionExists("visit_log_day")){
				System.out.println("colletion visit_log_day not fonud ");
				db.createCollection("visit_log_day",null);
				System.out.println("colletion visit_log_day created ");
				db.getCollection("visit_log_day").insert(new BasicDBObject("last_syn_time", time));
			}
			
			//创建小时统计维度表
			if(!db.collectionExists("visit_log_hour")){
				System.out.println("colletion visit_log_hour not fonud ");
				db.createCollection("visit_log_hour",null);
				System.out.println("colletion visit_log_hour created ");
				db.getCollection("visit_log_hour").insert(new BasicDBObject("last_syn_time", time));
			}
			
			//创建分统计维度表
			if(!db.collectionExists("visit_log_minute")){
				System.out.println("colletion visit_log_minute not fonud ");
				db.createCollection("visit_log_minute",null);
				System.out.println("colletion visit_log_minute created ");
				db.getCollection("visit_log_minute").insert(new BasicDBObject("last_syn_time", time));
			}
			
			
			//创建小时访问计数表
			//TODO:
			
			//创建天访问计数表
			//TODO:
			
			
			//创建索引
			db.getCollection("visit_log").createIndex(new BasicDBObject("time", -1));
			db.getCollection("visit_log_day").createIndex(new BasicDBObject("time", -1));
			db.getCollection("visit_log_hour").createIndex(new BasicDBObject("time", -1));
			db.getCollection("visit_log_minute").createIndex(new BasicDBObject("time", -1));
			
			ScheduledExecutorService  ses=Executors.newScheduledThreadPool(3);
			
			//从访问主表向分钟维度统计表同步数据
			ses.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					
					synLog(getVisitLogCollection(),getVisitLogMinuteCollection());
					
				}
				
			}, 1, 1, TimeUnit.MINUTES);
			
			//从分钟维度表向小时维度表同步数据
			ses.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					
					synLog(getVisitLogMinuteCollection(),getVisitLogHourCollection());

				}
				
			}, 1, 1, TimeUnit.HOURS);
			
			//从小时维度表天维度表同步数据
			ses.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					
					synLog(getVisitLogHourCollection(),getVisitLogDayCollection());

				}
				
			}, 1, 1, TimeUnit.DAYS);
			
			isInited=true;
		}
		
	}
	
	@Override
	public void destroy() {
		getMongoClient().close();
	}

	@Override
	public void cacheToPersistence(LogCachePool cachePool){
		
		if(!cachePool.isEmpty()){
			
			synchronized (cachePool.getCachedLogs()) {//加锁,保证写入持久化容器的时候缓存池不会有改变
				
				System.out.println("--cacheToPersistence begin--");
				
				BulkWriteOperation builder=getVisitLogCollection().initializeOrderedBulkOperation();
					
				int len=cachePool.size();
				LogObject log;
				
				for(int i=0;i<len;i++){
					log=cachePool.get(i);
					System.out.println(log);
					builder.insert(new LogDBObject(log));
				}
				
				try {
					builder.execute(WriteConcern.UNACKNOWLEDGED);
				} catch (MongoException e) {
					throw new RuntimeException(e);
				}
				
				cachePool.getCachedLogs().clear();
				
				System.out.println("--cacheToPersistence end--");
					
			}
		}
		
	}
	
	@Override
	public int getHourVisitCount(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDayVisitConut(String ip) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public  void insertVisitLog(LogObject log) {
		getVisitLogCollection().insert( new LogDBObject(log) );
	}
	
	public MongoClient getMongoClient(){
		return ClientHolder.mongoClient;
	}
	
	public  DBCollection getVisitLogCollection (){
		return getMongoClient().getDB("crawler_defender").getCollection("visit_log");
	}	
	
	public  DBCollection getVisitLogDayCollection (){
		return getMongoClient().getDB("crawler_defender").getCollection("visit_log_day");
	}
	
	public  DBCollection getVisitLogHourCollection (){
		return getMongoClient().getDB("crawler_defender").getCollection("visit_log_hour");
	}
	
	public  DBCollection getVisitLogMinuteCollection (){
		return getMongoClient().getDB("crawler_defender").getCollection("visit_log_minute");
	}
	
	public  void insertDayVisitLog(LogObject log) {
		getVisitLogCollection().insert( new DayLogDBObject(log) );
	}
	
	public  void insertHourVisitLog(LogObject log) {
		getVisitLogCollection().insert( new HourLogDBObject(log) );
	}
	
	public  void insertMinuteVisitLog(LogObject log) {
		getVisitLogCollection().insert( new MinuteLogDBObject(log) );
	}
	
	/**
	 * 将低维度的表的数据同步到高维度表
	 * @param fromColl 
	 * @param toColl
	 */
	private  void synLog(DBCollection fromColl,DBCollection toColl){
		
		DBObject lastSynTime=null;
		
		try {
			
			lastSynTime= toColl.findOne(new BasicDBObject("last_syn_time",new BasicDBObject("$lt", new Date())));
			
			//上次同步时间之前
			DBObject match = new BasicDBObject("$match", new BasicDBObject("time", new BasicDBObject("$gt", lastSynTime.get("last_syn_time"))));
			
			//分组 : _id : { month: { $month: "$date" }, day: { $dayOfMonth: "$date" }, year: { $year: "$date" } }
			DBObject groupFields=null;
			
			if(toColl.equals(getVisitLogMinuteCollection())){
				groupFields = new BasicDBObject( 
						"_id", 
						new BasicDBObject("year",new BasicDBObject("$year","$time"))
						          .append("month",new BasicDBObject("$month","$time"))
						          .append("day",new BasicDBObject("$dayOfMonth","$time"))
						          .append("hour",new BasicDBObject("$hour","$time"))
						          .append("minute",new BasicDBObject("$minute","$time"))
						          .append("ip", "$ip")
						          .append("url", "$url")
						          .append("type", "$type")
						          .append("user_agent", "$user_agent")
				);
        	}else if(toColl.equals(getVisitLogHourCollection())){
        		groupFields = new BasicDBObject( 
						"_id", 
						new BasicDBObject("year",new BasicDBObject("$year","$time"))
						          .append("month",new BasicDBObject("$month","$time"))
						          .append("day",new BasicDBObject("$dayOfMonth","$time"))
						          .append("hour",new BasicDBObject("$hour","$time"))
						          .append("ip", "$ip")
						          .append("url", "$url")
						          .append("type", "$type")
						          .append("user_agent", "$user_agent")
		          );
        	}else if(toColl.equals(getVisitLogDayCollection())){
        		groupFields = new BasicDBObject( 
						"_id", 
						new BasicDBObject("year",new BasicDBObject("$year","$time"))
						          .append("month",new BasicDBObject("$month","$time"))
						          .append("day",new BasicDBObject("$dayOfMonth","$time"))
						          .append("ip", "$ip")
						          .append("url", "$url")
						          .append("type", "$type")
						          .append("user_agent", "$user_agent")
		          );
        	}
			
	        groupFields.put("count", new BasicDBObject( "$sum", 1));
	        DBObject group = new BasicDBObject("$group", groupFields);
	        
	        //排序
	        DBObject sort=null ;
	        
	        if(toColl.equals(getVisitLogMinuteCollection())){
	        	sort= new BasicDBObject("$sort", new BasicDBObject().append("_id.day", 1).append("_id.hour", 1).append("_id.minute", 1));
        	}else if(toColl.equals(getVisitLogHourCollection())){
	        	sort= new BasicDBObject("$sort", new BasicDBObject().append("_id.day", 1).append("_id.hour", 1));
        	}else if(toColl.equals(getVisitLogDayCollection())){
	        	sort= new BasicDBObject("$sort", new BasicDBObject().append("_id.day", 1));
        	}
	        
	        //字段筛选
	        DBObject fields = new BasicDBObject("url", 1);
	        fields.put("ip", 1);
	        fields.put("user_agent", 1);
	        fields.put("_id", 0);
	        fields.put("type", 1);
	        fields.put("time", 1);
	        DBObject project = new BasicDBObject("$project", fields );
	        
	        //构建管道
	        List<DBObject> pipeline = Arrays.asList(match ,project, group,sort);
	        AggregationOutput output = fromColl.aggregate(pipeline);
	        
	        Date now=new Date(); 
	        
	        Iterator<DBObject> it= output.results().iterator();
	        
	        DBObject rsOjb;
	        Calendar car=new GregorianCalendar();
	        
	        if(it.hasNext()){
				
	        	BulkWriteOperation builder=toColl.initializeOrderedBulkOperation();
	        	
	        	while(it.hasNext()){
	        		
	        		rsOjb=it.next();
		        	
		        	car.clear();
		        	
		        	car.set(Calendar.YEAR, (Integer)((DBObject)rsOjb.get("_id")).get("year"));
		        	car.set(Calendar.MONTH, (Integer)((DBObject)rsOjb.get("_id")).get("month")-1);
		        	car.set(Calendar.DAY_OF_MONTH, (Integer)((DBObject)rsOjb.get("_id")).get("day"));
		        	
		        	if(toColl.equals(getVisitLogHourCollection()) || toColl.equals(getVisitLogMinuteCollection())  ){
		        		car.set(Calendar.HOUR_OF_DAY, (Integer)((DBObject)rsOjb.get("_id")).get("hour"));
		        		car.add(Calendar.HOUR_OF_DAY, 8);//mongo默认为美国标准时间故加八个小时
		        	}
		        	
		        	if(toColl.equals(getVisitLogMinuteCollection())  ){
		        		car.set(Calendar.MINUTE, (Integer)((DBObject)rsOjb.get("_id")).get("minute"));
		        	}
		        	
		        	Date time=car.getTime();
		        	
		        	System.out.println(fromColl+" -> "+toColl+"-"+time+"-"+rsOjb);
	        		
		        	if(toColl.equals(getVisitLogMinuteCollection())){
		        		builder.insert(new MinuteLogDBObject(
			        			new LogObject(
					        			(String)((DBObject)rsOjb.get("_id")).get("url"),
					        			(String)((DBObject)rsOjb.get("_id")).get("ip"), 
					        			(String)((DBObject)rsOjb.get("_id")).get("user_agent"), 
					        			time,
					        			(Integer)((DBObject)rsOjb.get("_id")).get("type"),
					        			(Integer)(rsOjb.get("count"))
					        		)
			        			));
		        	}else if(toColl.equals(getVisitLogHourCollection())){
		        		builder.insert(new HourLogDBObject(
			        			new LogObject(
					        			(String)((DBObject)rsOjb.get("_id")).get("url"),
					        			(String)((DBObject)rsOjb.get("_id")).get("ip"), 
					        			(String)((DBObject)rsOjb.get("_id")).get("user_agent"), 
					        			time,
					        			(Integer)((DBObject)rsOjb.get("_id")).get("type"),
					        			(Integer)(rsOjb.get("count"))
					        		)
			        			));
		        	}else if(toColl.equals(getVisitLogDayCollection())){
		        		builder.insert(new DayLogDBObject(
			        			new LogObject(
					        			(String)((DBObject)rsOjb.get("_id")).get("url"),
					        			(String)((DBObject)rsOjb.get("_id")).get("ip"), 
					        			(String)((DBObject)rsOjb.get("_id")).get("user_agent"), 
					        			time,
					        			(Integer)((DBObject)rsOjb.get("_id")).get("type"),
					        			(Integer)(rsOjb.get("count"))
					        		)
			        			));
		        	}
		        	
		        	
	        		
	        	}
	        	
	        	builder.execute(WriteConcern.UNACKNOWLEDGED);
	        	
	        	toColl.update(lastSynTime, new BasicDBObject("$set", new BasicDBObject("last_syn_time", now)));;

	        }
	        

	        
	        /* mongo 2.6 以后版本才能使用以下代码
	         * AggregationOptions aggregationOptions = AggregationOptions.builder()
	                .outputMode(AggregationOptions.OutputMode.CURSOR)
	                .allowDiskUse(true)
	                .build();
	        
	        Cursor cursor = getVisitLogCollection().aggregate(pipeline, aggregationOptions);
	        while (cursor.hasNext()) {
	            System.out.println(cursor.next());
	        }
	        */
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			//同步失败则直接将上次同步直接置为当前时间
        	toColl.update(lastSynTime, new BasicDBObject("$set", new BasicDBObject("last_syn_time", new Date())));
			
		}
	}
	
	
	public static void main(String[] args) {
		
		MongoPersistence pc=MongoPersistence.getInstance();
		
		Calendar cal=new GregorianCalendar(2015,1,23,8,1,1);
		pc.insertVisitLog(new LogObject("http://localhost:8080/anticrawler/index.jsp", "0:0:0:0:0:0:0:1", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36", cal.getTime(), 0));

		
		/*for(int i=0;i<60;i++){
			Calendar cal=new GregorianCalendar(2015,1,23,8,i,1);
			insertVisitLog(new LogObject("http://localhost:8080/anticrawler/index.jsp", "0:0:0:0:0:0:0:1", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36", cal.getTime(), 0));
		}
		
		for(int i=0;i<12;i++){
			Calendar cal=new GregorianCalendar(2015,1,23,i,9,1);
			insertVisitLog(new LogObject("http://localhost:8080/anticrawler/index.jsp", "0:0:0:0:0:0:0:1", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36", cal.getTime(), 0));
		}
		
		for(int i=1;i<30;i++){
			Calendar cal=new GregorianCalendar(2015,1,23,7,9,1);
			insertVisitLog(new LogObject("http://localhost:8080/anticrawler/index.jsp", "0:0:0:0:0:0:0:1", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36", cal.getTime(), 0));
		}*/
			
		//synLog(getVisitLogCollection(),getVisitLogMinuteCollection());
		//synLog(getVisitLogMinuteCollection(),getVisitLogHourCollection());
		//synLog(getVisitLogHourCollection(),getVisitLogDayCollection());
		
		
		
		/*System.out.println("=============getVisitLogCollection================");
		DBCursor  cursor0=pc.getVisitLogCollection ().find();
		while(cursor0.hasNext()){
			System.out.println(cursor0.next());
		}
		
		System.out.println("=============getVisitLogMinuteCollection================");
		DBCursor  cursor=pc.getVisitLogMinuteCollection ().find();
		while(cursor.hasNext()){
			System.out.println(cursor.next());
		}
		
		System.out.println("=============getVisitLogHourCollection================");
		DBCursor  cursor2=pc.getVisitLogHourCollection ().find();
		while(cursor2.hasNext()){
			System.out.println(cursor2.next());
		}
		
		System.out.println("===============getVisitLogDayCollection==============");
		DBCursor  cursor3=pc.getVisitLogDayCollection ().find();
		while(cursor3.hasNext()){
			System.out.println(cursor3.next());
		}*/
	}

	
	

	
}
